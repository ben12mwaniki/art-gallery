package ca.mcgill.ecse321.gallerysystem.integration_test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.springframework.http.ResponseEntity;

import ca.mcgill.ecse321.gallerysystem.dao.ArtPieceRepository;
import ca.mcgill.ecse321.gallerysystem.dao.ArtistRepository;
import ca.mcgill.ecse321.gallerysystem.dao.CustomerRepository;
import ca.mcgill.ecse321.gallerysystem.dao.OrderItemRepository;
import ca.mcgill.ecse321.gallerysystem.dao.OrderRepository;
import ca.mcgill.ecse321.gallerysystem.dao.SelectedItemRepository;
import ca.mcgill.ecse321.gallerysystem.dao.ShoppingCartRepository;
import ca.mcgill.ecse321.gallerysystem.dto.ErrorResponseDto;
import ca.mcgill.ecse321.gallerysystem.dto.OrderDto;
import ca.mcgill.ecse321.gallerysystem.dto.OrderItemDto;
import ca.mcgill.ecse321.gallerysystem.dto.SelectedItemDto;
import ca.mcgill.ecse321.gallerysystem.dto.SelectedItemRequestDto;
import ca.mcgill.ecse321.gallerysystem.dto.ShoppingCartDto;
import ca.mcgill.ecse321.gallerysystem.model.ArtPiece;
import ca.mcgill.ecse321.gallerysystem.model.Artist;
import ca.mcgill.ecse321.gallerysystem.model.Customer;
import ca.mcgill.ecse321.gallerysystem.model.SelectedItem;
import ca.mcgill.ecse321.gallerysystem.model.ShoppingCart;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = "spring.profiles.active=test")
public class TestCartAndCheckoutControllerIntegration {

        private static final String CUSTOMER_EMAIL = "controller-customer@email.com";

        @LocalServerPort
        private int port;

        @Autowired
        private TestRestTemplate restTemplate;
        @Autowired
        private ArtistRepository artistRepository;
        @Autowired
        private ArtPieceRepository artPieceRepository;
        @Autowired
        private CustomerRepository customerRepository;
        @Autowired
        private ShoppingCartRepository shoppingCartRepository;
        @Autowired
        private SelectedItemRepository selectedItemRepository;
        @Autowired
        private OrderRepository orderRepository;
        @Autowired
        private OrderItemRepository orderItemRepository;

        private Integer artID;
        private HttpHeaders headers;

        @BeforeEach
        public void setup() {
                // Setup JSON headers
                headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(java.util.Arrays.asList(MediaType.APPLICATION_JSON));

                Artist artist = new Artist();
                artist.setEmail("controller-artist@email.com");
                artist.setUserName("Controller Artist");
                artist.setPassword("password");
                artist = artistRepository.save(artist);

                ArtPiece artPiece = new ArtPiece();
                artPiece.setArtName("Controller Test Piece");
                artPiece.setDescription("Used by controller integration tests");
                artPiece.setPrice(100.0f);
                artPiece.setDiscountPercentage(10);
                artPiece.setCommissionPercentage(20.0f);
                artPiece.setQuantity(5);
                artPiece.setActive(true);
                artPiece.setArtist(artist);
                artPiece = artPieceRepository.save(artPiece);
                artID = artPiece.getArtID();

                Customer customer = new Customer();
                customer.setEmail(CUSTOMER_EMAIL);
                customer.setUserName("Controller Customer");
                customer.setAddress("123 Controller Street");
                customer.setPassword("password");
                customerRepository.save(customer);
        }

        @AfterEach
        public void cleanup() {
                orderItemRepository.deleteAll();
                orderRepository.deleteAll();
                selectedItemRepository.deleteAll();
                shoppingCartRepository.deleteAll();
                artPieceRepository.deleteAll();
                customerRepository.deleteAll();
                artistRepository.deleteAll();
        }

