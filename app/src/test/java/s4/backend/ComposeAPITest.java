package s4.backend;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.time.Duration;

@Testcontainers
public class ComposeAPITest {

    private static final String server_name = "app";

    @Container
    private static final ComposeContainer composeContainer =
            new ComposeContainer(new File("/home/downsd/repos/ssss-backend/docker-compose.yml"))
                    .withExposedService(server_name, 
                            8080,
                            Wait.forListeningPort())
                    .withExposedService("db", 5432);

    @Test
    void contextLoads() {
        System.out.println("Docker Compose container is up and running!");
    }
 
    @Test
    void testSpringBootAppIsRunning() {
        String host = composeContainer.getServiceHost(server_name, 8080);
        Integer port = composeContainer.getServicePort(server_name, 8080);

        System.out.println("Spring Boot app is running at: " + host + ":" + port);

        // Example: Make HTTP request to verify
        // RestTemplate restTemplate = new RestTemplate();
        // String body = restTemplate.getForObject("http://" + host + ":" + port + "/actuator/health", String.class);
        // Assertions.assertTrue(body.contains("UP"));
    }
}