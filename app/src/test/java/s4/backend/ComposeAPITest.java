package s4.backend;


import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
//import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.client.RestTemplate;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.stream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Testcontainers
public class ComposeAPITest {

    private static final String server_name = "app";

    @Container
    private static final ComposeContainer composeContainer =
            new ComposeContainer(new File("/home/downsd/repos/ssss-backend/docker-compose.yml"))
                    .withExposedService(server_name, 
                        8080,
                        Wait.forListeningPort())
                    .withCopyFilesInContainer("/home/downsd/repos/ssss-backend/app/src/test/resources/ai.png")
                    .withExposedService("db", 5432);

    @AfterAll
    static void PostTest()
    {
        String logs = composeContainer.getContainerByServiceName(server_name)
            .get()
            .getLogs();
        System.out.println(logs);
    }

    @Test 
    @Order(1)
    @DisplayName("Should use the docker compose file to open the app and data base")
    void apiUpTest(){
        String host = composeContainer.getServiceHost(server_name, 8080);
        Integer port = composeContainer.getServicePort(server_name, 8080);

        System.out.println("Spring Boot app is running at: " + host + ":" + port);
    }

    @Test
    @Order(2)
    @DisplayName("Should use the \\up REST api to add to the database")
    void testPostMultipartFormData() {
        composeContainer.withLogConsumer(server_name, new Slf4jLogConsumer(LoggerFactory.getLogger("UnitTestContainer")));   

        ResponseEntity<String> response = uploadData();

        assertEquals(response.getStatusCode().value(),200);
        assertEquals(response.getBody(), "{\"name\":\"test\",\"description\":\"example\"}");

        System.out.println(response.getBody());
    }

    @Test
    @Order(3)
    @DisplayName("Should use the \\query REST api to query the database")
    void testQuery() throws IOException{
        String host = composeContainer.getServiceHost(server_name, 8080);
        Integer port = composeContainer.getServicePort(server_name, 8080);
        String url = "http://" + host + ":" + port + "/query"; // your endpoint

        uploadData();

        // Send the request
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<byte[]> response = restTemplate.getForEntity(url,  byte[].class);

        assertEquals(response.getStatusCode().value(),200);

        System.out.println(response.getBody());      
        
        InputStream byteStream = new ByteArrayInputStream(response.getBody());
        ZipInputStream zipStream = new ZipInputStream(byteStream);

        List<String> fileNames = new ArrayList<>();
        ZipEntry entry;
        byte[] buffer = new byte[1024];
        while ((entry = zipStream.getNextEntry()) != null) {
            fileNames.add(entry.getName());
            FileOutputStream fos = new FileOutputStream("/home/downsd/repos/ssss-backend/app/src/test/resources/test.png");
            
            int len;
            while((len = zipStream.read(buffer)) > 0){
                fos.write(buffer, 0, len);
            }
            fos.close();
        }

        System.out.println(fileNames);
    }

    // uploads dummy data
    ResponseEntity<String> uploadData(){
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

        return response;

    }
}