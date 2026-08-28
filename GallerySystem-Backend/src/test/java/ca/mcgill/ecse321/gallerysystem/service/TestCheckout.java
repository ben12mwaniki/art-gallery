package ca.mcgill.ecse321.gallerysystem.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.dao.DataIntegrityViolationException;

import ca.mcgill.ecse321.gallerysystem.dao.ArtPieceRepository;
import ca.mcgill.ecse321.gallerysystem.dao.CustomerRepository;
import ca.mcgill.ecse321.gallerysystem.dao.OrderRepository;
import ca.mcgill.ecse321.gallerysystem.dao.ShoppingCartRepository;
import ca.mcgill.ecse321.gallerysystem.exception.ResourceNotFoundException;
import ca.mcgill.ecse321.gallerysystem.model.ArtPiece;
import ca.mcgill.ecse321.gallerysystem.model.Customer;
import ca.mcgill.ecse321.gallerysystem.model.Order;
import ca.mcgill.ecse321.gallerysystem.model.OrderItem;
import ca.mcgill.ecse321.gallerysystem.model.SelectedItem;
import ca.mcgill.ecse321.gallerysystem.model.ShoppingCart;

@ExtendWith(MockitoExtension.class)
public class TestCheckout {
    @Mock
    private CustomerRepository customerDao;
    @Mock
    private ShoppingCartRepository shoppingCartDao;
    @Mock
    private ArtPieceRepository artPieceDao;
    @Mock
    private OrderRepository orderDao;

    @InjectMocks
    private GallerySystemService service;

    private Customer customer;
    private ShoppingCart cart;
    private ArtPiece art;
    private SelectedItem selectedItem;

    @BeforeEach
    public void setMockOutput() {
        Answer<?> returnParameterAsAnswer = (InvocationOnMock invocation) -> {
            return invocation.getArgument(0);
        };
        lenient().when(orderDao.save(any(Order.class))).thenAnswer(returnParameterAsAnswer);
        lenient().when(artPieceDao.save(any(ArtPiece.class))).thenAnswer(returnParameterAsAnswer);
        lenient().when(shoppingCartDao.save(any(ShoppingCart.class))).thenAnswer(returnParameterAsAnswer);

        // Daily sequence starts at zero unless a test overrides it.
        lenient().when(orderDao.countByOrderNumberBetween(any(Integer.class), any(Integer.class)))
                .thenReturn(0);

        customer = new Customer();
        customer.setEmail("alice@email.com");
        customer.setUserName("Alice");

        art = new ArtPiece();
        art.setArtID(1);
        art.setArtName("Starry Night");
        art.setDescription("A test piece");
        art.setPrice(100.0f);
        art.setDiscountPercentage(10);
        art.setCommissionPercentage(20.0f);
        art.setQuantity(5);
        art.setActive(true);

        selectedItem = new SelectedItem();
        selectedItem.setArtPiece(art);
        selectedItem.setItemQuantity(2);

        Set<SelectedItem> items = new HashSet<>();
        items.add(selectedItem);

        cart = new ShoppingCart();
        cart.setCustomer(customer);
        cart.setSelectedItems(items);
    }

    /**
     * Happy path: valid customer, valid non-empty cart, sufficient stock.
     * Verifies the returned Order's snapshot fields, that stock was
     * decremented on the ArtPiece, and that the cart was emptied.
     */
    @Test
    public void testCheckoutHappyPath() {
        lenient().when(customerDao.findCustomerByEmail("alice@email.com")).thenReturn(customer);
        lenient().when(shoppingCartDao.findShoppingCartByCustomerEmail("alice@email.com")).thenReturn(cart);

        Order order = assertDoesNotThrow(() -> service.checkout("alice@email.com"));

        assertEquals(customer, order.getCustomer());
        assertEquals(1, order.getOrderItems().size());

        OrderItem oi = order.getOrderItems().iterator().next();
        assertEquals(2, oi.getQuantity());
        assertEquals(100.0f, oi.getListPrice());
        assertEquals(90.0f, oi.getUnitPrice()); // 100 - 10%
        assertEquals(Integer.valueOf(10), oi.getDiscountPercentage());
        assertEquals(20.0f, oi.getCommissionPercentage());
        assertEquals("Starry Night", oi.getArtName());
        assertEquals(art, oi.getArtPiece());

        // Stock must be decremented by the purchased quantity.
        assertEquals(Integer.valueOf(3), art.getQuantity()); // 5 - 2

        // Cart must be emptied only after the order is safely created.
        assertEquals(0, cart.getSelectedItems().size());
    }