        @Test
        public void testCreateCartAddListAndRemoveSelectedItem() {
                // 1. Create cart
                ResponseEntity<String> createCartResponse = restTemplate.postForEntity(
                                url("/shopping-carts/" + CUSTOMER_EMAIL), null, String.class);
                assertEquals(HttpStatus.CREATED, createCartResponse.getStatusCode());

                // 2. Add item using Request DTO
                SelectedItemRequestDto addItemRequest = new SelectedItemRequestDto();
                addItemRequest.setArtID(artID);
                addItemRequest.setQuantity(2);

                HttpEntity<SelectedItemRequestDto> addItemEntity = new HttpEntity<>(addItemRequest, headers);

                ResponseEntity<SelectedItemDto> addItemResponse = restTemplate.postForEntity(
                                url("/shopping-carts/" + CUSTOMER_EMAIL + "/items"),
                                addItemEntity,
                                SelectedItemDto.class);
                assertEquals(HttpStatus.CREATED, addItemResponse.getStatusCode());
                assertNotNull(addItemResponse.getBody());
                assertEquals(Integer.valueOf(2), addItemResponse.getBody().getItemQuantity());
                assertEquals(artID, addItemResponse.getBody().getArtID());

                // 3. List items
                ResponseEntity<SelectedItemDto[]> listItemsResponse = restTemplate.getForEntity(
                                url("/shopping-carts/" + CUSTOMER_EMAIL + "/items"),
                                SelectedItemDto[].class);
                assertEquals(HttpStatus.OK, listItemsResponse.getStatusCode());
                assertNotNull(listItemsResponse.getBody());
                assertEquals(1, listItemsResponse.getBody().length);

                // 4. Verify from database
                List<SelectedItem> selectedItems = toList(selectedItemRepository.findAll());
                assertEquals(1, selectedItems.size());
                assertEquals(Integer.valueOf(2), selectedItems.get(0).getItemQuantity());

                // 5. Delete item
                Integer itemID = selectedItems.get(0).getItemID();
                ResponseEntity<Void> deleteItemResponse = restTemplate.exchange(
                                url("/shopping-carts/" + CUSTOMER_EMAIL + "/items/" + itemID),
                                HttpMethod.DELETE,
                                null,
                                Void.class);
                assertEquals(HttpStatus.NO_CONTENT, deleteItemResponse.getStatusCode());

                // 6. Verify cart is empty
                ShoppingCart reloadedCart = shoppingCartRepository
                                .findShoppingCartByCustomerEmail(CUSTOMER_EMAIL);
                assertEquals(0, reloadedCart.getSelectedItems().size());
                assertEquals(0, selectedItemRepository.count());
        }

        @Test
        public void testCheckoutThroughControllerPersistsOrderAndEmptiesCart() {
                createCartAndAddOneItem();

                ResponseEntity<OrderDto> checkoutResponse = restTemplate.postForEntity(
                                url("/customers/" + CUSTOMER_EMAIL + "/checkout"),
                                null,
                                OrderDto.class);

                assertEquals(HttpStatus.CREATED, checkoutResponse.getStatusCode());
                assertNotNull(checkoutResponse.getBody());

                assertEquals(1, orderRepository.count());
                assertEquals(1, orderItemRepository.count());

                ArtPiece reloadedArtPiece = artPieceRepository
                                .findArtPieceByArtID(artID);
                assertEquals(Integer.valueOf(3), reloadedArtPiece.getQuantity());

                ShoppingCart reloadedCart = shoppingCartRepository
                                .findShoppingCartByCustomerEmail(CUSTOMER_EMAIL);
                assertEquals(0, reloadedCart.getSelectedItems().size());
        }

