package ca.mcgill.ecse321.gallerysystem.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
public class ArtPiece {

	private Integer quantity;
	private float price;
	private String artName;
	private Integer discountPercentage;
	private String description;
	private float commissionPercentage;
	private boolean active = true;

	private Integer artID;
	private Artist artist;

	public void setQuantity(Integer value) {
		this.quantity = value;
	}

	public Integer getQuantity() {
		return this.quantity;
	}

	public void setPrice(float value) {
		this.price = value;
	}

	public float getPrice() {
		return this.price;
	}

	public void setCommissionPercentage(float value) {
		this.commissionPercentage = value;
	}

	public float getCommissionPercentage() {
		return this.commissionPercentage;
	}

	public void setDiscountPercentage(Integer value) {
		this.discountPercentage = value;
	}

	public Integer getDiscountPercentage() {
		return this.discountPercentage;
	}

	public void setArtName(String value) {
		this.artName = value;
	}

	public String getArtName() {
		return this.artName;
	}

	public void setDescription(String value) {
		this.description = value;
	}

	public String getDescription() {
		return this.description;
	}

	public void setActive(boolean value) {
		this.active = value;
	}

	public boolean isActive() {
		return this.active;
	}

	public void setArtID(Integer value) {
		this.artID = value;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Integer getArtID() {
		return this.artID;
	}

	@ManyToOne(optional = false)
	public Artist getArtist() {
		return this.artist;
	}

	@Transient
	public String getArtpieceArtist() {
		return this.artist.getUserName();
	}

	public void setArtist(Artist artist) {
		this.artist = artist;
	}
}