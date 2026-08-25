package ca.mcgill.ecse321.gallerysystem.dto;

import java.sql.Date;
import java.util.List;

public class OrderDto {

	private Integer orderNumber;
	private Date orderDate;
	private String customerEmail;
	private List<OrderItemDto> orderItems;

	public OrderDto() {
	}

	public OrderDto(Integer orderNumber, Date orderDate,
			String customerEmail, List<OrderItemDto> orderItems) {

		this.orderNumber = orderNumber;
		this.orderDate = orderDate;
		this.customerEmail = customerEmail;
		this.orderItems = orderItems;
	}

	public Integer getOrderNumber() {
		return orderNumber;
	}

	public Date getOrderDate() {
		return orderDate;
	}

	public String getCustomerEmail() {
		return customerEmail;
	}

	public List<OrderItemDto> getOrderItems() {
		return orderItems;
	}
}