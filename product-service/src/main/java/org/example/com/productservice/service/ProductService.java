package org.example.com.productservice.service;


import org.example.com.productservice.pojo.Product;

import java.util.List;

public interface ProductService {
    Product getProductById(String id);
    Product createProduct(Product product);
    List<Product> getProductsByUserId(String userId);
}
