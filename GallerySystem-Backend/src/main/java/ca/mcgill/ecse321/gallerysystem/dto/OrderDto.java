package ca.mcgill.ecse321.gallerysystem.dto;

import java.sql.Date;

import ca.mcgill.ecse321.gallerysystem.model.Customer;
import ca.mcgill.ecse321.gallerysystem.model.ShoppingCart;

public class OrderDto {

	private Integer orderNumber;
	private Date orderDate;
	private ShoppingCart shoppingCart;
	private Customer customer;

	public OrderDto() {
	}

	public OrderDto(Integer orderNumber, Date orderDate, Customer customer) {
		this.orderNumber = orderNumber;
		this.orderDate = orderDate;
		this.customer = customer;
	}

	public Integer getOrderNumber() {
		return orderNumber;
	}

	public Date getOrderDate() {
		return orderDate;
	}

	public ShoppingCart getShoppingCart() {
		return shoppingCart;
	}

	public Customer getCustomer() {
		return customer;
	}
}
