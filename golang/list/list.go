package list

import (
	"log"

	"github.com/google/uuid"
)

type Item struct {
	UserId string `json:"userId"`
	ListId string `json:"listId"`
	Name   string `json:"name"`
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
		UserId: userId,
		ListId: listId.String(),
		Name:   data.Name,
	}
}
