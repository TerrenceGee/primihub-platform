package com.primihub.biz.util.crypt;

import com.primihub.biz.constant.RemoteConstant;

import java.util.Random;

public class RemoteUtil {
    private static final String PREFIX = "TSREQ_";
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int RANDOM_STRING_LENGTH = 19;

    public static void main(String[] args) {
        String randomString = generateRandomString();
        System.out.println(randomString);
    }

    public static String generateRandomString() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        // Add prefix
        sb.append(PREFIX);

        // Generate random alphanumeric characters
        for (int i = 0; i < RANDOM_STRING_LENGTH; i++) {
            int index = random.nextInt(ALPHABET.length());
            char randomChar = ALPHABET.charAt(index);
            sb.append(randomChar);
        }

        return sb.toString();
    }

    public static String generateSignature(String requestId, String secretId, String secretKey) {
        String plainSignature = RemoteConstant.SIGN_TEMPLATE.replace("<requestRefId>", requestId)
                .replace("<secretId>", secretId);
        return SM3Util.encrypt(plainSignature, secretKey);
    }


}
