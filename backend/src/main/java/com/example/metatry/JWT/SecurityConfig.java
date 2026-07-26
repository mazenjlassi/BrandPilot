package com.example.metatry.JWT;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.*;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                //  ENABLE CORS HERE
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // ✅ PUBLIC
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/api/facebook/public-test").permitAll()
                        .requestMatchers("/api/linkedin/callback").permitAll()
                        .requestMatchers("/api/linkedin/auth-url").permitAll()
                        .requestMatchers("/api/scraper/scrape").permitAll()
                        .requestMatchers("/api/scraper/companies").permitAll()
                        .requestMatchers("/api/scraper/ingest").permitAll()
                        .requestMatchers("/api/scraper/trigger").authenticated()
                        .requestMatchers("/api/scraped-posts/**").authenticated()
                        .requestMatchers("/api/patterns/**").authenticated()
                        .requestMatchers("/api/company-profiles/**").authenticated()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/health").permitAll()

                        // ✅ PUBLIC READ-ONLY POST ENDPOINTS (dashboard, calendar, etc.)
                        .requestMatchers(HttpMethod.GET, "/posts/top").permitAll()
                        .requestMatchers(HttpMethod.GET, "/posts/latestPublished").permitAll()
                        .requestMatchers(HttpMethod.GET, "/posts/stats").permitAll()
                        .requestMatchers(HttpMethod.GET, "/posts/permanent").permitAll()
                        .requestMatchers(HttpMethod.GET, "/posts/calendar").permitAll()
                        .requestMatchers(HttpMethod.GET, "/posts/timing-analysis").permitAll()
                        .requestMatchers(HttpMethod.GET, "/posts/weekly-comparison").permitAll()
                        .requestMatchers(HttpMethod.GET, "/posts/upcoming-scheduled").permitAll()

                        // ✅ PUBLIC READ-ONLY CAMPAIGN ENDPOINTS
                        .requestMatchers(HttpMethod.GET, "/campaigns/recent").permitAll()
                        .requestMatchers(HttpMethod.GET, "/campaigns").permitAll()
                        .requestMatchers(HttpMethod.GET, "/campaigns/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/campaigns/{campaignId}/posts").permitAll()

                        // ✅ PROTECTED
                        .requestMatchers("/api/facebook/**").authenticated()
                        .requestMatchers("/api/instagram/**").authenticated()
                        .requestMatchers("/api/linkedin/**").authenticated()
                        .requestMatchers("/publish/**").authenticated()
                        .requestMatchers("/posts/**").authenticated()
                        .requestMatchers("/analytics/**").authenticated()

                        .anyRequest().authenticated()
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // CORS CONFIGURATION (THIS FIXES YOUR ISSUE)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "https://brand-pilot-an7hy2pem-mazenjlassis-projects.vercel.app",
                "https://brand-pilot-xi.vercel.app"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}