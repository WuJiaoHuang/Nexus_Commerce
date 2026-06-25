package org.example.com.productservice.controller;


import org.example.com.productservice.pojo.Product;
import org.example.com.productservice.pojo.User;
import org.example.com.productservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {
    ProductService productService;
    @Autowired
    RestTemplate restTemplate;
    //UserRepo userRepo;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
        //this.userRepo = userRepo;
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
        User user = restTemplate.getForObject("http://USER-SERVICE/user/{id}", User.class, id);
        System.out.println(user);

        /*User user = userRepo.findByUserId(id);
        if (user == null) {
           throw new UserNotFoundException(id);
        }*/
        return ResponseEntity.ok(productService.getProductsByUserId(id));
    }
}
