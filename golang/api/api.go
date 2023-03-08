package api

import (
	"log"
	"net/http"

	jwtmiddleware "github.com/auth0/go-jwt-middleware/v2"
	"github.com/auth0/go-jwt-middleware/v2/validator"
	"github.com/gin-gonic/gin"

	"getit-api/service"
)

func getUserId(c *gin.Context) string {
	claims := c.Request.Context().Value(jwtmiddleware.ContextKey{}).(*validator.ValidatedClaims)
	return claims.RegisteredClaims.Subject
}

func Create(service service.GetItService) *gin.Engine {
	r := gin.Default()

	r.GET("/v1/lists", func(c *gin.Context) {
		log.Println("bar")
		userId := getUserId(c)

		lists, err := service.GetLists(userId)
		if err != nil {
			c.AbortWithError(http.StatusInternalServerError, err)
		}

		c.JSON(http.StatusOK, lists)
	})

	return r
}
