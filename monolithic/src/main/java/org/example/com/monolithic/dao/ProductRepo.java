package org.example.com.monolithic.dao;


import org.example.com.monolithic.pojo.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepo extends JpaRepository<Product, String> {
}
