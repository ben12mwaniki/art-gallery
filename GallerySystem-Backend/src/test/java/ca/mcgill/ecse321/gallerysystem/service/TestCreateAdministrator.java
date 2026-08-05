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

import ca.mcgill.ecse321.gallerysystem.dao.AdministratorRepository;
import ca.mcgill.ecse321.gallerysystem.dao.UserRepository;
import ca.mcgill.ecse321.gallerysystem.model.Administrator;
import ca.mcgill.ecse321.gallerysystem.model.User;

/**
 * This test class did not previously exist - createAdministrator had no dedicated
 * test coverage at all in the original project, despite createArtist and
 * createCustomer both having their own test files. Mirrors the pattern used for
 * those two, since createAdministrator shares the same validation logic.
 */
@ExtendWith(MockitoExtension.class)
public class TestCreateAdministrator {
	@Mock
	private AdministratorRepository administratorDao;
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
				Administrator existing = new Administrator();
				existing.setEmail(EXISTING_EMAIL);
				return existing;
			}
			return null;
		});
		// Whenever anything is saved, just return the parameter object
		Answer<?> returnParameterAsAnswer = (InvocationOnMock invocation) -> {
			return invocation.getArgument(0);
		};
		lenient().when(administratorDao.save(any(Administrator.class))).thenAnswer(returnParameterAsAnswer);
		lenient().when(userDao.save(any(User.class))).thenAnswer(returnParameterAsAnswer);
	}

	/**
	 * Test to create an administrator with valid, distinct information.
	 */
	@Test
	public void testCreateAdministrator() {
		assertEquals(0, service.getAllAdministrators().size());

		String userName = "Admina";
		String email = "admina@mail.com";
		String password = "pass";
		Administrator administrator = null;
		try {
			administrator = service.createAdministrator(userName, email, password);
		} catch (IllegalArgumentException e) {
			fail();
		}
		checkResultCreateAdministrator(administrator, userName, email, password);
	}

	/**
	 * Checks the result of testCreateAdministrator by checking that the fields of the
	 * created administrator correspond to the given input.
	 *
	 * @param administrator The administrator object which the test would be performed on.
	 * @param userName The username to be used to check the validity of the administrator.
	 * @param email The email address to be used to check the validity of the administrator.
	 * @param password The password to be used to check the validity of the administrator.
	 */
	private void checkResultCreateAdministrator(Administrator administrator, String userName, String email, String password) {
		assertNotNull(administrator);
		assertEquals(userName, administrator.getUserName());
		assertEquals(email, administrator.getEmail());
		assertEquals(password, administrator.getPassword());
	}

	/**
	 * Test to create an administrator with a null username. Username is validated
	 * first, so that is the specific error we expect.
	 */
	@Test
	public void testCreateAdministratorNullUsername() {
		String error = null;
		Administrator administrator = null;
		try {
			administrator = service.createAdministrator(null, "admina@mail.com", "pass");
		} catch (IllegalArgumentException e) {
			error = e.getMessage();
		}
		assertNull(administrator);
		assertEquals("Username cannot be empty.", error);
	}

	/**
	 * Test to create an administrator with a blank (whitespace-only) username.
	 */
	@Test
	public void testCreateAdministratorBlankUsername() {
		String error = null;
		Administrator administrator = null;
		try {
			administrator = service.createAdministrator("   ", "admina@mail.com", "pass");
		} catch (IllegalArgumentException e) {
			error = e.getMessage();
		}
		assertNull(administrator);
		assertEquals("Username cannot be empty.", error);
	}

	/**
	 * Test to create an administrator with a blank password.
	 */
	@Test
	public void testCreateAdministratorBlankPassword() {
		String error = null;
		Administrator administrator = null;
		try {
			administrator = service.createAdministrator("Admina", "admina@mail.com", "   ");
		} catch (IllegalArgumentException e) {
			error = e.getMessage();
		}
		assertNull(administrator);
		assertEquals("Password cannot be empty.", error);
	}

	/**
	 * Test to create an administrator with a malformed email.
	 */
	@Test
	public void testCreateAdministratorInvalidEmailFormat() {
		String error = null;
		Administrator administrator = null;
		try {
			administrator = service.createAdministrator("Admina", "not-an-email", "pass");
		} catch (IllegalArgumentException e) {
			error = e.getMessage();
		}
		assertNull(administrator);
		assertEquals("Email is not a validly formatted email address: not-an-email", error);
	}

	/**
	 * Test that leading/trailing whitespace on all string fields is trimmed rather than
	 * stored as-is.
	 */
	@Test
	public void testCreateAdministratorTrimsWhitespace() {
		Administrator administrator = null;
		try {
			administrator = service.createAdministrator("  Admina  ", "  admina@mail.com  ", "  pass  ");
		} catch (IllegalArgumentException e) {
			fail();
		}
		checkResultCreateAdministrator(administrator, "Admina", "admina@mail.com", "pass");
	}

	/**
	 * Test to create an administrator with an email that already belongs to an existing
	 * user (email is the primary key shared across all User subtypes).
	 */
	@Test
	public void testCreateAdministratorDuplicateEmail() {
		String error = null;
		Administrator administrator = null;
		try {
			administrator = service.createAdministrator("Someone Else", EXISTING_EMAIL, "pass2");
		} catch (IllegalArgumentException e) {
			error = e.getMessage();
		}
		assertNull(administrator);
		assertEquals("A user with this email already exists: " + EXISTING_EMAIL, error);
	}
}