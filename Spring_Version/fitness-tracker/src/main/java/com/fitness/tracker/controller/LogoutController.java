package com.fitness.tracker.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LogoutController {

    @GetMapping("/logout")
    public String logout(HttpSession session,
                         HttpServletResponse response) {

        // 🔥 destroy session
        if (session != null) {
            session.invalidate();
        }

        // 🔥 delete cookie
        Cookie cookie = new Cookie("user_id", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        return "redirect:/landingPage.html";
    }
}