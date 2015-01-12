package mobi.esys.fileworks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class FileWorks {
	private transient String filePath;

	public FileWorks(String filePath) {
		super();
		this.filePath = filePath;
	}

	public void createFile() {
		File file = new File(filePath);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
			}
		}
	}

	public void deleteFile() {
		File file = new File(filePath);
		if (!Arrays.asList(file.getName()).contains("dd")) {
			file.delete();
		}
	}

	private static byte[] createChecksum(String filename) {
		InputStream fis;
		byte[] sum = new byte[1];
		try {
			fis = new FileInputStream(filename);

			byte[] buffer = new byte[1024];
			MessageDigest complete = MessageDigest.getInstance("MD5");
			int numRead;

			do {
				numRead = fis.read(buffer);
				if (numRead > 0) {
					complete.update(buffer, 0, numRead);
				}
			} while (numRead != -1);

			fis.close();
			sum = complete.digest();

		} catch (FileNotFoundException e) {
		} catch (NoSuchAlgorithmException e) {
		} catch (IOException e) {
		}

		return sum;
	}

	public String getFileMD5() {
		byte[] b;

		b = createChecksum(filePath);

		String result = "";

		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

	public String getFileName() {
		File file = new File(filePath);
		return file.getName();
	}
}
