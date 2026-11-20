package com.organizer.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class FirebaseAuthenticationFilter(
    private val verifier: FirebaseTokenVerifier,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val header = request.getHeader("Authorization")
        if (header != null && header.startsWith("Bearer ")) {
            val token = header.substringAfter("Bearer ").trim()
            if (token.isNotEmpty()) {
                try {
                    val decoded = verifier.verify(token)
                    val principal = FirebasePrincipal(
                        uid = decoded.uid,
                        email = decoded.email,
                        displayName = decoded.name,
                    )
                    val authentication = UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        emptyList(),
                    )
                    authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authentication
                } catch (ex: Exception) {
                    SecurityContextHolder.clearContext()
                    response.status = HttpServletResponse.SC_UNAUTHORIZED
                    response.contentType = "application/json"
                    response.writer.write("""{"error":"unauthorized","message":"Invalid or expired Firebase ID token"}""")
                    return
                }
            }
        }
        filterChain.doFilter(request, response)
    }
}