/**
 * @EMS Jan 2019
 *
 * 20190201 - a9ra5213 - Ricky Afriano - KPC UPGRADE Ellipse 8
 *            Initial Coding - Search CIC in ELL38I Detail Screen 
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

public class CIC_SEARCH extends GenericScriptPlugin implements GenericScriptExecuteForCollection, GenericScriptUpdate, GenericScriptCreate, GenericScriptDelete{
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
	
	public GenericScriptResults executeForCollection(SecurityToken securityToken, RequestAttributes requestAttributes,
		Integer maxNumberOfObjects, RestartAttributes restartAttributes) throws FatalException {
		log.info("Execute Colection CIC_SEARCH : " + version )
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
	public GenericScriptResults create(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings)
	throws FatalException {
		log.info("Create CIC_SEARCH : " + version )
		GenericScriptResults results = new GenericScriptResults()
		GenericScriptResult result = new GenericScriptResult()
		RequestAttributes reqAtt = requestAttributes[0]
		
		results.add(result)
		return results
	}
	public GenericScriptResults update(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings)
	throws FatalException {
		log.info("Update CIC_SEARCH : " + version )
		GenericScriptResults results = new GenericScriptResults()
		GenericScriptResult result = new GenericScriptResult()
		RequestAttributes reqAtt = requestAttributes[0]
		
		results.add(result)
		return results
	}
	public GenericScriptResults delete(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings)
	throws FatalException {
		log.info("Delete CIC_SEARCH : " + version )
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