package KPC
/**
* @EMS Mar 2019
*
* 20190306 - a9ra5213 - Ricky Afriano - KPC UPGRADE
*            Initial Coding - Display TOW Supplier List into dropdown component in MSE140 Screen
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

public class TOW_SUPP_LIST extends GenericScriptPlugin implements GenericScriptExecuteForCollection{
   String version = "1";
		   
   InitialContext initial = new InitialContext()
   Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
   def sql = new Sql(CAISource)
   
   public GenericScriptResults executeForCollection(SecurityToken securityToken, RequestAttributes requestAttributes,
	   Integer maxNumberOfObjects, RestartAttributes restartAttributes) throws FatalException {
	   log.info("Exec for coll TOW_SUPP_LIST : " + version )
	   RequestAttributes reqAtt = requestAttributes
	   String strSearch = reqAtt.getAttributeStringValue("param");
	   String strCon = reqAtt.getAttributeStringValue("parTowCode");
	   def results = new GenericScriptResults();
	   String QryCon = "";
	   if (strCon.equals(null)) {
		   QryCon = " and trim(TABLE_CODE) = trim('') ";
	   }else {
		   QryCon = " and trim(TABLE_CODE) like trim('" +strCon+ "%') ";
	   }
	   String StrSQL = ""
	   
	   if(strSearch.equals(null)){
		   log.info("maxNumberOfObjects : " + maxNumberOfObjects );
		   
		   if (restartAttributes.equals(null)){
			   StrSQL = "SELECT row_number () over(order by TABLE_CODE) NO,substr(TABLE_CODE,6,6) TABLE_CODE,TABLE_DESC FROM msf010 " +
						   "where TABLE_TYPE= 'TOS' " +QryCon+
						   "ORDER BY TABLE_CODE OFFSET 0 ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY" ;
			   log.info ("StrSQL : " + StrSQL);
			   sql.eachRow(StrSQL, {
				   GenericScriptResult result = new GenericScriptResult()
				   result.addAttribute("resCode", it.TABLE_CODE);
				   result.addAttribute("resDesc", it.TABLE_DESC);
				   result.addAttribute("lastRow", maxNumberOfObjects.toString());
				   results.add(result);
			   })
		   }else {
			   log.info("restartAttributes : " + restartAttributes.getAttributeStringValue("lastRow") );
			   Integer MaxInst = Integer.parseInt(restartAttributes.getAttributeStringValue("lastRow"));
			   //MaxInst = MaxInst + maxNumberOfObjects
			   StrSQL = "SELECT row_number () over(order by TABLE_CODE) NO,substr(TABLE_CODE,6,6) TABLE_CODE,TABLE_DESC FROM msf010 " +
						   "where TABLE_TYPE= 'TOS' " +QryCon+
						   "ORDER BY TABLE_CODE OFFSET "+MaxInst.toString()+" ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY" ;
			   log.info ("StrSQL : " + StrSQL);
			   sql.eachRow(StrSQL, {
				   GenericScriptResult result = new GenericScriptResult();
				   MaxInst = it.NO
				   result.addAttribute("resCode", it.TABLE_CODE);
				   result.addAttribute("resDesc", it.TABLE_DESC);
				   result.addAttribute("lastRow", MaxInst.toString());
				   results.add(result);
			   })
		   }
	   }else{
		   
		   log.info("maxNumberOfObjects : " + maxNumberOfObjects );
		   
		   if (restartAttributes.equals(null)){
			   StrSQL = "SELECT row_number () over(order by TABLE_CODE) NO,substr(TABLE_CODE,6,6) TABLE_CODE,TABLE_DESC FROM msf010 " +
						   "where TABLE_TYPE= 'TOS' and ((upper(TABLE_CODE) like '%'||upper('"+strSearch+"')||'%') or (upper(TABLE_DESC) like '%'||upper('"+strSearch+"')||'%')) " +QryCon+
						   "ORDER BY TABLE_CODE OFFSET 0 ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY" ;
			   log.info ("StrSQL : " + StrSQL);
			   sql.eachRow(StrSQL, {
				   GenericScriptResult result = new GenericScriptResult();
				   result.addAttribute("resCode", it.TABLE_CODE);
				   result.addAttribute("resDesc", it.TABLE_DESC);
				   result.addAttribute("lastRow", maxNumberOfObjects.toString());
				   results.add(result);
			   })
		   }else {
			   log.info("restartAttributes : " + restartAttributes.getAttributeStringValue("lastRow") );
			   Integer MaxInst = Integer.parseInt(restartAttributes.getAttributeStringValue("lastRow"));
			   //MaxInst = MaxInst + maxNumberOfObjects
			   StrSQL = "SELECT row_number () over(order by TABLE_CODE) NO,substr(TABLE_CODE,6,6) TABLE_CODE,TABLE_DESC FROM msf010 " +
						   "where TABLE_TYPE= 'TOS' and ((upper(TABLE_CODE) like '%'||upper('"+strSearch+"')||'%') or (upper(TABLE_DESC) like '%'||upper('"+strSearch+"')||'%')) " +QryCon+
						   "ORDER BY TABLE_CODE OFFSET "+MaxInst.toString()+" ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY" ;
			   log.info ("StrSQL : " + StrSQL);
			   sql.eachRow(StrSQL, {
				   GenericScriptResult result = new GenericScriptResult();
				   MaxInst = it.NO
				   result.addAttribute("resCode", it.TABLE_CODE);
				   result.addAttribute("resDesc", it.TABLE_DESC);
				   result.addAttribute("lastRow", MaxInst.toString());
				   results.add(result);
			   })
		   }
	   }
	   
	   return results
   }
}
