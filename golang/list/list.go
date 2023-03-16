package list

type List struct {
	UserId string `json:"userId"`
	ListId string `json:"listId"`
	Name   string `json:"name"`
}

type Data struct {
	Name string `json:"name"`
}
