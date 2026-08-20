package ca.mcgill.ecse321.gallerysystem.controller;

import java.sql.Date;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

//Building a RESTful Web Service, Controller

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ca.mcgill.ecse321.gallerysystem.dto.AdministratorDto;
import ca.mcgill.ecse321.gallerysystem.dto.ArtPieceDto;

import ca.mcgill.ecse321.gallerysystem.dto.ArtistDto;
import ca.mcgill.ecse321.gallerysystem.dto.CustomerDto;
import ca.mcgill.ecse321.gallerysystem.dto.OrderDto;

import ca.mcgill.ecse321.gallerysystem.model.Administrator;

import ca.mcgill.ecse321.gallerysystem.model.ArtPiece;
import ca.mcgill.ecse321.gallerysystem.model.Artist;
import ca.mcgill.ecse321.gallerysystem.model.Customer;
import ca.mcgill.ecse321.gallerysystem.model.Order;
import ca.mcgill.ecse321.gallerysystem.service.GallerySystemService;
import ch.qos.logback.classic.Logger;
import ca.mcgill.ecse321.gallerysystem.dto.ShoppingCartDto;
import ca.mcgill.ecse321.gallerysystem.dto.UserDto;
import ca.mcgill.ecse321.gallerysystem.dto.SelectedItemDto;
import ca.mcgill.ecse321.gallerysystem.model.ShoppingCart;
import ca.mcgill.ecse321.gallerysystem.model.User;
import ca.mcgill.ecse321.gallerysystem.model.SelectedItem;

@CrossOrigin(origins = "*")
@RestController
public class GallerySystemRestController {

	@Autowired
	private GallerySystemService service;

	@PostMapping(value = { "/artpiece/{name}", "/artpiece/{name}/" })
	public ArtPieceDto createArtPieceDto(@PathVariable("name") String name, @RequestParam Integer quantity,
			@RequestParam float price, @RequestParam Integer discountPercentage,
			@RequestParam Float commissionPercentage, @RequestParam String description,
			@RequestParam String artistEmail) throws IllegalArgumentException {
		ArtPiece artpiece = service.createArtPiece(name, quantity, price, discountPercentage, commissionPercentage,
				description, artistEmail);
		return convertToDto(artpiece);
	}

	@GetMapping(value = { "/artpieces", "/artpieces/" })
	public List<ArtPieceDto> getAllArtPieces() {
		List<ArtPieceDto> artPieceDtos = new ArrayList<>();
		for (ArtPiece artpiece : service.getAllArtPieces()) {
			artPieceDtos.add(convertToDto(artpiece));
		}
		return artPieceDtos;
	}

	@DeleteMapping(value = { "/artpiece/{artID}", "/customer/{artID}/" })
	public ResponseEntity<String> deleteArtpiece(@PathVariable("artID") Integer artID) {
		service.deleteArtpiece(artID);
		return new ResponseEntity<>("Artpiece deleted", HttpStatus.OK);
	}

	@DeleteMapping(value = { "/artpieces", "/artpieces/" })
	public ResponseEntity<String> deleteAllArtPieces() {
		service.deleteAllArtPieces();
		return new ResponseEntity<>("All artpieces deleted", HttpStatus.OK);
	}

	private ArtPieceDto convertToDto(ArtPiece a) {
		if (a == null) {
			throw new IllegalArgumentException("There is no such ArtPiece!");
		}
		ArtPieceDto artPieceDto = new ArtPieceDto(a.getArtName(), a.getQuantity(), a.getPrice(),
				a.getDiscountPercentage(), a.getCommissionPercentage(), a.getDescription(), a.getArtID(),
				a.getArtist());
		return artPieceDto;
	}

	@GetMapping(value = { "/user/{email}", "/user/{email}/" })
	public UserDto getUser(@PathVariable("email") String email) {
		return convertToDto(service.getUser(email));
	}

	private UserDto convertToDto(User u) {
		if (u == null) {
			throw new IllegalArgumentException("There is no such User!");
		}
		if (u instanceof Customer) {
			Customer c = (Customer) u;
			UserDto userDto = new UserDto(c.getUserName(), c.getEmail(), c.getPassword(), "Customer");
			return userDto;
		}
		if (u instanceof Artist) {
			Artist a = (Artist) u;
			UserDto userDto = new UserDto(a.getUserName(), a.getEmail(), a.getPassword(), "Artist");
			return userDto;
		} else {
			Administrator admin = (Administrator) u;
			UserDto userDto = new UserDto(admin.getUserName(), admin.getEmail(), admin.getPassword(), "Administrator");
			return userDto;
		}

	}

	@PostMapping(value = { "/customer/{name}", "/customer/{name}/" })
	public CustomerDto createCustomer(@PathVariable("name") String userName, @RequestParam String address,
			@RequestParam String email, @RequestParam String password) throws IllegalArgumentException {
		Customer customer = service.createCustomer(userName, email, address, password);
		return convertToDto(customer);
	}

