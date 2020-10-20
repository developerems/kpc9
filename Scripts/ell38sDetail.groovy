/**
 * @EMS Nov 201905
 *
 * a9ra5213 - Ricky Afriano - Initial Code - KPC
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
import com.mincom.ellipse.errors.Error;
import com.mincom.ellipse.errors.CobolMessages;


public class ell38sDetail extends GenericScriptPlugin implements GenericScriptExecute, GenericScriptUpdate, GenericScriptCreate, GenericScriptDelete{
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
		log.info("Execute ELL38S_DETAIL_EXECUTE : " + version );
		GenericScriptResults results = new GenericScriptResults();
		GenericScriptResult result = new GenericScriptResult();
		RequestAttributes reqAtt = requestAttributes[0];
		
		String GP = "";
		log.info("GP1 : " + reqAtt.getAttributeStringValue("sGp") );
		log.info("GP2 : " + reqAtt.getAttributeStringValue("gp") );
		
		if (reqAtt.getAttributeStringValue("sGp").equals(null)) {
			GP = reqAtt.getAttributeStringValue("gp");
		}else {
			GP = reqAtt.getAttributeStringValue("sGp");
		}
		log.info("GP : " + GP );
		String app = "";
		if (reqAtt.getAttributeStringValue("appDetail").equals(null)) {
			app = reqAtt.getAttributeStringValue("app");
		}else {
			app = reqAtt.getAttributeStringValue("appDetail");
		}
		//String app = reqAtt.getAttributeStringValue("appDetail")
		log.info("app : " + app );
		def QRY1;
		QRY1 = sql.firstRow("select * from msf02a where APPLICATION_NAME = '"+app+"' and ENTRY_TYPE = 'G' and trim(ENTITY) = trim('" + GP + "')");
		if(!QRY1.equals(null)) {
			result.addAttribute("gp", GP.trim());
		}
		result.addAttribute("app", app);
		results.add(result);
		return results;
	}
	public GenericScriptResults create(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings)
	throws FatalException {
		log.info("Create ELL38S_DETAIL_CREATE : " + version )
		GenericScriptResults results = new GenericScriptResults();
		GenericScriptResult result = new GenericScriptResult();
		RequestAttributes reqAtt = requestAttributes[0];
		String app = reqAtt.getAttributeStringValue("app");
		if (app.equals(null)) {
			StrErr = "APPLICATION REQUIRED!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("app")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}else {
			if (app.trim().equals("")) {
				StrErr = "APPLICATION REQUIRED!"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("app")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}else {
				if (!app.trim().equals("ELL38C") && !app.trim().equals("ELL38I")) {
					StrErr = "INVALID APPLICATION!"
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					err.setFieldId("app")
					result.addError(err)
					results.add(result)
					RollErrMes();
					return results
				}
			}
		}
		
		String GP = reqAtt.getAttributeStringValue("gp").toUpperCase();
				
		def QRY1;
		QRY1 = sql.firstRow("select * from msf02a where APPLICATION_NAME = '"+app+"' and ENTRY_TYPE = 'G' and trim(ENTITY) = trim('" + GP + "')");
		log.info ("FIND GP  : " + QRY1);
		
		try
			{
				String EMP_ID = GetUserEmpID(securityToken.getUserId(),securityToken.getDistrict());
				GetNowDateTime();
				if (!QRY1.equals(null)){
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_0019)
					err.setFieldId("gp")
					result.addError(err)
					results.add(result)
					return results
				}else{
					String QueryInsert = ("Insert into MSF02A (APPLICATION_NAME,APPLICATION_TYPE,DSTRCT_CODE,ENTITY,ENTRY_TYPE,ACCESS_LEVEL,LAST_MOD_DATE,LAST_MOD_EMP,LAST_MOD_TIME,LAST_MOD_USER) values ('"+app+"','C','    ','"+GP+"','G','2','"+strCrDT+"','" + EMP_ID + "','" + strCrTM + "','" + securityToken.getUserId() + "')");
					sql.execute(QueryInsert);
				}
			} catch (Exception  e) {
				log.info ("Exception is : " + e);
				StrErr = "INSERT EXCEPTION ON QUERY INSERT"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
		
		result.addAttribute("gp", GP.trim());
		result.addAttribute("app", app);
		results.add(result);
		return results;
	}
	public GenericScriptResults update(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings)
	throws FatalException {
		log.info("Update ELL38S_DETAIL_UPDATE : " + version );
		GenericScriptResults results = new GenericScriptResults();
		GenericScriptResult result = new GenericScriptResult();
		RequestAttributes reqAtt = requestAttributes[0];
		
		results.add(result);
		return results;
	}
	public GenericScriptResults delete(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings)
	throws FatalException {
		log.info("Delete ELL38S_DETAIL_DELETE : " + version )
		GenericScriptResults results = new GenericScriptResults();
		GenericScriptResult result = new GenericScriptResult();
		RequestAttributes reqAtt = requestAttributes[0];
		String app = reqAtt.getAttributeStringValue("app");
		if (app.equals(null)) {
			StrErr = "APPLICATION REQUIRED!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("app")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}else {
			if (app.trim().equals("")) {
				StrErr = "APPLICATION REQUIRED!"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("app")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}else {
				if (!app.trim().equals("ELL38C") && !app.trim().equals("ELL38I")) {
					StrErr = "INVALID APPLICATION!"
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					err.setFieldId("app")
					result.addError(err)
					results.add(result)
					RollErrMes();
					return results
				}
			}
		}
		
		String GP = reqAtt.getAttributeStringValue("gp");
		def QRY1;
		QRY1 = sql.firstRow("select * from msf02a where APPLICATION_NAME = '"+app+"' and ENTRY_TYPE = 'G' and trim(ENTITY) = trim('" + GP + "')");
		log.info ("FIND GP  : " + QRY1);
		try
		{
			GetNowDateTime();
			if (!QRY1.equals(null)){
				String QueryDelete = ("delete msf02a where APPLICATION_NAME = '"+app+"' and ENTRY_TYPE = 'G' and trim(ENTITY) = trim('" + GP + "')");
				sql.execute(QueryDelete);
			}
		} catch (Exception  e) {
			log.info ("Exception is : " + e);
			StrErr = "INSERT EXCEPTION ON QUERY DELETE"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		result.addAttribute("app", app);
		result.addAttribute("gp", GP.trim());
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
	public String GetUserEmpID(String user, String district) {
		String EMP_ID = "";
		def QRY1;
		QRY1 = sql.firstRow("select * From msf020 where ENTRY_TYPE = 'S' and trim(ENTITY) = trim('"+user+"') and DSTRCT_CODE = '"+district+"'");
		if(!QRY1.equals(null)) {
			EMP_ID = QRY1.EMPLOYEE_ID;
		}
		return EMP_ID;
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
