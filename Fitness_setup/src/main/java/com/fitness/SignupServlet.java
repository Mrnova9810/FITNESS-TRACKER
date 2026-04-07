package com.fitness;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/signup")
public class SignupServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Get data from form
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try {
            // 2. Load JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 3. Create connection
            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/fitness_db",
                    "root",
                    ""   // 🔴 replace with your MySQL password
            );

            // 4. SQL Query
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO users(username, email, password) VALUES (?, ?, ?)"
            );

            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, password);

            // 5. Execute
            int result = ps.executeUpdate();

            if (result > 0) {
                // success → redirect to login page
                response.sendRedirect("index.html");
                System.out.println(" GO TO login page");
            } else {
                response.getWriter().println("Signup Failed");
            }

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}