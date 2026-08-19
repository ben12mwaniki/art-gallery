package ca.mcgill.ecse321.gallerysystem.dao;

import java.util.Set;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import ca.mcgill.ecse321.gallerysystem.model.ArtPiece;
import ca.mcgill.ecse321.gallerysystem.model.Order;
import ca.mcgill.ecse321.gallerysystem.model.OrderItem;

@RepositoryRestResource()
public interface OrderItemRepository extends CrudRepository<OrderItem, Integer> {
    OrderItem findOrderItemByOrderItemID(Integer orderItemID);

    Set<OrderItem> findByOrder(Order order);

    Set<OrderItem> findByArtPiece(ArtPiece art);

}
