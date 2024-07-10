package it.polito.students.clientouth

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.csrf.CookieCsrfTokenRepository

@EnableWebSecurity
@Configuration
class SecurityConfig(val crr: ClientRegistrationRepository) {

    @Value("\${server.port}")
    private lateinit var serverPort: String

    //handle RP-initiated logout
    fun oidcLogoutSuccessHandler() = OidcClientInitiatedLogoutSuccessHandler(crr)
        .also { it.setPostLogoutRedirectUri("http://localhost:${serverPort}") }

    @Bean
    fun securityFilterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        return httpSecurity
            .csrf {
                it.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            }
            .authorizeHttpRequests {
                it.requestMatchers("/me").permitAll()
                it.requestMatchers("/logout").permitAll()
                it.requestMatchers("/ui/**").permitAll()
                it.anyRequest().authenticated()         //only authenticated users can access
            }                                           //any other resource
            .oauth2Login { }
            .logout { it.logoutSuccessHandler(oidcLogoutSuccessHandler()) }
            .build()
    }
}