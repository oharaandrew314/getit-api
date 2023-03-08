package main

import (
	"context"
	"log"
	"net/url"
	"os"

	jwtmiddleware "github.com/auth0/go-jwt-middleware/v2"
	"github.com/aws/aws-sdk-go-v2/config"
	"github.com/aws/aws-sdk-go-v2/service/dynamodb"
	adapter "github.com/gwatts/gin-adapter"

	"getit-api/api"
	"getit-api/item"
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
		list.NewDynamo(client, os.Getenv("lists_table_name")),
		item.NewDynamo(client, os.Getenv("items_table_name")),
	)
}

func initJwt() *jwtmiddleware.JWTMiddleware {
	oidcUrl, err := url.Parse("https://accounts.google.com")
	if err != nil {
		log.Fatalf("Unable to parse issuer url, %v", err)
	}

	jwtAudience := os.Getenv("jwt_audience")
	jwtIssuer := "accounts.google.com"

	jwt, err := api.CreateJwtValidator(oidcUrl, jwtIssuer, jwtAudience)
	if err != nil {
		log.Fatalf("Unable to create JWT validator, %v", err)
	}

	return jwtmiddleware.New(jwt.ValidateToken)
}

func main() {
	service := initService()
	jwt := initJwt()

	r := api.Create(service, adapter.Wrap(jwt.CheckJWT))
	r.Run() // listen and serve on 0.0.0.0:8080
}
