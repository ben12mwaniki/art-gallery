package ca.mcgill.ecse321.gallerysystem.dao;

import java.util.Date;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import ca.mcgill.ecse321.gallerysystem.model.Order;

@RepositoryRestResource(collectionResourceRel = "order_data", path = "order_data")

public interface OrderRepository extends CrudRepository<Order, Integer> {

	Order findOrderByOrderNumber(Integer orderNumber);

	Order findCustomerByCustomerEmail(String email);

	int countByOrderDateBetween(Date startInclusive, Date endExclusive);

	int countByOrderNumberBetween(Integer from, Integer to);

}
