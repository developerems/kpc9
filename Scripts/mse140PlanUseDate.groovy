/**
 * @EMS 2019
 *
 * 20190901 - a9ra5213 - Ricky Afriano - KPC UPGRADE Ellipse 8
 *            Initial Coding - Custom Manual Depreciation 
 **/
package KPC

import javax.naming.InitialContext

import com.mincom.ellipse.app.security.SecurityToken
import com.mincom.ellipse.ejp.exceptions.InteractionNotAuthorisedException
import com.mincom.ellipse.errors.exceptions.FatalException
import com.mincom.ellipse.script.plugin.GenericScriptCreate
import com.mincom.ellipse.script.plugin.GenericScriptDelete
import com.mincom.ellipse.script.plugin.GenericScriptExecute
import com.mincom.ellipse.script.plugin.GenericScriptExecuteForCollection
import com.mincom.ellipse.script.plugin.GenericScriptPlugin
import com.mincom.ellipse.script.plugin.GenericScriptResults
import com.mincom.ellipse.script.plugin.GenericScriptUpdate
import com.mincom.ellipse.script.plugin.RequestAttributes
import com.mincom.ellipse.script.plugin.GenericScriptResult
import com.mincom.ellipse.script.plugin.RestartAttributes
import groovy.sql.Sql;

import java.text.DecimalFormat
import com.mincom.enterpriseservice.ellipse.*
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import com.mincom.ellipse.errors.Error;
import com.mincom.ellipse.errors.UnlocalisedError
import com.mincom.ellipse.errors.UnlocalisedMessage
import com.mincom.ellipse.errors.UnlocalisedWarning
import com.mincom.ellipse.errors.CobolMessages;
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsService
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsServiceCreateReplyDTO
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsServiceCreateRequestDTO
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsServiceDeleteReplyDTO


public class mse140PlanUseDate extends GenericScriptPlugin implements GenericScriptExecute{
	String version = "1";
	InitialContext initial = new InitialContext();
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource");
	def sql = new Sql(CAISource);
	
