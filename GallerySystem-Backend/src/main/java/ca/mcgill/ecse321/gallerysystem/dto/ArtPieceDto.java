package ca.mcgill.ecse321.gallerysystem.dto;

import ca.mcgill.ecse321.gallerysystem.model.Artist;

public class ArtPieceDto {

	private Integer quantity;
	private float price;
	private Integer discountPercentage;
	private Float commissionPercentage;
	private String artName;
	private String description;
	private Integer artID;
	private String artpieceArtist;

	public ArtPieceDto() {
	}

	public ArtPieceDto(String artName) {
		this.artName = artName;
		this.quantity = 1;
	}

	public ArtPieceDto(String artName, Integer quantity, float price, Integer discountPercentage,
			Float commissionPercentage, String description, Integer artID, Artist artist) {
		this.quantity = quantity;
		this.price = price;
		this.discountPercentage = discountPercentage;
		this.commissionPercentage = commissionPercentage;
		this.artName = artName;
		this.description = description;
		this.artID = artID;
		this.artpieceArtist = artist.getUserName();
	}

	public Integer getQuantity() {
		return this.quantity;
	}

	public float getPrice() {
		return this.price;
	}

	public Integer getDiscountPercentage() {
		return this.discountPercentage;
	}

	public Float getCommissionPercentage() {
		return this.commissionPercentage;
	}

	public String getArtPieceName() {
		return this.artName;
	}

	public String getArtPieceDescription() {
		return this.description;
	}

	public Integer getArtID() {
		return this.artID;
	}

	public String getArtPieceArtist() {
		return this.artpieceArtist;
	}

}
