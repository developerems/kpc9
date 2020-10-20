/**
 * @EMS Nov 2018
 *
 * 20181217 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Initial Coding
 **/
import javax.naming.InitialContext

import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.ellipse.service.m3875.approvalsmanager.ApprovalsManagerService
import com.mincom.ellipse.types.m3875.instances.TransactionRetrievalCriteriaSearchParam;
import com.mincom.ellipse.types.m3875.instances.TransactionDTO
import com.mincom.ellipse.types.m3875.instances.TransactionServiceResult;
import com.mincom.ellipse.service.ServiceDTO;
import com.mincom.enterpriseservice.exception.*
import groovy.sql.Sql
import com.mincom.enterpriseservice.ellipse.*
import com.mincom.ellipse.attribute.Attribute


class ApprovalsManagerService_retrieveApprovals extends ServiceHook{

	String hookVersion = "1"
	String CNT_NO = "";
	String CIC_NO = "";
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
		log.info("Hooks ApprovalsManagerService_retrieveApprovals onPreExecute logging.version: ${hookVersion}")

		TransactionRetrievalCriteriaSearchParam c = (TransactionRetrievalCriteriaSearchParam) input
		log.info("TransactionDTO: " + c)
		
		if (c.getTran877Type().getValue().equals("VA")){
			List<Attribute> custAttribs = c.getCustomAttributes()

			if (custAttribs) {
				log.info("CUst Attrib ada isinya")
			} else {
				log.info("CUst Attrib null")
			}

			custAttribs.each{Attribute customAttribute ->
				log.info ("Attribute Name = ${customAttribute.getName()}")
				log.info ("Attribute Value = ${customAttribute.getValue()}")
	
				if (customAttribute.getName().equals(new String("parCntNo2"))){
					CNT_NO = customAttribute.getValue()
					log.info("CNT_NOooo: $CNT_NO")
				}
				if (customAttribute.getName().equals(new String("param"))){
					CIC_NO = customAttribute.getValue()
					log.info("CIC_NOooo: $CIC_NO")
				}
			}
			if (!CNT_NO.equals(null) && !CIC_NO.equals(null)) {
				c.getTransactionApprovalkeyPart1().setValue(CNT_NO)
				def QRY7 = sql.firstRow("select * from msf071 " +
					"where ENTITY_TYPE = 'CIV' and ENTITY_VALUE = '"+tools.commarea.District.trim()+CNT_NO.trim()+CIC_NO.trim()+"' and REF_NO = '001' and SEQ_NUM = '001'");
					log.info ("FIND VALN_NO  : " + QRY7);
				if(!QRY7.equals(null)) {
					c.getTransactionApprovalkeyPart2().setValue(QRY7.REF_CODE.trim())
				}else {
					if (!CIC_NO.trim().equals("")) {
						// Raise Error
						throw new EnterpriseServiceOperationException(
						new ErrorMessageDTO(
						"9999", "CIC NUMBER DOES NOT EXIST!", "cicNo", 0, 0))
						return input
					}
				}
			}
			
			/*
			if (!CNT_NO.equals(null)) {
				def QueryRes1 = sql.firstRow("select * from msf384 where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and CONTRACT_NO = '"+CNT_NO.trim()+"'");
				log.info ("QueryRes1 : " + QueryRes1);
				if (!QueryRes1.equals(null)){
					StrCURR = QueryRes1.CURRENCY_TYPE.trim();
					if (StrCURR.trim().equals("IDR")) {
						def QueryRes2 = sql.firstRow("select to_char(sysdate,'YYYYMMDD') NOW_DATE from dual");
						if (!QueryRes2.equals(null)){
							NowDate = QueryRes2.NOW_DATE.trim()
							def QueryRes3 = sql.firstRow("select (99999999-DATE_PER_REVSD) TGL_OLD_KURS,BUYING_RATE,SELLING_RATE from msf912 " +
								"where (99999999-DATE_PER_REVSD) in ( " +
								"select MAX(99999999-DATE_PER_REVSD) KURS_INV from msf912 " +
								"where (99999999-DATE_PER_REVSD) <= '"+NowDate+"' and LOCAL_CURRENCY = 'USD' and FOREIGN_CURR = '"+StrCURR+"') and " +
								"LOCAL_CURRENCY = 'USD' and FOREIGN_CURR = '"+StrCURR+"'");
							log.info ("QueryRes3 : " + QueryRes3);
							if (!QueryRes3.equals(null)){
								//Change rate to round UP / DOWN to Prevent Imbalance Transaction
								def QueryRes4_A = sql.firstRow("select case when substr(trunc("+QueryRes3.BUYING_RATE+"),-1) = 6 then "+QueryRes3.BUYING_RATE+" - 1 " +
									 "when substr(trunc("+QueryRes3.BUYING_RATE+"),-1) = 7 then "+QueryRes3.BUYING_RATE+" - 1 " +
									 "when substr(trunc("+QueryRes3.BUYING_RATE+"),-1) = 8 then "+QueryRes3.BUYING_RATE+" - 1 " +
									 "when substr(trunc("+QueryRes3.BUYING_RATE+"),-1) = 9 then "+QueryRes3.BUYING_RATE+" - 1 " +
									 "when substr(trunc("+QueryRes3.BUYING_RATE+"),-1) = 5 then "+QueryRes3.BUYING_RATE+" - 1 " +
									 "when substr(trunc("+QueryRes3.BUYING_RATE+"),-1) = 4 then "+QueryRes3.BUYING_RATE+" - 1 " +
									 "when substr(trunc("+QueryRes3.BUYING_RATE+"),-1) = 3 then "+QueryRes3.BUYING_RATE+" - 1 " +
									 "when substr(trunc("+QueryRes3.BUYING_RATE+"),-1) = 2 then "+QueryRes3.BUYING_RATE+" - 1 " +
									 "when substr(trunc("+QueryRes3.BUYING_RATE+"),-1) = 1 then "+QueryRes3.BUYING_RATE+" - 1 else "+QueryRes3.BUYING_RATE+" end NEW_RATE " +
								"from dual");
								log.info ("QueryRes4_A : " + QueryRes4_A);
								if (!QueryRes4_A.equals(null)){
									NewRate = QueryRes4_A.NEW_RATE.toString().trim();
									log.info ("NewRate : " + NewRate);
									StrOldBYRT = NewRate
									StrOldSLRT = NewRate
									GetNowDateTime()
									def QueryRes4 = sql.firstRow("select to_char(99999999-DATE_PER_REVSD) TGL_CURRENT_KURS,BUYING_RATE,SELLING_RATE from msf912 where (99999999-DATE_PER_REVSD) = '"+strCrDT+"' and LOCAL_CURRENCY = 'USD' and FOREIGN_CURR = '"+StrCURR+"'");
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
									}else{
										RevsDT = (99999999 - Integer.parseInt(strCrDT)).toString();
										log.info ("RevsDT : " + RevsDT);
										String QueryUpdate = ("Insert into MSF912 (DATE_PER_REVSD,FOREIGN_CURR,LOCAL_CURRENCY,BUYING_RATE,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,NATIONAL_RATE,SELLING_RATE,LAST_MOD_EMP) values (?,'"+StrCURR+" ','USD ',?,'20180410','175015','ADMIN     ',0,?,'ADMIN     ')");
										try{
											log.info("QueryUpdate = : " + QueryUpdate);
											def QueryRes5 = sql.execute(QueryUpdate,[RevsDT,StrOldBYRT,StrOldSLRT]);
											//log.info("Insert into MSF912 (DATE_PER_REVSD,FOREIGN_CURR,LOCAL_CURRENCY,BUYING_RATE,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,NATIONAL_RATE,SELLING_RATE,LAST_MOD_EMP) values ('"+RevsDT+"','"+StrCURR+" ','USD ','"+StrOldBYRT+"','20180410','175015','ADMIN     ',0,"+StrOldSLRT+",'ADMIN     ')");
											//def QueryRes6 = sql.firstRow("Insert into MSF912 (DATE_PER_REVSD,FOREIGN_CURR,LOCAL_CURRENCY,BUYING_RATE,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,NATIONAL_RATE,SELLING_RATE,LAST_MOD_EMP) values ('"+RevsDT+"','"+StrCURR+" ','USD ','"+StrOldBYRT+"','20180410','175015','ADMIN     ',0,"+StrOldSLRT+",'ADMIN     ')");
										}catch (Exception  e) {
											log.info("execption Insert = : " + e);
										}
										MODE_QUERY = "DELETE";
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
			}
			*/
		}

