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
import java.math.RoundingMode;
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


public class ell685GridCalc extends GenericScriptPlugin implements GenericScriptExecuteForCollection{
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
	
	
	GenericScriptResults executeForCollection(SecurityToken securityToken, RequestAttributes requestAttributes,
		Integer maxNumberOfObjects, RestartAttributes restartAttributes) throws FatalException{
			log.info("ell685GridCalc executeForCollection : " + version );
			def results = new GenericScriptResults();
			
			RequestAttributes reqAtt = requestAttributes;
			String parAssetNo = reqAtt.getAttributeStringValue("assetNumber");
			String parAssetType = reqAtt.getAttributeStringValue("assetTy");
			String parSubAssetNo = reqAtt.getAttributeStringValue("subAssetNo");
			String parBookType = reqAtt.getAttributeStringValue("bookTy");
			String parStartPeriod = reqAtt.getAttributeStringValue("startPeriod");
			
			if (parAssetNo.equals(null)) {
				parAssetNo = "";
			}
			parAssetNo = String.format("%-12s", parAssetNo )
			if (parSubAssetNo.equals(null)) {
				parSubAssetNo = "";
			}
			parSubAssetNo = String.format("%6s", parSubAssetNo ).replace(' ', '0')
			if (parAssetType.equals(null)) {
				parAssetType = "";
			}
			
			if (parBookType.equals(null)) {
				parBookType = "";
			}
			
			String StrSQL = "";
			log.info("maxNumberOfObjects : " + maxNumberOfObjects );
			DecimalFormat df = new DecimalFormat("#,##0.00;-#,##0.00");
			
			if (restartAttributes.equals(null)){
				   StrSQL = "select row_number () over(order by a.ENTITY_VALUE) NO,a.* from V_ELL685_CALC a where trim(ENTITY_VALUE) = trim('"+securityToken.getDistrict()+parAssetNo+parSubAssetNo+parAssetType+parBookType+"') " + 
							   "ORDER BY a.ENTITY_VALUE OFFSET 0 ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY" ;
				   log.info ("StrSQL : " + StrSQL);
				   sql.eachRow(StrSQL, {
					   GenericScriptResult result = new GenericScriptResult()
					   if (it.GRP.equals("A")) {
						   //result.addAttribute("calcYear", "YEAR " + it.YEAR_SEQ.substring(0,4));
						   result.addAttribute("calcYear", "");
						   //result.addAttribute("calcPct", it.PCT + "%");
						   result.addAttribute("calcPct", "");
						   result.addAttribute("period1", it.MNTH1);
						   result.addAttribute("period2", it.MNTH2);
						   result.addAttribute("period3", it.MNTH3);
						   result.addAttribute("period4", it.MNTH4);
						   result.addAttribute("period5", it.MNTH5);
						   result.addAttribute("period6", it.MNTH6);
						   result.addAttribute("period7", it.MNTH7);
						   result.addAttribute("period8", it.MNTH8);
						   result.addAttribute("period9", it.MNTH9);
						   result.addAttribute("period10", it.MNTH10);
						   result.addAttribute("period11", it.MNTH11);
						   result.addAttribute("period12", it.MNTH12);
					   }else {
						   result.addAttribute("calcYear", "YEAR " + it.YEAR_SEQ.substring(0,4));
						   result.addAttribute("calcPct", it.PCT + "%");
						   Float mnth = Float.parseFloat(it.MNTH1);
						   log.info ("mnth : " + mnth);
						   result.addAttribute("period1", df.format(mnth));
						   result.addAttribute("period2", df.format(mnth));
						   result.addAttribute("period3", df.format(mnth));
						   result.addAttribute("period4", df.format(mnth));
						   result.addAttribute("period5", df.format(mnth));
						   result.addAttribute("period6", df.format(mnth));
						   result.addAttribute("period7", df.format(mnth));
						   result.addAttribute("period8", df.format(mnth));
						   result.addAttribute("period9", df.format(mnth));
						   result.addAttribute("period10", df.format(mnth));
						   result.addAttribute("period11", df.format(mnth));
						   result.addAttribute("period12", df.format(mnth));
					   }
					   result.addAttribute("LAST_ROW", maxNumberOfObjects.toString());
					   results.add(result);
				   })
			}else {
				   log.info("restartAttributes : " + restartAttributes.getAttributeStringValue("LAST_ROW") );
				   Integer MaxInst = Integer.parseInt(restartAttributes.getAttributeStringValue("LAST_ROW"));
				   
				   StrSQL = "select row_number () over(order by a.ENTITY_VALUE) NO,a.* from V_ELL685_CALC a where trim(ENTITY_VALUE) = trim('"+securityToken.getDistrict()+parAssetNo+parSubAssetNo+parAssetType+parBookType+"') " +
							   "ORDER BY a.ENTITY_VALUE OFFSET "+MaxInst.toString()+" ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY" ;
				   log.info ("StrSQL : " + StrSQL);
				   MaxInst = MaxInst + maxNumberOfObjects
				   sql.eachRow(StrSQL, {
					   GenericScriptResult result = new GenericScriptResult()
					   if (it.GRP.equals("A")) {
						   //result.addAttribute("calcYear", "YEAR " + it.YEAR_SEQ.substring(0,4));
						   result.addAttribute("calcYear", "");
						   //result.addAttribute("calcPct", it.PCT + "%");
						   result.addAttribute("calcPct", "");
						   result.addAttribute("period1", it.MNTH1);
						   result.addAttribute("period2", it.MNTH2);
						   result.addAttribute("period3", it.MNTH3);
						   result.addAttribute("period4", it.MNTH4);
						   result.addAttribute("period5", it.MNTH5);
						   result.addAttribute("period6", it.MNTH6);
						   result.addAttribute("period7", it.MNTH7);
						   result.addAttribute("period8", it.MNTH8);
						   result.addAttribute("period9", it.MNTH9);
						   result.addAttribute("period10", it.MNTH10);
						   result.addAttribute("period11", it.MNTH11);
						   result.addAttribute("period12", it.MNTH12);
					   }else {
						   result.addAttribute("calcYear", "YEAR " + it.YEAR_SEQ.substring(0,4));
						   result.addAttribute("calcPct", it.PCT + "%");
						   Float mnth = Float.parseFloat(it.MNTH1);
						   log.info ("mnth : " + mnth);
						   result.addAttribute("period1", df.format(mnth));
						   result.addAttribute("period2", df.format(mnth));
						   result.addAttribute("period3", df.format(mnth));
						   result.addAttribute("period4", df.format(mnth));
						   result.addAttribute("period5", df.format(mnth));
						   result.addAttribute("period6", df.format(mnth));
						   result.addAttribute("period7", df.format(mnth));
						   result.addAttribute("period8", df.format(mnth));
						   result.addAttribute("period9", df.format(mnth));
						   result.addAttribute("period10", df.format(mnth));
						   result.addAttribute("period11", df.format(mnth));
						   result.addAttribute("period12", df.format(mnth));
					   }
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
	private String CREATE_VAL(String CNT_NO,String CONTRACTOR,String VALUED_BY,BigDecimal CON_AMT,String District,String CIC_NO){
		String MESSAGE = "";
		try
		{
			log.info ("CREATE_VAL:");
			ValuationsServiceCreateReplyDTO CRE_REP_DTO = service.get("Valuations").create({
				it.valnTypeFlag = "N"
				//it.getLoggedOnDistrict = "ID"
				//it.valuedByUpdate = "N"
				//it.multiValnAllwd = "N"
				//it.ctlAccountsEnabledSw = "N"
				//it.authSecSW = "N"
				it.extInvAsInv = false
				it.finalValn = false
				//it.paidByClientFlg = "N"
				it.copyPrevValnFlag = false
				//it.extCommentExists = "N"
				//it.intCommentExists = "N"
				//it.displayLDFlag = "N"
				it.contractNo = CNT_NO
				it.contractor = CONTRACTOR
				it.valuedBy = VALUED_BY
				it.cntrctrRefAmt = CON_AMT
				//it.intComment = "CV"
				//it.extComment = "XV"
				},false)
			try
			{
				GetNowDateTime();
				String QueryInsert = (
					"Insert into MSF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY) values ('CIV','"+District.trim()+CNT_NO.trim()+CIC_NO.trim()+"','001','001','"+strCrDT+"','"+strCrTM+"','"+VALUED_BY+"','"+CRE_REP_DTO.getValuationNo()+"','            ')");
				sql.execute(QueryInsert);
			} catch (Exception  e) {
				log.info ("Exception is : " + e);
				log.info ("INSERT EXCEPTION MSF071 CIV:");
				MESSAGE = "INSERT EXCEPTION MSF071 CIV:";
			}
			log.info ("CONTRACT No:" + CRE_REP_DTO.getContractNo());
			log.info ("VAL No:" + CRE_REP_DTO.getValuationNo());
			log.info ("VAL Status:" + CRE_REP_DTO.getValnStatusDescription());
		}catch (EnterpriseServiceOperationException e){
			log.info ("MASUK EXCEPTION:");
			List <ErrorMessageDTO> listError = e.getErrorMessages()
			listError.each{ErrorMessageDTO errorDTO ->
					log.info ("Erorr Code:" + errorDTO.getCode())
					log.info ("Error Message:" + errorDTO.getMessage())
					log.info ("Error Fields: " + errorDTO.getFieldName())
					MESSAGE = errorDTO.getMessage();
				}
		}catch (InteractionNotAuthorisedException e1) {
			log.info ("MASUK EXCEPTION2:");
			MESSAGE = e1.getMessage();
		}
		return MESSAGE;
	}
	private String DELETE_VAL(String CNT_NO,String VALN_NO,String CIC_NO,String District){
		String MESSAGE = "";
		try
		{
			log.info ("DELETE_VAL:");
			ValuationsServiceDeleteReplyDTO DEL_REP_DTO = service.get("Valuations").delete({
				it.contractNo = CNT_NO
				it.valuationNo = VALN_NO
				},false)
			try
			{
				log.info ("DELETE MSF877 & MSF87A:");
				String QueryDelete = (
					"delete msf87A where DSTRCT_CODE = '"+District.trim()+"' and trim(transaction_key) = trim(RPAD('"+CNT_NO+"',8)||'"+VALN_NO+"')");
				sql.execute(QueryDelete);
				
				QueryDelete = (
					"delete msf877 where DSTRCT_CODE = '"+District.trim()+"' and trim(transaction_key) = trim(RPAD('"+CNT_NO+"',8)||'"+VALN_NO+"')");
				sql.execute(QueryDelete);
				
				log.info ("DELETE MSF071 CIV:");
				QueryDelete = (
					"delete msf071 where ENTITY_TYPE = 'CIV' and trim(ENTITY_VALUE) = trim('"+District.trim()+CNT_NO.trim()+CIC_NO.trim()+"')");
				sql.execute(QueryDelete);
				
				log.info ("DELETE ACA.KPF38G:");
				QueryDelete = (
					"delete ACA.KPF38G where DSTRCT_CODE = '"+District.trim()+"' and trim(CONTRACT_NO) = trim('"+CNT_NO+"') and trim(CIC_NO) = trim('"+CIC_NO+"')");
				sql.execute(QueryDelete);
				
				log.info ("DELETE ACA.KPF38F:");
				QueryDelete = (
					"delete ACA.KPF38F where DSTRCT_CODE = '"+District.trim()+"' and trim(CONTRACT_NO) = trim('"+CNT_NO+"') and trim(CIC_NO) = trim('"+CIC_NO+"')");
				sql.execute(QueryDelete);
			} catch (Exception  e) {
				log.info ("Exception is : " + e);
				log.info ("DELETE EXCEPTION MSF071 CIV:");
				MESSAGE = "DELETE EXCEPTION";
			}
		}catch (EnterpriseServiceOperationException e){
			log.info ("MASUK EXCEPTION:");
			List <ErrorMessageDTO> listError = e.getErrorMessages()
			listError.each{ErrorMessageDTO errorDTO ->
					log.info ("Erorr Code:" + errorDTO.getCode())
					log.info ("Error Message:" + errorDTO.getMessage())
					log.info ("Error Fields: " + errorDTO.getFieldName())
					MESSAGE = errorDTO.getMessage();
				}
		}
		return MESSAGE;
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
	public boolean isNumeric2(String str) {
		try {
			str = str.replace(",", "")
			//Integer.parseInt(str);
			Float.parseFloat(str);
			return true;
		  }
		  catch (NumberFormatException e) {
			// s is not numeric
			return false;
		  }
	}
}
