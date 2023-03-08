package service

import (
	"github.com/google/uuid"

	"getit-api/list"
)

type tables struct {
	Lists list.Dao
}

func (tables tables) CreateList(userId string, data list.Data) (list.List, error) {
	item := data.ToList(userId)
	err := tables.Lists.Save(item)
	return item, err
}

func (tables tables) GetLists(userId string) ([]list.List, error) {
	return tables.Lists.GetListsForUser(userId)
}

func (tables tables) DeleteList(userId string, listId uuid.UUID) error {
	return tables.Lists.Delete(userId, listId)
}
