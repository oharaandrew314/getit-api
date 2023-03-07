package main

import (
	"context"
	"log"
	"net/url"
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

	issuerUrl, err := url.Parse("https://accounts.google.com")
	if err != nil {
		log.Fatalf("Unable to parse issuer url, %v", err)
	}

	jwtAudience := os.Getenv("jwt_audience")

	validator, err := api.CreateJwtValidator(issuerUrl, "accounts.google.com", jwtAudience)
	if err != nil {
		log.Fatalf("Unable to create JWT validator, %v", err)
	}

	r := api.Create(service, validator)
	r.Run() // listen and serve on 0.0.0.0:8080
}
