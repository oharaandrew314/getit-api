using Xunit;

namespace getit_api.api;

public class ShoppingListApiTest
{
    [Fact]
    public async Task GetLists()
    {
        var client = new TestServerFixture().CreateClient();

        // Act
        var response = await client.GetAsync("/v1/lists");

        // Assert
        response.EnsureSuccessStatusCode();
        var content = await response.Content.ReadAsStringAsync();
        Assert.Equal("[]", content);
    }
}
