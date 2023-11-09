namespace getit_api.dao
{
    public interface ShoppingListDao {
        List<ShoppingList> getLists(string userId);
        ShoppingList? getList(string userId, Guid listId);

        void add(ShoppingList list);
        void remove(ShoppingList list);
    }

    public class MemoryShoppingListDao: ShoppingListDao {

        private readonly List<ShoppingList> lists = new();

        List<ShoppingList> ShoppingListDao.getLists(string userId)
        {
            return lists.FindAll(item => item.userId == userId);
        }

        void ShoppingListDao.add(ShoppingList list)
        {
            lists.Add(list);
        }

        ShoppingList? ShoppingListDao.getList(string userId, Guid listId)
        {
            return lists.Find(item => item.userId == userId && item.listId == listId);
        }

        void ShoppingListDao.remove(ShoppingList list)
        {
            lists.Remove(list);
        }
    }
}
