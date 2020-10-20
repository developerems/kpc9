package KPC
/**
 * @EMS 2019
 *
 * 20190901 - a9ra5213 - Ricky Afriano - KPC UPGRADE Ellipse 8
 *            Initial Coding - Custom Manual Depreciation 
 **/
import java.util.List

import javax.naming.InitialContext

import com.mincom.ellipse.app.security.SecurityToken
import com.mincom.ellipse.errors.exceptions.FatalException
import com.mincom.ellipse.script.plugin.GenericScriptExecuteForCollection
import com.mincom.ellipse.script.plugin.GenericScriptExecute
import com.mincom.ellipse.script.plugin.GenericScriptCreate
import com.mincom.ellipse.script.plugin.GenericScriptUpdate
import com.mincom.ellipse.script.plugin.GenericScriptDelete
import com.mincom.ellipse.script.plugin.GenericScriptPlugin
import com.mincom.ellipse.script.plugin.GenericScriptResult
import com.mincom.ellipse.script.plugin.GenericScriptResults
import com.mincom.ellipse.script.plugin.RequestAttributes
import com.mincom.ellipse.script.plugin.RestartAttributes
import com.mincom.ellipse.script.util.CommAreaScriptWrapper;

import groovy.sql.Sql

import com.mincom.eql.impl.*
import com.mincom.eql.*
import com.mincom.enterpriseservice.ellipse.*
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import com.mincom.ellipse.*

public class PeriodList extends GenericScriptPlugin implements GenericScriptExecuteForCollection{
   String version = "1";
		   
   InitialContext initial = new InitialContext()
   Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
   def sql = new Sql(CAISource)
   
   public GenericScriptResults executeForCollection(SecurityToken securityToken, RequestAttributes requestAttributes,
	   Integer maxNumberOfObjects, RestartAttributes restartAttributes) throws FatalException {
	   log.info("Exec for coll PeriodList : " + version )
	   RequestAttributes reqAtt = requestAttributes
	   String strSearch = reqAtt.getAttributeStringValue("parAtt");
	   def results = new GenericScriptResults();
	   
	   String StrSQL = ""
	   
	   if(strSearch.equals(null)){
		   log.info("maxNumberOfObjects : " + maxNumberOfObjects );
		   
		   if (restartAttributes.equals(null)){
			   StrSQL = "select row_number () over(order by period) NO,period from (with t as (select date '2004-01-01' start_date,date '2200-01-01' end_date from dual) " +
							"select  to_char(add_months(trunc(start_date,'mm'),level - 1),'yyyy') || to_char(add_months(trunc(start_date,'mm'),level - 1),'mm') period " +
							"from  t " +
							"connect by trunc(end_date,'mm') >= add_months(trunc(start_date,'mm'),level - 1)) " +
						   "ORDER BY period OFFSET 0 ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY " ;
			   log.info ("StrSQL : " + StrSQL);
			   sql.eachRow(StrSQL, {
				   GenericScriptResult result = new GenericScriptResult()
				   result.addAttribute("resCode", it.period);
				   result.addAttribute("resDesc", "");
				   result.addAttribute("LAST_ROW", maxNumberOfObjects.toString());
				   results.add(result);
			   })
		   }else {
			   log.info("restartAttributes : " + restartAttributes.getAttributeStringValue("LAST_ROW") );
			   Integer MaxInst = Integer.parseInt(restartAttributes.getAttributeStringValue("LAST_ROW"));
			   //MaxInst = MaxInst + maxNumberOfObjects
			   StrSQL = "select row_number () over(order by period) NO,period from (with t as (select date '2004-01-01' start_date,date '2200-01-01' end_date from dual) " +
							"select  to_char(add_months(trunc(start_date,'mm'),level - 1),'yyyy') || to_char(add_months(trunc(start_date,'mm'),level - 1),'mm') period " +
							"from  t " +
							"connect by trunc(end_date,'mm') >= add_months(trunc(start_date,'mm'),level - 1)) " +
						   "ORDER BY period OFFSET "+MaxInst.toString()+" ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY " ;
			   log.info ("StrSQL : " + StrSQL);
			   sql.eachRow(StrSQL, {
				   GenericScriptResult result = new GenericScriptResult();
				   MaxInst = it.NO
				   result.addAttribute("resCode", it.period);
				   result.addAttribute("resDesc", "");
				   result.addAttribute("LAST_ROW", MaxInst.toString());
				   results.add(result);
			   })
		   }
	   }else{
		   
		   log.info("maxNumberOfObjects : " + maxNumberOfObjects );
		   
		   if (restartAttributes.equals(null)){
			   StrSQL = "select row_number () over(order by period) NO,period from (with t as (select date '2004-01-01' start_date,date '2200-01-01' end_date from dual) " +
							"select  to_char(add_months(trunc(start_date,'mm'),level - 1),'yyyy') || to_char(add_months(trunc(start_date,'mm'),level - 1),'mm') period " +
							"from  t " +
							"connect by trunc(end_date,'mm') >= add_months(trunc(start_date,'mm'),level - 1)) " +
						   "where ((upper(period) like '%'||upper('"+strSearch+"')||'%')) " +
						   "ORDER BY period OFFSET 0 ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY " ;
			   log.info ("StrSQL : " + StrSQL);
			   sql.eachRow(StrSQL, {
				   GenericScriptResult result = new GenericScriptResult();
				   result.addAttribute("resCode", it.period);
				   result.addAttribute("resDesc", "");
				   result.addAttribute("LAST_ROW", maxNumberOfObjects.toString());
				   results.add(result);
			   })
		   }else {
			   log.info("restartAttributes : " + restartAttributes.getAttributeStringValue("LAST_ROW") );
			   Integer MaxInst = Integer.parseInt(restartAttributes.getAttributeStringValue("LAST_ROW"));
			   //MaxInst = MaxInst + maxNumberOfObjects
			   StrSQL = "select row_number () over(order by period) NO,period from (with t as (select date '2004-01-01' start_date,date '2200-01-01' end_date from dual) " +
							"select  to_char(add_months(trunc(start_date,'mm'),level - 1),'yyyy') || to_char(add_months(trunc(start_date,'mm'),level - 1),'mm') period " +
							"from  t " +
							"connect by trunc(end_date,'mm') >= add_months(trunc(start_date,'mm'),level - 1)) " +
						   "where ((upper(period) like '%'||upper('"+strSearch+"')||'%')) " +
						   "ORDER BY period OFFSET "+MaxInst.toString()+" ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY " ;
			   log.info ("StrSQL : " + StrSQL);
			   sql.eachRow(StrSQL, {
				   GenericScriptResult result = new GenericScriptResult();
				   MaxInst = it.NO
				   result.addAttribute("resCode", it.period);
				   result.addAttribute("resDesc", "");
				   result.addAttribute("LAST_ROW", MaxInst.toString());
				   results.add(result);
			   })
		   }
	   }
	   
	   return results
   }
}
