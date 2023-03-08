package service

import (
	"getit-api/list"
)

type GetItService interface {
	CreateList(userId string, data list.Data) (list.List, error)
	GetLists(userId string) ([]list.List, error)
	DeleteList(userId string, listId string) (*list.List, error)
}

func NewGetItService(lists list.Dao) GetItService {
	return &tables{
		Lists: lists,
	}
}
