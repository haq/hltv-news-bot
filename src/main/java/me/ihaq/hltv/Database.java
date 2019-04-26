package me.ihaq.hltv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Database {

    private Connection connection;
    private Properties properties;

    public Connection connect() {
        if (connection == null) {
            try {
                Class.forName(Data.Database.DRIVER);
                connection = DriverManager.getConnection(Data.Database.URL, getProperties());
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }
        }
        return connection;
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            properties.setProperty("user", Data.Database.USERNAME);
            properties.setProperty("password", Data.Database.PASSWORD);
            properties.setProperty("MaxPooledStatements", Data.Database.MAX_POOL);
        }
        return properties;
    }
}