package s4.backend;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

import javax.crypto.SecretKey;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String TEST_KEY = "./testkey.txt";

    @Bean
    @Profile("test")
    public JwtDecoder jwtDecoder() throws IOException{
        assert "test".equals(System.getProperty("spring.profiles.active"));

        String secret = Files.readString(Path.of(TEST_KEY)).trim();
        byte[] keyBytes = Decoders.BASE64.decode(secret);

        // Create a SecretKey from the same secret used to sign tokens
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        
        // Return a NimbusJwtDecoder that can decode and validate the JWT
        return NimbusJwtDecoder.withSecretKey(key).build();
    }

    
    @Bean
    @Profile("test")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/**")
                .authenticated()  // secure endpoint
                .anyRequest()
                .permitAll()                     // optional public endpoints
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt());   // validate JWT
        return http.build();
    } 


    @Bean
    @Profile("!test")
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/**")
                .authenticated()
                .anyRequest()
                .permitAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(new CustomJwtAuthenticationConverter())
                )
            );
        return http.build();
    }

    
    @Profile("!test")
    public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

        private final JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();

        @Override
        public AbstractAuthenticationToken convert(Jwt jwt) {
            if (!checkJwtValidity(jwt)) {
                throw new BadCredentialsException("JWT validation failed");
            }
            
            Collection<GrantedAuthority> authorities = authoritiesConverter.convert(jwt);
            String principal = jwt.getClaimAsString("preferred_username"); // Custom principal claim
            return new JwtAuthenticationToken(jwt, authorities, principal);
        }
    } 

    private boolean checkJwtValidity(Jwt jwt) {
        
        //Creates new map for converting objects to string 
        Map<String, String> claimsStrings = getJwtClaimStrings(jwt);

        //Hard coded public key
        String realAud = System.getenv("GOOGLE_ACCOUNT_PUBLIC_KEY"); 
        Instant time = Instant.parse(claimsStrings.get("exp"));
        //Checks
        if( claimsStrings.get("aud").equals(realAud) && 
            claimsStrings.get("iss").equals("https://accounts.google.com") &&
            claimsStrings.get("email_verified").equals("true") &&
            Instant.now().compareTo(time) < 0) {

            return true;
        }
        else{
            return false;
        }  
    }

}
    

