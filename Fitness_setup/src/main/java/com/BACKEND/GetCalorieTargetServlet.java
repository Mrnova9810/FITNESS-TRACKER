package com.BACKEND;

import java.io.IOException;
import java.sql.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/getCalorieTarget")
public class GetCalorieTargetServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        try {
            HttpSession session = request.getSession(false);

            if (session == null || session.getAttribute("user_id") == null) {
                response.getWriter().print(2200); // fallback
                return;
            }

            Integer userId = (Integer) session.getAttribute("user_id");

            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/fitness_db",
                "root",
                "1234"
            );

            PreparedStatement ps = con.prepareStatement(
                "SELECT calorie_target FROM user_stats WHERE user_id = ?"
            );
            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            int calories = 2200; // fallback

            if (rs.next()) {
                calories = rs.getInt("calorie_target");
            }

            response.getWriter().print(calories);

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}