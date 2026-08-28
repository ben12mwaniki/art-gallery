package ca.mcgill.ecse321.gallerysystem.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ca.mcgill.ecse321.gallerysystem.dto.AdministratorDto;
import ca.mcgill.ecse321.gallerysystem.dto.AdministratorRequestDto;
import ca.mcgill.ecse321.gallerysystem.dto.ArtPieceDto;
import ca.mcgill.ecse321.gallerysystem.dto.ArtPieceRequestDto;
import ca.mcgill.ecse321.gallerysystem.dto.ArtistDto;
import ca.mcgill.ecse321.gallerysystem.dto.ArtistRequestDto;
import ca.mcgill.ecse321.gallerysystem.dto.CustomerDto;
import ca.mcgill.ecse321.gallerysystem.dto.CustomerRequestDto;
import ca.mcgill.ecse321.gallerysystem.dto.CustomerUpdateRequestDto;
import ca.mcgill.ecse321.gallerysystem.dto.OrderDto;
import ca.mcgill.ecse321.gallerysystem.dto.OrderItemDto;
import ca.mcgill.ecse321.gallerysystem.dto.SelectedItemDto;
import ca.mcgill.ecse321.gallerysystem.dto.SelectedItemRequestDto;
import ca.mcgill.ecse321.gallerysystem.dto.ShoppingCartDto;
import ca.mcgill.ecse321.gallerysystem.dto.UserDto;
import ca.mcgill.ecse321.gallerysystem.exception.ResourceNotFoundException;
import ca.mcgill.ecse321.gallerysystem.model.Administrator;
import ca.mcgill.ecse321.gallerysystem.model.ArtPiece;
import ca.mcgill.ecse321.gallerysystem.model.Artist;
import ca.mcgill.ecse321.gallerysystem.model.Customer;
import ca.mcgill.ecse321.gallerysystem.model.Order;
import ca.mcgill.ecse321.gallerysystem.model.OrderItem;
import ca.mcgill.ecse321.gallerysystem.model.SelectedItem;
import ca.mcgill.ecse321.gallerysystem.model.ShoppingCart;
import ca.mcgill.ecse321.gallerysystem.model.User;
import ca.mcgill.ecse321.gallerysystem.service.GallerySystemService;

// Swagger imports
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@CrossOrigin(origins = "*")
@RestController
@Tag(name = "Gallery System API", description = "Art Gallery E-Commerce Backend")
public class GallerySystemRestController {

	@Autowired
	private GallerySystemService service;

	// ============================================================
	// 1. USER ENDPOINTS
	// ============================================================

	@Operation(summary = "Get a user by email", description = "Returns a user (Customer, Artist, or Administrator) by their email address")
	@GetMapping(value = { "/user/{email}", "/user/{email}/" })
	public ResponseEntity<UserDto> getUser(@PathVariable("email") String email) {
		User user = service.getUser(email);
		return new ResponseEntity<>(convertToDto(user), HttpStatus.OK);
	}

	// ============================================================
	// 2. CUSTOMER ENDPOINTS
	// ============================================================

	@Operation(summary = "Create a new customer", description = "Registers a new customer in the gallery system")
	@PostMapping(value = { "/customer", "/customer/" })
	public ResponseEntity<CustomerDto> createCustomer(@Valid @RequestBody CustomerRequestDto requestDto) {
		Customer customer = service.createCustomer(
				requestDto.getUserName(),
				requestDto.getEmail(),
				requestDto.getAddress(),
				requestDto.getPassword());
		return new ResponseEntity<>(convertToDto(customer), HttpStatus.CREATED);
	}

	@Operation(summary = "Get a customer by email", description = "Returns a customer's details by their email address")
	@GetMapping(value = { "/customer/{email}", "/customer/{email}/" })
	public ResponseEntity<CustomerDto> getCustomer(@PathVariable("email") String email) {
		Customer customer = service.getCustomer(email);
		return new ResponseEntity<>(convertToDto(customer), HttpStatus.OK);
	}

