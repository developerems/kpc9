/** @EMS Nov 201812
 *
 * a9ra5213 - Ricky Afriano - Initial Code - PJB - ELL6A1 Tahap 2
 **/
package KPC
import java.text.DecimalFormat
import java.util.Date
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
import com.mincom.ellipse.errors.Error
import com.mincom.ellipse.errors.CobolMessages
import com.mincom.ellipse.*

public class ELL38C_CREATE extends GenericScriptPlugin implements GenericScriptExecute,GenericScriptExecuteForCollection, GenericScriptUpdate, GenericScriptCreate, GenericScriptDelete{
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	
	String version = "1";
	String strCrDT = "";
	String strCrTM = "";
	String StrDT = "";
	String StrMT = "";
	String StrYR = "";
	String StrHH = "";
	String StrMM = "";
	String StrSS = "";
	String Errexcpt = "";
	Integer LastIndext = 0;
	String StrErr = "";
	
	public GenericScriptResults execute(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings)
	throws FatalException {
		log.info("Execute ELL38C_CREATE Execute : " + version );
		GenericScriptResults results = new GenericScriptResults();
		RequestAttributes reqAtt = requestAttributes[0];
		String CNT_NO = "";
		String WO = "";
		String qryWO = "";
		
		CNT_NO = reqAtt.getAttributeStringValue("creCntNo");
		WO = reqAtt.getAttributeStringValue("creWo");
		if(WO.equals(null)) {
			qryWO = "";
		}else {
			qryWO = " and upper(trim(WORK_ORDER)) = upper(trim('"+WO+"')) ";
		}
		log.info("CNT_NO : " + CNT_NO );
		log.info("WO : " + WO );
		//Validate Contract No
		def QRY1;
		QRY1 = sql.firstRow("select * from msf384 where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) ");
		log.info ("FIND CONTRACT  : " + QRY1);
		if(QRY1.equals(null)) {
			GenericScriptResult result = new GenericScriptResult()
			StrErr = "INVALID CONTRACT NUMBER / DOESN'T EXIST"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("creCntNo")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		
		//Validate WO NO
		if(!WO.equals(null)) {
			def QRY2;
			QRY2 = sql.firstRow("select * from MSF620 where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(WORK_ORDER)) = upper(trim('"+WO+"')) ");
			log.info ("FIND WO  : " + QRY2);
			if(QRY2.equals(null)) {
				GenericScriptResult result = new GenericScriptResult()
				StrErr = "INVALID WO NUMBER / DOESN'T EXIST"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("creWo")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
		}
		DecimalFormat df = new DecimalFormat("#,##0.00;-#,##0.00");
		
		GenericScriptResult result = new GenericScriptResult();
		result.addAttribute("noUrut", "1");
		results.add(result);
		result.addAttribute("noUrut", "2");
		results.add(result);
		return results;
	}
	public GenericScriptResults executeForCollection(SecurityToken securityToken, RequestAttributes requestAttributes,
		Integer maxNumberOfObjects, RestartAttributes restartAttributes) throws FatalException {
		log.info("Execute Colection ELL38C_CREATE : " + version )
		GenericScriptResults results = new GenericScriptResults()
		RequestAttributes reqAtt = requestAttributes
		
		String CNT_NO = "";
		if (reqAtt.getAttributeStringValue("parCntNo").equals(null)) {
			CNT_NO = reqAtt.getAttributeStringValue("cntNo");
		}else {
			CNT_NO = reqAtt.getAttributeStringValue("parCntNo");
		}
		
		String WO = "";
		if (reqAtt.getAttributeStringValue("parWo").equals(null)) {
			WO = reqAtt.getAttributeStringValue("wo");
		}else {
			WO = reqAtt.getAttributeStringValue("parWo");
		}
		DecimalFormat df = new DecimalFormat("#,##0.00;-#,##0.00");
		/*
		//Initiate
		String CIC_TYPE = "";
		
		//Validate Contract No
		def QRY1;
		QRY1 = sql.firstRow("select * from msf384 where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) ");
		log.info ("FIND CONTRACT  : " + QRY1);
		if(QRY1.equals(null)) {
			GenericScriptResult result = new GenericScriptResult()
			StrErr = "INVALID CONTRACT NUMBER / DOESN'T EXIST"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("parCntNo")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		
		//Validate WO NO
		def QRY2;
		QRY2 = sql.firstRow("select * from MSF620 where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(WORK_ORDER)) = upper(trim('"+WO+"')) ");
		log.info ("FIND WO  : " + QRY2);
		if(QRY2.equals(null)) {
			GenericScriptResult result = new GenericScriptResult()
			StrErr = "INVALID WO NUMBER / DOESN'T EXIST"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("parWo")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		
		//Validate CIC Type
		if (!WO.equals(null) && (QRY1.COND_OF_CNTRCT.trim().equals("CB") || QRY1.COND_OF_CNTRCT.trim().equals("UM"))) {
			CIC_TYPE = "wo";
		}else {
			CIC_TYPE = "LS";
		}
		
		//Validate CIC No
		
		
		String StrSQL = ""
		log.info("maxNumberOfObjects : " + maxNumberOfObjects );
		if (restartAttributes.equals(null)){
			
			StrSQL = "select rownum NO,a.PORTION_NO,a.ELEMENT_NO,b.CATEGORY_NO,b.CATEG_DESC,b.CATEG_BASE_QTY,b.CATEG_BASE_UN, " +
				"case when trim(c.TABLE_DESC) is null then ' ' else trim(c.TABLE_DESC) end CATEG_BASE_UN_DESC, " +
				"b.CATEG_BASE_PRC_RT,b.CATEG_BASE_VAL,b.CATEG_BASE_DT,b.CATEG_BASE_PRICE,b.CATEGORY_387_TYPE,d.COND_OF_CNTRCT " +
				"from msf386 a " +
				"left outer join msf387 b on (a.CONTRACT_NO = b.CONTRACT_NO and a.PORTION_NO = b.PORTION_NO and a.ELEMENT_NO = b.ELEMENT_NO) " +
				"left outer join msf010 c on (c.table_type = 'EU' and trim(table_code) = trim(b.CATEG_BASE_UN)) " +
				"left outer join msf384 d on (a.CONTRACT_NO = d.CONTRACT_NO) " +
				"where upper(trim(a.CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and d.dstrct_code = '"+securityToken.getDistrict()+"' and b.CATEG_BASE_PRICE <> 0 and b.CATEG_BASE_PRC_RT <> 0 " +
				"Order by a.PORTION_NO,a.ELEMENT_NO,b.CATEGORY_NO OFFSET 0 ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY";
			log.info ("StrSQL : " + StrSQL);
			
			sql.eachRow(StrSQL, {
				if ((!WO.equals(null) && (it.COND_OF_CNTRCT.trim().equals("CB") || it.COND_OF_CNTRCT.trim().equals("UM"))) ||  it.COND_OF_CNTRCT.trim().equals("NU")) {
					GenericScriptResult result = new GenericScriptResult();
					result.addAttribute("noUrut", it.NO);
					result.addAttribute("PORTION", it.PORTION_NO);
					result.addAttribute("ELEMENT", it.ELEMENT_NO);
					result.addAttribute("CATEGORY", it.CATEGORY_NO);
					result.addAttribute("CAT_DESC", it.CATEG_DESC);
					result.addAttribute("CAT_QTY", df.format(it.CATEG_BASE_QTY));
					result.addAttribute("CAT_UNIT", it.CATEG_BASE_UN);
					result.addAttribute("CAT_UNIT_DESC", it.CATEG_BASE_UN_DESC);
					result.addAttribute("CAT_BASE_PR_RT", df.format(it.CATEG_BASE_PRC_RT));
					result.addAttribute("CAT_BASE_VAL", df.format(it.CATEG_BASE_VAL));
					result.addAttribute("CAT_BASE_DT", it.CATEG_BASE_DT);
					result.addAttribute("CAT_BASE_PR", df.format(it.CATEG_BASE_PRICE));
					result.addAttribute("CAT_TYPE", it.CATEGORY_387_TYPE);
					result.addAttribute("ITEM_TYPE", it.CATEGORY_387_TYPE);
					result.addAttribute("LAST_ROW", maxNumberOfObjects.toString());
					results.add(result);
				}
			})
		}else {
			log.info("restartAttributes : " + restartAttributes.getAttributeStringValue("LAST_ROW") );
			Integer MaxInst = Integer.parseInt(restartAttributes.getAttributeStringValue("LAST_ROW"));
			//MaxInst = MaxInst + maxNumberOfObjects
			StrSQL = "select rownum NO,a.PORTION_NO,a.ELEMENT_NO,b.CATEGORY_NO,b.CATEG_DESC,b.CATEG_BASE_QTY,b.CATEG_BASE_UN, " +
				"case when trim(c.TABLE_DESC) is null then ' ' else trim(c.TABLE_DESC) end CATEG_BASE_UN_DESC, " +
				"b.CATEG_BASE_PRC_RT,b.CATEG_BASE_VAL,b.CATEG_BASE_DT,b.CATEG_BASE_PRICE,b.CATEGORY_387_TYPE " +
				"from msf386 a " +
				"left outer join msf387 b on (a.CONTRACT_NO = b.CONTRACT_NO and a.PORTION_NO = b.PORTION_NO and a.ELEMENT_NO = b.ELEMENT_NO) " +
				"left outer join msf010 c on (c.table_type = 'EU' and trim(table_code) = trim(b.CATEG_BASE_UN)) " +
				"left outer join msf384 d on (a.CONTRACT_NO = d.CONTRACT_NO) " +
				"where upper(trim(a.CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and d.dstrct_code = '"+securityToken.getDistrict()+"' and b.CATEG_BASE_PRICE <> 0 and b.CATEG_BASE_PRC_RT <> 0 " +
				"Order by a.PORTION_NO,a.ELEMENT_NO,b.CATEGORY_NO OFFSET "+MaxInst.toString()+" ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY";
			log.info ("StrSQL : " + StrSQL);
			
			sql.eachRow(StrSQL, {
				if ((!WO.equals(null) && (it.COND_OF_CNTRCT.trim().equals("CB") || it.COND_OF_CNTRCT.trim().equals("UM"))) ||  it.COND_OF_CNTRCT.trim().equals("NU")) {
					GenericScriptResult result = new GenericScriptResult();
					MaxInst = it.NO
					result.addAttribute("noUrut", it.NO);
					result.addAttribute("PORTION", it.PORTION_NO);
					result.addAttribute("ELEMENT", it.ELEMENT_NO);
					result.addAttribute("CATEGORY", it.CATEGORY_NO);
					result.addAttribute("CAT_DESC", it.CATEG_DESC);
					result.addAttribute("CAT_QTY", df.format(it.CATEG_BASE_QTY));
					result.addAttribute("CAT_UNIT", it.CATEG_BASE_UN);
					result.addAttribute("CAT_UNIT_DESC", it.CATEG_BASE_UN_DESC);
					result.addAttribute("CAT_BASE_PR_RT", df.format(it.CATEG_BASE_PRC_RT));
					result.addAttribute("CAT_BASE_VAL", df.format(it.CATEG_BASE_VAL));
					result.addAttribute("CAT_BASE_DT", it.CATEG_BASE_DT);
					result.addAttribute("CAT_BASE_PR", df.format(it.CATEG_BASE_PRICE));
					result.addAttribute("CAT_TYPE", it.CATEGORY_387_TYPE);
					result.addAttribute("ITEM_TYPE", it.CATEGORY_387_TYPE);
					result.addAttribute("LAST_ROW", MaxInst.toString());
					results.add(result);
				}
			})
		}
		*/
		
		return results
	}
	public GenericScriptResults create(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings)
	throws FatalException {
		log.info("Create ELL38C_CREATE : " + version )
		GenericScriptResults results = new GenericScriptResults()
		GenericScriptResult result = new GenericScriptResult()
		RequestAttributes reqAtt = requestAttributes[0]
		
		results.add(result)
		return results
	}
	public GenericScriptResults update(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings)
	throws FatalException {
		log.info("Update ELL38C_CREATE : " + version )
		GenericScriptResults results = new GenericScriptResults()
		GenericScriptResult result = new GenericScriptResult()
		RequestAttributes reqAtt = requestAttributes[0]
		
		results.add(result)
		return results
	}
	public GenericScriptResults delete(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings)
	throws FatalException {
		log.info("Delete ELL38C_CREATE : " + version )
		GenericScriptResults results = new GenericScriptResults()
		GenericScriptResult result = new GenericScriptResult()
		
		results.add(result)
		return results
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
	public def GetNowDateTime() {
		Date InPer = new Date();
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(InPer);
		int iyear = cal.get(Calendar.YEAR);
		int imonth = cal.get(Calendar.MONTH);
		int iday = cal.get(Calendar.DAY_OF_MONTH);
		int iHH = cal.get(Calendar.HOUR_OF_DAY);
		int iMM = cal.get(Calendar.MINUTE);
		int iSS = cal.get(Calendar.SECOND);
		
		if (iday.toString().trim().length() < 2){
			StrDT = "0" + iday.toString().trim()
		}else{
			StrDT = iday.toString().trim()
		}
		
		"(imonth + 1) untuk membuat bulan sesuai"
		if ((imonth + 1).toString().trim().length() < 2){
			StrMT = "0" + (imonth + 1).toString().trim()
		}else{
			StrMT = (imonth + 1).toString().trim()
		}
		
		if (iyear.toString().trim().length() < 3){
			StrYR = "20" + iyear.toString().trim()
		}else{
			StrYR = iyear.toString().trim()
		}
		
		strCrDT = StrYR + StrMT + StrDT
		
		if (iHH.toString().trim().length() < 2){
			StrHH = "0" + iHH.toString().trim()
		}else{
			StrHH = iHH.toString().trim()
		}
		
		if (iMM.toString().trim().length() < 2){
			StrMM = "0" + iMM.toString().trim()
		}else{
			StrMM = iMM.toString().trim()
		}
		
		if (iSS.toString().trim().length() < 2){
			StrSS = "0" + iSS.toString().trim()
		}else{
			StrSS = iSS.toString().trim()
		}
		
		strCrTM = StrHH + StrMM + StrSS
	}
}