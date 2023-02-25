package main

import (
	"context"
	"log"
	"net/http"
	"os"

	"github.com/aws/aws-sdk-go-v2/config"
	"github.com/aws/aws-sdk-go-v2/service/dynamodb"
	"github.com/gin-gonic/gin"

	"getit-api/list"
)

func initService() GetItService {
	cfg, err := config.LoadDefaultConfig(context.TODO())
	if err != nil {
		log.Fatalf("unable to load AWS config, %v", err)
	}
	client := dynamodb.NewFromConfig(cfg)

	return newGetItService(
		list.Table{
			DynamoDbClient: client,
			TableName:      os.Getenv("lists_table_name"),
		},
	)
}

func main() {
	service := initService()
	userId := "user1"

	r := gin.Default()

	r.GET("/v1/lists", func(c *gin.Context) {
		lists, err := service.GetLists(userId)
		if err != nil {
			log.Printf("Error getting lists: %v\n", err)
			c.AbortWithError(http.StatusInternalServerError, err)
		}

		c.JSON(http.StatusOK, lists)
	})

	r.GET("/ping", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{
			"message": "pong",
		})
	})
	r.Run() // listen and serve on 0.0.0.0:8080 (for windows "localhost:8080")
}
