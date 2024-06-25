import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Properties;

public class CryptoUtil3 {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String PROPERTIES_FILE = "config.properties";
    private static final String SECRET_KEY_PROPERTY = "secretKey";
    private static final int GCM_IV_LENGTH = 12; // 12 bytes for GCM IV
    private static final int GCM_TAG_LENGTH = 128; // 128 bits for GCM tag
    private static final int KEY_SIZE = 256; // 256 bits for AES key

    public static void main(String[] args) {
        try {
            Properties prop = new Properties();

            // Check if the properties file exists and load it
            try (FileInputStream fis = new FileInputStream(PROPERTIES_FILE)) {
                prop.load(fis);
            } catch (IOException e) {
                // If the properties file doesn't exist, create it and generate a new key
                System.out.println("Properties file not found. Generating a new key and creating the properties file.");
                String secretKey = generateSecretKey();
                prop.setProperty(SECRET_KEY_PROPERTY, secretKey);
                saveProperties(prop);
            }

            String secretKey = prop.getProperty(SECRET_KEY_PROPERTY);
            if (secretKey == null || secretKey.length() != 44) {
                throw new IllegalArgumentException("Invalid secret key in properties file. The key must be 44 characters long (Base64 encoded 256-bit key).");
            }

            String originalString = "Hello, World!";
            String encryptedString = encrypt(originalString, secretKey);
            String decryptedString = decrypt(encryptedString, secretKey);

            System.out.println("Original String: " + originalString);
            System.out.println("Encrypted String: " + encryptedString);
            System.out.println("Decrypted String: " + decryptedString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String generateSecretKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(KEY_SIZE); // AES-256
        SecretKey secretKey = keyGen.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    public static void saveProperties(Properties prop) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(PROPERTIES_FILE)) {
            prop.store(fos, null);
        }
    }

    public static String encrypt(String strToEncrypt, String secret) throws Exception {
        byte[] key = Base64.getDecoder().decode(secret);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, ALGORITHM);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv); // Secure random IV generation

        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmParameterSpec);

        byte[] encryptedBytes = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));
        byte[] encryptedIVAndText = new byte[GCM_IV_LENGTH + encryptedBytes.length];

        System.arraycopy(iv, 0, encryptedIVAndText, 0, GCM_IV_LENGTH);
        System.arraycopy(encryptedBytes, 0, encryptedIVAndText, GCM_IV_LENGTH, encryptedBytes.length);

        return Base64.getEncoder().encodeToString(encryptedIVAndText);
    }

    public static String decrypt(String strToDecrypt, String secret) throws Exception {
        byte[] key = Base64.getDecoder().decode(secret);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, ALGORITHM);

        byte[] encryptedIVAndText = Base64.getDecoder().decode(strToDecrypt);
        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(encryptedIVAndText, 0, iv, 0, iv.length);

        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmParameterSpec);

        byte[] encryptedBytes = new byte[encryptedIVAndText.length - GCM_IV_LENGTH];
        System.arraycopy(encryptedIVAndText, GCM_IV_LENGTH, encryptedBytes, 0, encryptedBytes.length);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}
