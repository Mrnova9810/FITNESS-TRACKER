package com.fitness.tracker.controller;

import com.fitness.tracker.util.DBConnection;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.*;

@Controller
public class AddWeightController {

    @PostMapping("/addWeight")
    public String addWeight(@RequestParam double weight,
                            HttpSession session) {

        Integer userId = (Integer) session.getAttribute("user_id");

        // 🔒 not logged in
        if (userId == null) {
            return "redirect:/landingPage.html";
        }

        try {
            Connection con = DBConnection.getConnection();

            // 🔍 Check if today's entry exists
            PreparedStatement check = con.prepareStatement(
                    "SELECT id \n" +
                            "FROM weight \n" +
                            "WHERE user_id = ? AND date = CURRENT_DATE"
            );
            check.setInt(1, userId);

            ResultSet rs = check.executeQuery();

            if (rs.next()) {
                // 🔄 UPDATE
                PreparedStatement update = con.prepareStatement(
                        "UPDATE weight SET weight=? WHERE user_id=? AND date= CURRENT_DATE"
                );
                update.setDouble(1, weight);
                update.setInt(2, userId);
                update.executeUpdate();

            } else {
                // ➕ INSERT
                PreparedStatement insert = con.prepareStatement(
                        "INSERT INTO weight(user_id, weight, date ) VALUES (?, ?, CURRENT_DATE)"
                );
                insert.setInt(1, userId);
                insert.setDouble(2, weight);
                insert.executeUpdate();
            }

            con.close();

            return "redirect:/weight.html";

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/error.html";
    }
}