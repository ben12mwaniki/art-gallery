package ca.mcgill.ecse321.gallerysystem.dto;

import ca.mcgill.ecse321.gallerysystem.model.ArtPiece;

public class SelectedItemDto {

	private Integer itemID;
	private Integer itemQuantity;
	private Integer artID;
	private String artName;
	private Float price;
	private Integer discountPercentage;
	private String description;

	// Default constructor for serialization
	public SelectedItemDto() {
	}

	// Constructor matching what your controller currently uses
	public SelectedItemDto(Integer itemID, Integer itemQuantity, ArtPiece artPiece) {
		this.itemID = itemID;
		this.itemQuantity = itemQuantity;
		if (artPiece != null) {
			this.artID = artPiece.getArtID();
			this.artName = artPiece.getArtName();
			this.price = artPiece.getPrice();
			this.discountPercentage = artPiece.getDiscountPercentage();
			this.description = artPiece.getDescription();
		}
	}

	// Alternative constructor if you want to build from fields directly
	public SelectedItemDto(Integer itemID, Integer itemQuantity,
			Integer artID, String artName, Float price,
			Integer discountPercentage, String description) {
		this.itemID = itemID;
		this.itemQuantity = itemQuantity;
		this.artID = artID;
		this.artName = artName;
		this.price = price;
		this.discountPercentage = discountPercentage;
		this.description = description;
	}

	// Getters and Setters
	public Integer getItemID() {
		return itemID;
	}

	public void setItemID(Integer itemID) {
		this.itemID = itemID;
	}

	public Integer getItemQuantity() {
		return itemQuantity;
	}

	public void setItemQuantity(Integer itemQuantity) {
		this.itemQuantity = itemQuantity;
	}

	public Integer getArtID() {
		return artID;
	}

	public void setArtID(Integer artID) {
		this.artID = artID;
	}

	public String getArtName() {
		return artName;
	}

	public void setArtName(String artName) {
		this.artName = artName;
	}

	public Float getPrice() {
		return price;
	}

	public void setPrice(Float price) {
		this.price = price;
	}

	public Integer getDiscountPercentage() {
		return discountPercentage;
	}

	public void setDiscountPercentage(Integer discountPercentage) {
		this.discountPercentage = discountPercentage;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "SelectedItemDto{" +
				"itemID=" + itemID +
				", itemQuantity=" + itemQuantity +
				", artID=" + artID +
				", artName='" + artName + '\'' +
				", price=" + price +
				", discountPercentage=" + discountPercentage +
				'}';
	}
}