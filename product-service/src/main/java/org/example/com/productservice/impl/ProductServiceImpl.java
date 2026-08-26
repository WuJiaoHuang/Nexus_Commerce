package org.example.com.productservice.impl;

import org.example.com.productservice.dao.ProductRepo;
import org.example.com.productservice.cache.ProductCacheService;
import org.example.com.productservice.kafka.ProductEventProducer;
import org.example.com.productservice.pojo.Product;
import org.example.com.productservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {


    ProductRepo productRepo;
    ProductEventProducer productEventProducer;
    ProductCacheService productCacheService;

    @Autowired
    public ProductServiceImpl(ProductRepo productRepo,
                              ProductEventProducer productEventProducer,
                              ProductCacheService productCacheService) {
        this.productRepo = productRepo;
        this.productEventProducer = productEventProducer;
        this.productCacheService = productCacheService;
    }

    @Override
    public Product getProductById(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("product id is required");
        }
        return productCacheService.getProduct(id,
                () -> productRepo.findById(id).orElse(null));
    }

    @Override
    public Product createProduct(Product product) {
       if (product.getStock() == null || product.getStock() < 0) {
           throw new IllegalArgumentException("stock must be greater than or equal to 0");
       }
        if (product.getPrice() == null || product.getPrice().signum() <= 0)  {
            throw new IllegalArgumentException("price must not be null and greater than 0");
        }
        Product savedProduct = productRepo.save(product);
        productCacheService.cacheProduct(savedProduct);
        productEventProducer.publishProductCreated(savedProduct);
        return savedProduct;
    }

    @Override
    public List<Product> getProductsByUserId(String userId) {
        return productCacheService.getProductsByUser(userId, productRepo::findAll);
    }
}
