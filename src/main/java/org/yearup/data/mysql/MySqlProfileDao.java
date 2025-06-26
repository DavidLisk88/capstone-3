package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.models.Profile;
import org.yearup.data.ProfileDao;
import org.yearup.models.User;

import javax.sql.DataSource;
import java.sql.*;

@Component
public class MySqlProfileDao extends MySqlDaoBase implements ProfileDao
{

    private final Connection connection;

    public MySqlProfileDao(DataSource dataSource) throws SQLException
    {
        super(dataSource);
        this.connection = dataSource.getConnection();
    }



    @Override
    public Profile create(Profile profile)
    {
        String sql = "INSERT INTO profiles (user_id, first_name, last_name, phone, email, address, city, state, zip) " +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try(Connection connection = getConnection())
        {
            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, profile.getUserId());
            ps.setString(2, profile.getFirstName());
            ps.setString(3, profile.getLastName());
            ps.setString(4, profile.getPhone());
            ps.setString(5, profile.getEmail());
            ps.setString(6, profile.getAddress());
            ps.setString(7, profile.getCity());
            ps.setString(8, profile.getState());
            ps.setString(9, profile.getZip());

            ps.executeUpdate();

            return profile;
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

@Override
    public void updateProfile (Profile profile) throws SQLException {
        String sql = "UPDATE profiles SET first_name = ?, last_name = ?, " +
                "phone = ?, email = ?, address = ?, city = ?, state = ?, zip = ? " +
                "WHERE user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, profile.getFirstName());
            statement.setString(2, profile.getLastName());
            statement.setString(3, profile.getPhone());
            statement.setString(4, profile.getEmail());
            statement.setString(5, profile.getAddress());
            statement.setString(6, profile.getCity());
            statement.setString(7, profile.getState());
            statement.setString(8, profile.getZip());
            statement.setInt(9, profile.getUserId());
            statement.executeUpdate();
        }



}

@Override
public Profile getByUserId (int userId) {
        String sql = "SELECT * FROM profiles WHERE user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setInt(1, userId);
            ResultSet results = statement.executeQuery();
            if (results.next()){
                Profile profile = new Profile();
                profile.setUserId(results.getInt("user_id"));
                profile.setFirstName(results.getString("first_name"));
                profile.setLastName(results.getString("last_name"));
                profile.setPhone(results.getString("phone"));
                profile.setEmail(results.getString("email"));
                profile.setAddress(results.getString("address"));
                profile.setCity(results.getString("city"));
                profile.setState(results.getString("state"));
                profile.setZip(results.getString("zip"));
                return profile;

            } else {
                return null ;
            }

        } catch (SQLException e){
            e.printStackTrace();
        }
      return new Profile();
}

}
