package ca.mcgill.ecse321.gallerysystem.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import ca.mcgill.ecse321.gallerysystem.model.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
public class TestRepositoryPersistence {

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private SelectedItemRepository selectedItemRepository;

	@Autowired
	private ArtPieceRepository artPieceRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private ShoppingCartRepository shoppingCartRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ArtistRepository artistRepository;

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private AdministratorRepository administratorRepository;

	@Test
	public void testPersistAndLoadCustomer() {

		Customer customer = new Customer();

		customer.setEmail("customer@example.com");
		customer.setPassword("password");
		customer.setUserName("customer");
		customer.setAddress("123 Main Street");

		customer = customerRepository.save(customer);

		/*
		 * Force Hibernate to write to PostgreSQL and clear the persistence
		 * context. The following query therefore has to retrieve the entity
		 * from the database rather than returning the same cached instance.
		 */
		entityManager.flush();
		entityManager.clear();

		Customer savedCustomer = customerRepository.findCustomerByEmail("customer@example.com");

		assertNotNull(savedCustomer);
		assertEquals("customer@example.com", savedCustomer.getEmail());
		assertEquals("password", savedCustomer.getPassword());
		assertEquals("customer", savedCustomer.getUserName());
		assertEquals("123 Main Street", savedCustomer.getAddress());
	}

	@Test
	public void testPersistAndLoadArtist() {

		Artist artist = new Artist();

		artist.setEmail("artist@example.com");
		artist.setPassword("password");
		artist.setUserName("artist");

		artist = artistRepository.save(artist);

		entityManager.flush();
		entityManager.clear();

		Artist savedArtist = artistRepository.findArtistByEmail("artist@example.com");

		assertNotNull(savedArtist);
		assertEquals("artist@example.com", savedArtist.getEmail());
		assertEquals("password", savedArtist.getPassword());
		assertEquals("artist", savedArtist.getUserName());
	}

	@Test
	public void testPersistAndLoadAdministrator() {

		Administrator administrator = new Administrator();

		administrator.setEmail("admin@example.com");
		administrator.setPassword("password");
		administrator.setUserName("admin");

		administrator = administratorRepository.save(administrator);

		entityManager.flush();
		entityManager.clear();

		Administrator savedAdministrator = administratorRepository.findAdministratorByEmail("admin@example.com");

		assertNotNull(savedAdministrator);
		assertEquals("admin@example.com", savedAdministrator.getEmail());
		assertEquals("password", savedAdministrator.getPassword());
		assertEquals("admin", savedAdministrator.getUserName());
	}

	@Test
	public void testPersistAndLoadArtPiece() {

		Artist artist = new Artist();

		artist.setEmail("artist@example.com");
		artist.setPassword("password");
		artist.setUserName("artist");

		artist = artistRepository.save(artist);

		ArtPiece art = new ArtPiece();
		art.setArtName("Mona Lisa");
		art.setDescription("A test piece of artwork");
		art.setDiscountPercentage(Integer.valueOf(10));
		art.setPrice(100.0f);
		art.setQuantity(Integer.valueOf(5));
		art.setCommissionPercentage(20.0f);
		art.setArtist(artist);

		/*
		 * artID is @GeneratedValue, so capture the returned entity to obtain
		 * the database-generated ID.
		 */
		art = artPieceRepository.save(art);

		entityManager.flush();
		Integer artID = art.getArtID();

		entityManager.clear();

		ArtPiece savedArt = artPieceRepository.findArtPieceByArtID(artID);

		assertNotNull(savedArt);
		assertEquals(artID, savedArt.getArtID());
		assertEquals("Mona Lisa", savedArt.getArtName());
		assertEquals("A test piece of artwork", savedArt.getDescription());
		assertEquals(Integer.valueOf(10), savedArt.getDiscountPercentage());
		assertEquals(100.0f, savedArt.getPrice());
		assertEquals(Integer.valueOf(5), savedArt.getQuantity());
		assertEquals(20.0f, savedArt.getCommissionPercentage());

		assertNotNull(savedArt.getArtist());
		assertEquals(
				"artist@example.com",
				savedArt.getArtist().getEmail());
	}

	@Test
	public void testPersistAndLoadSelectedItem() {

		Artist artist = new Artist();

		artist.setEmail("artist@example.com");
		artist.setPassword("password");
		artist.setUserName("artist");

		artist = artistRepository.save(artist);

		ArtPiece art = new ArtPiece();
		art.setArtName("Test Art");
		art.setDescription("Test description");
		art.setDiscountPercentage(Integer.valueOf(0));
		art.setPrice(50.0f);
		art.setQuantity(Integer.valueOf(10));
		art.setCommissionPercentage(20.0f);
		art.setArtist(artist);

		art = artPieceRepository.save(art);

		Customer customer = new Customer();
		customer.setEmail("customer@example.com");
		customer.setPassword("password");
		customer.setUserName("customer");
		customer.setAddress("123 Main Street");

		customer = customerRepository.save(customer);

		ShoppingCart cart = new ShoppingCart();
		cart.setCustomer(customer);
		cart.setIsEmpty(false);
		cart.setItemNumber(Integer.valueOf(1));
		cart.setSelectedItem(new HashSet<SelectedItem>());

		cart = shoppingCartRepository.save(cart);

		SelectedItem item = new SelectedItem();
		item.setArtPiece(art);
		item.setItemQuantity(Integer.valueOf(2));
		item.setShoppingCart(cart);

		item = selectedItemRepository.save(item);

		// Keep both sides of the relationship synchronized
		cart.getSelectedItem().add(item);
		shoppingCartRepository.save(cart);

		entityManager.flush();
		Integer itemID = item.getItemID();

		entityManager.clear();

		SelectedItem savedItem = selectedItemRepository.findSelectedItemByItemID(itemID);

		assertNotNull(savedItem);
		assertEquals(itemID, savedItem.getItemID());
		assertEquals(Integer.valueOf(2), savedItem.getItemQuantity());

		assertNotNull(savedItem.getArtPiece());
		assertEquals(
				art.getArtID(),
				savedItem.getArtPiece().getArtID());

		assertNotNull(savedItem.getShoppingCart());
		assertEquals(
				cart.getCartID(),
				savedItem.getShoppingCart().getCartID());
	}

