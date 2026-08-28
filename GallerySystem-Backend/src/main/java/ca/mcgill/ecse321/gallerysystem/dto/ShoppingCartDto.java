package ca.mcgill.ecse321.gallerysystem.dto;

public class ShoppingCartDto {

	private Integer itemCount;
	private Integer cartID;
	private boolean isEmpty;

	public ShoppingCartDto(Integer itemCount, Integer cartID) {
		this.itemCount = itemCount;
		this.cartID = cartID;
		this.isEmpty = itemCount == 0;
	}

	public Integer getItemCount() {
		return itemCount;
	}

	public Integer getCartID() {
		return cartID;
	}

	public boolean getIsEmpty() {
		return isEmpty;
	}

}
