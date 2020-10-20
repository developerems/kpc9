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


public class ell685Upload extends GenericScriptPlugin implements GenericScriptExecute{
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
		log.info("Execute ell685Upload Execute : " + version );
		GenericScriptResults results = new GenericScriptResults();
		GenericScriptResult result = new GenericScriptResult();
		RequestAttributes reqAtt = requestAttributes[0];
		
		String assetNo = "";
		if (!reqAtt.getAttributeStringValue("assetNumber").equals(null)) {
			assetNo = reqAtt.getAttributeStringValue("assetNumber");
		}else {
			assetNo = reqAtt.getAttributeStringValue("assetNo");
		}
		String subAssetNo = "";
		if (!reqAtt.getAttributeStringValue("subAssetNo").equals(null)) {
			subAssetNo = reqAtt.getAttributeStringValue("subAssetNo");
		}else {
			subAssetNo = reqAtt.getAttributeStringValue("parSubAssetNo");
		}
		String bookTy = "";
		if (!reqAtt.getAttributeStringValue("bookTy").equals(null)) {
			bookTy = reqAtt.getAttributeStringValue("bookTy");
		}else {
			bookTy = reqAtt.getAttributeStringValue("parBookTy");
		}
				
		log.info("assetNo : " + assetNo );
		log.info("subAssetNo : " + subAssetNo );
		log.info("bookTy : " + bookTy );
		
		String assetTy = "";
		
		if (assetNo.equals(null) || assetNo.equals("")) {
			StrErr = "ASSET NUMBER REQUIRED!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("assetNumber")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		
		assetNo = String.format("%-12s", assetNo )
		
		if (subAssetNo.equals(null) || subAssetNo.equals("")) {
			StrErr = "SUB ASSET NUMBER REQUIRED!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("subAssetNo")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		
		if (bookTy.equals(null) || bookTy.equals("")) {
			StrErr = "BOOK TYPE REQUIRED!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("bookTy")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		
		def QRY0;
		QRY0 = sql.firstRow("select * from msf685 where dstrct_code = '"+securityToken.getDistrict()+"' and trim(asset_no) = upper(trim('"+assetNo+"')) and trim(SUB_ASSET_NO) = upper(trim('"+subAssetNo+"')) ");
		if(QRY0.equals(null)) {
			StrErr = "INVALID ASSET NO OR SUB ASSET NUMBER!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			//err.setFieldId("CRE_CNT_NO")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}else {
			assetTy = QRY0.ASSET_TY
		}
		
		QRY0 = sql.firstRow("select * from msf010 where table_type = 'TX' and trim(table_code) = upper(trim('"+bookTy+"')) ");
		if(QRY0.equals(null)) {
			StrErr = "INVALID BOOK TYPE!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			//err.setFieldId("CRE_CNT_NO")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		
		QRY0 = sql.firstRow("select sum(case when to_number(trim(REF_CODE)) is null then 0 else to_number(trim(REF_CODE)) end) ACT_PCT from msf071 where ENTITY_TYPE = 'CMD' and REF_NO = '002' and trim(ENTITY_VALUE) = trim('"+securityToken.getDistrict()+assetNo+subAssetNo+assetTy+bookTy+"') ");
		log.info ("QRY ACT PCT : " + QRY0);
		if(!QRY0.ACT_PCT.equals(null)) {
			log.info ("TOTAL PCT : " + QRY0.ACT_PCT);
			if (QRY0.ACT_PCT < 100.00) {
				StrErr = "TOTAL PERCENTAGE MUST BE 100.00!"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("pct")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
		}
				
		String EMP_ID = GetUserEmpID(securityToken.getUserId(), securityToken.getDistrict())
		try
		{
			BigDecimal ccost = 0;
			BigDecimal calcManDepr = 0;
			DecimalFormat df = new DecimalFormat("###0.00;-###0.00");
			def QRY2 = sql.firstRow("select * from msf686 where DSTRCT_CODE = '"+securityToken.getDistrict()+"' " + 
					"and trim(ASSET_NO) = trim('"+assetNo+"') and SUB_ASSET_NO = trim('"+subAssetNo+"') and  ASSET_TY = trim('"+assetTy+"') and DEPR_REC_TYPE = trim('"+bookTy+"') ");
			log.info ("FIND CCOST  : " + QRY2);
			if(!QRY2.equals(null)) {
				ccost = QRY2.CCOST_L_CV
				def QRY1 = sql.firstRow("select * from ( " +
					"select a.*,case when a.asset_period between YEAR_SEQ and LAST_PER then 'TRUE' else ' ' end CURR_FLAG from ( " +
					"select a.*,'20'||b.CURR_ACCT_YR||b.CURR_ACCT_MN asset_period, " +
					"to_char(to_date('01'||' '||mnth12,'DD MON YYYY'),'YYYYMM') LAST_PER from V_ELL685_CALC a " +
					"left outer join MSF000_CP b on (b.DSTRCT_CODE = substr(a.ENTITY_VALUE,1,4) and b.CONTROL_REC_NO = '0005') " +
					"where trim(a.ENTITY_VALUE) = trim('"+securityToken.getDistrict()+assetNo+subAssetNo+assetTy+bookTy+"') and a.GRP = 'A') a) where CURR_FLAG = 'TRUE'");
				log.info ("FIND CURRENT FLAG  : " + QRY1);
				if(!QRY1.equals(null)) {
					Float newPct = Float.parseFloat(QRY1.PCT);
					log.info ("newPct  : " + newPct);
					BigDecimal newPct2 = new BigDecimal(newPct);
					log.info ("newPct2  : " + newPct2);
					calcManDepr = (ccost * newPct2) / 100 / 12;
					log.info ("calcManDepr  : " + calcManDepr);
					Float decCalcManDepr = Float.parseFloat(df.format(calcManDepr));
					//calcManDepr = (ccost * QRY1.PCT) / 100
					//String strCalcManDepr = df.format(calcManDepr)
					String QueryUpdate = (
						"update msf686 set DEPR_METHOD = ?,DEPR_RATE = ?,FIN_MAN_PER = ?,MAN_PER_DEPR = ? where DSTRCT_CODE = '"+securityToken.getDistrict()+"' " +
						"and trim(ASSET_NO) = trim('"+assetNo+"') and SUB_ASSET_NO = trim('"+subAssetNo+"') and  ASSET_TY = trim('"+assetTy+"') and DEPR_REC_TYPE = trim('"+bookTy+"') "
						);
					sql.execute(QueryUpdate,["S",QRY1.PCT,QRY1.LAST_PER,decCalcManDepr]);
					result.addInformationalMessage(new UnlocalisedMessage("DATA UPLOADED"))
				}else {
					result.addInformationalMessage(new UnlocalisedMessage("DATA NOT UPLOADED"))
				}
			}else {
				result.addInformationalMessage(new UnlocalisedMessage("DATA NOT UPLOADED"))
			}
		} catch (Exception  e) {
			log.info ("Exception is : " + e);
			log.info ("UPDATE EXCEPTION MSF686 CMD:");
			StrErr = "UPDATE EXCEPTION MSF686 CMD!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			//err.setFieldId("CRE_CNT_NO")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		
		result.addAttribute("assetNumber", assetNo);
		result.addAttribute("subAssetNo", subAssetNo);
		result.addAttribute("bookTy", bookTy);
		result.addAttribute("assetTy", assetTy);

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
}
