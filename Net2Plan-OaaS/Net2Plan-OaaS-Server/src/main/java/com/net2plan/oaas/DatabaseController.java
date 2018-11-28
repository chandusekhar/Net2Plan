package com.net2plan.oaas;

import java.sql.*;

public class DatabaseController
{
    private Connection connection;
    private final String defaultDataBaseName = "net2planOaaS";
    private final String defaultTableName = "users";
    private String databaseName;

    public DatabaseController(String dbUrl, String dbUser, String dbPass, String... optionalDatabase)
    {
        try {
            this.connection = DriverManager.getConnection(dbUrl, dbUser, dbPass);
            int optionalDBsize = optionalDatabase.length;
            if(optionalDBsize == 0)
                this.databaseName = defaultDataBaseName;
            else if(optionalDBsize == 1)
                this.databaseName = optionalDatabase[0];
            else{
                throw new RuntimeException("More than one database is not allowed");
            }
            checkIfDatabaseIsCreated();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private void checkIfDatabaseIsCreated()
    {
        try {
        String checkDatabaseQuery = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '" + databaseName + "'";
        ResultSet checkSet = executeQuery(checkDatabaseQuery);
        if(checkSet.next())
        {
            setDatabase(databaseName);
            createDefaultTableIfNotExists();
        }

        } catch (SQLException e)
        {
            e.printStackTrace();
        }

    }

    private void createDefaultTableIfNotExists()
    {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS 'users' (" +
                "'id' INT NOT NULL AUTO_INCREMENT," +
                "'user' VARCHAR(20) NOT NULL," +
                "'password' VARCHAR(100) NOT NULL)";
        executeQuery(createTableQuery);
    }

    private ResultSet executeQuery(String query)
    {
        ResultSet set = null;
        try {
            Statement statement = connection.createStatement();
            set = statement.executeQuery(query);

        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        return set;
    }

    public void setDatabase(String dbName)
    {
        executeQuery("USE "+dbName);
    }

    public boolean logInUser(String user, String password)
    {
        boolean exists = false;
        String loginQuery = "SELECT * FROM users WHERE user = '" + user + "' AND password = '" + password + "'";
        ResultSet loginSet = executeQuery(loginQuery);
        try {
            if(loginSet.next())
            {
                exists = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return exists;
    }

}
