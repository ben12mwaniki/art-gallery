package ca.mcgill.ecse321.gallerysystem.model;

import javax.persistence.Entity;

import javax.persistence.FetchType;
import javax.persistence.CascadeType;
import java.util.Set;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity
public class Customer extends User {
	private String userRole = "customer";
	private ShoppingCart shoppingCart;
	private String address;

	public void setUserRole(String value) {
		this.userRole = value;
	}

	public String getUserRole() {
		return this.userRole;
	}

	@OneToOne(mappedBy = "customer", cascade = { CascadeType.ALL })
	public ShoppingCart getShoppingCart() {
		return this.shoppingCart;
	}

	public void setShoppingCart(ShoppingCart shoppingCart) {
		this.shoppingCart = shoppingCart;
	}

	private Set<Order> orders;

	@OneToMany(mappedBy = "customer", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	public Set<Order> getOrders() {
		return this.orders;
	}

	public void setOrders(Set<Order> orders) {
		this.orders = orders;
	}

	public String getAddress() {
		return this.address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

}
