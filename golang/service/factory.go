package service

import (
	"getit-api/item"
	"getit-api/list"
)

type GetItService interface {
	CreateList(userId string, data *list.Data) (*list.List, error)
	GetLists(userId string) ([]list.List, error)
	DeleteList(userId string, listId string) (*list.List, error)
	NewItem(userId string, listId string, data *item.Data) (*item.Item, error)
	UpdateItem(userId string, listId string, itemId string, data *item.Data) (*item.Item, error)
	DeleteItem(userId string, listId string, itemId string) (*item.Item, error)
}

func NewGetItService(lists list.Dao, items item.Dao) GetItService {
	return &tables{
		Lists: lists,
		Items: items,
	}
}
