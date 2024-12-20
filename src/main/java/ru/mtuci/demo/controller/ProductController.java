package ru.mtuci.demo.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.demo.model.Products;
import ru.mtuci.demo.services.ProductService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("products")
@RestController
@PreAuthorize("hasAnyRole('ADMIN')")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public List<Products> getAll() {
        return productService.getAll();
    }

    @GetMapping("/{id}")
    public Products getById(@PathVariable("id") Long id) {
        return productService.getProductById(id);
    }

    @GetMapping("/name/{name}")
    public Products getByName(@PathVariable("name") String name) {
        return productService.getByName(name);
    }

    @PostMapping("/add")
    public void add(@RequestBody Products product) {
        productService.add(product);
    }
}
