package ru.maliutin.manager.client;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import ru.maliutin.manager.controller.payload.NewProductPayload;
import ru.maliutin.manager.controller.payload.UpdateProductPayload;
import ru.maliutin.manager.entity.Product;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@RequiredArgsConstructor
public class RestClientProductsRestClient implements ProductsRestClient{
    /**
     * Константа для преобразования ответа в список продуктов.
     */
    private static final ParameterizedTypeReference<List<Product>> PRODUCTS_TYPE_REFERENCE =
            new ParameterizedTypeReference<>() {
    };
    /**
     * Объект Spring для доступа к удаленному api.
     */
    private final RestClient restClient;

    /**
     * Получение всех товаров.
     * @return список товаров.
     */
    @Override
    public List<Product> findAllProducts(String filter) {
        return this.restClient
                // Отправляем запрос на удаленный api
                .get()
                // По заданному uri
                .uri("/catalogue-api/products?filter={filter}", filter)
                // Получаем ответ
                .retrieve()
                // Преобразуем ответ к необходимому типу данных
                .body(PRODUCTS_TYPE_REFERENCE);
    }

    /**
     * Создание нового товара.
     * @param title название товара.
     * @param details описание товара.
     * @return
     */
    @Override
    public Product createProduct(String title, String details) {
        try {
            return this.restClient
                    // Отправляем post запрос к удаленному api
                    .post()
                    // По uri
                    .uri("/catalogue-api/products")
                    // С типом запроса
                    .contentType(MediaType.APPLICATION_JSON)
                    // В тело запроса помещаем объект с созд. товаром.
                    .body(new NewProductPayload(title, details))
                    // Получаем ответ
                    .retrieve()
                    // Преобразуем полученный ответ к нужному типу данных
                    .body(Product.class);
        }catch (HttpClientErrorException.BadRequest exception){
            // Если при валидации на удаленном api выброшено исключение и в ответе будет получен ProblemDetail
            ProblemDetail problemDetail = exception.getResponseBodyAs(ProblemDetail.class);
            // Получаем ProblemDetail и передаем список с ошибками в собственное исключение.
            throw new BadRequestException((List<String>) problemDetail.getProperties().get("errors"));
        }
    }

    /**
     * Получение товара по id.
     * @param productId идентификатор товара.
     * @return объект Optional с товаром или null.
     */
    @Override
    public Optional<Product> findProduct(int productId) {
        try{
            return Optional.ofNullable(
                    this.restClient
                            .get()
                            // формируем uri для запроса
                            .uri("/catalogue-api/products/{productId}", Map.of("productId", productId))
                            .retrieve()
                            .body(Product.class)
            );
        }catch (HttpClientErrorException.NotFound exception){
            // Если на удаленном api не будет найден товар и будет получен ответ 404.
            return Optional.empty();
        }
    }

    /**
     * Обновление товара.
     * @param productId идентификатор товара.
     * @param title наименование товара.
     * @param details описание товара.
     */
    @Override
    public void updateProduct(int productId, String title, String details) {
        try {
            this.restClient
                    // Отправляем put запрос к удаленному api
                    .patch()
                    // По uri
                    .uri("/catalogue-api/products/{productId}", Map.of("productId", productId))
                    // С типом запроса
                    .contentType(MediaType.APPLICATION_JSON)
                    // В тело запроса помещаем объект с созд. товаром.
                    .body(new UpdateProductPayload(title, details))
                    // Отправляем запрос
                    .retrieve()
                    // Если необходим доступ к ответу, если не нужен, то toBodilessEntity можно не вызывать.
                    .toBodilessEntity();
        }catch (HttpClientErrorException.BadRequest exception){
            // Если при валидации на удаленном api выброшено исключение и в ответе будет получен ProblemDetail
            ProblemDetail problemDetail = exception.getResponseBodyAs(ProblemDetail.class);
            // Получаем ProblemDetail и передаем список с ошибками в собственное исключение.
            throw new BadRequestException((List<String>) problemDetail.getProperties().get("errors"));
        }
    }

    /**
     * Удаление товара.
     * @param productId идентификатор товара.
     */
    @Override
    public void deleteProduct(int productId) {
        try{
            restClient
                    .delete()
                    .uri("/catalogue-api/products/{productId}", Map.of("productId", productId))
                    .retrieve()
                    .toBodilessEntity();
        }catch (HttpClientErrorException.NotFound exception){
            throw new NoSuchElementException(exception);
        }
    }
}
