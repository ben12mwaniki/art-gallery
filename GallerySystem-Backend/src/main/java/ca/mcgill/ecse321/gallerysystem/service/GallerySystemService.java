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
import ca.mcgill.ecse321.gallerysystem.exception.ResourceNotFoundException;
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

	// ==================== ART PIECE METHODS ====================

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
			throw new ResourceNotFoundException("No artist found with email: " + artistEmail);
		}

		ArtPiece artpiece = new ArtPiece();
		artpiece.setQuantity(quantity);
		artpiece.setPrice(price);
		artpiece.setDiscountPercentage(discountPercentage);
		artpiece.setCommissionPercentage(commissionPercentage);
		artpiece.setArtName(artName);
		artpiece.setDescription(description);
		artpiece.setArtist(artist);
		artpiece.setActive(true);

		artPieceRepository.save(artpiece);
		return artpiece;
	}

	@Transactional
	public ArtPiece getArtpiece(Integer artID) {
		if (artID == null) {
			throw new IllegalArgumentException("Art piece ID cannot be null.");
		}

		ArtPiece artpiece = artPieceRepository.findArtPieceByArtID(artID);
		if (artpiece == null) {
			throw new ResourceNotFoundException("No art piece found with ID: " + artID);
		}
		return artpiece;
	}

	@Transactional
	public List<ArtPiece> getArtPiecesByArtistEmail(String artistEmail) {
		artistEmail = requireValidEmail(artistEmail, "Artist email");
		Artist artist = artistRepository.findArtistByEmail(artistEmail);
		if (artist == null) {
			throw new ResourceNotFoundException("No artist found with email: " + artistEmail);
		}
		return toList(artPieceRepository.findByArtist(artist));
	}

	@Transactional
	public List<ArtPiece> getAllArtPieces() {
		return toList(artPieceRepository.findAll());
	}

	@Transactional
	public void deleteArtpiece(Integer artID) {
		if (artID == null) {
			throw new IllegalArgumentException("Art piece ID cannot be null.");
		}

		ArtPiece artPiece = artPieceRepository.findArtPieceByArtID(artID);
		if (artPiece == null) {
			throw new ResourceNotFoundException("No art piece found with ID: " + artID);
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

	// ==================== CUSTOMER METHODS ====================

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
			throw new ResourceNotFoundException("No customer found with email: " + email);
		}

		existingCustomer.setUserName(requireNonBlank(customer.getUserName(), "Username"));
		existingCustomer.setAddress(requireNonBlank(customer.getAddress(), "Address"));
		existingCustomer.setPassword(requireNonBlank(customer.getPassword(), "Password"));

		return customerRepository.save(existingCustomer);
	}

	@Transactional
	public Customer getCustomer(String email) {
		email = requireValidEmail(email, "Email");
		Customer customer = customerRepository.findCustomerByEmail(email);
		if (customer == null) {
			throw new ResourceNotFoundException("No customer found with email: " + email);
		}
		return customer;
	}

	@Transactional
	public List<Customer> getAllCustomers() {
		return toList(customerRepository.findAll());
	}

	@Transactional
	public void deleteCustomer(String email) {
		email = requireValidEmail(email, "Customer email");
		Customer customer = customerRepository.findCustomerByEmail(email);
		if (customer == null) {
			throw new ResourceNotFoundException("No customer found with email: " + email);
		}
		customerRepository.delete(customer);
	}

	@Transactional
	public void deleteAllCustomers() {
		customerRepository.deleteAll();
	}

	// ==================== ARTIST METHODS ====================

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
		if (artist == null) {
			throw new ResourceNotFoundException("No artist found with email: " + email);
		}
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
			throw new ResourceNotFoundException("No artist found with email: " + artistEmail);
		}

		Set<ArtPiece> artPieces = artPieceRepository.findByArtist(artist);
		for (ArtPiece artPiece : artPieces) {
			detachOrderItemsFromArtPiece(artPiece);
			artPieceRepository.delete(artPiece);
		}

		artistRepository.delete(artist);
	}

	@Transactional
	public void deleteAllArtists() {
		List<Artist> artists = toList(artistRepository.findAll());
		for (Artist artist : artists) {
			deleteArtist(artist.getEmail());
		}
	}

	// ==================== ADMINISTRATOR METHODS ====================

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
	public Administrator getAdministrator(String adminEmail) {
		adminEmail = requireValidEmail(adminEmail, "Administrator email");
		Administrator admin = administratorRepository.findAdministratorByEmail(adminEmail);
		if (admin == null) {
			throw new ResourceNotFoundException("No administrator found with email: " + adminEmail);
		}
		return admin;
	}

	@Transactional
	public List<Administrator> getAllAdministrators() {
		return toList(administratorRepository.findAll());
	}

	@Transactional
	public void deleteAdministrator(String email) {
		email = requireValidEmail(email, "Administrator email");
		Administrator admin = administratorRepository.findAdministratorByEmail(email);
		if (admin == null) {
			throw new ResourceNotFoundException("No administrator found with email: " + email);
		}
		administratorRepository.delete(admin);
	}

	@Transactional
	public void deleteAllAdministrators() {
		administratorRepository.deleteAll();
	}

	// ==================== USER METHODS ====================

	@Transactional
	public User getUser(String email) {
		email = requireValidEmail(email, "Email");
		User user = userRepository.findUserByEmail(email);
		if (user == null) {
			throw new ResourceNotFoundException("No user found with email: " + email);
		}
		return user;
	}

	@Transactional
	public List<User> getAllUsers() {
		return toList(userRepository.findAll());
	}

	// ==================== SHOPPING CART METHODS ====================

	@Transactional
	public ShoppingCart createShoppingCart(String customerEmail) {
		customerEmail = requireValidEmail(customerEmail, "Customer email");

		Customer customer = customerRepository.findCustomerByEmail(customerEmail);
		if (customer == null) {
			throw new ResourceNotFoundException("No customer found with email: " + customerEmail);
		}

		if (shoppingCartRepository.findShoppingCartByCustomerEmail(customerEmail) != null) {
			throw new IllegalArgumentException("Customer already has a cart!");
		}

		ShoppingCart sc = new ShoppingCart();
		customer.setShoppingCart(sc);
		sc.setCustomer(customer);
		sc.setSelectedItems(new HashSet<>());
		return shoppingCartRepository.save(sc);
	}

	@Transactional
	public ShoppingCart getShoppingCart(String email) {
		email = requireValidEmail(email, "Customer email");
		ShoppingCart sc = shoppingCartRepository.findShoppingCartByCustomerEmail(email);
		if (sc == null) {
			throw new ResourceNotFoundException("No shopping cart found for customer: " + email);
		}
		return sc;
	}

	@Transactional
	public List<ShoppingCart> getAllShoppingCarts() {
		return toList(shoppingCartRepository.findAll());
	}

	@Transactional
	public void deleteShoppingCart(Integer cartID) {
		if (cartID == null) {
			throw new IllegalArgumentException("Cart ID cannot be null.");
		}

		ShoppingCart cart = shoppingCartRepository.findShoppingCartByCartID(cartID);
		if (cart == null) {
			throw new ResourceNotFoundException("No shopping cart found with ID: " + cartID);
		}
		shoppingCartRepository.delete(cart);
	}

	@Transactional
	public void deleteAllShoppingCarts() {
		shoppingCartRepository.deleteAll();
	}

	@Transactional
	public void emptyShoppingCart(String customerEmail) {
		customerEmail = requireValidEmail(customerEmail, "Customer email");

		ShoppingCart cart = shoppingCartRepository.findShoppingCartByCustomerEmail(customerEmail);
		if (cart == null) {
			throw new ResourceNotFoundException("No shopping cart found for customer: " + customerEmail);
		}

		cart.getSelectedItems().clear();
		shoppingCartRepository.save(cart);
	}

	// ==================== SELECTED ITEM METHODS ====================

	@Transactional
	public SelectedItem createSelectedItem(Integer artID, Integer quantity, String customerEmail) {
		if (artID == null) {
			throw new IllegalArgumentException("Art piece ID cannot be null.");
		}
		if (quantity == null || quantity <= 0) {
			throw new IllegalArgumentException("Quantity must be a positive number.");
		}
		customerEmail = requireValidEmail(customerEmail, "Customer email");

		ArtPiece artPiece = artPieceRepository.findArtPieceByArtID(artID);
		if (artPiece == null) {
			throw new ResourceNotFoundException("No art piece found with ID: " + artID);
		}

		if (quantity > artPiece.getQuantity()) {
			throw new IllegalArgumentException("Requested quantity (" + quantity
					+ ") exceeds available stock (" + artPiece.getQuantity()
					+ ") for art piece ID: " + artID);
		}

		ShoppingCart shoppingCart = shoppingCartRepository.findShoppingCartByCustomerEmail(customerEmail);
		if (shoppingCart == null) {
			throw new ResourceNotFoundException("No shopping cart found for customer: " + customerEmail);
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
			throw new IllegalArgumentException("Requested quantity (" + requestedQuantity
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

	@Transactional
	public List<SelectedItem> getSelectedItems(String customerEmail) {
		customerEmail = requireValidEmail(customerEmail, "Customer email");

		ShoppingCart cart = shoppingCartRepository.findShoppingCartByCustomerEmail(customerEmail);
		if (cart == null) {
			throw new ResourceNotFoundException("No shopping cart found for customer: " + customerEmail);
		}

		return new ArrayList<>(cart.getSelectedItems());
	}

	@Transactional
	public SelectedItem getSelectedItem(Integer itemID) {
		if (itemID == null) {
			throw new IllegalArgumentException("Selected item ID cannot be null.");
		}

		SelectedItem selectedItem = selectedItemRepository.findSelectedItemByItemID(itemID);
		if (selectedItem == null) {
			throw new ResourceNotFoundException("No selected item found with ID: " + itemID);
		}
		return selectedItem;
	}

	@Transactional
	public void deleteSelectedItem(String customerEmail, Integer itemID) {
		customerEmail = requireValidEmail(customerEmail, "Customer email");
		if (itemID == null) {
			throw new IllegalArgumentException("Selected item ID cannot be null.");
		}

		ShoppingCart cart = shoppingCartRepository.findShoppingCartByCustomerEmail(customerEmail);
		if (cart == null) {
			throw new ResourceNotFoundException("No shopping cart found for customer: " + customerEmail);
		}

		SelectedItem selectedItem = selectedItemRepository.findSelectedItemByItemID(itemID);
		if (selectedItem == null) {
			throw new ResourceNotFoundException("No selected item found with ID: " + itemID);
		}

		if (!selectedItem.getShoppingCart().getCartID().equals(cart.getCartID())) {
			throw new IllegalArgumentException("Selected item does not belong to this shopping cart.");
		}

		cart.getSelectedItems().remove(selectedItem);
		shoppingCartRepository.save(cart);
	}

	@Transactional
	public void deleteSelectedItem(Integer itemID) {
		if (itemID == null) {
			throw new IllegalArgumentException("Selected item ID cannot be null.");
		}

		SelectedItem selectedItem = selectedItemRepository.findSelectedItemByItemID(itemID);
		if (selectedItem == null) {
			throw new ResourceNotFoundException("No selected item found with ID: " + itemID);
		}
		selectedItemRepository.delete(selectedItem);
	}

	@Transactional
	public List<SelectedItem> getAllSelectedItem() {
		return toList(selectedItemRepository.findAll());
	}

	@Transactional
	public void deleteAllSelectedItems() {
		selectedItemRepository.deleteAll();
	}

	// ==================== ORDER METHODS ====================

	@Transactional
	public Order checkout(String customerEmail) {
		customerEmail = requireValidEmail(customerEmail, "Customer email");

		Customer customer = customerRepository.findCustomerByEmail(customerEmail);
		if (customer == null) {
			throw new ResourceNotFoundException("No customer found with email: " + customerEmail);
		}

		ShoppingCart cart = shoppingCartRepository.findShoppingCartByCustomerEmail(customerEmail);
		if (cart == null) {
			throw new ResourceNotFoundException("No cart found for this customer!");
		}

		if (cart.getSelectedItems().isEmpty()) {
			throw new IllegalArgumentException("Cannot checkout an empty cart!");
		}

		// Total all requested quantities by ArtPiece before changing any stock.
		Map<Integer, Integer> requestedQuantities = new HashMap<>();
		Map<Integer, ArtPiece> artPieces = new HashMap<>();

		for (SelectedItem selectedItem : cart.getSelectedItems()) {
			ArtPiece artPiece = selectedItem.getArtPiece();
			requestedQuantities.merge(artPiece.getArtID(), selectedItem.getItemQuantity(), Integer::sum);
			artPieces.put(artPiece.getArtID(), artPiece);
		}

		// Validate the combined request for each ArtPiece.
		for (Integer artID : requestedQuantities.keySet()) {
			ArtPiece artPiece = artPieces.get(artID);
			int requestedQuantity = requestedQuantities.get(artID);

			if (!artPiece.isActive()) {
				throw new IllegalArgumentException("Art piece '" + artPiece.getArtName()
						+ "' is no longer available!");
			}

			if (artPiece.getQuantity() < requestedQuantity) {
				throw new IllegalArgumentException("Insufficient stock for '"
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
			orderItem.setUnitPrice(calculateUnitPrice(artPiece.getPrice(), artPiece.getDiscountPercentage()));
			orderItem.setCommissionPercentage(artPiece.getCommissionPercentage());
			orderItem.setArtName(artPiece.getArtName());
			orderItem.setDescription(artPiece.getDescription());

			orderItems.add(orderItem);
		}

		// Decrement each ArtPiece once, by its combined requested quantity.
		for (Integer artID : requestedQuantities.keySet()) {
			ArtPiece artPiece = artPieces.get(artID);
			int requestedQuantity = requestedQuantities.get(artID);
			artPiece.setQuantity(artPiece.getQuantity() - requestedQuantity);
			artPieceRepository.save(artPiece);
		}

		// Existing order-number allocation logic.
		Order order = null;
		int attempts = 0;

		while (order == null && attempts < 5) {
			try {
				Integer orderNumber = generateOrderNumber();
				order = createOrder(orderNumber, new Date(System.currentTimeMillis()), customer, orderItems);
			} catch (DataIntegrityViolationException e) {
				attempts++;
			}
		}

		if (order == null) {
			throw new IllegalStateException("Could not allocate order number, please retry.");
		}

		// This happens only after the order was created.
		cart.getSelectedItems().clear();
		shoppingCartRepository.save(cart);

		return order;
	}

	@Transactional
	public Order createOrder(Integer orderNumber, Date orderDate, Customer customer, Set<OrderItem> orderItems) {
		if (orderNumber == null || orderDate == null || customer == null || orderItems == null) {
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

	@Transactional
	public Order getOrder(Integer orderNumber) {
		if (orderNumber == null) {
			throw new IllegalArgumentException("Order number cannot be null.");
		}

		Order o = orderRepository.findOrderByOrderNumber(orderNumber);
		if (o == null) {
			throw new ResourceNotFoundException("No order found with number: " + orderNumber);
		}
		return o;
	}

	@Transactional(readOnly = true)
	public List<Order> getOrdersByCustomer(String customerEmail) {
		customerEmail = requireValidEmail(customerEmail, "Customer email");

		Customer customer = customerRepository.findCustomerByEmail(customerEmail);
		if (customer == null) {
			throw new ResourceNotFoundException("No customer found with email: " + customerEmail);
		}

		return orderRepository.findByCustomerEmailOrderByOrderDateDesc(customerEmail);
	}

	@Transactional
	public List<Order> getAllOrder() {
		return toList(orderRepository.findAll());
	}

	@Transactional
	public void deleteOrder(Integer orderNumber) {
		if (orderNumber == null) {
			throw new IllegalArgumentException("Order number cannot be null.");
		}

		Order order = orderRepository.findOrderByOrderNumber(orderNumber);
		if (order == null) {
			throw new ResourceNotFoundException("No order found with number: " + orderNumber);
		}
		orderRepository.delete(order);
	}

	@Transactional
	public void deleteAllOrders() {
		orderRepository.deleteAll();
	}

	// ==================== HELPER METHODS ====================

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

	private void detachOrderItemsFromArtPiece(ArtPiece artPiece) {
		Set<OrderItem> orderItems = orderItemRepository.findByArtPiece(artPiece);
		for (OrderItem orderItem : orderItems) {
			orderItem.setArtPiece(null);
			orderItemRepository.save(orderItem);
		}
	}

	private <T> List<T> toList(Iterable<T> iterable) {
		List<T> resultList = new ArrayList<T>();
		for (T t : iterable) {
			resultList.add(t);
		}
		return resultList;
	}

	// ==================== VALIDATION HELPERS ====================

	private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

	private String requireNonBlank(String value, String fieldName) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException(fieldName + " cannot be empty.");
		}
		return value.trim();
	}

	private String requireValidEmail(String email, String fieldName) {
		String trimmed = requireNonBlank(email, fieldName);
		if (!trimmed.matches(EMAIL_REGEX)) {
			throw new IllegalArgumentException(fieldName + " is not a validly formatted email address: " + trimmed);
		}
		return trimmed;
	}
}