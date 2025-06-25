package org.yearup.data.mysql;

import org.apache.ibatis.jdbc.SQL;
import org.springframework.stereotype.Component;
import org.yearup.data.CategoryDao;
import org.yearup.models.Category;

import javax.sql.DataSource;
import java.sql.*;
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
        return new Category();
    }

    @Override
    public Category create(Category category)
    {
        // create a new category
        String sql = "INSERT INTO categories (name, description) VALUES (?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());
            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                category.setCategoryId(generatedKeys.getInt(1));
            }
            return category;
        } catch (SQLException e){
            e.printStackTrace();
        }
        return null;
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
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Delete Successful");
            } else {
                System.out.println("No category found with ID: " + categoryId);
            }
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
