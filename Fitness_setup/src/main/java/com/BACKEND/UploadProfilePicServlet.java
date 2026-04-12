package com.BACKEND;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Map;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/uploadProfilePic")
@MultipartConfig
public class UploadProfilePicServlet extends HttpServlet {
    private Cloudinary cloudinary;

    @Override
    public void init() {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dki9kibca",
                "api_key", "965142775362433",
                "api_secret", "2eyDSyPKOQzNA-ytLcVtAG_trPc"
        ));
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType("application/json");
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user_id") == null) {
            response.getWriter().write("Session expired");
            return;
        }

        int userId = (int) session.getAttribute("user_id");

        try {

            // 🔹 Get file from request
            Part filePart = request.getPart("file");
            String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();

            File tempFile = File.createTempFile("upload_", fileName);
            filePart.write(tempFile.getAbsolutePath());

            Map uploadResult = cloudinary.uploader().upload(
                    tempFile,
                    ObjectUtils.emptyMap()
            );

            tempFile.delete();

            String imageUrl = (String) uploadResult.get("secure_url");
            if (uploadResult == null || uploadResult.get("secure_url") == null) {
                throw new RuntimeException("Cloudinary upload failed");
            }


            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/fitness_db",
                    "root",
                    "1234"
            );

            // 🔹 Update DB with filename only
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE user_profile SET profile_pic=? WHERE user_id=?"
            );


            ps.setString(1, imageUrl);
            ps.setInt(2, userId);
            ps.executeUpdate();

            response.getWriter().write("{\"status\":\"success\",\"url\":\"" + imageUrl + "\"}");

        } catch (Exception e) {
            e.printStackTrace();

            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}"
            );
        }
    }
}