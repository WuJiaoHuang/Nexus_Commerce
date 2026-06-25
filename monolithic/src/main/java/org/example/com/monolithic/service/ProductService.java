package org.example.com.monolithic.service;

import org.example.com.monolithic.pojo.Product;

import java.util.List;

public interface ProductService {
    Product getProductById(String id);
    Product createProduct(Product product);
    List<Product> getProductsByUserId(String userId);
    List<Product> getProducts();
}
