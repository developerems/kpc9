package KPC
/**
* @EMS Des 2018
*
* 20181217 - a9ra5213 - Ricky Afriano - KPC UPGRADE
*            Initial Coding - Display Incoide data into dropdown component in ELL38I Screen
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

public class INV_LIST extends GenericScriptPlugin implements GenericScriptExecuteForCollection{
   String version = "1";
		   
   InitialContext initial = new InitialContext()
   Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
   def sql = new Sql(CAISource)
   
   public GenericScriptResults executeForCollection(SecurityToken securityToken, RequestAttributes requestAttributes,
	   Integer maxNumberOfObjects, RestartAttributes restartAttributes) throws FatalException {
	   log.info("Exec for coll INV_LIST : " + version )
	   RequestAttributes reqAtt = requestAttributes
	   String strSearch = reqAtt.getAttributeStringValue("param");
	   String strCon = reqAtt.getAttributeStringValue("parCntNo2");
	   def results = new GenericScriptResults();
	   String QryCon = "";
	   if (strCon.equals(null)) {
		   QryCon = "";
	   }else {
		   QryCon = " and trim(CONTRACT_NO) = trim('" +strCon+ "') ";
	   }
	   String StrSQL = ""
	   
	   if(strSearch.equals(null)){
		   log.info("maxNumberOfObjects : " + maxNumberOfObjects );
		   
		   if (restartAttributes.equals(null)){
			   StrSQL = "SELECT row_number () over(order by CIC_INVOICE) NO,CIC_INVOICE FROM ACA.KPF38I " +
						   "where DSTRCT_CODE = '"+securityToken.getDistrict()+"' " +QryCon+
						   "ORDER BY CIC_INVOICE OFFSET 0 ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY" ;
			   log.info ("StrSQL : " + StrSQL);
			   sql.eachRow(StrSQL, {
				   GenericScriptResult result = new GenericScriptResult()
				   result.addAttribute("resCode", it.CIC_INVOICE);
				   result.addAttribute("lastRow", maxNumberOfObjects.toString());
				   results.add(result);
			   })
		   }else {
			   log.info("restartAttributes : " + restartAttributes.getAttributeStringValue("lastRow") );
			   Integer MaxInst = Integer.parseInt(restartAttributes.getAttributeStringValue("lastRow"));
			   //MaxInst = MaxInst + maxNumberOfObjects
			   StrSQL = "SELECT row_number () over(order by CIC_INVOICE) NO,CIC_INVOICE FROM ACA.KPF38I " +
						   "where DSTRCT_CODE = '"+securityToken.getDistrict()+"' " +QryCon+
						   "ORDER BY CIC_INVOICE OFFSET "+MaxInst.toString()+" ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY" ;
			   log.info ("StrSQL : " + StrSQL);
			   sql.eachRow(StrSQL, {
				   GenericScriptResult result = new GenericScriptResult();
				   MaxInst = it.NO
				   result.addAttribute("resCode", it.CIC_INVOICE);
				   result.addAttribute("lastRow", MaxInst.toString());
				   results.add(result);
			   })
		   }
	   }else{
		   
		   log.info("maxNumberOfObjects : " + maxNumberOfObjects );
		   
		   if (restartAttributes.equals(null)){
			   StrSQL = "SELECT row_number () over(order by CIC_INVOICE) NO,CIC_INVOICE FROM ACA.KPF38I " +
						   "where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and ((upper(CIC_INVOICE) like '%'||upper('"+strSearch+"')||'%')) " +QryCon+
						   "ORDER BY CIC_INVOICE OFFSET 0 ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY" ;
			   log.info ("StrSQL : " + StrSQL);
			   sql.eachRow(StrSQL, {
				   GenericScriptResult result = new GenericScriptResult();
				   result.addAttribute("resCode", it.CIC_INVOICE);
				   result.addAttribute("lastRow", maxNumberOfObjects.toString());
				   results.add(result);
			   })
		   }else {
			   log.info("restartAttributes : " + restartAttributes.getAttributeStringValue("lastRow") );
			   Integer MaxInst = Integer.parseInt(restartAttributes.getAttributeStringValue("lastRow"));
			   //MaxInst = MaxInst + maxNumberOfObjects
			   StrSQL = "SELECT row_number () over(order by CIC_INVOICE) NO,CIC_INVOICE FROM ACA.KPF38I " +
						   "where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and ((upper(CIC_INVOICE) like '%'||upper('"+strSearch+"')||'%')) " +QryCon+
						   "ORDER BY CIC_INVOICE OFFSET "+MaxInst.toString()+" ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY" ;
			   log.info ("StrSQL : " + StrSQL);
			   sql.eachRow(StrSQL, {
				   GenericScriptResult result = new GenericScriptResult();
				   MaxInst = it.NO
				   result.addAttribute("resCode", it.CIC_INVOICE);
				   result.addAttribute("lastRow", MaxInst.toString());
				   results.add(result);
			   })
		   }
	   }
	   
	   return results
   }
}