	@PutMapping(value = { "/customer/{email}", "/customer/{email}/" })
	public CustomerDto updateCustomer(@PathVariable("email") String email, @RequestBody Customer customerDetails)
			throws ResourceNotFoundException {

		Customer customer = service.getCustomer(email);
		customer.setAddress(customerDetails.getAddress());
		customer.setEmail(customerDetails.getEmail());
		customer.setPassword(customerDetails.getPassword());
		customer.setUserName(customerDetails.getUserName());

		final Customer updatedCustomer = service.createCustomer(customer);
		return convertToDto(updatedCustomer);
	}

	@GetMapping(value = { "/customers", "/customers/" })
	public List<CustomerDto> getAllCustomers() {
		return service.getAllCustomers().stream().map(c -> convertToDto(c)).collect(Collectors.toList());
	}

	@GetMapping(value = { "/customer/{email}", "/customer/{email}/" })
	public CustomerDto getCustomer(@PathVariable("email") String email) {
		return convertToDto(service.getCustomer(email));
	}

	@DeleteMapping(value = { "/customer/{email}", "/customer/{email}/" })
	public ResponseEntity<String> deleteCustomer(@PathVariable("email") String email) {
		service.deleteCustomer(email);
		return new ResponseEntity<>("Customer deleted", HttpStatus.OK);
	}

	@DeleteMapping(value = { "/customers", "/customers/" })
	public ResponseEntity<String> deleteAllCustomers() {
		service.deleteAllCustomers();
		return new ResponseEntity<>("All customers deleted", HttpStatus.OK);
	}

	private CustomerDto convertToDto(Customer c) {
		if (c == null) {
			throw new IllegalArgumentException("There is no such Customer!");
		}
		CustomerDto customerDto = new CustomerDto(c.getUserName(), c.getAddress(), c.getEmail(), c.getPassword());
		return customerDto;
	}

	@PostMapping(value = { "/artist/{name}", "/artist/{name}/" })
	public ArtistDto createArtist(@PathVariable("name") String userName, @RequestParam String email,
			@RequestParam String password) throws IllegalArgumentException {
		Artist artist = service.createArtist(userName, email, password);
		return convertToDto(artist);
	}

	@GetMapping(value = { "/artist/{email}", "/artist/{email}/" })
	public ArtistDto getArtist(@PathVariable("email") String email) {
		return convertToDto(service.getArtist(email));
	}

	@GetMapping(value = { "/artists", "/artists/" })
	public List<ArtistDto> getAllArtists() {
		return service.getAllArtists().stream().map(a -> convertToDto(a)).collect(Collectors.toList());
	}

	@DeleteMapping(value = { "/artist/{email}", "/artist/{email}/" })
	public ResponseEntity<String> deleteArtist(@PathVariable("email") String email) {
		service.deleteArtist(email);
		return new ResponseEntity<>("Artist deleted", HttpStatus.OK);
	}

	@DeleteMapping(value = { "/artists", "/artists/" })
	public ResponseEntity<String> deleteAllArtists() {
		service.deleteAllArtists();
		return new ResponseEntity<>("All artists deleted", HttpStatus.OK);
	}

	@PostMapping(value = { "/administrator/{name}", "/administrator/{name}/" })
	public AdministratorDto createAdministrator(@PathVariable("name") String userName, @RequestParam String email,
			@RequestParam String password) throws IllegalArgumentException {
		Administrator administrator = service.createAdministrator(userName, email, password);
		return convertToDto(administrator);
	}

	private AdministratorDto convertToDto(Administrator admin) {
		if (admin == null) {
			throw new IllegalArgumentException("There is no such Administrator!");
		}

		AdministratorDto adminDto = new AdministratorDto(admin.getUserName(), admin.getEmail(), admin.getPassword());
		return adminDto;
	}

	@GetMapping(value = { "/administrator/{email}", "/administrator/{email}/" })
	public AdministratorDto getAdministator(@PathVariable("email") String email) {
		return convertToDto(service.getAdministrator(email));
	}

	@GetMapping(value = { "/administrators", "/administrators/" })
	public List<AdministratorDto> getAllAdministrators() {
		return service.getAllAdministrators().stream().map(admin -> convertToDto(admin)).collect(Collectors.toList());
	}

	@DeleteMapping(value = { "/administrator/{email}", "/administrator/{email}/" })
	public ResponseEntity<String> deleteAdministrator(@PathVariable("email") String email) {
		service.deleteAdministrator(email);
		return new ResponseEntity<>("Administrator deleted", HttpStatus.OK);
	}

	@DeleteMapping(value = { "/administrators", "/administrators/" })
	public ResponseEntity<String> deleteAllAdministrators() {
		service.deleteAllAdministrators();
		return new ResponseEntity<>("All administrators deleted", HttpStatus.OK);
	}

	private ArtistDto convertToDto(Artist a) {
		if (a == null) {
			throw new IllegalArgumentException("There is no such Artist!");
		}
		ArtistDto artistDto = new ArtistDto(a.getUserName(), a.getEmail(), a.getPassword());
		return artistDto;
	}

