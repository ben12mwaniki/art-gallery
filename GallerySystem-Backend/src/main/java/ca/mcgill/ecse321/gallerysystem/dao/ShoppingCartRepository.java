package ca.mcgill.ecse321.gallerysystem.dao;

import org.springframework.data.repository.CrudRepository;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import ca.mcgill.ecse321.gallerysystem.model.ShoppingCart;

@RepositoryRestResource(collectionResourceRel = "shoppingCart_data", path = "shoppingCart_data")
public interface ShoppingCartRepository extends CrudRepository<ShoppingCart, Integer> {

	ShoppingCart findShoppingCartByCartID(Integer cartID);

	ShoppingCart findShoppingCartByCustomerEmail(String email);

}
