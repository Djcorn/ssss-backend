
package s4.backend;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.DefaultUriBuilderFactory.EncodingMode;
import org.springframework.http.*;
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
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FilterTest {

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

    private static ZonedDateTime cutoffTime = null;

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
    @Order(3)
    @DisplayName("Testing multiple data uploads, data will be used for next tests")
    void testBulkUploading() throws Exception{
        uploadBulkData();
        System.out.print("DATA UPLOADED");
    }

    @Test
    @Order(4)
    @DisplayName("Should use the /getimagesdata REST api to query the database but only recieve updated data that is inside the given LatLon box")
    void testGetImagesDataLatLonFiltered() throws Exception{
        String host = composeContainer.getServiceHost(APP_NAME, APP_PORT);
        Integer port = composeContainer.getServicePort(APP_NAME, APP_PORT);
        String url = "http://" + host + ":" + port + DATA_ENDPOINT; 

        Double lat1 = 90.0;
        Double lon1 = 90.0;

        Double lat2 = 110.0;
        Double lon2 = 110.0;

        //need headers specifically for the JWT
        HttpHeaders headers = new HttpHeaders();
        String jwtToken = generateJwtToken();
        headers.set("Authorization", "Bearer " + jwtToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // Send the request
        RestTemplate restTemplate = new RestTemplate();
        DefaultUriBuilderFactory uriBuilder = new DefaultUriBuilderFactory();
        uriBuilder.setEncodingMode(EncodingMode.NONE); 

        URI uri = uriBuilder.uriString(url+"?latitude_1={value}&longitude_1={value}&latitude_2={value}&longitude_2={value}").build(lat1, lon1, lat2, lon2);

        restTemplate.setUriTemplateHandler(uriBuilder);

        ParameterizedTypeReference<List<PhotoData>> responseType = new ParameterizedTypeReference<List<PhotoData>>() {};
        ResponseEntity<List<PhotoData>> response = restTemplate.exchange(uri,  HttpMethod.GET, requestEntity, responseType);
        //System.out.println(response);
        assertEquals(response.getStatusCode().value(),200);

        List<PhotoData> viableData = response.getBody();
        assertEquals(5, viableData.size());

        //TODO: add check for specific data?
    }

    @Test
    @Order(4)
    @DisplayName("Should use the /getimagesdata REST api to query the database but only recieve updated data that is inside the given LatLon box and after the given date")
    void testGetImagesDataLatLonDateFiltered() throws Exception{
        String host = composeContainer.getServiceHost(APP_NAME, APP_PORT);
        Integer port = composeContainer.getServicePort(APP_NAME, APP_PORT);
        String url = "http://" + host + ":" + port + DATA_ENDPOINT; 

        Double lat1 = 90.0;
        Double lon1 = 90.0;

        Double lat2 = 110.0;
        Double lon2 = 110.0;

        //need headers specifically for the JWT
        HttpHeaders headers = new HttpHeaders();
        String jwtToken = generateJwtToken();
        headers.set("Authorization", "Bearer " + jwtToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // Send the request
        RestTemplate restTemplate = new RestTemplate();
        DefaultUriBuilderFactory uriBuilder = new DefaultUriBuilderFactory();
        uriBuilder.setEncodingMode(EncodingMode.NONE); 

        Long millisecondsSinceEpoch = cutoffTime.toInstant().toEpochMilli();
        URI uri = uriBuilder.uriString(url+"?latitude_1={value}&longitude_1={value}&latitude_2={value}&longitude_2={value}&startTime={value}").build(lat1, lon1, lat2, lon2, millisecondsSinceEpoch);

        restTemplate.setUriTemplateHandler(uriBuilder);

        ParameterizedTypeReference<List<PhotoData>> responseType = new ParameterizedTypeReference<List<PhotoData>>() {};
        ResponseEntity<List<PhotoData>> response = restTemplate.exchange(uri,  HttpMethod.GET, requestEntity, responseType);
        //System.out.println(response);
        assertEquals(response.getStatusCode().value(),200);

        List<PhotoData> viableData = response.getBody();
        assertEquals(3, viableData.size());

        //TODO: add check for specific data?
    } 

    @Test
    @Order(4)
    @DisplayName("Should use the /getimagesdata REST api to query the database but only recieve updated data that is after the given date")
    void testGetImagesDataDateFiltered() throws Exception{
        String host = composeContainer.getServiceHost(APP_NAME, APP_PORT);
        Integer port = composeContainer.getServicePort(APP_NAME, APP_PORT);
        String url = "http://" + host + ":" + port + DATA_ENDPOINT; 

        //need headers specifically for the JWT
        HttpHeaders headers = new HttpHeaders();
        String jwtToken = generateJwtToken();
        headers.set("Authorization", "Bearer " + jwtToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // Send the request
        RestTemplate restTemplate = new RestTemplate();
        DefaultUriBuilderFactory uriBuilder = new DefaultUriBuilderFactory();
        uriBuilder.setEncodingMode(EncodingMode.NONE); 

        Long millisecondsSinceEpoch = cutoffTime.toInstant().toEpochMilli();
        URI uri = uriBuilder.uriString(url+"?startTime={value}").build(millisecondsSinceEpoch);

        restTemplate.setUriTemplateHandler(uriBuilder);

        ParameterizedTypeReference<List<PhotoData>> responseType = new ParameterizedTypeReference<List<PhotoData>>() {};
        ResponseEntity<List<PhotoData>> response = restTemplate.exchange(uri,  HttpMethod.GET, requestEntity, responseType);
        //System.out.println(response);
        assertEquals(response.getStatusCode().value(),200);

        List<PhotoData> viableData = response.getBody();
        assertEquals(7, viableData.size());

        //TODO: add check for specific data?
    } 

    @Test
    @Order(5)
    @DisplayName("Should use the /getimages REST api to query the database but only recieve data that is inside the given LatLon box and after the given date")
    void testGetImagesLatLonDateFiltered() throws Exception{
        String host = composeContainer.getServiceHost(APP_NAME, APP_PORT);
        Integer port = composeContainer.getServicePort(APP_NAME, APP_PORT);
        String url = "http://" + host + ":" + port + IMAGES_ENDPOINT; 

        Double lat1 = 90.0;
        Double lon1 = 90.0;

        Double lat2 = 110.0;
        Double lon2 = 110.0;

        //need headers specifically for the JWT
        HttpHeaders headers = new HttpHeaders();
        String jwtToken = generateJwtToken();
        headers.set("Authorization", "Bearer " + jwtToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // Send the request
        RestTemplate restTemplate = new RestTemplate();
        DefaultUriBuilderFactory uriBuilder = new DefaultUriBuilderFactory();
        uriBuilder.setEncodingMode(EncodingMode.NONE); 

        Long millisecondsSinceEpoch = cutoffTime.toInstant().toEpochMilli();
        URI uri = uriBuilder.uriString(url+"?latitude_1={value}&longitude_1={value}&latitude_2={value}&longitude_2={value}&startTime={value}").build(lat1, lon1, lat2, lon2, millisecondsSinceEpoch);

        restTemplate.setUriTemplateHandler(uriBuilder);

        ResponseEntity<byte[]> response = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, byte[].class);
        //System.out.println(response);
        assertEquals(response.getStatusCode().value(),200);

        InputStream byteStream = new ByteArrayInputStream(response.getBody());
        ZipInputStream zipStream = new ZipInputStream(byteStream);

        List<String> fileNames = new ArrayList<>();
        ZipEntry entry;
        byte[] buffer = new byte[1024];
        while ((entry = zipStream.getNextEntry()) != null) {
            String fileName = "src/test/resources/"+entry.getName();
            System.out.println(fileName);
            fileNames.add(fileName);
            FileOutputStream fos = new FileOutputStream(fileName);
            
            int len;
            while((len = zipStream.read(buffer)) > 0){
                fos.write(buffer, 0, len);
            }
            fos.close();
        }

        assertEquals(3, fileNames.size());

        for (String name: fileNames){
            File rep = new File(name);
            assertTrue(rep.exists(), "Image file "+name+" unsuccessfully downloaded.");

            rep.delete();
        }   
    }

    @Test
    @Order(5)
    @DisplayName("Should use the /getimages REST api to query the database but only recieve data that is inside the given LatLon box")
    void testGetImagesLatLonFiltered() throws Exception{
        String host = composeContainer.getServiceHost(APP_NAME, APP_PORT);
        Integer port = composeContainer.getServicePort(APP_NAME, APP_PORT);
        String url = "http://" + host + ":" + port + IMAGES_ENDPOINT; 

        Double lat1 = 90.0;
        Double lon1 = 90.0;

        Double lat2 = 110.0;
        Double lon2 = 110.0;

        //need headers specifically for the JWT
        HttpHeaders headers = new HttpHeaders();
        String jwtToken = generateJwtToken();
        headers.set("Authorization", "Bearer " + jwtToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // Send the request
        RestTemplate restTemplate = new RestTemplate();
        DefaultUriBuilderFactory uriBuilder = new DefaultUriBuilderFactory();
        uriBuilder.setEncodingMode(EncodingMode.NONE); 

        //URI uri = uriBuilder.uriString(url+"?startdate={value}").build("2007-12-03T10:15:30+B01:00");
        URI uri = uriBuilder.uriString(url+"?latitude_1={value}&longitude_1={value}&latitude_2={value}&longitude_2={value}").build(lat1, lon1, lat2, lon2);
        restTemplate.setUriTemplateHandler(uriBuilder);

        ResponseEntity<byte[]> response = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, byte[].class);
        //System.out.println(response);
        assertEquals(response.getStatusCode().value(),200);

        InputStream byteStream = new ByteArrayInputStream(response.getBody());
        ZipInputStream zipStream = new ZipInputStream(byteStream);

        List<String> fileNames = new ArrayList<>();
        ZipEntry entry;
        byte[] buffer = new byte[1024];
        while ((entry = zipStream.getNextEntry()) != null) {
            String fileName = "src/test/resources/"+entry.getName();
            System.out.println(fileName);
            fileNames.add(fileName);
            FileOutputStream fos = new FileOutputStream(fileName);
            
            int len;
            while((len = zipStream.read(buffer)) > 0){
                fos.write(buffer, 0, len);
            }
            fos.close();
        }

        assertEquals(5, fileNames.size());

        for (String name: fileNames){
            File rep = new File(name);
            assertTrue(rep.exists(), "Image file "+name+" unsuccessfully downloaded.");

            rep.delete();
        }   
    }

    @Test
    @Order(5)
    @DisplayName("Should use the /getimages REST api to query the database but only recieve data after the given date")
    void testGetImagesDateFiltered() throws Exception{
        String host = composeContainer.getServiceHost(APP_NAME, APP_PORT);
        Integer port = composeContainer.getServicePort(APP_NAME, APP_PORT);
        String url = "http://" + host + ":" + port + IMAGES_ENDPOINT; 

        //need headers specifically for the JWT
        HttpHeaders headers = new HttpHeaders();
        String jwtToken = generateJwtToken();
        headers.set("Authorization", "Bearer " + jwtToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // Send the request
        RestTemplate restTemplate = new RestTemplate();
        DefaultUriBuilderFactory uriBuilder = new DefaultUriBuilderFactory();
        uriBuilder.setEncodingMode(EncodingMode.NONE); 

        Long millisecondsSinceEpoch = cutoffTime.toInstant().toEpochMilli();
        URI uri = uriBuilder.uriString(url+"?startTime={value}").build(millisecondsSinceEpoch);

        restTemplate.setUriTemplateHandler(uriBuilder);

        ResponseEntity<byte[]> response = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, byte[].class);
        //System.out.println(response);
        assertEquals(response.getStatusCode().value(),200);

        InputStream byteStream = new ByteArrayInputStream(response.getBody());
        ZipInputStream zipStream = new ZipInputStream(byteStream);

        List<String> fileNames = new ArrayList<>();
        ZipEntry entry;
        byte[] buffer = new byte[1024];
        while ((entry = zipStream.getNextEntry()) != null) {
            String fileName = "src/test/resources/"+entry.getName();
            System.out.println(fileName);
            fileNames.add(fileName);
            FileOutputStream fos = new FileOutputStream(fileName);
            
            int len;
            while((len = zipStream.read(buffer)) > 0){
                fos.write(buffer, 0, len);
            }
            fos.close();
        }

        assertEquals(7, fileNames.size());

        for (String name: fileNames){
            File rep = new File(name);
            assertTrue(rep.exists(), "Image file "+name+" unsuccessfully downloaded.");

            rep.delete();
        }   
    }

    // uploads dummy data
    private ResponseEntity<String> uploadData(Double lat, Double lon) throws Exception{
        String host = composeContainer.getServiceHost(APP_NAME, APP_PORT);
        Integer port = composeContainer.getServicePort(APP_NAME, APP_PORT);
        String url = "http://" + host + ":" + port + UPLOAD_ENDPOINT; 

        // Create the JSON part
        String jsonString = createPhotoJsonData(lat, lon);

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

    private void uploadBulkData() throws Exception{
        // should be outside of latlon box (assuming box is at 90,90 and 110,110)
        uploadData(-1.0, -1.0);
        uploadData(-1.0, 50.0);
        uploadData(-1.0, 200.0);
        uploadData(50.0, -1.0);

        // should all be inside of latlon box
        uploadData(90.0, 100.0);
        uploadData(100.0, 90.0);

        //data is uploaded in sections so that different filters have unique correct answers for better testing

        wait(1000);
        cutoffTime = ZonedDateTime.now();
        //System.out.println(cutoffTime.toString());
        wait(1000);


        // should be outside of latlon box
        uploadData(50.0, 200.0);
        uploadData(200.0, -1.0);
        uploadData(200.0, 50.0);
        uploadData(200.0, 200.0);

        // should all be inside of latlon box
        uploadData(100.0, 100.0);
        uploadData(100.0, 110.0);
        uploadData(110.0, 110.0);
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

    private String createPhotoJsonData(Double lat, Double lon) {
        
        String jsonString = "{\r\n" + //
                        "    \"device_id\": 0,\r\n" + //
                        "    \"timestamp\": 10,\r\n" + //
                        "    \"media_type\": \"photo\",\r\n" + //
                        "    \"latitude\": " + lat.toString() + ",\r\n" + //
                        "    \"longitude\": " + lon.toString() + ",\r\n" + //
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

    private static void wait(int ms)
    {
        try
        {
            Thread.sleep(ms);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }
} 