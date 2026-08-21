package ca.mcgill.ecse321.gallerysystem.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.OneToMany;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

@Entity
public class ShoppingCart {
	private Integer cartID;
	private Customer customer;

	private Set<SelectedItem> selectedItems = new HashSet<>();

	@OneToMany(mappedBy = "shoppingCart", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<SelectedItem> getSelectedItems() {
		return this.selectedItems;
	}

	public void setSelectedItems(Set<SelectedItem> selectedItems) {
		this.selectedItems = selectedItems;
	}

	@Transient
	public boolean isEmpty() {
		return selectedItems.isEmpty();
	}

	public void setCartID(Integer value) {
		this.cartID = value;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Integer getCartID() {
		return this.cartID;
	}

	@Transient
	public int getItemCount() {
		return this.selectedItems.size();
	}

	@OneToOne(optional = false)
	public Customer getCustomer() {
		return this.customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

}
