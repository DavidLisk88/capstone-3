package org.yearup.data.mysql;

import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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



}
