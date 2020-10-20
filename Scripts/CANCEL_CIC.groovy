/**
 * @EMS Jan 2019
 *
 * 20190303 - a9ra5213 - Ricky Afriano - KPC UPGRADE Ellipse 8
 *            Initial Coding - Cancel CIC Item data in ELL38C Detail Screen and delete Valuation in MSE389
 *            using valuation service
 **/
package KPC

import javax.naming.InitialContext

import com.mincom.ellipse.app.security.SecurityToken
import com.mincom.ellipse.ejra.mso.GenericMsoRecord
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
import com.mincom.ellipse.errors.Warning
import com.mincom.ellipse.errors.UnlocalisedError
import com.mincom.ellipse.errors.UnlocalisedMessage
import com.mincom.ellipse.errors.UnlocalisedWarning
import com.mincom.ellipse.errors.CobolMessages;
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsService
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsServiceCreateReplyDTO
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsServiceCreateRequestDTO
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsServiceDeleteReplyDTO

public class CANCEL_CIC extends GenericScriptPlugin implements GenericScriptExecute{
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
	String EQP_NO = "";
	
	
	public GenericScriptResults execute(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings)
	throws FatalException {
		log.info("Execute CANCEL_CIC Execute : " + version );
		GenericScriptResults results = new GenericScriptResults();
		GenericScriptResult result = new GenericScriptResult();
		RequestAttributes reqAtt = requestAttributes[0];
		
		String CNT_NO = "";
		if (!reqAtt.getAttributeStringValue("diaCntNo").equals(null)) {
			CNT_NO = reqAtt.getAttributeStringValue("diaCntNo");
		}
		else if (reqAtt.getAttributeStringValue("parGrdCntNo").equals(null)) {
			CNT_NO = reqAtt.getAttributeStringValue("cntNo");
		}else {
			CNT_NO = reqAtt.getAttributeStringValue("parGrdCntNo");
		}
		
		String CIC_NO = "";
		if (!reqAtt.getAttributeStringValue("diaCicNo").equals(null)) {
			CIC_NO = reqAtt.getAttributeStringValue("diaCicNo");
		}
		else if (reqAtt.getAttributeStringValue("parGrdCicNo").equals(null)) {
			CIC_NO = reqAtt.getAttributeStringValue("cicNo");
		}else {
			CIC_NO = reqAtt.getAttributeStringValue("parGrdCicNo");
		}
		
		String WO = "";
		if (!reqAtt.getAttributeStringValue("diaParWo").equals(null)) {
			WO = reqAtt.getAttributeStringValue("diaParWo");
		}
		else if (reqAtt.getAttributeStringValue("parGrdWoNo").equals(null)) {
			WO = reqAtt.getAttributeStringValue("wo");
		}else {
			WO = reqAtt.getAttributeStringValue("parGrdWoNo");
		}
		String EMP_ID = GetUserEmpID(securityToken.getUserId(), securityToken.getDistrict())
		
		//Cek Current Period
		def QRY0_1;
		QRY0_1 = sql.firstRow("select 'CURRENT_PERIOD' DUMMY,a.CURR_ACCT_MN AP_PER, b.CURR_ACCT_MN GL_PER " +
			"from dual " +
			"left outer join MSF000_CP a on (a.DSTRCT_CODE = '"+securityToken.getDistrict()+"' and a.CONTROL_REC_NO = '0002') " +
			"left outer join MSF000_CP b on (b.DSTRCT_CODE = '"+securityToken.getDistrict()+"' and b.CONTROL_REC_NO = '0010')");
		log.info ("FIND CURRENT PERIOD  : ");
		if(!QRY0_1.equals(null)) {
			if(!QRY0_1.AP_PER.trim().equals(QRY0_1.GL_PER.trim())) {
				/*
				StrErr = "AP ACCOUNTING PERIOD NOT EQUAL TO GL !"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				//err.setFieldId("CRE_CNT_NO")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
				*/
			}
		}else {
			StrErr = "CURRENT PERIOD NOT FOUND!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			//err.setFieldId("CRE_CNT_NO")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		//Cek Security
		def QRY0;
		QRY0 = sql.firstRow("select a.EMPLOYEE_ID,a.POSITION_ID, " +
			"case when b.AUTHTY_TYPE is null then 'FALSE' else 'TRUE' END IAPP, " +
			"case when c.AUTHTY_TYPE is null then 'FALSE' else 'TRUE' END INIT, " +
			"case when d.AUTHTY_TYPE is null then 'FALSE' else 'TRUE' END AUINTO, " +
			"e.GLOBAL_PROFILE, " +
			"case when substr(f.PROFILE,IDX_260,1) <> NILAI_GP_260 then 'FALSE' else 'TRUE' end SEC_260, " +
			"case when substr(f.PROFILE,IDX_261,1) <> '9' then 'FALSE' else 'TRUE' end SEC_261, " +
			"case when substr(f.PROFILE,IDX_904,1) <> '9' then 'FALSE' else 'TRUE' end SEC_904 " +
			"from msf878 a " +
			"left outer join ( " +
			"select * from MSF872 " +
			"where AUTHTY_TYPE = 'IAPP') b on (a.POSITION_ID = b.POSITION_ID) " +
			"left outer join ( " +
			"select * from MSF872 " +
			"where AUTHTY_TYPE = 'INIT') c on (a.POSITION_ID = c.POSITION_ID) " +
			"left outer join ( " +
			"select * from MSF872 " +
			"where AUTHTY_TYPE = 'INTO') d on (a.POSITION_ID = d.POSITION_ID) " +
			"left outer join msf870 e on (a.POSITION_ID = e.POSITION_ID) " +
			"left outer join msf020 f on (trim(e.GLOBAL_PROFILE) = trim(f.entity) and entry_type = 'G') " +
			"left outer join ( " +
			"select substr(trim(PROFILE),LENGTH(trim(PROFILE)),1) NILAI_GP_260,LENGTH(trim(PROFILE)) IDX_260 from msf020 " +
			"where ENTRY_TYPE = 'P' and entity = 'MSO260' " +
			") g on (1=1) " +
			"left outer join ( " +
			"select substr(trim(PROFILE),LENGTH(trim(PROFILE)),1) NILAI_GP_261,LENGTH(trim(PROFILE)) IDX_261 from msf020 " +
			"where ENTRY_TYPE = 'P' and entity = 'MSO261' " +
			") h on (1=1) " +
			"left outer join ( " +
			"select substr(trim(PROFILE),LENGTH(trim(PROFILE)),1) NILAI_GP_904,LENGTH(trim(PROFILE)) IDX_904 from msf020 " +
			"where ENTRY_TYPE = 'P' and entity = 'MSO904' " +
			") i on (1=1) " +
			"where a.EMPLOYEE_ID = '"+EMP_ID+"' and (a.POS_STOP_DATE = '00000000' or a.POS_STOP_DATE = ' ' or (case when a.POS_STOP_DATE not in ('00000000',' ') then to_date(a.POS_STOP_DATE,'YYYYMMDD') else null end >= sysdate) ) and trim(a.POSITION_ID) = trim('"+securityToken.getRole()+"')") ;
		if(!QRY0.equals(null)) {
			log.info("QRY0 : " + QRY0 );
			if(QRY0.SEC_904.trim().equals("FALSE")) {
				/*
				StrErr = "YOUR SECURITY PROFILE VALUE FOR MSO904 SHOULD BE 9"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				//err.setFieldId("CRE_CNT_NO")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
				*/
			}
		}else {
			StrErr = "USER SECURITY NOT FOUND"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			//err.setFieldId("CRE_CNT_NO")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		
		log.info("CNT_NO : " + CNT_NO );
		log.info("WO : " + WO );
		log.info("returnWarnings : " + returnWarnings );
		
		if (returnWarnings) {
			result.addWarning(new UnlocalisedWarning("THIS ACTION WILL CANCEL THE CIC !"))
			results.add(result);
			return results;
		}else {
			 //Validate Contract No
			 def QRY1;
			 QRY1 = sql.firstRow("select * from msf384 where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) ");
			 log.info ("FIND CONTRACT  : " + QRY1);
			 if(QRY1.equals(null)) {
				 StrErr = "INVALID CONTRACT NUMBER / DOESN'T EXIST"
				 SetErrMes();
				 com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				 err.setFieldId("CRE_CNT_NO")
				 result.addError(err)
				 results.add(result)
				 RollErrMes();
				 return results
			 }
			 
			 //Validate WO NO
			 EQP_NO = "";
			 if(!WO.equals(null)) {
				 def QRY2;
				 QRY2 = sql.firstRow("select * from MSF620 where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(WORK_ORDER)) = upper(trim('"+WO+"')) ");
				 log.info ("FIND WO  : " + QRY2);
				 if(QRY2.equals(null)) {
					 StrErr = "INVALID WO NUMBER / DOESN'T EXIST"
					 SetErrMes();
					 com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					 err.setFieldId("wo")
					 result.addError(err)
					 results.add(result)
					 RollErrMes();
					 return results
				 }else {
					 EQP_NO = QRY2.EQUIP_NO;
					 if(QRY2.final_costs.trim().equals("Y")) {
						 StrErr = "WORK ORDER COSTS HAS BEEN FINALIZED"
						 SetErrMes();
						 com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
						 err.setFieldId("wo")
						 result.addError(err)
						 results.add(result)
						 RollErrMes();
						 return results
					 }
				 }
			 }
			 
			 String CIC_TYPE = "";
			 //Validate CIC No
			 def QRY2;
			 QRY2 = sql.firstRow("select * from ACA.KPF38F where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"'");
			 //log.info ("FIND CIC  : " + QRY2);
			 if(QRY2.equals(null)) {
				 StrErr = "INVALID CIC NUMBER / DOESN'T EXIST"
				 SetErrMes();
				 com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				 err.setFieldId("cicNo")
				 result.addError(err)
				 results.add(result)
				 RollErrMes();
				 return results
			 }else {
				 //Validate CIC Status
				 if(QRY2.CIC_STATUS.equals("4")) {
					 StrErr = "CIC ALREADY INVOICED!"
					 SetErrMes();
					 com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					 //err.setFieldId("cicNo")
					 result.addError(err)
					 results.add(result)
					 RollErrMes();
					 return results
				 }
				 if(QRY2.CIC_STATUS.equals("3")) {
					 StrErr = "CIC ALREADY CANCELED!"
					 SetErrMes();
					 com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					 //err.setFieldId("cicNo")
					 result.addError(err)
					 results.add(result)
					 RollErrMes();
					 return results
				 }
				 CIC_TYPE = QRY2.CIC_TYPE;
			 }
			 
			 //Update Valuation
			 String VAL_MESS = "";
			 
			 def QRY7 = sql.firstRow("select * from msf071 " +
				 "where ENTITY_TYPE = 'CIV' and ENTITY_VALUE = '"+securityToken.getDistrict().trim()+CNT_NO.trim()+CIC_NO.trim()+"' and REF_NO = '001' and SEQ_NUM = '001'");
				 log.info ("FIND VALN_NO  : " + QRY7);
			 if(!QRY7.equals(null)) {
				 if(!QRY7.REF_CODE.trim().equals("")) {
					 VAL_MESS = DEL_VAL(CNT_NO,QRY7.REF_CODE.trim());
				 }
			 }
			 if (!VAL_MESS.equals("")) {
				 StrErr = VAL_MESS
				 log.info ("StrErr  : " + StrErr);
				 StrErr = StrErr.replace("VALUATION", "CIC")
				 if (StrErr.length() > 50) {
					 String[] strKeteranganUpload = StrErr.split("\\n");
					 for(String lstitem : strKeteranganUpload){
						 result.addError(new Error(lstitem))
					 }
				 }else {
					 SetErrMes();
					 com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					 //err.setFieldId("cicNo")
					 result.addError(err)
				 }
				 results.add(result)
				 RollErrMes();
				 return results
			 }
			 
			 try
			 {
				 String QueryUpdate = ("update ACA.KPF38F set CIC_STATUS = ? where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"'");
				 sql.execute(QueryUpdate,["3"]);
				 QueryUpdate = ("update MSF071 set REF_CODE = ? where ENTITY_TYPE = 'CIV' and trim(ENTITY_VALUE) = trim('"+securityToken.getDistrict()+CNT_NO.trim()+CIC_NO.trim()+"') and REF_NO = '001' and SEQ_NUM = '001'");
				 sql.execute(QueryUpdate,[" "]);
				 result.addAttribute("cicValnNo", " ");
			 } catch (Exception  e) {
				 log.info ("Exception is : " + e);
				 StrErr = "EXCEPTION UPDATE ACA.KPF38F : ";
				 SetErrMes();
				 com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541);
				 result.addError(err);
				 results.add(result);
				 RollErrMes();
				 return results
			 }
			 
			 def QRY3;
			 QRY3 = sql.firstRow("select * from ACA.KPF38F where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"'");
			 if(QRY3.equals(null)) {
				 result.addAttribute("cicStat", "");
			 }else {
				 if (QRY3.CIC_STATUS.trim().equals("1")) {
					 result.addAttribute("cicStat", "Estimated");
				 }else if (QRY3.CIC_STATUS.trim().equals("2")) {
					 result.addAttribute("cicStat", "Accepted");
				 }else if (QRY3.CIC_STATUS.trim().equals("3")) {
					 result.addAttribute("cicStat", "Canceled");
				 }else if (QRY3.CIC_STATUS.trim().equals("4")) {
					 result.addAttribute("cicStat", "Invoiced");
				 }else if (QRY3.CIC_STATUS.trim().equals("U")) {
					 result.addAttribute("cicStat", "Awaiting Approval");
				 }else if (QRY3.CIC_STATUS.trim().equals("R")) {
					 result.addAttribute("cicStat", "Rejected");
				 }
			 }
			 
			 result.addAttribute("cntNo", QRY1.CONTRACT_NO);
			 result.addAttribute("cicNo", QRY2.CIC_NO);
			 result.addAttribute("wo", QRY2.WORK_ORDER);
			 result.addInformationalMessage(new UnlocalisedMessage("CIC CANCELED"))
		}
		results.add(result);
		return results;
	}
	private String DEL_VAL(String CNT_NO,String VAL_NO){
		String MESSAGE = "";
		try
		{
			log.info ("DELETE VALUATION:");
			ValuationsServiceDeleteReplyDTO delete = service.get("Valuations").delete({
				it.contractNo = CNT_NO.trim()
				it.valuationNo = VAL_NO.trim()
				},false)
			log.info ("DELETE VALUATION END:");
		}catch (EnterpriseServiceOperationException e){
			log.info ("MASUK EXCEPTION MOD VAL:");
			List <ErrorMessageDTO> listError = e.getErrorMessages()
			listError.each{ErrorMessageDTO errorDTO ->
					log.info ("Erorr Code:" + errorDTO.getCode())
					log.info ("Error Message:" + errorDTO.getMessage())
					log.info ("Error Fields: " + errorDTO.getFieldName())
					MESSAGE = MESSAGE + errorDTO.getMessage() + "\n" ;
				}
		}
		return MESSAGE;
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

}
