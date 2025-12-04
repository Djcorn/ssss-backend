package s4.backend;
/*
//https://docs.spring.io/spring-security/reference/servlet/architecture.html
//https://docs.spring.io/spring-security/reference/servlet/oauth2/login/core.html
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

 disabled for testing
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/upload").authenticated()  // secure endpoint
                .anyRequest().permitAll()                     // optional public endpoints
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt());   // validate JWT
        return http.build();
    }
    
} */
