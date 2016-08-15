/**
 * CROSSFIRE JAPAN INCORPORATED
 * This source code is under GPL License.
 * info@crossfire.jp
 * Official web site
 * http://alinous.org
 * 
 *  Copyright (C) 2007 Tomohiro Iizuka
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.alinous.poi;

import java.io.IOException;
import java.io.OutputStream;

import org.alinous.cloud.file.AlinousFile;
import org.alinous.cloud.file.AlinousFileInputStream;
import org.alinous.cloud.file.AlinousFileOutputStream;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public class PoiManager
{
	private String path;
	private AlinousFileInputStream inStream;
	private POIFSFileSystem fs;
	private HSSFWorkbook wb;
	
	public PoiManager()
	{
	}
	
	public void open(String path) throws IOException
	{
		this.path = path;
		
		this.inStream = new AlinousFileInputStream(new AlinousFile(this.path));
		this.fs = new POIFSFileSystem(this.inStream);
		
		this.wb = new HSSFWorkbook(this.fs, false);

	} 
	
	public void write(String outPath) throws IOException
	{
		OutputStream fileOut = new AlinousFileOutputStream(new AlinousFile(outPath));

	    this.wb.write(fileOut);
	    
	    fileOut.close();

	}
	
	public void close()
	{		
		if(this.inStream != null){
			try {
				this.inStream.close();
			} catch (IOException e) {e.printStackTrace();}
			this.inStream = null;
		}
	}
	
	public String getValue(int col, int row, int sheetNum)
	{
		HSSFSheet sheet = this.wb.getSheetAt(sheetNum);
		
		HSSFRow hssfRow = sheet.getRow(row);
		if(hssfRow == null){
			hssfRow = sheet.createRow(row);
		}
		
		HSSFCell cell = hssfRow.getCell(col);
		if(cell == null){
			cell = hssfRow.createCell(col);
		}
		
		HSSFRichTextString text = cell.getRichStringCellValue();
		
		return text.getString();
	}
	
	public void setSellValue(int col, int row, int sheetNum, String value)
	{
		HSSFSheet sheet = this.wb.getSheetAt(sheetNum);
		
		HSSFRow hssfRow = sheet.getRow(row);
		if(hssfRow == null){
			hssfRow = sheet.createRow(row);
		}
		
		HSSFCell cell = hssfRow.getCell(col);
		if(cell == null){
			cell = hssfRow.createCell(col);
		}

		HSSFRichTextString text = new HSSFRichTextString(value);

		cell.setCellValue(text);
	}
	
	
}
