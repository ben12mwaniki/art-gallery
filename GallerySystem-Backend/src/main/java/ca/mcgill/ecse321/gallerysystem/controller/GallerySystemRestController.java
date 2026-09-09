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
public class GallerySystemRestController {

	@Autowired
	private GallerySystemService service;

	// ============================================================
	// 1. USER ENDPOINTS
	// ============================================================

	@Tag(name = "Users", description = "Operations for managing users")
	@Operation(summary = "Get a user by email")
	@GetMapping("/user/{email}")
	public ResponseEntity<UserDto> getUser(@PathVariable("email") String email) {
		User user = service.getUser(email);
		return new ResponseEntity<>(convertToDto(user), HttpStatus.OK);
	}

	// ============================================================
	// 2. CUSTOMER ENDPOINTS
	// ============================================================

	@Tag(name = "Customers", description = "Manage customer accounts")
	@Operation(summary = "Create a new customer")
	@PostMapping("/customer")
	public ResponseEntity<CustomerDto> createCustomer(@Valid @RequestBody CustomerRequestDto requestDto) {
		Customer customer = service.createCustomer(
				requestDto.getUserName(),
				requestDto.getEmail(),
				requestDto.getAddress(),
				requestDto.getPassword());
		return new ResponseEntity<>(convertToDto(customer), HttpStatus.CREATED);
	}

	@Tag(name = "Customers", description = "Manage customer accounts")
	@Operation(summary = "Get a customer by email")
	@GetMapping("/customer/{email}")
	public ResponseEntity<CustomerDto> getCustomer(@PathVariable("email") String email) {
		Customer customer = service.getCustomer(email);
		return new ResponseEntity<>(convertToDto(customer), HttpStatus.OK);
	}

	@Tag(name = "Customers", description = "Manage customer accounts")
	@Operation(summary = "Get all customers")
	@GetMapping("/customers")
	public ResponseEntity<List<CustomerDto>> getAllCustomers() {
		List<CustomerDto> customers = service.getAllCustomers().stream()
				.map(this::convertToDto)
				.collect(Collectors.toList());
		return new ResponseEntity<>(customers, HttpStatus.OK);
	}

	@Tag(name = "Customers", description = "Manage customer accounts")
	@Operation(summary = "Partially update a customer")
	@PatchMapping("/customer/{email}")
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

	@Tag(name = "Customers", description = "Manage customer accounts")
	@Operation(summary = "Delete a customer")
	@DeleteMapping("/customer/{email}")
	public ResponseEntity<String> deleteCustomer(@PathVariable("email") String email) {
		service.deleteCustomer(email);
		return new ResponseEntity<>("Customer deleted", HttpStatus.OK);
	}

	// ============================================================
	// 3. ARTIST ENDPOINTS
	// ============================================================

	@Tag(name = "Artists", description = "Manage artist accounts")
	@Operation(summary = "Create a new artist")
	@PostMapping("/artist")
	public ResponseEntity<ArtistDto> createArtist(@Valid @RequestBody ArtistRequestDto requestDto) {
		Artist artist = service.createArtist(
				requestDto.getUserName(),
				requestDto.getEmail(),
				requestDto.getPassword());
		return new ResponseEntity<>(convertToDto(artist), HttpStatus.CREATED);
	}

	@Tag(name = "Artists", description = "Manage artist accounts")
	@Operation(summary = "Get an artist by email")
	@GetMapping("/artist/{email}")
	public ResponseEntity<ArtistDto> getArtist(@PathVariable("email") String email) {
		Artist artist = service.getArtist(email);
		return new ResponseEntity<>(convertToDto(artist), HttpStatus.OK);
	}

	@Tag(name = "Artists", description = "Manage artist accounts")
	@Operation(summary = "Get all artists")
	@GetMapping("/artists")
	public ResponseEntity<List<ArtistDto>> getAllArtists() {
		List<ArtistDto> artists = service.getAllArtists().stream()
				.map(this::convertToDto)
				.collect(Collectors.toList());
		return new ResponseEntity<>(artists, HttpStatus.OK);
	}