	@Operation(summary = "Get all customers", description = "Returns a list of all registered customers")
	@GetMapping(value = { "/customers", "/customers/" })
	public ResponseEntity<List<CustomerDto>> getAllCustomers() {
		List<CustomerDto> customers = service.getAllCustomers().stream()
				.map(this::convertToDto)
				.collect(Collectors.toList());
		return new ResponseEntity<>(customers, HttpStatus.OK);
	}

	@Operation(summary = "Partially update a customer", description = "Updates only the fields provided in the request (PATCH)")
	@PatchMapping(value = { "/customer/{email}", "/customer/{email}/" })
	public ResponseEntity<CustomerDto> updateCustomerPartial(
			@PathVariable("email") String email,
			@RequestBody CustomerUpdateRequestDto updateDto) {

		Customer customer = service.getCustomer(email);

		if (updateDto.hasUserName()) {
			customer.setUserName(updateDto.getUserName());
		}
		if (updateDto.hasAddress()) {
			customer.setAddress(updateDto.getAddress());
		}
		if (updateDto.hasPassword()) {
			customer.setPassword(updateDto.getPassword());
		}

		Customer updatedCustomer = service.updateCustomer(customer);
		return new ResponseEntity<>(convertToDto(updatedCustomer), HttpStatus.OK);
	}

	@Operation(summary = "Delete a customer", description = "Deletes a customer by their email address")
	@DeleteMapping(value = { "/customer/{email}", "/customer/{email}/" })
	public ResponseEntity<String> deleteCustomer(@PathVariable("email") String email) {
		service.deleteCustomer(email);
		return new ResponseEntity<>("Customer deleted", HttpStatus.OK);
	}

	// ============================================================
	// 3. ARTIST ENDPOINTS
	// ============================================================

	@Operation(summary = "Create a new artist", description = "Registers a new artist in the gallery system")
	@PostMapping(value = { "/artist", "/artist/" })
	public ResponseEntity<ArtistDto> createArtist(@Valid @RequestBody ArtistRequestDto requestDto) {
		Artist artist = service.createArtist(
				requestDto.getUserName(),
				requestDto.getEmail(),
				requestDto.getPassword());
		return new ResponseEntity<>(convertToDto(artist), HttpStatus.CREATED);
	}

	@Operation(summary = "Get an artist by email", description = "Returns an artist's details by their email address")
	@GetMapping(value = { "/artist/{email}", "/artist/{email}/" })
	public ResponseEntity<ArtistDto> getArtist(@PathVariable("email") String email) {
		Artist artist = service.getArtist(email);
		return new ResponseEntity<>(convertToDto(artist), HttpStatus.OK);
	}

	@Operation(summary = "Get all artists", description = "Returns a list of all registered artists")
	@GetMapping(value = { "/artists", "/artists/" })
	public ResponseEntity<List<ArtistDto>> getAllArtists() {
		List<ArtistDto> artists = service.getAllArtists().stream()
				.map(this::convertToDto)
				.collect(Collectors.toList());
		return new ResponseEntity<>(artists, HttpStatus.OK);
	}

	@Operation(summary = "Delete an artist", description = "Deletes an artist and all their associated art pieces")
	@DeleteMapping(value = { "/artist/{email}", "/artist/{email}/" })
	public ResponseEntity<String> deleteArtist(@PathVariable("email") String email) {
		service.deleteArtist(email);
		return new ResponseEntity<>("Artist deleted", HttpStatus.OK);
	}

	// ============================================================
	// 4. ADMINISTRATOR ENDPOINTS
	// ============================================================

