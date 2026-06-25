package org.example.com.inventoryservice.dao;

import org.example.com.inventoryservice.pojo.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryItem, String> {
}
