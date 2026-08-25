package ca.mcgill.ecse321.gallerysystem.dto;

public class OrderItemDto {

    private Integer orderItemID;
    private Integer artPieceID;
    private Integer quantity;
    private float listPrice;
    private float unitPrice;
    private Integer discountPercentage;
    private float commissionPercentage;
    private String artName;
    private String description;

    public OrderItemDto() {
    }

    public OrderItemDto(Integer orderItemID, Integer artPieceID,
            Integer quantity, float listPrice, float unitPrice,
            Integer discountPercentage, float commissionPercentage,
            String artName, String description) {

        this.orderItemID = orderItemID;
        this.artPieceID = artPieceID;
        this.quantity = quantity;
        this.listPrice = listPrice;
        this.unitPrice = unitPrice;
        this.discountPercentage = discountPercentage;
        this.commissionPercentage = commissionPercentage;
        this.artName = artName;
        this.description = description;
    }

    public Integer getOrderItemID() {
        return orderItemID;
    }

    public Integer getArtPieceID() {
        return artPieceID;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public float getListPrice() {
        return listPrice;
    }

    public float getUnitPrice() {
        return unitPrice;
    }

    public Integer getDiscountPercentage() {
        return discountPercentage;
    }

    public float getCommissionPercentage() {
        return commissionPercentage;
    }

    public String getArtName() {
        return artName;
    }

    public String getDescription() {
        return description;
    }
}