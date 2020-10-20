/**
 * @EMS Mar 2019
 *
 * 20190911 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Initial Coding - Customisastion to Prevent Create CIC when Contract status Hold 
 **/

import javax.naming.InitialContext

import com.mincom.enterpriseservice.ellipse.*
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsServiceCreateRequestDTO
import com.mincom.enterpriseservice.exception.*
import groovy.sql.Sql
import com.mincom.ellipse.client.connection.*
import com.mincom.ellipse.ejra.mso.*;

class ValuationsService_create extends ServiceHook {
	String hookVersion = "1"
	String strCrDT = "";
	String strCrTM = "";
	String StrDT = "";
	String StrMT = "";
	String StrYR = "";
	String StrHH = "";
	String StrMM = "";
	String StrSS = "";
	String StrCURR = "";
	String StrOldBYRT = "";
	String StrOldSLRT = "";
	String NewRate = "";
	String NowDate = "";
	String StrTGLNowKurs = "";
	String StrNowBYRT = "";
	String StrNowSLRT = "";
	String MODE_QUERY = "";
	String RevsDT = "";
	
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	
	EllipseScreenService screenService = EllipseScreenServiceLocator.ellipseScreenService;
	ConnectionId msoCon = ConnectionHolder.connectionId;
	GenericMsoRecord screen = new GenericMsoRecord();
	Boolean LOOPFLAG = false;
	String ErrorMessage = "";
	String CALC_CIC_NO = "";
	String EQP_NO = "";
	
	@Override
	Object onPreExecute(Object input) {
		log.info("Hooks ValuationsService_create onPreExecute logging.version: ${hookVersion}")
		ValuationsServiceCreateRequestDTO c = (ValuationsServiceCreateRequestDTO) input;
		def QueryRes1 = sql.firstRow("select * from msf384 where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and CONTRACT_NO = '"+c.getContractNo()+"'");
		log.info ("QueryRes1 : " + QueryRes1);
		if (QueryRes1){
			if (QueryRes1.STATUS_384.trim().equals("HL")) {
				throw new EnterpriseServiceOperationException(
					new ErrorMessageDTO(
					"9999", "CONTARCT STATUS ON HOLD", "", 0, 0))
					return input
			}
		}else {
			throw new EnterpriseServiceOperationException(
			new ErrorMessageDTO(
			"9999", "INVALID CONTRACT NUMBER / DOESN'T EXIST!", "", 0, 0))
			return input
		}
		return null
	}
	
	@Override
	Object onPostExecute(Object input, Object result) {
		log.info("Hooks ValuationsService_create onPostExecute logging.version: ${hookVersion}")
				
		return result
	}

	String GetUserEmpID(String user, String district) {
		String EMP_ID = "";
		def QRY1;
		QRY1 = sql.firstRow("select * From msf020 where ENTRY_TYPE = 'S' and trim(ENTITY) = trim('"+user+"') and DSTRCT_CODE = '"+district+"'");
		if(!QRY1.equals(null)) {
			EMP_ID = QRY1.EMPLOYEE_ID;
		}
		return EMP_ID;
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
