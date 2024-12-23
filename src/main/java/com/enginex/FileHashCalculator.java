package com.enginex;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileHashCalculator {

    public static String getMD5Hash(Path filePath) throws IOException, NoSuchAlgorithmException {
        // Create an MD5 MessageDigest instance
        MessageDigest md5Digest = MessageDigest.getInstance("MD5");

        // Use a FileInputStream wrapped in a DigestInputStream
        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             DigestInputStream dis = new DigestInputStream(fis, md5Digest)) {

            // Read the file data to update the MessageDigest
            byte[] buffer = new byte[1024]; // 1 KB buffer
            while (dis.read(buffer) != -1) {
                // The DigestInputStream updates the digest automatically
            }
        }

        // Get the final hash bytes
        byte[] hashBytes = md5Digest.digest();

        // Convert hash bytes to a hexadecimal string
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0'); // Pad single-digit hex values
            hexString.append(hex);
        }

        return hexString.toString();
    }
}