    @Test
    public void testCheckoutCustomerNotFound() {
        lenient().when(customerDao.findCustomerByEmail("nobody@email.com")).thenReturn(null);

        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class,
                () -> service.checkout("nobody@email.com"));
        assertEquals("No customer found with email: nobody@email.com", e.getMessage());
    }

    @Test
    public void testCheckoutNoCartForCustomer() {
        lenient().when(customerDao.findCustomerByEmail("alice@email.com")).thenReturn(customer);
        lenient().when(shoppingCartDao.findShoppingCartByCustomerEmail("alice@email.com")).thenReturn(null);

        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class,
                () -> service.checkout("alice@email.com"));
        assertEquals("No cart found for this customer!", e.getMessage());
    }

    @Test
    public void testCheckoutEmptyCart() {
        cart.setSelectedItems(new HashSet<SelectedItem>());

        lenient().when(customerDao.findCustomerByEmail("alice@email.com")).thenReturn(customer);
        lenient().when(shoppingCartDao.findShoppingCartByCustomerEmail("alice@email.com")).thenReturn(cart);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.checkout("alice@email.com"));
        assertEquals("Cannot checkout an empty cart!", e.getMessage());
    }

    @Test
    public void testCheckoutInactiveArtPieceFails() {
        art.setActive(false);

        lenient().when(customerDao.findCustomerByEmail("alice@email.com")).thenReturn(customer);
        lenient().when(shoppingCartDao.findShoppingCartByCustomerEmail("alice@email.com")).thenReturn(cart);

        assertThrows(IllegalArgumentException.class, () -> service.checkout("alice@email.com"));

        // Nothing should have been mutated: fail fast, before any writes.
        assertEquals(Integer.valueOf(5), art.getQuantity());
        assertEquals(1, cart.getSelectedItems().size());
    }

    @Test
    public void testCheckoutInsufficientStockFails() {
        selectedItem.setItemQuantity(10); // more than the 5 in stock

        lenient().when(customerDao.findCustomerByEmail("alice@email.com")).thenReturn(customer);
        lenient().when(shoppingCartDao.findShoppingCartByCustomerEmail("alice@email.com")).thenReturn(cart);

        assertThrows(IllegalArgumentException.class, () -> service.checkout("alice@email.com"));

        // Fail fast, before any stock is decremented or the cart is touched.
        assertEquals(Integer.valueOf(5), art.getQuantity());
        assertEquals(1, cart.getSelectedItems().size());
    }

    /**
     * Simulates a concurrent checkout claiming the same orderNumber first.
     * The first createOrder() attempt fails with a PK collision; checkout()
     * must retry and succeed on the second attempt rather than propagating
     * the exception.
     */
    @Test
    public void testCheckoutRetriesOnOrderNumberCollision() {
        lenient().when(customerDao.findCustomerByEmail("alice@email.com")).thenReturn(customer);
        lenient().when(shoppingCartDao.findShoppingCartByCustomerEmail("alice@email.com")).thenReturn(cart);

        when(orderDao.save(any(Order.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate orderNumber"))
                .thenAnswer((InvocationOnMock invocation) -> invocation.getArgument(0));

        Order order = assertDoesNotThrow(() -> service.checkout("alice@email.com"));

        assertEquals(customer, order.getCustomer());
        verify(orderDao, times(2)).save(any(Order.class));
    }

    /**
     * If every retry attempt collides, checkout() must give up with a clear
     * IllegalStateException after exactly 5 attempts, rather than retrying
     * fewer/more times, looping indefinitely, or leaking the underlying
     * DataIntegrityViolationException to the caller.
     *
     * NOTE: this test cannot verify that the stock decrement is rolled
     * back, since there is no real Spring-managed transaction in a
     * Mockito-only unit test. That guarantee requires a separate
     * integration test against the real database.
     */
    @Test
    public void testCheckoutGivesUpAfterRepeatedCollisions() {
        lenient().when(customerDao.findCustomerByEmail("alice@email.com")).thenReturn(customer);
        lenient().when(shoppingCartDao.findShoppingCartByCustomerEmail("alice@email.com")).thenReturn(cart);

        when(orderDao.save(any(Order.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate orderNumber"));

        assertThrows(IllegalStateException.class, () -> service.checkout("alice@email.com"));

        verify(orderDao, times(5)).save(any(Order.class));
    }
}