package com.BACKEND;

import java.io.IOException;
import java.sql.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/addWeight")
public class AddWeightServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        double weight = Double.parseDouble(request.getParameter("weight"));

        HttpSession session = request.getSession(false);
        int userId = (int) session.getAttribute("user_id"); // ✅ INT from session

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/fitness_db",
                    "root",
                    "1234"
            );

            // 🔍 Check if today's entry exists
            PreparedStatement check = con.prepareStatement(
                    "SELECT id FROM weight WHERE user_id=? AND `date`=CURDATE()"
            );
            check.setInt(1, userId);

            ResultSet rs = check.executeQuery();

            if (rs.next()) {
                // 🔄 UPDATE
                PreparedStatement update = con.prepareStatement(
                        "UPDATE weight SET weight=? WHERE user_id=? AND `date`=CURDATE()"
                );
                update.setDouble(1, weight);
                update.setInt(2, userId);
                update.executeUpdate();
            } else {
                // ➕ INSERT
                PreparedStatement insert = con.prepareStatement(
                        "INSERT INTO weight(user_id, weight, `date`) VALUES (?, ?, CURDATE())"
                );
                insert.setInt(1, userId);
                insert.setDouble(2, weight);
                insert.executeUpdate();
            }

            con.close();
            response.sendRedirect("weight.html");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}