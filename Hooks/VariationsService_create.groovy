package KPC.HOOKS
/**
 * @EMS Mar 2019
 *
 * 20190317 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Initial Coding - Forward Fit from Ellipse 5 to set District based on login district
 **/

import javax.naming.InitialContext

import com.mincom.ellipse.attribute.Attribute
import com.mincom.enterpriseservice.ellipse.*
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.enterpriseservice.ellipse.contractitem.ContractItemService
import com.mincom.enterpriseservice.ellipse.contractitem.ContractItemServiceModifyPortMileRequestDTO
import com.mincom.enterpriseservice.ellipse.contract.ContractServiceModifyRequestDTO
import com.mincom.enterpriseservice.ellipse.contract.ContractServiceCreateRequestDTO
import com.mincom.enterpriseservice.ellipse.contract.ContractServiceRetrieveRequestDTO
import com.mincom.enterpriseservice.ellipse.variations.VariationsServiceRetrieveRequestDTO
import com.mincom.enterpriseservice.ellipse.variations.VariationsServiceCreateRequestDTO
import com.mincom.enterpriseservice.ellipse.variations.VariationsServiceCreateReplyDTO
import com.mincom.enterpriseservice.ellipse.contract.ContractServiceCreateReplyDTO
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextService
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextServiceCreateReplyDTO
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextServiceSetTextReplyDTO
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextServiceDeleteReplyDTO
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextServiceAppendReplyDTO
import com.mincom.ellipse.service.ServiceDTO;
import com.mincom.enterpriseservice.exception.*
import groovy.sql.Sql

class VariationsService_create extends ServiceHook {
	String hookVersion = "1"
	String strCrDT = "";
	String strCrTM = "";
	String StrDT = "";
	String StrMT = "";
	String StrYR = "";
	String StrHH = "";
	String StrMM = "";
	String StrSS = "";
	String VarPrefix = "";

	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	@Override
	public Object onPreExecute(Object input) {
		log.info("Hooks VariationsService_create onPreExecute logging.version: ${hookVersion}")

		VariationsServiceCreateRequestDTO c = (VariationsServiceCreateRequestDTO) input
		log.info("VariationsServiceCreateRequestDTO: " + c)
		if (!c.getContractNumber().equals(null)) {
			VarPrefix = "";
			def QRY1;
			QRY1 = sql.firstRow("select * from msf071 where ENTITY_TYPE = 'CTR' and REF_NO = '001' and trim(ENTITY_VALUE) = trim('" +c.getContractNumber() + "')");
			log.info ("FIND CTR  : " + QRY1);
			if (!QRY1.equals(null)){
				VarPrefix = QRY1.REF_CODE.trim()
				if (VarPrefix.equals("Z")){
					throw new EnterpriseServiceOperationException(
					new ErrorMessageDTO(
					"9999", "COULD NOT USE PREFIX \"Z\", RESERVED BY SYSTEM", "variationNumber", 0, 0))
					return input;
				}
			}else {
				throw new EnterpriseServiceOperationException(
				new ErrorMessageDTO(
				"9999", "THIS CONTRACT DOESN'T HAVE PREFIX FOR VARIATION", "variationNumber", 0, 0))
				return input;
			}
			def QRY1A;
			QRY1A = sql.firstRow("select MAX(VARIATION_NO) LAST_VAR_NO FROM MSF388 where trim(CONTRACT_NO) = trim('"+c.getContractNumber()+"') and VARIATION_NO like '"+VarPrefix+"%'");
			if (!QRY1A.equals(null)){
				if (QRY1A.LAST_VAR_NO.equals(VarPrefix + "999")){
					throw new EnterpriseServiceOperationException(
					new ErrorMessageDTO(
					"9999", "MAXIMUM VARIATION NUMBER REACH, CHANGE PREFIX", "variationNumber", 0, 0))
					return input;
				}
			}
			def QRY2 = sql.firstRow("select * from msf384 where trim(CONTRACT_NO) = trim('"+c.getContractNumber()+"')");
			if (!QRY2.equals(null)){
				if (!QRY2.LAST_VAR_NO_EO.equals(QRY2.LAST_VAR_NO_VA)){
					if (QRY2.LAST_VAR_NO_EO.equals("999") || QRY2.LAST_VAR_NO_VA.equals("999")) {
						log.info (" MAX 999 ");
						String QueryUdate = ""
						QueryUdate = ("update msf384 set LAST_VAR_NO_VA = '000',LAST_VAR_NO_EO = '000' where trim(CONTRACT_NO) = trim('"+c.getContractNumber()+"')");
						sql.execute(QueryUdate);
					}else {
						if (QRY2.LAST_VAR_NO_EO > QRY2.LAST_VAR_NO_VA){
							log.info (" EO > VA ");
							String QueryUdate = ""
							QueryUdate = ("update msf384 set LAST_VAR_NO_VA = '"+QRY2.LAST_VAR_NO_EO+"' where trim(CONTRACT_NO) = trim('"+c.getContractNumber()+"')");
							sql.execute(QueryUdate);
							log.info ("update sequence variation msf384  : ");
						}else {
							log.info (" EO < VA ");
							String QueryUdate = ""
							QueryUdate = ("update msf384 set LAST_VAR_NO_EO = '"+QRY2.LAST_VAR_NO_VA+"' where trim(CONTRACT_NO) = trim('"+c.getContractNumber()+"')");
							sql.execute(QueryUdate);
							log.info ("update sequence variation msf384  : ");
						}
					}
				}
			}
			GetNowDateTime();
			String QueryUdate = ""
			QueryUdate = ("update msfx31 set VARIATION_NO = 'Z'||substr(VARIATION_NO,2,3),LAST_MOD_DATE = '" + strCrDT + "',LAST_MOD_TIME = '" + strCrTM + "',LAST_MOD_USER = '" + tools.commarea.UserId + "' where trim(CONTRACT_NO) = trim('"+c.getContractNumber()+"') and substr(VARIATION_NO,1,1) = 'V'");
			//log.info ("QueryUdate : " + QueryUdate);
			sql.execute(QueryUdate);
			log.info ("update msfx31  : ");
			QueryUdate = ("update msf388 set PREV_VAR_NO = 'Z'||substr(VARIATION_NO,2,3),LAST_MOD_DATE = '" + strCrDT + "',LAST_MOD_TIME = '" + strCrTM + "',LAST_MOD_USER = '" + tools.commarea.UserId + "' where trim(CONTRACT_NO) = trim('"+c.getContractNumber()+"') and substr(PREV_VAR_NO,1,1) = 'V'");
			sql.execute(QueryUdate);
			log.info ("update msf388_1  : ");
			QueryUdate = ("update msf388 set VARIATION_NO = 'Z'||substr(VARIATION_NO,2,3),LAST_MOD_DATE = '" + strCrDT + "',LAST_MOD_TIME = '" + strCrTM + "',LAST_MOD_USER = '" + tools.commarea.UserId + "' where trim(CONTRACT_NO) = trim('"+c.getContractNumber()+"') and substr(VARIATION_NO,1,1) = 'V'");
			sql.execute(QueryUdate);
			log.info ("update msf388_2  : ");
			String EMP_ID = GetUserEmpID(tools.commarea.UserId, tools.commarea.District.trim());
			QueryUdate = ("update msf061 set DATA_2_061 = substr(DATA_2_061,1,8)||'Z'||substr(DATA_2_061,-3) where PART_1_061 = 'OR' and PART_2_061 = '2R' and DATA_1_061 = '"+EMP_ID+"' and REVSD_XREF_061 = '2ROR' and trim(replace(DATA_2_061,substr(DATA_2_061,-4),'')) = trim('"+c.getContractNumber()+"') and substr(DATA_2_061,9,1) = 'V'");
			sql.execute(QueryUdate);
			log.info ("update msf061  : ");
		}
		return null
	}

