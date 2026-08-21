package ca.mcgill.ecse321.gallerysystem.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

import java.sql.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import ca.mcgill.ecse321.gallerysystem.dao.CustomerRepository;
import ca.mcgill.ecse321.gallerysystem.dao.OrderRepository;
import ca.mcgill.ecse321.gallerysystem.model.Customer;
import ca.mcgill.ecse321.gallerysystem.model.Order;
import ca.mcgill.ecse321.gallerysystem.model.OrderItem;

@ExtendWith(MockitoExtension.class)
public class TestCreateOrder {
	@Mock
	private CustomerRepository customerDao;
	@Mock
	private OrderRepository orderDao;

	@InjectMocks
	private GallerySystemService service;

	@BeforeEach
	public void setMockOutput() {
		Answer<?> returnParameterAsAnswer = (InvocationOnMock invocation) -> {
			return invocation.getArgument(0);
		};
		lenient().when(orderDao.save(any(Order.class))).thenAnswer(returnParameterAsAnswer);
		lenient().when(customerDao.save(any(Customer.class))).thenAnswer(returnParameterAsAnswer);
	}

	/**
	 * createOrder() is a pure construction/persistence method: it does not
	 * look up the customer or touch a shopping cart. Its only job is to
	 * assemble an Order from already-known, already-validated data and
	 * wire the owning side of the OrderItem relationship.
	 */
	@Test
	public void testCreateOrder() {
		Integer orderNumber = 2026081901;
		Date orderDate = Date.valueOf("2026-08-19");
		Customer customer = new Customer();
		customer.setEmail("david@mail.com");
		customer.setUserName("David");

		OrderItem oi = new OrderItem();
		oi.setArtName("Test Piece");
		Set<OrderItem> orderItems = new HashSet<>();
		orderItems.add(oi);

		Order order = assertDoesNotThrow(
				() -> service.createOrder(orderNumber, orderDate, customer, orderItems));

		assertEquals(orderNumber, order.getOrderNumber());
		assertEquals(orderDate, order.getOrderDate());
		assertEquals(customer, order.getCustomer());
		assertEquals(1, order.getOrderItems().size());

		// createOrder must set the owning side of the relationship: every
		// OrderItem passed in must point back at the Order it belongs to.
		OrderItem savedItem = order.getOrderItems().iterator().next();
		assertEquals(order, savedItem.getOrder());
	}

	@Test
	public void testInvalidCreateOrderNullOrderNumber() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> service.createOrder(null, Date.valueOf("2026-08-19"),
						new Customer(), new HashSet<OrderItem>()));
		assertEquals("Invalid Input!", e.getMessage());
	}

	@Test
	public void testInvalidCreateOrderNullOrderDate() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> service.createOrder(2026081901, null,
						new Customer(), new HashSet<OrderItem>()));
		assertEquals("Invalid Input!", e.getMessage());
	}

	@Test
	public void testInvalidCreateOrderNullCustomer() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> service.createOrder(2026081901, Date.valueOf("2026-08-19"),
						null, new HashSet<OrderItem>()));
		assertEquals("Invalid Input!", e.getMessage());
	}

	@Test
	public void testInvalidCreateOrderNullOrderItems() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> service.createOrder(2026081901, Date.valueOf("2026-08-19"),
						new Customer(), null));
		assertEquals("Invalid Input!", e.getMessage());
	}
}