package com.net2plan.oaas;

import com.net2plan.utils.Pair;

import java.sql.*;

public class DatabaseController
{
    private Connection connection;
    private final String defaultDataBaseName = "net2planOaaS";
    private final String defaultTableName = "users";
    private String databaseName;

    protected DatabaseController(String dbIpPort, String dbUser, String dbPass) throws SQLException, ClassNotFoundException
    {
        Class.forName("com.mysql.cj.jdbc.Driver");
        this.connection = DriverManager.getConnection("jdbc:mysql://"+dbIpPort+"?useLegacyDatetimeCode=false&serverTimezone=UTC", dbUser, dbPass);
        this.databaseName = defaultDataBaseName;
        checkIfDatabaseIsCreated();

    }

    private void checkIfDatabaseIsCreated() throws SQLException
    {
        String checkDatabaseQuery = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '" + databaseName + "'";
        ResultSet checkSet = executeQuery(checkDatabaseQuery);
        if(checkSet.next())
        {
            setDatabase(databaseName);
            createDefaultTableIfNotExists();
        }
        else{
            String createDatabaseQuery = "CREATE DATABASE "+databaseName;
            executeQuery(createDatabaseQuery);
            setDatabase(databaseName);
        }


    }

    private void createDefaultTableIfNotExists() throws SQLException
    {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + defaultTableName + " (id INT NOT NULL AUTO_INCREMENT, user VARCHAR(20) NOT NULL, password VARCHAR(100) NOT NULL, category ENUM('BRONZE','SILVER','GOLD') NOT NULL, PRIMARY KEY (id))";
        executeQuery(createTableQuery);
    }

    private ResultSet executeQuery(String query) throws SQLException
    {
        ResultSet set = null;
        Statement statement = connection.createStatement();
        boolean execute = statement.execute(query);
        if(execute)
            set = statement.getResultSet();
        else
            set = null;

        return set;
    }

    private void setDatabase(String dbName) throws SQLException
    {
        executeQuery("USE "+dbName);
    }

    protected Pair<Boolean, ResultSet> authenticate(String user, String password) throws SQLException
    {
        boolean exists = false;
        ResultSet set = null;
        String loginQuery = "SELECT * FROM " + defaultTableName + " WHERE user = '" + user + "' AND password = '" + password + "'";
        ResultSet loginSet = executeQuery(loginQuery);
        if(loginSet.first())
        {
            exists = true;
            set = loginSet;
        }

        return Pair.unmodifiableOf(exists, set);
    }

}
