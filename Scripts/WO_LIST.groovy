package KPC
/**
* @EMS Des 2018
*
* 20181217 - a9ra5213 - Ricky Afriano - KPC UPGRADE
*            Initial Coding - Display WO data into dropdown component in ELL38C Screen
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

public class WO_LIST extends GenericScriptPlugin implements GenericScriptExecuteForCollection{
   String version = "1";
		   
   InitialContext initial = new InitialContext()
   Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
   def sql = new Sql(CAISource)
   
   public GenericScriptResults executeForCollection(SecurityToken securityToken, RequestAttributes requestAttributes,
	   Integer maxNumberOfObjects, RestartAttributes restartAttributes) throws FatalException {
	   log.info("Exec for coll WO_LIST : " + version )
	   RequestAttributes reqAtt = requestAttributes
	   String strSearch = reqAtt.getAttributeStringValue("param");
	   String strCon = reqAtt.getAttributeStringValue("parCntNo");
	   def results = new GenericScriptResults();
	   String QryCon = "";
	   if (strCon.equals(null)) {
		   QryCon = "";
	   }else {
		   QryCon = " and trim(ORIG_DOC_NO) = trim('" +strCon+ "') ";
	   }
	   String StrSQL = ""
	   
	   if(strSearch.equals(null)){
		   log.info("maxNumberOfObjects : " + maxNumberOfObjects );
		   
		   if (restartAttributes.equals(null)){
			   StrSQL = "select row_number () over(order by WORK_ORDER) NO,WORK_ORDER,WO_DESC from msf620 " +
						   "where DSTRCT_CODE = '"+securityToken.getDistrict()+"' " +QryCon+
						   "ORDER BY WORK_ORDER,WO_DESC OFFSET 0 ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY" ;
			   log.info ("StrSQL : " + StrSQL);
			   sql.eachRow(StrSQL, {
				   GenericScriptResult result = new GenericScriptResult();
				   result.addAttribute("resCode", it.WORK_ORDER);
				   result.addAttribute("resDesc", it.WO_DESC);
				   result.addAttribute("lastRow", maxNumberOfObjects.toString());
				   results.add(result);
			   })
		   }else {
			   log.info("restartAttributes : " + restartAttributes.getAttributeStringValue("lastRow") );
			   Integer MaxInst = Integer.parseInt(restartAttributes.getAttributeStringValue("lastRow"));
			   //MaxInst = MaxInst + maxNumberOfObjects
			   StrSQL = "select row_number () over(order by WORK_ORDER) NO,WORK_ORDER,WO_DESC from msf620 " +
						   "where DSTRCT_CODE = '"+securityToken.getDistrict()+"' " +QryCon+
						   "ORDER BY WORK_ORDER,WO_DESC OFFSET "+MaxInst.toString()+" ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY" ;
			   log.info ("StrSQL : " + StrSQL);
			   sql.eachRow(StrSQL, {
				   GenericScriptResult result = new GenericScriptResult();
				   MaxInst = it.NO
				   result.addAttribute("resCode", it.WORK_ORDER);
				   result.addAttribute("resDesc", it.WO_DESC);
				   result.addAttribute("lastRow", MaxInst.toString());
				   results.add(result);
			   })
		   }
	   }else{
		   
		   log.info("maxNumberOfObjects : " + maxNumberOfObjects );
		   
		   if (restartAttributes.equals(null)){
			   StrSQL = "select row_number () over(order by WORK_ORDER) NO,WORK_ORDER,WO_DESC from msf620 " +
						   "where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and (upper(WORK_ORDER) like '%'||upper('"+strSearch+"')||'%') or (upper(WO_DESC) like '%'||upper('"+strSearch+"')||'%') " +QryCon+
						   "ORDER BY WORK_ORDER,WO_DESC OFFSET 0 ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY" ;
			   log.info ("StrSQL : " + StrSQL);
			   sql.eachRow(StrSQL, {
				   GenericScriptResult result = new GenericScriptResult();
				   result.addAttribute("resCode", it.WORK_ORDER);
				   result.addAttribute("resDesc", it.WO_DESC);
				   result.addAttribute("lastRow", maxNumberOfObjects.toString());
				   results.add(result);
			   })
		   }else {
			   log.info("restartAttributes : " + restartAttributes.getAttributeStringValue("lastRow") );
			   Integer MaxInst = Integer.parseInt(restartAttributes.getAttributeStringValue("lastRow"));
			   //MaxInst = MaxInst + maxNumberOfObjects
			   StrSQL = "select row_number () over(order by WORK_ORDER) NO,WORK_ORDER,WO_DESC from msf620 " +
						   "where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and (upper(WORK_ORDER) like '%'||upper('"+strSearch+"')||'%') or (upper(WO_DESC) like '%'||upper('"+strSearch+"')||'%') " +QryCon+
						   "ORDER BY WORK_ORDER,WO_DESC OFFSET "+MaxInst.toString()+" ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY" ;
			   log.info ("StrSQL : " + StrSQL);
			   sql.eachRow(StrSQL, {
				   GenericScriptResult result = new GenericScriptResult();
				   MaxInst = it.NO
				   result.addAttribute("resCode", it.WORK_ORDER);
				   result.addAttribute("resDesc", it.WO_DESC);
				   result.addAttribute("lastRow", MaxInst.toString());
				   results.add(result);
			   })
		   }
	   }
	   
	   return results
   }
}
