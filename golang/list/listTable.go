package list

import (
	"context"

	"github.com/aws/aws-sdk-go-v2/aws"
	"github.com/aws/aws-sdk-go-v2/feature/dynamodb/attributevalue"
	"github.com/aws/aws-sdk-go-v2/feature/dynamodb/expression"
	"github.com/aws/aws-sdk-go-v2/service/dynamodb"
	"github.com/aws/aws-sdk-go-v2/service/dynamodb/types"
	"github.com/google/uuid"
)

type table struct {
	client *dynamodb.Client
	name   string
}

type Dao interface {
	GetListsForUser(userId string) ([]Item, error)
	GetList(userId string, listId uuid.UUID) (Item, error)
	Save(list Item) error
	Delete(userId string, listId uuid.UUID) error
}

func CreateDao(DynamoDbClient *dynamodb.Client, TableName string) Dao {
	return &table{
		client: DynamoDbClient,
		name:   TableName,
	}
}

func (table table) GetListsForUser(userId string) ([]Item, error) {
	keyEx := expression.Key("userId").Equal(expression.Value(userId))
	expr, err := expression.NewBuilder().WithKeyCondition(keyEx).Build()
	if err != nil {
		return nil, err
	}

	response, err := table.client.Query(context.TODO(), &dynamodb.QueryInput{
		TableName:                 aws.String(table.name),
		ExpressionAttributeNames:  expr.Names(),
		ExpressionAttributeValues: expr.Values(),
		KeyConditionExpression:    expr.KeyCondition(),
	})
	if err != nil {
		return nil, err
	}

	var shoppingLists []Item
	err = attributevalue.UnmarshalListOfMaps(response.Items, &shoppingLists)

	return shoppingLists, err
}

func (table table) GetList(userId string, listId uuid.UUID) (Item, error) {
	response, err := table.client.GetItem(context.TODO(), &dynamodb.GetItemInput{
		Key:       createKey(userId, listId),
		TableName: aws.String(table.name),
	})
	if err != nil {
		panic(err)
	}

	var list Item
	err = attributevalue.UnmarshalMap(response.Item, &list)

	return list, err
}

func (table table) Save(list Item) error {
	item, err := attributevalue.MarshalMap(list)
	if err != nil {
		panic(err)
	}
	_, err = table.client.PutItem(context.TODO(), &dynamodb.PutItemInput{
		TableName: aws.String(table.name),
		Item:      item,
	})
	return err
}

func (table table) Delete(userId string, listId uuid.UUID) error {
	_, err := table.client.DeleteItem(context.TODO(), &dynamodb.DeleteItemInput{
		TableName: aws.String(table.name),
		Key:       createKey(userId, listId),
	})
	return err
}

func createKey(userId string, listId uuid.UUID) map[string]types.AttributeValue {
	listIdValue, err := attributevalue.Marshal(listId.String())
	if err != nil {
		panic(err)
	}

	userIdValue, err := attributevalue.Marshal(userId)
	if err != nil {
		panic(err)
	}

	return map[string]types.AttributeValue{
		"userId": userIdValue,
		"listId": listIdValue,
	}
}
