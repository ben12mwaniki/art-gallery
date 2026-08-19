package ca.mcgill.ecse321.gallerysystem.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
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

import ca.mcgill.ecse321.gallerysystem.dao.CustomerRepository;
import ca.mcgill.ecse321.gallerysystem.dao.SelectedItemRepository;
import ca.mcgill.ecse321.gallerysystem.dao.ShoppingCartRepository;
import ca.mcgill.ecse321.gallerysystem.dao.UserRepository;
import ca.mcgill.ecse321.gallerysystem.model.Customer;
import ca.mcgill.ecse321.gallerysystem.model.SelectedItem;
import ca.mcgill.ecse321.gallerysystem.model.ShoppingCart;
import ca.mcgill.ecse321.gallerysystem.model.User;

@ExtendWith(MockitoExtension.class)
public class TestCreateShoppingCart {
	@Mock
	private CustomerRepository customerDao;
	@Mock
	private ShoppingCartRepository shoppingCartDao;
	@Mock
	private UserRepository userDao;
	@Mock
	private SelectedItemRepository selectedItemDao;

	@InjectMocks
	private GallerySystemService service;

	@BeforeEach
	public void setMockOutput() {

		// Whenever anything is saved, just return the parameter object
		Answer<?> returnParameterAsAnswer = (InvocationOnMock invocation) -> {
			return invocation.getArgument(0);
		};
		lenient().when(customerDao.save(any(Customer.class))).thenAnswer(returnParameterAsAnswer);
		lenient().when(shoppingCartDao.save(any(ShoppingCart.class))).thenAnswer(returnParameterAsAnswer);
		lenient().when(userDao.save(any(User.class))).thenAnswer(returnParameterAsAnswer);
	}

	/**
	 * Test to create a Shopping cart with a valid customer email. Confirms
	 * the cart is created and, importantly, that its selectedItem set is
	 * initialized (not null) - this is a regression test for a bug where
	 * a freshly created cart had a null selectedItem set, causing a
	 * NullPointerException the first time an item was added to it.
	 */
	@Test
	public void testCreateShoppingCart() {
		String customerEmail = "alice@email.com";
		Customer customer = new Customer();
		customer.setEmail(customerEmail);

		lenient().when(customerDao.findCustomerByEmail(customerEmail)).thenReturn(customer);
		ShoppingCart sc = service.createShoppingCart(customerEmail);

		assertNotNull(sc);
		assertEquals(customer, sc.getCustomer());
		assertEquals(0, sc.getItemCount());
		assertEquals(true, sc.isEmpty());

		// Regression check: selectedItem must be a real (empty) Set, not null
		assertNotNull(sc.getSelectedItems());
		assertEquals(0, sc.getSelectedItems().size());
	}

	/**
	 * Test to create an invalid shopping cart with a null customer email.
	 * The service class is expected to not create any ShoppingCart object
	 * and throw an exception instead.
	 */
	@Test
	public void testInvalidCreateShoppingCart() {
		String error = null;
		String customerEmail = null;
		ShoppingCart shoppingCart = null;
		try {
			shoppingCart = service.createShoppingCart(customerEmail);
		} catch (IllegalArgumentException e) {
			error = e.getMessage();
		}
		assertNull(shoppingCart);
		assertEquals("Invalid Inputs!", error);
	}

	/**
	 * Regression test: appending an item to a brand new shopping cart used
	 * to throw a NullPointerException, because createShoppingCart initialized
	 * selectedItem to null instead of an empty Set. This test creates a cart,
	 * then immediately appends an item to it, and asserts no exception is
	 * thrown and the item is actually present afterward.
	 */
	@Test
	public void testAppendItemToFreshShoppingCart() {
		String customerEmail = "alice@email.com";
		Customer customer = new Customer();
		customer.setEmail(customerEmail);

		ShoppingCart sc = new ShoppingCart();
		sc.setCustomer(customer);
		sc.setSelectedItems(new HashSet<SelectedItem>());

		SelectedItem item = new SelectedItem();

		lenient().when(shoppingCartDao.findShoppingCartByCustomerEmail(customerEmail)).thenReturn(sc);
		lenient().when(selectedItemDao.findSelectedItemByItemID(any(Integer.class))).thenReturn(item);

		assertDoesNotThrow(() -> {
			service.appendItemToShoppingCart(1, customerEmail);
		});

		assertEquals(1, sc.getSelectedItems().size());
	}
}