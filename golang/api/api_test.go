package api_test

import (
	"bytes"
	"context"
	"encoding/json"
	"log"
	"net/http"
	"net/http/httptest"
	"testing"

	jwtmiddleware "github.com/auth0/go-jwt-middleware/v2"
	"github.com/auth0/go-jwt-middleware/v2/validator"
	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"

	"getit-api/api"
	"getit-api/item"
	"getit-api/list"
	"getit-api/service"
)

type testApp struct {
	lists   list.Dao
	items   item.Dao
	service service.GetItService
	router  *gin.Engine
}

func newTestApp() testApp {
	listDao := list.Memory()
	itemDao := item.Memory()
	getItService := service.NewGetItService(listDao, itemDao)
	router := api.Create(getItService)

	return testApp{
		lists:   listDao,
		items:   itemDao,
		service: getItService,
		router:  router,
	}
}

func withUser(request *http.Request, userId string) *http.Request {
	claims := &validator.ValidatedClaims{
		RegisteredClaims: validator.RegisteredClaims{
			Subject: userId,
		},
		CustomClaims: nil,
	}

	ctx := context.WithValue(request.Context(), jwtmiddleware.ContextKey{}, claims)

	return request.WithContext(ctx)
}

func (app testApp) execute(req *http.Request, userId string) *httptest.ResponseRecorder {
	w := httptest.NewRecorder()
	app.router.ServeHTTP(w, withUser(req, userId))
	return w
}

func (app testApp) newList(userId string, name string) *list.List {
	data := list.Data{
		Name: name,
	}

	list, err := app.service.CreateList(userId, &data)
	if err != nil {
		log.Fatalf("Error creating list: %v", err)
	}

	return list
}

func (app testApp) newItem(list *list.List, name string, completed bool) *item.Item {
	data := item.Data{
		Name:      name,
		Completed: completed,
	}

	item, err := app.service.NewItem(list.UserId, list.ListId, &data)
	if err != nil {
		log.Fatalf("Error creating item: %v", err)
	}

	return item
}

var user1 = "user123"

func TestLists_GetEmpty(t *testing.T) {
	app := newTestApp()
	req, _ := http.NewRequest("GET", "/v1/lists", nil)
	resp := app.execute(req, user1)

	assert.Equal(t, 200, resp.Code)
	assert.Equal(t, "[]", resp.Body.String())
}

func TestLists_GetMany(t *testing.T) {
	app := newTestApp()
	list1 := app.newList(user1, "list1")
	list2 := app.newList(user1, "list2")

	req, _ := http.NewRequest("GET", "/v1/lists", nil)
	resp := app.execute(req, user1)

	assert.Equal(t, 200, resp.Code)

	var actual []list.List
	err := json.Unmarshal(resp.Body.Bytes(), &actual)
	assert.Nil(t, err)
	assert.Equal(t, []list.List{*list1, *list2}, actual)
}

func TestLists_Create(t *testing.T) {
	app := newTestApp()

	body, _ := json.Marshal(map[string]string{
		"name": "foo",
	})

	req, _ := http.NewRequest("POST", "/v1/lists", bytes.NewBuffer(body))
	resp := app.execute(req, user1)

	assert.Equal(t, 200, resp.Code)

	var actual list.List
	err := json.Unmarshal(resp.Body.Bytes(), &actual)
	assert.Nil(t, err)

	assert.Equal(t, "foo", actual.Name)
	assert.Equal(t, user1, actual.UserId)
	assert.NotNil(t, actual.ListId)

	retrieved, _ := app.lists.GetList(user1, actual.ListId)
	assert.Equal(t, &actual, retrieved)
}

func TestList_Delete_NotFound(t *testing.T) {
	app := newTestApp()

	req, _ := http.NewRequest("DELETE", "/v1/lists/foo", nil)
	resp := app.execute(req, user1)

	assert.Equal(t, 404, resp.Code)
}

func TestList_Delete_Deleted(t *testing.T) {
	app := newTestApp()

	list1 := app.newList(user1, "list1")
	list2 := app.newList(user1, "list2")

	req, _ := http.NewRequest("DELETE", "/v1/lists/"+list1.ListId, nil)
	resp := app.execute(req, user1)

	assert.Equal(t, 200, resp.Code)

	var deleted list.List
	err := json.Unmarshal(resp.Body.Bytes(), &deleted)
	assert.Nil(t, err)
	assert.Equal(t, *list1, deleted)

	retrieved, _ := app.lists.GetListsForUser(user1)
	assert.Equal(t, []list.List{*list2}, retrieved)
}

func TestItem_ForList_ListNotFound(t *testing.T) {
	app := newTestApp()

	req, _ := http.NewRequest("GET", "/v1/lists/foo/items", nil)
	resp := app.execute(req, user1)

	// verify status code
	assert.Equal(t, 404, resp.Code)
}

func TestItem_ForList_Success(t *testing.T) {
	app := newTestApp()
	list1 := app.newList(user1, "list1")
	item1 := app.newItem(list1, "item1", false)
	item2 := app.newItem(list1, "item2", true)

	req, _ := http.NewRequest("GET", "/v1/lists/"+list1.ListId+"/items", nil)
	resp := app.execute(req, user1)

	// verify status code
	assert.Equal(t, 200, resp.Code)

	// verify response body
	var actual []item.Item
	err := json.Unmarshal(resp.Body.Bytes(), &actual)
	assert.Nil(t, err)
	assert.Equal(t, []item.Item{*item1, *item2}, actual)
}

