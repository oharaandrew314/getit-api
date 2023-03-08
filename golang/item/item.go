package item

type Item struct {
	ListId    string `json:"listId"`
	Name      string `json:"name"`
	ItemId    string `json:"itemId"`
	Completed bool   `json:"completed"`
}

type Data struct {
	Name      string `json:"name"`
	Completed bool   `json:"completed"`
}
