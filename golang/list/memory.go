package list

type memoryListTable struct {
	lists map[string]List
}

func (data *memoryListTable) GetListsForUser(userId string) ([]List, error) {
	filtered := []List{}

	for _, item := range data.lists {
		if item.UserId == userId {
			filtered = append(filtered, item)
		}
	}

	return filtered, nil
}

func (data *memoryListTable) GetList(userId string, listId string) (*List, error) {
	list, found := data.lists[listId]
	if !found || list.UserId != userId {
		return nil, nil
	}

	return &list, nil
}

func (data *memoryListTable) Save(list *List) error {
	data.lists[list.ListId] = *list
	return nil
}

func (data *memoryListTable) Delete(userId string, listId string) error {
	list, found := data.lists[listId]
	if !found || list.UserId != userId {
		return nil
	}

	delete(data.lists, listId)

	return nil
}

func Memory() Dao {
	return &memoryListTable{
		lists: map[string]List{},
	}
}
