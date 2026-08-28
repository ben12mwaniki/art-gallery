package ca.mcgill.ecse321.gallerysystem.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import ca.mcgill.ecse321.gallerysystem.exception.ResourceNotFoundException;
import ca.mcgill.ecse321.gallerysystem.model.Artist;
import ca.mcgill.ecse321.gallerysystem.model.User;

@ExtendWith(MockitoExtension.class)
public class TestGetArtist {
	@Mock
	private ArtistRepository artistDao;
	@Mock
	private UserRepository userDao;

	@InjectMocks
	private GallerySystemService service;

	private static final String ARTIST_KEY = "TestArtist@test.com";
	private static final String NONEXISTING_KEY = "NotAnArtist@test.com";

	@BeforeEach
	public void setMockOutput() {
		lenient().when(artistDao.findArtistByEmail(anyString())).thenAnswer((InvocationOnMock invocation) -> {
			if (invocation.getArgument(0).equals(ARTIST_KEY)) {
				Artist artist = new Artist();
				artist.setEmail(ARTIST_KEY);
				return artist;
			} else {
				return null;
			}
		});
		// Whenever anything is saved, just return the parameter object
		Answer<?> returnParameterAsAnswer = (InvocationOnMock invocation) -> {
			return invocation.getArgument(0);
		};
		lenient().when(artistDao.save(any(Artist.class))).thenAnswer(returnParameterAsAnswer);
		lenient().when(userDao.save(any(User.class))).thenAnswer(returnParameterAsAnswer);
	}

	/**
	 * Test to get an existing artist with provided email and verify.
	 */
	@Test
	public void testGetExistingArtist() {
		assertEquals(ARTIST_KEY, service.getArtist(ARTIST_KEY).getEmail());
	}

	/**
	 * Test to get an non-existing artist with provided email and verify.
	 */
	@Test
	public void testGetNonExistingArtist() {
		assertThrows(ResourceNotFoundException.class, () -> service.getArtist(NONEXISTING_KEY));
	}

}