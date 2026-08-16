package ca.mcgill.ecse321.gallerysystem.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class SelectedItem {

	private Integer itemID;
	private Integer quantity;
	private ArtPiece artPiece;
	private ShoppingCart shoppingCart;

	public void setItemID(Integer value) {
		this.itemID = value;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Integer getItemID() {
		return this.itemID;
	}

	@ManyToOne(optional = false)
	public ShoppingCart getShoppingCart() {
		return this.shoppingCart;
	}

	public void setShoppingCart(ShoppingCart shoppingCart) {
		this.shoppingCart = shoppingCart;
	}

	public void setItemQuantity(Integer value) {
		this.quantity = value;
	}

	public Integer getItemQuantity() {
		return this.quantity;
	}

	@ManyToOne(optional = false)
	public ArtPiece getArtPiece() {
		return this.artPiece;
	}

	public void setArtPiece(ArtPiece artPiece) {
		this.artPiece = artPiece;
	}
}