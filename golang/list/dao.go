package list

type Dao interface {
	GetListsForUser(userId string) ([]List, error)
	GetList(userId string, listId string) (*List, error)
	Save(list *List) error
	Delete(userId string, listId string) error
}
