namespace getit_api.dao
{
    public readonly record struct ShoppingList(
        string userId,
        Guid listId,
        string name
    );

    public readonly record struct ShoppingListData(
        string name
    );

    public readonly record struct ShoppingItem(
        Guid listId,
        Guid itemId,
        string name,
        bool completed
    );

    public readonly record struct ShoppingItemData(
        string name,
        bool completed
    );
}