	@Test
	public void testPersistAndLoadShoppingCart() {

		Customer customer = new Customer();

		customer.setEmail("customer@example.com");
		customer.setPassword("password");
		customer.setUserName("customer");
		customer.setAddress("123 Main Street");

		customer = customerRepository.save(customer);

		Artist artist = new Artist();

		artist.setEmail("artist@example.com");
		artist.setPassword("password");
		artist.setUserName("artist");

		artist = artistRepository.save(artist);

		ArtPiece art = new ArtPiece();
		art.setArtName("Test Art");
		art.setDescription("Test description");
		art.setDiscountPercentage(Integer.valueOf(0));
		art.setPrice(50.0f);
		art.setQuantity(Integer.valueOf(10));
		art.setCommissionPercentage(20.0f);
		art.setArtist(artist);

		art = artPieceRepository.save(art);

		ShoppingCart cart = new ShoppingCart();
		cart.setCustomer(customer);
		cart.setIsEmpty(false);

		/*
		 * cartID is @GeneratedValue, so capture the returned entity.
		 */
		cart = shoppingCartRepository.save(cart);

		SelectedItem item = new SelectedItem();
		item.setArtPiece(art);
		item.setItemQuantity(Integer.valueOf(2));
		item.setShoppingCart(cart);

		item = selectedItemRepository.save(item);

		Set<SelectedItem> items = new HashSet<>();
		items.add(item);

		cart.setSelectedItem(items);
		cart.setItemNumber(Integer.valueOf(items.size()));

		entityManager.flush();
		Integer cartID = cart.getCartID();

		entityManager.clear();

		ShoppingCart savedCart = shoppingCartRepository.findShoppingCartByCartID(cartID);

		assertNotNull(savedCart);
		assertEquals(cartID, savedCart.getCartID());
		assertEquals(false, savedCart.isIsEmpty());
		assertEquals(Integer.valueOf(1), savedCart.getItemNumber());

		assertNotNull(savedCart.getCustomer());
		assertEquals(
				"customer@example.com",
				savedCart.getCustomer().getEmail());

		assertNotNull(savedCart.getSelectedItem());
		assertEquals(1, savedCart.getSelectedItem().size());

		SelectedItem savedItem = savedCart.getSelectedItem().iterator().next();

		assertEquals(item.getItemID(), savedItem.getItemID());
		assertEquals(Integer.valueOf(2), savedItem.getItemQuantity());
		assertEquals(
				art.getArtID(),
				savedItem.getArtPiece().getArtID());
	}

	@Test
	public void testPersistAndLoadOrder() {

		Customer customer = new Customer();

		customer.setEmail("customer@example.com");
		customer.setPassword("password");
		customer.setUserName("customer");
		customer.setAddress("123 Main Street");

		customer = customerRepository.save(customer);

		ShoppingCart cart = new ShoppingCart();
		cart.setCustomer(customer);
		cart.setIsEmpty(true);
		cart.setItemNumber(Integer.valueOf(0));
		cart.setSelectedItem(new HashSet<SelectedItem>());

		cart = shoppingCartRepository.save(cart);

		Order order = new Order();

		/*
		 * orderNumber is the @Id and is manually assigned. It does not use
		 * 
		 * @GeneratedValue.
		 */
		order.setOrderNumber(Integer.valueOf(1001));
		order.setOrderDate(Date.valueOf("2020-11-28"));
		order.setCustomer(customer);
		order.setShoppingCart(cart);

		order = orderRepository.save(order);

		entityManager.flush();
		entityManager.clear();

		Order savedOrder = orderRepository.findOrderByOrderNumber(Integer.valueOf(1001));

		assertNotNull(savedOrder);
		assertEquals(Integer.valueOf(1001), savedOrder.getOrderNumber());
		assertEquals(
				Date.valueOf("2020-11-28"),
				savedOrder.getOrderDate());

		assertNotNull(savedOrder.getCustomer());
		assertEquals(
				"customer@example.com",
				savedOrder.getCustomer().getEmail());

		assertNotNull(savedOrder.getShoppingCart());
		assertEquals(
				cart.getCartID(),
				savedOrder.getShoppingCart().getCartID());
	}
}