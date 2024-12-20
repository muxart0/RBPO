package ru.mtuci.demo.services;

import ru.mtuci.demo.model.Products;


import java.util.List;

public interface ProductService {
    List<Products> getAll();
    void add(Products products);
    Products getByName(String name);
    Products getProductById(Long id);
}

