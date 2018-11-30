package com.net2plan.oaas;


import java.sql.SQLException;

public class Main
{
    public static void main(String [] args)
    {
        try {
            DatabaseController cont = new DatabaseController("localhost:3306","girtel","girtelserver");
        } catch (SQLException e)
        {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
