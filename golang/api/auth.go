package api

import (
	"net/url"
	"time"

	"github.com/auth0/go-jwt-middleware/v2/jwks"
	"github.com/auth0/go-jwt-middleware/v2/validator"
)

func CreateJwtValidator(oidcUrl *url.URL, issuer string, audience string) (*validator.Validator, error) {
	provider := jwks.NewCachingProvider(oidcUrl, time.Duration(5*time.Minute))

	return validator.New(provider.KeyFunc,
		validator.RS256,
		issuer,
		[]string{audience},
	)
}
