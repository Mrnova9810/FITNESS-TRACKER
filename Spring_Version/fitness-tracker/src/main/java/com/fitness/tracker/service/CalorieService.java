package com.fitness.tracker.service;

import com.fitness.tracker.util.DBConnection;
import org.springframework.stereotype.Service;

import java.sql.*;

@Service
public class CalorieService {

    public String recalculateCalories(int userId) {

        Connection con = null;

        PreparedStatement lastUpdatePs = null;
        PreparedStatement checkDays = null;
        PreparedStatement w2Ps = null;
        PreparedStatement w1Ps = null;
        PreparedStatement goalPs = null;
        PreparedStatement updatePs = null;

        ResultSet lastRs = null;
        ResultSet daysRs = null;
        ResultSet w2Rs = null;
        ResultSet w1Rs = null;
        ResultSet goalRs = null;

        try {
            con = DBConnection.getConnection();

            // 1. Get current stats
            lastUpdatePs = con.prepareStatement(
                    "SELECT last_updated, calorie_target FROM user_stats WHERE user_id=?"
            );
            lastUpdatePs.setInt(1, userId);

            lastRs = lastUpdatePs.executeQuery();

            if (!lastRs.next()) return "ERROR: NO_STATS";

            // ✅ FIX: use Timestamp instead of Date
            Timestamp lastUpdated = lastRs.getTimestamp("last_updated");
            int currentCalories = lastRs.getInt("calorie_target");

            // 2. Days difference
            checkDays = con.prepareStatement(
                    "SELECT DATEDIFF(CURDATE(), ?) AS days"
            );
            checkDays.setTimestamp(1, lastUpdated);

            daysRs = checkDays.executeQuery();
            daysRs.next();

            if (daysRs.getInt("days") < 14) {
                return "SKIPPED: TOO_EARLY";
            }

            // 3. Weight week 2
            w2Ps = con.prepareStatement(
                    "SELECT AVG(weight) FROM weight WHERE user_id=? AND date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)"
            );
            w2Ps.setInt(1, userId);

            w2Rs = w2Ps.executeQuery();
            double week2 = (w2Rs.next()) ? w2Rs.getDouble(1) : 0;
            if (w2Rs.wasNull()) week2 = 0;

            // 4. Weight week 1
            w1Ps = con.prepareStatement(
                    "SELECT AVG(weight) FROM weight WHERE user_id=? AND date BETWEEN DATE_SUB(CURDATE(), INTERVAL 14 DAY) AND DATE_SUB(CURDATE(), INTERVAL 7 DAY)"
            );
            w1Ps.setInt(1, userId);

            w1Rs = w1Ps.executeQuery();
            double week1 = (w1Rs.next()) ? w1Rs.getDouble(1) : week2;
            if (w1Rs.wasNull()) week1 = week2;

            double change = week2 - week1;

            // 5. Goal
            goalPs = con.prepareStatement(
                    "SELECT goal FROM user_profile WHERE user_id=?"
            );
            goalPs.setInt(1, userId);

            goalRs = goalPs.executeQuery();

            String goal = goalRs.next() ? goalRs.getString("goal") : "maintenance";

            int newCalories = applyGoalAdjustment(goal,change,currentCalories);

            // 8. Update DB
            updatePs = con.prepareStatement(
                    "UPDATE user_stats SET calorie_target=?, last_updated=CURDATE() WHERE user_id=?"
            );

            updatePs.setInt(1, newCalories);
            updatePs.setInt(2, userId);
            updatePs.executeUpdate();

            return "SUCCESS:" + newCalories;

        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR: SERVER";

        } finally {

            try { if (lastRs != null) lastRs.close(); } catch (Exception ignored) {}
            try { if (daysRs != null) daysRs.close(); } catch (Exception ignored) {}
            try { if (w2Rs != null) w2Rs.close(); } catch (Exception ignored) {}
            try { if (w1Rs != null) w1Rs.close(); } catch (Exception ignored) {}
            try { if (goalRs != null) goalRs.close(); } catch (Exception ignored) {}

            try { if (lastUpdatePs != null) lastUpdatePs.close(); } catch (Exception ignored) {}
            try { if (checkDays != null) checkDays.close(); } catch (Exception ignored) {}
            try { if (w2Ps != null) w2Ps.close(); } catch (Exception ignored) {}
            try { if (w1Ps != null) w1Ps.close(); } catch (Exception ignored) {}
            try { if (goalPs != null) goalPs.close(); } catch (Exception ignored) {}
            try { if (updatePs != null) updatePs.close(); } catch (Exception ignored) {}

            try { if (con != null) con.close(); } catch (Exception ignored) {}
        }
    }

    private int applyGoalAdjustment(String goal, double change, int currentCalories) {

        int newCalories = currentCalories;

        if ("Weight Loss".equals(goal)) {
            if (change > -0.3) newCalories -= 200;
            else if (change < -1.2) newCalories += 150;

        } else if ("Muscle Gain".equals(goal)) {
            if (change < 0.2) newCalories += 200;
            else if (change > 0.8) newCalories -= 150;

        } else {
            if (Math.abs(change) > 0.5) {
                newCalories += (change > 0) ? -150 : 150;
            }
        }

        return Math.max(1200, Math.min(newCalories, 4000));
    }

    public void recalculateFromProfile(int userId) {


        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "SELECT up.age, up.height, up.goal, up.gender, up.activity_level, us.current_weight " +
                            "FROM user_profile up " +
                            "JOIN user_stats us ON up.user_id = us.user_id " +
                            "WHERE up.user_id=?"
            );

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) return;

            int age = rs.getInt("age");
            int height = rs.getInt("height");
            double weight = rs.getDouble("current_weight");
            String gender = rs.getString("gender");
            String activity = rs.getString("activity_level");
            String goal = rs.getString("goal");

            // 🔹 BMR
            double bmr = "male".equals(gender)
                    ? (10 * weight) + (6.25 * height) - (5 * age) + 5
                    : (10 * weight) + (6.25 * height) - (5 * age) - 161;

            // 🔹 Activity multiplier
            double multiplier = 1.2;
            if ("light".equals(activity)) multiplier = 1.375;
            else if ("moderate".equals(activity)) multiplier = 1.55;
            else if ("heavy".equals(activity)) multiplier = 1.725;

            // 🔹 TDEE
            double tdee = bmr * multiplier;

            // 🔹 Goal calories
            int calories;
            if ("Weight Loss".equals(goal)) {
                calories = (int) (tdee - 400);
            } else if ("Muscle Gain".equals(goal)) {
                calories = (int) (tdee + 300);
            } else {
                calories = (int) tdee;
            }

            // 🔒 Safety
            calories = Math.max(1200, Math.min(calories, 4000));

            // 🔥 UPDATE BOTH calorie_target + tdee
            PreparedStatement update = con.prepareStatement(
                    "UPDATE user_stats SET calorie_target=?, tdee=?, last_updated=CURDATE() WHERE user_id=?"
            );

            update.setInt(1, calories);
            update.setDouble(2, tdee);
            update.setInt(3, userId);

            update.executeUpdate();
            // check calories update in profile in terminal.
//            System.out.println("userId:" + userId);
//            System.out.println("goal : " + goal);
//            System.out.println("profile change due to Recalculate : " + calories);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}