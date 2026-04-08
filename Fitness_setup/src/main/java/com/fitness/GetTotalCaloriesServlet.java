package com.fitness;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/getTotalCalories")
public class GetTotalCaloriesServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("user_id");

        if (userId == null) {
            response.getWriter().print("0"); // user not logged in
            return;
        }
        int totalCalories = 0;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/fitness_db",
                "root",
                "1234"
            );

            // 🔥 FIXED QUERY
            PreparedStatement ps = con.prepareStatement(
                    "SELECT SUM(calories) FROM calories WHERE user_id=? AND DATE(time)=CURDATE()"
            );

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                totalCalories = rs.getInt(1);
            }

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        out.print(totalCalories);
    }
}