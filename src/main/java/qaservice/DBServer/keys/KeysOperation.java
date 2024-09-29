package qaservice.DBServer.keys;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.security.Provider.Service;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Iterator;

import qaservice.Common.charcterutil.CharUtil;
import qaservice.DBServer.keys.exception.KeySettingException;
import qaservice.DBServer.main.DBServerMainGuiStart;
import qaservice.DBServer.util.DBServerPropReader;

public class KeysOperation {
	private static final String PRIVATE_KEY = "private-key";
	private static final String PUBLIC_KEY = "public-key";
	
	private static String KEY_DIR_PATH = "";
	
	public static void initialize() throws KeySettingException {
		String keydirPath = DBServerPropReader.getProperties("keysdirectorypath").toString();
		KEY_DIR_PATH = keydirPath;
		boolean everyKeyGenerate = Boolean.parseBoolean(DBServerPropReader.getProperties("everyKeyGenerate").toString());
		if(everyKeyGenerate || !existsKey()) {
			newCreateKey();
		}
	}
	
	private static void newCreateKey() throws KeySettingException {
		Path dirPath = Paths.get(KEY_DIR_PATH);
		if(!Files.exists(dirPath)) {
			try {
				Files.createDirectories(dirPath);
			} catch(IOException e) {
				e.printStackTrace();
				throw new KeySettingException(e.getMessage());
			}
		}
		String sp = DBServerPropReader.getProperties("securityProvider").toString();
		String algorithm = DBServerPropReader.getProperties("securityAlgorithm").toString();
		Provider provider = Security.getProvider(sp);
		try {
			KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(algorithm, provider);
			KeyPair keyPair = keyPairGen.generateKeyPair();
			PrivateKey privateKey = keyPair.getPrivate();
			String privateKeyBase64 = Base64.getEncoder().encodeToString(privateKey.getEncoded());
			PublicKey pubKey = keyPair.getPublic();
			String pubKeyBase64 = Base64.getEncoder().encodeToString(pubKey.getEncoded());
			//System.out.println(privateKeyBase64);
			//System.out.println(pubKeyBase64);
			Path privPath = Paths.get(KEY_DIR_PATH + File.separator + PRIVATE_KEY);
			if(!Files.exists(privPath)) {
				Files.createFile(privPath);
			}
			Path pubPath = Paths.get(KEY_DIR_PATH + File.separator + PUBLIC_KEY);
			if(!Files.exists(pubPath)) {
				Files.createFile(pubPath);
			}
			//Private Key output to file
			FileOutputStream filePriv = new FileOutputStream(privPath.toFile());
			filePriv.write(privateKeyBase64.getBytes(CharUtil.getCharset()));
			filePriv.flush();
			filePriv.close();
			
			//Public Key output to file
			FileOutputStream filepub = new FileOutputStream(pubPath.toFile());
			filepub.write(pubKeyBase64.getBytes(CharUtil.getCharset()));
			filepub.flush();
			filepub.close();
		} catch(NoSuchAlgorithmException e) {
			//e.printStackTrace();
			DBServerMainGuiStart.guiConsoleOut(e.getMessage());
			throw new KeySettingException(e.getMessage());
		} catch(IOException ie) {
			//ie.printStackTrace();
			DBServerMainGuiStart.guiConsoleOut(ie.getMessage());
			throw new KeySettingException(ie.getMessage());
		}
	}
	
	private static boolean existsKey() throws KeySettingException {
		Path dirPath = Paths.get(KEY_DIR_PATH);
		if(!Files.exists(dirPath)) {
			try {
				Files.createDirectories(dirPath);
			} catch(IOException e) {
				//e.printStackTrace();
				DBServerMainGuiStart.guiConsoleOut(e.getMessage());
				throw new KeySettingException(e.getMessage());
			}
			return false;
		}
		for(File f : dirPath.toFile().listFiles()) {
			if(PRIVATE_KEY.equals(f.getName())) {
				return true;
			}
		}
		return false;
	}
	
	public static void printSecurityProviderList() {
		for(Provider p : Security.getProviders()) {
			System.out.println("++++++++++++++++++++++++++++++++");
			System.out.println(p.getName());
			System.out.println(p.getInfo());
			Iterator<Service> is = p.getServices().iterator();
			while(is.hasNext()) {
				Service s = is.next();
				System.out.println(s.getAlgorithm() + " / " + s.getProvider().getName() + " / " + s.getType());
			}
			System.out.println("++++++++++++++++++++++++++++++++");
			System.out.println("");
		}
	}
	
