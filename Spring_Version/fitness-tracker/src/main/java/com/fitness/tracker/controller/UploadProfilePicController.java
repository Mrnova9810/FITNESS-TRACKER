package com.fitness.tracker.controller;
import org.springframework.beans.factory.annotation.Value;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fitness.tracker.util.DBConnection;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

@RestController
public class UploadProfilePicController {
    private final Cloudinary cloudinary;
    public UploadProfilePicController(  @Value("${cloudinary.cloud_name}") String cloudName,
                                        @Value("${cloudinary.api_key}") String apiKey,
                                        @Value("${cloudinary.api_secret}") String apiSecret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

    @PostMapping("/uploadProfilePic")
    public Map<String, Object> uploadProfilePic(
            @RequestParam("file") MultipartFile file,
            HttpSession session
    ) {

        Map<String, Object> response = new HashMap<>();

        try {
            Integer userId = (Integer) session.getAttribute("user_id");

            if (userId == null) {
                response.put("status", "session_expired");
                return response;
            }

            var con = DBConnection.getConnection();


            //delete pic from cloud
            PreparedStatement getPs = con.prepareStatement(
                    "SELECT pic_id FROM user_profile WHERE user_id=?"
            );
            // get user old pic id row!
            getPs.setInt(1, userId);
            ResultSet rs = getPs.executeQuery();

            // updating users profile pic

            // 1. Convert MultipartFile → File
            File tempFile = File.createTempFile("upload_",".tmp" );
            file.transferTo(tempFile);


            // 2. Upload to Cloudinary
            Map uploadResult = cloudinary.uploader().upload(
                    tempFile,
                    ObjectUtils.emptyMap()
            );

            tempFile.delete();

            String imageUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");


            if (uploadResult == null || uploadResult.get("secure_url") == null){
                throw new RuntimeException("Upload failed");
            }

            PreparedStatement ps = con.prepareStatement(
                    "UPDATE user_profile SET profile_pic=?, pic_id=? WHERE user_id=?"
            );

            ps.setString(1, imageUrl);
            ps.setString(2, publicId);
            ps.setInt(3, userId);
            int rows = ps.executeUpdate();
            ps.close();
            if (rows == 0) {
                var insertPs = con.prepareStatement(
                        "INSERT INTO user_profile (user_id, profile_pic, pic_id) VALUES (?, ?, ?)"
                );

                insertPs.setInt(1, userId);
                insertPs.setString(2, imageUrl);
                insertPs.setString(3, publicId);
                insertPs.executeUpdate();
                insertPs.close();
            }



            // now delete after the user profile pic is added!
            if (rs.next()) {
                String oldPublicId = rs.getString("pic_id");

                if (oldPublicId != null && !oldPublicId.isEmpty()) {
                    cloudinary.uploader().destroy(oldPublicId, ObjectUtils.emptyMap());
                }
            }

            rs.close();
            con.close();

            response.put("status", "success");
            response.put("url", imageUrl);

            return response;

        } catch (Exception e) {
            e.printStackTrace();
        }

        response.put("status", "error");
        return response;
    }
}