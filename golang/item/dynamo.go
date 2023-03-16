package item

import (
	"context"

	"github.com/aws/aws-sdk-go-v2/aws"
	"github.com/aws/aws-sdk-go-v2/feature/dynamodb/attributevalue"
	"github.com/aws/aws-sdk-go-v2/feature/dynamodb/expression"
	"github.com/aws/aws-sdk-go-v2/service/dynamodb"
	"github.com/aws/aws-sdk-go-v2/service/dynamodb/types"
)

type dynamoItemTable struct {
	client *dynamodb.Client
	name   string
}

func NewDynamo(DynamoDbClient *dynamodb.Client, TableName string) Dao {
	return &dynamoItemTable{
		client: DynamoDbClient,
		name:   TableName,
	}
}

func (table *dynamoItemTable) ForList(listId string) ([]Item, error) {
	keyEx := expression.Key("listId").Equal(expression.Value(listId))
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

	var items []Item
	err = attributevalue.UnmarshalListOfMaps(response.Items, &items)
	return items, err
}

func (table *dynamoItemTable) Get(listId string, itemId string) (*Item, error) {
	response, err := table.client.GetItem(context.TODO(), &dynamodb.GetItemInput{
		Key:       createKey(listId, itemId),
		TableName: aws.String(table.name),
	})
	if err != nil {
		panic(err)
	}

	var item Item
	err = attributevalue.UnmarshalMap(response.Item, &item)
	return &item, err
}

func (table *dynamoItemTable) Save(item *Item) error {
	dynamoItem, err := attributevalue.MarshalMap(item)
	if err != nil {
		panic(err)
	}
	_, err = table.client.PutItem(context.TODO(), &dynamodb.PutItemInput{
		TableName: aws.String(table.name),
		Item:      dynamoItem,
	})
	return err
}

func (table *dynamoItemTable) Delete(listId string, itemId string) error {
	_, err := table.client.DeleteItem(context.TODO(), &dynamodb.DeleteItemInput{
		TableName: aws.String(table.name),
		Key:       createKey(listId, itemId),
	})
	return err
}

func createKey(listId string, itemId string) map[string]types.AttributeValue {
	listIdValue, err := attributevalue.Marshal(listId)
	if err != nil {
		panic(err)
	}

	itemIdValue, err := attributevalue.Marshal(itemId)
	if err != nil {
		panic(err)
	}

	return map[string]types.AttributeValue{
		"listId": listIdValue,
		"itemId": itemIdValue,
	}
}
