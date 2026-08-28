package ca.mcgill.ecse321.gallerysystem.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

public class SelectedItemRequestDto {

    @NotNull(message = "Art ID is required")
    @Positive(message = "Art ID must be positive")
    private Integer artID;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    public SelectedItemRequestDto() {
    }

    public SelectedItemRequestDto(Integer artID, Integer quantity) {
        this.artID = artID;
        this.quantity = quantity;
    }

    // Getters and Setters
    public Integer getArtID() {
        return artID;
    }

    public void setArtID(Integer artID) {
        this.artID = artID;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}