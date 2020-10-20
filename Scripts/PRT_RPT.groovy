package KPC
/**
 * @EMS Feb 2019
 *
 * 20190201 - a9ra5213 - Ricky Afriano - KPC UPGRADE Ellipse 8
 *            Initial Coding - Print CIC via ELL38C Search Screen 
 *            and Detail Sreen
 **/
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
import com.mincom.ellipse.errors.UnlocalisedError
import com.mincom.ellipse.errors.UnlocalisedMessage
import com.mincom.ellipse.*
import com.mincom.ellipse.edoi.ejb.msf071.MSF071Key
import com.mincom.ellipse.edoi.ejb.msf071.MSF071Rec
import com.mincom.ellipse.ejra.mso.GenericMsoRecord
import com.mincom.ellipse.client.connection.*
import com.mincom.ellipse.ejra.mso.*;
import com.mincom.enterpriseservice.exception.*;


public class PRT_RPT extends GenericScriptPlugin implements GenericScriptExecute {
	String version = "1";
	String strPmtId = ""
	String StrErr = "";
	String ErrorMessage = ""
	
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	
	EllipseScreenService screenService = EllipseScreenServiceLocator.ellipseScreenService;
	ConnectionId msoCon = ConnectionHolder.connectionId;
	GenericMsoRecord screen = new GenericMsoRecord();
	Boolean LOOPFLAG = false;
	
	String MED = "";
	String PRINTER = "";
	String REP_NAME = "";
	String District = "";
	String CNT_NO = "";
	String CIC_NO = "";
	String WO_NO = "";
	
	String strCrDT = "";
	String strCrTM = "";
	String StrDT = "";
	String StrMT = "";
	String StrYR = "";
	String StrHH = "";
	String StrMM = "";
	String StrSS = "";
	
