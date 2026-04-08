package com.fitness;

import java.io.IOException;
import java.sql.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/updateWeight")
public class UpdateWeightServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("user_id");

        if (userId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in");
            return;
        }

        int id = Integer.parseInt(request.getParameter("id"));
        double weight = Double.parseDouble(request.getParameter("weight"));

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/fitness_db",
                    "root",
                    "1234"
            );

            // ✅ Only update if this id belongs to the logged-in user
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE weight SET weight=? WHERE id=? AND user_id=?"
            );
            ps.setDouble(1, weight);
            ps.setInt(2, id);
            ps.setInt(3, userId);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You cannot update this entry");
            }

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}