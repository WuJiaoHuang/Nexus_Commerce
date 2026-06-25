package org.example.com.monolithic.impl;

import org.example.com.monolithic.dao.ProductRepo;
import org.example.com.monolithic.pojo.Product;
import org.example.com.monolithic.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    ProductRepo productRepo;

    @Autowired
    public ProductServiceImpl(ProductRepo productRepo) {
        this.productRepo = productRepo;
    }

    @Override
    public Product getProductById(String id) {
        return productRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
    }

    @Override
    public Product createProduct(Product product) {
       if (product.getStock() == null || product.getStock() < 0) {
           throw new IllegalArgumentException("stock must be greater than or equal to 0");
       }
        if (product.getPrice() == null || product.getPrice().signum() <= 0) {
            throw new IllegalArgumentException("price must not be null and greater than 0");
        }
       return productRepo.save(product);
    }

    @Override
    public List<Product> getProductsByUserId(String userId) {
        return productRepo.findAll();
    }

    @Override
    public List<Product> getProducts() {
        return productRepo.findAll();
    }
}
