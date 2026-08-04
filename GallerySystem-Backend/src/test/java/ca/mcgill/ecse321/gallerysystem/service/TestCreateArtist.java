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
import ca.mcgill.ecse321.gallerysystem.dao.ArtistRepository;
import ca.mcgill.ecse321.gallerysystem.dao.UserRepository;
import ca.mcgill.ecse321.gallerysystem.model.Artist;
import ca.mcgill.ecse321.gallerysystem.model.User;

@ExtendWith(MockitoExtension.class)
public class TestCreateArtist {
    @Mock
	private ArtistRepository artistDao;
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
				Artist existing = new Artist();
				existing.setEmail(EXISTING_EMAIL);
				return existing;
			}
			return null;
		});
		// Whenever anything is saved, just return the parameter object
		Answer<?> returnParameterAsAnswer = (InvocationOnMock invocation) -> {
			return invocation.getArgument(0);
		};
		lenient().when(artistDao.save(any(Artist.class))).thenAnswer(returnParameterAsAnswer);
		lenient().when(userDao.save(any(User.class))).thenAnswer(returnParameterAsAnswer);
	}
	
	/**
	 * Test to create a valid artist with valid, distinct username, email, and password. 
	 */
    @Test
	public void testCreateArtist() {
		assertEquals(0, service.getAllArtists().size());
		
		String userName = "John";
		String email = "john@mail.com";
		String password = "pass";
		Artist artist = null;
		try {
			artist = service.createArtist(userName, email, password);
		}catch (IllegalArgumentException e) {
			fail();
		}
		checkResultCreateArtist(artist, userName, email, password);
	}
	
	
	/** 
	 * Checks the result of testCreateArtist by checking that the fields of the created artist 
	 * correspond to the given input.
	 * 
	 * @param artist The artist object which the test would be performed on. 
	 * @param userName	The username to be used to check the validity of the artist.
	 * @param email	The email address to be used to check the validity of the artist.
	 * @param password	The password to be used to check the validity of the artist.
	 */
	private void checkResultCreateArtist(Artist artist, String userName, String email, String password) {
		assertNotNull(artist);
		assertEquals(userName, artist.getUserName());
		assertEquals(email, artist.getEmail());
		assertEquals(password, artist.getPassword());
	}
	
	/**
	 * Test to create an artist with all fields set to null. The service is expected to 
	 * reject this and throw a specific error identifying the missing field (username is
	 * validated first, so that is the error we expect here).
	 */
	@Test
	public void testCreateArtistNull() {
		assertEquals(0, service.getAllArtists().size());
		String error = null;
		String userName = null;
		String email = null;
		String password = null;
		Artist artist = null;
		try {
			artist = service.createArtist(userName, email, password);
		} catch (IllegalArgumentException e) {
			error = e.getMessage();
		}
		assertNull(artist);
		assertEquals("Username cannot be empty.", error);
	}

	/**
	 * Test to create an artist with a blank (whitespace-only) username. This used to pass
	 * the old null-only check; now it should be rejected since blank values are trimmed
	 * and treated as empty.
	 */
	@Test
	public void testCreateArtistBlankUsername() {
		String error = null;
		Artist artist = null;
		try {
			artist = service.createArtist("   ", "john@mail.com", "pass");
		} catch (IllegalArgumentException e) {
			error = e.getMessage();
		}
		assertNull(artist);
		assertEquals("Username cannot be empty.", error);
	}

	/**
	 * Test to create an artist with a malformed email (no domain). Should be rejected by
	 * the new email format validation.
	 */
	@Test
	public void testCreateArtistInvalidEmailFormat() {
		String error = null;
		Artist artist = null;
		try {
			artist = service.createArtist("John", "not-an-email", "pass");
		} catch (IllegalArgumentException e) {
			error = e.getMessage();
		}
		assertNull(artist);
		assertEquals("Email is not a validly formatted email address: not-an-email", error);
	}

	/**
	 * Test that leading/trailing whitespace on otherwise-valid fields is trimmed rather
	 * than stored as-is (regression test for the "\t" bug found via manual testing).
	 */
	@Test
	public void testCreateArtistTrimsWhitespace() {
		Artist artist = null;
		try {
			artist = service.createArtist("  John  ", "  john@mail.com  ", "  pass  ");
		} catch (IllegalArgumentException e) {
			fail();
		}
		checkResultCreateArtist(artist, "John", "john@mail.com", "pass");
	}
	
	/**
	 * Test to create two artists with the same email. Since email is the primary key for
	 * all User subtypes, this must now be rejected - this is a regression test for a bug
	 * where a second createArtist call with a duplicate email would silently overwrite
	 * the first artist's data instead of being rejected.
	 */
	@Test
	public void testCreate2ArtistSameEmail() {
		String userName2 = "Adam";
		String password2 = "pass2";
		Artist artist2 = null;
		String error = null;
		try {
			artist2 = service.createArtist(userName2, EXISTING_EMAIL, password2);
		} catch (IllegalArgumentException e) {
			error = e.getMessage();
		}
		assertNull(artist2);
		assertEquals("A user with this email already exists: " + EXISTING_EMAIL, error);
	}
}