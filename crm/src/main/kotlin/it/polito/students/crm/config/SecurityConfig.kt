package it.polito.students.crm.config

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
                //CONTACTS
                it.requestMatchers(HttpMethod.GET, "/API/contacts")
                    .hasAnyAuthority(Roles.ROLE_MANAGER, Roles.ROLE_OPERATOR, Roles.ROLE_GUEST)
                it.requestMatchers(HttpMethod.GET, "/API/contacts/{contactID}")
                    .hasAnyAuthority(Roles.ROLE_MANAGER, Roles.ROLE_OPERATOR, Roles.ROLE_GUEST)
                it.requestMatchers(HttpMethod.GET, "/API/contacts/{contactID}/{whatContact}")
                    .hasAnyAuthority(Roles.ROLE_MANAGER, Roles.ROLE_OPERATOR, Roles.ROLE_GUEST)
                it.requestMatchers(HttpMethod.POST, "/API/contacts").hasAnyAuthority(Roles.ROLE_OPERATOR)
                it.requestMatchers(HttpMethod.POST, "/API/contacts/{contactID}/{whatContact}")
                    .hasAnyAuthority(Roles.ROLE_OPERATOR)
                it.requestMatchers(HttpMethod.PUT, "/API/contacts/{contactID}/{whatContact}/{id}")
                    .hasAnyAuthority(Roles.ROLE_OPERATOR)
                it.requestMatchers(HttpMethod.PATCH, "/API/contacts/{contactID}").hasAnyAuthority(Roles.ROLE_OPERATOR)
                it.requestMatchers(HttpMethod.DELETE, "/API/contacts/{contactID}/{whatContact}/{id}")
                    .hasAnyAuthority(Roles.ROLE_OPERATOR)

                //CUSTOMERS
                it.requestMatchers(HttpMethod.GET, "/API/customers")
                    .hasAnyAuthority(Roles.ROLE_MANAGER, Roles.ROLE_OPERATOR, Roles.ROLE_GUEST)
                it.requestMatchers(HttpMethod.GET, "/API/customers/{customerID}")
                    .hasAnyAuthority(Roles.ROLE_MANAGER, Roles.ROLE_OPERATOR, Roles.ROLE_GUEST)
                it.requestMatchers(HttpMethod.POST, "/API/customers").hasAnyAuthority(Roles.ROLE_OPERATOR)
                it.requestMatchers(HttpMethod.PATCH, "/API/customers/{customerID}").hasAnyAuthority(Roles.ROLE_OPERATOR)
                it.requestMatchers(HttpMethod.DELETE, "/API/customers/{customerID}")
                    .hasAnyAuthority(Roles.ROLE_OPERATOR)

                //JOBOFFERS
                it.requestMatchers(HttpMethod.GET, "/API/joboffers")
                    .hasAnyAuthority(Roles.ROLE_MANAGER, Roles.ROLE_OPERATOR, Roles.ROLE_GUEST)
                it.requestMatchers(HttpMethod.GET, "/API/joboffers/{jobOfferId}/value")
                    .hasAnyAuthority(Roles.ROLE_MANAGER, Roles.ROLE_OPERATOR, Roles.ROLE_GUEST)
                it.requestMatchers(HttpMethod.POST, "/API/joboffers").hasAnyAuthority(Roles.ROLE_OPERATOR)
                it.requestMatchers(HttpMethod.PATCH, "/API/joboffers").hasAnyAuthority(Roles.ROLE_OPERATOR)
                it.requestMatchers(HttpMethod.PATCH, "/API/joboffers/{jobOfferId}").hasAnyAuthority(Roles.ROLE_OPERATOR)
                it.requestMatchers(HttpMethod.DELETE, "/API/joboffers/{jobOfferId}")
                    .hasAnyAuthority(Roles.ROLE_OPERATOR)
                it.requestMatchers(HttpMethod.POST, "/API/joboffers/generate").hasAnyAuthority(Roles.ROLE_OPERATOR)
                it.requestMatchers(HttpMethod.POST, "/API/joboffers/skills/generate").hasAnyAuthority(Roles.ROLE_OPERATOR)
                
                //MESSAGGES
                it.requestMatchers(HttpMethod.GET, "/API/messages")
                    .hasAnyAuthority(Roles.ROLE_MANAGER, Roles.ROLE_OPERATOR, Roles.ROLE_GUEST)
                it.requestMatchers(HttpMethod.GET, "/API/messages/{messageId}")
                    .hasAnyAuthority(Roles.ROLE_MANAGER, Roles.ROLE_OPERATOR, Roles.ROLE_GUEST)
                it.requestMatchers(HttpMethod.GET, "/API/messages/{messageId}/history")
                    .hasAnyAuthority(Roles.ROLE_MANAGER, Roles.ROLE_OPERATOR, Roles.ROLE_GUEST)
                it.requestMatchers(HttpMethod.POST, "/API/messages").hasAnyAuthority(Roles.ROLE_OPERATOR)
                it.requestMatchers(HttpMethod.PATCH, "/API/messages/{messageId}").hasAnyAuthority(Roles.ROLE_OPERATOR)

                //PROFESSIONALS
                it.requestMatchers(HttpMethod.GET, "/API/professionals")
                    .hasAnyAuthority(Roles.ROLE_MANAGER, Roles.ROLE_OPERATOR, Roles.ROLE_GUEST)
                it.requestMatchers(HttpMethod.GET, "/API/professionals/{professionalID}")
                    .hasAnyAuthority(Roles.ROLE_MANAGER, Roles.ROLE_OPERATOR, Roles.ROLE_GUEST)
                it.requestMatchers(HttpMethod.POST, "/API/professionals").hasAnyAuthority(Roles.ROLE_OPERATOR)
                it.requestMatchers(HttpMethod.PATCH, "/API/professionals/{professionalID}")
                    .hasAnyAuthority(Roles.ROLE_OPERATOR)
                it.requestMatchers(HttpMethod.DELETE, "/API/professionals/{professionalID}")
                    .hasAnyAuthority(Roles.ROLE_OPERATOR)

                it.requestMatchers("/actuator/**").permitAll()
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