	String strCrDT = "";
	String strCrTM = "";
	String StrDT = "";
	String StrMT = "";
	String StrYR = "";
	String StrHH = "";
	String StrMM = "";
	String StrSS = "";
	String StrErr = "";
	
	
	public GenericScriptResults execute(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings)
	throws FatalException {
		log.info("Execute mse140PlanUseDate Execute : " + version );
		GenericScriptResults results = new GenericScriptResults();
		GenericScriptResult result = new GenericScriptResult();
		RequestAttributes reqAtt = requestAttributes[0];
		
		String ireqNo = "";
		ireqNo = reqAtt.getAttributeStringValue("ireqNo");
		
		if (ireqNo.equals(null) || ireqNo.equals("")) {
			StrErr = "IREQ NO REQUIRED!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("ireqNo")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		
		String ireqType = "";
		ireqType = reqAtt.getAttributeStringValue("ireqType");
		
		String planUseDate = "";
		if(!reqAtt.getAttributeDateValue("parPlanUseDate").equals(null)){
			if (!reqAtt.getAttributeDateValue("parPlanUseDate").equals("")){
				DateToString(reqAtt.getAttributeDateValue("parPlanUseDate"));
				planUseDate = strCrDT
			}
		}
				
		if (planUseDate.equals(null) || planUseDate.equals("")) {
			planUseDate = " ";
		}
		if (ireqType.trim().equals("NI")) {
			GetNowDateTime();
			
			def QRY1;
			if (!planUseDate.equals(null) && !planUseDate.equals("") && !planUseDate.equals(" ")) {
				QRY1 = sql.firstRow("select case when '"+planUseDate+"' < to_char(sysdate,'YYYYMMDD') then 'TRUE' else 'FALSE' end FLAG from dual");
				if(QRY1.FLAG.equals("TRUE")) {
					StrErr = "PLAN USE DATE CAN'T BE BEFORE TODAYS DATE"
					com.mincom.ellipse.errors.Error err = new UnlocalisedError(StrErr);
					err.setFieldId("parPlanUseDate")
					result.addError(err)
					results.add(result)
					return results
				}
			}
			QRY1 = sql.firstRow("select * from msf071 where ENTITY_TYPE = 'PUD' and upper(trim(ENTITY_VALUE)) = upper(trim('"+securityToken.getDistrict()+ireqNo+"')) and REF_NO = '001' and SEQ_NUM = '001'");
			log.info ("FIND Plan Use Date  : ");
			if(!QRY1.equals(null)) {
				String QueryUpdate = ("update msf071 set ref_code = '"+planUseDate+"',LAST_MOD_DATE = '"+strCrDT+"',LAST_MOD_TIME = '"+strCrTM+"',LAST_MOD_USER = '"+securityToken.getUserId()+"' where ENTITY_TYPE = 'PUD' and upper(trim(ENTITY_VALUE)) = upper(trim('"+securityToken.getDistrict()+ireqNo+"')) and REF_NO = '001' and SEQ_NUM = '001'");
				sql.execute(QueryUpdate);
			}else {
				String QueryInsert = ("Insert into MSF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY) values ('PUD','"+securityToken.getDistrict()+ireqNo+"','001','001','" + strCrDT + "','" + strCrTM + "','" + securityToken.getUserId() + "','"+planUseDate+"','            ')");
				sql.execute(QueryInsert);
			}
		}
		result.addAttribute("planUseDate", planUseDate);
		results.add(result);
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
	public String GetUserEmpID(String user, String district) {
		String EMP_ID = "";
		def QRY1;
		QRY1 = sql.firstRow("select * From msf020 where ENTRY_TYPE = 'S' and trim(ENTITY) = trim('"+user+"') and DSTRCT_CODE = '"+district+"'");
		if(!QRY1.equals(null)) {
			EMP_ID = QRY1.EMPLOYEE_ID;
		}
		return EMP_ID;
	}
	public boolean isNumeric(String str) {
		try {
			str = str.replace(",", "")
			Integer.parseInt(str);
			//Float.parseFloat(str);
			return true;
		  }
		  catch (NumberFormatException e) {
			// s is not numeric
			return false;
		  }
	}
	public def DateToString(Date ParDate) {
		Date InPer = ParDate;
		log.info("Hasil InPer : " + InPer.toString())
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(InPer);
		int iyear = cal.get(Calendar.YEAR);
		int imonth = cal.get(Calendar.MONTH);
		int iday = cal.get(Calendar.DAY_OF_MONTH);
		int iHH = cal.get(Calendar.HOUR);
		int iMM = cal.get(Calendar.MINUTE);
		int iSS = cal.get(Calendar.SECOND);
		
		if (iday.toString().trim().length() < 2){
			StrDT = "0" + iday.toString().trim()
		}else{
			StrDT = iday.toString().trim()
		}
		log.info("StrDT : " + StrDT )
		
		"(imonth + 1) untuk membuat bulan sesuai"
		if ((imonth + 1).toString().trim().length() < 2){
			StrMT = "0" + (imonth + 1).toString().trim()
		}else{
			StrMT = (imonth + 1).toString().trim()
		}
		log.info("StrMT : " + StrMT )
		if (iyear.toString().trim().length() < 3){
			StrYR = "20" + iyear.toString().trim()
		}else{
			StrYR = iyear.toString().trim()
		}
		log.info("StrYR : " + StrYR )
		strCrDT = StrYR + StrMT + StrDT
		log.info("strCrDT : " + strCrDT )
		
		if (iHH.toString().trim().length() < 2){
			StrHH = "0" + iHH.toString().trim()
		}else{
			StrHH = iHH.toString().trim()
		}
		log.info("StrHH : " + StrHH )
		
		if (iMM.toString().trim().length() < 2){
			StrMM = "0" + iMM.toString().trim()
		}else{
			StrMM = iMM.toString().trim()
		}
		log.info("StrMM : " + StrMM )
		
		if (iSS.toString().trim().length() < 2){
			StrSS = "0" + iSS.toString().trim()
		}else{
			StrSS = iSS.toString().trim()
		}
		log.info("StrSS : " + StrSS )
		
		strCrTM = StrHH + StrMM + StrSS
		log.info("strCrTM : " + strCrTM )
	}
}
