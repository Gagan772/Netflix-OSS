package com.netflixoss.stock.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.netflixoss.stock.dto.ProductResponse;
import com.netflixoss.stock.dto.ReserveStockRequest;
import com.netflixoss.stock.dto.ReserveStockResponse;
import com.netflixoss.stock.entity.ProductEntity;
import com.netflixoss.stock.exception.ResourceNotFoundException;
import com.netflixoss.stock.repository.ProductRepository;

@Service
public class ProductStockService {

    private final ProductRepository productRepository;

    public ProductStockService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ProductResponse getProductBySku(String sku) {
        ProductEntity product = getBySkuOrThrow(sku);
        return mapToResponse(product);
    }

    @Transactional
    public ReserveStockResponse reserveStock(ReserveStockRequest request) {
        ProductEntity product = getBySkuOrThrow(request.getSku());

        if (product.getAvailableQuantity() < request.getQuantity()) {
            return new ReserveStockResponse(false, product.getAvailableQuantity(), "Insufficient stock");
        }

        product.setAvailableQuantity(product.getAvailableQuantity() - request.getQuantity());
        productRepository.save(product);
        return new ReserveStockResponse(true, product.getAvailableQuantity(), "Stock reserved");
    }

    @Transactional
    public int restockProduct(String sku, int quantity) {
        ProductEntity product = getBySkuOrThrow(sku);
        product.setAvailableQuantity(product.getAvailableQuantity() + quantity);
        productRepository.save(product);
        return product.getAvailableQuantity();
    }

    public int getStockLevel(String sku) {
        return getBySkuOrThrow(sku).getAvailableQuantity();
    }

    private ProductEntity getBySkuOrThrow(String sku) {
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found for sku: " + sku));
    }

    private ProductResponse mapToResponse(ProductEntity product) {
        return new ProductResponse(product.getSku(), product.getName(), product.getAvailableQuantity(), product.getPrice());
    }
}
