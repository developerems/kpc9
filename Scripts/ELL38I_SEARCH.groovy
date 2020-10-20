/**
 * @EMS Feb 2019
 *
 * 20190201 - a9ra5213 - Ricky Afriano - KPC UPGRADE Ellipse 8
 *            Initial Coding - Search CIC Invoice in ELL38I Search Screen 
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


public class ELL38I_SEARCH extends GenericScriptPlugin implements GenericScriptExecuteForCollection {
	String version = "1";
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	String StrErr = "";
	
	GenericScriptResults executeForCollection(SecurityToken securityToken, RequestAttributes requestAttributes,
		Integer maxNumberOfObjects, RestartAttributes restartAttributes) throws FatalException{
			log.info("ELL38I_SEARCH executeForCollection : " + version );
			def results = new GenericScriptResults();
			
			RequestAttributes reqAtt = requestAttributes;
			String CNT = reqAtt.getAttributeStringValue("parCntNo2");
			String INV = reqAtt.getAttributeStringValue("parInvNo");
			String INV_BY = reqAtt.getAttributeStringValue("parInpBy");
			String INV_ST = reqAtt.getAttributeStringValue("parInvStat");
			
			if (CNT == null) {
				CNT = "";
			}else {
				def QRY1;
				QRY1 = sql.firstRow("select * from msf384 where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT+"')) ");
				//log.info ("FIND CONTRACT  : " + QRY1);
				if(QRY1.equals(null)) {
					GenericScriptResult result = new GenericScriptResult()
					StrErr = "INVALID CONTRACT NUMBER"
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					err.setFieldId("parCntNo2")
					result.addError(err)
					results.add(result)
					RollErrMes();
					return results
				}
				CNT = " and upper(trim(CONTRACT_NO)) = upper(trim('" + CNT + "')) ";
			}
			
			if (INV.equals(null)) {
				INV = "";
			}else {
				def QRY1;
				QRY1 = sql.firstRow("select * from ACA.KPF38I where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CIC_INVOICE)) = upper(trim('"+INV+"')) ");
				log.info ("FIND INV  : " + QRY1);
				if(QRY1.equals(null)) {
					GenericScriptResult result = new GenericScriptResult()
					StrErr = "INVALID INVOICE NUMBER"
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					err.setFieldId("parInvNo")
					result.addError(err)
					results.add(result)
					RollErrMes();
					return results
				}
				INV = " and upper(trim(CIC_INVOICE)) = upper(trim('" + INV + "')) ";
			}
			
			if (INV_BY.equals(null)) {
				INV_BY = "";
			}else {
				def QRY1;
				QRY1 = sql.firstRow("select * from MSF810 where upper(trim(EMPLOYEE_ID)) = upper(trim('"+INV_BY+"')) ");
				log.info ("FIND EMPLOYEE  : " + QRY1);
				if(QRY1.equals(null)) {
					GenericScriptResult result = new GenericScriptResult()
					StrErr = "INVALID EMPLOYEE_ID NUMBER"
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					err.setFieldId("parInpBy")
					result.addError(err)
					results.add(result)
					RollErrMes();
					return results
				}
				INV_BY = " and upper(trim(INPUT_BY)) = upper(trim('" + INV_BY + "')) ";
			}
			
			if (INV_ST.equals(null)) {
				INV_ST = "";
			}else {
				def QRY1;
				QRY1 = sql.firstRow("select * from MSF010 where TABLE_TYPE = '+ISL' and upper(trim(TABLE_CODE)) = upper(trim('"+INV_ST+"')) ");
				log.info ("FIND EMPLOYEE  : " + QRY1);
				if(QRY1.equals(null)) {
					GenericScriptResult result = new GenericScriptResult()
					StrErr = "INVALID INVOICE STATUS"
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					err.setFieldId("parInvStat")
					result.addError(err)
					results.add(result)
					RollErrMes();
					return results
				}
				INV_ST = " and upper(trim(CIC_INV_ST)) = upper(trim('" + INV_ST + "')) ";
			}
			
			String StrSQL = "";
			log.info("maxNumberOfObjects : " + maxNumberOfObjects );
			DecimalFormat df = new DecimalFormat("#,##0.00;-#,##0.00");
			if (restartAttributes.equals(null)){
				   StrSQL = "select * from ACA.KPF38I " +
							   "where DSTRCT_CODE = '"+securityToken.getDistrict()+"'" + CNT + INV + INV_BY + INV_ST +
							   "ORDER BY trim(ACCEPT_DATE) DESC,DSTRCT_CODE,CONTRACT_NO,CIC_INVOICE OFFSET 0 ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY" ;
				   log.info ("StrSQL : " + StrSQL);
				   sql.eachRow(StrSQL, {
					   GenericScriptResult result = new GenericScriptResult()
					   result.addAttribute("grdDst", it.DSTRCT_CODE);
					   result.addAttribute("grdInvNo", it.CIC_INVOICE);
					   result.addAttribute("grdInvSt", it.CIC_INV_ST);
					   result.addAttribute("grdCntNo", it.CONTRACT_NO);
					   result.addAttribute("grdInvVal", df.format(it.INVOICE_VAL));
					   result.addAttribute("grdAccDt", it.ACCEPT_DATE);
					   result.addAttribute("grdCntRem", df.format(it.CNTRCT_REM));
					   result.addAttribute("grdCntRemVal", df.format(it.CNTRCT_REM_VAL));
					   result.addAttribute("grdTotCic", df.format(it.TOTAL_CIC_VAL));
					   result.addAttribute("grdInpBy", it.INPUT_BY);
					   result.addAttribute("grdCicBal", df.format(it.CIC_BALANCE));
					   result.addAttribute("grdTotAct", df.format(it.TOTAL_ACT));
					   
					   result.addAttribute("lastRow", maxNumberOfObjects.toString());
					   results.add(result);
				   })
			}else {
				   log.info("restartAttributes : " + restartAttributes.getAttributeStringValue("lastRow") );
				   Integer MaxInst = Integer.parseInt(restartAttributes.getAttributeStringValue("lastRow"));
				   
				   StrSQL = "select * from ACA.KPF38I " +
							   "where DSTRCT_CODE = '"+securityToken.getDistrict()+"'" + CNT + INV + INV_BY + INV_ST +
							   "ORDER BY trim(ACCEPT_DATE) DESC,DSTRCT_CODE,CONTRACT_NO,CIC_INVOICE OFFSET "+MaxInst.toString()+" ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY" ;
				   log.info ("StrSQL : " + StrSQL);
				   MaxInst = MaxInst + maxNumberOfObjects
				   sql.eachRow(StrSQL, {
					   GenericScriptResult result = new GenericScriptResult()
					   result.addAttribute("grdDst", it.DSTRCT_CODE);
					   result.addAttribute("grdInvNo", it.CIC_INVOICE);
					   result.addAttribute("grdInvSt", it.CIC_INV_ST);
					   result.addAttribute("grdCntNo", it.CONTRACT_NO);
					   result.addAttribute("grdInvVal", df.format(it.INVOICE_VAL));
					   result.addAttribute("grdAccDt", it.ACCEPT_DATE);
					   result.addAttribute("grdCntRem", df.format(it.CNTRCT_REM));
					   result.addAttribute("grdCntRemVal", df.format(it.CNTRCT_REM_VAL));
					   result.addAttribute("grdTotCic", df.format(it.TOTAL_CIC_VAL));
					   result.addAttribute("grdInpBy", it.INPUT_BY);
					   result.addAttribute("grdCicBal", df.format(it.CIC_BALANCE));
					   result.addAttribute("grdTotAct", df.format(it.TOTAL_ACT));
					   result.addAttribute("lastRow", MaxInst.toString());
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