	@Tag(name = "Artists", description = "Manage artist accounts")
	@Operation(summary = "Delete an artist")
	@DeleteMapping("/artist/{email}")
	public ResponseEntity<String> deleteArtist(@PathVariable("email") String email) {
		service.deleteArtist(email);
		return new ResponseEntity<>("Artist deleted", HttpStatus.OK);
	}

	// ============================================================
	// 4. ADMINISTRATOR ENDPOINTS
	// ============================================================

	@Tag(name = "Administrators", description = "Manage administrator accounts")
	@Operation(summary = "Create a new administrator")
	@PostMapping("/administrator")
	public ResponseEntity<AdministratorDto> createAdministrator(
			@Valid @RequestBody AdministratorRequestDto requestDto) {
		Administrator administrator = service.createAdministrator(
				requestDto.getUserName(),
				requestDto.getEmail(),
				requestDto.getPassword());
		return new ResponseEntity<>(convertToDto(administrator), HttpStatus.CREATED);
	}

	@Tag(name = "Administrators", description = "Manage administrator accounts")
	@Operation(summary = "Get an administrator by email")
	@GetMapping("/administrator/{email}")
	public ResponseEntity<AdministratorDto> getAdministrator(@PathVariable("email") String email) {
		Administrator administrator = service.getAdministrator(email);
		return new ResponseEntity<>(convertToDto(administrator), HttpStatus.OK);
	}

	@Tag(name = "Administrators", description = "Manage administrator accounts")
	@Operation(summary = "Delete an administrator")
	@DeleteMapping("/administrator/{email}")
	public ResponseEntity<String> deleteAdministrator(@PathVariable("email") String email) {
		service.deleteAdministrator(email);
		return new ResponseEntity<>("Administrator deleted", HttpStatus.OK);
	}

	// ============================================================
	// 5. ARTWORK ENDPOINTS
	// ============================================================

	@Tag(name = "Artwork", description = "Browse and manage artwork listed in the gallery")
	@Operation(summary = "Create a new artwork")
	@PostMapping("/artpiece")
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

	@Tag(name = "Artwork", description = "Browse and manage artwork listed in the gallery")
	@Operation(summary = "Browse available artwork")
	@GetMapping("/artpieces")
	public ResponseEntity<List<ArtPieceDto>> getAllArtPieces() {
		List<ArtPieceDto> artPieceDtos = new ArrayList<>();
		for (ArtPiece artpiece : service.getAllArtPieces()) {
			artPieceDtos.add(convertToDto(artpiece));
		}
		return new ResponseEntity<>(artPieceDtos, HttpStatus.OK);
	}

	@Tag(name = "Artwork", description = "Browse and manage artwork listed in the gallery")
	@Operation(summary = "Get artwork by ID")
	@GetMapping("/artpiece/{artID}")
	public ResponseEntity<ArtPieceDto> getArtPiece(@PathVariable("artID") Integer artID) {
		ArtPiece artPiece = service.getArtpiece(artID);
		return new ResponseEntity<>(convertToDto(artPiece), HttpStatus.OK);
	}

	@Tag(name = "Artwork", description = "Browse and manage artwork listed in the gallery")
	@Operation(summary = "Delete an artwork")
	@DeleteMapping("/artpiece/{artID}")
	public ResponseEntity<String> deleteArtpiece(@PathVariable("artID") Integer artID) {
		service.deleteArtpiece(artID);
		return new ResponseEntity<>("Artpiece deleted", HttpStatus.OK);
	}

	// ============================================================
	// 6. SHOPPING CART ENDPOINTS
	// ============================================================

	@Tag(name = "Shopping Cart", description = "Manage customer shopping carts")
	@Operation(summary = "Create a shopping cart")
	@PostMapping("/shopping-carts/{email}")
	public ResponseEntity<ShoppingCartDto> createShoppingCart(@PathVariable("email") String customerEmail) {
		ShoppingCart cart = service.createShoppingCart(customerEmail);
		return new ResponseEntity<>(convertToDto(cart), HttpStatus.CREATED);
	}

