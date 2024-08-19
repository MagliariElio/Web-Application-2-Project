package it.polito.students.clientouth.controllers

import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("")
class HomeController {

    @GetMapping("/me", "  /me")
    fun me(
        @CookieValue(name="XSRF-TOKEN", required = false)
        xsrf: String?,
        authentication: Authentication?
    ): Map<String, Any?> {
        val principal: OidcUser? = authentication?.principal as? OidcUser
        val name = principal?.userInfo?.givenName ?: ""
        val surname = principal?.userInfo?.familyName ?: ""
        return mapOf(
            "name" to name,
            "surname" to surname,
            "loginUrl" to "/oauth2/authorization/oidc-app-client",
            "logoutUrl" to "/logout",
            "principal" to principal,
            "xsrfToken" to xsrf
        )
    }
}