	@Operation(summary = "Create a new administrator", description = "Registers a new administrator in the gallery system")
	@PostMapping(value = { "/administrator", "/administrator/" })
	public ResponseEntity<AdministratorDto> createAdministrator(
			@Valid @RequestBody AdministratorRequestDto requestDto) {
		Administrator administrator = service.createAdministrator(
				requestDto.getUserName(),
				requestDto.getEmail(),
				requestDto.getPassword());
		return new ResponseEntity<>(convertToDto(administrator), HttpStatus.CREATED);
	}

	@Operation(summary = "Get an administrator by email", description = "Returns an administrator's details by their email address")
	@GetMapping(value = { "/administrator/{email}", "/administrator/{email}/" })
	public ResponseEntity<AdministratorDto> getAdministrator(@PathVariable("email") String email) {
		Administrator administrator = service.getAdministrator(email);
		return new ResponseEntity<>(convertToDto(administrator), HttpStatus.OK);
	}

	@Operation(summary = "Delete an administrator", description = "Deletes an administrator by their email address")
	@DeleteMapping(value = { "/administrator/{email}", "/administrator/{email}/" })
	public ResponseEntity<String> deleteAdministrator(@PathVariable("email") String email) {
		service.deleteAdministrator(email);
		return new ResponseEntity<>("Administrator deleted", HttpStatus.OK);
	}

	// ============================================================
	// 5. ART PIECE ENDPOINTS
	// ============================================================

	@Operation(summary = "Create a new art piece", description = "Adds a new art piece to the gallery")
	@PostMapping(value = { "/artpiece", "/artpiece/" })
	public ResponseEntity<ArtPieceDto> createArtPiece(@Valid @RequestBody ArtPieceRequestDto requestDto) {
		ArtPiece artpiece = service.createArtPiece(
				requestDto.getArtName(),
				requestDto.getQuantity(),
				requestDto.getPrice(),
				requestDto.getDiscountPercentage(),
				requestDto.getCommissionPercentage(),
				requestDto.getDescription(),
				requestDto.getArtistEmail());
		return new ResponseEntity<>(convertToDto(artpiece), HttpStatus.CREATED);
	}

	@Operation(summary = "Get all art pieces", description = "Returns a list of all art pieces in the gallery")
	@GetMapping(value = { "/artpieces", "/artpieces/" })
	public ResponseEntity<List<ArtPieceDto>> getAllArtPieces() {
		List<ArtPieceDto> artPieceDtos = new ArrayList<>();
		for (ArtPiece artpiece : service.getAllArtPieces()) {
			artPieceDtos.add(convertToDto(artpiece));
		}
		return new ResponseEntity<>(artPieceDtos, HttpStatus.OK);
	}

	@Operation(summary = "Delete an art piece", description = "Deletes an art piece by its ID")
	@DeleteMapping(value = { "/artpiece/{artID}", "/artpiece/{artID}/" })
	public ResponseEntity<String> deleteArtpiece(@PathVariable("artID") Integer artID) {
		service.deleteArtpiece(artID);
		return new ResponseEntity<>("Artpiece deleted", HttpStatus.OK);
	}

	// ============================================================
	// 6. SHOPPING CART ENDPOINTS
	// ============================================================

	@Operation(summary = "Create a shopping cart", description = "Creates a new shopping cart for a customer")
	@PostMapping(value = { "/shopping-carts/{email}", "/shopping-carts/{email}/" })
	public ResponseEntity<ShoppingCartDto> createShoppingCart(@PathVariable("email") String customerEmail) {
		ShoppingCart cart = service.createShoppingCart(customerEmail);
		return new ResponseEntity<>(convertToDto(cart), HttpStatus.CREATED);
	}

	@Operation(summary = "Get a shopping cart", description = "Returns a customer's shopping cart")
	@GetMapping(value = { "/shopping-carts/{email}", "/shopping-carts/{email}/" })
	public ResponseEntity<ShoppingCartDto> getShoppingCart(@PathVariable("email") String customerEmail) {
		ShoppingCart cart = service.getShoppingCart(customerEmail);
		return new ResponseEntity<>(convertToDto(cart), HttpStatus.OK);
	}

