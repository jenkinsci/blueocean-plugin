package io.jenkins.blueocean.commons;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * common place for sha 256 Hex etc...
 */
public class DigestUtils
{

    public static String sha256(byte[] original) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(original);
            StringBuilder sb = new StringBuilder(2+encodedhash.length);
            for(byte b:encodedhash){
                sb.append(b);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static String sha256(String original) {
        return sha256(original, StandardCharsets.UTF_8);
    }

    public static String sha256(String original, Charset charset) {
        return sha256(original.getBytes(charset));
    }

    public static String sha256Hex(String original) {
        try {
            MessageDigest digest = MessageDigest.getInstance( "SHA-256");
            byte[] encodedhash = digest.digest(
                original.getBytes( StandardCharsets.UTF_8));
            return bytesToHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString( 0xff & b );
            if (hex.length() == 1) {
                hexString.append( '0' );
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

}
