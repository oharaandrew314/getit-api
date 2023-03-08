package list

import (
	"github.com/google/uuid"
)

type memoryListTable struct {
	items []List
}

func (data *memoryListTable) GetListsForUser(userId string) ([]List, error) {
	filtered := []List{}

	for _, item := range data.items {
		if item.UserId == userId {
			filtered = append(filtered, item)
		}
	}

	return filtered, nil
}

func (data *memoryListTable) GetList(userId string, listId uuid.UUID) (List, error) {
	for _, item := range data.items {
		if item.UserId == userId && item.ListId == listId.String() {
			return item, nil
		}
	}

	return List{}, nil
}

func (data *memoryListTable) Save(list List) error {
	data.items = append(data.items, list)
	return nil
}

func (data *memoryListTable) Delete(userId string, listId uuid.UUID) error {
	for i, item := range data.items {
		if item.UserId == userId && item.ListId == listId.String() {
			data.items = append(data.items[:i], data.items[i+1:]...)
		}
	}

	return nil
}

func Memory() Dao {
	return &memoryListTable{
		items: []List{},
	}
}
