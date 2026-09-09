package org.example.com.productservice.controller;


import org.example.com.common.result.Result;
import org.example.com.productservice.pojo.Product;
import org.example.com.productservice.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{id}")
    public Result<Product> getProduct(@PathVariable String id) {
        Product productById = productService.getProductById(id);
        return Result.success(productById);
    }

    @PostMapping
    public ResponseEntity<Result<Product>> createProduct(@RequestBody Product product) {
        Product savedProduct = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.success(HttpStatus.CREATED.value(), "created", savedProduct));
    }

    @GetMapping("/user/{id}")
    public Result<List<Product>> getProducts(@PathVariable String id) {
        return Result.success(productService.getProductsByUserId(id));
    }
}
