package datasheet.hellostatistics;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.swing.text.DefaultEditorKit.InsertBreakAction;

public class KeyChangeCli {
	public static void main(String[] args) {
		try(FileInputStream fis = new FileInputStream(new File("keys/box/private-key"));) {
			long size = Files.size(Paths.get("keys/box/private-key"));
			byte[] bytes = new byte[(int)size];
			fis.read(bytes);
			fis.close();
			byte[] privateKeyBytes = Base64.getDecoder().decode(bytes);
//			System.out.println(Base64.getEncoder().encodeToString(objectByte));
//			ByteArrayInputStream bis = new ByteArrayInputStream(objectByte);
//			ObjectInputStream obj = new ObjectInputStream(new ByteArrayInputStream(objectByte));
			PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
//			X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
			KeyFactory keyFactory = KeyFactory.getInstance("DSA", Security.getProvider("SUN"));
//			PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
			PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
			//privateKey.getEncoded();
			Signature sign = Signature.getInstance("SHA256withDSA", Security.getProvider("SUN"));
			sign.initSign(privateKey);
			sign.update("CLOSE".getBytes());
			byte[] signData = sign.sign();
			Socket socket = new Socket("localhost", 9098);
			OutputStream os = socket.getOutputStream();
			os.write(signData);
			os.flush();
			os.close();
			socket.close();
		} catch(IOException e) {
			e.printStackTrace();
		} catch(NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch(InvalidKeySpecException e) {
			e.printStackTrace();
		} catch(InvalidKeyException e) {
			e.printStackTrace();
		} catch(SignatureException e) {
			e.printStackTrace();
		}
	}
}