	@Operation(summary = "Add an item to cart", description = "Adds an art piece to the shopping cart")
	@PostMapping(value = { "/shopping-carts/{email}/items", "/shopping-carts/{email}/items/" })
	public ResponseEntity<SelectedItemDto> createSelectedItem(
			@PathVariable("email") String customerEmail,
			@Valid @RequestBody SelectedItemRequestDto requestDto) {

		SelectedItem selectedItem = service.createSelectedItem(
				requestDto.getArtID(),
				requestDto.getQuantity(),
				customerEmail);

		return new ResponseEntity<>(convertToDto(selectedItem), HttpStatus.CREATED);
	}

	@Operation(summary = "Get cart items", description = "Returns all items in a customer's shopping cart")
	@GetMapping(value = { "/shopping-carts/{email}/items", "/shopping-carts/{email}/items/" })
	public ResponseEntity<List<SelectedItemDto>> getSelectedItems(@PathVariable("email") String customerEmail) {
		List<SelectedItemDto> items = service.getSelectedItems(customerEmail).stream()
				.map(this::convertToDto)
				.collect(Collectors.toList());
		return new ResponseEntity<>(items, HttpStatus.OK);
	}

	@Operation(summary = "Remove an item from cart", description = "Removes a specific item from the shopping cart")
	@DeleteMapping(value = { "/shopping-carts/{email}/items/{itemID}", "/shopping-carts/{email}/items/{itemID}/" })
	public ResponseEntity<Void> deleteSelectedItem(
			@PathVariable("email") String customerEmail,
			@PathVariable("itemID") Integer itemID) {

		service.deleteSelectedItem(customerEmail, itemID);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Operation(summary = "Empty the shopping cart", description = "Removes all items from the shopping cart")
	@DeleteMapping(value = { "/shopping-carts/{email}/items", "/shopping-carts/{email}/items/" })
	public ResponseEntity<Void> emptyShoppingCart(@PathVariable("email") String customerEmail) {
		service.emptyShoppingCart(customerEmail);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	// ============================================================
	// 7. ORDER ENDPOINTS
	// ============================================================

	@Operation(summary = "Checkout", description = "Converts the shopping cart into an order")
	@PostMapping(value = { "/customers/{email}/checkout", "/customers/{email}/checkout/" })
	public ResponseEntity<OrderDto> checkout(@PathVariable("email") String customerEmail) {
		Order order = service.checkout(customerEmail);
		return new ResponseEntity<>(convertToDto(order), HttpStatus.CREATED);
	}

	@Operation(summary = "Get orders by customer", description = "Returns all orders for a specific customer")
	@GetMapping(value = { "/customers/{email}/orders", "/customers/{email}/orders/" })
	public ResponseEntity<List<OrderDto>> getOrdersByCustomer(@PathVariable("email") String customerEmail) {
		List<OrderDto> orders = service.getOrdersByCustomer(customerEmail).stream()
				.map(this::convertToDto)
				.collect(Collectors.toList());
		return new ResponseEntity<>(orders, HttpStatus.OK);
	}

	@Operation(summary = "Delete an order", description = "Deletes an order by its order number")
	@DeleteMapping(value = { "/order/{orderNumber}", "/order/{orderNumber}/" })
	public ResponseEntity<String> deleteOrder(@PathVariable("orderNumber") Integer orderNumber) {
		service.deleteOrder(orderNumber);
		return new ResponseEntity<>("Order deleted", HttpStatus.OK);
	}

	// ============================================================
	// 8. DTO CONVERSION METHODS
	// ============================================================

	/**
	 * Convert User entity to UserDto
	 */
	private UserDto convertToDto(User u) {
		if (u == null) {
			throw new ResourceNotFoundException("There is no such User!");
		}
		if (u instanceof Customer) {
			Customer c = (Customer) u;
			return new UserDto(c.getUserName(), c.getEmail(), c.getPassword(), "Customer");
		}
		if (u instanceof Artist) {
			Artist a = (Artist) u;
			return new UserDto(a.getUserName(), a.getEmail(), a.getPassword(), "Artist");
		} else {
			Administrator admin = (Administrator) u;
			return new UserDto(admin.getUserName(), admin.getEmail(), admin.getPassword(), "Administrator");
		}
	}

	/**
	 * Convert Customer entity to CustomerDto
	 */
	private CustomerDto convertToDto(Customer c) {
		if (c == null) {
			throw new ResourceNotFoundException("There is no such Customer!");
		}
		return new CustomerDto(
				c.getUserName(),
				c.getAddress(),
				c.getEmail());
	}

	/**
	 * Convert Artist entity to ArtistDto
	 */
	private ArtistDto convertToDto(Artist a) {
		if (a == null) {
			throw new ResourceNotFoundException("There is no such Artist!");
		}
		return new ArtistDto(
				a.getUserName(),
				a.getEmail());
	}

	/**
	 * Convert Administrator entity to AdministratorDto
	 */
	private AdministratorDto convertToDto(Administrator admin) {
		if (admin == null) {
			throw new ResourceNotFoundException("There is no such Administrator!");
		}
		return new AdministratorDto(
				admin.getUserName(),
				admin.getEmail());
	}

	/**
	 * Convert ArtPiece entity to ArtPieceDto
	 */
	private ArtPieceDto convertToDto(ArtPiece a) {
		if (a == null) {
			throw new ResourceNotFoundException("There is no such ArtPiece!");
		}
		return new ArtPieceDto(
				a.getArtName(),
				a.getQuantity(),
				a.getPrice(),
				a.getDiscountPercentage(),
				a.getCommissionPercentage(),
				a.getDescription(),
				a.getArtID(),
				a.getArtist().getUserName());
	}

	/**
	 * Convert ShoppingCart entity to ShoppingCartDto
	 */
	private ShoppingCartDto convertToDto(ShoppingCart sc) {
		if (sc == null) {
			throw new ResourceNotFoundException("There is no such Shopping Cart!");
		}
		return new ShoppingCartDto(sc.getItemCount(), sc.getCartID());
	}

	/**
	 * Convert SelectedItem entity to SelectedItemDto
	 */
	private SelectedItemDto convertToDto(SelectedItem si) {
		if (si == null) {
			throw new ResourceNotFoundException("There is no such Selected Item!");
		}
		ArtPiece artPiece = si.getArtPiece();
		return new SelectedItemDto(
				si.getItemID(),
				si.getItemQuantity(),
				artPiece.getArtID(),
				artPiece.getArtName(),
				artPiece.getPrice(),
				artPiece.getDiscountPercentage(),
				artPiece.getDescription());
	}

	/**
	 * Convert Order entity to OrderDto
	 */
	private OrderDto convertToDto(Order order) {
		if (order == null) {
			throw new ResourceNotFoundException("There is no such Order!");
		}

		List<OrderItemDto> orderItemDtos = order.getOrderItems().stream()
				.map(this::convertToDto)
				.collect(Collectors.toList());

		return new OrderDto(
				order.getOrderNumber(),
				order.getOrderDate(),
				order.getCustomer().getEmail(),
				orderItemDtos);
	}

	/**
	 * Convert OrderItem entity to OrderItemDto
	 */
	private OrderItemDto convertToDto(OrderItem orderItem) {
		Integer artPieceID = (orderItem.getArtPiece() != null)
				? orderItem.getArtPiece().getArtID()
				: null;

		return new OrderItemDto(
				orderItem.getOrderItemID(),
				artPieceID,
				orderItem.getQuantity(),
				orderItem.getListPrice(),
				orderItem.getUnitPrice(),
				orderItem.getDiscountPercentage(),
				orderItem.getCommissionPercentage(),
				orderItem.getArtName(),
				orderItem.getDescription());
	}
}