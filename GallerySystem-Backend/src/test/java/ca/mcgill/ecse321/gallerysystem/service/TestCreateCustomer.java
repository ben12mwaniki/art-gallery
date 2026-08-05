package ca.mcgill.ecse321.gallerysystem.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import ca.mcgill.ecse321.gallerysystem.dao.CustomerRepository;
import ca.mcgill.ecse321.gallerysystem.dao.UserRepository;
import ca.mcgill.ecse321.gallerysystem.model.Customer;
import ca.mcgill.ecse321.gallerysystem.model.User;

@ExtendWith(MockitoExtension.class)
public class TestCreateCustomer {
	@Mock
	private CustomerRepository customerDao;
	@Mock
	private UserRepository userDao;
	
	@InjectMocks
	private GallerySystemService service;

	// Email that the mocked repositories will treat as "already registered"
	private static final String EXISTING_EMAIL = "existing@mail.com";
	
	@BeforeEach
	public void setMockOutput() {
		// Simulates the DB already containing a user with EXISTING_EMAIL.
		// Any other email is treated as "not found" -> returns null.
		lenient().when(userDao.findUserByEmail(anyString())).thenAnswer((InvocationOnMock invocation) -> {
			String requestedEmail = invocation.getArgument(0);
			if (requestedEmail != null && requestedEmail.equals(EXISTING_EMAIL)) {
				Customer existing = new Customer();
				existing.setEmail(EXISTING_EMAIL);
				return existing;
			}
			return null;
		});
		// Whenever anything is saved, just return the parameter object
		Answer<?> returnParameterAsAnswer = (InvocationOnMock invocation) -> {
			return invocation.getArgument(0);
		};
		lenient().when(customerDao.save(any(Customer.class))).thenAnswer(returnParameterAsAnswer);
		lenient().when(userDao.save(any(User.class))).thenAnswer(returnParameterAsAnswer);
	}
	
	/**
	 * Test to create a customer with valid, distinct information. The created customer 
	 * object will be checked with checkResultCreateCustomer method.
	 */
    @Test
	public void testCreateCustomer() {
		assertEquals(0, service.getAllCustomers().size());
		
		String userName = "David";
		String email = "david@mail.com";
		String password = "pass";
		String address = "North Carol Drive #86";
		Customer customer = null;
		try {
			customer = service.createCustomer(userName, email, address, password);
		}catch (IllegalArgumentException e) {
			fail();
		}
		checkResultCreateCustomer(customer, userName, email, password, address);
	}
	
	
	/** 
	 * Checks the customer being created and sees if it was successfully created. 
	 * @param customer The customer object to be checked. 
	 * @param userName Customer's username to be checked with the created customer. 
	 * @param email Customer's email to be checked with the created customer. 
	 * @param password Customer's password to be checked with the created customer. 
	 * @param address Customer's address to be checked with the created customer. 
	 */
	private void checkResultCreateCustomer(Customer customer, String userName, String email, String password, String address) {
		assertNotNull(customer);
		assertEquals(userName, customer.getUserName());
		assertEquals(email, customer.getEmail());
		assertEquals(password, customer.getPassword());
		assertEquals(address, customer.getAddress());
	}
	
	/**
	 * Test to create a customer with a null username. Username is validated first, so
	 * that is the specific error we expect.
	 */
	@Test
	public void testCreateCustomerNullUsername() {
		String error = null;
		String userName = null;
		String email = "david@mail.com";
		String password = "pass";
		String address = "North Carol Drive #86";
		Customer customer = null;
		try {
			customer = service.createCustomer(userName, email, address, password);
		} catch (IllegalArgumentException e) {
			error = e.getMessage();
		}
		assertNull(customer);
		assertEquals("Username cannot be empty.", error);	
	}

	/**
	 * Test to create a customer with a blank (whitespace-only) address. Regression test
	 * for the old null-only check that let whitespace-only strings through.
	 */
	@Test
	public void testCreateCustomerBlankAddress() {
		String error = null;
		Customer customer = null;
		try {
			customer = service.createCustomer("David", "david@mail.com", "   ", "pass");
		} catch (IllegalArgumentException e) {
			error = e.getMessage();
		}
		assertNull(customer);
		assertEquals("Address cannot be empty.", error);
	}

	/**
	 * Test to create a customer with a blank password.
	 */
	@Test
	public void testCreateCustomerBlankPassword() {
		String error = null;
		Customer customer = null;
		try {
			customer = service.createCustomer("David", "david@mail.com", "North Carol Drive #86", "   ");
		} catch (IllegalArgumentException e) {
			error = e.getMessage();
		}
		assertNull(customer);
		assertEquals("Password cannot be empty.", error);
	}

	/**
	 * Test to create a customer with a malformed email.
	 */
	@Test
	public void testCreateCustomerInvalidEmailFormat() {
		String error = null;
		Customer customer = null;
		try {
			customer = service.createCustomer("David", "not-an-email", "North Carol Drive #86", "pass");
		} catch (IllegalArgumentException e) {
			error = e.getMessage();
		}
		assertNull(customer);
		assertEquals("Email is not a validly formatted email address: not-an-email", error);
	}

	/**
	 * Test that leading/trailing whitespace on all string fields is trimmed rather than
	 * stored as-is.
	 */
	@Test
	public void testCreateCustomerTrimsWhitespace() {
		Customer customer = null;
		try {
			customer = service.createCustomer("  David  ", "  david@mail.com  ", "  North Carol Drive #86  ", "  pass  ");
		} catch (IllegalArgumentException e) {
			fail();
		}
		checkResultCreateCustomer(customer, "David", "david@mail.com", "pass", "North Carol Drive #86");
	}

	/**
	 * Test to create a customer with an email that already belongs to an existing user.
	 * Regression test for the bug where a duplicate email would silently overwrite the
	 * existing user's data instead of being rejected (email is the primary key shared
	 * across all User subtypes).
	 */
	@Test
	public void testCreateCustomerDuplicateEmail() {
		String error = null;
		Customer customer = null;
		try {
			customer = service.createCustomer("Someone Else", EXISTING_EMAIL, "Another Address", "pass2");
		} catch (IllegalArgumentException e) {
			error = e.getMessage();
		}
		assertNull(customer);
		assertEquals("A user with this email already exists: " + EXISTING_EMAIL, error);
	}
}