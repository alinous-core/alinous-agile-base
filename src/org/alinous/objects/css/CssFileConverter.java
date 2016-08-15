package org.alinous.objects.css;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class CssFileConverter {
	
	public static void main(String[] args) {
		String absPath = "src/org/alinous/objects/css/jquery.ui.theme.css";
		
		String replaceBase = "/admin/jquery/";
		
		String result = convert(absPath, replaceBase);
		
		System.out.println(result);
	}
	
	public static String convert(String absPath, String replaceBase)
	{
		String css = readAllText(absPath, "utf-8");
		
		CssUrlConverter converter = new CssUrlConverter(css, replaceBase);
		String afterCss = converter.convert();
		
		return afterCss;
	}
	
	public static String readAllText(String srcPath, String encode)
	{
		StringBuffer buff = new StringBuffer();
		
		File file = new File(srcPath);
		FileInputStream fStream = null;
		InputStreamReader reader = null;
		try {
			fStream = new FileInputStream(file);
			reader = new InputStreamReader(fStream, encode);
			
			char readBuffer[] = new char[1024];
			int n = 1;
			
			while(n > 0){
				n = reader.read(readBuffer, 0, readBuffer.length);
				
				if(n > 0){
					buff.append(readBuffer, 0, n);
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally{
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e) {e.printStackTrace();}
			}
			if(fStream != null){
				try {
					fStream.close();
				} catch (IOException e) {e.printStackTrace();}
			}
		}
		
		return buff.toString();	
	}
}
