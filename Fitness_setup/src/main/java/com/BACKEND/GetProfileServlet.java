package com.BACKEND;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/getProfile")
public class GetProfileServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // 🔹 2. Get user_id from session
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user_id") == null) {
            response.getWriter().write("Session expired");
            return;
        }

        int userId = (int) session.getAttribute("user_id");



        try {
            // 🔹 Connect DB
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/fitness_db",
                    "root",
                    "1234"
            );


            PreparedStatement ps = con.prepareStatement("SELECT u.username, u.email,\n" +
                    "       up.age, up.height, up.goal, up.profile_pic\n" +
                    "FROM users u\n" +
                    "LEFT JOIN user_profile up ON u.id = up.user_id\n" +
                    "WHERE u.id = ?");
            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String json = "{"
                        + "\"username\":\"" + rs.getString("username") + "\","
                        + "\"email\":\"" + rs.getString("email") + "\","
                        + "\"goal\":\"" + (rs.getString("goal") != null ? rs.getString("goal") : "") + "\","
                        + "\"age\":\"" + rs.getInt("age") + "\","
                        + "\"height\":\"" + rs.getInt("height") + "\","
                        + "\"profilePic\":\"" + (rs.getString("profile_pic") != null ? rs.getString("profile_pic") : "") + "\""
                        + "}";

                out.print(json);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}