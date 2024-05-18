package com.swd.uniportal.infrastructure.config.security;

import com.swd.uniportal.domain.account.Role;
import com.swd.uniportal.infrastructure.config.security.authentication.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    @Autowired
    public SecurityConfig(AuthenticationEntryPoint authenticationEntryPoint,
                          JwtAuthenticationFilter jwtAuthenticationFilter, UserDetailsService userDetailsService) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    @SuppressWarnings("java:S1192")
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .requiresChannel(config ->
                        config.anyRequest()
                                .requiresSecure())

                .authorizeHttpRequests(config ->
                        config.requestMatchers("/api/v1/auth/**")
                                .permitAll())

                .authorizeHttpRequests(config ->
                        config.requestMatchers(HttpMethod.POST, "/api/v1/accounts")
                                .permitAll())

                .authorizeHttpRequests(config ->
                        config.requestMatchers("/api/v1/students/current/**")
                                .hasRole(Role.STUDENT.name()))

                .authorizeHttpRequests(config ->
                        config.requestMatchers(HttpMethod.GET, "/api/v1/majors/**")
                                .permitAll())
                .authorizeHttpRequests(config ->
                        config.requestMatchers(HttpMethod.POST, "/api/v1/majors/**")
                                .hasAnyRole(Role.ADMIN.name(), Role.STAFF.name()))
                .authorizeHttpRequests(config ->
                        config.requestMatchers(HttpMethod.PUT, "/api/v1/majors/**")
                                .hasAnyRole(Role.ADMIN.name(), Role.STAFF.name()))
                .authorizeHttpRequests(config ->
                        config.requestMatchers(HttpMethod.DELETE, "/api/v1/majors/**")
                                .hasAnyRole(Role.ADMIN.name(), Role.STAFF.name()))

                .authorizeHttpRequests(config ->
                        config.requestMatchers(HttpMethod.GET, "/api/v1/institutions/**")
                                .permitAll())
                .authorizeHttpRequests(config ->
                        config.requestMatchers(HttpMethod.POST, "/api/v1/institutions/**")
                                .hasAnyRole(Role.ADMIN.name(), Role.STAFF.name()))
                .authorizeHttpRequests(config ->
                        config.requestMatchers(HttpMethod.PUT, "/api/v1/institutions/**")
                                .hasAnyRole(Role.ADMIN.name(), Role.STAFF.name()))
                .authorizeHttpRequests(config ->
                        config.requestMatchers(HttpMethod.DELETE, "/api/v1/institutions/**")
                                .hasAnyRole(Role.ADMIN.name(), Role.STAFF.name()))

                .authorizeHttpRequests(config ->
                        config.requestMatchers(HttpMethod.GET, "/api/v1/high-schools/**")
                                .permitAll())
                .authorizeHttpRequests(config ->
                        config.requestMatchers(HttpMethod.POST, "/api/v1/high-schools/**")
                                .hasAnyRole(Role.ADMIN.name(), Role.STAFF.name()))
                .authorizeHttpRequests(config ->
                        config.requestMatchers(HttpMethod.PUT, "/api/v1/high-schools/**")
                                .hasAnyRole(Role.ADMIN.name(), Role.STAFF.name()))
                .authorizeHttpRequests(config ->
                        config.requestMatchers(HttpMethod.DELETE, "/api/v1/high-schools/**")
                                .hasAnyRole(Role.ADMIN.name(), Role.STAFF.name()))

                .authorizeHttpRequests(config ->
                        config.requestMatchers(HttpMethod.GET, "/api/v1/admission-plans/**")
                                .permitAll())
                .authorizeHttpRequests(config ->
                        config.requestMatchers(HttpMethod.POST, "/api/v1/admission-plans/**")
                                .hasAnyRole(Role.ADMIN.name(), Role.STAFF.name()))
                .authorizeHttpRequests(config ->
                        config.requestMatchers(HttpMethod.PUT, "/api/v1/admission-plans/**")
                                .hasAnyRole(Role.ADMIN.name(), Role.STAFF.name()))
                .authorizeHttpRequests(config ->
                        config.requestMatchers(HttpMethod.DELETE, "/api/v1/admission-plans/**")
                                .hasAnyRole(Role.ADMIN.name(), Role.STAFF.name()))

                .authorizeHttpRequests(config ->
                        config.requestMatchers(HttpMethod.GET, "/api/v1/addresses/**")
                                .permitAll())
                .authorizeHttpRequests(config ->
                        config.requestMatchers(HttpMethod.POST, "/api/v1/addresses/**")
                                .hasAnyRole(Role.ADMIN.name(), Role.STAFF.name()))
                .authorizeHttpRequests(config ->
                        config.requestMatchers(HttpMethod.PUT, "/api/v1/addresses/**")
                                .hasAnyRole(Role.ADMIN.name(), Role.STAFF.name()))
                .authorizeHttpRequests(config ->
                        config.requestMatchers(HttpMethod.DELETE, "/api/v1/addresses/**")
                                .hasAnyRole(Role.ADMIN.name(), Role.STAFF.name()))

                .authorizeHttpRequests(config ->
                        config.requestMatchers(HttpMethod.GET, "/api/v1/accounts/current")
                                .authenticated())
                .authorizeHttpRequests(config ->
                        config.requestMatchers(HttpMethod.PUT, "/api/v1/accounts/current")
                                .authenticated())

                .authorizeHttpRequests(config ->
                        config.requestMatchers(HttpMethod.GET, "/api/v1/accounts/**")
                                .permitAll())
                .authorizeHttpRequests(config ->
                        config.requestMatchers(HttpMethod.PUT, "/api/v1/accounts/**")
                                .hasAnyRole(Role.ADMIN.name(), Role.STAFF.name()))
                .authorizeHttpRequests(config ->
                        config.requestMatchers(HttpMethod.DELETE, "/api/v1/accounts/**")
                                .hasAnyRole(Role.ADMIN.name(), Role.STAFF.name()))

                .authorizeHttpRequests(config ->
                        config.requestMatchers(HttpMethod.GET, "subjects/**")
                                .permitAll())
                .authorizeHttpRequests(config ->
                        config.requestMatchers(HttpMethod.GET)
                                .permitAll())
                .authorizeHttpRequests(config ->
                        config.anyRequest()
                                .authenticated())
                .exceptionHandling(config ->
                        config.authenticationEntryPoint(authenticationEntryPoint))
                .sessionManagement(config ->
                        config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(BCryptPasswordEncoder.BCryptVersion.$2A);
    }

    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}
