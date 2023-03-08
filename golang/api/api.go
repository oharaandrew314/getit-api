package api

import (
	"net/http"

	jwtmiddleware "github.com/auth0/go-jwt-middleware/v2"
	"github.com/auth0/go-jwt-middleware/v2/validator"
	"github.com/gin-gonic/gin"

	"getit-api/item"
	"getit-api/list"
	"getit-api/service"
)

func getUserId(c *gin.Context) string {
	claims := c.Request.Context().Value(jwtmiddleware.ContextKey{}).(*validator.ValidatedClaims)
	return claims.RegisteredClaims.Subject
}

func Create(service service.GetItService) *gin.Engine {
	r := gin.Default()

	r.GET("/v1/lists", func(c *gin.Context) {
		userId := getUserId(c)

		lists, err := service.GetLists(userId)
		if err != nil {
			c.AbortWithError(http.StatusInternalServerError, err)
		}

		c.JSON(http.StatusOK, lists)
	})

	r.POST("/v1/lists", func(c *gin.Context) {
		userId := getUserId(c)

		var data list.Data
		err := c.BindJSON(&data)
		if err != nil {
			c.AbortWithError(http.StatusInternalServerError, err)
		}

		list, err := service.CreateList(userId, &data)
		if err != nil {
			c.AbortWithError(http.StatusBadRequest, err)
		}

		c.JSON(http.StatusOK, list)
	})

	r.DELETE("/v1/lists/:listId", func(c *gin.Context) {
		userId := getUserId(c)
		listId := c.Param("listId")

		list, err := service.DeleteList(userId, listId)
		if err != nil {
			c.AbortWithError(http.StatusInternalServerError, err)
		}

		if list == nil {
			c.Status(http.StatusNotFound)
		} else {
			c.JSON(http.StatusOK, list)
		}
	})

	r.POST("/v1/lists/:listId/items", func(c *gin.Context) {
		userId := getUserId(c)
		listId := c.Param("listId")

		var data item.Data
		err := c.BindJSON(&data)
		if err != nil {
			c.AbortWithError(http.StatusBadRequest, err)
		}

		item, err := service.NewItem(userId, listId, &data)

		switch {
		case err != nil:
			c.AbortWithError(http.StatusInternalServerError, err)
		case item == nil:
			c.Status(http.StatusNotFound)
		default:
			c.JSON(http.StatusOK, item)
		}
	})

	r.PUT("/v1/lists/:listId/items/:itemId", func(c *gin.Context) {
		userId := getUserId(c)
		listId := c.Param("listId")
		itemId := c.Param("itemId")

		var data item.Data
		err := c.BindJSON(&data)
		if err != nil {
			c.AbortWithError(http.StatusBadRequest, err)
		}

		item, err := service.UpdateItem(userId, listId, itemId, &data)

		switch {
		case err != nil:
			c.AbortWithError(http.StatusInternalServerError, err)
		case item == nil:
			c.Status(http.StatusNotFound)
		default:
			c.JSON(http.StatusOK, item)
		}
	})

	r.DELETE("/v1/lists/:listId/items/:itemId", func(c *gin.Context) {
		userId := getUserId(c)
		listId := c.Param("listId")
		itemId := c.Param("itemId")

		item, err := service.DeleteItem(userId, listId, itemId)
		switch {
		case err != nil:
			c.AbortWithError(http.StatusInternalServerError, err)
		case item == nil:
			c.Status(http.StatusNotFound)
		default:
			c.JSON(http.StatusOK, item)
		}
	})

	return r
}
