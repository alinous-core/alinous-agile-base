package org.alinous.exec.check;

public class IncludeCheck {
	private int line;
	private int linePosition;
	private String filePath;
	private String includeModule;
	private String originalInclude;
	
	
	private boolean used;

	public boolean isUsed()
	{
		return used;
	}

	public void setUsed(boolean used)
	{
		this.used = used;
	}

	public int getLine()
	{
		return line;
	}

	public void setLine(int line)
	{
		this.line = line;
	}

	public int getLinePosition()
	{
		return linePosition;
	}

	public void setLinePosition(int linePosition)
	{
		this.linePosition = linePosition;
	}

	public String getFilePath()
	{
		return filePath;
	}

	public void setFilePath(String filePath)
	{
		this.filePath = filePath;
	}

	public String getIncludeModule()
	{
		return includeModule;
	}

	public void setIncludeModule(String includeModule)
	{
		this.includeModule = includeModule;
	}

	public String getOriginalInclude()
	{
		return originalInclude;
	}

	public void setOriginalInclude(String originalInclude)
	{
		this.originalInclude = originalInclude;
	}
	
	
}
