
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
 
/**
 *  
 * @author Giulio
 */
public class ToolKit {

	    public static Cipher ecipher;
	    public static Cipher dcipher;

	    ToolKit(SecretKey key) {
	        try {
	            ecipher = Cipher.getInstance("DES");
	            dcipher = Cipher.getInstance("DES");
	            ecipher.init(Cipher.ENCRYPT_MODE, key);
	            dcipher.init(Cipher.DECRYPT_MODE, key);

	        } catch (javax.crypto.NoSuchPaddingException e) {
	        } catch (java.security.NoSuchAlgorithmException e) {
	        } catch (java.security.InvalidKeyException e) {
	        }
	    }

	   static public String encrypt(String str) {
	        try {
	            // Encode the string into bytes using utf-8
	            byte[] utf8 = str.getBytes("UTF8");

	            // Encrypt
	            byte[] enc = ecipher.doFinal(utf8);

	            // Encode bytes to base64 to get a string
	            return new sun.misc.BASE64Encoder().encode(enc);
	        } catch (javax.crypto.BadPaddingException e) {
	        } catch (IllegalBlockSizeException e) {
	        } catch (UnsupportedEncodingException e) {
	        } catch (java.io.IOException e) {
	        }
	        return null;
	    }

	   static public String decrypt(String str) {
	        try {
	            // Decode base64 to get bytes
	            byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(str);

	            // Decrypt
	            byte[] utf8 = dcipher.doFinal(dec);

	            // Decode using utf-8
	            return new String(utf8, "UTF8");
	        } catch (javax.crypto.BadPaddingException e) {
	        } catch (IllegalBlockSizeException e) {
	        } catch (UnsupportedEncodingException e) {
	        } catch (java.io.IOException e) {
	        }
	        return null;
	    }
	
	
    public static void main(String[] args){
    	
    	try {
    	    // Generate a temporary key. In practice, you would save this key.
    	    // See also Encrypting with DES Using a Pass Phrase.
    	    SecretKey key = KeyGenerator.getInstance("DES").generateKey();

    	    // Create encrypter/decrypter class
    	    ToolKit encrypter = new ToolKit(key);

    	    // Encrypt
    	    String encrypted = encrypter.encrypt("Don't tell anybody!");

    	    // Decrypt
    	    String decrypted = encrypter.decrypt(encrypted);
    	} catch (Exception e) {
    	}
    	
    	String s = "";
    	
			System.out.print(ToolKit.encrypt(s));
		
			
			if(s.equals(ToolKit.decrypt(ToolKit.encrypt(s)))){
			System.out.println("fucking vodka");
			}
		
		
 
    }
}
 