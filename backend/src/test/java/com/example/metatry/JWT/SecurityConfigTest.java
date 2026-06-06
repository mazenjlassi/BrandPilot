package com.example.metatry.JWT;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock private JwtAuthenticationFilter jwtFilter;

    @Test
    void passwordEncoder_returnsBCrypt() {
        SecurityConfig config = new SecurityConfig(jwtFilter);
        PasswordEncoder encoder = config.passwordEncoder();
        assertThat(encoder).isInstanceOf(BCryptPasswordEncoder.class);
    }

    @Test
    void corsConfigurationSource_allowsLocalhost4200() {
        SecurityConfig config = new SecurityConfig(jwtFilter);
        var source = config.corsConfigurationSource();
        assertThat(source).isInstanceOf(UrlBasedCorsConfigurationSource.class);
        var urlSource = (UrlBasedCorsConfigurationSource) source;
        var corsConfig = urlSource.getCorsConfigurations().get("/**");
        assertThat(corsConfig).isNotNull();
        assertThat(corsConfig.getAllowedOrigins()).contains("http://localhost:4200");
        assertThat(corsConfig.getAllowedMethods()).contains("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}
