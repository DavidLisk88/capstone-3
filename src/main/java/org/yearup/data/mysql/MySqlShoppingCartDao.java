package org.yearup.data.mysql;

import org.yearup.data.ShoppingCartDao;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao {

    private final Connection connection;

    public MySqlShoppingCartDao (DataSource config) throws SQLException {
        super(config);
        this.connection = config.getConnection();
    }



}
