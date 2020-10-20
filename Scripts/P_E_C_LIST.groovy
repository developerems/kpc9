package KPC
/**
* @EMS Des 2018
*
* 20181217 - a9ra5213 - Ricky Afriano - KPC UPGRADE
*            Initial Coding - Display Portion, Element and Category Description in ELL38C 
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

public class P_E_C_LIST extends GenericScriptPlugin implements GenericScriptExecuteForCollection{
   String version = "1";
		   
   InitialContext initial = new InitialContext()
   Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
   def sql = new Sql(CAISource)
   
   public GenericScriptResults executeForCollection(SecurityToken securityToken, RequestAttributes requestAttributes,
	   Integer maxNumberOfObjects, RestartAttributes restartAttributes) throws FatalException {
	   log.info("Exec for coll P_E_C_LIST : " + version )
	   RequestAttributes reqAtt = requestAttributes
	   String strSearch = reqAtt.getAttributeStringValue("param");
	   String strCode_P = reqAtt.getAttributeStringValue("codeP");
	   String strCode_E = reqAtt.getAttributeStringValue("codeE");
	   String strCode_C = reqAtt.getAttributeStringValue("codeC");
	   
	   String CNT_NO = "";
	   if (reqAtt.getAttributeStringValue("parGrdCntNo").equals(null)) {
		   CNT_NO = reqAtt.getAttributeStringValue("cntNo");
	   }else {
		   CNT_NO = reqAtt.getAttributeStringValue("parGrdCntNo");
	   }
	   
	   String CIC_NO = "";
	   if (reqAtt.getAttributeStringValue("parGrdCicNo").equals(null)) {
		   CIC_NO = reqAtt.getAttributeStringValue("cicNo");
	   }else {
		   CIC_NO = reqAtt.getAttributeStringValue("parGrdCicNo");
	   }
	   
	   def results = new GenericScriptResults();
	   
	   String WCLAUSE = "";
	   
	   if (strSearch.equals("P")) {
		   WCLAUSE = " and PORTION_NO = trim('"+strCode_P+"')";
		   def QRY1;
		   QRY1 = sql.firstRow("select PORTION_NO,PORTION_DESC from msf385 " +
		   "where upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) " + WCLAUSE);
		   GenericScriptResult result = new GenericScriptResult()
		   result.addAttribute("resCode", QRY1.PORTION_NO);
		   result.addAttribute("resDesc", QRY1.PORTION_DESC);
		   results.add(result);
	   }else if (strSearch.equals("E")) {
		   WCLAUSE = " and PORTION_NO = trim('"+strCode_P+"') and ELEMENT_NO = trim('"+strCode_E+"')";
		   def QRY1;
		   QRY1 = sql.firstRow("select ELEMENT_NO,ELEMENT_DESC from msf386 " +
		   "where upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) " + WCLAUSE);
		   GenericScriptResult result = new GenericScriptResult()
		   result.addAttribute("resCode", QRY1.ELEMENT_NO);
		   result.addAttribute("resDesc", QRY1.ELEMENT_DESC);
		   results.add(result);
	   }else if (strSearch.equals("C")) {
		   WCLAUSE = " and PORTION_NO = trim('"+strCode_P+"') and ELEMENT_NO = trim('"+strCode_E+"') and CATEGORY_NO = trim('"+strCode_C+"')";
		   def QRY1;
		   QRY1 = sql.firstRow("select CATEGORY_NO,CATEG_DESC from msf387 " +
		   "where upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) " + WCLAUSE);
		   GenericScriptResult result = new GenericScriptResult()
		   result.addAttribute("resCode", QRY1.CATEGORY_NO);
		   result.addAttribute("resDesc", QRY1.CATEG_DESC);
		   results.add(result);
	   }
	   
	   return results
   }
}
