package com.fitness.tracker.controller;

import com.fitness.tracker.service.CalorieService;
import com.fitness.tracker.util.DBConnection;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

@RestController
public class UpdateProfileController {
    @Autowired
    private CalorieService calorieService;

    @PostMapping("/updateProfile")
    public Map<String, String> updateProfile(
            @RequestBody Map<String, Object> body,
            HttpSession session
    ) {

        Map<String, String> response = new HashMap<>();

        try {
            Integer userId = (Integer) session.getAttribute("user_id");

            if (userId == null) {
                response.put("status", "session_expired");
                return response;
            }

            // 🔹 Get values directly (no parsing hacks)
            String username = (String) body.get("username");
            String email = (String) body.get("email");
            String password = (String) body.get("password");
            String goal = (String) body.get("goal");

            Integer age = body.get("age") != null ? Integer.parseInt(body.get("age").toString()) : 0;
            Integer height = body.get("height") != null ? Integer.parseInt(body.get("height").toString()) : 0;

            Connection con = DBConnection.getConnection();

            // 🔐 Hash password if provided
            if (password != null && !password.isEmpty()) {

                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

                PreparedStatement userPs = con.prepareStatement(
                        "UPDATE users SET username=?, email=?, password=? WHERE id=?"
                );

                userPs.setString(1, username);
                userPs.setString(2, email);
                userPs.setString(3, hashedPassword);
                userPs.setInt(4, userId);
                userPs.executeUpdate();

            } else {

                PreparedStatement userPs = con.prepareStatement(
                        "UPDATE users SET username=?, email=? WHERE id=?"
                );

                userPs.setString(1, username);
                userPs.setString(2, email);
                userPs.setInt(3, userId);
                userPs.executeUpdate();
            }
            // after updating users table
            session.setAttribute("username", username);



            // 🔹 Check if profile exists
            PreparedStatement checkPs = con.prepareStatement(
                    "SELECT * FROM user_profile WHERE user_id=?"
            );
            checkPs.setInt(1, userId);

            ResultSet rs = checkPs.executeQuery();

            if (rs.next()) {

                // UPDATE
                PreparedStatement updatePs = con.prepareStatement(
                        "UPDATE user_profile SET age=?, height=?, goal=? WHERE user_id=?"
                );

                updatePs.setInt(1, age);
                updatePs.setInt(2, height);
                updatePs.setString(3, goal);
                updatePs.setInt(4, userId);

                updatePs.executeUpdate();

            } else {

                // INSERT
                PreparedStatement insertPs = con.prepareStatement(
                        "INSERT INTO user_profile (user_id, age, height, goal) VALUES (?, ?, ?, ?)"
                );

                insertPs.setInt(1, userId);
                insertPs.setInt(2, age);
                insertPs.setInt(3, height);
                insertPs.setString(4, goal);

                insertPs.executeUpdate();
            }


            con.close();
            calorieService.recalculateFromProfile(userId);

            response.put("status", "success");
            return response;

        } catch (Exception e) {
            e.printStackTrace();
        }

        response.put("status", "error");
        return response;
    }
}