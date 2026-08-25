package ca.mcgill.ecse321.gallerysystem.integration_test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ca.mcgill.ecse321.gallerysystem.dao.ArtPieceRepository;
import ca.mcgill.ecse321.gallerysystem.dao.ArtistRepository;
import ca.mcgill.ecse321.gallerysystem.dao.CustomerRepository;
import ca.mcgill.ecse321.gallerysystem.dao.OrderItemRepository;
import ca.mcgill.ecse321.gallerysystem.dao.OrderRepository;
import ca.mcgill.ecse321.gallerysystem.dao.SelectedItemRepository;
import ca.mcgill.ecse321.gallerysystem.dao.ShoppingCartRepository;
import ca.mcgill.ecse321.gallerysystem.dto.OrderDto;
import ca.mcgill.ecse321.gallerysystem.dto.OrderItemDto;
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

        @BeforeEach
        public void setup() {
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
                ResponseEntity<String> createCartResponse = restTemplate.postForEntity(
                                url("/shopping-carts/" + CUSTOMER_EMAIL), null, String.class);
                assertEquals(HttpStatus.CREATED, createCartResponse.getStatusCode());

                ResponseEntity<String> addItemResponse = restTemplate.postForEntity(
                                url("/shopping-carts/" + CUSTOMER_EMAIL
                                                + "/items?artID=" + artID + "&quantity=2"),
                                null,
                                String.class);
                assertEquals(HttpStatus.CREATED, addItemResponse.getStatusCode());

                ResponseEntity<String> listItemsResponse = restTemplate.getForEntity(
                                url("/shopping-carts/" + CUSTOMER_EMAIL + "/items"),
                                String.class);
                assertEquals(HttpStatus.OK, listItemsResponse.getStatusCode());

                List<SelectedItem> selectedItems = toList(selectedItemRepository.findAll());
                assertEquals(1, selectedItems.size());
                assertEquals(Integer.valueOf(2), selectedItems.get(0).getItemQuantity());

                Integer itemID = selectedItems.get(0).getItemID();
                ResponseEntity<String> deleteItemResponse = restTemplate.exchange(
                                url("/shopping-carts/" + CUSTOMER_EMAIL + "/items/" + itemID),
                                HttpMethod.DELETE,
                                null,
                                String.class);
                assertEquals(HttpStatus.NO_CONTENT, deleteItemResponse.getStatusCode());

                ShoppingCart reloadedCart = shoppingCartRepository
                                .findShoppingCartByCustomerEmail(CUSTOMER_EMAIL);
                assertEquals(0, reloadedCart.getSelectedItems().size());
                assertEquals(0, selectedItemRepository.count());
        }

        @Test
        public void testCheckoutThroughControllerPersistsOrderAndEmptiesCart() {
                createCartAndAddOneItem();

                ResponseEntity<String> checkoutResponse = restTemplate.postForEntity(
                                url("/customers/" + CUSTOMER_EMAIL + "/checkout"), null, String.class);

                assertEquals(HttpStatus.CREATED, checkoutResponse.getStatusCode());

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
                                url("/customers/" + CUSTOMER_EMAIL + "/checkout"), null, OrderDto.class);
                assertEquals(HttpStatus.CREATED, checkoutResponse.getStatusCode());

                ResponseEntity<OrderDto[]> beforeDeletionResponse = restTemplate.getForEntity(
                                url("/customers/" + CUSTOMER_EMAIL + "/orders"), OrderDto[].class);
                assertEquals(HttpStatus.OK, beforeDeletionResponse.getStatusCode());
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

                ResponseEntity<String> deleteArtPieceResponse = restTemplate.exchange(
                                url("/artpiece/" + artID), HttpMethod.DELETE, null, String.class);
                assertEquals(HttpStatus.OK, deleteArtPieceResponse.getStatusCode());

                ResponseEntity<OrderDto[]> afterDeletionResponse = restTemplate.getForEntity(
                                url("/customers/" + CUSTOMER_EMAIL + "/orders"), OrderDto[].class);
                assertEquals(HttpStatus.OK, afterDeletionResponse.getStatusCode());
                assertEquals(1, afterDeletionResponse.getBody().length);
                assertEquals(1, afterDeletionResponse.getBody()[0].getOrderItems().size());
                OrderItemDto afterDeletionItem = afterDeletionResponse.getBody()[0].getOrderItems().get(0);
                assertNull(afterDeletionItem.getArtPieceID());
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
                ResponseEntity<String> createCartResponse = restTemplate.postForEntity(
                                url("/shopping-carts/" + CUSTOMER_EMAIL), null, String.class);
                assertEquals(HttpStatus.CREATED, createCartResponse.getStatusCode());

                ResponseEntity<String> checkoutResponse = restTemplate.postForEntity(
                                url("/customers/" + CUSTOMER_EMAIL + "/checkout"), null, String.class);

                assertEquals(HttpStatus.BAD_REQUEST, checkoutResponse.getStatusCode());
                assertTrue(checkoutResponse.getBody()
                                .contains("Cannot checkout an empty cart!"));
                assertEquals(0, orderRepository.count());
        }

        private void createCartAndAddOneItem() {
                ResponseEntity<String> createCartResponse = restTemplate.postForEntity(
                                url("/shopping-carts/" + CUSTOMER_EMAIL), null, String.class);
                assertEquals(HttpStatus.CREATED, createCartResponse.getStatusCode());

                ResponseEntity<String> addItemResponse = restTemplate.postForEntity(
                                url("/shopping-carts/" + CUSTOMER_EMAIL
                                                + "/items?artID=" + artID + "&quantity=2"),
                                null,
                                String.class);
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