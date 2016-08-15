package org.alinous.script.sql;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.alinous.script.sql.condition.AndExpression;
import org.alinous.script.sql.condition.BetweenClauseExpression;
import org.alinous.script.sql.condition.ISQLExpression;
import org.alinous.script.sql.condition.InClauseExpression;
import org.alinous.script.sql.condition.IsNullClauseExpression;
import org.alinous.script.sql.condition.LikeExpression;
import org.alinous.script.sql.condition.OrExpression;
import org.alinous.script.sql.condition.ParenthesisExpression;
import org.alinous.script.sql.condition.SQLNotExpression;
import org.alinous.script.sql.condition.TwoClauseExpression;
import org.alinous.script.sql.statement.ColumnIdentifier;
import org.alinous.script.sql.statement.ISQLStatement;
import org.alinous.script.sql.statement.SQLStatement;

public class ExpressionChecker
{
	public static void checkoutSqlExpression(List<DmlCheckListElement> checkList, ISQLExpression exp, String defaultTable)
	{
		if(exp instanceof BetweenClauseExpression){
			
		}
		else if(exp instanceof InClauseExpression){
			
		}
		else if(exp instanceof IsNullClauseExpression){
			
		}
		else if(exp instanceof LikeExpression){
			
		}
		else if(exp instanceof TwoClauseExpression){
			TwoClauseExpression tw = (TwoClauseExpression)exp;
			handleTwoClauseExpression(checkList, tw, defaultTable);
		}
		
		else if(exp instanceof AndExpression){
			Iterator<ISQLExpression> it = ((AndExpression)exp).getExpressions().iterator();
			while(it.hasNext()){
				ISQLExpression chexp = it.next();
				checkoutSqlExpression(checkList, chexp, defaultTable);
			}
		}
		else if(exp instanceof OrExpression){
			Iterator<ISQLExpression> it = ((OrExpression)exp).getExpressions().iterator();
			while(it.hasNext()){
				ISQLExpression chexp = it.next();
				checkoutSqlExpression(checkList, chexp, defaultTable);
			}
		}
		else if(exp instanceof ParenthesisExpression){
			Iterator<ISQLExpression> it = ((ParenthesisExpression)exp).getExpressions().iterator();
			while(it.hasNext()){
				ISQLExpression chexp = it.next();
				checkoutSqlExpression(checkList, chexp, defaultTable);
			}
		}
		else if(exp instanceof SQLNotExpression){
			checkoutSqlExpression(checkList, ((SQLNotExpression)exp).getExpression(), defaultTable);
		}
	}
	
	private static void handleTwoClauseExpression(List<DmlCheckListElement> checkList, TwoClauseExpression tw, String defaultTable)
	{
		ISQLStatement st = tw.getLeft();
		if(st instanceof SQLStatement){
			SQLStatement stmt = (SQLStatement)st;
			
			ISQLStatement fst = stmt.getFirstStmt();
			if(fst instanceof ColumnIdentifier){
				ColumnIdentifier cid = (ColumnIdentifier) fst;
				
				String columnName = cid.getColumnName();
				
				String tableName = cid.getTableName();
				if(tableName == null){
					tableName = defaultTable;
				}
				
				DmlMatchRequest matchReq = new DmlMatchRequest(tableName, columnName);
				
				checkAndDeleteFromList(checkList, matchReq);
			}
		}
		
		st = tw.getRight();
		if(st instanceof SQLStatement){
			SQLStatement stmt = (SQLStatement)st;
			
			ISQLStatement fst = stmt.getFirstStmt();
			if(fst instanceof ColumnIdentifier){
				ColumnIdentifier cid = (ColumnIdentifier) fst;
				
				String columnName = cid.getColumnName();
				
				String tableName = cid.getTableName();
				if(tableName == null){
					tableName = defaultTable;
				}
				
				DmlMatchRequest matchReq = new DmlMatchRequest(tableName, columnName);
				
				checkAndDeleteFromList(checkList, matchReq);
			}
		}
	}
	
	private static void checkAndDeleteFromList(List<DmlCheckListElement> checkList, DmlMatchRequest matchReq)
	{
		LinkedList<DmlCheckListElement> delList = new LinkedList<DmlCheckListElement>();
		
		Iterator<DmlCheckListElement> it = checkList.iterator();
		while(it.hasNext()){
			DmlCheckListElement el = it.next();
			
			el.handleMatchRequest(matchReq);
			
			boolean b = el.isDeletable();
			
			if(b){
				delList.add(el);
			}
		}
		
		it = delList.iterator();
		while(it.hasNext()){
			DmlCheckListElement el = it.next();
			checkList.remove(el);
		}
	}
}
