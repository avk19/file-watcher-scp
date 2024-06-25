import org.apache.commons.crypto.cipher.CryptoCipher;
import org.apache.commons.crypto.cipher.CryptoCipherFactory;
import org.apache.commons.crypto.utils.Utils;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Arrays;
import java.util.Properties;

public class CryptoUtil {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String PROPERTIES_FILE = "config.properties";
    private static final String SECRET_KEY_PROPERTY = "secretKey";

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
            if (secretKey == null || secretKey.length() != 24) {
                throw new IllegalArgumentException("Invalid secret key in properties file. The key must be 24 characters long (Base64 encoded).");
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
        keyGen.init(128); // AES-128
        SecretKey secretKey = keyGen.generateKey();
        return Base64.encodeBase64String(secretKey.getEncoded());
    }

    public static void saveProperties(Properties prop) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(PROPERTIES_FILE)) {
            prop.store(fos, null);
        }
    }

    public static String encrypt(String strToEncrypt, String secret) throws Exception {
        byte[] key = Base64.decodeBase64(secret);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, ALGORITHM);

        byte[] iv = new byte[16]; // Initialization vector with 16 bytes (128 bits)
        Arrays.fill(iv, (byte) 0x00); // Initialize with zero

        Properties properties = new Properties();
        try (CryptoCipher encipher = CryptoCipherFactory.getCryptoCipher(TRANSFORMATION, properties)) {
            encipher.init(CryptoCipher.ENCRYPT_MODE, secretKeySpec, new javax.crypto.spec.IvParameterSpec(iv));
            byte[] input = strToEncrypt.getBytes(StandardCharsets.UTF_8);
            byte[] output = new byte[encipher.getOutputSize(input.length)];
            int updateBytes = encipher.update(input, 0, input.length, output, 0);
            int finalBytes = encipher.doFinal(input, 0, 0, output, updateBytes);
            return Base64.encodeBase64String(output, 0, updateBytes + finalBytes);
        }
    }

    public static String decrypt(String strToDecrypt, String secret) throws Exception {
        byte[] key = Base64.decodeBase64(secret);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, ALGORITHM);

        byte[] iv = new byte[16]; // Initialization vector with 16 bytes (128 bits)
        Arrays.fill(iv, (byte) 0x00); // Initialize with zero

        Properties properties = new Properties();
        try (CryptoCipher decipher = CryptoCipherFactory.getCryptoCipher(TRANSFORMATION, properties)) {
            decipher.init(CryptoCipher.DECRYPT_MODE, secretKeySpec, new javax.crypto.spec.IvParameterSpec(iv));
            byte[] input = Base64.decodeBase64(strToDecrypt);
            byte[] output = new byte[decipher.getOutputSize(input.length)];
            int updateBytes = decipher.update(input, 0, input.length, output, 0);
            int finalBytes = decipher.doFinal(input, 0, 0, output, updateBytes);
            return new String(output, 0, updateBytes + finalBytes, StandardCharsets.UTF_8);
        }
    }
}
