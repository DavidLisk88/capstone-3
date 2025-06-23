package org.yearup.data;

import org.yearup.models.Product;

import java.math.BigDecimal;
import java.util.List;

public interface ProductDao
{
    List<Product> search(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String color);
    List<Product> listByCategoryId(Integer categoryId);
    Product getById(int productId);
    Product createProduct(Product product);
    void updateProduct(int productId, Product product);
    void delete(int productId);
}
