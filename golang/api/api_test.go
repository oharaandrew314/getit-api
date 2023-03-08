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
	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"

	"getit-api/api"
	"getit-api/list"
	"getit-api/service"
)

type testApp struct {
	lists  list.Dao
	router *gin.Engine
}

func newTestApp() testApp {
	listDao := list.Memory()
	var getItService = service.NewGetItService(listDao)
	router := api.Create(getItService)

	return testApp{
		lists:  listDao,
		router: router,
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

func (app testApp) newList(name string) *list.List {
	id, err := uuid.NewRandom()
	if err != nil {
		log.Fatalf("Error creating uuid: %v", err)
	}

	created := &list.List{
		UserId: user1,
		ListId: id.String(),
		Name:   name,
	}
	app.lists.Save(created)
	return created
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
	list1 := app.newList("list1")
	list2 := app.newList("list2")

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

	list1 := app.newList("list1")
	list2 := app.newList("list2")

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
