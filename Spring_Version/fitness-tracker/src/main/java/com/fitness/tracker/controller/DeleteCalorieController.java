package com.fitness.tracker.controller;

import com.fitness.tracker.util.DBConnection;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Connection;
import java.sql.PreparedStatement;

@Controller
public class DeleteCalorieController {

    @PostMapping("/deleteCalorie")
    public String deleteCalorie(@RequestParam int id,
                                HttpSession session) {

        Integer userId = (Integer) session.getAttribute("user_id");

        // 🔒 not logged in
        if (userId == null) {
            return "redirect:/landingPage.html";
        }

        try {
            Connection con = DBConnection.getConnection();

            // ✅ delete only user's data
            PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM calories WHERE id=? AND user_id=?"
            );

            ps.setInt(1, id);
            ps.setInt(2, userId);

            ps.executeUpdate();
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 🔁 stay on same page (or refresh)
        return "redirect:/calorie.html";
    }
}
