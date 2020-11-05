/**
 * @EMS Mar 2019
 *
 * 20191023 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Hooks to validate custodian id as cost center in MSF920 and MSF810
 * 20190613 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Add fungction to baypass Subledger mandatory for Account Code
 * 20180319 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Initial Coding - This hooks to show last modified by in equipment Data 
 **/

import org.apache.xpath.operations.Bool

import javax.naming.InitialContext

import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.enterpriseservice.ellipse.equipment.EquipmentServiceModifyRequestDTO
import com.mincom.ellipse.service.ServiceDTO
import com.mincom.enterpriseservice.exception.*
import groovy.sql.Sql
import com.mincom.enterpriseservice.ellipse.*

import java.text.SimpleDateFormat

class EquipmentService_modify extends ServiceHook{
	String hookVersion = "1"
	
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)

	String strCrDT = ""
	String strCrTM = ""
	String StrDT = ""
	String StrMT = ""
	String StrYR = ""
	String StrHH = ""
	String StrMM = ""
	String StrSS = ""
	String StrErr = ""
	String ACCT_CODE = ""

	@Override
	Object onPreExecute(Object input) {
		log.info("Hooks EquipmentService_modify onPreExecute logging.version: ${hookVersion}")
		EquipmentServiceModifyRequestDTO c = (EquipmentServiceModifyRequestDTO) input
		String EQP_NO = c.getEquipmentNo()

		String custId = c.getCustodian()
		log.info("custId:" + custId)
		if (custId){
			if (custId.trim() != ""){
				log.info ("isNumeric: ${isNumeric(custId)}")
				if (isNumeric(custId.trim()) == true) {
					custId = String.format("%010d", Integer.parseInt(custId))
					log.info("custId: $custId")
				}
				def QRY1 = sql.firstRow("select a.*,b.surname from ( " +
						"select case when LENGTH(TRIM(TRANSLATE(a.COST_CTRE_SEG, ' +-.0123456789', ' '))) is null then LPAD(trim(a.COST_CTRE_SEG),10,'0') " +
						"else trim(a.COST_CTRE_SEG) end NEW_COST_CTRE " +
						"from msf920 a  " +
						"where a.DSTRCT_CODE = '${tools.commarea.District.trim()}' and a.ACTIVE_STATUS <> 'I') a " +
						"join msf810 b on (trim(a.NEW_COST_CTRE) = trim(b.EMPLOYEE_ID)) " +
						"where trim(NEW_COST_CTRE) = trim('$custId')")
				if(!QRY1) {
					throw new EnterpriseServiceOperationException(
					new ErrorMessageDTO(
					"9999", "INVALID COST CENTER (MSE920/MSE81S) OR INACTIVE !", "custodian", 0, 0))
					return input
				}
			}
		}

		ACCT_CODE = ""
		def QRY1A = sql.firstRow("select * from msf966 " +
				"where DSTRCT_CODE = '${tools.commarea.District.trim()}' and trim(ACCOUNT_CODE) = trim('${c.accountCode}')")
		log.info ("FIND ACCOUNT: $QRY1A")
		if(QRY1A) {
			if(QRY1A.SUBLEDGER_IND == "M") {
				ACCT_CODE = c.accountCode
				String QueryUpdate = ("update msf966 " +
						"set SUBLEDGER_IND = 'O'" +
						"where DSTRCT_CODE = '${tools.commarea.District.trim()}' and trim(ACCOUNT_CODE) = trim('${c.accountCode}')")
				sql.execute(QueryUpdate)
			}
		}

		if (EQP_NO) {
			List<Attribute> custAttribs = c.getCustomAttributes()
			custAttribs.each{Attribute customAttribute ->
				log.info ("Attribute Name = ${customAttribute.getName()}")
				log.info ("Attribute Value = ${customAttribute.getValue()}")
				if (customAttribute.getName() == new String("newTargetLifeUnit")){
					String TAR_LIFE_UNIT = customAttribute.getValue()
					log.info("TAR_LIFE_UNIT: $TAR_LIFE_UNIT")
					def QRY1 = sql.firstRow("select * from msf010 where TABLE_TYPE = 'SS' and trim(TABLE_CODE) = trim('$TAR_LIFE_UNIT') ")
					if(!QRY1) {
						throw new EnterpriseServiceOperationException(
						new ErrorMessageDTO(
						"9999", "INVALID TARGET UNIT LIFE!", "newTargetLifeUnit", 0, 0))
						return input
					}
				}
				if (customAttribute.getName() == new String("newTargetLife")){
					String EQP_TAR_LIFE = customAttribute.getValue()
					log.info("EQP_TAR_LIFE: $EQP_TAR_LIFE")
					if (EQP_TAR_LIFE) {
						if(EQP_TAR_LIFE.length() > 12) {
							throw new EnterpriseServiceOperationException(
							new ErrorMessageDTO(
							"9999", "INVALID LENGTH MAX(12)!", "newTargetLife", 0, 0))
							return input
						}

						try {
							EQP_TAR_LIFE as double
							log.info("Arsiadi EQPTARLIFE IS NUMBER")
						} catch (e) {
							log.info("Arsiadi EQPTARLIFE IS NOT A NUMBER")
							throw new EnterpriseServiceOperationException(
							new ErrorMessageDTO(
							"9999", "INPUT SHOULD BE NUMERIC!", "equipTargetLife", 0, 0))
							return input
						}
					}
				}
			}
		}
		return null
	}
	@Override
	Object onPostExecute(Object input, Object result) {
		log.info("Hooks EquipmentService_modify onPostExecute logging.version: ${hookVersion}")

		EquipmentServiceModifyRequestDTO c = (EquipmentServiceModifyRequestDTO) input
		String EQP_NO = c.getEquipmentNo()
		if (ACCT_CODE != "") {
			String QueryUpdate = ("update msf966 " +
					"set SUBLEDGER_IND = 'M'" +
					"where DSTRCT_CODE = '${tools.commarea.District.trim()}' and trim(ACCOUNT_CODE) = trim('$ACCT_CODE')")
			sql.execute(QueryUpdate)
		}
		ACCT_CODE = ""

		if (EQP_NO) {
			List<Attribute> custAttribs = c.getCustomAttributes()
			custAttribs.each{Attribute customAttribute ->
				log.info("Attribute Name: ${customAttribute.getName()}")
				log.info("Attribute Value: ${customAttribute.getValue()}")
				log.info("Attribute Namespace: ${customAttribute.getNamespace()}")
				if (customAttribute.getName() == new String("newTargetLifeUnit")){
					GetNowDateTime()
					String TAR_LIFE_UNIT = customAttribute.getValue()
					def QRY1 = sql.firstRow("select * from msf010 where TABLE_TYPE = 'SS' and trim(TABLE_CODE) = trim('$TAR_LIFE_UNIT') ")
					if(QRY1) {
						def QRY2 = sql.firstRow("select * from msf071 where ENTITY_TYPE = 'EQP' and REF_NO = '110' and SEQ_NUM = '001' and ENTITY_VALUE = '$EQP_NO'")
						if(QRY2) {
							String QueryUpdate = ("update msf071 set REF_CODE = '$TAR_LIFE_UNIT',LAST_MOD_DATE = '$strCrDT',LAST_MOD_TIME = '$strCrTM',LAST_MOD_USER = '${tools.commarea.UserId}' where ENTITY_TYPE = 'EQP' and REF_NO = '110' and SEQ_NUM = '001' and ENTITY_VALUE = '$EQP_NO'")
							sql.execute(QueryUpdate)
						}else {
							String QueryInsert = ("Insert into MSF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY) values ('EQP','$EQP_NO','110','001','$strCrDT','$strCrTM','${tools.commarea.UserId}','$TAR_LIFE_UNIT','            ')")
							sql.execute(QueryInsert)
						}
					}
				}
				if (customAttribute.getName() == new String("newTargetLife")) {
					GetNowDateTime()
					String EQP_TAR_LIFE = customAttribute.getValue()
					def QRY2 = sql.firstRow("select * from msf071 where ENTITY_TYPE = 'EQP' and REF_NO = '100' and SEQ_NUM = '001' and ENTITY_VALUE = '$EQP_NO'")
					if(QRY2) {
						String QueryUpdate = ("update msf071 set REF_CODE = '$EQP_TAR_LIFE',LAST_MOD_DATE = '$strCrDT',LAST_MOD_TIME = '$strCrTM',LAST_MOD_USER = '${tools.commarea.UserId}'  where ENTITY_TYPE = 'EQP' and REF_NO = '100' and SEQ_NUM = '001' and ENTITY_VALUE = '$EQP_NO'")
						sql.execute(QueryUpdate)
					}else {
						String QueryInsert = ("Insert into MSF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY) values ('EQP','$EQP_NO','100','001','$strCrDT','$strCrTM','${tools.commarea.UserId}','$EQP_TAR_LIFE','            ')")
						sql.execute(QueryInsert)
					}
				}
			}
		}
		GetNowDateTime()
		String EMP_ID = GetUserEmpID(tools.commarea.UserId, tools.commarea.District.trim())
		String QueryUpdate = ("update msf071 set REF_CODE = '"+EMP_ID+"',LAST_MOD_DATE = '" + strCrDT + "',LAST_MOD_TIME = '" + strCrTM + "',LAST_MOD_USER = '" + tools.commarea.UserId + "'  where ENTITY_TYPE = 'EQP' and REF_NO = '001' and SEQ_NUM = '001' and ENTITY_VALUE = '" + EQP_NO + "'")
		sql.execute(QueryUpdate)
		return result
	}

	def isValidInteger(value){
		value instanceof Integer
	}

	Boolean isNumeric(String s) {
		log.info("Validate Numeric String")
		log.info("s: $s")
		Boolean result
		if (!s || s == "") {
			result = false
		} else {
			if (isValidInteger(s)) {
				result = true
			} else {
				result = false
			}
		}
		log.info("isNumericresult: $result")
//		for (int i = 0 ; i < s.length() ; i++) {
//			char c = s.charAt(i)
//			if (c < 0 || c > 9) {
//				return false
//			}
//		}
		return result
	}

	def GetNowDateTime() {
		Date InPer = new Date()

		Calendar cal = Calendar.getInstance()
		cal.setTime(InPer)
		int iyear = cal.get(Calendar.YEAR)
		int imonth = cal.get(Calendar.MONTH)
		int iday = cal.get(Calendar.DAY_OF_MONTH)
		int iHH = cal.get(Calendar.HOUR_OF_DAY)
		int iMM = cal.get(Calendar.MINUTE)
		int iSS = cal.get(Calendar.SECOND)

		if (iday.toString().trim().length() < 2){
			StrDT = "0" + iday.toString().trim()
		}else{
			StrDT = iday.toString().trim()
		}

//		(imonth + 1) untuk membuat bulan sesuai
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
	String GetUserEmpID(String user, String district) {
		String result
		def QRY1 = sql.firstRow("select * From msf020 where ENTRY_TYPE = 'S' and trim(ENTITY) = trim('$user') and DSTRCT_CODE = '$district'")
		if(QRY1) {
			result = QRY1.EMPLOYEE_ID
		} else {
			result = ""
		}
		return result
	}
}