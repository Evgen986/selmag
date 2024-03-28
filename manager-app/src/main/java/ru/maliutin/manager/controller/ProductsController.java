package ru.maliutin.manager.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.maliutin.manager.client.BadRequestException;
import ru.maliutin.manager.client.ProductsRestClient;
import ru.maliutin.manager.controller.payload.NewProductPayload;
import ru.maliutin.manager.entity.Product;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("catalogue/products")
public class ProductsController {

    private final ProductsRestClient productsRestClient;

    @GetMapping("list")
    public String getProductsList(Model model, @RequestParam(name = "filter", required = false) String filter){
        model.addAttribute("products", this.productsRestClient.findAllProducts(filter));
        model.addAttribute("filter", filter);
        return "catalogue/products/list";
    }

    @GetMapping("create")
    public String getNewProductPage(){
        return "catalogue/products/new_product";
    }

    @PostMapping("create")
    public String createProduct(NewProductPayload payload, Model model){
        try {
            Product product = this.productsRestClient.createProduct(payload.title(), payload.details());
            return "redirect:/catalogue/products/%d".formatted(product.id());
        }catch (BadRequestException exception){
            model.addAttribute("payload", payload);
            model.addAttribute("errors",
                    exception.getErrors());
            return "catalogue/products/new_product";
        }
    }

}
