package dev.andrewohara.getit.spring

import dev.andrewohara.getit.api.Authorizer
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JwtSecurityFilter(private val authorizer: Authorizer) : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        getAuthentication(request)?.also {
            SecurityContextHolder.getContext().authentication = it
        }

        return chain.doFilter(request, response)
    }

    private fun getAuthentication(request: HttpServletRequest): UsernamePasswordAuthenticationToken? {
        val token = request
            .getHeader("Authorization")
            ?.replace("Bearer", "", ignoreCase = true)
            ?.trim()
            ?: return null

        val userId = authorizer(token) ?: return null

        return UsernamePasswordAuthenticationToken(userId, null, emptyList())
    }
}
