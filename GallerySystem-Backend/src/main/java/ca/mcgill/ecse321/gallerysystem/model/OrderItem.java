package ca.mcgill.ecse321.gallerysystem.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class OrderItem {

    private Integer orderItemID;
    private Integer quantity;

    private float listPrice;
    private float unitPrice;

    private Integer discountPercentage;
    private float commissionPercentage;

    private String artName;
    private String description;

    private Order order;
    private ArtPiece artPiece;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Integer getOrderItemID() {
        return this.orderItemID;
    }

    public void setOrderItemID(Integer orderItemID) {
        this.orderItemID = orderItemID;
    }

    @ManyToOne(optional = false)
    public Order getOrder() {
        return this.order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    @ManyToOne(optional = false)
    public ArtPiece getArtPiece() {
        return this.artPiece;
    }

    public void setArtPiece(ArtPiece artPiece) {
        this.artPiece = artPiece;
    }

    public Integer getQuantity() {
        return this.quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public float getListPrice() {
        return this.listPrice;
    }

    public void setListPrice(float listPrice) {
        this.listPrice = listPrice;
    }

    public float getUnitPrice() {
        return this.unitPrice;
    }

    public void setUnitPrice(float unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getDiscountPercentage() {
        return this.discountPercentage;
    }

    public void setDiscountPercentage(Integer discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public float getCommissionPercentage() {
        return this.commissionPercentage;
    }

    public void setCommissionPercentage(float commissionPercentage) {
        this.commissionPercentage = commissionPercentage;
    }

    public String getArtName() {
        return this.artName;
    }

    public void setArtName(String artName) {
        this.artName = artName;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}