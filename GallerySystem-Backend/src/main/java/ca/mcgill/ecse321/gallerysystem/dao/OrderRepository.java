package ca.mcgill.ecse321.gallerysystem.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import ca.mcgill.ecse321.gallerysystem.model.Order;

@RepositoryRestResource(exported = false)

public interface OrderRepository extends CrudRepository<Order, Integer> {

	Order findOrderByOrderNumber(Integer orderNumber);

	List<Order> findByCustomerEmailOrderByOrderDateDesc(String email);

	int countByOrderDateBetween(Date startInclusive, Date endExclusive);

	int countByOrderNumberBetween(Integer from, Integer to);

}
