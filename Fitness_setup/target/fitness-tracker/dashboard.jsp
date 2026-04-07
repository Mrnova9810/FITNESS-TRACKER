<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="javax.servlet.http.*, javax.servlet.*" %>

<%
// Get existing session
HttpSession session = request.getSession(false);

// If session doesn't exist or username not set, redirect to login page
if (session == null || session.getAttribute("username") == null) {
response.sendRedirect("login.html");
return;
}

String username = (String) session.getAttribute("username");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Dashboard</title>
</head>
<body>
<h1>Welcome, <%= username %>!</h1>
<p>This is your fitness dashboard.</p>
</body>
</html>