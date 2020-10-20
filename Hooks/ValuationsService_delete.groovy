package KPC.HOOKS
/**
 * @EMS Mar 2019
 *
 * 20190317 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Initial Coding - Customisastion to modify actual value when Valuation Approved 
 **/

import javax.naming.InitialContext

import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.ejra.mso.GenericMsoRecord
import com.mincom.enterpriseservice.ellipse.*
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.enterpriseservice.ellipse.contractitem.ContractItemService
import com.mincom.enterpriseservice.ellipse.contractitem.ContractItemServiceModifyPortMileRequestDTO
import com.mincom.enterpriseservice.ellipse.contract.ContractServiceModifyRequestDTO
import com.mincom.enterpriseservice.ellipse.contract.ContractServiceCreateRequestDTO
import com.mincom.enterpriseservice.ellipse.contract.ContractServiceRetrieveRequestDTO
import com.mincom.enterpriseservice.ellipse.variations.VariationsServiceRetrieveRequestDTO
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsServiceRetrieveRequestDTO
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsServiceRetrieveReplyDTO
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsServiceRetrieveReplyCollectionDTO
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsServiceApproveReplyDTO
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsServiceApproveRequestDTO
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsServiceDeleteRequestDTO
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsServiceDeleteReplyDTO
import com.mincom.enterpriseservice.ellipse.contract.ContractServiceCreateReplyDTO
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextService
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextServiceCreateReplyDTO
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextServiceSetTextReplyDTO
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextServiceDeleteReplyDTO
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextServiceAppendReplyDTO
import com.mincom.ellipse.service.ServiceDTO;
import com.mincom.enterpriseservice.exception.*
import groovy.sql.Sql
import com.mincom.ellipse.client.connection.*
import com.mincom.ellipse.ejra.mso.*;

