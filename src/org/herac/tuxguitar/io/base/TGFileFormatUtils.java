package org.herac.tuxguitar.io.base;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class TGFileFormatUtils {
	
	public static String getFileExtension(String path) {
		return getFileExtension(path, null);
	}

	public static String getFileExtension(String path, String defaultValue) {
		int index = path.lastIndexOf(".");
		if( index > 0 ){
			return path.substring(index);
		}
		return defaultValue;
	}
	
	public static String getFileFormatCode(String path) {
		return getFileFormatCode(path, null);
	}
	
	public static String getFileFormatCode(String path, String defaultValue) {
		String extension = getFileExtension(path);
		if( extension != null && extension.length() > 1 ) {
			return extension.substring(1);
		}
		return defaultValue;
	}
	
	public static byte[] getBytes(InputStream in)throws Throwable {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int read;
		while((read = in.read()) != -1){
			out.write(read);
		}
		byte[] bytes = out.toByteArray();
		in.close();
		out.close();
		out.flush();
		return bytes;
	}
}
