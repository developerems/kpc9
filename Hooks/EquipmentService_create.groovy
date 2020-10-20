package KPC.HOOKS

/**
 * @EMS Mar 2019
 *
 * 20191023 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Hooks to validate custodian id as cost center in MSF920 and MSF810
 * 20190613 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Add fungction to baypass Subledger mandatory for Account Code
 * 20180319 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Initial Coding - Forward Fit to default Login Employee ID for Input By when Create Equipment  
 **/
import javax.naming.InitialContext

import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.ellipse.service.m3875.approvalsmanager.ApprovalsManagerService
import com.mincom.ellipse.types.m3875.instances.TransactionRetrievalCriteriaSearchParam;
import com.mincom.ellipse.types.m3875.instances.TransactionDTO
import com.mincom.ellipse.types.m3875.instances.TransactionServiceResult;
import com.mincom.enterpriseservice.ellipse.equipment.EquipmentServiceReadReplyDTO
import com.mincom.enterpriseservice.ellipse.equipment.EquipmentServiceModifyRequestDTO
import com.mincom.enterpriseservice.ellipse.equipment.EquipmentServiceCreateRequestDTO
import com.mincom.ellipse.service.ServiceDTO;
import com.mincom.enterpriseservice.exception.*
import groovy.sql.Sql
import com.mincom.enterpriseservice.ellipse.*

class EquipmentService_create extends ServiceHook{
	String hookVersion = "1"
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	
	String strCrDT = "";
	String strCrTM = "";
	String StrDT = "";
	String StrMT = "";
	String StrYR = "";
	String StrHH = "";
	String StrMM = "";
	String StrSS = "";
	String StrErr = "";
	String ACCT_CODE = "";
	
	@Override
	public Object onPreExecute(Object input) {
		log.info("Hooks EquipmentService_create onPreExecute logging.version: ${hookVersion}")
		EquipmentServiceCreateRequestDTO c = (EquipmentServiceCreateRequestDTO) input;
		
		String custId = c.getCustodian();
		log.info("custId:" + custId);
		if (!custId.equals(null)){
			if (!custId.trim().equals("")){
				log.info ("isNumeric : " + isNumeric(custId));
				if (isNumeric(custId.trim()).equals(true)) {
					custId = String.format("%010d", Integer.parseInt(custId));
					log.info("custId:" + custId);
				}
				def QRY1 = sql.firstRow("select a.*,b.surname from ( " +
						"select case when LENGTH(TRIM(TRANSLATE(a.COST_CTRE_SEG, ' +-.0123456789', ' '))) is null then LPAD(trim(a.COST_CTRE_SEG),10,'0') " +
						"else trim(a.COST_CTRE_SEG) end NEW_COST_CTRE " +
						"from msf920 a  " +
						"where a.DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and a.ACTIVE_STATUS <> 'I') a " +
						"join msf810 b on (trim(a.NEW_COST_CTRE) = trim(b.EMPLOYEE_ID)) " +
						"where trim(NEW_COST_CTRE) = trim('"+custId+"')");
				if(QRY1.equals(null)) {
					throw new EnterpriseServiceOperationException(
					new ErrorMessageDTO(
					"9999", "INVALID COST CENTER (MSE920/MSE81S) OR INACTIVE !", "custodian", 0, 0))
					return input
				}
			}
		}
		
		String EMP_ID = GetUserEmpID(tools.commarea.UserId, tools.commarea.District.trim())
		c.setInputBy(EMP_ID)
		ACCT_CODE = "";
		def QRY1 = sql.firstRow("select * from msf966 " +
			"where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and trim(ACCOUNT_CODE) = trim('"+c.accountCode+"')");
			log.info ("FIND ACCOUNT  : " + QRY1);
			if(!QRY1.equals(null)) {
				if(QRY1.SUBLEDGER_IND.equals("M")) {
					ACCT_CODE = c.accountCode;
					String QueryUpdate = ("update msf966 " +
											"set SUBLEDGER_IND = 'O'" +
											"where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and trim(ACCOUNT_CODE) = trim('"+c.accountCode+"')");
					sql.execute(QueryUpdate);
				}
			}
		return null;
	}
	@Override
	public Object onPostExecute(Object input, Object result) {
		log.info("Hooks EquipmentService_create onPostExecute logging.version: ${hookVersion}")
		if (!ACCT_CODE.equals("")) {
			String QueryUpdate = ("update msf966 " +
							"set SUBLEDGER_IND = 'M'" +
							"where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and trim(ACCOUNT_CODE) = trim('"+ACCT_CODE+"')");
			sql.execute(QueryUpdate);
		}
		ACCT_CODE = "";
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
	public static boolean isNumeric(String s) {
		if (s == null || s.equals("")) {
			return false;
		}

		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c < '0' || c > '9') {
				return false;
			}
		}
		return true;
	}
}