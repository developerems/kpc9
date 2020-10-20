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

public class SubAssetList extends GenericScriptPlugin implements GenericScriptExecuteForCollection{
   String version = "1";
		   
   InitialContext initial = new InitialContext()
   Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
   def sql = new Sql(CAISource)
   
   public GenericScriptResults executeForCollection(SecurityToken securityToken, RequestAttributes requestAttributes,
	   Integer maxNumberOfObjects, RestartAttributes restartAttributes) throws FatalException {
	   log.info("Exec for coll SubAssetList : " + version )
	   RequestAttributes reqAtt = requestAttributes
	   String strSearch = reqAtt.getAttributeStringValue("parSubAssetNo");
	   String strAssetNo = reqAtt.getAttributeStringValue("parAssetNo");
	   def results = new GenericScriptResults();
	   
	   String QryAssetNo = "";
	   if (strAssetNo.equals(null)) {
		   QryAssetNo = "";
	   }else {
		   QryAssetNo = " and trim(ASSET_NO) = trim('" +strAssetNo+ "') ";
	   }
	   String StrSQL = ""
	   
	   if(strSearch.equals(null)){
		   log.info("maxNumberOfObjects : " + maxNumberOfObjects );
		   
		   if (restartAttributes.equals(null)){
			   StrSQL = "select row_number () over(order by a.SUB_ASSET_NO) NO,a.* from (" +
			               "SELECT distinct a.SUB_ASSET_NO From msf685 a " +
						   "where a.DSTRCT_CODE = '"+securityToken.getDistrict()+"' " + QryAssetNo+
						   "ORDER BY a.SUB_ASSET_NO OFFSET 0 ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY ) a" ;
			   log.info ("StrSQL : " + StrSQL);
			   sql.eachRow(StrSQL, {
				   GenericScriptResult result = new GenericScriptResult()
				   result.addAttribute("resCode", it.SUB_ASSET_NO);
				   result.addAttribute("resDesc", "");
				   result.addAttribute("lastRow", maxNumberOfObjects.toString());
				   results.add(result);
			   })
		   }else {
			   log.info("restartAttributes : " + restartAttributes.getAttributeStringValue("lastRow") );
			   Integer MaxInst = Integer.parseInt(restartAttributes.getAttributeStringValue("lastRow"));
			   //MaxInst = MaxInst + maxNumberOfObjects
			   StrSQL = "select row_number () over(order by a.SUB_ASSET_NO) NO,a.* from (" +
			               "SELECT distinct a.SUB_ASSET_NO From msf685 a " +
						   "where a.DSTRCT_CODE = '"+securityToken.getDistrict()+"' " +QryAssetNo+
						   "ORDER BY a.SUB_ASSET_NO OFFSET "+MaxInst.toString()+" ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY ) a" ;
			   log.info ("StrSQL : " + StrSQL);
			   sql.eachRow(StrSQL, {
				   GenericScriptResult result = new GenericScriptResult();
				   MaxInst = it.NO
				   result.addAttribute("resCode", it.SUB_ASSET_NO);
				   result.addAttribute("resDesc", "");
				   result.addAttribute("lastRow", MaxInst.toString());
				   results.add(result);
			   })
		   }
	   }else{
		   
		   log.info("maxNumberOfObjects : " + maxNumberOfObjects );
		   
		   if (restartAttributes.equals(null)){
			   StrSQL = "select row_number () over(order by a.SUB_ASSET_NO) NO,a.* from (" +
			               "SELECT distinct a.SUB_ASSET_NO From msf685 a " +
						   "where a.DSTRCT_CODE = '"+securityToken.getDistrict()+"' and ((upper(a.SUB_ASSET_NO) like '%'||upper('"+strSearch+"')||'%')) " + QryAssetNo+
						   "ORDER BY a.SUB_ASSET_NO OFFSET 0 ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY ) a" ;
			   log.info ("StrSQL : " + StrSQL);
			   sql.eachRow(StrSQL, {
				   GenericScriptResult result = new GenericScriptResult();
				   result.addAttribute("resCode", it.SUB_ASSET_NO);
				   result.addAttribute("resDesc", "");
				   result.addAttribute("lastRow", maxNumberOfObjects.toString());
				   results.add(result);
			   })
		   }else {
			   log.info("restartAttributes : " + restartAttributes.getAttributeStringValue("lastRow") );
			   Integer MaxInst = Integer.parseInt(restartAttributes.getAttributeStringValue("lastRow"));
			   //MaxInst = MaxInst + maxNumberOfObjects
			   StrSQL = "select row_number () over(order by a.SUB_ASSET_NO) NO,a.* from (" +
			               "SELECT distinct a.SUB_ASSET_NO From msf685 a " +
						   "where a.DSTRCT_CODE = '"+securityToken.getDistrict()+"' and ((upper(a.SUB_ASSET_NO) like '%'||upper('"+strSearch+"')||'%')) " + QryAssetNo+
						   "ORDER BY a.SUB_ASSET_NO OFFSET "+MaxInst.toString()+" ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY ) a" ;
			   log.info ("StrSQL : " + StrSQL);
			   sql.eachRow(StrSQL, {
				   GenericScriptResult result = new GenericScriptResult();
				   MaxInst = it.NO
				   result.addAttribute("resCode", it.SUB_ASSET_NO);
				   result.addAttribute("resDesc", "");
				   result.addAttribute("lastRow", MaxInst.toString());
				   results.add(result);
			   })
		   }
	   }
	   
	   return results
   }
}
