using Microsoft.AspNetCore.Mvc.Testing;

namespace getit_api.api;

public class TestServerFixture : WebApplicationFactory<Program>
{
    protected override IHost CreateHost(IHostBuilder builder)
    {
        builder.UseContentRoot(Directory.GetCurrentDirectory());
        return base.CreateHost(builder);
    }
}