package ru.mtuci.demo.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mtuci.demo.exception.ProductException;
import ru.mtuci.demo.exception.UserException;
import ru.mtuci.demo.model.Products;
import ru.mtuci.demo.repo.ProductRepository;
import ru.mtuci.demo.services.ProductService;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public Products getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductException("Product not found"));
    }

    @Override
    public List<Products> getAll() {
        return productRepository.findAll();
    }

    public void add(Products product) {
        if (productRepository.existsByName(product.getName())) {
            throw new IllegalArgumentException("Product with the same name already exists.");
        }
        productRepository.save(product);
    }


    @Override
    public Products getByName(String name)  {
        return productRepository.findByName(name)
                .orElseThrow(() -> new UserException("User not found"));
    }
}
