/**
 * @EMS Jan 2019
 *
 * 20190201 - a9ra5213 - Ricky Afriano - KPC UPGRADE Ellipse 8
 *            Initial Coding - Search CIC in ELL38I Detail Screen 
 **/
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

public class CIC_SEARCH extends GenericScriptPlugin implements GenericScriptExecuteForCollection, GenericScriptUpdate, GenericScriptCreate, GenericScriptDelete, GenericScriptExecute{
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	
	String version = "2";
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

	GenericScriptResults executeForCollection(SecurityToken securityToken, RequestAttributes requestAttributes, Integer maxNumberOfObjects, RestartAttributes restartAttributes) throws FatalException {
		log.info("Execute Collection CIC_SEARCH : " + version )
		GenericScriptResults results = new GenericScriptResults()
		RequestAttributes reqAtt = requestAttributes
		
		String CNT_NO = "";
		if (reqAtt.getAttributeStringValue("grdCntNo").equals(null)) {
			CNT_NO = reqAtt.getAttributeStringValue("detCntNo");
		}else {
			CNT_NO = reqAtt.getAttributeStringValue("grdCntNo");
		}
		
		String PAR_CIC_FROM = "";
		if (reqAtt.getAttributeStringValue("parCicFrom").equals(null)) {
			PAR_CIC_FROM = "";
		}else {
			PAR_CIC_FROM = reqAtt.getAttributeStringValue("parCicFrom");
		}
		
		String PAR_CIC_TO = "";
		if (reqAtt.getAttributeStringValue("parCicTo").equals(null)) {
			PAR_CIC_TO = "";
		}else {
			PAR_CIC_TO = reqAtt.getAttributeStringValue("parCicTo");
		}
		
		if(!PAR_CIC_FROM.trim().equals("")) {
			PAR_CIC_FROM = " AND CIC_NO >= '" + PAR_CIC_FROM + "' ";
		}
		
		if(!PAR_CIC_TO.trim().equals("")) {
			PAR_CIC_TO = " AND CIC_NO <= '" + PAR_CIC_TO + "' ";
		}
		
		DecimalFormat df = new DecimalFormat("#,##0.00;-#,##0.00");
		String StrSQL = ""
		log.info("maxNumberOfObjects : " + maxNumberOfObjects );
		if (restartAttributes.equals(null)){
			
			StrSQL = "select row_number () over(order by a.CIC_NO) NO,a.*,b.WO_DESC from ACA.KPF38F a " +
				"left outer join msf620 b on (a.DSTRCT_CODE = b.DSTRCT_CODE and a.WORK_ORDER = b.WORK_ORDER) " +
				"where upper(trim(a.CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and a.dstrct_code = '"+securityToken.getDistrict()+"' and a.CIC_STATUS = '2' and CIC_INVOICE = ' ' " + PAR_CIC_FROM + PAR_CIC_TO +
				"Order by a.CIC_NO OFFSET 0 ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY";
			log.info ("StrSQL : " + StrSQL);
			
			sql.eachRow(StrSQL, {
				
				GenericScriptResult result = new GenericScriptResult();
				result.addAttribute("grd2CicNo", it.CIC_NO);
				result.addAttribute("grd2CicDesc", it.CIC_DESC);
				if (it.CIC_STATUS.trim().equals("1")) {
					result.addAttribute("grd2CicStat", it.CIC_STATUS + " - Estimated");
				}else if (it.CIC_STATUS.trim().equals("2")) {
					result.addAttribute("grd2CicStat", it.CIC_STATUS + " - Accepted");
				}else if (it.CIC_STATUS.trim().equals("3")) {
					result.addAttribute("grd2CicStat", it.CIC_STATUS + " - Cancel");
				}else if (it.CIC_STATUS.trim().equals("4")) {
					result.addAttribute("grd2CicStat", it.CIC_STATUS + " - Invoiced");
				}else if (it.CIC_STATUS.trim().equals("U")) {
					result.addAttribute("grd2CicStat", it.CIC_STATUS + " - Awaiting Approval");
				}else if (it.CIC_STATUS.trim().equals("R")) {
					result.addAttribute("grd2CicStat", it.CIC_STATUS + " - Rejected");
				}
				result.addAttribute("grd2ActCst", df.format(it.ACT_COST));
				result.addAttribute("grd2WoNo", it.WORK_ORDER);
				result.addAttribute("grd2CntNo", it.CONTRACT_NO);
				if(it.WO_DESC.equals(null)) {
					result.addAttribute("grd2WoDesc", " ");
				}else {
					result.addAttribute("grd2WoDesc", it.WO_DESC);
				}
				result.addAttribute("lastRow", maxNumberOfObjects.toString());
				results.add(result);
			})		
		}else {
			log.info("restartAttributes : " + restartAttributes.getAttributeStringValue("lastRow") );
		    Integer MaxInst = Integer.parseInt(restartAttributes.getAttributeStringValue("lastRow"));
		    //MaxInst = MaxInst + maxNumberOfObjects
			StrSQL = "select row_number () over(order by a.CIC_NO) NO,a.*,b.WO_DESC from ACA.KPF38F a " +
				"left outer join msf620 b on (a.DSTRCT_CODE = b.DSTRCT_CODE and a.WORK_ORDER = b.WORK_ORDER) " + 
				"where upper(trim(a.CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and a.dstrct_code = '"+securityToken.getDistrict()+"' and a.CIC_STATUS = '2' and CIC_INVOICE = ' ' "  + PAR_CIC_FROM + PAR_CIC_TO +
				"Order by a.CIC_NO OFFSET "+MaxInst.toString()+" ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY";
			log.info ("StrSQL : " + StrSQL);
			sql.eachRow(StrSQL, {
				GenericScriptResult result = new GenericScriptResult();
				MaxInst = it.NO
				result.addAttribute("grd2CicNo", it.CIC_NO);
				result.addAttribute("grd2CicDesc", it.CIC_DESC);
				if (it.CIC_STATUS.trim().equals("1")) {
					result.addAttribute("grd2CicStat", it.CIC_STATUS + " - Estimated");
				}else if (it.CIC_STATUS.trim().equals("2")) {
					result.addAttribute("grd2CicStat", it.CIC_STATUS + " - Accepted");
				}else if (it.CIC_STATUS.trim().equals("3")) {
					result.addAttribute("grd2CicStat", it.CIC_STATUS + " - Cancel");
				}else if (it.CIC_STATUS.trim().equals("4")) {
					result.addAttribute("grd2CicStat", it.CIC_STATUS + " - Invoiced");
				}else if (it.CIC_STATUS.trim().equals("U")) {
					result.addAttribute("grd2CicStat", it.CIC_STATUS + " - Awaiting Approval");
				}else if (it.CIC_STATUS.trim().equals("R")) {
					result.addAttribute("grd2CicStat", it.CIC_STATUS + " - Rejected");
				}
				result.addAttribute("grd2ActCst", df.format(it.ACT_COST));
				result.addAttribute("grd2WoNo", it.WORK_ORDER);
				result.addAttribute("grd2CntNo", it.CONTRACT_NO);
				if(it.WO_DESC.equals(null)) {
					result.addAttribute("grd2WoDesc", " ");
				}else {
					result.addAttribute("grd2WoDesc", it.WO_DESC);
				}
				result.addAttribute("lastRow", MaxInst.toString());
				results.add(result);
			})
		}
		return results
	}

