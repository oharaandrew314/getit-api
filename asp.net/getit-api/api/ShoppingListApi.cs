using getit_api.dao;
using Microsoft.AspNetCore.Mvc;

namespace getit_api.api
{
    public delegate string GetUserId();

    public static class ShoppingListApi
    {
        public static void Register(WebApplication app, ShoppingListDao lists, GetUserId getUserId)
        {
            app.MapGet("/v1/lists", () =>
                lists.getLists(getUserId())
            ).WithName("Get Lists");

            app.MapGet("/v1/lists/{listId:Guid}", (Guid listId) =>
                lists.getList(getUserId(), listId)
            ).WithName("Get Items");
        }
    }

    public class ShoppingListController : Controller
    {
        private readonly ShoppingListDao _lists;
        private readonly GetUserId _getUserId;

        public ShoppingListController(GetUserId getUserId, ShoppingListDao lists)
        {
            _getUserId = getUserId;
            _lists = lists;
        }

        public List<ShoppingList> Index()
        {
            return _lists.getLists(_getUserId());
        }

        public ShoppingList? Get(Guid listId)
        {
            return _lists.getList(_getUserId(), listId);
        }

        public ShoppingList Save(ShoppingListData data)
        {
            var list = new ShoppingList(_getUserId(), Guid.NewGuid(), data.name);
            _lists.add(list);
            return list;
        }
    }
}


