/**
 * @EMS Nov 2018
 *
 * 20181217 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Initial Coding
 **/

import javax.naming.InitialContext

import com.mincom.ellipse.attribute.Attribute
import com.mincom.enterpriseservice.ellipse.*
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.enterpriseservice.ellipse.contractitem.ContractItemServiceModifyPortMileRequestDTO
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextServiceCreateReplyDTO
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextServiceDeleteReplyDTO
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextServiceAppendReplyDTO
import com.mincom.enterpriseservice.exception.*
import groovy.sql.Sql

import java.text.SimpleDateFormat

class ContractItemService_modifyPortMile extends ServiceHook {
	String hookVersion = "2"
	String strCrDT = "";
	String strCrTM = "";
	String StrDT = "";
	String StrMT = "";
	String StrYR = "";
	String StrHH = "";
	String StrMM = "";
	String StrSS = "";
	
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)

	@Override
	Object onPreExecute(Object input) {
		log.info("[ARSIADI]Hooks ContractItemService_modifyPortMile onPreExecute logging.version: $hookVersion")
		ContractItemServiceModifyPortMileRequestDTO c = (ContractItemServiceModifyPortMileRequestDTO) input
		log.info("ContractItemServiceModifyPortMileRequestDTO: " + c)

		String addTaxType = 'CIA'
		String whTaxType = 'CIW'
		log.info("Contract Item Type: ${c.itemType}")

		if (c.itemType == "M") {
			String addTaxCode
			String wtCode
			String taxText
			String contractNo = c.contractNo
			String portMile = c.portion + "0101"

			List<Attribute> custAttribs = c.getCustomAttributes()
			custAttribs.each{Attribute customAttribute ->
//				log.info ("Attribute Name = ${customAttribute.getName()}")
//				log.info ("Attribute Value = ${customAttribute.getValue()}")
	
				if (customAttribute.getName() == new String("custAtaxCode")){
					addTaxCode = customAttribute.getValue() ? customAttribute.getValue().trim() : ''
					log.info("ARS1 addTaxCode: $addTaxCode")
				}

				if (customAttribute.getName() == new String("wtCode")){
					wtCode = customAttribute.getValue() ? customAttribute.getValue().trim() : ''
					log.info("ARS wtCode: $wtCode")
				}

				if (customAttribute.getName() == new String("taxText")){
					taxText = customAttribute.getValue() ? customAttribute.getValue().trim() : ''
					log.info("ARS taxText: $taxText")

					String[] TemporaryArray = taxText.split("(?<=\\G.{60})")
					log.info ("TemporaryArray Length :" + TemporaryArray.length)

					String valMess = createText(contractNo + portMile, TemporaryArray)
					log.info("valMess: $valMess")

					if (valMess != "") {
						// Raise Error
						throw new EnterpriseServiceOperationException(new ErrorMessageDTO("9999", valMess, "taxText", 0, 0))
						return input
					}
				}
			}
			
			//Validate Add Tax Code
			if (!addTaxCode || addTaxCode == '') {
				// Raise Error
				log.info("ARS2 addTaxCode: $addTaxCode")
				throw new EnterpriseServiceOperationException(new ErrorMessageDTO("9999", "ADDITIONAL TAX REQUIRED!", "addTaxCode", 0, 0))
				return input
			} else {
				log.info("ARS3 addTaxCode: $addTaxCode")
				def QRY1 = sql.firstRow("SELECT * FROM MSF013 WHERE TRIM(ATAX_CODE) = TRIM('$addTaxCode')")
				log.info("QRY1: $QRY1")
				if (!QRY1) {
					// Raise Error
					throw new EnterpriseServiceOperationException(new ErrorMessageDTO("9999", "ADDITIONAL TAX DOES NOT EXIST!", "addTaxCode", 0, 0))
					return input
				}
			}

			//Validate WH Tax Code
			if (!wtCode || wtCode == '') {
				// Raise Error
				throw new EnterpriseServiceOperationException(new ErrorMessageDTO("9999", "WITHHOLDING TAX REQUIRED!", "wtCode", 0, 0))
				return input
			} else {
				def QRY1 = sql.firstRow("select * from msf010 where trim(TABLE_TYPE) = 'WT' and trim(TABLE_CODE) = trim('$wtCode')")
				log.info("QRY1: $QRY1")
				if(!QRY1) {
					// Raise Error
					throw new EnterpriseServiceOperationException(new ErrorMessageDTO("9999", "ADDITIONAL TAX DOES NOT EXIST!", "wtCode", 0, 0))
					return input
				}
			}
			
			String employeeID = GetUserEmpID(tools.commarea.UserId, tools.commarea.District.trim())
			String entityValue = tools.commarea.District.trim() + contractNo.trim() + portMile.trim()

			log.info("employeeID: $employeeID")
			log.info("entityValue: $entityValue")
			log.info("ARS4 addTaxCode: $addTaxCode")

			def QRY1 = sql.firstRow("SELECT * FROM MSF071 WHERE ENTITY_TYPE = '$addTaxType' and ENTITY_VALUE = '$entityValue' and REF_NO = '001' and SEQ_NUM = '001'")

			log.info("ARSIADI ADD_TAX_CODE: $addTaxCode")
			log.info ("ARS FIND ATAX CODE: $QRY1")

			if (QRY1) {
				if (addTaxCode && addTaxCode != '') {
					String dateNow = getNowDate()
					String timeNow = getNowTime()
					log.info("Entity Type: $addTaxType")
					log.info("Entity Value: $entityValue")
					log.info("ARSIADI Update addTaxCode: $addTaxCode")
					log.info("last Modified Employee: $employeeID")
					log.info("Date Now: $dateNow()")
					log.info("Time Now: $timeNow()")

					String updateResult = updateMSF071(addTaxType, entityValue, addTaxCode, employeeID, dateNow, timeNow)
					log.info("ARS updateResult: $updateResult")

					if (updateResult != 'OK') {
						throw new EnterpriseServiceOperationException(new ErrorMessageDTO("9999", updateResult, "addTaxCode", 0, 0))
					}
				}
			} else {
				if (addTaxCode && addTaxCode != '') {
					String dateNow = getNowDate()
					String timeNow = getNowTime()
					log.info("Entity Type: $addTaxType")
					log.info("Entity Value: $entityValue")
					log.info("ARSIADI Insert addTaxCode: $addTaxCode")
					log.info("last Modified Employee: $employeeID")
					log.info("Date Now: $dateNow()")
					log.info("Time Now: $timeNow()")

					String insertResult = insertMSF071(addTaxType, entityValue, addTaxCode, employeeID, dateNow, timeNow)
					log.info("ARS insertResult: $insertResult)")

					if (insertResult != 'OK') {
						throw new EnterpriseServiceOperationException(new ErrorMessageDTO("9999", insertResult, "addTaxCode", 0, 0))
					}
				}
			}
			
			def QRY2 = sql.firstRow("select * from msf071 where ENTITY_TYPE = '$whTaxType' and ENTITY_VALUE = '$entityValue' and REF_NO = '001' and SEQ_NUM = '001'")

			log.info("ARSIADI whTaxCode: $wtCode")
			log.info ("FIND WHTAX CODE: " + QRY2)

			if (QRY2) {
				if (wtCode && wtCode != '') {
					String dateNow = getNowDate()
					String timeNow = getNowTime()
					log.info("Entity Type: $whTaxType")
					log.info("Entity Value: $entityValue")
					log.info("ARSIADI Update addTaxCode: $wtCode")
					log.info("last Modified Employee: $employeeID")
					log.info("Date Now: $dateNow()")
					log.info("Time Now: $timeNow()")

					String updateResult = updateMSF071(whTaxType, entityValue, wtCode, employeeID, dateNow, timeNow)
					log.info("ARS updateResultWT: $updateResult")

					if (updateResult != 'OK') {
						throw new EnterpriseServiceOperationException(new ErrorMessageDTO("9999", updateResult, "wtCode", 0, 0))
					}
				}
			} else {
				if (wtCode && wtCode != '') {
					String dateNow = getNowDate()
					String timeNow = getNowTime()
					log.info("Entity Type: $whTaxType")
					log.info("Entity Value: $entityValue")
					log.info("ARSIADI Insert addTaxCode: $wtCode")
					log.info("last Modified Employee: $employeeID")
					log.info("Date Now: $dateNow()")
					log.info("Time Now: $timeNow()")

					String insertResult = insertMSF071(whTaxType, entityValue, wtCode, employeeID, dateNow, timeNow)
					log.info("ARS insertResultWT: $insertResult)")

					if (insertResult != 'OK') {
						throw new EnterpriseServiceOperationException(new ErrorMessageDTO("9999", insertResult, "wtCode", 0, 0))
					}
				}
			}
		}
		return null
	}

	private String createText(String KEY, String[] CONTENT){
		String MESSAGE = "";
		try
		{
			log.info ("CREATE_TEXT:");
			def QRY1 = sql.firstRow("select * from MSF096_STD_STATIC " +
						"where STD_TEXT_CODE = 'GT' and trim(STD_KEY) = trim('"+KEY+"') and STD_LINE_NO = '0000'");
			//log.info ("FIND TEXT  : " + QRY1);
			String[] CONTENT2 = new String[20];
			//log.info ("CONTENT2 LENGTH :" + CONTENT2.length);
			if(QRY1.equals(null)) {
				if (CONTENT.length <= 20) {
					StdTextServiceCreateReplyDTO CRE_REP_DTO = tools.service.get("StdText").create({
						it.headingLine = " ";
						String[] textLines = null;
						textLines = CONTENT;
						it.textLine = textLines;
						it.startLineNo = 1;
						it.lineCount = CONTENT.length;
						it.totalCurrentLines = CONTENT.length;
						it.stdTextId = "GT" + KEY;
						},false)
					//log.info ("TEXT ID:" + CRE_REP_DTO.getStdTextId());
				}else {
					Integer i,j,k,l;
					l = Math.abs(CONTENT.length / 20);
					if ((CONTENT.length % 20) > 0) {
						l = l + 1;
					}
					j=0
					for(k=1;k<l;k++) {
						for(i=0;i<=19;i++) {
							CONTENT2[i] = CONTENT[i+j];
							if ((i+j) == CONTENT.length - 1) {
								break;
							}
						}
						if(k == 1) {
							StdTextServiceCreateReplyDTO CRE_REP_DTO = tools.service.get("StdText").create({
								it.headingLine = " ";
								String[] textLines = null;
								textLines = CONTENT2;
								it.textLine = textLines;
								it.startLineNo = 1;
								it.lineCount = CONTENT2.length;
								it.totalCurrentLines = CONTENT2.length;
								it.stdTextId = "GT" + KEY;
								},false)
						}else {
							StdTextServiceAppendReplyDTO APPEND_REP_DTO = tools.service.get("StdText").append({
								String[] textLines = null;
								textLines = CONTENT2;
								it.textLine = textLines;
								it.lineCount = CONTENT2.length;
								it.stdTextId = "GT" + KEY;
								},false)
						}
						j = k * 20
					}
				}
			}else {
				StdTextServiceDeleteReplyDTO DEL_REP_DTO = tools.service.get("StdText").delete({
					it.stdTextId = "GT" + KEY;
					},false)
				if (CONTENT.length <= 20) {
					StdTextServiceCreateReplyDTO CRE_REP_DTO = tools.service.get("StdText").create({
						it.headingLine = " ";
						String[] textLines = null;
						textLines = CONTENT;
						it.textLine = textLines;
						it.startLineNo = 1;
						it.lineCount = CONTENT.length;
						it.totalCurrentLines = CONTENT.length;
						it.stdTextId = "GT" + KEY;
						},false)
					//log.info ("TEXT ID:" + CRE_REP_DTO.getStdTextId());
				}else {
					Integer i,j,k,l;
					l = Math.abs(CONTENT.length / 20);
					if ((CONTENT.length % 20) > 0) {
						l = l + 1;
					}
					j=0
					for(k=1;k<l;k++) {
						for(i=0;i<=19;i++) {
							CONTENT2[i] = CONTENT[i+j];
							if ((i+j) == CONTENT.length - 1) {
								break;
							}
						}
						if(k == 1) {
							StdTextServiceCreateReplyDTO CRE_REP_DTO = tools.service.get("StdText").create({
								it.headingLine = " ";
								String[] textLines = null;
								textLines = CONTENT2;
								it.textLine = textLines;
								it.startLineNo = 1;
								it.lineCount = CONTENT2.length;
								it.totalCurrentLines = CONTENT2.length;
								it.stdTextId = "GT" + KEY;
								},false)
						}else {
							StdTextServiceAppendReplyDTO APPEND_REP_DTO = tools.service.get("StdText").append({
								String[] textLines = null;
								textLines = CONTENT2;
								it.textLine = textLines;
								it.lineCount = CONTENT2.length;
								it.stdTextId = "GT" + KEY;
								},false)
						}
						j = k * 20
					}
				}
			}
			
		}catch (EnterpriseServiceOperationException e){
			log.info ("MASUK EXCEPTION:");
			List <ErrorMessageDTO> listError = e.getErrorMessages()
			listError.each{ErrorMessageDTO errorDTO ->
					log.info ("Erorr Code:" + errorDTO.getCode())
					log.info ("Error Message:" + errorDTO.getMessage())
					log.info ("Error Fields: " + errorDTO.getFieldName())
					MESSAGE = errorDTO.getMessage();
				}
		}
		return MESSAGE;
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
	String getNowDate(){
		def date = new Date()
		def sdf = new SimpleDateFormat("yyyyMMdd")
		return sdf.format(date)
//		def stf = new SimpleDateFormat("HHmmss")
//		String timeNow = stf.format(date)
	}
	String getNowTime(){
		def date = new Date()
		def stf = new SimpleDateFormat("HHmmss")
		return stf.format(date)
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
	String insertMSF071(String entityType, String entityValue, String taxCode, String lastModBy, String dateNow, String timeNow) {
		String result
		InitialContext insert = new InitialContext()
		Object insertDataSource = insert.lookup("java:jboss/datasources/ApplicationDatasource")
		def sqlInsert = new Sql(insertDataSource)
		String queryInsert = "INSERT INTO MSF071 (ENTITY_TYPE, ENTITY_VALUE, REF_NO, SEQ_NUM, LAST_MOD_DATE, LAST_MOD_TIME,LAST_MOD_USER, REF_CODE, STD_TXT_KEY) " +
				"VALUES ('$entityType','$entityValue', '001', '001','$dateNow','$timeNow','$lastModBy','$taxCode','            ')"
		log.info("ARS queryUpdate: $queryInsert")
		try{
			sqlInsert.execute(queryInsert)
			result = "OK"
		} catch (Exception e) {
			return e.getMessage()
		}
		return result
	}
	String updateMSF071(String entityType, String entityValue, String taxCode, String lastModBy, String dateNow, String timeNow) {
		String result
		InitialContext update = new InitialContext()
		Object updateDataSource = update.lookup("java:jboss/datasources/ApplicationDatasource")
		def sqlUpdate = new Sql(updateDataSource)
		String queryUpdate = "UPDATE MSF071 SET REF_CODE = '$taxCode', LAST_MOD_DATE = '$dateNow', LAST_MOD_TIME = '$timeNow', LAST_MOD_USER = '$lastModBy' " +
				"WHERE ENTITY_TYPE = '$entityType' AND ENTITY_VALUE = '$entityValue' AND REF_NO = '001' and SEQ_NUM = '001'"
		log.info("ARS queryUpdate: $queryUpdate")
		try{
			sqlUpdate.execute(queryUpdate)
			result = "OK"
		} catch (Exception e) {
			return e.getMessage()
		}
		return result
	}
}
