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

public class AssetList extends GenericScriptPlugin implements GenericScriptExecuteForCollection{
   String version = "1";
		   
   InitialContext initial = new InitialContext()
   Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
   def sql = new Sql(CAISource)
   
   public GenericScriptResults executeForCollection(SecurityToken securityToken, RequestAttributes requestAttributes,
	   Integer maxNumberOfObjects, RestartAttributes restartAttributes) throws FatalException {
	   log.info("Exec for coll AssetList : " + version )
	   RequestAttributes reqAtt = requestAttributes
	   String strSearch = reqAtt.getAttributeStringValue("parAtt");
	   def results = new GenericScriptResults();
	   
	   String StrSQL = ""
	   
	   if(strSearch.equals(null)){
		   log.info("maxNumberOfObjects : " + maxNumberOfObjects );
		   
		   if (restartAttributes.equals(null)){
			   StrSQL = "select row_number () over(order by a.ASSET_NO) NO,a.* from (" +
			               "SELECT DISTINCT a.ASSET_NO,case when a.ASSET_TY = 'A' then trim(b.ASSET_DESC) else trim(c.item_name_1) || trim(c.item_name_2) end ASSET_DESC from msf685 a " +
			               "left outer join msf680 b on (a.DSTRCT_CODE = b.DSTRCT_CODE and a.ASSET_NO = b.ASSET_NO) " +
						   "left outer join msf600 c on (a.DSTRCT_CODE = c.DSTRCT_CODE and a.ASSET_NO = c.EQUIP_NO) " +
						   "where a.DSTRCT_CODE = '"+securityToken.getDistrict()+"' " +
						   ") a ORDER BY a.ASSET_NO OFFSET 0 ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY " ;
			   log.info ("StrSQL : " + StrSQL);
			   sql.eachRow(StrSQL, {
				   GenericScriptResult result = new GenericScriptResult()
				   result.addAttribute("resCode", it.ASSET_NO);
				   result.addAttribute("resDesc", it.ASSET_DESC);
				   result.addAttribute("LAST_ROW", maxNumberOfObjects.toString());
				   results.add(result);
			   })
		   }else {
			   log.info("restartAttributes : " + restartAttributes.getAttributeStringValue("LAST_ROW") );
			   Integer MaxInst = Integer.parseInt(restartAttributes.getAttributeStringValue("LAST_ROW"));
			   //MaxInst = MaxInst + maxNumberOfObjects
			   StrSQL = "select row_number () over(order by a.ASSET_NO) NO,a.* from (" +
			               "SELECT DISTINCT a.ASSET_NO,case when a.ASSET_TY = 'A' then trim(b.ASSET_DESC) else trim(c.item_name_1) || trim(c.item_name_2) end ASSET_DESC from msf685 a " +
						   "left outer join msf680 b on (a.DSTRCT_CODE = b.DSTRCT_CODE and a.ASSET_NO = b.ASSET_NO) " +
						   "left outer join msf600 c on (a.DSTRCT_CODE = c.DSTRCT_CODE and a.ASSET_NO = c.EQUIP_NO) " +
						   "where a.DSTRCT_CODE = '"+securityToken.getDistrict()+"' " +
						   ") a ORDER BY a.ASSET_NO OFFSET "+MaxInst.toString()+" ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY " ;
			   log.info ("StrSQL : " + StrSQL);
			   sql.eachRow(StrSQL, {
				   GenericScriptResult result = new GenericScriptResult();
				   MaxInst = it.NO
				   result.addAttribute("resCode", it.ASSET_NO);
				   result.addAttribute("resDesc", it.ASSET_DESC);
				   result.addAttribute("LAST_ROW", MaxInst.toString());
				   results.add(result);
			   })
		   }
	   }else{
		   
		   log.info("maxNumberOfObjects : " + maxNumberOfObjects );
		   
		   if (restartAttributes.equals(null)){
			   StrSQL = "select row_number () over(order by a.ASSET_NO) NO,a.* from (" +
			               "SELECT DISTINCT a.ASSET_NO,case when a.ASSET_TY = 'A' then trim(b.ASSET_DESC) else trim(c.item_name_1) || trim(c.item_name_2) end ASSET_DESC from msf685 a " +
						   "left outer join msf680 b on (a.DSTRCT_CODE = b.DSTRCT_CODE and a.ASSET_NO = b.ASSET_NO) " +
						   "left outer join msf600 c on (a.DSTRCT_CODE = c.DSTRCT_CODE and a.ASSET_NO = c.EQUIP_NO) " +
						   "where a.DSTRCT_CODE = '"+securityToken.getDistrict()+"' and ((upper(a.ASSET_NO) like '%'||upper('"+strSearch+"')||'%') or (upper(case when a.ASSET_TY = 'A' then trim(b.ASSET_DESC) else trim(c.item_name_1) || trim(c.item_name_2) end) like '%'||upper('"+strSearch+"')||'%')) " +
						   ") a ORDER BY a.ASSET_NO OFFSET 0 ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY " ;
			   log.info ("StrSQL : " + StrSQL);
			   sql.eachRow(StrSQL, {
				   GenericScriptResult result = new GenericScriptResult();
				   result.addAttribute("resCode", it.ASSET_NO);
				   result.addAttribute("resDesc", it.ASSET_DESC);
				   result.addAttribute("LAST_ROW", maxNumberOfObjects.toString());
				   results.add(result);
			   })
		   }else {
			   log.info("restartAttributes : " + restartAttributes.getAttributeStringValue("LAST_ROW") );
			   Integer MaxInst = Integer.parseInt(restartAttributes.getAttributeStringValue("LAST_ROW"));
			   //MaxInst = MaxInst + maxNumberOfObjects
			   StrSQL = "select row_number () over(order by a.ASSET_NO) NO,a.* from (" +
			               "SELECT DISTINCT a.ASSET_NO,case when a.ASSET_TY = 'A' then trim(b.ASSET_DESC) else trim(c.item_name_1) || trim(c.item_name_2) end ASSET_DESC from msf685 a " +
						   "left outer join msf680 b on (a.DSTRCT_CODE = b.DSTRCT_CODE and a.ASSET_NO = b.ASSET_NO) " +
						   "left outer join msf600 c on (a.DSTRCT_CODE = c.DSTRCT_CODE and a.ASSET_NO = c.EQUIP_NO) " +
						   "where a.DSTRCT_CODE = '"+securityToken.getDistrict()+"' and ((upper(a.ASSET_NO) like '%'||upper('"+strSearch+"')||'%') or (upper(case when a.ASSET_TY = 'A' then trim(b.ASSET_DESC) else trim(c.item_name_1) || trim(c.item_name_2) end) like '%'||upper('"+strSearch+"')||'%')) " +
						   ") a ORDER BY a.ASSET_NO OFFSET "+MaxInst.toString()+" ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY " ;
			   log.info ("StrSQL : " + StrSQL);
			   sql.eachRow(StrSQL, {
				   GenericScriptResult result = new GenericScriptResult();
				   MaxInst = it.NO
				   result.addAttribute("resCode", it.CIC_NO);
				   result.addAttribute("resDesc", it.CIC_DESC);
				   result.addAttribute("LAST_ROW", MaxInst.toString());
				   results.add(result);
			   })
		   }
	   }
	   
	   return results
   }
}
