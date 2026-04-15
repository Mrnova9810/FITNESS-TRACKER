package com.fitness.tracker.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {


    // locally
//    private static final String URL = "jdbc:mysql://localhost:3306/fitness_db";
//    private static final String USER = "root";
//    private static final String PASSWORD = "1234";


    private static final String URL = System.getenv("SPRING_DATASOURCE_URL");
    private static final String USER = System.getenv("SPRING_DATASOURCE_USERNAME");
    private static final String PASSWORD = System.getenv("SPRING_DATASOURCE_PASSWORD");

    public static Connection getConnection() throws Exception {

//        Class.forName("com.mysql.cj.jdbc.Driver");
        Class.forName("org.postgresql.Driver");




        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}