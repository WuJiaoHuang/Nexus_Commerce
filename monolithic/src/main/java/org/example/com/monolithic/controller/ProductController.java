package org.example.com.monolithic.controller;

import org.example.com.monolithic.dao.UserRepo;
import org.example.com.monolithic.exception.UserNotFoundException;
import org.example.com.monolithic.pojo.Product;
import org.example.com.monolithic.pojo.User;
import org.example.com.monolithic.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {
    ProductService productService;
    UserRepo userRepo;

    @Autowired
    public ProductController(ProductService productService, UserRepo userRepo) {
        this.productService = productService;
        this.userRepo = userRepo;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable String id) {
        Product productById = productService.getProductById(id);
        return ResponseEntity.ok(productById);
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return ResponseEntity.ok(productService.createProduct(product));
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<Product>> getProducts(@PathVariable String id) {
        User user = userRepo.findByUserId(id);
        if (user == null) {
           throw new UserNotFoundException(id);
        }
        return ResponseEntity.ok(productService.getProductsByUserId(id));
    }
}
