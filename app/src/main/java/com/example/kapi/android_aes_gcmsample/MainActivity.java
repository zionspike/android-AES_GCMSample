package com.example.kapi.android_aes_gcmsample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {


    // AES-GCM parameters
    public static final int AES_KEY_SIZE = 128; // in bits
    public static final int GCM_NONCE_LENGTH = 12; // in bytes
    public static final int GCM_TAG_LENGTH = 16; // in bytes

    /**
     * Helper converting hex string to string e.g. "7a4b7a78476d58513766333730626464" to "zKzxGmXQ7f370bdd"
     *
     * @param[hex] hex string like "7a4b7a78476d58513766333730626464"
     * @return[string] string from input hex string.
     */
    public static String convertHexToString(String hex) {
        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        // 49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for (int i = 0; i < hex.length() - 1; i += 2) {
            String output = hex.substring(i, (i + 2));
            int decimal = Integer.parseInt(output, 16);
            sb.append((char) decimal);
            temp.append(decimal);
        }
        return sb.toString();
    }

    /**
     * Generate SecretKey instance for using during encryption or decryption.
     *
     * @param[password] The 16 bytes encryption key in hex string format e.g. "7a4b7a78476d58513766333730626464"
     * @return[SecretKey] SecretKey instance.
     */
    public static SecretKey generateSecretKey(String password) {
        return new SecretKeySpec(password.getBytes(), "AES");
    }

    /**
     * Generate hex string from bytes array.
     *
     * @param[bytes] Data in bytes array.
     * @return[hexString] Hex string of input data.
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Encrypt byte plain text and return byte cipher.
     *
     * @param[hexKeyString] The 16 bytes encryption key in hex string format e.g. "7a4b7a78476d58513766333730626464"
     * @param[hexIvString] The 12 bytes IV in hex string format e.g. "36623865" + "3466363851555034"
     * @return[bytePlain] Cipher text in byte array format.
     */
    public static byte[] encrypt(byte[] bytePlain, String hexKeyString, String hexIvString) {
        byte[] cipherText = null;

        try {
            byte[] input = bytePlain;

            String keyString = convertHexToString(hexKeyString);
            String ivString = convertHexToString(hexIvString);
            SecretKey key;

            key = generateSecretKey(keyString);

            System.out.println("[+] Key: " + keyString);
            System.out.println("[+] IV: " + ivString);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            byte[] nonce = new byte[GCM_NONCE_LENGTH];

            nonce = ivString.getBytes();
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            // This is an optional, so just hard coded it for now.
            byte[] aad = "ABCDEFGHIJKL".getBytes();
            cipher.updateAAD(aad);

            cipherText = cipher.doFinal(input);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {

            e.printStackTrace();
        }
        return cipherText;
    }

    /**
     * Decrypt byte cipher and return byte plain text.
     *
     * @param[hexKeyString] The 16 bytes encryption key in hex string format e.g. "7a4b7a78476d58513766333730626464"
     * @param[hexIvString] The 12 bytes IV in hex string format e.g. "36623865" + "3466363851555034"
     * @return[bytePlain] Plain text data in byte array format.
     */
    public static byte[] decrypt(byte[] byteCipher, String hexKeyString, String hexIvString) {
        byte[] bytePlain = null;

        try {
            byte[] input = byteCipher;

            String keyString = convertHexToString(hexKeyString);
            String ivString = convertHexToString(hexIvString);
            SecretKey key;

            key = generateSecretKey(keyString);

            System.out.println("[+] Key: " + keyString);
            System.out.println("[+] IV: " + ivString);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            byte[] nonce = new byte[GCM_NONCE_LENGTH];

            nonce = ivString.getBytes();
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            // This is an optional, so just hard coded it for now.
            byte[] aad = "ABCDEFGHIJKL".getBytes();
            cipher.updateAAD(aad);

            bytePlain = cipher.doFinal(input);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {

            e.printStackTrace();
        }

        return bytePlain;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("KAPI-DEBUG","##### Encrypting #####");
        String text = "this is the test text";
        String keyHexString = "7a4b7a78476d58513766333730626464";

        // IV is 12-byte random number and considered as a nonce in AES/GCM.
        // This nonce must be unique so that for each encryption with the same key the nonce should not be repeat
        // In RFC5288 the 3rd section (https://tools.ietf.org/html/rfc5288#section-3) inform about handling IV
        // IV is composed of 4 bytes salt and 8 bytes nonce_explicit
        // The first 4 bytes of IV should be chosen during handshake and keep it secretly on both sides.
        // The second 8 bytes of IV can be sent in clear-text along with the encrypted data
        // Each value of the nonce_explicit MUST be distinct for each distinct
        // invocation of the GCM encrypt function for any fixed key.  Failure to
        // meet this uniqueness requirement can significantly degrade security.
        // The nonce_explicit MAY be the 64-bit sequence number.
        String ivSaltHex = "36623865";
        String nonceHex = "3466363851555034";
        String ivHexString = ivSaltHex + nonceHex;

        byte[] byteCipher = encrypt(text.getBytes(), keyHexString, ivHexString);
        byte[] tag = Arrays.copyOfRange(byteCipher, byteCipher.length - ((GCM_TAG_LENGTH * 8) / Byte.SIZE),
                byteCipher.length);

        // This line utilizes Base64 of java.util.Base64
        // String base64Cipher = Base64.getEncoder().encodeToString(byteCipher);

        // For Android app use the following line
        String base64Cipher = Base64.encodeToString(byteCipher, Base64.DEFAULT);

        String nonce = convertHexToString(ivHexString);

        Log.d("KAPI-DEBUG","[+] Plain: " + text);
        Log.d("KAPI-DEBUG","[+] Cipher(hex): " + bytesToHex(byteCipher));
        Log.d("KAPI-DEBUG","[+] Tag(hex): " + bytesToHex(tag));
        Log.d("KAPI-DEBUG","[+] Encoded cipher: " + base64Cipher);
        Log.d("KAPI-DEBUG","[+] Nonce: " + nonce);

        Log.d("KAPI-DEBUG","\n##### Decrypting #####");

        byte[] tagFromCipher = Arrays.copyOfRange(byteCipher, byteCipher.length - ((GCM_TAG_LENGTH * 8) / Byte.SIZE),
                byteCipher.length);

        // ivSalt is known implicitly and chosen during handshake. Nonce is carried explicitly for each packets.
        byte[] bytePlain = decrypt(byteCipher, keyHexString, (ivSaltHex + nonceHex));
        String decryptedText = convertHexToString(bytesToHex(bytePlain));

        Log.d("KAPI-DEBUG","[+] Plain(hex): " + bytesToHex(bytePlain));
        Log.d("KAPI-DEBUG","[+] Tag(hex): " + bytesToHex(tagFromCipher));
        Log.d("KAPI-DEBUG","[+] Decoded plain: " + decryptedText);
        Log.d("KAPI-DEBUG","[+] Nonce: " + nonce);

        Log.d("KAPI-DEBUG",""+text.equals(decryptedText));

        // ##### Encrypting #####
        // [+] Plain: this is the test text
        // [+] Cipher(hex): 382E86E8756828D1DE17B5BA885B34F9E0A8F94EA656C77699EA6182D3C227DBE0DFF3B19B
        // [+] Tag(hex): 56C77699EA6182D3C227DBE0DFF3B19B
        // [+] Encoded cipher: OC6G6HVoKNHeF7W6iFs0+eCo+U6mVsd2mephgtPCJ9vg3/Oxmw==
        // [+] Nonce: 6b8e4f68QUP4
        // ##### Decrypting #####
        // [+] Plain(hex): 746869732069732074686520746573742074657874
        // [+] Tag(hex): 56C77699EA6182D3C227DBE0DFF3B19B
        // [+] Decoded plain: this is the test text
        // [+] Nonce: 6b8e4f68QUP4
        // true

    }
}