class ValuationsService_delete extends ServiceHook {
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
	String currPer = "";
	Integer ctr1 = 0;
	
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
	public Object onPreExecute(Object input) {
		log.info("Hooks ValuationsService_delete onPreExecute logging.version: ${hookVersion}")
		ValuationsServiceDeleteRequestDTO c = (ValuationsServiceDeleteRequestDTO) input;
		def QRY0_1;
		QRY0_1 = sql.firstRow("select 'CURRENT_PERIOD' DUMMY,a.curr_acct_YR,a.CURR_ACCT_MN AP_PER, b.CURR_ACCT_MN GL_PER " +
			"from dual " +
			"left outer join MSF000_CP a on (a.DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and a.CONTROL_REC_NO = '0002') " +
			"left outer join MSF000_CP b on (b.DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and b.CONTROL_REC_NO = '0010')");
		log.info ("FIND CURRENT PERIOD  : ");
		
		//DANOV COMMENT OUT
		if(!QRY0_1.equals(null)) {
			currPer = "20" + QRY0_1.curr_acct_YR + QRY0_1.AP_PER.trim();
			if(!QRY0_1.AP_PER.trim().equals(QRY0_1.GL_PER.trim())) {
//				throw new EnterpriseServiceOperationException(
//				new ErrorMessageDTO(
//				"9999", "AP ACCOUNTING PERIOD NOT EQUAL TO GL !", "", 0, 0))
//				return input
			}
		}else {
//			throw new EnterpriseServiceOperationException(
//			new ErrorMessageDTO(
//			"9999", "CURRENT PERIOD NOT FOUND!", "", 0, 0))
//			return input
		}
		def QueryRes1 = sql.firstRow("select * from msf384 where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and CONTRACT_NO = '"+c.getContractNo()+"'");
		//log.info ("QueryRes1 : " + QueryRes1);
		if (!QueryRes1.equals(null)){
			StrCURR = QueryRes1.CURRENCY_TYPE.trim();
			if (StrCURR.trim().equals("IDR")) {
				log.info ("Contract_no : " + c.getContractNo());
				log.info ("Valn_no : " + c.getValuationNo());
				def QueryRes2 = sql.firstRow("select APPROVED_DATE NOW_DATE from msf38b where CONTRACT_NO = '"+c.getContractNo()+"' and VALN_NO = '"+c.getValuationNo()+"'");
				if (!QueryRes2.equals(null)){
					NowDate = QueryRes2.NOW_DATE.trim()
					def QueryRes3 = sql.firstRow("select (99999999-DATE_PER_REVSD) TGL_OLD_KURS,BUYING_RATE,SELLING_RATE from msf912 " +
						"where (99999999-DATE_PER_REVSD) in ( " +
						"select MAX(99999999-DATE_PER_REVSD) KURS_INV from msf912 " +
						"where (99999999-DATE_PER_REVSD) <= '"+NowDate+"' and LOCAL_CURRENCY = 'USD' and FOREIGN_CURR = '"+StrCURR+"') and " +
						"LOCAL_CURRENCY = 'USD' and FOREIGN_CURR = '"+StrCURR+"'");
					log.info ("QueryRes3 : " + QueryRes3);
					if (!QueryRes3.equals(null)){
						Integer ctr = 0;
						for (int i = 1;i < 22;i++) {
							def QueryRes4_A = sql.firstRow("select sum(CST) TOTAL from ( " +
							"select round(a.ALLOC_VAL / ("+QueryRes3.BUYING_RATE + " + " + ctr +"),2) CST " +
							"from msf38d a " +
							"where a.CONTRACT_NO = '"+c.getContractNo().trim()+"' and a.VALN_NO = '"+c.getValuationNo()+"' " +
							"union all " +
							"select round(a.VALUE_THIS_VALN / ("+QueryRes3.BUYING_RATE + " + " + ctr +") * -1,2) CST from msf38b a " +
							"where a.CONTRACT_NO = '"+c.getContractNo().trim()+"' and a.VALN_NO = '"+c.getValuationNo()+"') ");
						   if (!QueryRes4_A.equals(null)){
							   if (QueryRes4_A.TOTAL == 0) {
								   NewRate = (QueryRes3.BUYING_RATE + ctr).toString();
								   log.info ("NewRate : " + NewRate);
								   StrOldBYRT = NewRate
								   StrOldSLRT = NewRate
								   GetNowDateTime()
								   def QueryRes4 = sql.firstRow("select to_char(99999999-DATE_PER_REVSD) TGL_CURRENT_KURS,BUYING_RATE,SELLING_RATE from msf912 where (99999999-DATE_PER_REVSD) = '"+QueryRes3.TGL_OLD_KURS+"' and LOCAL_CURRENCY = 'USD' and FOREIGN_CURR = '"+StrCURR+"'");
								   log.info ("select to_char(99999999-DATE_PER_REVSD) TGL_CURRENT_KURS,BUYING_RATE,SELLING_RATE from msf912 where (99999999-DATE_PER_REVSD) = '"+QueryRes3.TGL_OLD_KURS+"' and LOCAL_CURRENCY = 'USD' and FOREIGN_CURR = '"+StrCURR+"'");
								   log.info ("QueryRes4 : " + QueryRes4);
								   if (!QueryRes4.equals(null)){
									   StrTGLNowKurs = QueryRes4.TGL_CURRENT_KURS.trim()
									   StrNowBYRT = QueryRes4.BUYING_RATE.toString().trim()
									   StrNowSLRT = QueryRes4.SELLING_RATE.toString().trim()
									   String QueryUpdate = ("update msf912 " +
														   "set BUYING_RATE = ?,SELLING_RATE = ? " +
														   "where (99999999-DATE_PER_REVSD) = '"+StrTGLNowKurs+"' and LOCAL_CURRENCY = 'USD' and FOREIGN_CURR = '"+StrCURR+"'");
									   try{
										   log.info("QueryUpdate = : " + QueryUpdate);
										   def QueryRes5 = sql.execute(QueryUpdate,[StrOldBYRT,StrOldSLRT]);
									   }catch (Exception  e) {
										   log.info("execption Update = : " + e);
									   }
									   MODE_QUERY = "UPDATE";
								   }
								   return;
							   }else {
								   ctr = ctr + 1;
							   }
						   }
						}
					}
				}
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
	public Object onPostExecute(Object input, Object result) {
		log.info("Hooks ValuationsService_delete onPostExecute logging.version: ${hookVersion}")
		//ValuationsServiceDeleteReplyDTO d = (ValuationsServiceDeleteReplyDTO) result
		ValuationsServiceDeleteRequestDTO d = (ValuationsServiceDeleteRequestDTO) input;
		String EMP_ID = GetUserEmpID(tools.commarea.UserId, tools.commarea.District.trim());
		
		def QueryRes1 = sql.firstRow("select * from msf384 where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and CONTRACT_NO = '"+d.getContractNo()+"'");
		//log.info ("QueryRes1 : " + QueryRes1);
		if (!QueryRes1.equals(null)){
			StrCURR = QueryRes1.CURRENCY_TYPE.trim();
			if (StrCURR.trim().equals("IDR")) {
				log.info ("MODE_QUERY : " + MODE_QUERY);
				if (MODE_QUERY == "UPDATE"){
					String QueryUpdate = ("update msf912 " +
						"set BUYING_RATE = ?,SELLING_RATE = ? " +
						"where (99999999-DATE_PER_REVSD) = '"+StrTGLNowKurs+"' and LOCAL_CURRENCY = 'USD' and FOREIGN_CURR = '"+StrCURR+"'");
						try{
							log.info("QueryUpdate = : " + QueryUpdate);
							def QueryRes5 = sql.execute(QueryUpdate,[StrNowBYRT,StrNowSLRT]);
						}catch (Exception  e) {
							log.info("execption Update = : " + e);
						}
				}
			}
			
			//Add Reclass WO Number in transaction
			CALC_CIC_NO = "";
			def QRY7 = sql.firstRow("select * from msf071 where ENTITY_TYPE = 'CIV' and ENTITY_VALUE like '"+tools.commarea.District.trim()+d.getContractNo().trim()+"%' and REF_NO = '001' and SEQ_NUM = '001' and trim(REF_CODE) = trim('"+d.getValuationNo().trim()+"')");
			//log.info ("FIND VALN_NO  : " + QRY7);
			if(!QRY7.equals(null)) {
				CALC_CIC_NO = QRY7.ENTITY_VALUE.toString().replace(tools.commarea.District.trim(), "");
				CALC_CIC_NO = CALC_CIC_NO.replace(d.getContractNo().trim(), "");
			}
			if (!CALC_CIC_NO.equals("")) {
				def QRY1 = sql.firstRow("select * from aca.kpf38f where trim(CONTRACT_NO) = trim('"+d.getContractNo()+"') and CIC_NO = '"+CALC_CIC_NO+"'");
				
				if(!QRY1.equals(null)) {
					if (QRY1.CIC_TYPE.equals("WO")) {
						def QRY2;
						QRY2 = sql.firstRow("select * from MSF620 where DSTRCT_CODE = '"+tools.commarea.District+"' and upper(trim(WORK_ORDER)) = upper(trim('"+QRY1.WORK_ORDER+"')) ");
						log.info ("FIND WO  : " + QRY2);
						if(QRY2.equals(null)) {
							throw new EnterpriseServiceOperationException(
								new ErrorMessageDTO(
								"9999", "INVALID WO NUMBER / DOESN'T EXIST!", "", 0, 0))
								return input
						}else {
							EQP_NO = QRY2.EQUIP_NO;
							GetNowDateTime();
							ctr1 = 0;
							String StrSQL2 = "select DSTRCT_CODE,PROCESS_DATE||TRANSACTION_NO||USERNO||REC900_TYPE TRANS_ID from msf900 where TRAN_GROUP_KEY in (  " +
							   "select b.TRAN_GROUP_KEY " +
							   "from msf384 a " +
							   "left outer join aca.kpf38f c on (trim(a.CONTRACT_NO) = trim(c.CONTRACT_NO) and c.cic_no = '"+CALC_CIC_NO+"') " +
							   "left outer join msf900 b on (b.DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and b.PROCESS_DATE = '"+strCrDT+"' and b.REC900_TYPE = 'I' and b.TRAN_TYPE = 'IIP' and trim(MIMS_SL_KEY) = a.SUPPLIER_NO||a.CONTRACT_NO||'"+d.getValuationNo().trim()+"' and " +
							   "                             b.TRAN_AMOUNT > 0 and abs(case when a.currency_type <> 'USD' then b.memo_amount else b.tran_amount end) = abs(c.act_cost)) " +
							   "where trim(a.CONTRACT_NO) = trim('"+d.getContractNo()+"')) and DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and FULL_PERIOD = '"+currPer+"' and TRAN_TYPE = 'CNT'";
							log.info ("StrSQL2 : " + StrSQL2);
							sql.eachRow(StrSQL2, {
								ctr1 = ctr1 + 1
								//DANOV-COMMENT OUT
								//invoke_MSO904(tools.commarea.District.trim(),it.TRANS_ID,QRY2.WORK_ORDER,EQP_NO);
								//String QueryUpdate2 = ("update msf900 set MIMS_SL_KEY = '"+QRY1.CONTRACT_NO+CALC_CIC_NO.trim()+"' where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and PROCESS_DATE||TRANSACTION_NO||USERNO||REC900_TYPE = '"+it.TRANS_ID+"'");
								String QueryUpdate2 = ("update msf900 set DESCRIPTION = '"+QRY1.CONTRACT_NO+CALC_CIC_NO.trim()+"' where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and PROCESS_DATE = substr('"+it.TRANS_ID+"',1,8) and TRANSACTION_NO = substr('"+it.TRANS_ID+"',9,11) and USERNO = substr('"+it.TRANS_ID+"',20,4) and REC900_TYPE = substr('"+it.TRANS_ID+"',24,1)");
								//log.info ("QueryUpdate2 : " + QueryUpdate2);
								sql.execute(QueryUpdate2);
							})
						}
					}
				}
			}
		}
		
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

	private def MainMSO3(){
		Integer i = 0;
		while(LOOPFLAG.equals(false)) {
			i = i + 1;
			log.info("MAIN MSO1 :" + screen.mapname.trim())
			screen.setNextAction(GenericMsoRecord.F3_KEY)
			//screen.nextAction = GenericMsoRecord.F3_KEY;
			screen = screenService.execute(msoCon, screen);
			log.info("MAIN MSO2 :" + screen.mapname.trim())
			if ( screen.mapname.trim().equals(new String("MSM904A")) ) {
				LOOPFLAG = true
			}
			if (i == 20) {
				LOOPFLAG = false
			}
		}
		LOOPFLAG = false
		
	}
	private String invoke_MSO904(String DSTRCT,String TRANS_ID,String NEW_WO,String EQP_NO) {
		log.info("----------------------------------------------")
		log.info("how_to_invoke_service - screen service MSO904 - Start")
		log.info("----------------------------------------------")
		
		if (ctr1 == 1) {
			screen = screenService.executeByName(msoCon, "MSO904");
		}
		log.info("MSO ID : " + msoCon.getId())
		log.info("MSO SCREEN1 : " + screen.mapname.trim())
		MainMSO3();
		log.info("MSO SCREEN2 : " + screen.mapname.trim())

		if ( screen.mapname.trim().equals(new String("MSM904A")) ) {
			log.info("STEP1 : ")
			screen.setFieldValue("DSTRCT_CODE1I", DSTRCT);
			screen.setFieldValue("TRAN_ID1I", TRANS_ID);
						
			screen.nextAction = GenericMsoRecord.TRANSMIT_KEY;
			screen = screenService.execute(msoCon, screen);
			
			if (isError(screen) ) {
				log.info("Error Message:" + screen.getErrorString())
				ErrorMessage = screen.getErrorString();
				return ErrorMessage
			}
		}
		
		if ( screen.mapname.trim().equals(new String("MSM904A")) ) {
			log.info("STEP2 : ")
			screen.setFieldValue("WO_PROJ21I", NEW_WO);
			screen.setFieldValue("WP_IND21I", "W");
			screen.setFieldValue("EQUIP2_REF1I", EQP_NO);
			screen.setFieldValue("REASON1I", "MODIFY COSTING FOR WO : " + NEW_WO);
			
			screen.nextAction = GenericMsoRecord.TRANSMIT_KEY;
			screen = screenService.execute(msoCon, screen);
			
			if (isError(screen) ) {
				log.info("Error Message:" + screen.getErrorString())
				ErrorMessage = screen.getErrorString();
				return ErrorMessage
			}
			String StrFKEYS = screen.getFunctionKeyLine().getValue()
			if (StrFKEYS.trim().contains("Confirm")){
				screen.nextAction = GenericMsoRecord.TRANSMIT_KEY;
				screen = screenService.execute(msoCon, screen);
				
				if (isError(screen) ) {
					log.info("Error Message:" + screen.getErrorString())
					ErrorMessage = screen.getErrorString();
					return ErrorMessage
				}
			}
		}
		
		
		log.info("MSO SCREEN3 : " + screen.mapname.trim());
		String StrFKEYS = screen.getFunctionKeyLine().getValue();
		log.info("StrFKEYS : " + StrFKEYS)
		
		log.info ("-----------------------------");
		return ErrorMessage
	}
	private boolean isError(GenericMsoRecord screen) {
		
		return ((char)screen.errorType) == MsoErrorMessage.ERR_TYPE_ERROR ;
	}
	private boolean isWarning(GenericMsoRecord screen) {
		
		((char)screen.errorType) == MsoErrorMessage.ERR_TYPE_WARNING ;
	}
	
}
