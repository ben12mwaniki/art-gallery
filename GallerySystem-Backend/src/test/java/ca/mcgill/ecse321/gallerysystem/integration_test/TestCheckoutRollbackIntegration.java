package ca.mcgill.ecse321.gallerysystem.integration_test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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
import ca.mcgill.ecse321.gallerysystem.model.SelectedItem;
import ca.mcgill.ecse321.gallerysystem.model.ShoppingCart;
import ca.mcgill.ecse321.gallerysystem.service.GallerySystemService;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = { "spring.profiles.active=test" })
public class TestCheckoutRollbackIntegration {

    @Autowired
    private GallerySystemService service;

    /*
     * @SpyBean wraps the REAL Spring-managed OrderRepository bean. Unlike a
     * 
     * @Mock, calls we don't explicitly stub still hit the real DB. We only
     * override save(Order) to force a deterministic collision - everything
     * else in checkout() (customer/cart/art-piece writes) goes through
     * genuinely, so this stays a real integration test, not a unit test in
     * disguise.
     */
    @SpyBean
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private ArtistRepository artistRepository;
    @Autowired
    private ArtPieceRepository artPieceRepository;
    @Autowired
    private ShoppingCartRepository shoppingCartRepository;
    @Autowired
    private SelectedItemRepository selectedItemRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;

    private static final String CUSTOMER_EMAIL = "rollback-test@email.com";
    private Integer artID;

    @BeforeEach
    public void setup() {
        Artist artist = new Artist();
        artist.setEmail("artist-rollback@email.com");
        artist.setPassword("password");
        artist.setUserName("artist");
        artistRepository.save(artist);

        ArtPiece art = new ArtPiece();
        art.setArtName("Rollback Test Piece");
        art.setDescription("Used to verify stock rollback");
        art.setPrice(100.0f);
        art.setDiscountPercentage(0);
        art.setCommissionPercentage(10.0f);
        art.setQuantity(5);
        art.setActive(true);
        art.setArtist(artist);
        art = artPieceRepository.save(art);
        artID = art.getArtID();

        Customer customer = new Customer();
        customer.setEmail(CUSTOMER_EMAIL);
        customer.setPassword("password");
        customer.setUserName("customer");
        customer = customerRepository.save(customer);

        SelectedItem item = new SelectedItem();
        item.setArtPiece(art);
        item.setItemQuantity(2);

        ShoppingCart cart = new ShoppingCart();
        cart.setCustomer(customer);
        Set<SelectedItem> items = new HashSet<>();
        item.setShoppingCart(cart);
        items.add(item);
        cart.setSelectedItems(items);
        cart = shoppingCartRepository.save(cart);
        selectedItemRepository.save(item);
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

    /**
     * Forces every Order save attempt to collide, exhausting checkout()'s
     * retry loop. checkout() is @Transactional, so when it throws
     * IllegalStateException (a RuntimeException), Spring must roll back
     * the entire transaction - including the ArtPiece stock decrement and
     * the cart mutation that happened earlier in the same method, even
     * though those writes succeeded individually before the failure.
     *
     * Deliberately NOT annotating this test method with @Transactional:
     * doing so would make checkout()'s @Transactional join the test's
     * transaction instead of owning its own, and reads after the throw
     * would come from the same persistence context rather than proving a
     * real DB-level rollback occurred.
     */
    @Test
    public void testCheckoutRollsBackStockAndCartOnRepeatedOrderNumberCollision() {

        doThrow(new DataIntegrityViolationException("forced collision"))
                .when(orderRepository).save(any(Order.class));

        assertThrows(IllegalStateException.class, () -> service.checkout(CUSTOMER_EMAIL));

        // Fresh reads against the real DB, outside the failed transaction.
        ArtPiece reloadedArt = artPieceRepository.findArtPieceByArtID(artID);
        assertEquals(Integer.valueOf(5), reloadedArt.getQuantity());

        ShoppingCart reloadedCart = shoppingCartRepository.findShoppingCartByCustomerEmail(CUSTOMER_EMAIL);
        assertEquals(1, reloadedCart.getSelectedItems().size());

        assertEquals(0, orderRepository.count());
        assertEquals(0, orderItemRepository.count());
    }
}