		return null
	}

	@Override
	public Object onPostExecute(Object input, Object result) {
		log.info("Hooks ApprovalsManagerService_retrieveApprovals onPostExecute logging.version: ${hookVersion}")
		TransactionServiceResult[] d = (TransactionServiceResult[]) result
		Integer i = 0
		for (i = 0;i<d.length;i++) {
			
			TransactionDTO TRANS_DTO = d[i].getTransactionDTO();
			log.info("TransactionDTO RESULT : " + TRANS_DTO);
			if (!TRANS_DTO.equals(null)) {
				if (!TRANS_DTO.transactionKey.equals(null)) {
					log.info ("RES VAL : " + TRANS_DTO.getTransactionKey().value);
					//TransactionDTO TRANS_DTO_SET = new TransactionDTO();

					Attribute[] ATT = new Attribute[2];
					log.info("ATT SIZE: " + ATT.size());
					ATT[0] = new Attribute();
					ATT[0].setName("contractNo");
					if (TRANS_DTO.tran877Type.value.equals("VA")) {
						ATT[0].setValue(TRANS_DTO.getTransactionKey().value.substring(0,8));
						log.info("contract nya ada isinya: ${TRANS_DTO.getTransactionKey().value.substring(0,8)}")
					}else {
						ATT[0].setValue("N/A");
						log.info("contract nya kosong")
					}
								
					def QRY7 = sql.firstRow("select * from msf071 " +
						"where ENTITY_TYPE = 'CIV' and ENTITY_VALUE like '"+tools.commarea.District.trim()+TRANS_DTO.getTransactionKey().value.substring(0,8).trim()+"%' and length(trim(replace(ENTITY_VALUE,'"+tools.commarea.District.trim()+TRANS_DTO.getTransactionKey().value.substring(0,8).trim()+"'))) = 8 and REF_NO = '001' and SEQ_NUM = '001' and trim(REF_CODE) = trim('"+TRANS_DTO.getTransactionKey().value.substring(8,16).trim()+"')");
						log.info ("FIND VALN_NO  : " + QRY7);
					if(!QRY7.equals(null)) {
						ATT[1] = new Attribute();
						ATT[1].setName("cicNo");
						String CALC_CIC_NO = QRY7.ENTITY_VALUE.toString().replace(tools.commarea.District.trim(), "");
						CALC_CIC_NO = CALC_CIC_NO.replace(TRANS_DTO.getTransactionKey().value.substring(0,8).trim(), "");
						ATT[1].setValue(CALC_CIC_NO.trim());
					}else {
						ATT[1] = new Attribute();
						ATT[1].setName("cicNo");
						ATT[1].setValue("N/A");
					}
					TRANS_DTO.setCustomAttributes(ATT);
					d[i].setTransactionDTO(TRANS_DTO);
				}
			}
		}
		if (!CNT_NO.equals(null)) {
			if (!CNT_NO.trim().equals("")) {
				def QueryRes1 = sql.firstRow("select * from msf384 where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and CONTRACT_NO = '"+CNT_NO.trim()+"'");
				log.info ("QueryRes1 : " + QueryRes1);
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
						}else if (MODE_QUERY == "DELETE"){
							String QueryUpdate = ("delete msf912 where DATE_PER_REVSD = ? and LOCAL_CURRENCY = 'USD' and FOREIGN_CURR = '"+StrCURR+"'");
								try{
									log.info("QueryDelete = : " + QueryUpdate);
									def QueryRes5 = sql.execute(QueryUpdate,[RevsDT]);
								}catch (Exception  e) {
									log.info("execption Delete = : " + e);
								}
						}
					}
				}
			}
		}
		
		//log.info("getUuid : " + d.getTransactionDTO().getUuid().value)
		//log.info("getStepAppCode : " + d.getTransactionDTO().getStepAppCode().getValue())
		/*
		ServiceDTO[] RES_DET; 
		RES_DET = d.dto
		log.info ("RES LEN : " + RES_DET.length);
		log.info ("RES DTO : " + RES_DET);
		Integer i = 0;
		for (i = 0;i<RES_DET.length;i++) {
			log.info ("RES VAL : " + RES_DET[i].getValueByName("transactionKey"));
		}
		*/
		return result
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