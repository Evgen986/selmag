package ru.maliutin.manager.service;

import ru.maliutin.manager.entity.Product;

import java.util.List;

public interface ProductService {
    List<Product> findAllProducts();

    Product createProduct(String title, String details);
}
