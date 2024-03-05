package ru.maliutin.manager.repository;

import ru.maliutin.manager.entity.Product;

import java.util.List;

public interface ProductRepository {
    List<Product> findAll();

    Product save(Product product);
}
