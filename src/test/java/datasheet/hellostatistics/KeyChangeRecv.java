package datasheet.hellostatistics;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class KeyChangeRecv {
	public static void main(String[] args) {
		try(FileInputStream fis = new FileInputStream(new File("keys/box/pulic-key"));) {
			long size = Files.size(Paths.get("keys/box/pulic-key"));
			byte[] bytes = new byte[(int)size];
			fis.read(bytes);
			fis.close();
			byte[] publicKeyBytes = Base64.getDecoder().decode(bytes);
			X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
			KeyFactory keyFactory = KeyFactory.getInstance("DSA", Security.getProvider("SUN"));
			PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
			Signature sign = Signature.getInstance("SHA256withDSA", Security.getProvider("SUN"));
			sign.initVerify(publicKey);
			sign.update("CLOSE".getBytes());
			try(ServerSocket svrSock = new ServerSocket(9098)) {
				Socket sock = svrSock.accept();
				InputStream is = sock.getInputStream();
				byte[] data = new byte[1024];
				int dataSize = is.read(data);
				byte[] newData = Arrays.copyOf(data, dataSize);
				if(sign.verify(newData)){
					System.out.println(new String(newData));
				}
				is.close();
				sock.close();
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
