package org.alinous.parallel.exam;

public class SImpleOut {
	
	public void outTest()
	{
		for (int i = 0; i < 30; i++) {
			//scope.startExec(new String[]{"**************************aaaaa" + i});
			
			int x = SimpleOutEexc.job("*" + i);
			System.out.println(x);
		}
	}
	public static void main(String[] args) {
		new SImpleOut().outTest();
	}
}