        @Test
        public void testOrderHistoryPreservesSnapshotsAfterArtPieceDeletion() {
                createCartAndAddOneItem();

                ResponseEntity<OrderDto> checkoutResponse = restTemplate.postForEntity(
                                url("/customers/" + CUSTOMER_EMAIL + "/checkout"),
                                null,
                                OrderDto.class);
                assertEquals(HttpStatus.CREATED, checkoutResponse.getStatusCode());
                assertNotNull(checkoutResponse.getBody());

                ResponseEntity<OrderDto[]> beforeDeletionResponse = restTemplate.getForEntity(
                                url("/customers/" + CUSTOMER_EMAIL + "/orders"),
                                OrderDto[].class);
                assertEquals(HttpStatus.OK, beforeDeletionResponse.getStatusCode());
                assertNotNull(beforeDeletionResponse.getBody());
                assertEquals(1, beforeDeletionResponse.getBody().length);
                assertEquals(1, beforeDeletionResponse.getBody()[0].getOrderItems().size());

                OrderItemDto beforeDeletionItem = beforeDeletionResponse.getBody()[0].getOrderItems().get(0);
                assertEquals(artID, beforeDeletionItem.getArtPieceID());
                assertEquals(Integer.valueOf(2), beforeDeletionItem.getQuantity());
                assertEquals(100.0f, beforeDeletionItem.getListPrice());
                assertEquals(90.0f, beforeDeletionItem.getUnitPrice());
                assertEquals(Integer.valueOf(10), beforeDeletionItem.getDiscountPercentage());
                assertEquals(20.0f, beforeDeletionItem.getCommissionPercentage());
                assertEquals("Controller Test Piece", beforeDeletionItem.getArtName());
                assertEquals("Used by controller integration tests", beforeDeletionItem.getDescription());

                // Delete the art piece
                ResponseEntity<String> deleteArtPieceResponse = restTemplate.exchange(
                                url("/artpiece/" + artID),
                                HttpMethod.DELETE,
                                null,
                                String.class);
                assertEquals(HttpStatus.OK, deleteArtPieceResponse.getStatusCode());
                assertTrue(deleteArtPieceResponse.getBody().contains("Artpiece deleted"));

                // Verify order history still preserved
                ResponseEntity<OrderDto[]> afterDeletionResponse = restTemplate.getForEntity(
                                url("/customers/" + CUSTOMER_EMAIL + "/orders"),
                                OrderDto[].class);
                assertEquals(HttpStatus.OK, afterDeletionResponse.getStatusCode());
                assertNotNull(afterDeletionResponse.getBody());
                assertEquals(1, afterDeletionResponse.getBody().length);
                assertEquals(1, afterDeletionResponse.getBody()[0].getOrderItems().size());

                OrderItemDto afterDeletionItem = afterDeletionResponse.getBody()[0].getOrderItems().get(0);
                assertNull(afterDeletionItem.getArtPieceID()); // ArtPiece reference is null (snapshot preserved)
                assertEquals(beforeDeletionItem.getQuantity(), afterDeletionItem.getQuantity());
                assertEquals(beforeDeletionItem.getListPrice(), afterDeletionItem.getListPrice());
                assertEquals(beforeDeletionItem.getUnitPrice(), afterDeletionItem.getUnitPrice());
                assertEquals(beforeDeletionItem.getDiscountPercentage(), afterDeletionItem.getDiscountPercentage());
                assertEquals(beforeDeletionItem.getCommissionPercentage(), afterDeletionItem.getCommissionPercentage());
                assertEquals(beforeDeletionItem.getArtName(), afterDeletionItem.getArtName());
                assertEquals(beforeDeletionItem.getDescription(), afterDeletionItem.getDescription());
        }

        @Test
        public void testEmptyCartCheckoutReturnsBadRequest() {
                // Create empty cart
                ResponseEntity<String> createCartResponse = restTemplate.postForEntity(
                                url("/shopping-carts/" + CUSTOMER_EMAIL),
                                null,
                                String.class);
                assertEquals(HttpStatus.CREATED, createCartResponse.getStatusCode());

                // Try to checkout empty cart - Expect ErrorResponseDto
                ResponseEntity<ErrorResponseDto> checkoutResponse = restTemplate.postForEntity(
                                url("/customers/" + CUSTOMER_EMAIL + "/checkout"),
                                null,
                                ErrorResponseDto.class);

                assertEquals(HttpStatus.BAD_REQUEST, checkoutResponse.getStatusCode());
                assertNotNull(checkoutResponse.getBody());
                assertEquals("Bad Request", checkoutResponse.getBody().getError());
                assertTrue(checkoutResponse.getBody().getMessage().contains("Cannot checkout an empty cart!"));
                assertEquals(0, orderRepository.count());
        }

