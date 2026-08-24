package ca.mcgill.ecse321.gallerysystem.integration_test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.gallerysystem.dao.ArtPieceRepository;
import ca.mcgill.ecse321.gallerysystem.dao.ArtistRepository;
import ca.mcgill.ecse321.gallerysystem.dao.CustomerRepository;
import ca.mcgill.ecse321.gallerysystem.dao.OrderItemRepository;
import ca.mcgill.ecse321.gallerysystem.dao.OrderRepository;
import ca.mcgill.ecse321.gallerysystem.dao.SelectedItemRepository;
import ca.mcgill.ecse321.gallerysystem.dao.ShoppingCartRepository;
import ca.mcgill.ecse321.gallerysystem.model.ArtPiece;
import ca.mcgill.ecse321.gallerysystem.model.Artist;
import ca.mcgill.ecse321.gallerysystem.model.Customer;
import ca.mcgill.ecse321.gallerysystem.model.Order;
import ca.mcgill.ecse321.gallerysystem.model.OrderItem;
import ca.mcgill.ecse321.gallerysystem.model.SelectedItem;
import ca.mcgill.ecse321.gallerysystem.model.ShoppingCart;
import ca.mcgill.ecse321.gallerysystem.service.GallerySystemService;

@SpringBootTest(properties = "spring.profiles.active=test")
public class TestCheckoutIntegration {

    private static final String CUSTOMER_EMAIL = "checkout-customer@email.com";

    @Autowired
    private GallerySystemService service;
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
    public void testCheckoutPersistsOrderSnapshotsUpdatesStockAndEmptiesCart() {
        ArtPiece artPiece = createCartWithItems(5, 2);

        Order checkedOutOrder = service.checkout(CUSTOMER_EMAIL);

        Order savedOrder = orderRepository
                .findOrderByOrderNumber(checkedOutOrder.getOrderNumber());
        assertNotNull(savedOrder);
        assertEquals(CUSTOMER_EMAIL, savedOrder.getCustomer().getEmail());

        List<OrderItem> savedOrderItems = toList(orderItemRepository.findAll());
        assertEquals(1, savedOrderItems.size());

        OrderItem savedOrderItem = savedOrderItems.get(0);
        assertEquals(savedOrder.getOrderNumber(),
                savedOrderItem.getOrder().getOrderNumber());
        assertEquals(Integer.valueOf(2), savedOrderItem.getQuantity());
        assertEquals(100.0f, savedOrderItem.getListPrice());
        assertEquals(Integer.valueOf(10), savedOrderItem.getDiscountPercentage());
        assertEquals(90.0f, savedOrderItem.getUnitPrice());
        assertEquals(20.0f, savedOrderItem.getCommissionPercentage());
        assertEquals("Integration Test Piece", savedOrderItem.getArtName());
        assertEquals("A persisted checkout snapshot", savedOrderItem.getDescription());

        ArtPiece reloadedArtPiece = artPieceRepository
                .findArtPieceByArtID(artPiece.getArtID());
        assertEquals(Integer.valueOf(3), reloadedArtPiece.getQuantity());

        ShoppingCart reloadedCart = shoppingCartRepository
                .findShoppingCartByCustomerEmail(CUSTOMER_EMAIL);
        assertEquals(0, reloadedCart.getSelectedItems().size());
        assertEquals(true, reloadedCart.isEmpty());
    }

    private ArtPiece createCartWithItems(int availableStock, int... quantities) {
        Artist artist = new Artist();
        artist.setEmail("checkout-artist@email.com");
        artist.setUserName("Checkout Artist");
        artist.setPassword("password");
        artist = artistRepository.save(artist);

        ArtPiece artPiece = new ArtPiece();
        artPiece.setArtName("Integration Test Piece");
        artPiece.setDescription("A persisted checkout snapshot");
        artPiece.setPrice(100.0f);
        artPiece.setDiscountPercentage(10);
        artPiece.setCommissionPercentage(20.0f);
        artPiece.setQuantity(availableStock);
        artPiece.setActive(true);
        artPiece.setArtist(artist);
        artPiece = artPieceRepository.save(artPiece);

        Customer customer = new Customer();
        customer.setEmail(CUSTOMER_EMAIL);
        customer.setUserName("Checkout Customer");
        customer.setAddress("123 Test Street");
        customer.setPassword("password");
        customer = customerRepository.save(customer);

        ShoppingCart cart = new ShoppingCart();
        cart.setCustomer(customer);
        cart.setSelectedItems(new HashSet<SelectedItem>());
        cart = shoppingCartRepository.save(cart);

        Set<SelectedItem> selectedItems = cart.getSelectedItems();
        for (int quantity : quantities) {
            SelectedItem selectedItem = new SelectedItem();
            selectedItem.setArtPiece(artPiece);
            selectedItem.setItemQuantity(quantity);
            selectedItem.setShoppingCart(cart);
            selectedItem = selectedItemRepository.save(selectedItem);
            selectedItems.add(selectedItem);
        }
        shoppingCartRepository.save(cart);

        return artPiece;
    }

    private <T> List<T> toList(Iterable<T> iterable) {
        List<T> result = new java.util.ArrayList<T>();
        for (T element : iterable) {
            result.add(element);
        }
        return result;
    }
}