func TestItem_Create_ListNotFound(t *testing.T) {
	app := newTestApp()

	body, _ := json.Marshal(map[string]string{
		"name": "foo",
	})

	req, _ := http.NewRequest("POST", "/v1/lists/foo/items", bytes.NewBuffer(body))
	resp := app.execute(req, user1)

	assert.Equal(t, 404, resp.Code)
}

func TestItem_Create_BadRequest(t *testing.T) {
	app := newTestApp()

	req, _ := http.NewRequest("POST", "/v1/lists/foo/items", nil)
	resp := app.execute(req, user1)

	assert.Equal(t, 400, resp.Code)
}

func TestItem_Create(t *testing.T) {
	app := newTestApp()
	list1 := app.newList(user1, "list1")

	body, _ := json.Marshal(map[string]string{
		"name": "foo",
	})

	req, _ := http.NewRequest("POST", "/v1/lists/"+list1.ListId+"/items", bytes.NewBuffer(body))
	resp := app.execute(req, user1)

	// verify success
	assert.Equal(t, 200, resp.Code)

	// verify response body
	var actual item.Item
	err := json.Unmarshal(resp.Body.Bytes(), &actual)
	assert.Nil(t, err)
	assert.Equal(t, "foo", actual.Name)
	assert.Equal(t, list1.ListId, actual.ListId)
	assert.NotNil(t, actual.ItemId)

	// Verify saved to dao
	retrieved, _ := app.items.ForList(actual.ListId)
	assert.Equal(t, []item.Item{actual}, retrieved)
}

func TestItem_Update_ListNotFound(t *testing.T) {
	app := newTestApp()
	list1 := app.newList(user1, "list1")
	item1 := app.newItem(list1, "item1", false)

	body, _ := json.Marshal(map[string]any{
		"name":      "item1Updated",
		"completed": true,
	})

	req, _ := http.NewRequest("PUT", "/v1/lists/foo/items/"+item1.ItemId, bytes.NewBuffer(body))
	resp := app.execute(req, user1)

	// verify success
	assert.Equal(t, 404, resp.Code)
}

func TestItem_Update_ItemNotFound(t *testing.T) {
	app := newTestApp()
	list1 := app.newList(user1, "list1")

	body, _ := json.Marshal(map[string]any{
		"name":      "item1Updated",
		"completed": true,
	})

	req, _ := http.NewRequest("PUT", "/v1/lists/"+list1.ListId+"/items/foo", bytes.NewBuffer(body))
	resp := app.execute(req, user1)

	// verify success
	assert.Equal(t, 404, resp.Code)
}

func TestItem_Update_BadRequest(t *testing.T) {
	app := newTestApp()
	list1 := app.newList(user1, "list1")
	item1 := app.newItem(list1, "item1", false)

	req, _ := http.NewRequest("PUT", "/v1/lists/"+item1.ListId+"/items/"+item1.ItemId, nil)
	resp := app.execute(req, user1)

	// verify success
	assert.Equal(t, 400, resp.Code)
}

func TestItem_Update_Success(t *testing.T) {
	app := newTestApp()
	list1 := app.newList(user1, "list1")
	item1 := app.newItem(list1, "item1", false)

	body, _ := json.Marshal(map[string]any{
		"name":      "item1Updated",
		"completed": true,
	})

	req, _ := http.NewRequest("PUT", "/v1/lists/"+item1.ListId+"/items/"+item1.ItemId, bytes.NewBuffer(body))
	resp := app.execute(req, user1)

	// verify success
	assert.Equal(t, 200, resp.Code)

	expected := item1
	expected.Name = "item1Updated"
	expected.Completed = true

	// verify response body
	var actual item.Item
	err := json.Unmarshal(resp.Body.Bytes(), &actual)
	assert.Nil(t, err)
	assert.Equal(t, *expected, actual)

	// Verify saved to dao
	retrieved, _ := app.items.ForList(actual.ListId)
	assert.Equal(t, []item.Item{*expected}, retrieved)
}

func TestItem_Delete_ItemNotFound(t *testing.T) {
	app := newTestApp()
	list1 := app.newList(user1, "list1")

	req, _ := http.NewRequest("DELETE", "/v1/lists/"+list1.ListId+"/items/foo", nil)
	resp := app.execute(req, user1)

	// verify success
	assert.Equal(t, 404, resp.Code)
}

func TestItem_Delete_Success(t *testing.T) {
	app := newTestApp()
	list1 := app.newList(user1, "list1")
	item1 := app.newItem(list1, "item1", false)
	item2 := app.newItem(list1, "item2", false)

	req, _ := http.NewRequest("DELETE", "/v1/lists/"+item1.ListId+"/items/"+item1.ItemId, nil)
	resp := app.execute(req, user1)

	// verify success
	assert.Equal(t, 200, resp.Code)

	// verify response body
	var actual item.Item
	err := json.Unmarshal(resp.Body.Bytes(), &actual)
	assert.Nil(t, err)
	assert.Equal(t, *item1, actual)

	// Verify removed from dao
	retrieved, _ := app.items.ForList(list1.ListId)
	assert.Equal(t, []item.Item{*item2}, retrieved)
}
