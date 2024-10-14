package qaservice.WebServer.webSocket;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import qaservice.Common.charcterutil.CharUtil;

class WebSocketMessageUtil {
	static byte[] messageDecoder(DataInputStream dis) throws IOException {
		byte first = dis.readByte();
		byte second = dis.readByte();
		byte opCode = (byte) (first & 0x0F);
		// Check the opCode value
		if (opCode == 1) {
			//System.out.println("Start Decode");
		} else if (opCode == 8) {
			return null;
		}
		// Mask Check
		boolean masked = ((second & 0x80) != 0);
		if (!masked) {
			return null;
		}
		
		long length = second & 0x7F;
		if (length == 126) {
			length = dis.readUnsignedShort();
		} else if (length == 127) {
			length = dis.readLong();
		}
		//System.out.println("Message Length: " + length);
		// Read mask
		byte[] mask = new byte[4];
		dis.read(mask);
		// decoder
		byte[] encodedCharArray = new byte[(int) length];
		dis.read(encodedCharArray);
		// decode byte
		byte[] decodeByteArray = new byte[(int) length];
		StringBuilder decoded = new StringBuilder();
		for (int i = 0; i < encodedCharArray.length; i++) {
			//char decodedChar = (char) (encodedCharArray[i] ^ mask[i % 4]);
			decodeByteArray[i] = (byte)(encodedCharArray[i] ^ mask[i % 4]);
			//decoded.append(decodedChar);
		}
		//System.out.println("Decode Result  " + decoded.toString());
		//return decoded.toString(CharUtil.getCharset());
		//return new String(decodeByteArray, CharUtil.getCharset());
		return decodeByteArray;
	}

	static byte[] createSendMessage(byte[] data) {
		int headLen = 1;
		if(data.length < 126) {
			headLen += 1;
		} else if(data.length <= (Short.MAX_VALUE - Short.MIN_VALUE)) {
			headLen += 3;
		} else if (data.length <= Long.MAX_VALUE) {
			headLen += 9;
		}
		try(ByteArrayOutputStream byteArray = new ByteArrayOutputStream((data.length + headLen));
			DataOutputStream dos = new DataOutputStream(byteArray)) {
			//0x81 = -127;
			dos.writeByte(0x81);
			if(headLen == 2) {
				dos.writeByte(data.length);
			} else if(headLen == 4) {
				dos.writeByte(126);
				dos.writeShort(data.length);
			} else if(headLen == 10) {
				dos.writeByte(127);
				dos.writeLong(data.length);
			}
			dos.write(data);
			dos.flush();
			return byteArray.toByteArray();
		} catch(IOException e) {
			System.err.println("write data error");
			e.printStackTrace();
		}
		return null;
	}
}
