package ru.maliutin.catalogueservice.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.maliutin.catalogueservice.entity.Product;

@Repository
public interface ProductRepository extends CrudRepository<Product, Integer> {

    Iterable<Product> findAllByTitleLikeIgnoreCaseOrderByTitle(String filter);

}
