import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Properties;

public class CryptoUtil2 {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String PROPERTIES_FILE = "config.properties";
    private static final String SECRET_KEY_PROPERTY = "secretKey";
    private static final int KEY_SIZE = 256;

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
                throw new IllegalArgumentException("Invalid secret key in properties file. The key must be 44 characters long (Base64 encoded).");
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
        byte[] iv = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] encryptedBytes = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));

        byte[] encryptedBytesWithIv = new byte[iv.length + encryptedBytes.length];
        System.arraycopy(iv, 0, encryptedBytesWithIv, 0, iv.length);
        System.arraycopy(encryptedBytes, 0, encryptedBytesWithIv, iv.length, encryptedBytes.length);

        return Base64.getEncoder().encodeToString(encryptedBytesWithIv);
    }

    public static String decrypt(String strToDecrypt, String secret) throws Exception {
        byte[] key = Base64.getDecoder().decode(secret);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, ALGORITHM);

        byte[] encryptedBytesWithIv = Base64.getDecoder().decode(strToDecrypt);

        byte[] iv = new byte[16];
        byte[] encryptedBytes = new byte[encryptedBytesWithIv.length - iv.length];

        System.arraycopy(encryptedBytesWithIv, 0, iv, 0, iv.length);
        System.arraycopy(encryptedBytesWithIv, iv.length, encryptedBytes, 0, encryptedBytes.length);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}
