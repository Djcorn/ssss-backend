package s4.backend;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
//import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.client.RestTemplate;


import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    }

    @Test 
    void apiUpTest(){
        String host = composeContainer.getServiceHost(server_name, 8080);
        Integer port = composeContainer.getServicePort(server_name, 8080);

        System.out.println("Spring Boot app is running at: " + host + ":" + port);
    }

    @Test
    void testPostMultipartFormData() {
        String host = composeContainer.getServiceHost(server_name, 8080);
        Integer port = composeContainer.getServicePort(server_name, 8080);
        String url = "http://" + host + ":" + port + "/up"; // your endpoint

        // Create the JSON part
        String jsonString = "{\"name\":\"test\",\"description\":\"example\"}";
        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> jsonPart = new HttpEntity<>(jsonString, jsonHeaders);

        // Create the file part
        File file = new File("/home/downsd/repos/ssss-backend/app/src/test/resources/ai.png"); 
        FileSystemResource fileResource = new FileSystemResource(file);

        // Build the multipart request
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("json", jsonPart); // JSON part
        body.add("image", fileResource); // file part

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Send the request
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

        assertEquals(response.getStatusCode().value(),200);
        assertEquals(response.getBody(), "{\"name\":\"test\",\"description\":\"example\"}");

        System.out.println("Response status: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody());
    }
}