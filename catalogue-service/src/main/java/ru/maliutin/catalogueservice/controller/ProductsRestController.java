package ru.maliutin.catalogueservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import ru.maliutin.catalogueservice.controller.payload.NewProductPayload;
import ru.maliutin.catalogueservice.entity.Product;
import ru.maliutin.catalogueservice.service.ProductService;

import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("catalogue-api/products")
public class ProductsRestController {
    /**
     * Сервис товаров.
     */
    private final ProductService productService;

    /**
     * Получение все товаров.
     * @return список товаров.
     */
    @GetMapping
    public Iterable<Product> findProducts(@RequestParam(name = "filter", required = false) String filter){
        return productService.findAllProducts(filter);
    }

    /**
     * Добавление нового товара.
     * @param payload объект с новым товаром.
     * @param bindingResult объект с ошибками валидации.
     * @param uriComponentsBuilder объект построения URI для ответа.
     * @return ответ с URI для перехода к созданному объекту.
     * @throws BindException исключения валидации с ошибками.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createProduct(@Valid @RequestBody NewProductPayload payload,
                                                 BindingResult bindingResult,
                                                 UriComponentsBuilder uriComponentsBuilder)
            throws BindException{
        if (bindingResult.hasErrors()){
            if (bindingResult instanceof BindException exception){
                throw exception;
            }else {
                throw new BindException(bindingResult);
            }
        }else{
            Product product = productService.createProduct(
                    payload.title(), payload.details());
            return ResponseEntity
                    .created(uriComponentsBuilder
                            .replacePath("/catalogue-api/products/{productId}")
                            .build(Map.of("productId", product.getId())))
                    .body(product);
        }
    }

}