	@Tag(name = "Shopping Cart", description = "Manage customer shopping carts")
	@Operation(summary = "Get a shopping cart")
	@GetMapping("/shopping-carts/{email}")
	public ResponseEntity<ShoppingCartDto> getShoppingCart(@PathVariable("email") String customerEmail) {
		ShoppingCart cart = service.getShoppingCart(customerEmail);
		return new ResponseEntity<>(convertToDto(cart), HttpStatus.OK);
	}

	@Tag(name = "Shopping Cart", description = "Manage customer shopping carts")
	@Operation(summary = "Add an item to cart")
	@PostMapping("/shopping-carts/{email}/items")
	public ResponseEntity<SelectedItemDto> createSelectedItem(
			@PathVariable("email") String customerEmail,
			@Valid @RequestBody SelectedItemRequestDto requestDto) {

		SelectedItem selectedItem = service.createSelectedItem(
				requestDto.getArtID(),
				requestDto.getQuantity(),
				customerEmail);

		return new ResponseEntity<>(convertToDto(selectedItem), HttpStatus.CREATED);
	}

	@Tag(name = "Shopping Cart", description = "Manage customer shopping carts")
	@Operation(summary = "Get cart items")
	@GetMapping("/shopping-carts/{email}/items")
	public ResponseEntity<List<SelectedItemDto>> getSelectedItems(@PathVariable("email") String customerEmail) {
		List<SelectedItemDto> items = service.getSelectedItems(customerEmail).stream()
				.map(this::convertToDto)
				.collect(Collectors.toList());
		return new ResponseEntity<>(items, HttpStatus.OK);
	}

	@Tag(name = "Shopping Cart", description = "Manage customer shopping carts")
	@Operation(summary = "Remove an item from cart")
	@DeleteMapping("/shopping-carts/{email}/items/{itemID}")
	public ResponseEntity<Void> deleteSelectedItem(
			@PathVariable("email") String customerEmail,
			@PathVariable("itemID") Integer itemID) {

		service.deleteSelectedItem(customerEmail, itemID);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Tag(name = "Shopping Cart", description = "Manage customer shopping carts")
	@Operation(summary = "Empty the shopping cart")
	@DeleteMapping("/shopping-carts/{email}/items")
	public ResponseEntity<Void> emptyShoppingCart(@PathVariable("email") String customerEmail) {
		service.emptyShoppingCart(customerEmail);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	// ============================================================
	// 7. ORDER ENDPOINTS
	// ============================================================

	@Tag(name = "Orders", description = "Checkout and manage customer orders")
	@Operation(summary = "Checkout shopping cart")
	@PostMapping("/customers/{email}/checkout")
	public ResponseEntity<OrderDto> checkout(@PathVariable("email") String customerEmail) {
		Order order = service.checkout(customerEmail);
		return new ResponseEntity<>(convertToDto(order), HttpStatus.CREATED);
	}

	@Tag(name = "Orders", description = "Checkout and manage customer orders")
	@Operation(summary = "Get orders by customer")
	@GetMapping("/customers/{email}/orders")
	public ResponseEntity<List<OrderDto>> getOrdersByCustomer(@PathVariable("email") String customerEmail) {
		List<OrderDto> orders = service.getOrdersByCustomer(customerEmail).stream()
				.map(this::convertToDto)
				.collect(Collectors.toList());
		return new ResponseEntity<>(orders, HttpStatus.OK);
	}

	@Tag(name = "Orders", description = "Checkout and manage customer orders")
	@Operation(summary = "Delete an order")
	@DeleteMapping("/order/{orderNumber}")
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
			return new UserDto(c.getUserName(), c.getEmail(), "Customer");
		}
		if (u instanceof Artist) {
			Artist a = (Artist) u;
			return new UserDto(a.getUserName(), a.getEmail(), "Artist");
		} else {
			Administrator admin = (Administrator) u;
			return new UserDto(admin.getUserName(), admin.getEmail(), "Administrator");
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