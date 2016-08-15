package org.alinous.plugin.xa;

public class AlinousRegisterdGlobalId {
	private String dataSource;
	private String globalTrxId;
	
	public AlinousRegisterdGlobalId(String dataSource, String globalTrxId)
	{
		this.dataSource = dataSource;
		this.globalTrxId = globalTrxId;
	}

	public String getDataSource() {
		return dataSource;
	}

	public String getGlobalTrxId() {
		return globalTrxId;
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof AlinousRegisterdGlobalId)){
			return false;
		}
		
		AlinousRegisterdGlobalId gid = (AlinousRegisterdGlobalId) obj;
		
		return this.dataSource.equals(gid.dataSource) && this.globalTrxId.equals(gid.globalTrxId);
	}

	@Override
	public int hashCode() {
		return (this.dataSource + "_" + this.globalTrxId).hashCode();
	}
	
	
}
