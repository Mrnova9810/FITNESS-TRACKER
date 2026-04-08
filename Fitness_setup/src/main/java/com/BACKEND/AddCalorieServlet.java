package com.BACKEND;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/addCalorie")
public class AddCalorieServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String food = request.getParameter("food");
        int calories = Integer.parseInt(request.getParameter("calories"));

        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("user_id"); // ✅ user_id from session

        if (userId == null) {
            response.sendRedirect("index.html");
            return;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/fitness_db",
                    "root",
                    "1234"
            );

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO calories(user_id, food, calories, `time`) VALUES (?, ?, ?, NOW())"
            );

            ps.setInt(1, userId);
            ps.setString(2, food);
            ps.setInt(3, calories);

            ps.executeUpdate();
            con.close();

            response.sendRedirect("calorie.html");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}