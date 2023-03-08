package service

import (
	"getit-api/list"

	"github.com/google/uuid"
)

type GetItService interface {
	CreateList(userId string, data list.Data) (list.List, error)
	GetLists(userId string) ([]list.List, error)
	DeleteList(userId string, listId uuid.UUID) error
}

func NewGetItService(lists list.Dao) GetItService {
	return &tables{
		Lists: lists,
	}
}
