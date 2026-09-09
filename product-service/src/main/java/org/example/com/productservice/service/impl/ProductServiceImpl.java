package org.example.com.productservice.service.impl;

import org.example.com.common.exception.BusinessException;
import org.example.com.common.exception.ErrorCode;
import org.example.com.productservice.cache.ProductCacheService;
import org.example.com.productservice.kafka.ProductEventProducer;
import org.example.com.productservice.mapper.ProductMapper;
import org.example.com.productservice.pojo.Product;
import org.example.com.productservice.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {


    private final ProductMapper productMapper;
    private final ProductEventProducer productEventProducer;
    private final ProductCacheService productCacheService;

    public ProductServiceImpl(ProductMapper productMapper,
                              ProductEventProducer productEventProducer,
                              ProductCacheService productCacheService) {
        this.productMapper = productMapper;
        this.productEventProducer = productEventProducer;
        this.productCacheService = productCacheService;
    }

    @Override
    public Product getProductById(String id) {
        if (id == null || id.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "product id is required");
        }
        return productCacheService.getProduct(id,
                () -> productMapper.selectById(id));
    }

    @Override
    public Product createProduct(Product product) {
       if (product.getStock() == null || product.getStock() < 0) {
           throw new BusinessException(ErrorCode.BAD_REQUEST, "stock must be greater than or equal to 0");
       }
        if (product.getPrice() == null || product.getPrice().signum() <= 0)  {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "price must not be null and greater than 0");
        }
        productMapper.insert(product);
        productCacheService.cacheProduct(product);
        productEventProducer.publishProductCreated(product);
        return product;
    }

    @Override
    public List<Product> getProductsByUserId(String userId) {
        return productCacheService.getProductsByUser(userId, () -> productMapper.selectList(null));
    }
}
