package ca.mcgill.ecse321.gallerysystem.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ca.mcgill.ecse321.gallerysystem.dao.AdministratorRepository;
import ca.mcgill.ecse321.gallerysystem.dao.ArtPieceRepository;
import ca.mcgill.ecse321.gallerysystem.dao.ArtistRepository;
import ca.mcgill.ecse321.gallerysystem.dao.CustomerRepository;
import ca.mcgill.ecse321.gallerysystem.dao.OrderRepository;
import ca.mcgill.ecse321.gallerysystem.dao.SelectedItemRepository;
import ca.mcgill.ecse321.gallerysystem.dao.ShoppingCartRepository;
import ca.mcgill.ecse321.gallerysystem.dao.UserRepository;
import ca.mcgill.ecse321.gallerysystem.model.ArtPiece;
import ca.mcgill.ecse321.gallerysystem.model.SelectedItem;
import ca.mcgill.ecse321.gallerysystem.model.ShoppingCart;

@ExtendWith(MockitoExtension.class)
public class TestCreateSelectedItem {

	@Mock
	private ArtistRepository artistDao;

	@Mock
	private AdministratorRepository administratorDao;

	@Mock
	private ArtPieceRepository artPieceDao;

	@Mock
	private CustomerRepository customerDao;

	@Mock
	private OrderRepository orderDao;

	@Mock
	private SelectedItemRepository selectedItemDao;

	@Mock
	private ShoppingCartRepository shoppingCartDao;

	@Mock
	private UserRepository userDao;

	@InjectMocks
	private GallerySystemService service;

	@Test
	public void testCreateSelectedItem() {

		Integer artID = 1;
		Integer quantity = 2;
		String customerEmail = "customer@example.com";

		ArtPiece artPiece = new ArtPiece();
		artPiece.setArtID(artID);
		artPiece.setQuantity(10);

		ShoppingCart cart = new ShoppingCart();
		cart.setSelectedItems(new HashSet<SelectedItem>());

		when(artPieceDao.findArtPieceByArtID(artID))
				.thenReturn(artPiece);

		when(shoppingCartDao.findShoppingCartByCustomerEmail(customerEmail))
				.thenReturn(cart);

		SelectedItem selectedItem = service.createSelectedItem(
				artID,
				quantity,
				customerEmail);

		assertNotNull(selectedItem);
		assertEquals(quantity, selectedItem.getItemQuantity());
		assertEquals(artPiece, selectedItem.getArtPiece());
		assertEquals(cart, selectedItem.getShoppingCart());

		assertTrue(cart.getSelectedItems().contains(selectedItem));
		assertEquals(1, cart.getItemCount());
		assertFalse(cart.isEmpty());

		verify(shoppingCartDao).save(cart);
	}

	@Test
	public void testCreateSelectedItemNullArtID() {

		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> service.createSelectedItem(
						null,
						1,
						"customer@example.com"));

		assertEquals(
				"Art piece ID cannot be null.",
				exception.getMessage());
	}

	@Test
	public void testCreateSelectedItemInvalidQuantity() {

		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> service.createSelectedItem(
						1,
						0,
						"customer@example.com"));

		assertEquals(
				"Quantity must be a positive number.",
				exception.getMessage());
	}

	@Test
	public void testCreateSelectedItemNullCustomerEmail() {

		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> service.createSelectedItem(
						1,
						1,
						null));

		assertEquals(
				"Customer email cannot be null or blank.",
				exception.getMessage());
	}

	@Test
	public void testCreateSelectedItemBlankCustomerEmail() {

		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> service.createSelectedItem(
						1,
						1,
						"   "));

		assertEquals(
				"Customer email cannot be null or blank.",
				exception.getMessage());
	}

	@Test
	public void testCreateSelectedItemArtPieceNotFound() {

		Integer artID = 99;

		when(artPieceDao.findArtPieceByArtID(artID))
				.thenReturn(null);

		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> service.createSelectedItem(
						artID,
						1,
						"customer@example.com"));

		assertEquals(
				"No art piece found with ID: 99",
				exception.getMessage());
	}

	@Test
	public void testCreateSelectedItemQuantityExceedsStock() {

		Integer artID = 1;

		ArtPiece artPiece = new ArtPiece();
		artPiece.setArtID(artID);
		artPiece.setQuantity(3);

		when(artPieceDao.findArtPieceByArtID(artID))
				.thenReturn(artPiece);

		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> service.createSelectedItem(
						artID,
						4,
						"customer@example.com"));

		assertEquals(
				"Requested quantity (4) exceeds available stock (3) "
						+ "for art piece ID: 1",
				exception.getMessage());
	}

	@Test
	public void testCreateSelectedItemCartNotFound() {

		String customerEmail = "customer@example.com";

		ArtPiece artPiece = new ArtPiece();
		artPiece.setArtID(1);
		artPiece.setQuantity(10);

		when(artPieceDao.findArtPieceByArtID(1))
				.thenReturn(artPiece);

		when(shoppingCartDao.findShoppingCartByCustomerEmail(customerEmail))
				.thenReturn(null);

		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> service.createSelectedItem(
						1,
						1,
						customerEmail));

		assertEquals(
				"No shopping cart found for customer: "
						+ customerEmail,
				exception.getMessage());
	}
}