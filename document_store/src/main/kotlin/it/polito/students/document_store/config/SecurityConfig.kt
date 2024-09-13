package it.polito.students.document_store.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig(private val jwtAuthConverter: JwtAuthConverter) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .authorizeHttpRequests {
                it.requestMatchers(HttpMethod.GET, "/API/documents").hasAnyAuthority(Roles.ROLE_MANAGER, Roles.ROLE_OPERATOR, Roles.ROLE_GUEST)
                it.requestMatchers(HttpMethod.GET, "/API/documents/{metadataId}").hasAnyAuthority(Roles.ROLE_MANAGER, Roles.ROLE_OPERATOR, Roles.ROLE_GUEST)
                it.requestMatchers(HttpMethod.GET, "/API/documents/{metadataId}/data").hasAnyAuthority(Roles.ROLE_MANAGER, Roles.ROLE_OPERATOR, Roles.ROLE_GUEST)
                it.requestMatchers(HttpMethod.POST, "/API/documents").hasAnyAuthority(Roles.ROLE_OPERATOR)
                it.requestMatchers(HttpMethod.PUT, "/API/documents/{metadataId}").hasAnyAuthority(Roles.ROLE_OPERATOR)
                it.requestMatchers(HttpMethod.DELETE, "/API/documents/{metadataId}").hasAnyAuthority(Roles.ROLE_OPERATOR)

                //it.requestMatchers("/API/documents/auth").hasAuthority(Roles.ROLE_GUEST)
                it.requestMatchers("/actuator/**").permitAll()
                it.requestMatchers("/API/documents/auth/public").permitAll()
                it.anyRequest().authenticated()
            }
            .oauth2ResourceServer {
                it.jwt { jwtConfigurer ->
                    jwtConfigurer.jwtAuthenticationConverter(jwtAuthConverter)
                }
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .csrf { it.disable() }
            .cors { it.disable() }
            //.formLogin { it.disable() } per ora è commentato, poi con il frontend si può disabilitare
            .build()
    }

}