	@Override
	public Object onPostExecute(Object input, Object result) {
		log.info("Hooks VariationsService_create onPostExecute logging.version: ${hookVersion}")
		VariationsServiceCreateReplyDTO d = (VariationsServiceCreateReplyDTO) result
		GetNowDateTime();
		String QueryUdate = ""
		QueryUdate = ("update msf388 set VARIATION_NO = '"+VarPrefix+"'||substr(VARIATION_NO,2,3),LAST_MOD_DATE = '" + strCrDT + "',LAST_MOD_TIME = '" + strCrTM + "',LAST_MOD_USER = '" + tools.commarea.UserId + "' where trim(CONTRACT_NO) = trim('"+d.getContractNumber()+"') and trim(VARIATION_NO) = trim('"+d.getVariationNumber()+"')");
		log.info("UPDATE 1 : " + QueryUdate);
		sql.execute(QueryUdate);
		QueryUdate = ("update msfx31 set VARIATION_NO = '"+VarPrefix+"'||substr(VARIATION_NO,2,3),LAST_MOD_DATE = '" + strCrDT + "',LAST_MOD_TIME = '" + strCrTM + "',LAST_MOD_USER = '" + tools.commarea.UserId + "' where trim(CONTRACT_NO) = trim('"+d.getContractNumber()+"') and trim(VARIATION_NO) = trim('"+d.getVariationNumber()+"')");
		log.info("UPDATE 2 : " + QueryUdate);
		sql.execute(QueryUdate);
		String EMP_ID = GetUserEmpID(tools.commarea.UserId, tools.commarea.District.trim());
		QueryUdate = ("update msf061 set DATA_2_061 = substr(DATA_2_061,1,8)||'"+VarPrefix+"'||substr(DATA_2_061,-3) where PART_1_061 = 'OR' and PART_2_061 = '2R' and DATA_1_061 = '"+EMP_ID+"' and REVSD_XREF_061 = '2ROR' and trim(DATA_2_061) = trim(rpad('"+d.getContractNumber()+"',8)||'"+d.getVariationNumber()+"')");
		log.info("UPDATE 3 : " + QueryUdate);
		//log.info ("update msf061_2  : " + QueryUdate);
		sql.execute(QueryUdate);
		String NewVarNo = d.getVariationNumber().replace(d.getVariationNumber().substring(0,1), VarPrefix);
		String LastVarNo = " ";
		def QRY1;
		QRY1 = sql.firstRow("select MAX(VARIATION_NO) LAST_VAR_NO FROM MSF388 where trim(CONTRACT_NO) = trim('"+d.getContractNumber()+"') and VARIATION_NO like '"+VarPrefix+"%' and VARIATION_NO <> '"+NewVarNo+"'");
		if (!QRY1.equals(null)){
			if (!QRY1.LAST_VAR_NO.equals(null)){
				LastVarNo = QRY1.LAST_VAR_NO.trim();
			}else {
				LastVarNo = " ";
			}
		}else {
			LastVarNo = " ";
		}
		QueryUdate = ("update MSF388 set PREV_VAR_NO = '"+LastVarNo+"' where trim(CONTRACT_NO) = trim('"+d.getContractNumber()+"') and VARIATION_NO = '"+NewVarNo+"'");
		log.info("UPDATE 4 : " + QueryUdate);
		sql.execute(QueryUdate);
		d.setVariationNumber(NewVarNo);
		log.info("New Var No: " + d.getVariationNumber());
		return result
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
