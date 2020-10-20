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

class ValuationsService_approve extends ServiceHook {
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

	EllipseScreenService screenService = EllipseScreenServiceLocator.ellipseScreenService;
	ConnectionId msoCon = ConnectionHolder.connectionId;
	GenericMsoRecord screen = new GenericMsoRecord();
	Boolean LOOPFLAG = false;

	String MED = "";
	String PRINTER = "";
	String REP_NAME = "";
	String District = "";
	String CNT_NO = "";
	String CIC_NO = "";
	String WO_NO = "";
	String strPmtId = ""
	String StrErr = "";
	String ErrorMessage = ""
	String GLBLCALC_CIC_NO = ""
	String currPer = "";

	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)

	@Override
	public Object onPreExecute(Object input) {
		log.info("Hooks ValuationsService_approve onPreExecute logging.version: ${hookVersion}")
		ValuationsServiceApproveRequestDTO c = (ValuationsServiceApproveRequestDTO) input;
		def QRY0_1;

		String qry01 = "select 'CURRENT_PERIOD' DUMMY,a.curr_acct_YR,a.CURR_ACCT_MN AP_PER, b.CURR_ACCT_MN GL_PER " +
				"from dual " +
				"left outer join MSF000_CP a on (a.DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and a.CONTROL_REC_NO = '0002') " +
				"left outer join MSF000_CP b on (b.DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and b.CONTROL_REC_NO = '0010')"

		QRY0_1 = sql.firstRow("select 'CURRENT_PERIOD' DUMMY,a.curr_acct_YR,a.CURR_ACCT_MN AP_PER, b.CURR_ACCT_MN GL_PER " +
				"from dual " +
				"left outer join MSF000_CP a on (a.DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and a.CONTROL_REC_NO = '0002') " +
				"left outer join MSF000_CP b on (b.DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and b.CONTROL_REC_NO = '0010')");
		log.info ("FIND CURRENT PERIOD  : ")
		log.info("qry01: $qry01")
		if(QRY0_1) {
			currPer = "20" + QRY0_1.curr_acct_YR + QRY0_1.AP_PER.trim();
			if(!QRY0_1.AP_PER.trim().equals(QRY0_1.GL_PER.trim())) {
				//		 throw new EnterpriseServiceOperationException(
				//		 new ErrorMessageDTO(
				//		 "9999", "AP ACCOUNTING PERIOD NOT EQUAL TO GL !", "", 0, 0))
				//		 return input
			}
		}else {

			//		 throw new EnterpriseServiceOperationException(
			//		 new ErrorMessageDTO(
			//		 "9999", "CURRENT PERIOD NOT FOUND!", "", 0, 0))
			//		 return input
		}

		GLBLCALC_CIC_NO = "";
		def QRY7 = sql.firstRow("select * from msf071 " +
				"where ENTITY_TYPE = 'CIV' and ENTITY_VALUE like '"+tools.commarea.District.trim()+c.getContractNo().trim()+"%' and length(trim(replace(ENTITY_VALUE,'"+tools.commarea.District.trim()+c.getContractNo().trim()+"'))) = 8 and REF_NO = '001' and SEQ_NUM = '001' and trim(REF_CODE) = trim('"+c.getValuationNo().trim()+"')");
		log.info ("QRY7  : " + "select * from msf071 " +
				"where ENTITY_TYPE = 'CIV' and ENTITY_VALUE like '"+tools.commarea.District.trim()+c.getContractNo().trim()+"%' and length(trim(replace(ENTITY_VALUE,'"+tools.commarea.District.trim()+c.getContractNo().trim()+"'))) = 8 and REF_NO = '001' and SEQ_NUM = '001' and trim(REF_CODE) = trim('"+c.getValuationNo().trim()+"')");
		log.info ("FIND VALN_NO  : " + QRY7);
		if(!QRY7.equals(null)) {
			GLBLCALC_CIC_NO = QRY7.ENTITY_VALUE.toString().replace(tools.commarea.District.trim(), "");
		}else {
			throw new EnterpriseServiceOperationException(
			new ErrorMessageDTO(
			"9999", "INVALID CIC NUMBER / DOESN'T EXIST!", "", 0, 0));

			return input;
		}
		//Validate Remaining
		QRY7 = sql.firstRow("select sum(VALUE_THIS_VALN) TOT_APPR_VAL from msf38b where trim(CONTRACT_NO) = trim('"+c.getContractNo().trim()+"') and VALN_STATUS = 'A'");
		if(!QRY7.TOT_APPR_VAL.equals(null)) {
			def QRY8 = sql.firstRow("select CONTRACT_VAL from msf384 where trim(CONTRACT_NO) = trim('"+c.getContractNo().trim()+"')");
			if(!QRY8.equals(null)) {
				if(!QRY8.CONTRACT_VAL.equals(null)) {
					def QRY9 = sql.firstRow("select VALUE_THIS_VALN from msf38b where trim(CONTRACT_NO) = trim('"+c.getContractNo().trim()+"') and trim(VALN_NO) = trim('"+c.getValuationNo().trim()+"')");
					if(!QRY9.equals(null)) {
						if(!QRY9.VALUE_THIS_VALN.equals(null)) {
							if((QRY9.VALUE_THIS_VALN + QRY7.TOT_APPR_VAL) > QRY8.CONTRACT_VAL) {
								throw new EnterpriseServiceOperationException(
								new ErrorMessageDTO(
								"9999", "CIC VALUE WILL EXCEEDS CONTRACT VALUE!", "", 0, 0));

								return input;
							}else {
								String UpdateMSF38B = ("update msf38b set TOTAL_VALUATION = ? where trim(CONTRACT_NO) = trim('"+c.getContractNo().trim()+"') and trim(VALN_NO) = trim('"+c.getValuationNo().trim()+"')");
								sql.execute(UpdateMSF38B,QRY7.TOT_APPR_VAL);
							}
						}
					}
				}
			}
		}

		def QueryRes1 = sql.firstRow("select * from msf384 where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and CONTRACT_NO = '"+c.getContractNo()+"'");
		//log.info ("QueryRes1 : " + QueryRes1);
		if (!QueryRes1.equals(null)){
			StrCURR = QueryRes1.CURRENCY_TYPE.trim();
			if (StrCURR.trim().equals("IDR")) {
				//log.info ("Contract_no : " + c.getContractNo());
				//log.info ("Valn_no : " + c.getValuationNo());
				def QueryRes2 = sql.firstRow("select to_char(sysdate,'YYYYMMDD') NOW_DATE from dual");
				if (!QueryRes2.equals(null)){
					NowDate = QueryRes2.NOW_DATE.trim()
					def QueryRes3 = sql.firstRow("select (99999999-DATE_PER_REVSD) TGL_OLD_KURS,BUYING_RATE,SELLING_RATE from msf912 " +
							"where (99999999-DATE_PER_REVSD) in ( " +
							"select MAX(99999999-DATE_PER_REVSD) KURS_INV from msf912 " +
							"where (99999999-DATE_PER_REVSD) <= '"+NowDate+"' and LOCAL_CURRENCY = 'USD' and FOREIGN_CURR = '"+StrCURR+"') and " +
							"LOCAL_CURRENCY = 'USD' and FOREIGN_CURR = '"+StrCURR+"'");
					//log.info ("QueryRes3 : " + QueryRes3);
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
									//log.info ("NewRate : " + NewRate);
									StrOldBYRT = NewRate
									StrOldSLRT = NewRate
									GetNowDateTime()
									def QueryRes4 = sql.firstRow("select to_char(99999999-DATE_PER_REVSD) TGL_CURRENT_KURS,BUYING_RATE,SELLING_RATE from msf912 where (99999999-DATE_PER_REVSD) = '"+QueryRes3.TGL_OLD_KURS+"' and LOCAL_CURRENCY = 'USD' and FOREIGN_CURR = '"+StrCURR+"'");
									//log.info ("select to_char(99999999-DATE_PER_REVSD) TGL_CURRENT_KURS,BUYING_RATE,SELLING_RATE from msf912 where (99999999-DATE_PER_REVSD) = '"+QueryRes3.TGL_OLD_KURS+"' and LOCAL_CURRENCY = 'USD' and FOREIGN_CURR = '"+StrCURR+"'");
									//log.info ("QueryRes4 : " + QueryRes4);
									if (!QueryRes4.equals(null)){
										StrTGLNowKurs = QueryRes4.TGL_CURRENT_KURS.trim()
										StrNowBYRT = QueryRes4.BUYING_RATE.toString().trim()
										StrNowSLRT = QueryRes4.SELLING_RATE.toString().trim()
										String QueryUpdate = ("update msf912 " +
												"set BUYING_RATE = ?,SELLING_RATE = ? " +
												"where (99999999-DATE_PER_REVSD) = '"+StrTGLNowKurs+"' and LOCAL_CURRENCY = 'USD' and FOREIGN_CURR = '"+StrCURR+"'");
										try{
											//log.info("QueryUpdate = : " + QueryUpdate);
											def QueryRes5 = sql.execute(QueryUpdate,[StrOldBYRT, StrOldSLRT]);
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
		log.info("Hooks ValuationsService_approve onPostExecute logging.version: ${hookVersion}")
		ValuationsServiceApproveReplyDTO d = (ValuationsServiceApproveReplyDTO) result
		ValuationsServiceApproveRequestDTO c = (ValuationsServiceApproveRequestDTO) input;
		String EMP_ID = GetUserEmpID(tools.commarea.UserId, tools.commarea.District.trim());

		def QueryRes1 = sql.firstRow("select * from msf384 where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and CONTRACT_NO = '"+d.getContractNo()+"'");
		//log.info ("QueryRes1 : " + QueryRes1);
		if (!QueryRes1.equals(null)){
			StrCURR = QueryRes1.CURRENCY_TYPE.trim();
			if (StrCURR.trim().equals("IDR")) {
				//log.info ("MODE_QUERY : " + MODE_QUERY);
				if (MODE_QUERY == "UPDATE"){
					String QueryUpdate = ("update msf912 " +
							"set BUYING_RATE = ?,SELLING_RATE = ? " +
							"where (99999999-DATE_PER_REVSD) = '"+StrTGLNowKurs+"' and LOCAL_CURRENCY = 'USD' and FOREIGN_CURR = '"+StrCURR+"'");
					try{
						//log.info("QueryUpdate = : " + QueryUpdate);
						def QueryRes5 = sql.execute(QueryUpdate,[StrNowBYRT, StrNowSLRT]);
					}catch (Exception  e) {
						log.info("execption Update = : " + e);
					}
				}
			}
		}

		if (!d.getValnStatus().equals(null)) {
			if (d.getValnStatus().trim().equals("A")) {
				log.info("d.getValnStatus() = " + d.getValnStatus())

				BigDecimal EST_COST = 0;
				//def QRY7 = sql.firstRow("select * from msf071 where ENTITY_TYPE = 'CIV' and ENTITY_VALUE like '"+tools.commarea.District.trim()+d.getContractNo()+"%' and REF_NO = '001' and SEQ_NUM = '001' and trim(REF_CODE) = trim('"+d.getValuationNo().trim()+"')");
				//	log.info ("FIND VALN_NO  : " + QRY7);
				if(!GLBLCALC_CIC_NO.equals(null) && !GLBLCALC_CIC_NO.equals("")) {
					//String CALC_CIC_NO = QRY7.ENTITY_VALUE.toString().replace(tools.commarea.District.trim(), "");
					String CALC_CIC_NO = GLBLCALC_CIC_NO.trim();
					CALC_CIC_NO = CALC_CIC_NO.replace(d.getContractNo().trim(), "");
					//Validate CIC No
					log.info("Val CIC : ")
					def QRY2;
					QRY2 = sql.firstRow("select * from ACA.KPF38F where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+d.getContractNo().trim()+"')) and CIC_NO = '"+CALC_CIC_NO+"'");
					log.info ("FIND CIC  : " + "select * from ACA.KPF38F where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+d.getContractNo().trim()+"')) and CIC_NO = '"+CALC_CIC_NO+"'");
					if(QRY2.equals(null)) {
						// Raise Error
						throw new EnterpriseServiceOperationException(
						new ErrorMessageDTO(
						"9999", "INVALID CIC NUMBER / DOESN'T EXIST!", "", 0, 0))

						return input
					}else {
						EST_COST = QRY2.EST_COST
					}

					CNT_NO = d.getContractNo().trim();
					CIC_NO = CALC_CIC_NO;

					def QRY8_A = sql.firstRow("select trim(VALN_STATUS) VALN_STATUS from msf38b " +
							"where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and trim(VALN_NO) = trim('" +d.getValuationNo().trim()+ "') ");

					if(!QRY2.CIC_TYPE.trim().equals("WO")) {
						GetNowDateTime();
						if(!QRY8_A.equals(null)) {
							log.info ("VALN SERV, VALN_STATUS : " + QRY8_A.VALN_STATUS);
							if (QRY8_A.VALN_STATUS.equals("A")) {
								String QueryUpdate = ("update ACA.KPF38F " +
										"set CIC_STATUS = '2', COMPL_BY = '"+EMP_ID+"', COMPL_DATE = '" +strCrDT+ "' " +
										"where upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"'");
								sql.execute(QueryUpdate);
								QueryUpdate = ("update ACA.KPF38F " +
										"set COMPL_BY = '"+EMP_ID+"', COMPL_DATE = '" +strCrDT+ "', ACT_COST = ? " +
										"where upper(trim(CONTRACT_NO)) = upper(trim('"+d.getContractNo().trim()+"')) and CIC_NO = '"+CALC_CIC_NO+"'");
								sql.execute(QueryUpdate,[EST_COST]);
							}
							if (QRY8_A.VALN_STATUS.equals("U")) {
								String QueryUpdate = ("update ACA.KPF38F " +
										"set CIC_STATUS = 'U' " +
										"where upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"'");
								sql.execute(QueryUpdate);
							}
						}
					}

					if(!QRY8_A.equals(null)) {
						log.info ("VALN SERV, VALN_STATUS : " + QRY8_A.VALN_STATUS);
						if (QRY8_A.VALN_STATUS.equals("A")) {
							REP_NAME = "KPJ389";
							MED = "R";
							if (!REP_NAME.equals("")) {
								invoke_Report()
							}
						}
					}

					GetNowDateTime();
					String StrSQL2 = "select DSTRCT_CODE,PROCESS_DATE||TRANSACTION_NO||USERNO||REC900_TYPE TRANS_ID from msf900 where TRAN_GROUP_KEY in (  " +
							"select b.TRAN_GROUP_KEY " +
							"from msf384 a " +
							"left outer join aca.kpf38f c on (trim(a.CONTRACT_NO) = trim(c.CONTRACT_NO) and c.cic_no = '"+CIC_NO+"') " +
							"left outer join msf900 b on (b.DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and b.PROCESS_DATE = '"+strCrDT+"' and b.REC900_TYPE = 'I' and b.TRAN_TYPE = 'IIP' and trim(MIMS_SL_KEY) = a.SUPPLIER_NO||a.CONTRACT_NO||'"+c.getValuationNo().trim()+"' and " +
							"                             b.TRAN_AMOUNT < 0 and abs(case when a.currency_type <> 'USD' then b.memo_amount else b.tran_amount end) = abs(c.act_cost)) " +
							"where trim(a.CONTRACT_NO) = trim('"+CNT_NO+"')) and DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and FULL_PERIOD = '"+currPer+"' and TRAN_TYPE = 'CNT'";

					log.info ("CIC_NO_CEK : " + CIC_NO);
					log.info ("getValuationNo CEK : " + c.getValuationNo().trim());

					log.info ("StrSQL2 : " + StrSQL2);



					sql.eachRow(StrSQL2, {
						//String QueryUpdate2 = ("update msf900 set MIMS_SL_KEY = '"+CNT_NO+CIC_NO.trim()+"' where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and PROCESS_DATE||TRANSACTION_NO||USERNO||REC900_TYPE = '"+it.TRANS_ID+"'");
						String QueryUpdate2 = ("update msf900 set DESCRIPTION = '"+CNT_NO+CIC_NO.trim()+"' where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and PROCESS_DATE = substr('"+it.TRANS_ID+"',1,8) and TRANSACTION_NO = substr('"+it.TRANS_ID+"',9,11) and USERNO = substr('"+it.TRANS_ID+"',20,4) and REC900_TYPE = substr('"+it.TRANS_ID+"',24,1)");
						log.info ("QueryUpdate2 : " + QueryUpdate2);
						sql.execute(QueryUpdate2);
					})


					def QRY7 = sql.firstRow("select sum(VALUE_THIS_VALN) TOT_APPR_VAL from msf38b where trim(CONTRACT_NO) = trim('"+c.getContractNo().trim()+"') and VALN_STATUS = 'A'");
					if(!QRY7.TOT_APPR_VAL.equals(null)) {
						def QRY8 = sql.firstRow("select max(VALN_NO) MAX_VALN from msf38b where trim(CONTRACT_NO) = trim('"+c.getContractNo().trim()+"') and VALN_STATUS = 'A'");
						if(!QRY8.MAX_VALN.equals(null)) {
							String UpdateMSF38B = ("update msf38b set TOTAL_VALUATION = ? where trim(CONTRACT_NO) = trim('"+c.getContractNo().trim()+"') and trim(VALN_NO) = trim('"+QRY8.MAX_VALN+"')");
							sql.execute(UpdateMSF38B,QRY7.TOT_APPR_VAL);
						}
					}
				}else {
					throw new EnterpriseServiceOperationException(
					new ErrorMessageDTO(
					"9999", "INVALID CIC NUMBER / DOESN'T EXIST!", "", 0, 0));

					return input;
				}
			}
		}
		return result
	}

	private String invoke_Report(){

		log.info("----------------------------------------------")
		log.info("how_to_invoke_service - screen service - Start")
		log.info("----------------------------------------------")

		GenericMsoRecord screen = screenService.executeByName(msoCon, "MSO080");
		log.info("MSO ID : " + msoCon.getId())
		log.info("MSO SCREEN : " + screen.mapname.trim())

		if ( screen.mapname.trim().equals(new String("MSM080A")) ) {
			screen.setFieldValue("RESTART1I", REP_NAME);

			screen.nextAction = GenericMsoRecord.TRANSMIT_KEY;
			screen = screenService.execute(msoCon, screen);

			if (isErrorOrWarning(screen) ) {
				log.info("Error Message:" + screen.getErrorString())
				ErrorMessage = screen.getErrorString();
				return ErrorMessage
			}
		}

		log.info("MSO SCREEN : " + screen.mapname.trim())
		if ( screen.mapname.trim().equals(new String("MSM080A")) ) {
			screen.setFieldValue("SKLITEM1I", "1");

			screen.nextAction = GenericMsoRecord.TRANSMIT_KEY;
			screen = screenService.execute(msoCon, screen);

			if (isErrorOrWarning(screen) ) {
				log.info("Error Message:" + screen.getErrorString())
				ErrorMessage = screen.getErrorString();
				return ErrorMessage
			}
		}

		log.info("MSO SCREEN : " + screen.mapname.trim())
		if ( screen.mapname.trim().equals(new String("MSM080B")) ) {
			screen.setFieldValue("MEDIUM2I", MED);
			if (MED.equals("R")){
				screen.setFieldValue("PUB_TYPE2I", "PDF");
			}else{
				screen.setFieldValue("PRINTER_NAME2I", PRINTER);
			}
			screen.setFieldValue("SUBMIT_FLG2I", "Y");

			screen.nextAction = GenericMsoRecord.TRANSMIT_KEY;
			screen = screenService.execute(msoCon, screen);

			if (isErrorOrWarning(screen) ) {
				log.info("Error Message:" + screen.getErrorString())
				ErrorMessage = screen.getErrorString();
				return ErrorMessage
			}
		}

		log.info("MSO SCREEN : " + screen.mapname.trim())
		if ( screen.mapname.trim().equals(new String("MSM080C")) ) {
			screen.setFieldValue("PARM3I1", CNT_NO)
			screen.setFieldValue("PARM3I3", "Y")
			screen.setFieldValue("PARM3I4", CIC_NO)

			screen.nextAction = GenericMsoRecord.TRANSMIT_KEY;
			screen = screenService.execute(msoCon, screen);
			log.info(screen.getMapname())
			if (isErrorOrWarning(screen) ) {
				log.info("Error Message:" + screen.getErrorString())
				ErrorMessage = screen.getErrorString();
				return ErrorMessage
			}
		}

		log.info("MSO SCREEN : " + screen.mapname.trim())
		if ( screen.mapname.trim().equals(new String("MSM080B")) ) {
			screen.setFieldValue("DESC_LINE_22I", "")
			screen.nextAction = GenericMsoRecord.F3_KEY;
			screen = screenService.execute(msoCon, screen);
			log.info(screen.getMapname())
			if (isErrorOrWarning(screen) ) {
				log.info("Error Message:" + screen.getErrorString())
				ErrorMessage = screen.getErrorString();
				return ErrorMessage
			}
		}

		log.info("MSO SCREEN : " + screen.mapname.trim())
		if ( screen.mapname.trim().equals(new String("MSM080A")) ) {
			screen.setFieldValue("RESTART1I", "")
			screen.nextAction = GenericMsoRecord.F3_KEY;
			screen = screenService.execute(msoCon, screen);
			log.info(screen.getMapname())
			if (isErrorOrWarning(screen) ) {
				log.info("Error Message:" + screen.getErrorString())
				ErrorMessage = screen.getErrorString();
				return ErrorMessage
			}
		}
		log.info ("-----------------------------");
	}

	private boolean isErrorOrWarning(GenericMsoRecord screen) {

		return ((char)screen.errorType) == MsoErrorMessage.ERR_TYPE_ERROR || ((char)screen.errorType) == MsoErrorMessage.ERR_TYPE_WARNING;
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
