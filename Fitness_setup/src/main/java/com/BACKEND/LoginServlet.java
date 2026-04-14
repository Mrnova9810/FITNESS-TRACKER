package com.BACKEND;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.mindrot.jbcrypt.BCrypt;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/fitness_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "1234";

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);

            // 🔹 Check user exists
            PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM users WHERE username = ?"
            );
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            
            if(rs.next()){

                String storedHash = rs.getString("password");

                // 🔹 Check password
                if (BCrypt.checkpw(password, storedHash)) {

                    HttpSession session = request.getSession();
                    int userId = rs.getInt("id");

                    session.setAttribute("user_id", userId);
                    session.setAttribute("username", rs.getString("username"));

                    // 🔥 ADD COOKIE (NEW - DO NOT REMOVE)
                    Cookie cookie = new Cookie("user_id", String.valueOf(userId));
                    cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
                    cookie.setPath("/"); // IMPORTANT
                    response.addCookie(cookie);

                    // 🔥 CHECK IF PROFILE EXISTS
                    PreparedStatement ps2 = con.prepareStatement(
                        "SELECT * FROM user_profile WHERE user_id = ?"
                    );
                    ps2.setInt(1, userId);
                    ResultSet rs2 = ps2.executeQuery();

                    if (rs2.next()) {

                        // 🔥 CALL RECALCULATION (SAFE WAY)
                        try {
                            HttpURLConnection conn2 = (HttpURLConnection)
                                new java.net.URL("http://localhost:8080/one/recalculateCalories").openConnection();

                            conn2.setRequestMethod("POST");
                            conn2.setDoOutput(true);

                            // send session cookie
                            String sessionId = request.getSession().getId();
                            conn2.setRequestProperty("Cookie", "JSESSIONID=" + sessionId);

                            conn2.getResponseCode(); // trigger request

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // ✅ Redirect to dashboard
                        response.sendRedirect("dashboard.html");

                    } else {
                        // ❌ First time → Setup page
                        response.sendRedirect("setup.html");
                    }

                } else {
                    response.getWriter().println("Invalid Password");
                }

            } else {
                response.getWriter().println("User does not exist. Please sign up.");
            }

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Something went wrong: " + e.getMessage());
        }
    }
}