package com.BACKEND;

import java.io.IOException;
import java.sql.*;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/recalculateCalories")
public class RecalculateCaloriesServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        try {
            HttpSession session = request.getSession(false);
            Integer userId = (Integer) session.getAttribute("user_id");

            if (userId == null) {
                response.getWriter().print("User not logged in");
                return;
            }

            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/fitness_db",
                "root",
                "1234"
            );

            // 🔹 1. CHECK LAST UPDATE
            PreparedStatement lastUpdatePs = con.prepareStatement(
                "SELECT last_updated, calorie_target FROM user_stats WHERE user_id=?"
            );
            lastUpdatePs.setInt(1, userId);

            ResultSet lastRs = lastUpdatePs.executeQuery();

            if (!lastRs.next()) {
                response.getWriter().print("No stats found");
                con.close();
                return;
            }

            Date lastUpdated = lastRs.getDate("last_updated");
            int currentCalories = lastRs.getInt("calorie_target");

            // 🔹 Check if 14 days passed
            PreparedStatement checkDays = con.prepareStatement(
                "SELECT DATEDIFF(CURDATE(), ?) AS days"
            );
            checkDays.setDate(1, lastUpdated);

            ResultSet daysRs = checkDays.executeQuery();
            daysRs.next();

            int days = daysRs.getInt("days");

            if (days < 14) {
                response.getWriter().print("Not enough days yet");
                con.close();
                return;
            }

            // 🔥 =========================
            // 🔥 NEW TREND-BASED LOGIC
            // 🔥 =========================

            // 🔹 WEEK 2 (recent 7 days)
            PreparedStatement w2Ps = con.prepareStatement(
                "SELECT AVG(weight) FROM weight WHERE user_id=? AND date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)"
            );
            w2Ps.setInt(1, userId);
            ResultSet w2Rs = w2Ps.executeQuery();

            double week2 = 0;
            if (w2Rs.next()) week2 = w2Rs.getDouble(1);

            // 🔹 WEEK 1 (previous 7 days)
            PreparedStatement w1Ps = con.prepareStatement(
                "SELECT AVG(weight) FROM weight WHERE user_id=? AND date BETWEEN DATE_SUB(CURDATE(), INTERVAL 14 DAY) AND DATE_SUB(CURDATE(), INTERVAL 7 DAY)"
            );
            w1Ps.setInt(1, userId);
            ResultSet w1Rs = w1Ps.executeQuery();

            double week1 = week2;
            if (w1Rs.next()) week1 = w1Rs.getDouble(1);

            // 🔥 TREND CHANGE
            double change = week2 - week1;

            // 🔹 GET USER GOAL
            PreparedStatement goalPs = con.prepareStatement(
                "SELECT goal FROM user_profile WHERE user_id=?"
            );
            goalPs.setInt(1, userId);
            ResultSet goalRs = goalPs.executeQuery();

            String goal = "maintenance";
            if (goalRs.next()) {
                goal = goalRs.getString("goal");
            }

            int newCalories = currentCalories;

            // 🔥 GOAL-BASED ADAPTIVE LOGIC

            if (goal.equals("fat_loss")) {

                if (change > -0.3) {
                    newCalories -= 200; // no loss
                } 
                else if (change < -1.2) {
                    newCalories += 150; // too fast loss
                }

            } else if (goal.equals("muscle_gain")) {

                if (change < 0.2) {
                    newCalories += 200; // no gain
                } 
                else if (change > 0.8) {
                    newCalories -= 150; // too fast gain
                }

            } else { // maintenance

                if (Math.abs(change) > 0.5) {
                    newCalories += (change > 0) ? -150 : 150;
                }
            }

            // 🔒 SAFETY LIMITS
            if (newCalories < 1200) newCalories = 1200;
            if (newCalories > 4000) newCalories = 4000;

            // 🔹 UPDATE DATABASE
            PreparedStatement updatePs = con.prepareStatement(
                "UPDATE user_stats SET calorie_target=?, last_updated=CURDATE() WHERE user_id=?"
            );

            updatePs.setInt(1, newCalories);
            updatePs.setInt(2, userId);

            updatePs.executeUpdate();

            response.getWriter().print("Calories Updated to: " + newCalories);

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}