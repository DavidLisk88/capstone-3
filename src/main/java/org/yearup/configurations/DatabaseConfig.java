package org.yearup.configurations;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.mysql.MySqlShoppingCartDao;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
public class DatabaseConfig
{
    private BasicDataSource basicDataSource;


    @Bean
    public BasicDataSource dataSource()
    {
        return basicDataSource;
    }

    @Bean
    public ShoppingCartDao shoppingCartDao(DataSource dataSource, ProductDao productDao) throws SQLException {
        System.out.println("Creating ShoppingCartDao bean...");
        return new MySqlShoppingCartDao(dataSource, productDao);

    }


    @Autowired
    public DatabaseConfig(@Value("${datasource.url}") String url,
                          @Value("${datasource.username}") String username,
                          @Value("${datasource.password}") String password)
    {
        basicDataSource = new BasicDataSource();
        basicDataSource.setUrl(url);
        basicDataSource.setUsername(username);
        basicDataSource.setPassword(password);
    }

}