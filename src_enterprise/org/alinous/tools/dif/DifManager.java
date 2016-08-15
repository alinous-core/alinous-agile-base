package org.alinous.tools.dif;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.alinous.cloud.file.AlinousFile;
import org.alinous.cloud.file.AlinousFileInputStream;
import org.alinous.cloud.file.AlinousFileOutputStream;

public class DifManager {
	
	
	public static void main(String[] args) throws IOException {
		
	/*	convert("/work/kudo/adres1（顧客情報）.dif", "/work/kudo/adres1_cust.csv");
		convert("/work/kudo/adres1(顧客情報店舗用）.dif", "/work/kudo/adres1_retail.csv");
		convert("/work/kudo/icbalmie（商品原価）.dif", "/work/kudo/icbalmie.csv");
		convert("/work/kudo/icprt1（商品マスタ）.dif", "/work/kudo/icprt1.csv");
		convert("/work/kudo/oeindlic（送料）.dif", "/work/kudo/oeindlic.csv");
		convert("/work/kudo/oeindlid（伝票明細）.dif", "/work/kudo/oeindlid.csv");		
		convert("/work/kudo/oeinh（伝票ヘッダ）.dif", "/work/kudo/oeinh.csv");
		convert("/work/kudo/oeprc1（価格情報）.dif", "/work/kudo/oeprc1.csv");*/

		//convert("/work/kudo/adres1.dif", "/work/kudo/adres1_cust.csv");
		//convert("/work/kudo/20101201/sls_wk_p.dif", "/work/kudo/20101201/oeinh.csv");
		//convert("/work/kudo/20101201/sls_wk_pi.dif", "/work/kudo/20101201/oeindlid.csv");
		convert("/work/kudo/20110120/icprt1.dif", "/work/kudo/20110120/icprt1.csv");
	}
	
	private static void convert(String inFile, String outFile) throws IOException
	{
		String encode = "MS932";
		
		AlinousFileInputStream stream = new AlinousFileInputStream(new AlinousFile(inFile));
		
		OutputStream outStream = new AlinousFileOutputStream(new AlinousFile(outFile));
		OutputStreamWriter writer = new OutputStreamWriter(outStream, "utf-8");
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, encode));
		
		DifManager mgr = new DifManager();
		
		// TABLE
		mgr.readHeader(reader);
		
		// VECTORS
		mgr.readHeader(reader);
		
		// TUPLES
		mgr.readHeader(reader);
		
		// DATA
		mgr.readHeader(reader);
		
		// First BOT
		mgr.readField(reader);
		
		// rows
		DifRow row = mgr.readrow(reader);
		while(!row.isEndOfData() && !row.isEodNext()){
			System.out.println(row.toString());
			writer.append(row.toString());
			writer.append("\n");
			
			
			row = mgr.readrow(reader);
		}
		
		if(row.isEodNext()){
			System.out.println(row.toString());
			writer.append(row.toString());
			writer.append("\n");
		}
		
		reader.close();
		stream.close();
		
		writer.close();
		outStream.close();
	}
	
	public DifRow readrow(BufferedReader reader) throws IOException
	{
		DifRow row = new DifRow();
		AbstractDifDataField fld = readField(reader);
		
		// 最初のフィールドがEODの場合
		if(fld instanceof EndOfDataField){
			row.addColumn(fld);
			return row;
		}
		
		// BOTか、EOD が出るまで読み続ける
		while(!(fld instanceof EndOfDataField) && !(fld instanceof BotField))
		{
			row.addColumn(fld);
			fld = readField(reader);
		}
		
		if(fld instanceof EndOfDataField){
			row.setEodNext(true);
		}
		
		
		return row;
	}
	
	public AbstractDifDataField readField(BufferedReader reader) throws IOException
	{
		String first = reader.readLine();
		String second = reader.readLine();
		
		AbstractDifDataField fld = AbstractDifDataField.createDataField(first, second);
		
		return fld;
	}
	
	public void readHeader(BufferedReader reader) throws IOException
	{
		String line = reader.readLine();
		if(!line.equals("TABLE") && !line.equals("VECTORS") && !line.equals("TUPLES") && !line.equals("DATA")){
			return;
		}
		reader.readLine();
		reader.readLine();
	}
	
}
