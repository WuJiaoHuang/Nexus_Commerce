package org.example.com.productservice.dao;

import org.example.com.productservice.pojo.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepo extends JpaRepository<Product, String> {
}
