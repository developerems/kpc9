/**
 * @EMS 2019
 *
 * 20190901 - a9ra5213 - Ricky Afriano - KPC UPGRADE Ellipse 8
 *            Initial Coding - Custom Manual Depreciation 
 **/
package KPC

import javax.naming.InitialContext

import com.mincom.ellipse.app.security.SecurityToken
import com.mincom.ellipse.errors.exceptions.FatalException
import com.mincom.ellipse.script.plugin.GenericScriptExecuteForCollection
import com.mincom.ellipse.script.plugin.GenericScriptPlugin
import com.mincom.ellipse.script.plugin.GenericScriptResult
import com.mincom.ellipse.script.plugin.GenericScriptResults
import com.mincom.ellipse.script.plugin.RequestAttributes
import com.mincom.ellipse.script.plugin.RestartAttributes
import groovy.sql.Sql
import java.text.DecimalFormat
import com.mincom.ellipse.errors.Error
import com.mincom.ellipse.errors.CobolMessages


public class ell685Search extends GenericScriptPlugin implements GenericScriptExecuteForCollection {
	String version = "1";
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	String StrErr = "";
	
	GenericScriptResults executeForCollection(SecurityToken securityToken, RequestAttributes requestAttributes,
		Integer maxNumberOfObjects, RestartAttributes restartAttributes) throws FatalException{
			log.info("ell685Search executeForCollection : " + version );
			def results = new GenericScriptResults();
			
			RequestAttributes reqAtt = requestAttributes;
			String parAssetNo = reqAtt.getAttributeStringValue("parAssetNo");
			String parAssetType = reqAtt.getAttributeStringValue("parAssetType");
			String parSubAssetNo = reqAtt.getAttributeStringValue("parSubAssetNo");
			String parBookType = reqAtt.getAttributeStringValue("parBookType");
			String parStartPeriod = reqAtt.getAttributeStringValue("parStartPeriod");
			
			if (parAssetNo.equals(null)) {
				parAssetNo = "";
			}else {
				parAssetNo = " and upper(trim(a.ASSET_NO)) = upper(trim('" + parAssetNo + "')) ";
			}
			
			if (parSubAssetNo.equals(null)) {
				parSubAssetNo = "";
			}else {
				parSubAssetNo = " and upper(trim(a.SUB_ASSET_NO)) = upper(trim('" + parSubAssetNo + "')) ";
			}
			
			if (parAssetType.equals(null)) {
				parAssetType = "";
			}else {
				parAssetType = " and upper(trim(a.ASSET_TY)) = upper(trim('" + parAssetType + "')) ";
			}
			
			if (parBookType.equals(null)) {
				parBookType = "";
			}else {
				parBookType = " and upper(trim(a.BOOK_TY)) = upper(trim('" + parBookType + "')) ";
			}
			
			if (parStartPeriod.equals(null)) {
				parStartPeriod = "";
			}else {
				parStartPeriod = " and upper(trim(a.START_PERIOD)) = upper(trim('" + parStartPeriod + "')) ";
			}
			
			String StrSQL = "";
			log.info("maxNumberOfObjects : " + maxNumberOfObjects );
			DecimalFormat df = new DecimalFormat("#,##0.00;-#,##0.00");
			if (restartAttributes.equals(null)){
				   StrSQL = "select a.*,trim(b.SUB_ASSET_DESC) SUB_ASSET_DESC from (select " + 
								"substr(ENTITY_VALUE,1,4) DSTRCT_CODE,substr(ENTITY_VALUE,5,12) ASSET_NO, " +
								"substr(ENTITY_VALUE,17,6) SUB_ASSET_NO,substr(ENTITY_VALUE,23,1) ASSET_TY, " +
								"substr(ENTITY_VALUE,24,2) BOOK_TY,trim(REF_CODE) START_PERIOD " +
								"from msf071 where ENTITY_TYPE = 'CMD' and REF_NO = '001' and SEQ_NUM = '001') a " + 
								"left outer join msf685 b on (a.DSTRCT_CODE = b.DSTRCT_CODE and a.ASSET_NO = b.ASSET_NO and a.SUB_ASSET_NO = b.SUB_ASSET_NO) " +
							   "where a.DSTRCT_CODE = '"+securityToken.getDistrict()+"' " + parAssetNo + parSubAssetNo + parAssetType + parBookType + parStartPeriod + 
							   "ORDER BY a.ASSET_NO,a.SUB_ASSET_NO OFFSET 0 ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY" ;
				   log.info ("StrSQL : " + StrSQL);
				   sql.eachRow(StrSQL, {
					   GenericScriptResult result = new GenericScriptResult()
					   result.addAttribute("assetNo", it.ASSET_NO);
					   result.addAttribute("parSubAssetNo", it.SUB_ASSET_NO);
					   result.addAttribute("assetDesc", it.SUB_ASSET_DESC);
					   result.addAttribute("parAssetTy", it.ASSET_TY);
					   result.addAttribute("parBookTy", it.BOOK_TY);
					   result.addAttribute("parStartPeriod", it.START_PERIOD);
					   result.addAttribute("LAST_ROW", maxNumberOfObjects.toString());
					   results.add(result);
				   })
			}else {
				   log.info("restartAttributes : " + restartAttributes.getAttributeStringValue("LAST_ROW") );
				   Integer MaxInst = Integer.parseInt(restartAttributes.getAttributeStringValue("LAST_ROW"));
				   
				   StrSQL = "select a.*,trim(b.SUB_ASSET_DESC) SUB_ASSET_DESC from (select " + 
								"substr(ENTITY_VALUE,1,4) DSTRCT_CODE,substr(ENTITY_VALUE,5,12) ASSET_NO, " +
								"substr(ENTITY_VALUE,17,6) SUB_ASSET_NO,substr(ENTITY_VALUE,23,1) ASSET_TY, " +
								"substr(ENTITY_VALUE,24,2) BOOK_TY,trim(REF_CODE) START_PERIOD " +
								"from msf071 where ENTITY_TYPE = 'CMD' and REF_NO = '001' and SEQ_NUM = '001') a " + 
								"left outer join msf685 b on (a.DSTRCT_CODE = b.DSTRCT_CODE and a.ASSET_NO = b.ASSET_NO and a.SUB_ASSET_NO = b.SUB_ASSET_NO) " +
							   "where a.DSTRCT_CODE = '"+securityToken.getDistrict()+"' " + parAssetNo + parSubAssetNo + parAssetType + parBookType + parStartPeriod +
							   "ORDER BY a.ASSET_NO,a.SUB_ASSET_NO OFFSET "+MaxInst.toString()+" ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY" ;
				   log.info ("StrSQL : " + StrSQL);
				   MaxInst = MaxInst + maxNumberOfObjects
				   sql.eachRow(StrSQL, {
					   GenericScriptResult result = new GenericScriptResult()
					   result.addAttribute("assetNo", it.ASSET_NO);
					   result.addAttribute("parSubAssetNo", it.SUB_ASSET_NO);
					   result.addAttribute("assetDesc", it.SUB_ASSET_DESC);
					   result.addAttribute("parAssetTy", it.ASSET_TY);
					   result.addAttribute("parBookTy", it.BOOK_TY);
					   result.addAttribute("parStartPeriod", it.START_PERIOD);
					   result.addAttribute("LAST_ROW", MaxInst.toString());
					   results.add(result);
				   })
			}
			
			return results;
	}
	private String SetErrMes(){
		String Qerr = "UPDATE msf010 set TABLE_DESC = ? where TABLE_type = 'ER' and TABLE_CODE = '8541'";
		try
		{
			def QueryRes5 = sql.execute(Qerr,StrErr);
		} catch (Exception  e) {
			log.info ("Exception is : " + e);
		}
	}
	private String RollErrMes(){
		StrErr = "ERROR -"
		String Qerr = "UPDATE msf010 set TABLE_DESC = ? where TABLE_type = 'ER' and TABLE_CODE = '8541'";
		try
		{
			def QueryRes5 = sql.execute(Qerr,StrErr);
		} catch (Exception  e) {
			log.info ("Exception is : " + e);
		}
	}
	
}