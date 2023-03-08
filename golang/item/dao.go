package item

type Dao interface {
	ForList(listId string) ([]Item, error)
	Get(listId string, itemId string) (*Item, error)
	Save(item *Item) error
	Delete(listId string, itemId string) error
}
