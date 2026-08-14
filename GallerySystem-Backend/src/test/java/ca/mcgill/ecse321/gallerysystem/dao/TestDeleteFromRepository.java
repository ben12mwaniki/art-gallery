package ca.mcgill.ecse321.gallerysystem.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ca.mcgill.ecse321.gallerysystem.model.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest

public class TestDeleteFromRepository {

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
		shoppingCartRepository.deleteAll();
		userRepository.deleteAll();
		artistRepository.deleteAll();
		customerRepository.deleteAll();
		administratorRepository.deleteAll();
		artPieceRepository.deleteAll();
		orderRepository.deleteAll();
		selectedItemRepository.deleteAll();

	}

	@Test
	public void testDeleteCustomer() {

		Customer customer = new Customer();

		String email = "email";
		String password = "password";
		String name = "name";

		customer.setEmail(email);
		customer.setPassword(password);
		customer.setUserName(name);

		customerRepository.save(customer);
		customerRepository.deleteById(customer.getEmail());
		assertEquals(customerRepository.count(), 0);

	}

	@Test
	public void testDeleteArtist() {
		Artist artist = new Artist();

		String email = "artist_email";
		String password = "password";
		String name = "name";

		artist.setEmail(email);
		artist.setPassword(password);
		artist.setUserName(name);
		artistRepository.save(artist);
		artistRepository.deleteById(artist.getEmail());
		assertEquals(artistRepository.count(), 0);

	}

	@Test
	public void testDeleteArtPiece() {
		ArtPiece art = new ArtPiece();
		Artist artist = new Artist();
		Set<ArtPiece> artPieces = new HashSet<ArtPiece>();

		String email = "artist_email";
		String password = "password";
		String name = "name";
		Integer artID = 1;
		Integer discount = 70;
		float price = 1.1f;
		Integer quantity = 1;
		String artName = "artName";

		artist.setEmail(email);
		artist.setPassword(password);
		artist.setUserName(name);
		artistRepository.save(artist);

		art.setDescription("");
		art.setDiscountPercentage(discount);
		art.setPrice(price);
		art.setQuantity(quantity);
		art.setArtID(artID);
		art.setArtName(artName);
		art.setArtist(artist);
		artPieceRepository.save(art);

		artPieces.add(art);
		artPieceRepository.deleteById(art.getArtID());
		assertEquals(artPieceRepository.count(), 0);

	}

	@Test
	public void testDeleteShoppingCart() {

		Artist artist = new Artist();

		String artist_email = "artist_email";
		String artist_password = "password";
		String artist_name = "name";

		artist.setEmail(artist_email);
		artist.setPassword(artist_password);
		artist.setUserName(artist_name);
		artistRepository.save(artist);

		ArtPiece art = new ArtPiece();
		Integer artID = 1;
		Integer discount = 70;
		float price = 1.1f;
		Integer quantity = 1;
		String artName = "artName";
		art.setArtID(artID);
		art.setArtName(artName);
		art.setDescription("");
		art.setDiscountPercentage(discount);
		art.setPrice(price);
		art.setQuantity(quantity);
		art.setArtist(artist);
		artPieceRepository.save(art);

		Customer customer = new Customer();
		String customer_email = "customer_email";
		String customer_password = "password";
		String customer_name = "name";
		customer.setEmail(customer_email);
		customer.setPassword(customer_password);
		customer.setUserName(customer_name);
		customerRepository.save(customer);

		SelectedItem item = new SelectedItem();
		Integer itemID = 78;
		Integer itemQuantity = 20;
		item.setArtPiece(art);
		item.setItemID(itemID);
		item.setItemQuantity(itemQuantity);
		selectedItemRepository.save(item);
		Set<SelectedItem> items = new HashSet<SelectedItem>();
		items.add(item);

		ShoppingCart cart = new ShoppingCart();
		Integer cartID = 1;

		cart.setCartID(cartID);
		cart.setCustomer(customer);
		cart.setIsEmpty(true);
		cart.setSelectedItem(items);
		cart.setItemNumber(items.size());
		shoppingCartRepository.save(cart);
		shoppingCartRepository.deleteById(cart.getCartID());
		assertEquals(shoppingCartRepository.count(), 0);

	}

	@Test
	public void testDeleteOrder() {

		Customer customer = new Customer();
		String customer_email = "customer_email";
		String customer_password = "password";
		String customer_name = "name";
		customer.setEmail(customer_email);
		customer.setPassword(customer_password);
		customer.setUserName(customer_name);
		customerRepository.save(customer);

		ShoppingCart cart = new ShoppingCart();
		Integer cartID = 1;
		cart.setCartID(cartID);
		cart.setCustomer(customer);
		cart.setIsEmpty(true);
		shoppingCartRepository.save(cart);

		Order order = new Order();
		Integer year = 2020;
		Integer month = 10;
		Integer day = 28;
		Date date = new Date(year, month, day);
		Integer orderNumber = 10;
		order.setCustomer(customer);
		order.setOrderDate(date);
		order.setOrderNumber(orderNumber);
		order.setShoppingCart(cart);
		orderRepository.save(order);
		orderRepository.deleteById(order.getOrderNumber());
		assertEquals(orderRepository.count(), 0);

	}

}
