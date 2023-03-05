package main

import (
	"context"
	"log"
	"os"

	"github.com/aws/aws-sdk-go-v2/config"
	"github.com/aws/aws-sdk-go-v2/service/dynamodb"

	"getit-api/api"
	"getit-api/list"
	"getit-api/service"
)

func initService() service.GetItService {
	cfg, err := config.LoadDefaultConfig(context.TODO())
	if err != nil {
		log.Fatalf("unable to load AWS config, %v", err)
	}
	client := dynamodb.NewFromConfig(cfg)

	return service.NewGetItService(
		list.CreateDao(client, os.Getenv("lists_table_name")),
	)
}

func main() {
	service := initService()

	r := api.Create(service)
	r.Run() // listen and serve on 0.0.0.0:8080
}
