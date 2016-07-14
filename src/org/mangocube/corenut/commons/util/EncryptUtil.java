package org.mangocube.corenut.commons.util;

import org.mangocube.corenut.commons.io.resource.Resource;
import org.mangocube.corenut.commons.io.resource.ResourcePatternResolver;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.security.Key;
import java.security.Security;

/**
 * Util for encoding the password and decoding it back.
 *
 * @since 1.0
 */
public class EncryptUtil {
    //Default encrypt algorithm
    private static final String ALGORITHM = "DES";
    private Object inputKey;

    public EncryptUtil(Object inputKey) {
        this.inputKey = inputKey;
        initialize();
    }

    private Cipher encryptCipher = null;

    private Cipher decryptCipher = null;

    /**
     * transfor the byteArray into Hex type String.
     *
     * @param arr array needs to be transformed
     * @return transformed string
     */
    private String byteArr2HexStr(byte[] arr) {
        int arrLength = arr.length;
        // byte's size is 2 times than char's
        StringBuffer resultBuffer = new StringBuffer(arrLength * 2);
        for (int i = 0; i < arrLength; i++) {
            int varNum = arr[i];
            while (varNum < 0) {
                varNum = varNum + 256;
            }
            if (varNum < 16) {
                resultBuffer.append("0");
            }
            resultBuffer.append(Integer.toString(varNum, 16));
        }
        return resultBuffer.toString();
    }

    /**
     * transform the hex string into byte[].
     *
     * @param str string needs to be transformed
     * @return byte arrary
     * @throws Exception unexpected exception
     */
    private byte[] hexStr2ByteArr(String str) throws Exception {
        byte[] byteArr = str.getBytes();
        int byteArrLength = byteArr.length;
        byte[] resultArr = new byte[byteArrLength / 2];
        for (int i = 0; i < byteArrLength; i = i + 2) {
            resultArr[i / 2] = (byte) Integer.parseInt(new String(byteArr, i, 2), 16);
        }
        return resultArr;
    }

    /**
     * specifies the key.
     *
     * @param keyStr key String
     */
    private void initialize() {
        Security.addProvider(new com.sun.crypto.provider.SunJCE());
        try {
            Key encryptKey = retreiveKey(inputKey);
            //initial encrypt cipher
            encryptCipher = Cipher.getInstance(ALGORITHM);
            encryptCipher.init(Cipher.ENCRYPT_MODE, encryptKey);
            //initial decrypt cipher
            decryptCipher = Cipher.getInstance(ALGORITHM);
            decryptCipher.init(Cipher.DECRYPT_MODE, encryptKey);
        } catch (Exception e) {
            throw new RuntimeException("Initialize failed!", e);
        }
    }

    /**
     * encrypt the byte array.
     *
     * @param byteArr byte[] needs to be encrypt
     * @return encrypted byte[]
     */
    private byte[] encrypt(byte[] byteArr) {
        try {
            return encryptCipher.doFinal(byteArr);
        } catch (Exception e) {
            throw new RuntimeException("Encrypt failed!", e);
        }
    }

    /**
     * encrypt string.
     *
     * @param strIn string which needs to be encrypt
     * @return encrypted string
     */
    public String encrypt(String strIn) {
        return byteArr2HexStr(encrypt(strIn.getBytes()));
    }

    /**
     * dencypt byte[].
     *
     * @param arr byte array needs to be dencrypt
     * @return dencrypt byte[]
     */
    private byte[] decrypt(byte[] arr) {
        try {
            return decryptCipher.doFinal(arr);
        } catch (Exception e) {
            throw new RuntimeException("Decrypt failed!", e);
        }
    }

    /**
     * dencrypt string.
     *
     * @param input string needs to be dencrpt
     * @return dencrpted string
     */
    public String decrypt(String input) {
        try {
            return new String(decrypt(hexStr2ByteArr(input)));
        } catch (Exception e) {
            throw new RuntimeException("Decrypt failed!", e);
        }
    }

    /*-----------------------------generates the key file---------------------------*/
    public static void genKeyFile(OutputStream out) throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        Key key = keyGen.generateKey();
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(key);
        oos.flush();
        oos.close();
    }

    /*------------------------   Retrieves the Key object --------------------------*/
    private Key retreiveKey(Object input) throws Exception {
        if (input instanceof InputStream) {
            return retreiveKey((InputStream) input);
        } else if (input instanceof String) {
            return retreiveKey((String) input);
        } else if (input instanceof Reader) {
            return retreiveKey((Reader) input);
        } else
            return null;
    }


    private Key retreiveKey(InputStream input) throws Exception {
        //if the input is an instance of the objectInputStream then readObject from it
        if (!(input instanceof ObjectInputStream)) return null;
        ObjectInputStream ois = (ObjectInputStream) input;
        return (Key) ois.readObject();
    }

    private Key retreiveKey(String keyContent) throws Exception {
        if (keyContent != null && keyContent.length() > 0) {
            return generateKey(keyContent.getBytes());
        } else
            return null;
    }

    private Key retreiveKey(Reader keyReader) throws Exception {
        if (keyReader == null) return null;
        //using bufferedReader to read the content of the inputKey
        BufferedReader bufReader = new BufferedReader(keyReader);
        StringBuffer key = new StringBuffer();
        String s = null;
        while ((s = bufReader.readLine()) != null) {
            key.append(s);
        }
        //invokes the basic function to generate the inputKey object
        return generateKey(key.toString().getBytes());
    }

    private Key generateKey(byte[] arr) {
        byte[] bytes = new byte[8];
        for (int i = 0; i < arr.length && i < bytes.length; i++) {
            bytes[i] = arr[i];
        }
        return new SecretKeySpec(bytes, ALGORITHM);
    }

    private byte[] retrievesContent(InputStream original) {
        try {
            BufferedInputStream ois = new BufferedInputStream(original);
            byte[] b = new byte[ois.available()];
            ois.read(b);
            return b;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //Encrypt the version content as a file resource
    public void encryptContent(OutputStream out, String resourcePattern) throws Exception {
        ResourcePatternResolver resolver = ResourcePatternResolver.getInstance();
        Resource resource = resolver.getResource(resourcePattern);
        InputStream original = new FileInputStream(resource.getFile());
        byte[] cipherByte = encryptCipher.doFinal(retrievesContent(original));
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(cipherByte);
        oos.close();
    }

    //decrypt the content and then output to the dist according to the wirter
    public void decryptCotnent(InputStream lic, Writer writer) throws Exception {
        ObjectInputStream oin = new ObjectInputStream(lic);
        byte[] cipherByte = (byte[]) oin.readObject();
        byte[] clearByte = decryptCipher.doFinal(cipherByte);
        writer.write(new String(clearByte));
        writer.flush();
        writer.close();
    }

    //decrypt the content and then output to the dist according to the outputstream
    public void decryptCotnent(InputStream lic, OutputStream out) throws Exception {
        ObjectInputStream oin = new ObjectInputStream(lic);
        byte[] cipherByte = (byte[]) oin.readObject();
        byte[] clearByte = decryptCipher.doFinal(cipherByte);
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(clearByte);
        oos.close();
    }
}