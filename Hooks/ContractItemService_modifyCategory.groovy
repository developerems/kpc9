/**
 * @EMS Nov 2018
 *
 * 20181217 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Initial Coding
 **/
import java.math.BigDecimal

import javax.naming.InitialContext

import com.mincom.ellipse.attribute.Attribute
import com.mincom.enterpriseservice.ellipse.*
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.enterpriseservice.ellipse.contractitem.ContractItemService
import com.mincom.enterpriseservice.ellipse.contractitem.ContractItemServiceModifyPortMileRequestDTO
import com.mincom.enterpriseservice.ellipse.contractitem.ContractItemServiceModifyCategoryRequestDTO
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextService
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextServiceCreateReplyDTO
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextServiceSetTextReplyDTO
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextServiceDeleteReplyDTO
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextServiceAppendReplyDTO
import com.mincom.ellipse.service.ServiceDTO;
import com.mincom.enterpriseservice.exception.*
import groovy.sql.Sql

class ContractItemService_modifyCategory extends ServiceHook {
	String hookVersion = "1"
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
	public Object onPreExecute(Object input) {
		log.info("Hooks ContractItemService_modifyCategory onPreExecute logging.version: ${hookVersion}")

		ContractItemServiceModifyCategoryRequestDTO c = (ContractItemServiceModifyCategoryRequestDTO) input
		log.info("ContractItemServiceModifyCategoryRequestDTO: " + c)
		
		String ADD_TAX_CODE = "";
		String WT_CODE = "";
		String TAX_TEXT = "";
		String CNT_NO = "";
		String PORT_MILE = "";
		
		CNT_NO = c.contractNo;
		PORT_MILE = c.portion + c.element + c.categoryNo
		List<Attribute> custAttribs = c.getCustomAttributes()
		custAttribs.each{Attribute customAttribute ->
			//log.info ("Attribute Name = ${customAttribute.getName()}")
			//log.info ("Attribute Value = ${customAttribute.getValue()}")

			if (customAttribute.getName().equals(new String("addTaxCode"))){
				ADD_TAX_CODE = customAttribute.getValue()
			}
			if (customAttribute.getName().equals(new String("wtCode"))){
				WT_CODE = customAttribute.getValue()
			}
			if (customAttribute.getName().equals(new String("taxText"))){
				TAX_TEXT = customAttribute.getValue()
				String[] TemporaryArray;
				TemporaryArray = TAX_TEXT.split("(?<=\\G.{60})");
				log.info ("TemporaryArray Length :" + TemporaryArray.length)
				String VAL_MESS = "";
				VAL_MESS = CREATE_TEXT(CNT_NO + PORT_MILE,TemporaryArray);
				if (!VAL_MESS.equals("")) {
					// Raise Error
					throw new EnterpriseServiceOperationException(
					new ErrorMessageDTO(
					"9999", VAL_MESS, "TAX_TEXT", 0, 0))
					return input
				}
			}
		}
		
		//Validate Add Tax Code
		if (ADD_TAX_CODE.equals(null) || ADD_TAX_CODE.trim().equals("")) {
			// Raise Error
			throw new EnterpriseServiceOperationException(
			new ErrorMessageDTO(
			"9999", "ADDITIONAL TAX REQUIRED!", "addTaxCode", 0, 0))
			return input
		}else {
			def QRY1 = sql.firstRow("select * from msf013 where trim(ATAX_CODE) = trim('"+ADD_TAX_CODE+"')");
			if(QRY1.equals(null)) {
				// Raise Error
				throw new EnterpriseServiceOperationException(
				new ErrorMessageDTO(
				"9999", "ADDITIONAL TAX DOES NOT EXIST!", "addTaxCode", 0, 0))
				return input
			}
		}
		//Validate WH Tax Code
		if (WT_CODE.equals(null) || WT_CODE.trim().equals("")) {
			// Raise Error
			throw new EnterpriseServiceOperationException(
			new ErrorMessageDTO(
			"9999", "WITHHOLDING TAX REQUIRED!", "wtCode", 0, 0))
			return input
		}else {
			def QRY1 = sql.firstRow("select * from msf010 where trim(TABLE_TYPE) = 'WT' and trim(TABLE_CODE) = trim('"+WT_CODE+"')");
			if(QRY1.equals(null)) {
				// Raise Error
				throw new EnterpriseServiceOperationException(
				new ErrorMessageDTO(
				"9999", "ADDITIONAL TAX DOES NOT EXIST!", "wtCode", 0, 0))
				return input
			}
		}
		
		String EMP_ID = GetUserEmpID(tools.commarea.UserId, tools.commarea.District.trim());
		def QRY1 = sql.firstRow("select * from msf071 " +
			"where ENTITY_TYPE = 'CIA' and ENTITY_VALUE = '"+tools.commarea.District.trim()+CNT_NO.trim()+PORT_MILE.trim()+"' and REF_NO = '001' and SEQ_NUM = '001'");
			log.info ("FIND ATAX CODE: " + QRY1);
		if(!QRY1.equals(null)) {
			if (!ADD_TAX_CODE.equals(null) && !ADD_TAX_CODE.equals("")) {
				GetNowDateTime();
				String QueryUpdate = ("update msf071 set ref_code = '"+ADD_TAX_CODE+"',LAST_MOD_DATE = '"+strCrDT+"',LAST_MOD_TIME = '"+strCrTM+"',LAST_MOD_USER = '"+tools.commarea.UserId+"' where ENTITY_TYPE = 'CIA' and upper(trim(ENTITY_VALUE)) = upper(trim('"+tools.commarea.District.trim()+CNT_NO.trim()+PORT_MILE.trim()+"')) and REF_NO = '001' and SEQ_NUM = '001'");
				sql.execute(QueryUpdate);
			}
		}else {
			if (!ADD_TAX_CODE.equals(null) && !ADD_TAX_CODE.equals("")) {
				GetNowDateTime();
				String QueryInsert = ("Insert into MSF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY) values ('CIA','"+tools.commarea.District.trim()+CNT_NO.trim()+PORT_MILE.trim()+"','001','001','" + strCrDT + "','" + strCrTM + "','" + tools.commarea.UserId + "','"+ADD_TAX_CODE+"','            ')");
				sql.execute(QueryInsert);
			}
		}
		
		def QRY2 = sql.firstRow("select * from msf071 " +
			"where ENTITY_TYPE = 'CIW' and ENTITY_VALUE = '"+tools.commarea.District.trim()+CNT_NO.trim()+PORT_MILE.trim()+"' and REF_NO = '001' and SEQ_NUM = '001'");
			log.info ("FIND WHTAX CODE: " + QRY2);
		if(!QRY2.equals(null)) {
			if (!WT_CODE.equals(null) && !WT_CODE.equals("")) {
				GetNowDateTime();
				String QueryUpdate = ("update msf071 set ref_code = '"+WT_CODE+"',LAST_MOD_DATE = '"+strCrDT+"',LAST_MOD_TIME = '"+strCrTM+"',LAST_MOD_USER = '"+tools.commarea.UserId+"' where ENTITY_TYPE = 'CIW' and upper(trim(ENTITY_VALUE)) = upper(trim('"+tools.commarea.District.trim()+CNT_NO.trim()+PORT_MILE.trim()+"')) and REF_NO = '001' and SEQ_NUM = '001'");
				sql.execute(QueryUpdate);
			}
		}else {
			if (!WT_CODE.equals(null) && !WT_CODE.equals("")) {
				GetNowDateTime();
				String QueryInsert = ("Insert into MSF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY) values ('CIW','"+tools.commarea.District.trim()+CNT_NO.trim()+PORT_MILE.trim()+"','001','001','" + strCrDT + "','" + strCrTM + "','" + tools.commarea.UserId + "','"+WT_CODE+"','            ')");
				sql.execute(QueryInsert);
			}
		}

		return null
	}
	private String CREATE_TEXT(String KEY,String[] CONTENT){
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
