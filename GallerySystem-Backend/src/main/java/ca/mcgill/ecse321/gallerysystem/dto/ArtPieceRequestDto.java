package ca.mcgill.ecse321.gallerysystem.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

public class ArtPieceRequestDto {

    @NotBlank(message = "Art name is required")
    private String artName;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be at least 0")
    private Integer quantity;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Float price;

    @NotNull(message = "Discount percentage is required")
    @Min(value = 0, message = "Discount percentage cannot be negative")
    private Integer discountPercentage;

    @NotNull(message = "Commission percentage is required")
    @Min(value = 0, message = "Commission percentage cannot be negative")
    private Float commissionPercentage;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Artist email is required")
    @Email(message = "Artist email should be valid")
    private String artistEmail;

    public ArtPieceRequestDto() {
    }

    // Getters and Setters
    public String getArtName() {
        return artName;
    }

    public void setArtName(String artName) {
        this.artName = artName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
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

    public Float getCommissionPercentage() {
        return commissionPercentage;
    }

    public void setCommissionPercentage(Float commissionPercentage) {
        this.commissionPercentage = commissionPercentage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getArtistEmail() {
        return artistEmail;
    }

    public void setArtistEmail(String artistEmail) {
        this.artistEmail = artistEmail;
    }
}