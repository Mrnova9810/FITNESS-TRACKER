package com.BACKEND;

import java.io.IOException;
import java.sql.*;

import org.mindrot.jbcrypt.BCrypt;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/signup")
public class SignupServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/fitness_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "1234";

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);

            // 🔐 Hash password
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            // 🔥 IMPORTANT: get generated user_id
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO users(username, email, password) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );

            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, hashedPassword);

            int result = ps.executeUpdate();

            if (result > 0) {

                // 🔥 GET USER ID
                ResultSet rs = ps.getGeneratedKeys();
                int userId = -1;

                if (rs.next()) {
                    userId = rs.getInt(1);
                }

                // 🔥 CREATE SESSION (VERY IMPORTANT)
                HttpSession session = request.getSession();
                session.setAttribute("user_id", userId);
                session.setAttribute("username", username);

                // 🔥 REDIRECT TO SETUP PAGE
                response.sendRedirect("setup.html");

            } else {
                response.getWriter().println("Signup Failed");
            }

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Error: " + e.getMessage());
        }
    }
}