        @Test
        public void testCreateCartForExistingCustomer_Success() {
                // Create cart
                ResponseEntity<ShoppingCartDto> createCartResponse = restTemplate.postForEntity(
                                url("/shopping-carts/" + CUSTOMER_EMAIL),
                                null,
                                ShoppingCartDto.class);
                assertEquals(HttpStatus.CREATED, createCartResponse.getStatusCode());
                assertNotNull(createCartResponse.getBody());
                assertTrue(createCartResponse.getBody().getIsEmpty());

                // Verify cart exists in database
                ShoppingCart cart = shoppingCartRepository.findShoppingCartByCustomerEmail(CUSTOMER_EMAIL);
                assertNotNull(cart);
                assertEquals(0, cart.getItemCount());
                assertTrue(cart.isEmpty());
        }

        @Test
        public void testGetShoppingCart_Success() {
                // Create cart first
                restTemplate.postForEntity(
                                url("/shopping-carts/" + CUSTOMER_EMAIL),
                                null,
                                String.class);

                // Get cart
                ResponseEntity<ShoppingCartDto> getCartResponse = restTemplate.getForEntity(
                                url("/shopping-carts/" + CUSTOMER_EMAIL),
                                ShoppingCartDto.class);

                assertEquals(HttpStatus.OK, getCartResponse.getStatusCode());
                assertNotNull(getCartResponse.getBody());
                assertEquals(0, getCartResponse.getBody().getItemCount());
                assertTrue(getCartResponse.getBody().getIsEmpty());
        }

        @Test
        public void testEmptyShoppingCart_Success() {
                // Create cart and add items
                createCartAndAddOneItem();

                // Empty the cart
                ResponseEntity<Void> emptyCartResponse = restTemplate.exchange(
                                url("/shopping-carts/" + CUSTOMER_EMAIL + "/items"),
                                HttpMethod.DELETE,
                                null,
                                Void.class);
                assertEquals(HttpStatus.NO_CONTENT, emptyCartResponse.getStatusCode());

                // Verify cart is empty
                ShoppingCart reloadedCart = shoppingCartRepository
                                .findShoppingCartByCustomerEmail(CUSTOMER_EMAIL);
                assertEquals(0, reloadedCart.getSelectedItems().size());
                assertEquals(0, selectedItemRepository.count());
        }

        @Test
        public void testGetOrdersByCustomer_Success() {
                // Create and checkout an order
                createCartAndAddOneItem();
                restTemplate.postForEntity(
                                url("/customers/" + CUSTOMER_EMAIL + "/checkout"),
                                null,
                                OrderDto.class);

                // Get orders
                ResponseEntity<OrderDto[]> ordersResponse = restTemplate.getForEntity(
                                url("/customers/" + CUSTOMER_EMAIL + "/orders"),
                                OrderDto[].class);

                assertEquals(HttpStatus.OK, ordersResponse.getStatusCode());
                assertNotNull(ordersResponse.getBody());
                assertEquals(1, ordersResponse.getBody().length);

                OrderDto order = ordersResponse.getBody()[0];
                assertEquals(CUSTOMER_EMAIL, order.getCustomerEmail());
                assertEquals(1, order.getOrderItems().size());
        }

        private void createCartAndAddOneItem() {
                // Create cart
                ResponseEntity<String> createCartResponse = restTemplate.postForEntity(
                                url("/shopping-carts/" + CUSTOMER_EMAIL),
                                null,
                                String.class);
                assertEquals(HttpStatus.CREATED, createCartResponse.getStatusCode());

                // Add item using Request DTO
                SelectedItemRequestDto addItemRequest = new SelectedItemRequestDto();
                addItemRequest.setArtID(artID);
                addItemRequest.setQuantity(2);

                HttpEntity<SelectedItemRequestDto> addItemEntity = new HttpEntity<>(addItemRequest, headers);

                ResponseEntity<SelectedItemDto> addItemResponse = restTemplate.postForEntity(
                                url("/shopping-carts/" + CUSTOMER_EMAIL + "/items"),
                                addItemEntity,
                                SelectedItemDto.class);
                assertEquals(HttpStatus.CREATED, addItemResponse.getStatusCode());
        }

        private String url(String path) {
                return "http://localhost:" + port + path;
        }

        private <T> List<T> toList(Iterable<T> iterable) {
                List<T> result = new ArrayList<T>();
                for (T element : iterable) {
                        result.add(element);
                }
                return result;
        }
}