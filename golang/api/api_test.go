package api_test

import (
	"context"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	jwtmiddleware "github.com/auth0/go-jwt-middleware/v2"
	"github.com/auth0/go-jwt-middleware/v2/validator"
	"github.com/stretchr/testify/assert"

	"getit-api/api"
	"getit-api/list"
	"getit-api/service"
)

var listDao = list.Memory()
var getItService = service.NewGetItService(listDao)
var router = api.Create(getItService)

var user1 = "user123"

var list1 = list.List{
	UserId: user1,
	ListId: "list1",
	Name:   "List One",
}

var list2 = list.List{
	UserId: user1,
	ListId: "list2",
	Name:   "List Two",
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

func execute(request *http.Request, userId string) *httptest.ResponseRecorder {
	w := httptest.NewRecorder()
	router.ServeHTTP(w, withUser(request, "user123"))
	return w
}

func TestLists_GetEmpty(t *testing.T) {
	req, _ := http.NewRequest("GET", "/v1/lists", nil)
	resp := execute(req, "user123")

	assert.Equal(t, 200, resp.Code)
	assert.Equal(t, "[]", resp.Body.String())
}

func TestLists_GetMany(t *testing.T) {
	listDao.Save(list1)
	listDao.Save(list2)

	req, _ := http.NewRequest("GET", "/v1/lists", nil)
	resp := execute(req, "user123")

	assert.Equal(t, 200, resp.Code)

	var actual []list.List
	err := json.Unmarshal(resp.Body.Bytes(), &actual)
	assert.Nil(t, err)
	assert.Equal(t, []list.List{list1, list2}, actual)
}
