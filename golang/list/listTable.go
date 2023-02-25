package list

import (
	"context"
	"log"

	"github.com/aws/aws-sdk-go-v2/aws"
	"github.com/aws/aws-sdk-go-v2/feature/dynamodb/attributevalue"
	"github.com/aws/aws-sdk-go-v2/feature/dynamodb/expression"
	"github.com/aws/aws-sdk-go-v2/service/dynamodb"
	"github.com/aws/aws-sdk-go-v2/service/dynamodb/types"
	"github.com/google/uuid"
)

type Table struct {
	DynamoDbClient *dynamodb.Client
	TableName      string
}

func (table Table) GetListsForUser(userId string) ([]Item, error) {
	keyEx := expression.Key("userId").Equal(expression.Value(userId))
	expr, err := expression.NewBuilder().WithKeyCondition(keyEx).Build()
	if err != nil {
		log.Printf("Couldn't build expression for query. Here's why: %v\n", err)
		return nil, err
	}

	response, err := table.DynamoDbClient.Query(context.TODO(), &dynamodb.QueryInput{
		TableName:                 aws.String(table.TableName),
		ExpressionAttributeNames:  expr.Names(),
		ExpressionAttributeValues: expr.Values(),
		KeyConditionExpression:    expr.KeyCondition(),
	})
	if err != nil {
		log.Printf("Error getting lists for user %v. Here's why: %v\n", userId, err)
		return nil, err
	}

	var shoppingLists []Item
	err = attributevalue.UnmarshalListOfMaps(response.Items, &shoppingLists)
	if err != nil {
		log.Printf("Couldn't unmarshal query response. Here's why: %v\n", err)
		return nil, err
	}

	return shoppingLists, nil
}

func (table Table) Save(list Item) error {
	item, err := attributevalue.MarshalMap(list)
	if err != nil {
		panic(err)
	}
	_, err = table.DynamoDbClient.PutItem(context.TODO(), &dynamodb.PutItemInput{
		TableName: aws.String(table.TableName),
		Item:      item,
	})
	if err != nil {
		log.Printf("Couldn't add item to table. Here's why: %v\n", err)
	}
	return err
}

func (table Table) Delete(listId uuid.UUID) error {
	listIdValue, err := attributevalue.Marshal(listId.String())
	if err != nil {
		panic(err)
	}

	_, err = table.DynamoDbClient.DeleteItem(context.TODO(), &dynamodb.DeleteItemInput{
		TableName: aws.String(table.TableName),
		Key: map[string]types.AttributeValue{
			"listId": listIdValue,
		},
	})
	if err != nil {
		log.Printf("Couldn't delete %v from the table. Here's why: %v\n", listId, err)
	}

	return err
}
