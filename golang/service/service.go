package service

import (
	"getit-api/item"
	"getit-api/list"

	"github.com/google/uuid"
)

type tables struct {
	Lists list.Dao
	Items item.Dao
}

func (tables *tables) CreateList(userId string, data *list.Data) (*list.List, error) {
	id, err := uuid.NewRandom()
	if err != nil {
		return nil, err
	}

	created := &list.List{
		UserId: userId,
		ListId: id.String(),
		Name:   data.Name,
	}

	err = tables.Lists.Save(created)
	return created, err
}

func (tables *tables) GetLists(userId string) ([]list.List, error) {
	return tables.Lists.GetListsForUser(userId)
}

func (tables *tables) DeleteList(userId string, listId string) (*list.List, error) {
	list, err := tables.Lists.GetList(userId, listId)
	if list == nil {
		return list, err
	}

	err = tables.Lists.Delete(userId, listId)
	return list, err
}

func (t *tables) ItemsForList(userId string, listId string) ([]item.Item, error) {
	list, err := t.Lists.GetList(userId, listId)
	if err != nil || list == nil {
		return nil, err
	}

	items, err := t.Items.ForList(listId)
	return items, err
}

func (t *tables) NewItem(userId string, listId string, data *item.Data) (*item.Item, error) {
	list, err := t.Lists.GetList(userId, listId)
	if err != nil || list == nil {
		return nil, err
	}

	id, err := uuid.NewRandom()
	if err != nil {
		return nil, err
	}

	created := &item.Item{
		ListId:    listId,
		ItemId:    id.String(),
		Name:      data.Name,
		Completed: data.Completed,
	}

	err = t.Items.Save(created)

	return created, err
}

func (t *tables) UpdateItem(userId string, listId string, itemId string, data *item.Data) (*item.Item, error) {
	list, err := t.Lists.GetList(userId, listId)
	if err != nil || list == nil {
		return nil, err
	}

	item, err := t.Items.Get(listId, itemId)
	if err != nil || item == nil {
		return nil, err
	}

	item.Name = data.Name
	item.Completed = data.Completed

	err = t.Items.Save(item)
	return item, err
}

func (t *tables) DeleteItem(userId string, listId string, itemId string) (*item.Item, error) {
	list, err := t.Lists.GetList(userId, listId)
	if err != nil || list == nil {
		return nil, err
	}

	item, err := t.Items.Get(listId, itemId)
	if err != nil || item == nil {
		return nil, err
	}

	err = t.Items.Delete(listId, itemId)
	return item, err
}
