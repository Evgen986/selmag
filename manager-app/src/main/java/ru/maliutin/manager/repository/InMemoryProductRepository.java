package ru.maliutin.manager.repository;

import org.springframework.stereotype.Repository;
import ru.maliutin.manager.entity.Product;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

@Repository
public class InMemoryProductRepository implements ProductRepository {

    private final List<Product> products =
            Collections.synchronizedList(new LinkedList<>());

    public InMemoryProductRepository() {
        IntStream.range(1, 4)
                .forEach(i -> this.products.add(new Product(i, "Товар №%d".formatted(i),
                        "Описание товара №%d".formatted(i))));
    }

    @Override
    public List<Product> findAll() {
        return Collections.unmodifiableList(this.products);
    }

    @Override
    public Product save(Product product) {
        product.setId(
                this.products
                        .stream()
                        .max(Comparator.comparing(Product::getId))
                        .map(Product::getId)
                        .orElse(0) + 1);
        products.add(product);
        return product;
    }
}
