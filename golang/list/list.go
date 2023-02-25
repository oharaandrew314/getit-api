package list

import (
	"log"

	"github.com/google/uuid"
)

type Item struct {
	userId string
	listId uuid.UUID
	name   string
}

type Data struct {
	Name string
}

func (data Data) ToList(userId string) Item {
	listId, err := uuid.NewRandom()
	if err != nil {
		log.Fatalf("Error creating listId: %v", err)
	}

	return Item{
		userId: userId,
		listId: listId,
		name:   data.Name,
	}
}
