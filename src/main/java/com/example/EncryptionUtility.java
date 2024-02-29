import org.apache.commons.crypto.Crypto;
import org.apache.commons.crypto.cipher.CryptoCipher;
import org.apache.commons.crypto.cipher.CryptoCipherFactory;
import org.apache.commons.crypto.cipher.CryptoCipherFactory.CipherProvider;
import org.apache.commons.crypto.utils.Utils;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class EncryptionUtility {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATION_COUNT = 65536;
    private static final int KEY_LENGTH = 256;

    public static String encrypt(String data, String password) throws Exception {
        byte[] salt = generateSalt();
        byte[] key = Utils.pbkdf2(password, salt, ITERATION_COUNT, KEY_LENGTH);

        CipherProvider provider = Utils.getCipherProvider();
        try (CryptoCipher encipher = CryptoCipherFactory.getCryptoCipher(ALGORITHM + TRANSFORMATION, provider)) {
            encipher.init(Cipher.ENCRYPT_MODE, Utils.createKey(key), Utils.createNonce());

            byte[] input = data.getBytes(StandardCharsets.UTF_8);
            byte[] output = new byte[encipher.getOutputSize(input.length)];
            int updateBytes = encipher.update(input, 0, input.length, output, 0);
            int finalBytes = encipher.doFinal(output, updateBytes);

            byte[] result = new byte[updateBytes + finalBytes];
            System.arraycopy(output, 0, result, 0, result.length);

            return Base64.getEncoder().encodeToString(result);
        }
    }

    public static String decrypt(String encryptedData, String password) throws Exception {
        byte[] salt = generateSalt();
        byte[] key = Utils.pbkdf2(password, salt, ITERATION_COUNT, KEY_LENGTH);

        CipherProvider provider = Utils.getCipherProvider();
        try (CryptoCipher decipher = CryptoCipherFactory.getCryptoCipher(ALGORITHM + TRANSFORMATION, provider)) {
            decipher.init(Cipher.DECRYPT_MODE, Utils.createKey(key), Utils.createNonce());

            byte[] input = Base64.getDecoder().decode(encryptedData);
            byte[] output = new byte[decipher.getOutputSize(input.length)];
            int updateBytes = decipher.update(input, 0, input.length, output, 0);
            int finalBytes = decipher.doFinal(output, updateBytes);

            byte[] result = new byte[updateBytes + finalBytes];
            System.arraycopy(output, 0, result, 0, result.length);

            return new String(result, StandardCharsets.UTF_8);
        }
    }

    private static byte[] generateSalt() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        return salt;
    }

    public static void main(String[] args) {
        try {
            String originalData = "Hello, encryption!";
            String password = "StrongPassword";

            String encryptedData = encrypt(originalData, password);
            System.out.println("Encrypted: " + encryptedData);

            String decryptedData = decrypt(encryptedData, password);
            System.out.println("Decrypted: " + decryptedData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}