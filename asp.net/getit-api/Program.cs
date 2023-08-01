using getit_api.api;using getit_api.dao;

var builder = WebApplication.CreateBuilder(args);
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

var app = builder.Build();
app.UseSwagger();
app.UseSwaggerUI(options =>
{
    options.SwaggerEndpoint("swagger/v1/swagger.json", "v1");
    options.RoutePrefix = string.Empty;
});

var lists = new MemoryShoppingListDao();

string GetStaticUserId() => "user1";

ShoppingListApi.Register(app, lists, GetStaticUserId);

app.Run();

public partial class Program { }