package list

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

func (data *memoryListTable) GetList(userId string, listId string) (*List, error) {
	for _, item := range data.items {
		if item.UserId == userId && item.ListId == listId {
			return &item, nil
		}
	}

	return nil, nil
}

func (data *memoryListTable) Save(list *List) error {
	data.items = append(data.items, *list)
	return nil
}

func (data *memoryListTable) Delete(userId string, listId string) error {
	for i, item := range data.items {
		if item.UserId == userId && item.ListId == listId {
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
