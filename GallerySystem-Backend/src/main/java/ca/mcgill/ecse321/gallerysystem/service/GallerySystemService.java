package ca.mcgill.ecse321.gallerysystem.service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.util.HashMap;
import java.util.Map;

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

	// Note: not to be exposed via REST API, only used internally for testing.
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
	public Customer updateCustomer(Customer customer) {
		if (customer == null) {
			throw new IllegalArgumentException("Customer cannot be null.");
		}

		String email = requireValidEmail(customer.getEmail(), "Email");

		Customer existingCustomer = customerRepository.findCustomerByEmail(email);
		if (existingCustomer == null) {
			throw new IllegalArgumentException(
					"No customer found with email: " + email);
		}

		existingCustomer.setUserName(
				requireNonBlank(customer.getUserName(), "Username"));
		existingCustomer.setAddress(
				requireNonBlank(customer.getAddress(), "Address"));
		existingCustomer.setPassword(
				requireNonBlank(customer.getPassword(), "Password"));

		return customerRepository.save(existingCustomer);
	}

	@Transactional
	public Customer getCustomer(String email) {
		email = requireValidEmail(email, "Email");
		Customer customer = customerRepository.findCustomerByEmail(email);
		return customer;
	}

	@Transactional
	public List<Customer> getAllCustomers() {
		return toList(customerRepository.findAll());
	}

	@Transactional
	public void deleteCustomer(String email) {
		email = requireValidEmail(email, "Customer email");
		customerRepository.deleteById(email);
	}

	// Note: not to be exposed via REST API, only used internally for testing.
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
		email = requireValidEmail(email, "Email");
		Artist artist = artistRepository.findArtistByEmail(email);
		return artist;
	}

	@Transactional
	public List<Artist> getAllArtists() {
		return toList(artistRepository.findAll());
	}

	@Transactional
	public void deleteArtist(String artistEmail) {
		artistEmail = requireValidEmail(artistEmail, "Artist email");
		Artist artist = artistRepository.findArtistByEmail(artistEmail);

		if (artist == null) {
			throw new IllegalArgumentException("Artist not found!");
		}

		Set<ArtPiece> artPieces = artPieceRepository.findByArtist(artist);

		for (ArtPiece artPiece : artPieces) {
			detachOrderItemsFromArtPiece(artPiece);
			artPieceRepository.delete(artPiece);
		}

		artistRepository.delete(artist);
	}

	// Note: not to be exposed via REST API, only used internally for testing.
	@Transactional
	public void deleteAllArtists() {
		List<Artist> artists = toList(artistRepository.findAll());

		for (Artist artist : artists) {
			deleteArtist(artist.getEmail());
		}
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
		adminEmail = requireValidEmail(adminEmail, "Administrator email");
		Administrator admin = administratorRepository.findAdministratorByEmail(adminEmail);
		return admin;
	}

	@Transactional
	public void deleteAdministrator(String email) {
		email = requireValidEmail(email, "Administrator email");
		administratorRepository.deleteById(email);

	}

	// Note: not to be exposed via REST API, only used internally for testing.
	@Transactional
	public void deleteAllAdministrators() {
		administratorRepository.deleteAll();
	}

	@Transactional
	public User getUser(String email) {
		email = requireValidEmail(email, "Email");
		User user = userRepository.findUserByEmail(email);
		return user;
	}

	@Transactional
	public List<User> getAllUsers() {
		return toList(userRepository.findAll());
	}

	@Transactional
	public ShoppingCart createShoppingCart(String customerEmail) {

		customerEmail = requireNonBlank(customerEmail, "Customer email");
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
	public ShoppingCart getShoppingCart(String email) {
		email = requireValidEmail(email, "Customer email");
		ShoppingCart sc = shoppingCartRepository.findShoppingCartByCustomerEmail(email);
		return sc;
	}

	@Transactional
	public List<ShoppingCart> getAllShoppingCarts() {
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

		SelectedItem existingItem = null;

		for (SelectedItem item : shoppingCart.getSelectedItems()) {
			if (item.getArtPiece().getArtID().equals(artPiece.getArtID())) {
				existingItem = item;
				break;
			}
		}

		int requestedQuantity = quantity;
		if (existingItem != null) {
			requestedQuantity += existingItem.getItemQuantity();
		}

		if (requestedQuantity > artPiece.getQuantity()) {
			throw new IllegalArgumentException(
					"Requested quantity (" + requestedQuantity
							+ ") exceeds available stock (" + artPiece.getQuantity()
							+ ") for art piece ID: " + artID);
		}

		if (existingItem != null) {
			existingItem.setItemQuantity(requestedQuantity);
			return selectedItemRepository.save(existingItem);
		}

		SelectedItem selectedItem = new SelectedItem();
		selectedItem.setArtPiece(artPiece);
		selectedItem.setItemQuantity(quantity);
		selectedItem.setShoppingCart(shoppingCart);

		shoppingCart.getSelectedItems().add(selectedItem);
		shoppingCartRepository.save(shoppingCart);

		return selectedItem;
	}

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

		customerEmail = requireValidEmail(customerEmail, "Customer email");

		Customer customer = customerRepository.findCustomerByEmail(customerEmail);
		if (customer == null) {
			throw new IllegalArgumentException("Customer not found!");
		}

		ShoppingCart cart = shoppingCartRepository
				.findShoppingCartByCustomerEmail(customerEmail);
		if (cart == null) {
			throw new IllegalArgumentException("No cart found for this customer!");
		}

		if (cart.getSelectedItems().isEmpty()) {
			throw new IllegalArgumentException("Cannot checkout an empty cart!");
		}

		// Total all requested quantities by ArtPiece before changing any stock.
		Map<Integer, Integer> requestedQuantities = new HashMap<>();
		Map<Integer, ArtPiece> artPieces = new HashMap<>();

		for (SelectedItem selectedItem : cart.getSelectedItems()) {
			ArtPiece artPiece = selectedItem.getArtPiece();

			requestedQuantities.merge(
					artPiece.getArtID(),
					selectedItem.getItemQuantity(),
					Integer::sum);

			artPieces.put(artPiece.getArtID(), artPiece);
		}

		// Validate the combined request for each ArtPiece.
		for (Integer artID : requestedQuantities.keySet()) {
			ArtPiece artPiece = artPieces.get(artID);
			int requestedQuantity = requestedQuantities.get(artID);

			if (!artPiece.isActive()) {
				throw new IllegalArgumentException(
						"Art piece '" + artPiece.getArtName()
								+ "' is no longer available!");
			}

			if (artPiece.getQuantity() < requestedQuantity) {
				throw new IllegalArgumentException(
						"Insufficient stock for '"
								+ artPiece.getArtName() + "'!");
			}
		}

		// Create immutable purchase snapshots.
		Set<OrderItem> orderItems = new HashSet<>();

		for (SelectedItem selectedItem : cart.getSelectedItems()) {
			ArtPiece artPiece = selectedItem.getArtPiece();

			OrderItem orderItem = new OrderItem();
			orderItem.setArtPiece(artPiece);
			orderItem.setQuantity(selectedItem.getItemQuantity());
			orderItem.setListPrice(artPiece.getPrice());
			orderItem.setDiscountPercentage(artPiece.getDiscountPercentage());
			orderItem.setUnitPrice(calculateUnitPrice(
					artPiece.getPrice(),
					artPiece.getDiscountPercentage()));
			orderItem.setCommissionPercentage(artPiece.getCommissionPercentage());
			orderItem.setArtName(artPiece.getArtName());
			orderItem.setDescription(artPiece.getDescription());

			orderItems.add(orderItem);
		}

		// Decrement each ArtPiece once, by its combined requested quantity.
		for (Integer artID : requestedQuantities.keySet()) {
			ArtPiece artPiece = artPieces.get(artID);
			int requestedQuantity = requestedQuantities.get(artID);

			artPiece.setQuantity(
					artPiece.getQuantity() - requestedQuantity);

			artPieceRepository.save(artPiece);
		}

		// Existing order-number allocation logic.
		Order order = null;
		int attempts = 0;

		while (order == null && attempts < 5) {
			try {
				Integer orderNumber = generateOrderNumber();

				order = createOrder(
						orderNumber,
						new Date(System.currentTimeMillis()),
						customer,
						orderItems);

			} catch (DataIntegrityViolationException e) {
				attempts++;
			}
		}

		if (order == null) {
			throw new IllegalStateException(
					"Could not allocate order number, please retry.");
		}

		// This happens only after the order was created.
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
		ArtPiece artPiece = artPieceRepository.findArtPieceByArtID(artID);

		if (artPiece == null) {
			throw new IllegalArgumentException(
					"No art piece found with ID: " + artID);
		}

		detachOrderItemsFromArtPiece(artPiece);
		artPieceRepository.delete(artPiece);
	}

	@Transactional
	public void deleteAllArtPieces() {
		List<ArtPiece> artPieces = toList(artPieceRepository.findAll());

		for (ArtPiece artPiece : artPieces) {
			deleteArtpiece(artPiece.getArtID());
		}
	}

	private <T> List<T> toList(Iterable<T> iterable) {
		List<T> resultList = new ArrayList<T>();
		for (T t : iterable) {
			resultList.add(t);
		}
		return resultList;

	}

	private void detachOrderItemsFromArtPiece(ArtPiece artPiece) {
		Set<OrderItem> orderItems = orderItemRepository.findByArtPiece(artPiece);

		for (OrderItem orderItem : orderItems) {
			orderItem.setArtPiece(null);
			orderItemRepository.save(orderItem);
		}
	}

}