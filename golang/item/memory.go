package item

type memoryItems struct {
	items map[string]Item
}

func Memory() Dao {
	return &memoryItems{
		items: map[string]Item{},
	}
}

func (d *memoryItems) ForList(listId string) ([]Item, error) {
	filtered := []Item{}

	for _, item := range d.items {
		if item.ListId == listId {
			filtered = append(filtered, item)
		}
	}

	return filtered, nil
}

func (d *memoryItems) Get(listId string, itemId string) (*Item, error) {
	item := d.items[itemId]
	if item.ListId != listId {
		return nil, nil
	}

	return &item, nil
}

func (d *memoryItems) Save(item *Item) error {
	d.items[item.ItemId] = *item
	return nil
}

func (d *memoryItems) Delete(listId string, itemId string) error {
	item, found := d.items[itemId]
	if !found || item.ListId != listId {
		return nil
	}

	delete(d.items, itemId)

	return nil
}
