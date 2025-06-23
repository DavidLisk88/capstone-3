package org.yearup.data;

import org.yearup.models.Category;

import java.util.List;

public interface CategoryDao
{
    List<Category> getAllCategories(Integer categoryId, String name);
    Category getById(int id);
    Category create(Category category, Integer categoryId);
    void updateCategory(int categoryId, Category category);
    void deleteCategory(int categoryId);
}
