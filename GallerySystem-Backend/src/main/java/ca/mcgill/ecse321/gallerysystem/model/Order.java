package ca.mcgill.ecse321.gallerysystem.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;

import javax.persistence.Id;
import java.sql.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "orders")
public class Order {

	private Integer orderNumber;
	private Date orderDate;
	private Customer customer;
	private Set<OrderItem> orderItems = new HashSet<>();

	public void setOrderNumber(Integer value) {
		this.orderNumber = value;
	}

	@Id
	public Integer getOrderNumber() {
		return this.orderNumber;
	}

	public void setOrderDate(Date value) {
		this.orderDate = value;
	}

	public Date getOrderDate() {
		return this.orderDate;
	}

	@ManyToOne(optional = false)
	public Customer getCustomer() {
		return this.customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<OrderItem> getOrderItems() {
		return this.orderItems;
	}

	public void setOrderItems(Set<OrderItem> orderItems) {
		this.orderItems = orderItems;
	}

}