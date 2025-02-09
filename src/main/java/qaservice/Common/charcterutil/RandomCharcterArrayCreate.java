package qaservice.Common.charcterutil;

import java.util.Random;

public class RandomCharcterArrayCreate {
	private static final String USEDCHARCTERCODE = "abcdefghijklmnopqrstuvwxyzABCEEFGHIJKLMNOPQRSTUVWXYZ0123456789-?!#$%&+*";
	public static char[] createCharcterArray(int charcterCount) {
		Random r = new Random();
		char[] retChar = new char[charcterCount];
		int len = USEDCHARCTERCODE.length();
		for(int i = 0; i < charcterCount; i++) {
			int index = r.nextInt(len);
			retChar[i] = USEDCHARCTERCODE.charAt(index);
		}
		return retChar;
	}
}
