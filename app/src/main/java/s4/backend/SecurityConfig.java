package s4.backend;

import java.io.IOError;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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


// this needs to be assigned under the TEST profile
@Configuration
public class SecurityConfig {

    private static final String TEST_KEY = "./testkey.txt";

    @Bean
    public JwtDecoder jwtDecoder() throws IOException{
        String secret = Files.readString(Path.of(TEST_KEY)).trim();
        byte[] keyBytes = Decoders.BASE64.decode(secret);

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

    

