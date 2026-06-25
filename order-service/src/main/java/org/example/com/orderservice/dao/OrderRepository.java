package org.example.com.orderservice.dao;

import org.example.com.orderservice.pojo.OrderRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<OrderRecord, String> {
}
