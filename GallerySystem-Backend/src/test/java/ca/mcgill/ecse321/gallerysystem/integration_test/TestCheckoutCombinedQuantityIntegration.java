package ca.mcgill.ecse321.gallerysystem.integration_test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;

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
import ca.mcgill.ecse321.gallerysystem.model.SelectedItem;
import ca.mcgill.ecse321.gallerysystem.model.ShoppingCart;
import ca.mcgill.ecse321.gallerysystem.service.GallerySystemService;

@SpringBootTest(properties = "spring.profiles.active=test")
public class TestCheckoutCombinedQuantityIntegration {

    private static final String CUSTOMER_EMAIL = "combined-quantity@email.com";

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
    public void testCheckoutRejectsCombinedQuantityThatExceedsStock() {
        ArtPiece artPiece = createCartWithTwoItemsOfTheSameArtPiece();

        assertThrows(IllegalArgumentException.class,
                () -> service.checkout(CUSTOMER_EMAIL));

        ArtPiece reloadedArtPiece = artPieceRepository
                .findArtPieceByArtID(artPiece.getArtID());
        assertEquals(Integer.valueOf(3), reloadedArtPiece.getQuantity());

        ShoppingCart reloadedCart = shoppingCartRepository
                .findShoppingCartByCustomerEmail(CUSTOMER_EMAIL);
        assertEquals(2, reloadedCart.getSelectedItems().size());

        assertEquals(0, orderRepository.count());
        assertEquals(0, orderItemRepository.count());
    }

    private ArtPiece createCartWithTwoItemsOfTheSameArtPiece() {
        Artist artist = new Artist();
        artist.setEmail("combined-artist@email.com");
        artist.setUserName("Combined Quantity Artist");
        artist.setPassword("password");
        artist = artistRepository.save(artist);

        ArtPiece artPiece = new ArtPiece();
        artPiece.setArtName("Limited Edition Piece");
        artPiece.setDescription("Only three units are available");
        artPiece.setPrice(50.0f);
        artPiece.setDiscountPercentage(0);
        artPiece.setCommissionPercentage(10.0f);
        artPiece.setQuantity(3);
        artPiece.setActive(true);
        artPiece.setArtist(artist);
        artPiece = artPieceRepository.save(artPiece);

        Customer customer = new Customer();
        customer.setEmail(CUSTOMER_EMAIL);
        customer.setUserName("Combined Quantity Customer");
        customer.setAddress("456 Test Street");
        customer.setPassword("password");
        customer = customerRepository.save(customer);

        ShoppingCart cart = new ShoppingCart();
        cart.setCustomer(customer);
        cart.setSelectedItems(new HashSet<SelectedItem>());
        cart = shoppingCartRepository.save(cart);

        addSelectedItem(cart, artPiece, 2);
        addSelectedItem(cart, artPiece, 2);
        shoppingCartRepository.save(cart);

        return artPiece;
    }

    private void addSelectedItem(ShoppingCart cart, ArtPiece artPiece, int quantity) {
        SelectedItem selectedItem = new SelectedItem();
        selectedItem.setArtPiece(artPiece);
        selectedItem.setItemQuantity(quantity);
        selectedItem.setShoppingCart(cart);
        selectedItem = selectedItemRepository.save(selectedItem);
        cart.getSelectedItems().add(selectedItem);
    }
}
