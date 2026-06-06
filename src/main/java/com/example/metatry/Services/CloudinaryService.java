package com.example.metatry.Services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadImage(MultipartFile file) throws IOException {
        File convFile = File.createTempFile("upload_", "_" + file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        }

        try {
            Map uploadResult = cloudinary.uploader().upload(convFile, ObjectUtils.asMap(
                    "folder", "metatry/instagram",
                    "resource_type", "auto"
            ));
            return (String) uploadResult.get("secure_url");
        } finally {
            convFile.delete();
        }
    }

    public Map<String, Object> uploadWithOptions(MultipartFile file, Map<String, Object> options) throws IOException {
        File convFile = File.createTempFile("upload_", "_" + file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        }

        try {
            return cloudinary.uploader().upload(convFile, options);
        } finally {
            convFile.delete();
        }
    }
    public String uploadImageBytes(byte[] imageBytes) throws Exception {

        Map uploadResult = cloudinary.uploader().upload(
                imageBytes,
                Map.of("resource_type", "image")
        );

        return (String) uploadResult.get("secure_url");
    }
}