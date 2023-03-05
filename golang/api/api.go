package api

import (
	"net/http"

	"github.com/gin-gonic/gin"

	"getit-api/service"
)

func Create(service service.GetItService) *gin.Engine {
	r := gin.Default()
	userId := "user1"

	r.GET("/v1/lists", func(c *gin.Context) {
		lists, err := service.GetLists(userId)
		if err != nil {
			c.AbortWithError(http.StatusInternalServerError, err)
		}

		c.JSON(http.StatusOK, lists)
	})

	return r
}
