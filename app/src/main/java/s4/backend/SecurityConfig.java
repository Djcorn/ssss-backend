package s4.backend;

import javax.crypto.SecretKey;

//https://docs.spring.io/spring-security/reference/servlet/architecture.html
//https://docs.spring.io/spring-security/reference/servlet/oauth2/login/core.html
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Configuration
public class SecurityConfig {
    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] keyBytes = Decoders.BASE64.decode("c2VjdXJlc2VjdXJlc2VjdXJlc2VjcmV0c2VjcmV0a2V5Cg==");

        // Create a SecretKey from the same secret used to sign tokens
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        
        // Return a NimbusJwtDecoder that can decode and validate the JWT
        return NimbusJwtDecoder.withSecretKey(key).build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/**").authenticated()  // secure endpoint
                .anyRequest().permitAll()                     // optional public endpoints
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt());   // validate JWT
        return http.build();
    } 
}