	GenericScriptResults create(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings) throws FatalException {
		log.info("Create CIC_SEARCH : " + version )
		GenericScriptResults results = new GenericScriptResults()
		GenericScriptResult result = new GenericScriptResult()
		RequestAttributes reqAtt = requestAttributes[0]
		
		results.add(result)
		return results
	}

	GenericScriptResults update(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings) throws FatalException {
		log.info("Update CIC_SEARCH : " + version )
		GenericScriptResults results = new GenericScriptResults()
		GenericScriptResult result = new GenericScriptResult()
		RequestAttributes reqAtt = requestAttributes[0]
		
		results.add(result)
		return results
	}

	GenericScriptResults delete(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings) throws FatalException {
		log.info("Delete CIC_SEARCH : " + version )
		GenericScriptResults results = new GenericScriptResults()
		GenericScriptResult result = new GenericScriptResult()
		
		results.add(result)
		return results
	}

	GenericScriptResults execute(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings) throws FatalException {
		log.info("Execute ADD_CIC_INV : " + version )
		GenericScriptResults results = new GenericScriptResults()
		GenericScriptResult result = new GenericScriptResult()
		RequestAttributes reqAtt = requestAttributes[0]

		String CNT_NO
		String INV_NO
		String CIC_NO

		INV_NO = reqAtt.getAttributeStringValue("detInvNo");
		CNT_NO = reqAtt.getAttributeStringValue("detCntNo");

		BigDecimal INV_VAL = 0
		BigDecimal CIC_INV_VAL = 0
		BigDecimal BALANCE = 0

		requestAttributes.eachWithIndex {RequestAttributes reqAttItem, Integer index ->
			//Validate Contract No
			def QRY1
			QRY1 = sql.firstRow("select * from msf384 where DSTRCT_CODE = '${securityToken.getDistrict()}' and upper(trim(CONTRACT_NO)) = upper(trim('$CNT_NO')) ")

			if(!QRY1) {
				StrErr = "INVALID CONTRACT NUMBER / DOESN'T EXIST"
				SetErrMes()
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("creCntNo")
				result.addError(err)
				results.add(result)
				RollErrMes()
				return results
			}
			//Validate CIC INV No
			def QRY3
			QRY3 = sql.firstRow("select * from aca.kpf38i where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CIC_INVOICE)) = upper(trim('"+INV_NO+"')) ");
			log.info ("FIND CIC INV  : ");

			if(!QRY3) {
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
						String QueryUpdate = ("update ACA.KPF38F set CIC_INVOICE = ? where DSTRCT_CODE = '${securityToken.getDistrict()}' and upper(trim(CONTRACT_NO)) = upper(trim('$CNT_NO')) and CIC_NO = '$CIC_NO'")
						sql.execute(QueryUpdate,[INV_NO])
					} catch (Exception  e) {
						log.info ("Exception is : " + e)
						StrErr = "EXCEPTION UPDATE ACA.KPF38F: "
						SetErrMes();
						com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
						result.addError(err)
						results.add(result)
						RollErrMes()
						return results
					}
				}else {
					StrErr = "CIC ALREADY INVOICED!"
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					err.setFieldId("grd2CicNo")
					result.addError(err)
					results.add(result)
					RollErrMes()
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
				CIC_INV_VAL = 0
				BALANCE = 0
				BALANCE = INV_VAL - QRY1.SUM_ACT_COST
				CIC_INV_VAL = QRY1.SUM_ACT_COST
				log.info ("TOTAL_CIC_VAL: " + CIC_INV_VAL)
				log.info ("BALANCE: " + BALANCE)
				String QueryUpdate = ("update ACA.KPF38i set TOTAL_CIC_VAL = ?,CIC_BALANCE = ? where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and upper(trim(CIC_INVOICE)) = upper(trim('"+INV_NO+"')) ")
				sql.execute(QueryUpdate,[QRY1.SUM_ACT_COST,BALANCE])
			} catch (Exception  e) {
				log.info ("Exception is : " + e)
				StrErr = "EXCEPTION UPDATE ACA.KPF38I : "
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				result.addError(err)
				results.add(result)
				RollErrMes()
				return results
			}
		}
		result.addAttribute("detTotCicVal", CIC_INV_VAL)
		result.addAttribute("detBal", BALANCE)
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

	def GetNowDateTime() {
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