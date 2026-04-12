package com.BACKEND;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.DriverManager;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/setup")
public class SetupServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/fitness_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "1234";

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            // 🔹 Get session user_id
            HttpSession session = request.getSession();
            int userId = (int) session.getAttribute("user_id");

            // 🔹 Get form data
            int age = Integer.parseInt(request.getParameter("age"));
            int height = Integer.parseInt(request.getParameter("height"));
            double weight = Double.parseDouble(request.getParameter("weight"));
            String gender = request.getParameter("gender");
            String activity = request.getParameter("activity");
            String goal = request.getParameter("goal");

            // 🔹 Calculate BMR
            double bmr;
            if (gender.equals("male")) {
                bmr = (10 * weight) + (6.25 * height) - (5 * age) + 5;
            } else {
                bmr = (10 * weight) + (6.25 * height) - (5 * age) - 161;
            }

            // 🔹 Activity multiplier
            double multiplier = 1.2;
            if (activity.equals("light")) multiplier = 1.375;
            else if (activity.equals("moderate")) multiplier = 1.55;
            else if (activity.equals("heavy")) multiplier = 1.725;

            // 🔹 TDEE
            double tdee = bmr * multiplier;

            // 🔹 Goal calories
            int calories;
            if (goal.equals("fat_loss")) {
                calories = (int) (tdee - 400);
            } else if (goal.equals("muscle_gain")) {
                calories = (int) (tdee + 300);
            } else {
                calories = (int) tdee;
            }

            // 🔹 DB Connection
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);

            // 🔹 Insert into user_profile
            String profileQuery = "INSERT INTO user_profile (user_id, age, height, gender, activity_level, goal) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps1 = conn.prepareStatement(profileQuery);
            ps1.setInt(1, userId);
            ps1.setInt(2, age);
            ps1.setInt(3, height);
            ps1.setString(4, gender);
            ps1.setString(5, activity);
            ps1.setString(6, goal);
            ps1.executeUpdate();

            // 🔹 Insert into user_stats
            String statsQuery = "INSERT INTO user_stats (user_id, current_weight, calorie_target, tdee, last_updated) VALUES (?, ?, ?, ?, CURDATE())";
            PreparedStatement ps2 = conn.prepareStatement(statsQuery);
            ps2.setInt(1, userId);
            ps2.setDouble(2, weight);
            ps2.setInt(3, calories);
            ps2.setDouble(4, tdee);
            ps2.executeUpdate();

            // 🔹 Insert into weight table
            String weightQuery = "INSERT INTO weight (user_id, weight, date) VALUES (?, ?, CURDATE())";
            PreparedStatement ps3 = conn.prepareStatement(weightQuery);
            ps3.setInt(1, userId);
            ps3.setDouble(2, weight);
            ps3.executeUpdate();

            conn.close();

            // 🔹 Redirect to dashboard
            response.sendRedirect("dashboard.html");

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Error: " + e.getMessage());
        }
    }
}