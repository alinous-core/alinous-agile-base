package org.alinous.expections;

public class AlinousStackTraceElement {
	private int line;
	private String filePath;
	
	public AlinousStackTraceElement()
	{
		
	}
	
	public AlinousStackTraceElement(int line, String filePath)
	{
		this.line = line;
		this.filePath = filePath;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof AlinousStackTraceElement){
			AlinousStackTraceElement tr = (AlinousStackTraceElement)obj;
			return tr.getFilePath().equals(this.filePath) && tr.getLine() == this.line;
		}
		
		
		return super.equals(obj);
	}

	public int getLine() {
		return line;
	}
	public void setLine(int line) {
		this.line = line;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		if(filePath == null){
			throw new NullPointerException();
		}
		
		this.filePath = filePath;
	}
	
	
}
