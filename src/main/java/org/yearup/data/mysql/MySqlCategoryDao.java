package org.yearup.data.mysql;

import org.apache.ibatis.jdbc.SQL;
import org.springframework.stereotype.Component;
import org.yearup.data.CategoryDao;
import org.yearup.models.Category;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlCategoryDao extends MySqlDaoBase implements CategoryDao
{
    private final Connection connection;

    public MySqlCategoryDao(DataSource dataSource) throws SQLException
    {
        super(dataSource);
        this.connection = dataSource.getConnection();
    }

    @Override
    public List<Category> getAllCategories(Integer categoryId, String name)
    {
        List<Category> categories = new ArrayList<>();
        // get all categories
        String sql = "SELECT category_id, name, description FROM categories";

        try(PreparedStatement statement = connection.prepareStatement(sql)){
            ResultSet results = statement.executeQuery();
            while(results.next()){
                Category category = new Category();
                category.setCategoryId(results.getInt("category_id"));
                category.setName(results.getString("name"));
                category.setDescription(results.getString("description"));
                categories.add(category);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return categories;
    }


    @Override
    public Category getById(int id)
    {
        String sql = "SELECT * FROM categories WHERE category_id = ?";

        try(PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setInt(1, id);
            ResultSet results = statement.executeQuery();
            if (results.next()){
                Category category = new Category();
                category.setCategoryId(results.getInt("category_id"));
                category.setName(results.getString("name"));
                category.setDescription(results.getString("description"));
                return category;
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Category create(Category category, Integer categoryId)
    {
        // create a new category
        String sql = "INSERT INTO categories (category_id, name, description) VALUES (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setInt(1, categoryId);
            statement.setString(2, category.getName());
            statement.setString(3, category.getDescription());

            try (ResultSet results = statement.executeQuery()){
                return mapRow(results);
            }

        } catch (SQLException e){
            e.printStackTrace();
        }

        return category;
    }

    @Override
    public void updateCategory(int categoryId, Category category)
    {
        // update category
        String sql = "UPDATE categories SET category_id = ?, name = ?, description = ? WHERE category_id = ?";

        try(PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setInt(1, category.getCategoryId());
            statement.setString(2, category.getName());
            statement.setString(3, category.getDescription());
            statement.setInt(4, categoryId);
            statement.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public void deleteCategory(int categoryId)
    {
        // delete category
        String sql = "DELETE FROM categories WHERE category_id = ?";
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setInt(1, categoryId);
            statement.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    private Category mapRow(ResultSet row) throws SQLException
    {
        int categoryId = row.getInt("category_id");
        String name = row.getString("name");
        String description = row.getString("description");

        Category category = new Category()
        {{
            setCategoryId(categoryId);
            setName(name);
            setDescription(description);
        }};

        return category;
    }


}