	public GenericScriptResults execute(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings)
	throws FatalException {
		log.info("Execute PAY_VCR : " + version )
		GenericScriptResults results = new GenericScriptResults()
		GenericScriptResult result = new GenericScriptResult()
		RequestAttributes reqAtt = requestAttributes[0]
		
		CNT_NO = reqAtt.getAttributeStringValue("parGrdCntNo")
		CIC_NO = reqAtt.getAttributeStringValue("parGrdCicNo")
		WO_NO = reqAtt.getAttributeStringValue("parGrdWoNo")
		log.info("CNT_NO1 : " + CNT_NO )
		log.info("CIC_NO1 : " + CIC_NO )
		log.info("WO_NO1 : " + WO_NO )
		if (CNT_NO.equals(null)) {
			CNT_NO = reqAtt.getAttributeStringValue("cntNo")
		}
		if (CIC_NO.equals(null)) {
			CIC_NO = reqAtt.getAttributeStringValue("cicNo")
		}
		if (WO_NO.equals(null)) {
			WO_NO = reqAtt.getAttributeStringValue("wo")
		}
		if (WO_NO.equals(null)) {
			WO_NO = "";
		}
		log.info("CNT_NO2 : " + CNT_NO )
		log.info("CIC_NO2 : " + CIC_NO )
		log.info("WO_NO2 : " + WO_NO )
		
		CNT_NO = CNT_NO.toUpperCase()
		CIC_NO = CIC_NO.toUpperCase()
		WO_NO = WO_NO.toUpperCase()
		District = securityToken.getDistrict()
		log.info ("District : " + District);
		/*
		log.info ("REP_NAME : " + reqAtt.getAttributeStringValue("REP_NAME"));
		
		if (!reqAtt.getAttributeStringValue("REP_NAME").equals("BRJPMV")&&!reqAtt.getAttributeStringValue("REP_NAME").equals("BRJCRV")){
			StrErr = "REPORT DOES NOT EXIST !"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		*/
		String EMP_ID = GetUserEmpID(securityToken.getUserId(), securityToken.getDistrict());
		def QRY0;
		QRY0 = sql.firstRow("select a.EMPLOYEE_ID,a.POSITION_ID,e.GLOBAL_PROFILE, " + 
			"case when substr(f.PROFILE,IDX_CIC,1) < NILAI_GP_CIC then 'FALSE' else 'TRUE' end SEC_CIC, " +
			"case when substr(f.PROFILE,IDX_389,1) < NILAI_GP_389 then 'FALSE' else 'TRUE' end SEC_389, " +
			"case when substr(f.PROFILE,IDX_62C,1) < NILAI_GP_62C then 'FALSE' else 'TRUE' end SEC_62C " +
			"from msf878 a  " +
			"left outer join msf870 e on (a.POSITION_ID = e.POSITION_ID) " + 
			"left outer join msf020 f on (trim(e.GLOBAL_PROFILE) = trim(f.entity) and entry_type = 'G') " + 
			"left outer join (  " +
			"select substr(trim(PROFILE),LENGTH(trim(PROFILE)),1) NILAI_GP_CIC,LENGTH(trim(PROFILE)) IDX_CIC from msf020 " + 
			"where ENTRY_TYPE = 'P' and entity = 'KPJCIC01'  " +
			") g on (1=1)  " +
			"left outer join ( " + 
			"select substr(trim(PROFILE),LENGTH(trim(PROFILE)),1) NILAI_GP_389,LENGTH(trim(PROFILE)) IDX_389 from msf020 " + 
			"where ENTRY_TYPE = 'P' and entity = 'KPJ38901'  " +
			") h on (1=1)  " +
			"left outer join (  " +
			"select substr(trim(PROFILE),LENGTH(trim(PROFILE)),1) NILAI_GP_62C,LENGTH(trim(PROFILE)) IDX_62C from msf020 " + 
			"where ENTRY_TYPE = 'P' and entity = 'KPJ62C01'  " +
			") i on (1=1)  " +
			"where a.EMPLOYEE_ID = '"+EMP_ID+"' and (a.POS_STOP_DATE = '00000000' or a.POS_STOP_DATE = ' ' or (case when a.POS_STOP_DATE not in ('00000000',' ') then to_date(a.POS_STOP_DATE,'YYYYMMDD') else null end >= sysdate) ) and trim(a.POSITION_ID) = trim('"+securityToken.getRole()+"')") ;
		if(!QRY0.equals(null)) {
			log.info("QRY0 : " + QRY0 );
			if(QRY0.SEC_CIC.trim().equals("FALSE")) {
				StrErr = "YOU DO NOT HAVE ACCESS TO KPJCIC"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				//err.setFieldId("CRE_CNT_NO")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
			if(QRY0.SEC_62C.trim().equals("FALSE")) {
				StrErr = "YOU DO NOT HAVE ACCESS TO KPJ62C"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				//err.setFieldId("CRE_CNT_NO")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
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
		REP_NAME = "";
		def QRY1;
		QRY1 = sql.firstRow("select * from ACA.KPF38F where DSTRCT_CODE = '"+District+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"'");
		//log.info ("FIND CIC  : " + QRY2);
		if(!QRY1.equals(null)) {
			if (QRY1.CIC_TYPE.trim().equals("wo")) {
				REP_NAME = "KPJ62C";
			}else {
				REP_NAME = "KPJCIC";
			}
		}
		
		MED = reqAtt.getAttributeStringValue("med").toUpperCase()
		log.info ("MED : " + MED);
		PRINTER = reqAtt.getAttributeStringValue("printerName")
		if (PRINTER.equals(null)) {
			PRINTER = "";
		}
		log.info ("PRINTER : " + PRINTER);
		if (MED.equals("P")) {
			if (PRINTER.equals("")) {
				StrErr = "PRINTER NAME REQUIRED !"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
		}
		/*
		VERIFIED_BY = reqAtt.getAttributeStringValue("VERIFIED_BY").toUpperCase()
		log.info ("VERIFIED_BY : " + VERIFIED_BY);
		APPR_BY = reqAtt.getAttributeStringValue("APPR_BY").toUpperCase()
		log.info ("APPR_BY : " + APPR_BY);
		*/
		if (!REP_NAME.equals("")) {
			invoke_Report()
		}
		
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
	private String invoke_Report(){
		
		log.info("----------------------------------------------")
		log.info("how_to_invoke_service - screen service - Start")
		log.info("----------------------------------------------")
		
		GenericMsoRecord screen = screenService.executeByName(msoCon, "MSO080");
		LOOPFLAG = false;
		MainMSO(screen);
		log.info("MSO ID : " + msoCon.getId())
		log.info("MSO SCREEN : " + screen.mapname.trim())
		
		if ( screen.mapname.trim().equals(new String("MSM080A")) ) {
			screen.setFieldValue("RESTART1I", REP_NAME);
				
			screen.nextAction = GenericMsoRecord.TRANSMIT_KEY;
			screen = screenService.execute(msoCon, screen);
			
			if (isErrorOrWarning(screen) ) {
				log.info("Error Message:" + screen.getErrorString())
				ErrorMessage = screen.getErrorString();
				return ErrorMessage
			}
		}
		
		log.info("MSO SCREEN : " + screen.mapname.trim())
		if ( screen.mapname.trim().equals(new String("MSM080A")) ) {
			screen.setFieldValue("SKLITEM1I", "1");
				
			screen.nextAction = GenericMsoRecord.TRANSMIT_KEY;
			screen = screenService.execute(msoCon, screen);
			
			if (isErrorOrWarning(screen) ) {
				log.info("Error Message:" + screen.getErrorString())
				ErrorMessage = screen.getErrorString();
				return ErrorMessage
			}
		}
		
		log.info("MSO SCREEN : " + screen.mapname.trim())
		if ( screen.mapname.trim().equals(new String("MSM080B")) ) {
			screen.setFieldValue("MEDIUM2I", MED);
			if (MED.equals("R")){
				screen.setFieldValue("PUB_TYPE2I", "PDF");
			}else{
				screen.setFieldValue("PRINTER_NAME2I", PRINTER);
			}
			screen.setFieldValue("SUBMIT_FLG2I", "Y");
	 
			screen.nextAction = GenericMsoRecord.TRANSMIT_KEY;
			screen = screenService.execute(msoCon, screen);
			
			if (isErrorOrWarning(screen) ) {
				log.info("Error Message:" + screen.getErrorString())
				ErrorMessage = screen.getErrorString();
				return ErrorMessage
			}
		}
		
		log.info("MSO SCREEN : " + screen.mapname.trim())
		if ( screen.mapname.trim().equals(new String("MSM080C")) ) {
			if (REP_NAME.equals("KPJ62C")) {
				log.info ("REP_NAME1 : " + REP_NAME);
				screen.setFieldValue("PARM3I1", WO_NO)
				screen.setFieldValue("PARM3I2", CIC_NO)
				screen.setFieldValue("PARM3I3", CNT_NO)
				screen.setFieldValue("PARM3I4", District)
			}else {
				log.info ("REP_NAME2 : " + REP_NAME);
				screen.setFieldValue("PARM3I1", CIC_NO)
				screen.setFieldValue("PARM3I2", CNT_NO)
				screen.setFieldValue("PARM3I3", District)
			}
			
			screen.nextAction = GenericMsoRecord.TRANSMIT_KEY;
			screen = screenService.execute(msoCon, screen);
			log.info(screen.getMapname())
			if (isErrorOrWarning(screen) ) {
				log.info("Error Message:" + screen.getErrorString())
				ErrorMessage = screen.getErrorString();
				return ErrorMessage
			}
		}
		
		log.info("MSO SCREEN : " + screen.mapname.trim())
		if ( screen.mapname.trim().equals(new String("MSM080B")) ) {
			screen.setFieldValue("DESC_LINE_22I", "")
			screen.nextAction = GenericMsoRecord.F3_KEY;
			screen = screenService.execute(msoCon, screen);
			log.info(screen.getMapname())
			if (isErrorOrWarning(screen) ) {
				log.info("Error Message:" + screen.getErrorString())
				ErrorMessage = screen.getErrorString();
				return ErrorMessage
			}
		}
		
		log.info("MSO SCREEN : " + screen.mapname.trim())
		if ( screen.mapname.trim().equals(new String("MSM080A")) ) {
			screen.setFieldValue("RESTART1I", "")
			screen.nextAction = GenericMsoRecord.F3_KEY;
			screen = screenService.execute(msoCon, screen);
			log.info(screen.getMapname())
			if (isErrorOrWarning(screen) ) {
				log.info("Error Message:" + screen.getErrorString())
				ErrorMessage = screen.getErrorString();
				return ErrorMessage
			}
		}
		LOOPFLAG = false;
		MainMSO(screen);
		log.info ("-----------------------------");
	}
	private def MainMSO(GenericMsoRecord screen){
		log.info("MAIN MSO : ")
		while(LOOPFLAG.equals(false)) {
			screen.setNextAction(GenericMsoRecord.F3_KEY);
			screen = screenService.execute(msoCon, screen);
			if ( screen.mapname.trim().equals(new String("MSM080A")) ) {
				LOOPFLAG = true
			}
		}
		LOOPFLAG = false
		
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
	private boolean isErrorOrWarning(GenericMsoRecord screen) {
		
		return ((char)screen.errorType) == MsoErrorMessage.ERR_TYPE_ERROR || ((char)screen.errorType) == MsoErrorMessage.ERR_TYPE_WARNING;
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