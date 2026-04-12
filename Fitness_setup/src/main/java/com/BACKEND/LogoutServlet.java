package com.BACKEND;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate(); // destroy session
        }

        // 🔥 DELETE COOKIE (NEW)
        Cookie cookie = new Cookie("user_id", "");
        cookie.setMaxAge(0); // delete immediately
        cookie.setPath("/"); // IMPORTANT
        response.addCookie(cookie);

        response.sendRedirect("landingPage.html");
    }
}