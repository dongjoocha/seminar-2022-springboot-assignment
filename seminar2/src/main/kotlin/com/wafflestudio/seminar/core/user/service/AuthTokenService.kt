package com.wafflestudio.seminar.core.user.service

import com.wafflestudio.seminar.core.user.api.request.SignInRequest
import com.wafflestudio.seminar.core.user.api.request.SignUpRequest
import com.wafflestudio.seminar.core.user.domain.UserPort
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Service
@EnableConfigurationProperties(AuthProperties::class)
class AuthTokenService(
    private val authProperties: AuthProperties,
    private val userPort: UserPort,
) {
    private val tokenPrefix = "Bearer "
    private val signingKey = Keys.hmacShaKeyFor(authProperties.jwtSecret.toByteArray())

    fun generateTokenByEmail(email: String): AuthToken {
        val claims: MutableMap<String, Any> = mutableMapOf("email" to email)
        val issuer = authProperties.issuer
        val expiryDate: Date = Date.from(
            LocalDateTime
                .now()
                .plusSeconds(authProperties.jwtExpiration)
                .atZone(ZoneId.systemDefault())
                .toInstant()
        )
        val resultToken = Jwts.builder()
            .setClaims(claims)
            .setIssuer(issuer)
            .setExpiration(expiryDate)
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact()

        return AuthToken(resultToken)
    }

    fun verifyToken(authToken: String): Boolean {
        val email = getEmailFromToken(authToken)
        val expiryDateInMillisecond = parse(authToken).body.expiration.time
        return userPort.getUserIdByEmail(email).let { return expiryDateInMillisecond > System.currentTimeMillis() }
    }

    fun getCurrentUserId(authToken: String): Long {
        val email = getEmailFromToken(authToken)
        return userPort.getUserIdByEmail(email)
    }

    fun getEmailFromToken(authToken: String): String {
        return parse(authToken).body["email"] as String
    }

    private fun parse(authToken: String): Jws<Claims> {
        val prefixRemoved = authToken.replace(tokenPrefix, "").trim { it <= ' ' }
        return Jwts
            .parserBuilder()
            .setSigningKey(signingKey)
            .build()
            .parseClaimsJws(prefixRemoved)
    }

    @Transactional
    fun signUp(signUpRequest: SignUpRequest): AuthToken {
        val user = userPort.createUser(signUpRequest)
        return generateTokenByEmail(user.email)
    }

    fun signIn(signInRequest: SignInRequest): AuthToken {
        val user = userPort.getUser(signInRequest)
        return generateTokenByEmail(user.email)
    }
}