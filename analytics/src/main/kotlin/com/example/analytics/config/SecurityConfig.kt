package com.example.analytics.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(private val jwtAuthConverter: JwtAuthConverter) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .authorizeHttpRequests {
                //ANALYTICS
                it.requestMatchers(HttpMethod.GET, "/API/analytics/messages")
                    .hasAnyAuthority(Roles.ROLE_MANAGER, Roles.ROLE_OPERATOR)
                it.requestMatchers(HttpMethod.GET, "/API/analytics/messages/{year}")
                    .hasAnyAuthority(Roles.ROLE_MANAGER, Roles.ROLE_OPERATOR)
                it.requestMatchers(HttpMethod.GET, "/API/analytics/jobOffers")
                    .hasAnyAuthority(Roles.ROLE_MANAGER, Roles.ROLE_OPERATOR)
                it.requestMatchers(HttpMethod.GET, "/API/analytics/jobOffers/{year}")
                    .hasAnyAuthority(Roles.ROLE_MANAGER, Roles.ROLE_OPERATOR)
                it.requestMatchers(HttpMethod.GET, "/API/analytics/professionals")
                    .hasAnyAuthority(Roles.ROLE_MANAGER, Roles.ROLE_OPERATOR)
                it.requestMatchers(HttpMethod.GET, "/API/analytics/professionals")
                    .hasAnyAuthority(Roles.ROLE_MANAGER, Roles.ROLE_OPERATOR)

                //it.requestMatchers("/actuator/**").permitAll()
                it.anyRequest().authenticated()
            }
            .oauth2ResourceServer {
                it.jwt { jwtConfigurer ->
                    jwtConfigurer.jwtAuthenticationConverter(jwtAuthConverter)
                }
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .csrf { it.disable() }
            .cors { }
            .formLogin { it.disable() }
            .build()
    }

    @Bean
    fun corsFilter(): CorsFilter {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration()
        config.allowedOrigins = listOf("http://localhost:8080") // Permetti richieste da questo indirizzo
        config.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH") // Permetti questi metodi
        config.allowedHeaders = listOf("*") // Permetti tutti gli headers
        config.allowCredentials = true
        source.registerCorsConfiguration("/**", config)
        return CorsFilter(source)
    }
}