	@GetMapping(value = { "/shoppingCart", "/shoppingCart/" })
	public List<ShoppingCartDto> getAllShopingCart() {
		return service.getAllShoppingCart().stream().map(sc -> convertToDto(sc)).collect(Collectors.toList());
	}

	@PostMapping(value = { "/create-shoppingCart/{email}", "/create-shoppingCart/{email}/" })
	public ShoppingCartDto createShoppingCart(@PathVariable("email") String customerEmail)
			throws IllegalArgumentException {
		ShoppingCart sc = service.createShoppingCart(customerEmail);
		return convertToDto(sc);
	}

	@GetMapping(value = { "/shoppingCart/{email}", "/create-shoppingCart/{email}/" })
	public ShoppingCartDto getShoppingCart(@PathVariable("email") String customerEmail)
			throws IllegalArgumentException {
		return convertToDto(service.getShoppingCart(customerEmail));
	}

	@PutMapping(value = { "/shoppingCart/{email}", "/shoppingCart/{email}" })
	public ShoppingCartDto updateShoppingCart(@RequestParam("email") String customerEmail, @PathVariable Integer scID) {
		ShoppingCart sc = service.appendItemToShoppingCart(scID, customerEmail);
		return convertToDto(sc);
	}

	@DeleteMapping(value = { "/shoppingCart/{cartID}", "/shoppingCart/{cartID}/" })
	public ResponseEntity<String> deleteShoppingCart(@PathVariable("email") Integer cartID) {
		service.deleteShoppingCart(cartID);
		return new ResponseEntity<>("Shopping Cart deleted", HttpStatus.OK);
	}

	@DeleteMapping(value = { "/shoppingCarts", "/shoppingCarts/" })
	public ResponseEntity<String> deleteAllShoppingCarts() {
		service.deleteAllShoppingCarts();
		return new ResponseEntity<>("All Shopping Carts deleted", HttpStatus.OK);
	}

	private ShoppingCartDto convertToDto(ShoppingCart sc) {
		if (sc == null) {
			throw new IllegalArgumentException("There is no such Shopping Cart!");
		}
		ShoppingCartDto scDto = new ShoppingCartDto(sc.getItemCount(), sc.getCartID(), sc.isEmpty(),
				sc.getCustomer(), sc.getSelectedItems());

		return scDto;
	}

	@GetMapping(value = { "/orders", "/orders/" })
	public List<OrderDto> getAllOrder() {
		return service.getAllOrder().stream().map(o -> convertToDto(o)).collect(Collectors.toList());
	}

	public OrderDto checkout(
			@PathVariable("customerEmail") String customerEmail) throws IllegalArgumentException {

		Order order = service.checkout(customerEmail);
		return convertToDto(order);
	}

	@DeleteMapping(value = { "/order/{orderNumber}", "/shoppingCart/{orderNumber}/" })
	public ResponseEntity<String> deleteOrder(@PathVariable("orderNumber") Integer orderNumber) {
		service.deleteOrder(orderNumber);
		return new ResponseEntity<>("Order deleted", HttpStatus.OK);
	}

	@DeleteMapping(value = { "/orders", "/orders/" })
	public ResponseEntity<String> deleteAllOrders() {
		service.deleteAllOrders();
		return new ResponseEntity<>("All orders deleted", HttpStatus.OK);
	}

	private OrderDto convertToDto(Order o) {
		if (o == null) {
			throw new IllegalArgumentException("There is no such Order!");
		}
		OrderDto oDto = new OrderDto(o.getOrderNumber(), o.getOrderDate(), o.getCustomer());

		return oDto;
	}

	@GetMapping(value = { "/SelectedItem", "/SelectedItem/" })
	public List<SelectedItemDto> getAllSelectedItem() {

		return service.getAllSelectedItem().stream().map(si -> convertToDto(si)).collect(Collectors.toList());

	}

	@PostMapping(value = { "/SelectedItem/{artID}", "/SelectedItem/{artID}/" })
	public SelectedItemDto createSelectedItem(@PathVariable("artID") Integer artID, @RequestParam Integer quantity)
			throws IllegalArgumentException {
		SelectedItem si = service.createSelectedItem(artID, quantity);
		return convertToDto(si);

	}

	@DeleteMapping(value = { "/SelectedItem/{itemID}", "/SelectedItem/{itemID}/" })
	public ResponseEntity<String> deleteSelectedItem(@PathVariable("itemID") Integer itemID) {
		service.deleteSelectedItem(itemID);
		return new ResponseEntity<>("Selected Item deleted", HttpStatus.OK);
	}

	@DeleteMapping(value = { "/selecteditems", "/selecteditems/" })
	public ResponseEntity<String> deleteAllSelectedItems() {
		service.deleteAllSelectedItems();
		return new ResponseEntity<>("All selected items deleted", HttpStatus.OK);
	}

	private SelectedItemDto convertToDto(SelectedItem si) {
		if (si == null) {
			throw new IllegalArgumentException("There is no such Selected Item!");
		}
		SelectedItemDto siDto = new SelectedItemDto(si.getItemID(), si.getItemQuantity(), si.getArtPiece());
		return siDto;
	}

}
