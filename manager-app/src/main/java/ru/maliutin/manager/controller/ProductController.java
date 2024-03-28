package ru.maliutin.manager.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.maliutin.manager.client.BadRequestException;
import ru.maliutin.manager.client.ProductsRestClient;
import ru.maliutin.manager.controller.payload.UpdateProductPayload;
import ru.maliutin.manager.entity.Product;


import java.util.Locale;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("catalogue/products/{productId:\\d+}")
@RequiredArgsConstructor
public class ProductController {

    private final ProductsRestClient productsRestClient;
    /**
     * Объект для локализации сообщений.
     */
    private final MessageSource messageSource;

    @ModelAttribute("product")
    public Product product(@PathVariable("productId") Integer productId){
        return this.productsRestClient.findProduct(productId)
                // Если продукт не найден генерируем исключение в сообщение исключению передаем данные по ключу из resources/messages.properties
                .orElseThrow(() -> new NoSuchElementException("catalog.errors.product.not_found"));
    }

    @GetMapping
    public String getProduct(){
        return "catalogue/products/product";
    }

    @GetMapping("edit")
    public String getProductEditPage(){
        return "catalogue/products/edit";
    }

    @PostMapping("edit")
    public String updateProduct(@ModelAttribute(value = "product", binding = false) Product product,
                                UpdateProductPayload payload,
                                Model model){
        try {
            this.productsRestClient.updateProduct(product.id(), payload.title(), payload.details());
            return "redirect:/catalogue/products/%d".formatted(product.id());
        }catch (BadRequestException exception){
            model.addAttribute("payload", payload);
            model.addAttribute("errors",
                    exception.getErrors());
            return "catalogue/products/edit";
        }

    }

    @PostMapping("delete")
    public String deleteProduct(@ModelAttribute("product") Product product){
        this.productsRestClient.deleteProduct(product.id());
        return "redirect:/catalogue/products/list";
    }

    @ExceptionHandler(NoSuchElementException.class)
    public String handleNoSuchElement(NoSuchElementException e, Model model,
                                      HttpServletResponse response, Locale locale){
        response.setStatus(HttpStatus.NOT_FOUND.value());
        // В модель передаем данные об исключении - имя атрибута, объект локализации, у которого вызываем метод getMessage(сообщение, доп.аргументы, сообщение по умолчанию, локализация)
        model.addAttribute("error", this.messageSource.getMessage(e.getMessage(), new Object[0], e.getMessage(), locale));
        return "errors/404";
    }
}
