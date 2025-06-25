package org.yearup.data.mysql;

import org.springframework.security.core.parameters.P;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao {

    private final Connection connection;
    private ProductDao productDao;

    public MySqlShoppingCartDao (DataSource config) throws SQLException {
        super(config);
        this.connection = config.getConnection();
    }

    @Override
    public ShoppingCart getCart (int userId, Principal principal){
       ShoppingCart cartProducts = new ShoppingCart();

        String sql = "SELECT * FROM shopping_cart WHERE user_id = ? ";
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setInt(1, userId);
            ResultSet results = statement.executeQuery();
            while(results.next()){
                int productId = results.getInt("product_id");
                int quantity = results.getInt("quantity");

                Product product = productDao.getById(productId);
                if (product != null){
                    ShoppingCartItem item = new ShoppingCartItem();
                    item.setProduct(product);
                    item.setQuantity(quantity);
                    cartProducts.add(item);
                }
            }

        } catch (SQLException e){
            e.printStackTrace();
        }
        return cartProducts;
    }

    @Override
    public ShoppingCart getByUserId(int userId) {
        ShoppingCart cart = new ShoppingCart();
        Map<Integer, ShoppingCartItem> items = new HashMap<>();
        BigDecimal total = BigDecimal.ZERO;

        String sql = "SELECT p.*, sc.quantity " +
                "FROM shopping_cart sc " +
                "JOIN products p ON sc.product_id = p.product_id " +
                "WHERE sc.user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            ResultSet results = statement.executeQuery();

            while (results.next()) {
                Product product = new Product();
                product.setProductId(results.getInt("product_id"));
                product.setName(results.getString("name"));
                product.setPrice(results.getBigDecimal("price"));
                product.setDescription(results.getString("description"));
                product.setColor(results.getString("color"));
                product.setStock(results.getInt("stock"));
                product.setImageUrl(results.getString("image_url"));
                product.setFeatured(results.getBoolean("featured"));

                int quantity = results.getInt("quantity");
                BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));

                ShoppingCartItem item = new ShoppingCartItem();
                item.setProduct(product);
                item.setQuantity(quantity);
                item.setLineTotal(lineTotal);

                items.put(product.getProductId(), item);
                total = total.add(lineTotal);
            }
            cart.setItems(items);
            cart.setTotal(total);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    return cart;
    }

    @Override
    public void addItem (int userId, int productId){
        String selectQuery = "SELECT quantity FROM shopping_cart WHERE user_id = ? AND product_id = ?";
        String insertQuery = "INSERT INTO shopping_cart (user_id, product_id, quantity) VALUES (?, ?, 1)";
        String updateQuery = "UPDATE shopping_cart SET quantity = quantity + 1 WHERE user_id = ? AND product_id = ?";

        try(PreparedStatement selectStatement = connection.prepareStatement(selectQuery)){
            selectStatement.setInt(1, userId);
            selectStatement.setInt(2, productId);
            ResultSet results = selectStatement.executeQuery();

            if(results.next()) {
                try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                    updateStatement.setInt(1, userId);
                    updateStatement.setInt(2, productId);
                    updateStatement.executeUpdate();
                }
            }else {
                    try(PreparedStatement insertStatement = connection.prepareStatement(insertQuery)){
                        insertStatement.setInt(1, userId);
                        insertStatement.setInt(2, productId);
                        insertStatement.executeUpdate();
                    }
                }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }



}
