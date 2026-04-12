package com.BACKEND;

import org.mindrot.jbcrypt.BCrypt;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.*;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/updateProfile")
public class UpdateProfileServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");

        // 🔹 Get session
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user_id") == null) {
            response.getWriter().write("Session expired");
            return;
        }

        int userId = (int) session.getAttribute("user_id");

        // 🔹 Read JSON body
        StringBuilder body = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;

        while ((line = reader.readLine()) != null) {
            body.append(line);
        }


        // 🔹 VERY SIMPLE parsing (basic approach)
        String jsonStr = body.toString();

        String username = getValue(jsonStr, "username");
        String email = getValue(jsonStr, "email");
        String password = getValue(jsonStr, "password");
        String goal = getValue(jsonStr, "goal");
        String ageStr = getValue(jsonStr, "age");
        String heightStr = getValue(jsonStr, "height");

        int age = ageStr.isEmpty() ? 0 : Integer.parseInt(ageStr);
        int height = heightStr.isEmpty() ? 0 : Integer.parseInt(heightStr);

        try {


            // 🔹 DB Connection
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/fitness_db",
                    "root",
                    "1234"
            );
            // 4. Hash the password before storing
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            // =========================
            // 1. UPDATE USERS TABLE
            // =========================
            if (password != null && !password.isEmpty()) {

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



            // =========================
            // 2. CHECK PROFILE EXISTS
            // =========================
            PreparedStatement checkPs = con.prepareStatement(
                    "SELECT * FROM user_profile WHERE user_id=?"
            );

            checkPs.setInt(1, userId);
            ResultSet rs = checkPs.executeQuery();

            if (rs.next()) {

                // =========================
                // 3A. UPDATE PROFILE
                // =========================
                PreparedStatement updatePs = con.prepareStatement(
                        "UPDATE user_profile SET age=?, height=?, goal=? WHERE user_id=?"
                );

                updatePs.setInt(1, age);
                updatePs.setInt(2, height);
                updatePs.setString(3, goal);
                updatePs.setInt(4, userId);

                updatePs.executeUpdate();

            } else {

                // =========================
                // 3B. INSERT PROFILE
                // =========================
                PreparedStatement insertPs = con.prepareStatement(
                        "INSERT INTO user_profile (user_id, age, height, goal) VALUES (?, ?, ?, ?)"
                );

                insertPs.setInt(1, userId);
                insertPs.setInt(2, age);
                insertPs.setInt(3, height);
                insertPs.setString(4, goal);

                insertPs.executeUpdate();
            }

            response.getWriter().write("{\"status\":\"success\"}");
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("{\"status\":\"error\"}");
        }
    }
    // 🔥 THIS MUST BE HERE (inside class, outside doPost)
    private String getValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\":\"";
            int start = json.indexOf(pattern) + pattern.length();
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        } catch (Exception e) {
            return "";
        }
    }

}