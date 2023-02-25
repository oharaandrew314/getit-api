package main

import (
	"github.com/google/uuid"

	"getit-api/list"
)

type tables struct {
	Lists list.Table
}

type GetItService interface {
	CreateList(userId string, data list.Data) (list.Item, error)
	GetLists(userId string) ([]list.Item, error)
	DeleteList(listId uuid.UUID) error
}

func newGetItService(lists list.Table) GetItService {
	return &tables{
		Lists: lists,
	}
}

func (tables tables) CreateList(userId string, data list.Data) (list.Item, error) {
	item := data.ToList(userId)
	err := tables.Lists.Save(item)
	return item, err
}

func (tables tables) GetLists(userId string) ([]list.Item, error) {
	return tables.Lists.GetListsForUser(userId)
}

func (tables tables) DeleteList(listId uuid.UUID) error {
	return tables.Lists.Delete(listId)
}
