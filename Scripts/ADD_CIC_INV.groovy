/**
* @EMS Jan 2019
*
* 20190101 - a9ra5213 - Ricky Afriano - KPC UPGRADE
*            Initial Coding - Add CIC into CIC Invoice Item in ELL38I Screen
**/
package KPC
import javax.naming.InitialContext

import com.mincom.ellipse.app.security.SecurityToken
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
import com.mincom.ellipse.errors.Error;
import com.mincom.ellipse.errors.UnlocalisedError
import com.mincom.ellipse.errors.CobolMessages;

public class ADD_CIC_INV extends GenericScriptPlugin implements GenericScriptExecute{
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
		log.info("Execute ADD_CIC_INV : " + version )
		GenericScriptResults results = new GenericScriptResults()
		GenericScriptResult result = new GenericScriptResult()
		RequestAttributes reqAtt = requestAttributes[0]
		String CNT_NO,INV_NO,CIC_NO;
		INV_NO = reqAtt.getAttributeStringValue("detInvNo");
		CNT_NO = reqAtt.getAttributeStringValue("detCntNo");
		BigDecimal INV_VAL = 0;
		BigDecimal CIC_INV_VAL = 0;
		BigDecimal BALANCE = 0;
		requestAttributes.eachWithIndex {reqAttItem, index ->
			//Validate Contract No
			def QRY1;
			QRY1 = sql.firstRow("select * from msf384 where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) ");
			//log.info ("FIND CONTRACT  : " + QRY1);
			if(QRY1.equals(null)) {
				StrErr = "INVALID CONTRACT NUMBER / DOESN'T EXIST"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("creCntNo")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
			//Validate CIC INV No
			def QRY3;
			QRY3 = sql.firstRow("select * from aca.kpf38i where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CIC_INVOICE)) = upper(trim('"+INV_NO+"')) ");
			log.info ("FIND CIC INV  : ");
			if(QRY3.equals(null)) {
				StrErr = "INVOICE NUMBER DOES NOT EXIST"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("detInvNo")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
			if(!QRY3.CIC_INV_ST.trim().equals("")) {
				StrErr = "CIC INVOICE STATUS MUST BE SPACE"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("detInvNo")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
			INV_VAL = QRY3.INVOICE_VAL
			//Validate CIC NO
			CIC_NO = reqAttItem.getAttributeStringValue("grd2CicNo").toString();
			def QRY2;
			QRY2 = sql.firstRow("select * from ACA.KPF38F where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"'");
			//log.info ("FIND CIC  : " + QRY2);
			if(QRY2.equals(null)) {
				StrErr = "INVALID CIC NUMBER / DOESN'T EXIST"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("grd2CicNo")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}else {
				if(!QRY2.CIC_STATUS.trim().equals("2")) {
					StrErr = "CIC STATUS SHOULD BE ACCEPT"
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					err.setFieldId("grd2CicNo")
					result.addError(err)
					results.add(result)
					RollErrMes();
					return results
				}
				if(QRY2.CIC_INVOICE.trim().equals("")) {
					try
					{
						String QueryUpdate = ("update ACA.KPF38F set CIC_INVOICE = ? where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"'");
						sql.execute(QueryUpdate,[INV_NO]);
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
				}else {
					StrErr = "CIC ALREADY INVOICED!"
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					err.setFieldId("grd2CicNo")
					result.addError(err)
					results.add(result)
					RollErrMes();
					return results
				}
			}
		}
		//RECALCULATE BALANCE
		log.info ("RECALCULATE BALANCE: ");
		def QRY1;
		QRY1 = sql.firstRow("select sum(ACT_COST) SUM_ACT_COST from ACA.KPF38F where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and upper(trim(CIC_INVOICE)) = upper(trim('"+INV_NO+"')) ");
		if(QRY1.SUM_ACT_COST.equals(null)) {
			StrErr = "INVALID CIC INVOICE NUMBER / DOESN'T EXIST"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("detInvNo")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}else {
			try
			{
				CIC_INV_VAL = 0;
				BALANCE = 0;
				BALANCE = INV_VAL - QRY1.SUM_ACT_COST
				CIC_INV_VAL = QRY1.SUM_ACT_COST
				log.info ("TOTAL_CIC_VAL: " + CIC_INV_VAL);
				log.info ("BALANCE: " + BALANCE);
				String QueryUpdate = ("update ACA.KPF38i set TOTAL_CIC_VAL = ?,CIC_BALANCE = ? where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and upper(trim(CIC_INVOICE)) = upper(trim('"+INV_NO+"')) ");
				sql.execute(QueryUpdate,[QRY1.SUM_ACT_COST,BALANCE]);
			} catch (Exception  e) {
				log.info ("Exception is : " + e);
				StrErr = "EXCEPTION UPDATE ACA.KPF38I : ";
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541);
				result.addError(err);
				results.add(result);
				RollErrMes();
				return results
			}
		}
		result.addAttribute("detTotCicVal", CIC_INV_VAL);
		result.addAttribute("detBal", BALANCE);
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