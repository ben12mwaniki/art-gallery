package ca.mcgill.ecse321.gallerysystem.integration_test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import ca.mcgill.ecse321.gallerysystem.dao.AdministratorRepository;
import ca.mcgill.ecse321.gallerysystem.dao.ArtistRepository;
import ca.mcgill.ecse321.gallerysystem.dao.CustomerRepository;
import ca.mcgill.ecse321.gallerysystem.dto.AdministratorDto;
import ca.mcgill.ecse321.gallerysystem.dto.AdministratorRequestDto;
import ca.mcgill.ecse321.gallerysystem.dto.ArtistDto;
import ca.mcgill.ecse321.gallerysystem.dto.ArtistRequestDto;
import ca.mcgill.ecse321.gallerysystem.dto.CustomerDto;
import ca.mcgill.ecse321.gallerysystem.dto.CustomerRequestDto;
import ca.mcgill.ecse321.gallerysystem.dto.CustomerUpdateRequestDto;
import ca.mcgill.ecse321.gallerysystem.dto.ErrorResponseDto;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = "spring.profiles.active=test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestUserCreationAndUpdateIntegration {

        @LocalServerPort
        private int port;

        @Autowired
        private TestRestTemplate restTemplate;

        @Autowired
        private CustomerRepository customerRepository;
        @Autowired
        private ArtistRepository artistRepository;
        @Autowired
        private AdministratorRepository administratorRepository;

        private HttpHeaders headers;
        private String baseUrl;

        // Test data
        private static final String CUSTOMER_EMAIL = "test.customer@test.com";
        private static final String ARTIST_EMAIL = "test.artist@test.com";
        private static final String ADMIN_EMAIL = "test.admin@test.com";
        private static final String UPDATED_USERNAME = "UpdatedName";
        private static final String UPDATED_ADDRESS = "456 Updated Street";
        private static final String UPDATED_PASSWORD = "newPassword123";

        @BeforeEach
        public void setUp() {
                // Setup JSON headers
                headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(java.util.Arrays.asList(MediaType.APPLICATION_JSON));

                baseUrl = "http://localhost:" + port;

                // Clean up before each test
                cleanup();
        }

        @AfterEach
        public void tearDown() {
                cleanup();
        }

        private void cleanup() {
                customerRepository.deleteAll();
                artistRepository.deleteAll();
                administratorRepository.deleteAll();
        }

        // ==================== CUSTOMER TESTS ====================

        @Test
        @Order(1)
        public void testCreateCustomer_Success() {
                // Arrange
                CustomerRequestDto requestDto = createCustomerRequestDto(
                                "John Doe",
                                CUSTOMER_EMAIL,
                                "password123",
                                "123 Main Street");
                HttpEntity<CustomerRequestDto> request = new HttpEntity<>(requestDto, headers);

                // Act
                ResponseEntity<CustomerDto> response = restTemplate.postForEntity(
                                url("/customer"),
                                request,
                                CustomerDto.class);

                // Assert
                assertEquals(HttpStatus.CREATED, response.getStatusCode());
                assertNotNull(response.getBody());

                CustomerDto customerDto = response.getBody();
                assertEquals("John Doe", customerDto.getUserName());
                assertEquals(CUSTOMER_EMAIL, customerDto.getEmail());
                assertEquals("123 Main Street", customerDto.getAddress());

                // Verify in database
                assertTrue(customerRepository.findCustomerByEmail(CUSTOMER_EMAIL) != null);
        }

        @Test
        @Order(2)
        public void testCreateCustomer_DuplicateEmail_ThrowsException() {
                // Arrange - First create a customer
                createTestCustomer();

                // Act - Try to create another with same email
                CustomerRequestDto requestDto = createCustomerRequestDto(
                                "Jane Doe",
                                CUSTOMER_EMAIL, // Same email
                                "password456",
                                "456 Oak Street");
                HttpEntity<CustomerRequestDto> request = new HttpEntity<>(requestDto, headers);

                ResponseEntity<ErrorResponseDto> response = restTemplate.postForEntity(
                                url("/customer"),
                                request,
                                ErrorResponseDto.class);

                // Assert
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals("Bad Request", response.getBody().getError());
                assertTrue(response.getBody().getMessage().contains("already registered") ||
                                response.getBody().getMessage().contains("already exists"));
        }

        @Test
        @Order(3)
        public void testCreateCustomer_MissingRequiredFields_ThrowsException() {
                // Arrange - Create DTO with missing email
                CustomerRequestDto requestDto = new CustomerRequestDto();
                requestDto.setUserName("John Doe");
                // Email is missing
                requestDto.setPassword("password123");
                requestDto.setAddress("123 Main Street");

                HttpEntity<CustomerRequestDto> request = new HttpEntity<>(requestDto, headers);

                // Act
                ResponseEntity<ErrorResponseDto> response = restTemplate.postForEntity(
                                url("/customer"),
                                request,
                                ErrorResponseDto.class);

                // Assert
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals("Validation Failed", response.getBody().getError());
                assertTrue(response.getBody().getMessage().contains("Email is required"));

                // Verify field-specific errors
                assertNotNull(response.getBody().getErrors());
                assertTrue(response.getBody().getErrors().containsKey("email"));
                assertEquals("Email is required", response.getBody().getErrors().get("email"));
        }

        @Test
        @Order(4)
        public void testGetCustomer_Success() {
                // Arrange
                createTestCustomer();

                // Act
                ResponseEntity<CustomerDto> response = restTemplate.getForEntity(
                                url("/customer/" + CUSTOMER_EMAIL),
                                CustomerDto.class);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals("John Doe", response.getBody().getUserName()); // Fixed
                assertEquals(CUSTOMER_EMAIL, response.getBody().getEmail());
                assertEquals("123 Main Street", response.getBody().getAddress());
        }

        @Test
        @Order(5)
        public void testGetCustomer_NotFound_Returns404() {
                // Act
                ResponseEntity<ErrorResponseDto> response = restTemplate.getForEntity(
                                url("/customer/nonexistent@test.com"),
                                ErrorResponseDto.class);

                // Assert
                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals("Not Found", response.getBody().getError());
                assertTrue(response.getBody().getMessage().contains("No customer found") ||
                                response.getBody().getMessage().contains("There is no such Customer"));
        }

        @Test
        @Order(6)
        public void testUpdateCustomer_PartialUpdatePassword_Success() {
                // Arrange
                createTestCustomer();

                // Act - Update only password
                CustomerUpdateRequestDto updateDto = new CustomerUpdateRequestDto();
                updateDto.setPassword(UPDATED_PASSWORD);

                // Use RequestEntity.patch() instead of HttpMethod.PATCH
                RequestEntity<CustomerUpdateRequestDto> requestEntity = RequestEntity
                                .patch(URI.create(url("/customer/" + CUSTOMER_EMAIL)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .body(updateDto);

                ResponseEntity<CustomerDto> response = restTemplate.exchange(
                                requestEntity,
                                CustomerDto.class);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals("John Doe", response.getBody().getUserName());
                assertEquals(CUSTOMER_EMAIL, response.getBody().getEmail());
                assertEquals("123 Main Street", response.getBody().getAddress());
        }

        @Test
        @Order(7)
        public void testUpdateCustomer_PartialUpdateAddress_Success() {
                // Arrange
                createTestCustomer();

                // Act - Update only address
                CustomerUpdateRequestDto updateDto = new CustomerUpdateRequestDto();
                updateDto.setAddress(UPDATED_ADDRESS);
                HttpEntity<CustomerUpdateRequestDto> request = new HttpEntity<>(updateDto, headers);

                ResponseEntity<CustomerDto> response = restTemplate.exchange(
                                url("/customer/" + CUSTOMER_EMAIL),
                                HttpMethod.PATCH,
                                request,
                                CustomerDto.class);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals("John Doe", response.getBody().getUserName());
                assertEquals(CUSTOMER_EMAIL, response.getBody().getEmail());
                assertEquals(UPDATED_ADDRESS, response.getBody().getAddress());
        }

        @Test
        @Order(8)
        public void testUpdateCustomer_PartialUpdateUsername_Success() {
                // Arrange
                createTestCustomer();

                // Act - Update only username
                CustomerUpdateRequestDto updateDto = new CustomerUpdateRequestDto();
                updateDto.setUserName(UPDATED_USERNAME);
                HttpEntity<CustomerUpdateRequestDto> request = new HttpEntity<>(updateDto, headers);

                ResponseEntity<CustomerDto> response = restTemplate.exchange(
                                url("/customer/" + CUSTOMER_EMAIL),
                                HttpMethod.PATCH,
                                request,
                                CustomerDto.class);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals(UPDATED_USERNAME, response.getBody().getUserName());
                assertEquals(CUSTOMER_EMAIL, response.getBody().getEmail());
                assertEquals("123 Main Street", response.getBody().getAddress());
        }

        @Test
        @Order(9)
        public void testUpdateCustomer_MultipleFields_Success() {
                // Arrange
                createTestCustomer();

                // Act - Update multiple fields
                CustomerUpdateRequestDto updateDto = new CustomerUpdateRequestDto();
                updateDto.setUserName(UPDATED_USERNAME);
                updateDto.setAddress(UPDATED_ADDRESS);
                updateDto.setPassword(UPDATED_PASSWORD);
                HttpEntity<CustomerUpdateRequestDto> request = new HttpEntity<>(updateDto, headers);

                ResponseEntity<CustomerDto> response = restTemplate.exchange(
                                url("/customer/" + CUSTOMER_EMAIL),
                                HttpMethod.PATCH,
                                request,
                                CustomerDto.class);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals(UPDATED_USERNAME, response.getBody().getUserName());
                assertEquals(CUSTOMER_EMAIL, response.getBody().getEmail());
                assertEquals(UPDATED_ADDRESS, response.getBody().getAddress());
        }

        @Test
        @Order(10)
        public void testUpdateCustomer_NotFound_Returns404() {
                // Arrange
                CustomerUpdateRequestDto updateDto = new CustomerUpdateRequestDto();
                updateDto.setUserName(UPDATED_USERNAME);
                HttpEntity<CustomerUpdateRequestDto> request = new HttpEntity<>(updateDto, headers);

                // Act
                ResponseEntity<ErrorResponseDto> response = restTemplate.exchange(
                                url("/customer/nonexistent@test.com"),
                                HttpMethod.PATCH,
                                request,
                                ErrorResponseDto.class);

                // Assert
                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals("Not Found", response.getBody().getError());
                assertTrue(response.getBody().getMessage().contains("No customer found"));
        }

        @Test
        @Order(11)
        public void testGetAllCustomers_Success() {
                // Arrange
                createTestCustomer();

                // Create a second customer
                CustomerRequestDto secondCustomer = createCustomerRequestDto(
                                "Jane Doe",
                                "jane@test.com",
                                "password456",
                                "456 Oak Street");
                HttpEntity<CustomerRequestDto> request = new HttpEntity<>(secondCustomer, headers);
                restTemplate.postForEntity(url("/customer"), request, CustomerDto.class);

                // Act
                ResponseEntity<CustomerDto[]> response = restTemplate.getForEntity(
                                url("/customers"),
                                CustomerDto[].class);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals(2, response.getBody().length);
        }

        @Test
        @Order(12)
        public void testDeleteCustomer_Success() {
                // Arrange
                createTestCustomer();

                // Act
                ResponseEntity<String> response = restTemplate.exchange(
                                url("/customer/" + CUSTOMER_EMAIL),
                                HttpMethod.DELETE,
                                null,
                                String.class);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertTrue(response.getBody().contains("Customer deleted"));

                // Verify customer is deleted
                assertNull(customerRepository.findCustomerByEmail(CUSTOMER_EMAIL));
        }

        // ==================== ARTIST TESTS ====================

        @Test
        @Order(13)
        public void testCreateArtist_Success() {
                // Arrange
                ArtistRequestDto requestDto = createArtistRequestDto(
                                "Jane Artist",
                                ARTIST_EMAIL,
                                "artistPassword");
                HttpEntity<ArtistRequestDto> request = new HttpEntity<>(requestDto, headers);

                // Act
                ResponseEntity<ArtistDto> response = restTemplate.postForEntity(
                                url("/artist"),
                                request,
                                ArtistDto.class);

                // Assert
                assertEquals(HttpStatus.CREATED, response.getStatusCode());
                assertNotNull(response.getBody());

                assertEquals("Jane Artist", response.getBody().getUserName()); // Fixed
                assertEquals(ARTIST_EMAIL, response.getBody().getEmail());

                // Verify in database
                assertTrue(artistRepository.findArtistByEmail(ARTIST_EMAIL) != null);
        }

        @Test
        @Order(14)
        public void testCreateArtist_DuplicateEmail_ThrowsException() {
                // Arrange
                createTestArtist();

                // Act - Try to create another with same email
                ArtistRequestDto requestDto = createArtistRequestDto(
                                "Another Artist",
                                ARTIST_EMAIL,
                                "differentPassword");
                HttpEntity<ArtistRequestDto> request = new HttpEntity<>(requestDto, headers);

                ResponseEntity<ErrorResponseDto> response = restTemplate.postForEntity(
                                url("/artist"),
                                request,
                                ErrorResponseDto.class);

                // Assert
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals("Bad Request", response.getBody().getError());
                assertTrue(response.getBody().getMessage().contains("already registered") ||
                                response.getBody().getMessage().contains("already exists"));
        }

        @Test
        @Order(15)
        public void testGetArtist_Success() {
                // Arrange
                createTestArtist();

                // Act
                ResponseEntity<ArtistDto> response = restTemplate.getForEntity(
                                url("/artist/" + ARTIST_EMAIL),
                                ArtistDto.class);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals("Jane Artist", response.getBody().getUserName());
                assertEquals(ARTIST_EMAIL, response.getBody().getEmail());
        }

        @Test
        @Order(16)
        public void testGetArtist_NotFound_Returns404() {
                // Act
                ResponseEntity<ErrorResponseDto> response = restTemplate.getForEntity(
                                url("/artist/nonexistent@test.com"),
                                ErrorResponseDto.class);

                // Assert
                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals("Not Found", response.getBody().getError());
                assertTrue(response.getBody().getMessage().contains("No artist found") ||
                                response.getBody().getMessage().contains("There is no such Artist"));
        }

        @Test
        @Order(17)
        public void testGetAllArtists_Success() {
                // Arrange
                createTestArtist();

                // Create a second artist
                ArtistRequestDto secondArtist = createArtistRequestDto(
                                "Bob Artist",
                                "bob.artist@test.com",
                                "bobPassword");
                HttpEntity<ArtistRequestDto> request = new HttpEntity<>(secondArtist, headers);
                restTemplate.postForEntity(url("/artist"), request, ArtistDto.class);

                // Act
                ResponseEntity<ArtistDto[]> response = restTemplate.getForEntity(
                                url("/artists"),
                                ArtistDto[].class);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals(2, response.getBody().length);
        }

        @Test
        @Order(18)
        public void testDeleteArtist_Success() {
                // Arrange
                createTestArtist();

                // Act
                ResponseEntity<String> response = restTemplate.exchange(
                                url("/artist/" + ARTIST_EMAIL),
                                HttpMethod.DELETE,
                                null,
                                String.class);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertTrue(response.getBody().contains("Artist deleted"));

                // Verify artist is deleted
                assertNull(artistRepository.findArtistByEmail(ARTIST_EMAIL));
        }

        // ==================== ADMINISTRATOR TESTS ====================

        @Test
        @Order(19)
        public void testCreateAdministrator_Success() {
                // Arrange
                AdministratorRequestDto requestDto = createAdministratorRequestDto(
                                "Admin User",
                                ADMIN_EMAIL,
                                "adminPassword");
                HttpEntity<AdministratorRequestDto> request = new HttpEntity<>(requestDto, headers);

                // Act
                ResponseEntity<AdministratorDto> response = restTemplate.postForEntity(
                                url("/administrator"),
                                request,
                                AdministratorDto.class);

                // Assert
                assertEquals(HttpStatus.CREATED, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals("Admin User", response.getBody().getUserName());
                assertEquals(ADMIN_EMAIL, response.getBody().getEmail());

                // Verify in database
                assertTrue(administratorRepository.findAdministratorByEmail(ADMIN_EMAIL) != null);
        }

        @Test
        @Order(20)
        public void testCreateAdministrator_DuplicateEmail_ThrowsException() {
                // Arrange
                createTestAdministrator();

                // Act - Try to create another with same email
                AdministratorRequestDto requestDto = createAdministratorRequestDto(
                                "Another Admin",
                                ADMIN_EMAIL,
                                "differentPassword");
                HttpEntity<AdministratorRequestDto> request = new HttpEntity<>(requestDto, headers);

                ResponseEntity<ErrorResponseDto> response = restTemplate.postForEntity(
                                url("/administrator"),
                                request,
                                ErrorResponseDto.class);

                // Assert
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals("Bad Request", response.getBody().getError());
                assertTrue(response.getBody().getMessage().contains("already registered") ||
                                response.getBody().getMessage().contains("already exists"));
        }

        @Test
        @Order(21)
        public void testGetAdministrator_Success() {
                // Arrange
                createTestAdministrator();

                // Act
                ResponseEntity<AdministratorDto> response = restTemplate.getForEntity(
                                url("/administrator/" + ADMIN_EMAIL),
                                AdministratorDto.class);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals("Admin User", response.getBody().getUserName());
                assertEquals(ADMIN_EMAIL, response.getBody().getEmail());
        }

        @Test
        @Order(22)
        public void testGetAdministrator_NotFound_Returns404() {
                // Act
                ResponseEntity<ErrorResponseDto> response = restTemplate.getForEntity(
                                url("/administrator/nonexistent@test.com"),
                                ErrorResponseDto.class);

                // Assert
                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals("Not Found", response.getBody().getError());
                assertTrue(response.getBody().getMessage().contains("No administrator found") ||
                                response.getBody().getMessage().contains("There is no such Administrator"));
        }

        @Test
        @Order(24)
        public void testDeleteAdministrator_Success() {
                // Arrange
                createTestAdministrator();

                // Act
                ResponseEntity<String> response = restTemplate.exchange(
                                url("/administrator/" + ADMIN_EMAIL),
                                HttpMethod.DELETE,
                                null,
                                String.class);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertTrue(response.getBody().contains("Administrator deleted"));

                // Verify admin is deleted
                assertNull(administratorRepository.findAdministratorByEmail(ADMIN_EMAIL));
        }

        // ==================== HELPER METHODS ====================

        private void createTestCustomer() {
                CustomerRequestDto requestDto = createCustomerRequestDto(
                                "John Doe",
                                CUSTOMER_EMAIL,
                                "password123",
                                "123 Main Street");
                HttpEntity<CustomerRequestDto> request = new HttpEntity<>(requestDto, headers);
                restTemplate.postForEntity(url("/customer"), request, CustomerDto.class);
        }

        private void createTestArtist() {
                ArtistRequestDto requestDto = createArtistRequestDto(
                                "Jane Artist",
                                ARTIST_EMAIL,
                                "artistPassword");
                HttpEntity<ArtistRequestDto> request = new HttpEntity<>(requestDto, headers);
                restTemplate.postForEntity(url("/artist"), request, ArtistDto.class);
        }

        private void createTestAdministrator() {
                AdministratorRequestDto requestDto = createAdministratorRequestDto(
                                "Admin User",
                                ADMIN_EMAIL,
                                "adminPassword");
                HttpEntity<AdministratorRequestDto> request = new HttpEntity<>(requestDto, headers);
                restTemplate.postForEntity(url("/administrator"), request, AdministratorDto.class);
        }

        private CustomerRequestDto createCustomerRequestDto(String userName, String email,
                        String password, String address) {
                CustomerRequestDto dto = new CustomerRequestDto();
                dto.setUserName(userName);
                dto.setEmail(email);
                dto.setPassword(password);
                dto.setAddress(address);
                return dto;
        }

        private ArtistRequestDto createArtistRequestDto(String userName, String email, String password) {
                ArtistRequestDto dto = new ArtistRequestDto();
                dto.setUserName(userName);
                dto.setEmail(email);
                dto.setPassword(password);
                return dto;
        }

        private AdministratorRequestDto createAdministratorRequestDto(String userName, String email,
                        String password) {
                AdministratorRequestDto dto = new AdministratorRequestDto();
                dto.setUserName(userName);
                dto.setEmail(email);
                dto.setPassword(password);
                return dto;
        }

        private String url(String path) {
                return baseUrl + path;
        }
}