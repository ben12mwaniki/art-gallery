package ca.mcgill.ecse321.gallerysystem.service;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// Implementing use cases

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.mcgill.ecse321.gallerysystem.dao.*;
import ca.mcgill.ecse321.gallerysystem.model.*;

@Service
public class GallerySystemService {

	@Autowired
	SelectedItemRepository selectedItemRepository;
	@Autowired
	OrderRepository orderRepository;
	@Autowired
	ShoppingCartRepository shoppingCartRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	ArtistRepository artistRepository;
	@Autowired
	CustomerRepository customerRepository;
	@Autowired
	AdministratorRepository administratorRepository;
	@Autowired
	ArtPieceRepository artPieceRepository;
	@Autowired
	OrderItemRepository orderItemRepository;

	// Basic email format check: something@something.something
	private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

	/**
	 * Trims a String field and throws a specific, descriptive error if it is
	 * null or blank. Returns the trimmed value so callers can store the
	 * cleaned-up version instead of the raw input (fixes stray whitespace,
	 * e.g. tab/newline characters, from being silently persisted).
	 */
	private String requireNonBlank(String value, String fieldName) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException(fieldName + " cannot be empty.");
		}
		return value.trim();
	}

	/**
	 * Trims and validates an email field: not blank, and matches a basic
	 * email pattern. Returns the trimmed value.
	 */
	private String requireValidEmail(String email, String fieldName) {
		String trimmed = requireNonBlank(email, fieldName);
		if (!trimmed.matches(EMAIL_REGEX)) {
			throw new IllegalArgumentException(fieldName + " is not a validly formatted email address: " + trimmed);
		}
		return trimmed;
	}

	@Transactional
	public User getUser(String email) {
		User user = userRepository.findUserByEmail(email);
		return user;
	}

	@Transactional
	public ArtPiece createArtPiece(String artName, Integer quantity, float price, Integer discountPercentage,
			Float commissionPercentage, String description, String artistEmail) {
		artName = requireNonBlank(artName, "Art piece name");
		description = requireNonBlank(description, "Description");
		artistEmail = requireValidEmail(artistEmail, "Artist email");
		if (quantity == null || quantity <= 0) {
			throw new IllegalArgumentException("Quantity must be a positive number.");
		}
		if (price < 0) {
			throw new IllegalArgumentException("Price cannot be negative.");
		}
		if (discountPercentage == null || discountPercentage < 0) {
			throw new IllegalArgumentException("Discount percentage cannot be null or negative.");
		}
		if (commissionPercentage == null || commissionPercentage < 0) {
			throw new IllegalArgumentException("Commission percentage cannot be null or negative.");
		}

		Artist artist = artistRepository.findArtistByEmail(artistEmail);
		if (artist == null) {
			throw new IllegalArgumentException("No artist found with email: " + artistEmail);
		}

		ArtPiece artpiece = new ArtPiece();
		artpiece.setQuantity(quantity);
		artpiece.setPrice(price);
		artpiece.setDiscountPercentage(discountPercentage);
		artpiece.setCommissionPercentage(commissionPercentage);
		artpiece.setArtName(artName);
		artpiece.setDescription(description);
		artpiece.setArtist(artist);

		artPieceRepository.save(artpiece);
		return artpiece;
	}

	@Transactional
	public ArtPiece getArtpiece(Integer artID) {
		ArtPiece artpiece = artPieceRepository.findArtPieceByArtID(artID);
		return artpiece;
	}

	@Transactional
	public List<ArtPiece> getAllArtPieces() {
		return toList(artPieceRepository.findAll());
	}

	@Transactional
	public Customer createCustomer(String userName, String email, String address, String password) {
		userName = requireNonBlank(userName, "Username");
		email = requireValidEmail(email, "Email");
		address = requireNonBlank(address, "Address");
		password = requireNonBlank(password, "Password");
		if (userRepository.findUserByEmail(email) != null) {
			throw new IllegalArgumentException("A user with this email already exists: " + email);
		}
		Customer customer = new Customer();
		customer.setAddress(address);
		customer.setEmail(email);
		customer.setUserName(userName);
		customer.setPassword(password);

		customerRepository.save(customer);
		return customer;
	}

	@Transactional
	public Customer createCustomer(Customer customer) {
		Customer updatedCustomer = customer;

		customerRepository.save(updatedCustomer);
		return updatedCustomer;

	}

	@Transactional
	public Customer getCustomer(String email) {
		Customer customer = customerRepository.findCustomerByEmail(email);
		return customer;
	}

	@Transactional
	public List<Customer> getAllCustomers() {
		return toList(customerRepository.findAll());
	}

	@Transactional
	public void deleteCustomer(String email) {
		customerRepository.deleteById(email);
	}

	@Transactional
	public void deleteAllCustomers() {
		customerRepository.deleteAll();
	}

	@Transactional
	public Artist createArtist(String userName, String email, String password) {
		userName = requireNonBlank(userName, "Username");
		email = requireValidEmail(email, "Email");
		password = requireNonBlank(password, "Password");
		if (userRepository.findUserByEmail(email) != null) {
			throw new IllegalArgumentException("A user with this email already exists: " + email);
		}
		Artist artist = new Artist();
		artist.setEmail(email);
		artist.setUserName(userName);
		artist.setPassword(password);

		artistRepository.save(artist);
		return artist;
	}

	@Transactional
	public Artist getArtist(String email) {
		Artist artist = artistRepository.findArtistByEmail(email);
		return artist;
	}

	@Transactional
	public List<Artist> getAllArtists() {
		return toList(artistRepository.findAll());
	}

	@Transactional
	public void deleteArtist(String artistEmail) {
		Artist artist = artistRepository.findArtistByEmail(artistEmail);
		if (artist == null) {
			throw new IllegalArgumentException("Artist not found!");
		}

		Set<ArtPiece> pieces = artPieceRepository.findByArtist(artist);

		for (ArtPiece art : pieces) {
			// detach historical OrderItems before the ArtPiece row disappears
			Set<OrderItem> items = orderItemRepository.findByArtPiece(art);
			for (OrderItem oi : items) {
				oi.setArtPiece(null);
				orderItemRepository.save(oi);
			}
			artPieceRepository.delete(art);
		}

		artistRepository.delete(artist);
	}

	@Transactional
	public void deleteAllArtists() {
		artistRepository.deleteAll();
	}

	@Transactional
	public Administrator createAdministrator(String userName, String email, String password) {
		userName = requireNonBlank(userName, "Username");
		email = requireValidEmail(email, "Email");
		password = requireNonBlank(password, "Password");
		if (userRepository.findUserByEmail(email) != null) {
			throw new IllegalArgumentException("A user with this email already exists: " + email);
		}
		Administrator administrator = new Administrator();
		administrator.setEmail(email);
		administrator.setUserName(userName);
		administrator.setPassword(password);

		administratorRepository.save(administrator);
		return administrator;
	}

	@Transactional
	public List<Administrator> getAllAdministrators() {
		return toList(administratorRepository.findAll());
	}

	@Transactional
	public Administrator getAdministrator(String adminEmail) {
		Administrator admin = administratorRepository.findAdministratorByEmail(adminEmail);
		return admin;
	}

	@Transactional
	public void deleteAdministrator(String email) {
		administratorRepository.deleteById(email);

	}

	@Transactional
	public void deleteAllAdministrators() {
		administratorRepository.deleteAll();
	}

	@Transactional
	public List<User> getAllUsers() {
		return toList(userRepository.findAll());
	}

	@Transactional
	public ShoppingCart createShoppingCart(String customerEmail) {
		if (customerEmail == null) {
			throw new IllegalArgumentException("Invalid Inputs!");
		}

		Customer customer = customerRepository.findCustomerByEmail(customerEmail);
		if (customer == null) {
			throw new IllegalArgumentException("Customer not found!");
		}

		if (shoppingCartRepository.findShoppingCartByCustomerEmail(customerEmail) != null) {
			throw new IllegalArgumentException("Customer already has a cart!");
		}

		ShoppingCart sc = new ShoppingCart();
		sc.setCustomer(customer);
		sc.setSelectedItems(new HashSet<>());
		return shoppingCartRepository.save(sc);
	}

	@Transactional
	public ShoppingCart appendItemToShoppingCart(Integer scID, String email) {
		if (scID == null) {
			throw new IllegalArgumentException("Selected item ID cannot be null.");
		}
		email = requireValidEmail(email, "Customer email");

		ShoppingCart sc = shoppingCartRepository.findShoppingCartByCustomerEmail(email);
		if (sc == null) {
			throw new IllegalArgumentException("No shopping cart found for customer with email: " + email);
		}

		SelectedItem item = selectedItemRepository.findSelectedItemByItemID(scID);
		if (item == null) {
			throw new IllegalArgumentException("No selected item found with ID: " + scID);
		}

		Set<SelectedItem> si = sc.getSelectedItems();
		si.add(item);
		shoppingCartRepository.save(sc);
		return sc;
	}

	@Transactional
	public ShoppingCart getShoppingCart(String email) {
		ShoppingCart sc = shoppingCartRepository.findShoppingCartByCustomerEmail(email);
		return sc;
	}

	@Transactional
	public List<ShoppingCart> getAllShoppingCart() {
		return toList(shoppingCartRepository.findAll());
	}

	@Transactional
	public void deleteShoppingCart(Integer cartID) {
		shoppingCartRepository.deleteById(cartID);

	}

	@Transactional
	public void deleteAllShoppingCarts() {
		shoppingCartRepository.deleteAll();
	}

	@Transactional
	public SelectedItem createSelectedItem(
			Integer artID,
			Integer quantity,
			String customerEmail) {

		if (artID == null) {
			throw new IllegalArgumentException(
					"Art piece ID cannot be null.");
		}

		if (quantity == null || quantity <= 0) {
			throw new IllegalArgumentException(
					"Quantity must be a positive number.");
		}

		if (customerEmail == null || customerEmail.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"Customer email cannot be null or blank.");
		}

		ArtPiece artPiece = artPieceRepository.findArtPieceByArtID(artID);

		if (artPiece == null) {
			throw new IllegalArgumentException(
					"No art piece found with ID: " + artID);
		}

		if (quantity > artPiece.getQuantity()) {
			throw new IllegalArgumentException(
					"Requested quantity (" + quantity
							+ ") exceeds available stock (" + artPiece.getQuantity()
							+ ") for art piece ID: " + artID);
		}

		ShoppingCart shoppingCart = shoppingCartRepository
				.findShoppingCartByCustomerEmail(customerEmail.trim());

		if (shoppingCart == null) {
			throw new IllegalArgumentException(
					"No shopping cart found for customer: "
							+ customerEmail);
		}

		SelectedItem selectedItem = new SelectedItem();
		selectedItem.setArtPiece(artPiece);
		selectedItem.setItemQuantity(quantity);
		selectedItem.setShoppingCart(shoppingCart);

		shoppingCart.getSelectedItems().add(selectedItem);

		shoppingCartRepository.save(shoppingCart);

		return selectedItem;
	}

	// Note: not to be exposed via REST API, only used internally for testing.
	@Transactional
	public List<SelectedItem> getAllSelectedItem() {
		return toList(selectedItemRepository.findAll());
	}

	// Note: not to be exposed via REST API, only used internally for testing.
	@Transactional
	public void deleteAllSelectedItems() {
		selectedItemRepository.deleteAll();
	}

	@Transactional
	public void deleteSelectedItem(Integer itemID) {
		selectedItemRepository.deleteById(itemID);

	}

	@Transactional
	public Order checkout(String customerEmail) {

		Customer customer = customerRepository.findCustomerByEmail(customerEmail);
		if (customer == null) {
			throw new IllegalArgumentException("Customer not found!");
		}

		ShoppingCart cart = shoppingCartRepository.findShoppingCartByCustomerEmail(customerEmail);
		if (cart == null) {
			throw new IllegalArgumentException("No cart found for this customer!");
		}

		if (cart.getSelectedItems().isEmpty()) {
			throw new IllegalArgumentException("Cannot checkout an empty cart!");
		}

		// --- stock/availability validation (fail fast, before mutating anything) ---
		for (SelectedItem si : cart.getSelectedItems()) {
			ArtPiece art = si.getArtPiece();
			if (!art.isActive()) {
				throw new IllegalArgumentException(
						"Art piece '" + art.getArtName() + "' is no longer available!");
			}
			if (art.getQuantity() < si.getItemQuantity()) {
				throw new IllegalArgumentException(
						"Insufficient stock for '" + art.getArtName() + "'!");
			}
		}

		// --- build snapshot OrderItems + decrement stock ---
		Set<OrderItem> orderItems = new HashSet<>();
		for (SelectedItem si : cart.getSelectedItems()) {
			ArtPiece art = si.getArtPiece();

			OrderItem oi = new OrderItem();
			oi.setArtPiece(art);
			oi.setQuantity(si.getItemQuantity());
			oi.setListPrice(art.getPrice());
			oi.setDiscountPercentage(art.getDiscountPercentage());
			oi.setUnitPrice(calculateUnitPrice(art.getPrice(), art.getDiscountPercentage()));
			oi.setCommissionPercentage(art.getCommissionPercentage());
			oi.setArtName(art.getArtName());
			oi.setDescription(art.getDescription());
			orderItems.add(oi);

			art.setQuantity(art.getQuantity() - si.getItemQuantity());
			artPieceRepository.save(art);
		}

		// --- allocate order number, retrying on rare collision ---
		Order order = null;
		int attempts = 0;
		while (order == null && attempts < 5) {
			try {
				Integer orderNumber = generateOrderNumber();
				order = createOrder(orderNumber, new Date(System.currentTimeMillis()), customer, orderItems);
			} catch (DataIntegrityViolationException e) {
				attempts++; // another checkout grabbed this number first, retry
			}
		}
		if (order == null) {
			throw new IllegalStateException("Could not allocate order number, please retry.");
		}

		// --- empty the cart only after the order is safely created ---
		cart.getSelectedItems().clear();
		shoppingCartRepository.save(cart);

		return order;
	}

	/**
	 * Pure construction + persistence. Does not touch the cart.
	 * Kept separate so it can be unit-tested/reused without a full checkout flow.
	 */
	@Transactional
	public Order createOrder(
			Integer orderNumber,
			Date orderDate,
			Customer customer,
			Set<OrderItem> orderItems) {

		if (orderNumber == null || orderDate == null
				|| customer == null || orderItems == null) {
			throw new IllegalArgumentException("Invalid Input!");
		}

		Order o = new Order();
		o.setOrderNumber(orderNumber);
		o.setOrderDate(orderDate);
		o.setCustomer(customer);
		o.setOrderItems(orderItems);

		for (OrderItem oi : orderItems) {
			oi.setOrder(o);
		}

		return orderRepository.save(o);
	}

	public Integer generateOrderNumber() {
		LocalDate today = LocalDate.now();
		String key = today.format(DateTimeFormatter.BASIC_ISO_DATE);
		int from = Integer.parseInt(key + "00");
		int to = Integer.parseInt(key + "99") + 1;

		int seq = orderRepository.countByOrderNumberBetween(from, to) + 1;
		if (seq > 99) {
			throw new IllegalStateException("Daily order limit reached!");
		}
		return Integer.parseInt(key + String.format("%02d", seq));
	}

	private float calculateUnitPrice(float listPrice, Integer discountPercentage) {
		float discount = listPrice * (discountPercentage / 100f);
		return listPrice - discount;
	}

	@Transactional
	public Order getOrder(Integer ordernumber) {
		Order o = orderRepository.findOrderByOrderNumber(ordernumber);
		return o;
	}

	@Transactional
	public List<Order> getAllOrder() {
		return toList(orderRepository.findAll());
	}

	@Transactional
	public void deleteOrder(Integer orderNumber) {
		orderRepository.deleteById(orderNumber);

	}

	@Transactional
	public void deleteAllOrders() {
		orderRepository.deleteAll();
	}

	@Transactional
	public void deleteArtpiece(Integer artID) {
		artPieceRepository.deleteById(artID);

	}

	@Transactional
	public void deleteAllArtPieces() {
		artPieceRepository.deleteAll();
	}

	private <T> List<T> toList(Iterable<T> iterable) {
		List<T> resultList = new ArrayList<T>();
		for (T t : iterable) {
			resultList.add(t);
		}
		return resultList;
	}

}