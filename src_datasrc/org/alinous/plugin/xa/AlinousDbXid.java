package org.alinous.plugin.xa;

import javax.transaction.xa.Xid;

public class AlinousDbXid implements Xid {
	private int formatId;
	private byte[] globalId;
	private byte[] branchId;
	
	public AlinousDbXid(int formatId, byte[] globalId, byte[] branchId)
	{
		this.formatId = formatId;
		
		this.globalId = new byte[globalId.length];
		System.arraycopy(globalId, 0, this.globalId, 0, globalId.length);
		
		this.branchId = new byte[branchId.length];
		System.arraycopy(branchId, 0, this.branchId, 0, branchId.length);
	}
	
	@Override
	public byte[] getBranchQualifier() {
		return this.branchId;
	}

	@Override
	public int getFormatId() {
		return this.formatId;
	}

	@Override
	public byte[] getGlobalTransactionId() {
		return this.globalId;
	}

}
