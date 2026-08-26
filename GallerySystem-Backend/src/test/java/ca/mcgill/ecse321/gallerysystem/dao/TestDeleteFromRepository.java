package ca.mcgill.ecse321.gallerysystem.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.sql.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import ca.mcgill.ecse321.gallerysystem.model.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = "spring.profiles.active=test")
public class TestDeleteFromRepository {
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

	@Autowired
	private OrderItemRepository orderItemRepository;

	@AfterEach
	public void clearDatabase() {
		/*
		 * Dependency chain:
		 *
		 * OrderItem -> Order, ArtPiece
		 * Order -> Customer
		 * SelectedItem -> ShoppingCart, ArtPiece
		 * ShoppingCart -> Customer
		 * ArtPiece -> Artist
		 * Artist/Customer/Administrator -> User
		 */
		orderItemRepository.deleteAll();
		orderRepository.deleteAll();
		selectedItemRepository.deleteAll();
		shoppingCartRepository.deleteAll();
		artPieceRepository.deleteAll();
		customerRepository.deleteAll();
		artistRepository.deleteAll();
		administratorRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	public void testDeleteCustomer() {

		Customer customer = new Customer();

		customer.setEmail("customer@example.com");
		customer.setPassword("password");
		customer.setUserName("name");

		customerRepository.save(customer);
		customerRepository.deleteById(customer.getEmail());

		assertEquals(0, customerRepository.count());
	}

	@Test
	public void testDeleteArtist() {

		Artist artist = new Artist();

		artist.setEmail("artist@example.com");
		artist.setPassword("password");
		artist.setUserName("name");

		artistRepository.save(artist);
		artistRepository.deleteById(artist.getEmail());

		assertEquals(0, artistRepository.count());
	}

	@Test
	@Transactional
	public void testDeleteArtPiece() {

		Artist artist = new Artist();

		artist.setEmail("artist@example.com");
		artist.setPassword("password");
		artist.setUserName("name");

		artist = artistRepository.save(artist);

		ArtPiece art = new ArtPiece();
		art.setDescription("Test art piece");
		art.setDiscountPercentage(70);
		art.setPrice(1.1f);
		art.setQuantity(1);
		art.setArtName("artName");
		art.setArtist(artist);

		/*
		 * artID is @GeneratedValue. Do not assign it manually.
		 *
		 * Capture save()'s return value because the returned entity contains
		 * the database-generated ID used by deleteById().
		 */
		art = artPieceRepository.save(art);

		artPieceRepository.deleteById(art.getArtID());

		entityManager.flush();

		assertEquals(0, artPieceRepository.count());
	}

	@Test
	@Transactional
	public void testDeleteShoppingCart() {

		Artist artist = new Artist();
		artist.setEmail("artist@example.com");
		artist.setPassword("password");
		artist.setUserName("name");
		artist = artistRepository.save(artist);

		ArtPiece art = new ArtPiece();
		art.setArtName("artName");
		art.setDescription("Test art piece");
		art.setDiscountPercentage(70);
		art.setPrice(1.1f);
		art.setQuantity(1);
		art.setArtist(artist);
		art = artPieceRepository.save(art);

		Customer customer = new Customer();
		customer.setEmail("customer@example.com");
		customer.setPassword("password");
		customer.setUserName("name");
		customer = customerRepository.save(customer);

		/*
		 * Create and save the cart before the SelectedItem because
		 * SelectedItem.shoppingCart is a required relationship.
		 */
		ShoppingCart cart = new ShoppingCart();
		cart.setCustomer(customer);
		cart.setSelectedItems(new HashSet<SelectedItem>());
		cart = shoppingCartRepository.save(cart);

		SelectedItem item = new SelectedItem();
		item.setArtPiece(art);
		item.setItemQuantity(20);
		item.setShoppingCart(cart);
		item = selectedItemRepository.save(item);

		/*
		 * Keep both sides of the ShoppingCart-SelectedItem relationship
		 * synchronized.
		 */
		cart.getSelectedItems().add(item);
		cart = shoppingCartRepository.save(cart);

		entityManager.flush();
		assertEquals(1, shoppingCartRepository.count());

		/*
		 * cascade = CascadeType.ALL + orphanRemoval = true on
		 * ShoppingCart.selectedItems means deleting the cart must also
		 * delete its SelectedItem(s).
		 */
		shoppingCartRepository.deleteById(cart.getCartID());
		entityManager.flush();
		entityManager.clear();

		assertEquals(0, shoppingCartRepository.count());
		assertEquals(0, selectedItemRepository.count());
	}

	@Test
	@Transactional
	/*
	 * IMPORTANT:
	 *
	 * Order.orderNumber is a manually assigned primary key (@Id without
	 * 
	 * @GeneratedValue). Spring Data JPA therefore uses merge() rather than
	 * persist() when saving a new Order.
	 *
	 * Keeping this test transactional ensures that save(), delete(), and
	 * count() execute within the same persistence context. This avoids a
	 * Hibernate 5.x entity-lifecycle issue encountered when the Order was
	 * saved and deleted across separate transactions.
	 *
	 * Spring automatically rolls back the transaction after the test.
	 */
	public void testDeleteOrder() {

		Customer customer = new Customer();

		customer.setEmail("customer@example.com");
		customer.setPassword("password");
		customer.setUserName("name");

		/*
		 * Capture the returned entity. This ensures subsequent relationships
		 * use the entity instance managed by the current persistence context.
		 */
		customer = customerRepository.save(customer);

		ShoppingCart cart = new ShoppingCart();

		/*
		 * cartID is @GeneratedValue and must not be assigned manually.
		 */
		cart.setCustomer(customer);

		cart = shoppingCartRepository.save(cart);

		Order order = new Order();

		/*
		 * Use Date.valueOf() rather than java.sql.Date's deprecated
		 * (year, month, day) constructor.
		 */
		Date date = Date.valueOf("2020-11-28");

		/*
		 * orderNumber is NOT @GeneratedValue. It is a manually assigned
		 * primary key and therefore must be explicitly provided.
		 */
		order.setOrderNumber(10);
		order.setOrderDate(date);
		order.setCustomer(customer);

		/*
		 * Because orderNumber is a manually assigned ID, Spring Data JPA
		 * determines that this is not a generated-ID entity and uses merge().
		 *
		 * save() therefore returns the managed entity instance. Use that
		 * returned instance for delete() rather than the original object.
		 */
		order = orderRepository.save(order);

		orderRepository.delete(order);

		assertEquals(0, orderRepository.count());
	}

	@Test
	@Transactional
	public void testDeleteOrderCascadesOrderItemsButNotArtPiece() {

		Customer customer = new Customer();
		customer.setEmail("customer@example.com");
		customer.setPassword("password");
		customer.setUserName("name");
		customer = customerRepository.save(customer);

		ShoppingCart cart = new ShoppingCart();
		cart.setCustomer(customer);
		cart = shoppingCartRepository.save(cart);

		Artist artist = new Artist();
		artist.setEmail("artist@example.com");
		artist.setPassword("password");
		artist.setUserName("name");
		artist = artistRepository.save(artist);

		ArtPiece art = new ArtPiece();
		art.setArtName("artName");
		art.setDescription("Test art piece");
		art.setDiscountPercentage(0);
		art.setPrice(10.0f);
		art.setQuantity(5);
		art.setCommissionPercentage(10.0f);
		art.setArtist(artist);
		art = artPieceRepository.save(art);

		Integer artID = art.getArtID();

		Order order = new Order();
		order.setOrderNumber(10);
		order.setOrderDate(Date.valueOf("2026-08-19"));
		order.setCustomer(customer);

		OrderItem oi = new OrderItem();
		oi.setArtPiece(art);
		oi.setQuantity(1);
		oi.setListPrice(10.0f);
		oi.setUnitPrice(10.0f);
		oi.setDiscountPercentage(0);
		oi.setCommissionPercentage(10.0f);
		oi.setArtName("artName");
		oi.setDescription("Test art piece");
		oi.setOrder(order);

		Set<OrderItem> orderItems = new HashSet<>();
		orderItems.add(oi);
		order.setOrderItems(orderItems);

		order = orderRepository.save(order);
		entityManager.flush();

		assertEquals(1, orderItemRepository.count());
		assertEquals(1, artPieceRepository.count());

		/*
		 * cascade = CascadeType.ALL on Order.orderItems must include
		 * REMOVE: deleting the Order should delete its OrderItems too,
		 * without needing to delete them explicitly first.
		 */
		orderRepository.delete(order);
		entityManager.flush();
		entityManager.clear();

		assertEquals(0, orderRepository.count());
		assertEquals(0, orderItemRepository.count());

		/*
		 * Deleting order history must never touch the live ArtPiece or its
		 * stock. OrderItem.artPiece is a reference TO ArtPiece, not the
		 * other way around, so cascading from Order/OrderItem must stop
		 * there. This guards against someone later mis-cascading or adding
		 * a cleanup hook that reaches into ArtPiece.
		 */
		assertEquals(1, artPieceRepository.count());
		ArtPiece survivingArt = artPieceRepository.findArtPieceByArtID(artID);
		assertNotNull(survivingArt);
		assertEquals(Integer.valueOf(5), survivingArt.getQuantity());
		assertEquals("artName", survivingArt.getArtName());
	}

	@Test
	@Transactional
	public void testOrphanRemovalOnOrderItemDoesNotAffectArtPiece() {

		Customer customer = new Customer();
		customer.setEmail("customer@example.com");
		customer.setPassword("password");
		customer.setUserName("name");
		customer = customerRepository.save(customer);

		ShoppingCart cart = new ShoppingCart();
		cart.setCustomer(customer);
		cart = shoppingCartRepository.save(cart);

		Artist artist = new Artist();
		artist.setEmail("artist@example.com");
		artist.setPassword("password");
		artist.setUserName("name");
		artist = artistRepository.save(artist);

		ArtPiece art = new ArtPiece();
		art.setArtName("artName");
		art.setDescription("Test art piece");
		art.setDiscountPercentage(0);
		art.setPrice(10.0f);
		art.setQuantity(5);
		art.setCommissionPercentage(10.0f);
		art.setArtist(artist);
		art = artPieceRepository.save(art);

		Integer artID = art.getArtID();

		Order order = new Order();
		order.setOrderNumber(11);
		order.setOrderDate(Date.valueOf("2026-08-19"));
		order.setCustomer(customer);

		OrderItem oi = new OrderItem();
		oi.setArtPiece(art);
		oi.setQuantity(1);
		oi.setListPrice(10.0f);
		oi.setUnitPrice(10.0f);
		oi.setDiscountPercentage(0);
		oi.setCommissionPercentage(10.0f);
		oi.setArtName("artName");
		oi.setDescription("Test art piece");
		oi.setOrder(order);

		Set<OrderItem> orderItems = new HashSet<>();
		orderItems.add(oi);
		order.setOrderItems(orderItems);

		order = orderRepository.save(order);
		entityManager.flush();

		assertEquals(1, orderItemRepository.count());

		/*
		 * orphanRemoval = true: removing the OrderItem from the owning
		 * Order's set (without deleting the Order itself) must delete
		 * the now-orphaned OrderItem row on the next flush.
		 */
		order.getOrderItems().clear();
		orderRepository.save(order);
		entityManager.flush();
		entityManager.clear();

		assertEquals(0, orderItemRepository.count());
		assertEquals(1, orderRepository.count());

		// Same guard as above: orphan removal on OrderItem must not cascade
		// into ArtPiece or touch its stock.
		assertEquals(1, artPieceRepository.count());
		ArtPiece survivingArt = artPieceRepository.findArtPieceByArtID(artID);
		assertNotNull(survivingArt);
		assertEquals(Integer.valueOf(5), survivingArt.getQuantity());
	}
}