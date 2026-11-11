package com.smartbuilding.config;

import com.smartbuilding.security.CustomAccessDeniedHandler;
import com.smartbuilding.security.JwtAuthenticationEntryPoint;
import com.smartbuilding.security.UserDetailsServiceImpl;
import com.smartbuilding.security.jwt.JwtAuthenticationFilter;
import com.smartbuilding.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            @Autowired(required = false) JwtTokenProvider tokenProvider,
            @Autowired(required = false) UserDetailsServiceImpl userDetailsService) {
        if (tokenProvider != null && userDetailsService != null) {
            return new JwtAuthenticationFilter(tokenProvider, userDetailsService);
        }
        return null;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Autowired(required = false) JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            @Autowired(required = false) CustomAccessDeniedHandler customAccessDeniedHandler,
            @Autowired(required = false) JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/api/v1/auth/**",
                                "/api/v1/health/**",
                                "/api/v1/system/info",
                                "/actuator/**",
                                "/ws/**",
                                "/ws-raw/**",
                                "/error"
                        ).permitAll()
                        .anyRequest().authenticated()
                );

        if (jwtAuthenticationEntryPoint != null || customAccessDeniedHandler != null) {
            http.exceptionHandling(exceptions -> {
                if (jwtAuthenticationEntryPoint != null) {
                    exceptions.authenticationEntryPoint(jwtAuthenticationEntryPoint);
                }
                if (customAccessDeniedHandler != null) {
                    exceptions.accessDeniedHandler(customAccessDeniedHandler);
                }
            });
        }

        if (jwtAuthenticationFilter != null) {
            http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        }

        return http.build();
    }
}
