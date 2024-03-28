package ru.maliutin.catalogueservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import ru.maliutin.catalogueservice.controller.payload.UpdateProductPayload;
import ru.maliutin.catalogueservice.entity.Product;
import ru.maliutin.catalogueservice.service.ProductService;

import java.util.Locale;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("catalogue-api/products/{productId:\\d+}")
public class ProductRestController {
    /**
     * Сервис продуктов.
     */
    private final ProductService productService;
    /**
     * Объект для отправки сообщений с учетом локализации.
     */
    private final MessageSource messageSource;

    /**
     * Получение товара по уникальному идентификатору,
     * данный метод будет выполнен, каждый раз,
     * когда в методах класса будет вызвана аннотация @ModelAttribute()
     * @param productId идентификатор товара.
     * @return объект товара.
     */
    @ModelAttribute
    public Product getProduct(@PathVariable("productId") int productId){
        return this.productService
                .findProduct(productId)
                .orElseThrow(() -> new NoSuchElementException("catalog.errors.product.not_found"));
    }

    /**
     * Получение товара.
     * @param product объект товара, будет получен из метода getProduct, благодаря
     *                аннотации @ModelAttribute
     * @return найденный товар.
     */
    @GetMapping
    public Product findProduct(@ModelAttribute("product") Product product){
        return product;
    }

    /**
     * Обновление товара.
     * @param productId идентификатор товара.
     * @param payload объект с обновленными данными.
     * @param bindingResult объект с ошибками валидации обновления товара.
     * @return ответ с подтверждением.
     * @throws BindException исключение валидации с ошибками.
     */
    @PatchMapping
    public ResponseEntity<?> updateProduct(@PathVariable("productId") int productId,
                                              @Valid @RequestBody UpdateProductPayload payload,
                                              BindingResult bindingResult)
            throws BindException{
        if (bindingResult.hasErrors()){
            if (bindingResult instanceof BindException exception){
                throw exception;
            }else {
                throw new BindException(bindingResult);
            }
        }else{
            productService.updateProduct(productId, payload.title(), payload.details());
            return ResponseEntity.noContent().build();
        }
    }

    /**
     * Удаление товара.
     * @param productId идентификатор товара.
     * @return ответ с подтверждением.
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteProduct(@PathVariable("productId") int productId){
        this.productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Обработка запроса не существующего товара.
     * @param exception исключение.
     * @param locale локализация.
     * @return ответ с детализацией проблемы.
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ProblemDetail> handleNoSuchElementException(NoSuchElementException exception, Locale locale){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                this.messageSource.getMessage(exception.getMessage(),
                        new Object[0], exception.getMessage(), locale));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }
}
