package ca.mcgill.ecse321.gallerysystem.service;

import java.sql.Date;

// Implementing use cases


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import org.springframework.beans.factory.annotation.Autowired;
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
	public ArtPiece createArtPiece(String artName, Integer quantity, float price, Integer discountPercentage, Float commissionPercentage, String description, String artistEmail) {
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
	public Customer createCustomer(String userName,  String email, String address, String password) {
		userName = requireNonBlank(userName, "Username");
		email = requireValidEmail(email, "Email");
		address = requireNonBlank(address, "Address");
		password = requireNonBlank(password, "Password");
		if (userRepository.findUserByEmail(email) != null) {
			throw new IllegalArgumentException("A user with this email already exists: " + email);
		}
		Customer customer  = new Customer();
		customer.setAddress(address);
		customer.setEmail(email);
		customer.setUserName(userName);
		customer.setPassword(password);
		
		
		customerRepository.save(customer);
		return customer;	
	}
	
	@Transactional 
	public Customer createCustomer(Customer customer) {
		Customer updatedCustomer  = customer;

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
	public Artist createArtist(String userName,  String email, String password) {
		userName = requireNonBlank(userName, "Username");
		email = requireValidEmail(email, "Email");
		password = requireNonBlank(password, "Password");
		if (userRepository.findUserByEmail(email) != null) {
			throw new IllegalArgumentException("A user with this email already exists: " + email);
		}
		Artist artist  = new Artist();
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
	public void deleteArtist(String email) {
		artistRepository.deleteById(email);
		
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
		Administrator administrator  = new Administrator();
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
	public List<User> getAllUsers(){
		return toList(userRepository.findAll());
	}
	
	@Transactional
	public ShoppingCart createShoppingCart(String customerEmail) {
		Integer itemNumber = 0;
		boolean isEmpty = true;
		Set<SelectedItem> selectedItems = new HashSet<SelectedItem>();
		if(customerEmail == null) {
			 throw new IllegalArgumentException("Invalid Inputs!");
		}
		ShoppingCart sc = new ShoppingCart();
		//sc.setCartID(cartID);
		sc.setCustomer(customerRepository.findCustomerByEmail(customerEmail));
		sc.setItemNumber(itemNumber);
		sc.setIsEmpty(isEmpty);
		sc.setSelectedItem(selectedItems);
		shoppingCartRepository.save(sc);
		return sc;
	}
	
	@Transactional
	public ShoppingCart appendItemToShoppingCart(Integer scID, String email){
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

		Set<SelectedItem> si = sc.getSelectedItem();
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
	public List<ShoppingCart> getAllShoppingCart(){
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
	public SelectedItem createSelectedItem(Integer artID, Integer quantity) {
		if (artID == null) {
			throw new IllegalArgumentException("Art piece ID cannot be null.");
		}
		if (quantity == null || quantity <= 0) {
			throw new IllegalArgumentException("Quantity must be a positive number.");
		}

		ArtPiece artPiece = artPieceRepository.findArtPieceByArtID(artID);
		if (artPiece == null) {
			throw new IllegalArgumentException("No art piece found with ID: " + artID);
		}
		if (quantity > artPiece.getQuantity()) {
			throw new IllegalArgumentException(
				"Requested quantity (" + quantity + ") exceeds available stock (" + artPiece.getQuantity() + ") for art piece ID: " + artID);
		}

		SelectedItem si = new SelectedItem();
		si.setArtPiece(artPiece);
		si.setItemQuantity(quantity);
		selectedItemRepository.save(si);
		return si;
	}
	
	@Transactional
	public List<SelectedItem> getAllSelectedItem(){
		return toList(selectedItemRepository.findAll());
	}
	
	@Transactional
	public void deleteSelectedItem(Integer itemID) {
		selectedItemRepository.deleteById(itemID);
		
	}
	@Transactional
	public void deleteAllSelectedItems() {
		selectedItemRepository.deleteAll();
	}
	
	@Transactional
	public Order createOrder(Integer ordernumber, Date orderDate, ShoppingCart shoppingCart, Customer customer) {
		if(ordernumber == null || orderDate == null || shoppingCart == null || customer == null) {
			 throw new IllegalArgumentException("Invalid Input!");
		}
		Order o = new Order();
		o.setOrderNumber(ordernumber);
		o.setOrderDate(orderDate);
		o.setShoppingCart(shoppingCart);
		o.setCustomer(customer);
		return o;
	}
	
	
	
	@Transactional
	public Order getOrder(Integer ordernumber) {
		Order o = orderRepository.findOrderByOrderNumber(ordernumber);
		return o;
	}
	
	@Transactional
	public List<Order> getAllOrder(){
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
	
	private <T> List<T> toList(Iterable<T> iterable){
		List<T> resultList = new ArrayList<T>();
		for (T t : iterable) {
			resultList.add(t);
		}
		return resultList;
	}

	
	
	

}