	public static PublicKey createPublicKeyFromByteData() {
		Path keyPath = Paths.get(KEY_DIR_PATH + "/" + PUBLIC_KEY);
		String sp = DBServerPropReader.getProperties("securityProvider").toString();
		String algorithm = DBServerPropReader.getProperties("securityAlgorithm").toString();
		Provider provider = Security.getProvider(sp);
		try(FileInputStream fis = new FileInputStream(keyPath.toFile())) {
			long fileSize = Files.size(keyPath);
			byte[] readbuf = new byte[(int)fileSize];
			fis.read(readbuf);
			byte[] publicKeyBytes = Base64.getDecoder().decode(readbuf);
			X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
			KeyFactory keyFactory = KeyFactory.getInstance(algorithm, provider);
			return keyFactory.generatePublic(publicKeySpec);
		} catch(IOException e) {
			//e.printStackTrace();
			DBServerMainGuiStart.guiConsoleOut(e.getMessage());
			return null;
		} catch(NoSuchAlgorithmException e) {
			//e.printStackTrace();
			DBServerMainGuiStart.guiConsoleOut(e.getMessage());
			return null;
		} catch(InvalidKeySpecException e) {
			//e.printStackTrace();
			DBServerMainGuiStart.guiConsoleOut(e.getMessage());
			return null;
		}
	}

	public static PrivateKey createPrivateKeyFromByteData() {
		Path keyPath = Paths.get(KEY_DIR_PATH + "/" + PRIVATE_KEY);
		String sp = DBServerPropReader.getProperties("securityProvider").toString();
		String algorithm = DBServerPropReader.getProperties("securityAlgorithm").toString();
		Provider provider = Security.getProvider(sp);
		try (FileInputStream fis = new FileInputStream(keyPath.toFile())){
			long fileSize = Files.size(keyPath);
			byte[] readbuf = new byte[(int)fileSize];
			fis.read(readbuf);
			byte[] privateKeyByteData = Base64.getDecoder().decode(readbuf);
			PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKeyByteData);
			KeyFactory keyFactory = KeyFactory.getInstance(algorithm, provider);
			return keyFactory.generatePrivate(pkcs8EncodedKeySpec);
		} catch(IOException e) {
			DBServerMainGuiStart.guiConsoleOut(e.getMessage());
			return null;
		} catch(NoSuchAlgorithmException e) {
			DBServerMainGuiStart.guiConsoleOut(e.getMessage());
			return null;
		} catch(InvalidKeySpecException e) {
			DBServerMainGuiStart.guiConsoleOut(e.getMessage());
			return null;
		}
	}

	public static byte[] sign(PrivateKey privateKey, byte[] data) {
		String sp = DBServerPropReader.getProperties("securityProvider").toString();
		String algorithm = DBServerPropReader.getProperties("signatureAlgorithm").toString();
		Provider provider = Security.getProvider(sp);
		try {
			Signature signature = Signature.getInstance(algorithm, provider);
			signature.initSign(privateKey);
			signature.update(data);
			return signature.sign();
		} catch(NoSuchAlgorithmException e) {
			DBServerMainGuiStart.guiConsoleOut(e.getMessage());
			return null;
		} catch(InvalidKeyException e) {
			DBServerMainGuiStart.guiConsoleOut(e.getMessage());
			return null;
		} catch(SignatureException e) {
			DBServerMainGuiStart.guiConsoleOut(e.getMessage());
			return null;
		}
	}

	public static boolean verifiy(PublicKey publicKey, byte[] signData, byte[] checkData) {
		String sp = DBServerPropReader.getProperties("securityProvider").toString();
		String algorithm = DBServerPropReader.getProperties("signatureAlgorithm").toString();
		Provider provider = Security.getProvider(sp);
		try {
			Signature signature = Signature.getInstance(algorithm, provider);
			signature.initVerify(publicKey);
			signature.update(checkData);
			return signature.verify(signData);
		} catch(NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch(InvalidKeyException e) {
			e.printStackTrace();
		} catch(SignatureException e) {
			e.printStackTrace();
		}
		return false;
	}
}
