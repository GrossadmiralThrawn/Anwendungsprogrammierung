package de.ufo.cinemasystem.repository;

import org.salespointframework.order.Order.OrderIdentifier;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import de.ufo.cinemasystem.models.Orders;

/**
 * CRUD-Repository for Orders
 * @author Simon Liepe
 */
@Repository
public interface OrdersRepository extends CrudRepository<Orders, OrderIdentifier>{
    
}
