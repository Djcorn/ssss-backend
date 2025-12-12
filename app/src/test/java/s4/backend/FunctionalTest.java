
package s4.backend;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.apache.commons.io.FileUtils;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import s4.backend.data.PhotoData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class FunctionalTest {

    private static final String APP_NAME = "app";
    private static final String DB_NAME = "db";
    private static final int APP_PORT = 8080;
    private static final int DB_PORT = 5432;

    private static final String SECRETS_DIR = "../secrets/";
    private static final String TEST_KEY = "testkey.txt";

    private static final String UPLOAD_ENDPOINT = "/upload";
    private static final String IMAGES_ENDPOINT = "/getimages";
    private static final String DATA_ENDPOINT = "/getimagesdata";

    private static final String TEST_IMAGE = "src/test/resources/ai.png";
    private static final String REPLICATED_IMAGE = "src/test/resources/test.png";

    @Container
    private static final ComposeContainer composeContainer =
            new ComposeContainer(new File("../docker-compose.yml"))
                    .withExposedService(APP_NAME, 
                        APP_PORT,
                        Wait.forListeningPort())
                    .withExposedService(DB_NAME, DB_PORT);

    

    @BeforeAll
    @Order(1)
    static void checkProfile() {
        assumeTrue(
            "test".equals(System.getProperty("spring.profiles.active")),
            "Skipping entire class because profile is not test"
        );
    }

    @BeforeAll
    @Order(2)
    static void setUp() {
        composeContainer.start();
    }


    @AfterAll
    @Order(1)
    static void PostTest()
    {
        if (!"test".equals(System.getProperty("spring.profiles.active"))){
            return;
        }
        try {
             String logs = composeContainer.getContainerByServiceName(APP_NAME)
                .get()
                .getLogs();
            System.out.println(logs);
        } catch (NoSuchElementException e) {
            System.out.println("No logs found: "+e.toString());
        }
    }

    @AfterAll
    @Order(2)
    static void tearDown() {
        composeContainer.stop();
    }

    @Test 
    @Order(1)
    @DisplayName("Should use the docker compose file to open the app and data base")
    void apiUpTest(){
        String host = composeContainer.getServiceHost(APP_NAME, APP_PORT);
        Integer port = composeContainer.getServicePort(APP_NAME, APP_PORT);

        System.out.println("Spring Boot app is running at: " + host + ":" + port);
    }

    @Test
    @Order(2)
    @DisplayName("Should use the \\up REST api to add to the database")
    void testPostMultipartFormData() throws Exception{
        composeContainer.withLogConsumer(APP_NAME, new Slf4jLogConsumer(LoggerFactory.getLogger("UnitTestContainer")));   

        ResponseEntity<String> response = uploadData();

        assertEquals(response.getStatusCode().value(),200);
    }

    @Test
    @Order(3)
    @DisplayName("Should use the /getimages REST api to query the database. ")
    void testGetImages() throws Exception{
        String host = composeContainer.getServiceHost(APP_NAME, APP_PORT);
        Integer port = composeContainer.getServicePort(APP_NAME, APP_PORT);
        String url = "http://" + host + ":" + port + "/"+IMAGES_ENDPOINT; 

        uploadData();

        //need headers specifically for the JWT
        HttpHeaders headers = new HttpHeaders();
        String jwtToken = generateJwtToken();
        headers.set("Authorization", "Bearer " + jwtToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // Send the request
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<byte[]> response = restTemplate.exchange(url,  HttpMethod.GET, requestEntity, byte[].class);

        assertEquals(response.getStatusCode().value(),200);
        
        System.out.println(response); 
        System.out.println(response.getBody());      
        
        InputStream byteStream = new ByteArrayInputStream(response.getBody());
        ZipInputStream zipStream = new ZipInputStream(byteStream);

        List<String> fileNames = new ArrayList<>();
        ZipEntry entry;
        byte[] buffer = new byte[1024];
        while ((entry = zipStream.getNextEntry()) != null) {
            fileNames.add(entry.getName());
            FileOutputStream fos = new FileOutputStream(REPLICATED_IMAGE);
            
            int len;
            while((len = zipStream.read(buffer)) > 0){
                fos.write(buffer, 0, len);
            }
            fos.close();
        }

        File rep = new File(REPLICATED_IMAGE);
        assertTrue(rep.exists(), "Image file unsuccessfully downloaded.");
        //TODO: re-enable
        assertTrue(FileUtils.contentEquals(new File(TEST_IMAGE), 
            rep),
            "uploaded and downloaded image files do not match");

        if (rep.exists()) {
            rep.delete();
        }
    }

    @Test
    @Order(4)
    @DisplayName("Should use the /getimagesdata REST api to query the database. ")
    void testGetImagesData() throws Exception{
        String host = composeContainer.getServiceHost(APP_NAME, APP_PORT);
        Integer port = composeContainer.getServicePort(APP_NAME, APP_PORT);
        String url = "http://" + host + ":" + port + "/"+DATA_ENDPOINT; 

        //uploadData();

        //need headers specifically for the JWT
        HttpHeaders headers = new HttpHeaders();
        String jwtToken = generateJwtToken();
        headers.set("Authorization", "Bearer " + jwtToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);


        // Send the request
        RestTemplate restTemplate = new RestTemplate();
        ParameterizedTypeReference<List<PhotoData>> responseType = new ParameterizedTypeReference<List<PhotoData>>() {};
        ResponseEntity<List<PhotoData>> response = restTemplate.exchange(url,  HttpMethod.GET, requestEntity, responseType);

        assertEquals(response.getStatusCode().value(),200);

        System.out.println(response); 
        System.out.println(response.getBody());      

        //TODO: add check for expected data
        //assertEquals(response.getBody(), "{\"name\":\"test\",\"description\":\"example\"}");
    }

    // uploads dummy data
    private ResponseEntity<String> uploadData() throws Exception{
        String host = composeContainer.getServiceHost(APP_NAME, APP_PORT);
        Integer port = composeContainer.getServicePort(APP_NAME, APP_PORT);
        String url = "http://" + host + ":" + port + UPLOAD_ENDPOINT; 
        System.out.println(url);

        // Create the JSON part
        String jsonString = createPhotoJsonData();

        // Create the file part
        File file = new File(TEST_IMAGE); 
        assertTrue(file.exists(), file.toString() + " does not exist");
        FileSystemResource fileResource = new FileSystemResource(file);

        // Build the multipart request
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("json", jsonString); //jsonPart); // JSON part
        body.add("image", fileResource); // file part

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        String jwtToken = generateJwtToken();
        headers.set("Authorization", "Bearer " + jwtToken);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Send the request
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

        return response;

    }

    private String generateJwtToken() throws IOException{
        // Use a secure key - in production, load this from configuration
        //String secretKey = "yoursecretkeythatisatleast256bitslongforhs256";
        String secret = Files.readString(Path.of(SECRETS_DIR+TEST_KEY)).trim();
        byte[] keyBytes = Decoders.BASE64.decode(secret);


        return Jwts.builder()
            .subject("testuser")
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour expiration
            .claim("scope", "read write")
            .signWith(Keys.hmacShaKeyFor(keyBytes), SignatureAlgorithm.HS256)
            .compact();
    }

    private String createPhotoJsonData() {
        
        String jsonString = "{\r\n" + //
                        "    \"device_id\": 0,\r\n" + //
                        "    \"timestamp\": 10,\r\n" + //
                        "    \"media_type\": \"photo\",\r\n" + //
                        "    \"latitude\": 1234.1,\r\n" + //
                        "    \"longitude\": 1512321.123,\r\n" + //
                        "    \"location_accuracy\": 0,\r\n" + //
                        "    \"altitude_above_msl\": 0,\r\n" + //
                        "    \"height_above_ellipsoid\": 0,\r\n" + //
                        "    \"linear_error\": 0,\r\n" + //
                        "    \"resolution\": \"0\",\r\n" + //
                        "    \"zoom\": 0,\r\n" + //
                        "    \"horizontal_field_of_view\": 0,\r\n" + //
                        "    \"vertical_field_of_view\": 0,\r\n" + //
                        "    \"azumith\": 0, \r\n" + //
                        "    \"pitch\": 0,\r\n" + //
                        "    \"roll\": 0,\r\n" + //
                        "    \"lens_type\": \"wide\",\r\n" + //
                        "    \"location_provider\": \"fused\"\r\n" + //
                        "}";

        return jsonString;
    }
} 