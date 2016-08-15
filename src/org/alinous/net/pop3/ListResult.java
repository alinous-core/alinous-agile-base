package org.alinous.net.pop3;

public class ListResult
{
	private int offset;
	private int bytes;
	
	public ListResult()
	{
		
	}
	public ListResult(int offset, int bytes)
	{
		this.offset = offset;
		this.bytes = bytes;
	}
	
	public int getOffset()
	{
		return offset;
	}
	public void setOffset(int offset)
	{
		this.offset = offset;
	}
	public int getBytes()
	{
		return bytes;
	}
	public void setBytes(int bytes)
	{
		this.bytes = bytes;
	}
	
	
}
