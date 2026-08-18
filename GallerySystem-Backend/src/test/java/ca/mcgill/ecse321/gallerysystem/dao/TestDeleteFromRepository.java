package ca.mcgill.ecse321.gallerysystem.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

	@AfterEach
	public void clearDatabase() {
		/*
		 * Deletion order matters because of foreign-key relationships.
		 *
		 * Dependency chain:
		 *
		 * Order -> ShoppingCart, Customer
		 * ShoppingCart -> Customer, SelectedItem
		 * SelectedItem -> ArtPiece
		 * ArtPiece -> Artist
		 * Artist/Customer/Administrator -> User
		 *
		 * Deleting in dependency order prevents foreign-key constraint
		 * violations and ensures that data from one test does not leak
		 * into another.
		 *
		 * NOTE: ShoppingCart has orphanRemoval=true for SelectedItem,
		 * so carts are deleted before SelectedItems.
		 */
		orderRepository.deleteAll();
		shoppingCartRepository.deleteAll();
		selectedItemRepository.deleteAll();
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

		/*
		 * artID is @GeneratedValue and must not be assigned manually.
		 */
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
		cart.setIsEmpty(true);
		cart.setSelectedItem(new HashSet<SelectedItem>());
		cart.setItemNumber(0);

		/*
		 * cartID is @GeneratedValue and must not be assigned manually.
		 */
		cart = shoppingCartRepository.save(cart);

		SelectedItem item = new SelectedItem();
		item.setArtPiece(art);
		item.setItemQuantity(20);
		item.setShoppingCart(cart);

		/*
		 * itemID is @GeneratedValue and must not be assigned manually.
		 */
		item = selectedItemRepository.save(item);

		/*
		 * Keep both sides of the ShoppingCart-SelectedItem relationship
		 * synchronized.
		 */
		cart.getSelectedItem().add(item);
		cart.setItemNumber(1);
		cart.setIsEmpty(false);

		cart = shoppingCartRepository.save(cart);

		shoppingCartRepository.deleteById(cart.getCartID());

		assertEquals(0, shoppingCartRepository.count());
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
		cart.setIsEmpty(true);

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
		order.setShoppingCart(cart);

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
}