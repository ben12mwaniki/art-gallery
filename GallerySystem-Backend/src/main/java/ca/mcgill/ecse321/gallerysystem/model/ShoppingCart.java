package ca.mcgill.ecse321.gallerysystem.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.OneToMany;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class ShoppingCart {
	private Integer itemNumber;
	private Integer cartID;
	private boolean isEmpty;
	private Customer customer;

	private Set<SelectedItem> selectedItem = new HashSet<>();

	@OneToMany(mappedBy = "shoppingCart", fetch = FetchType.EAGER, orphanRemoval = true)
	public Set<SelectedItem> getSelectedItem() {
		return this.selectedItem;
	}

	public void setSelectedItem(Set<SelectedItem> selectedItems) {
		this.selectedItem = selectedItems;
	}

	public void setIsEmpty(boolean value) {
		this.isEmpty = value;
	}

	public boolean isIsEmpty() {
		return this.isEmpty;
	}

	public void setCartID(Integer value) {
		this.cartID = value;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Integer getCartID() {
		return this.cartID;
	}

	public void setItemNumber(Integer value) {
		this.itemNumber = value;
	}

	public Integer getItemNumber() {
		return this.itemNumber;
	}

	@OneToOne(optional = false)
	public Customer getCustomer() {
		return this.customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

}
