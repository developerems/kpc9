/**
* @EMS Mar 2019
*
* 20190306 - a9ra5213 - Ricky Afriano - KPC UPGRADE
*            Initial Coding - Validate and Save TOW data into MSF071 - TOW
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

public class TOW_SERV extends GenericScriptPlugin implements GenericScriptExecuteForCollection,GenericScriptExecute{
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
		log.info("Execute Colection TOW_SERV : " + version )
		GenericScriptResults results = new GenericScriptResults()
		RequestAttributes reqAtt = requestAttributes
		
		String PR_NO = "";
		PR_NO = reqAtt.getAttributeStringValue("PR_NO");
		log.info ("PR_NO  : " + PR_NO);
		
		String StrSQL = ""
		log.info("maxNumberOfObjects : " + maxNumberOfObjects );
		if (restartAttributes.equals(null)){
			
			StrSQL = "select row_number () over(order by a.ENTITY_VALUE) NO,a.* from MSF071 a " +
				"where ENTITY_TYPE = 'TOW' and trim(ENTITY_VALUE) like trim('"+securityToken.getDistrict().trim() + PR_NO.trim() +"%') "
				"Order by a.ENTITY_VALUE OFFSET 0 ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY";
			log.info ("StrSQL : " + StrSQL);
			
			sql.eachRow(StrSQL, {
				GenericScriptResult result = new GenericScriptResult();
				if(it.STD_TXT_KEY.trim().equals("OKA")) {
					result.addAttribute("PR_ITM_NO", right(it.ENTITY_VALUE.trim(), 3));
					result.addAttribute("TOW_TY", "O");
					result.addAttribute("OKA", it.REF_CODE.trim());
				}else {
					result.addAttribute("PR_ITM_NO", right(it.ENTITY_VALUE.trim(), 3));
					result.addAttribute("TOW_TY", "T");
					result.addAttribute("T_CODE", it.STD_TXT_KEY.trim());
					result.addAttribute("SUPP1", it.REF_CODE.substring(0,6).trim());
					result.addAttribute("SUPP2", it.REF_CODE.substring(6,12).trim());
					result.addAttribute("SUPP3", it.REF_CODE.substring(12,18).trim());
				}
				result.addAttribute("LAST_ROW", maxNumberOfObjects.toString());
				results.add(result);
			})
		}else {
			log.info("restartAttributes : " + restartAttributes.getAttributeStringValue("LAST_ROW") );
			Integer MaxInst = Integer.parseInt(restartAttributes.getAttributeStringValue("LAST_ROW"));
			//MaxInst = MaxInst + maxNumberOfObjects
			StrSQL = "select row_number () over(order by a.ENTITY_VALUE) NO,a.* from MSF071 a " +
				"where ENTITY_TYPE = 'TOW' and trim(ENTITY_VALUE) like trim('"+securityToken.getDistrict().trim() + PR_NO.trim() +"%') "
				"Order by a.ENTITY_VALUE OFFSET "+MaxInst.toString()+" ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY";
			log.info ("StrSQL : " + StrSQL);
			sql.eachRow(StrSQL, {
				MaxInst = it.NO
				GenericScriptResult result = new GenericScriptResult();
				if(it.STD_TXT_KEY.trim().equals("OKA")) {
					result.addAttribute("PR_ITM_NO", right(it.ENTITY_VALUE.trim(), 3));
					result.addAttribute("TOW_TY", "O");
					result.addAttribute("OKA", it.REF_CODE.trim());
				}else {
					result.addAttribute("PR_ITM_NO", right(it.ENTITY_VALUE.trim(), 3));
					result.addAttribute("TOW_TY", "T");
					result.addAttribute("T_CODE", it.STD_TXT_KEY.trim());
					result.addAttribute("SUPP1", it.REF_CODE.substring(0,6).trim());
					result.addAttribute("SUPP2", it.REF_CODE.substring(6,12).trim());
					result.addAttribute("SUPP3", it.REF_CODE.substring(12,18).trim());
				}
				result.addAttribute("LAST_ROW", MaxInst.toString());
				results.add(result);
			})
		}
		return results
	}
	public GenericScriptResults execute(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings)
	throws FatalException {
		log.info("Execute TOW_SERV : " + version )
		GenericScriptResults results = new GenericScriptResults()
		GenericScriptResult result = new GenericScriptResult()
		RequestAttributes reqAtt = requestAttributes[0]
		String PR_NO = "";
		PR_NO = reqAtt.getAttributeStringValue("PR_NO");
		String PR_ITEM_NO = "";
		PR_ITEM_NO = reqAtt.getAttributeStringValue("PR_ITEM_NO");
		if(!PR_ITEM_NO.equals(null)) {
			PR_ITEM_NO = String.format("%03d",(Integer.parseInt(PR_ITEM_NO)));
		}
		log.info ("PR_NO  : " + PR_NO);
		log.info ("PR_ITEM_NO  : " + PR_ITEM_NO);
		String TOW_TYPE = "";
		String INP_OUT_KPC = "";
		String INP_TOW_CODE = "";
		String INP_TOW_SUPP1 = "";
		String INP_TOW_SUPP2 = "";
		String INP_TOW_SUPP3 = "";
		
		TOW_TYPE = reqAtt.getAttributeStringValue("TOW_TYPE");
		log.info ("TOW_TYPE  : " + TOW_TYPE);
		INP_OUT_KPC = reqAtt.getAttributeStringValue("INP_OUT_KPC");
		log.info ("INP_OUT_KPC  : " + INP_OUT_KPC);
		INP_TOW_CODE = reqAtt.getAttributeStringValue("INP_TOW_CODE");
		log.info ("INP_TOW_CODE  : " + INP_TOW_CODE);
		INP_TOW_SUPP1 = reqAtt.getAttributeStringValue("INP_TOW_SUPP1");
		log.info ("INP_TOW_SUPP1  : " + INP_TOW_SUPP1);
		INP_TOW_SUPP2 = reqAtt.getAttributeStringValue("INP_TOW_SUPP2");
		log.info ("INP_TOW_SUPP2  : " + INP_TOW_SUPP2);
		INP_TOW_SUPP3 = reqAtt.getAttributeStringValue("INP_TOW_SUPP3");
		log.info ("INP_TOW_SUPP3  : " + INP_TOW_SUPP3);
		
		if(TOW_TYPE.equals(null)) {
			TOW_TYPE = "";
		}
		if(INP_OUT_KPC.equals(null)) {
			INP_OUT_KPC = "";
		}
		if(INP_TOW_CODE.equals(null)) {
			INP_TOW_CODE = "";
		}
		if(INP_TOW_SUPP1.equals(null)) {
			INP_TOW_SUPP1 = "";
		}
		if(INP_TOW_SUPP2.equals(null)) {
			INP_TOW_SUPP2 = "";
		}
		if(INP_TOW_SUPP3.equals(null)) {
			INP_TOW_SUPP3 = "";
		}
		String EMP_ID = "";
		String TYPE = "";
		def QRY1;
		QRY1 = sql.firstRow("select * from msf231 where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(PREQ_NO)) = upper(trim('"+PR_NO+"')) and upper(trim(PREQ_ITEM_NO)) = upper(trim('"+PR_ITEM_NO+"')) ");
		//log.info ("FIND PR  : " + QRY1);
		if(QRY1.equals(null)) {
			StrErr = "PREQ NUMBER / ITEM NUMBER DOESN'T EXIST"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("preqNo")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}else {
			if(!QRY1.REQ_TYPE.trim().equals("S")) {
				StrErr = "ONLY SERVICE ITEM CAN BE SET TOW"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				//err.setFieldId("preqNo")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
		}
		
		if (TOW_TYPE.trim().equals("O")) {
			if(INP_OUT_KPC.trim().equals("")) {
				StrErr = "INPUT REQUIRED!"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("INP_OUT_KPC")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
			if(INP_OUT_KPC.trim().length() > 40) {
				StrErr = "INVALID LENGTH, MAX 40!"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("INP_OUT_KPC")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
		}else if (TOW_TYPE.trim().equals("T")) {
			if(INP_TOW_CODE.trim().equals("")) {
				StrErr = "INPUT REQUIRED!"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("INP_TOW_CODE")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}else {
				def QRY2;
				QRY2 = sql.firstRow("select * from msf010 where TABLE_TYPE = 'TOW' and trim(upper(TABLE_CODE)) = trim(upper('"+INP_TOW_CODE.trim()+"'))");
				//log.info ("FIND PR  : " + QRY1);
				if(QRY2.equals(null)) {
					StrErr = "TOW CODE DOESN'T EXIST"
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					err.setFieldId("INP_TOW_CODE")
					result.addError(err)
					results.add(result)
					RollErrMes();
					return results
				}
			}
			if(INP_TOW_SUPP1.trim().equals("")) {
				StrErr = "INPUT REQUIRED!"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("INP_TOW_SUPP1")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}else {
				def QRY2;
				QRY2 = sql.firstRow("select * from msf010 where TABLE_TYPE = 'TOS' and trim(upper(TABLE_CODE)) = trim(upper('"+INP_TOW_CODE.trim()+INP_TOW_SUPP1.trim()+"'))");
				//log.info ("FIND PR  : " + QRY1);
				if(QRY2.equals(null)) {
					StrErr = "SUPPLIER DOESN'T EXIST IN TOW"
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					err.setFieldId("INP_TOW_SUPP1")
					result.addError(err)
					results.add(result)
					RollErrMes();
					return results
				}
			}
			if(INP_TOW_SUPP2.trim().equals("") && !INP_TOW_SUPP3.trim().equals("")) {
				StrErr = "INPUT REQUIRED!"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("INP_TOW_SUPP2")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
			if(!INP_TOW_SUPP2.trim().equals("")) {
				def QRY2;
				QRY2 = sql.firstRow("select * from msf010 where TABLE_TYPE = 'TOS' and trim(upper(TABLE_CODE)) = trim(upper('"+INP_TOW_CODE.trim()+INP_TOW_SUPP2.trim()+"'))");
				//log.info ("FIND PR  : " + QRY1);
				if(QRY2.equals(null)) {
					StrErr = "SUPPLIER DOESN'T EXIST IN TOW"
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					err.setFieldId("INP_TOW_SUPP2")
					result.addError(err)
					results.add(result)
					RollErrMes();
					return results
				}
			}
			if(!INP_TOW_SUPP3.trim().equals("")) {
				def QRY2;
				QRY2 = sql.firstRow("select * from msf010 where TABLE_TYPE = 'TOS' and trim(upper(TABLE_CODE)) = trim(upper('"+INP_TOW_CODE.trim()+INP_TOW_SUPP3.trim()+"'))");
				//log.info ("FIND PR  : " + QRY1);
				if(QRY2.equals(null)) {
					StrErr = "SUPPLIER DOESN'T EXIST IN TOW"
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					err.setFieldId("INP_TOW_SUPP3")
					result.addError(err)
					results.add(result)
					RollErrMes();
					return results
				}
			}
			if((INP_TOW_SUPP1.trim().equals(INP_TOW_SUPP2.trim()) && !INP_TOW_SUPP1.trim().equals("") && !INP_TOW_SUPP2.trim().equals("")) || 
				(INP_TOW_SUPP1.trim().equals(INP_TOW_SUPP3.trim()) && !INP_TOW_SUPP1.trim().equals("") && !INP_TOW_SUPP3.trim().equals("")) || 
				(INP_TOW_SUPP2.trim().equals(INP_TOW_SUPP3.trim()) && !INP_TOW_SUPP2.trim().equals("") && !INP_TOW_SUPP3.trim().equals(""))) {
				StrErr = "SUPPLIER ALREADY EXIST"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				//err.setFieldId("INP_TOW_SUPP3")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
		}
		
		if (TOW_TYPE.trim().equals("O")) {
			TYPE = "OKA";
		}else if (TOW_TYPE.trim().equals("T")) {
			TYPE = INP_TOW_CODE.trim();
		}else {
			StrErr = "INVALID TOW TYPE"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("TOW_TYPE")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		
		QRY1 = sql.firstRow("select * from msf071 where ENTITY_TYPE = 'TOW' and trim(ENTITY_VALUE) = trim('"+securityToken.getDistrict() + PR_NO.trim() + PR_ITEM_NO.trim() + "') ");
		if(QRY1.equals(null)) {
			try
			{
				GetNowDateTime();
				EMP_ID = GetUserEmpID(securityToken.getUserId(),securityToken.getDistrict());
				String QueryInsert = ("Insert into MSF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY,LAST_MOD_EMP) values ('TOW',?,'000','000',?,?,?,?,?,?)");
				if (TOW_TYPE.trim().equals("O")) {
					sql.execute(QueryInsert,[securityToken.getDistrict() + PR_NO.trim() + PR_ITEM_NO.trim(),strCrDT,strCrTM,securityToken.getUserId(),INP_OUT_KPC,TYPE,EMP_ID]);
				}else if (TOW_TYPE.trim().equals("T")) {
					sql.execute(QueryInsert,[securityToken.getDistrict() + PR_NO.trim() + PR_ITEM_NO.trim(),strCrDT,strCrTM,securityToken.getUserId(),INP_TOW_SUPP1.trim()+INP_TOW_SUPP2.trim()+INP_TOW_SUPP3.trim(),TYPE,EMP_ID]);
				}
				
			} catch (Exception  e) {
				log.info ("Exception is : " + e);
				StrErr = "EXCEPTION : ERROR WHEN INSERT MSF071";
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541);
				result.addError(err);
				results.add(result);
				RollErrMes();
				return results
			}
		}else {
			try
			{
				String Delete = ("delete msf071 where ENTITY_TYPE = 'TOW' and trim(ENTITY_VALUE) = trim('"+securityToken.getDistrict() + PR_NO.trim() + PR_ITEM_NO.trim() + "') ");
				sql.execute(Delete);
				GetNowDateTime();
				EMP_ID = GetUserEmpID(securityToken.getUserId(),securityToken.getDistrict());
				String QueryInsert = ("Insert into MSF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY,LAST_MOD_EMP) values ('TOW',?,'000','000',?,?,?,?,?,?)");
				if (TOW_TYPE.trim().equals("O")) {
					sql.execute(QueryInsert,[securityToken.getDistrict() + PR_NO.trim() + PR_ITEM_NO.trim(),strCrDT,strCrTM,securityToken.getUserId(),INP_OUT_KPC,TYPE,EMP_ID]);
				}else if (TOW_TYPE.trim().equals("T")) {
					sql.execute(QueryInsert,[securityToken.getDistrict() + PR_NO.trim() + PR_ITEM_NO.trim(),strCrDT,strCrTM,securityToken.getUserId(),INP_TOW_SUPP1.trim()+INP_TOW_SUPP2.trim()+INP_TOW_SUPP3.trim(),TYPE,EMP_ID]);
				}
			} catch (Exception  e) {
				log.info ("Exception is : " + e);
				StrErr = "EXCEPTION : ERROR WHEN INSERT MSF071";
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541);
				result.addError(err);
				results.add(result);
				RollErrMes();
				return results
			}
		}
		log.info ("TOW END : ");
		results.add(result)
		return results
	}
	public static String right(String value, int length) {
		// To get right characters from a string, change the begin index.
		return value.substring(value.length() - length);
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