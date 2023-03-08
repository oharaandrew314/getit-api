package list

import "github.com/google/uuid"

type Dao interface {
	GetListsForUser(userId string) ([]List, error)
	GetList(userId string, listId uuid.UUID) (List, error)
	Save(list List) error
	Delete(userId string, listId uuid.UUID) error
}
