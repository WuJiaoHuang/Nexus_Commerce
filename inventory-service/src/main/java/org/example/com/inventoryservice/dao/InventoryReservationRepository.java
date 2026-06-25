package org.example.com.inventoryservice.dao;

import org.example.com.inventoryservice.pojo.InventoryReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, String> {
}
