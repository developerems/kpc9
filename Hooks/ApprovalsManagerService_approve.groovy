/**
 * @EMS Des 2018
 *
 * 20181217 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Initial Coding Accept CIC VIA MSEAPM
 * 20200330 - Danov - KPC Customer Support
 * 			  Update MSF230 Authsd Status if Authsd Status of MSF230 different with MSF877            
 **/
import javax.naming.InitialContext

import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.ejra.mso.GenericMsoRecord
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.ellipse.service.m3875.approvalsmanager.ApprovalsManagerService
import com.mincom.ellipse.types.m3875.instances.TransactionRetrievalCriteriaSearchParam;
import com.mincom.ellipse.types.m3875.instances.TransactionDTO
import com.mincom.ellipse.types.m3875.instances.TransactionServiceResult;
import com.mincom.ellipse.service.ServiceDTO;
import com.mincom.enterpriseservice.exception.*
import groovy.sql.Sql
import com.mincom.enterpriseservice.ellipse.*
import com.mincom.ellipse.client.connection.*
import com.mincom.ellipse.ejra.mso.*;

class ApprovalsManagerService_approve extends ServiceHook {
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

	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	@Override
	public Object onPreExecute(Object input) {
		log.info("ARSIADI3 Hooks ApprovalsManagerService_approve onPreExecute logging.version: ${hookVersion}")
		TransactionDTO c = (TransactionDTO) input
		log.info("c: $c")

		if (c.getTran877Type().value.equals("VA")){
			def QRY7 = sql.firstRow("select * from msf071 " +
					"where ENTITY_TYPE = 'CIV' and ENTITY_VALUE like '"+tools.commarea.District.trim()+c.getTransactionKey().value.substring(0,8).trim()+"%' and length(trim(replace(ENTITY_VALUE,'"+tools.commarea.District.trim()+c.getTransactionKey().value.substring(0,8).trim()+"'))) = 8 and REF_NO = '001' and SEQ_NUM = '001' and trim(REF_CODE) = trim('"+c.getTransactionKey().value.substring(8,16).trim()+"')");
			log.info ("FIND VALN_NO  : " + QRY7);
			if(!QRY7.equals(null)) {
				def QRY8 = sql.firstRow("select * from msf38b " +
						"where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+c.getTransactionKey().value.substring(0,8).trim()+"')) and trim(VALN_NO) = trim('" +QRY7.REF_CODE.trim()+ "') ");
				if(!QRY8.equals(null)) {
					log.info ("AMT_TO_CONTRACTOR  : " + QRY8.AMT_TO_CONTRACTOR);
					log.info ("VALUE_THIS_VALN  : " + QRY8.VALUE_THIS_VALN);
					log.info ("EXT_INV_AMT  : " + QRY8.EXT_INV_AMT);
					if(QRY8.AMT_TO_CONTRACTOR == 0 || QRY8.VALUE_THIS_VALN == 0 || QRY8.EXT_INV_AMT == 0) {
						throw new EnterpriseServiceOperationException(
						new ErrorMessageDTO(
						"9999", "COULD NOT APPROVE ZERO VALUATION!", "", 0, 0))

						return input
					}
					String CALC_CIC_NO = QRY7.ENTITY_VALUE.toString().replace(tools.commarea.District.trim(), "");
					CALC_CIC_NO = CALC_CIC_NO.replace(c.getTransactionKey().value.substring(0,8).trim(), "");
					BigDecimal EST_COST = 0;
					def QRY2;
					QRY2 = sql.firstRow("select * from ACA.KPF38F where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+c.getTransactionKey().value.substring(0,8).trim()+"')) and CIC_NO = '"+CALC_CIC_NO+"'");
					//log.info ("FIND CIC  : " + QRY2);
					if(QRY2.equals(null)) {
						// Raise Error
						throw new EnterpriseServiceOperationException(
						new ErrorMessageDTO(
						"9999", "INVALID CIC NUMBER / DOESN'T EXIST!", "", 0, 0))

						return input
					}else {
						if(!QRY2.CIC_TYPE.trim().equals("WO")) {
							EST_COST = QRY2.EST_COST
							if(QRY8.AMT_TO_CONTRACTOR != EST_COST || QRY8.VALUE_THIS_VALN != EST_COST || QRY8.EXT_INV_AMT != EST_COST) {
								throw new EnterpriseServiceOperationException(
								new ErrorMessageDTO(
								"9999", "AMOUNT CIC AND VALUATION MISMATCH!", "", 0, 0))

								return input
							}
						}else {
							if(QRY8.AMT_TO_CONTRACTOR != QRY2.ACT_COST || QRY8.VALUE_THIS_VALN != QRY2.ACT_COST || QRY8.EXT_INV_AMT != QRY2.ACT_COST) {
								throw new EnterpriseServiceOperationException(
								new ErrorMessageDTO(
								"9999", "AMOUNT CIC AND VALUATION MISMATCH!", "", 0, 0))

								return input
							}
						}
					}
				}
			}
		}

		return null
	}
	public Object onPostExecute(Object input , Object result) {
		log.info("Hooks ApprovalsManagerService_approve onPostExecute logging.version: ${hookVersion}")

		TransactionDTO c = (TransactionDTO) input
		String EMP_ID = GetUserEmpID(tools.commarea.UserId, tools.commarea.District.trim());
		if (c.getTran877Type().value.equals("VA")){
			BigDecimal EST_COST = 0;
			def QRY7 = sql.firstRow("select * from msf071 " +
					"where ENTITY_TYPE = 'CIV' and ENTITY_VALUE like '"+tools.commarea.District.trim()+c.getTransactionKey().value.substring(0,8).trim()+"%' and length(trim(replace(ENTITY_VALUE,'"+tools.commarea.District.trim()+c.getTransactionKey().value.substring(0,8).trim()+"'))) = 8 and REF_NO = '001' and SEQ_NUM = '001' and trim(REF_CODE) = trim('"+c.getTransactionKey().value.substring(8,16).trim()+"')");
			log.info ("FIND VALN_NO  : " + QRY7);
			if(!QRY7.equals(null)) {
				String CALC_CIC_NO = QRY7.ENTITY_VALUE.toString().replace(tools.commarea.District.trim(), "");
				CALC_CIC_NO = CALC_CIC_NO.replace(c.getTransactionKey().value.substring(0,8).trim(), "");
				//Validate CIC No
				log.info("Val CIC : ")
				def QRY2;
				QRY2 = sql.firstRow("select * from ACA.KPF38F where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+c.getTransactionKey().value.substring(0,8).trim()+"')) and CIC_NO = '"+CALC_CIC_NO+"'");
				//log.info ("FIND CIC  : " + QRY2);
				if(QRY2.equals(null)) {
					// Raise Error
					throw new EnterpriseServiceOperationException(
					new ErrorMessageDTO(
					"9999", "INVALID CIC NUMBER / DOESN'T EXIST!", "", 0, 0))

					return input
				}else {
					EST_COST = QRY2.EST_COST
				}
				CNT_NO = c.getTransactionKey().value.substring(0,8).trim();
				CIC_NO = CALC_CIC_NO;
				if(!QRY2.CIC_TYPE.trim().equals("WO")) {
					GetNowDateTime();
					def QRY8 = sql.firstRow("select trim(VALN_STATUS) VALN_STATUS from msf38b " +
							"where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and trim(VALN_NO) = trim('" +c.getTransactionKey().value.substring(8,12).trim()+ "') ");
					if(!QRY8.equals(null)) {
						log.info ("APPR VALN_STATUS : " + QRY8.VALN_STATUS);
						if (QRY8.VALN_STATUS.equals("A")) {
							String QueryUpdate = ("update ACA.KPF38F " +
									"set CIC_STATUS = '2', COMPL_BY = '"+EMP_ID+"', COMPL_DATE = '" +strCrDT+ "' " +
									"where upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"'");
							sql.execute(QueryUpdate);
							QueryUpdate = ("update ACA.KPF38F " +
									"set COMPL_BY = '"+EMP_ID+"', COMPL_DATE = '" +strCrDT+ "', ACT_COST = ? " +
									"where upper(trim(CONTRACT_NO)) = upper(trim('"+c.getTransactionKey().value.substring(0,8).trim()+"')) and CIC_NO = '"+CALC_CIC_NO+"'");
							sql.execute(QueryUpdate,[EST_COST]);
							//Create Request For KPJ389
							REP_NAME = "KPJ389";
							MED = "R";
							if (!REP_NAME.equals("")) {
								invoke_Report()
							}
						}
						if (QRY8.VALN_STATUS.equals("U")) {
							String QueryUpdate = ("update ACA.KPF38F " +
									"set CIC_STATUS = 'U' " +
									"where upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"'");
							sql.execute(QueryUpdate);
						}
					}

				}else {
					GetNowDateTime();
					def QRY8 = sql.firstRow("select trim(VALN_STATUS) VALN_STATUS from msf38b " +
							"where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and trim(VALN_NO) = trim('" +c.getTransactionKey().value.substring(8,12).trim()+ "') ");
					if(!QRY8.equals(null)) {
						log.info ("APPR VALN_STATUS : " + QRY8.VALN_STATUS);
						if (QRY8.VALN_STATUS.equals("A")) {
							String QueryUpdate = ("update ACA.KPF38F " +
									"set CIC_STATUS = '2', COMPL_BY = '"+EMP_ID+"', COMPL_DATE = '" +strCrDT+ "' " +
									"where upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"'");
							sql.execute(QueryUpdate);
							QueryUpdate = ("update ACA.KPF38F " +
									"set COMPL_BY = '"+EMP_ID+"', COMPL_DATE = '" +strCrDT+ "' " +
									"where upper(trim(CONTRACT_NO)) = upper(trim('"+c.getTransactionKey().value.substring(0,8).trim()+"')) and CIC_NO = '"+CALC_CIC_NO+"'");
							sql.execute(QueryUpdate);
							//Create Request For KPJ389
							REP_NAME = "KPJ389";
							MED = "R";
							CNT_NO = c.getTransactionKey().value.substring(0,8).trim();
							CIC_NO = CALC_CIC_NO;
							if (!REP_NAME.equals("")) {
								invoke_Report()
							}
						}
						if (QRY8.VALN_STATUS.equals("U")) {
							String QueryUpdate = ("update ACA.KPF38F " +
									"set CIC_STATUS = 'U' " +
									"where upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"'");
							sql.execute(QueryUpdate);
						}
					}
				}
			}

			String cntNo = "";
			String valNo = "";
			String suppNo = "";
			cntNo = c.getTransactionKey().value.substring(0,8);
			valNo = c.getTransactionKey().value.substring(8,12).trim();
			log.info ("cntNo  : " + cntNo);
			log.info ("valNo  : " + valNo);
			def QRY2A;
			QRY2A = sql.firstRow("select * from MSF384 where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+cntNo+"'))");
			//log.info ("FIND SUPP  : " + QRY2A);
			if(!QRY2A.equals(null)) {
				suppNo = QRY2A.SUPPLIER_NO;
				if(!suppNo.equals(null)) {
					if(!suppNo.trim().equals("")) {
						String Dstrct = tools.commarea.District.trim();
						def QRY2B = sql.firstRow("select '20'||CURR_ACCT_YR||CURR_ACCT_MN CURR_PER from msf000_CP where DSTRCT_CODE = '"+Dstrct+"' and CONTROL_REC_NO = '0002'");
						log.info ("FIND SUPP  : " + QRY2B);
						if(!QRY2B.equals(null)) {
							String currPer = QRY2B.CURR_PER;
							log.info ("currPer  : " + currPer);
							if(!currPer.equals(null)) {
								if(!currPer.trim().equals("")) {

									String StrSQL = "select a.*,count(b.MIMS_SUBLEDG) JML_SUBLEDGE, " +
											"'update msf900 set TRAN_AMOUNT = '||a.NILAI_IIP_BARU||' where dstrct_code = ''"+Dstrct+"'' and full_period = ''"+currPer+"'' and TRAN_GROUP_KEY = '''||a.TRAN_GROUP_KEY||''' and process_date||transaction_no||userno||rec900_type = '''||a.TRANS_ID||''' ' SCRIPT_UPDATE_900, " +
											"'update msf906 set TRAN_AMOUNT = '||a.NILAI_IIP_BARU||' where dstrct_code = ''"+Dstrct+"'' and full_period = ''"+currPer+"'' and process_date||transaction_no||userno||rec900_type = '''||a.TRANS_ID||''' ' SCRIPT_UPDATE_906 " +
											"from ( " +
											"select a.TRAN_GROUP_KEY,a.MIMS_SL_KEY,IMBALANCE,CNT_IIP,max(AMT_IIP) AMT_IIP,sum(c.TRAN_AMOUNT * -1) AMT_CNT, " +
											"(IMBALANCE * -1) + max(AMT_IIP) NILAI_IIP_BARU,TRANS_ID " +
											"from ( " +
											"select a.TRAN_GROUP_KEY,b.MIMS_SL_KEY,sum(a.TRAN_AMOUNT) IMBALANCE, " +
											"sum(case when a.tran_type = 'IIP' then 1 else 0 end) CNT_IIP, " +
											"b.TRAN_AMOUNT AMT_IIP,b.MEMO_AMOUNT MEMO_IIP,b.process_date||b.transaction_no||b.userno||b.rec900_type TRANS_ID " +
											"from msf900 a " +
											"left outer join msf900 b on (a.TRAN_GROUP_KEY = b.TRAN_GROUP_KEY and b.TRAN_TYPE = 'IIP' and b.DSTRCT_CODE = '"+Dstrct+"' and b.FULL_PERIOD = '"+currPer+"') " +
											"where a.DSTRCT_CODE = '"+Dstrct+"' and a.FULL_PERIOD = '"+currPer+"' " +
											"group by a.TRAN_GROUP_KEY,b.MIMS_SL_KEY,b.TRAN_AMOUNT,b.MEMO_AMOUNT,b.process_date||b.transaction_no||b.userno||b.rec900_type " +
											"having sum(a.TRAN_AMOUNT) <> 0 and sum(case when a.tran_type = 'IIP' then 1 else 0 end) = 1) a " +
											"left outer join msf900 c on (a.TRAN_GROUP_KEY = c.TRAN_GROUP_KEY and c.TRAN_TYPE = 'CNT' and c.DSTRCT_CODE = '"+Dstrct+"' and c.FULL_PERIOD = '"+currPer+"') " +
											"group by a.TRAN_GROUP_KEY,a.MIMS_SL_KEY,IMBALANCE,CNT_IIP,TRANS_ID) a " +
											"left outer join msf906 b on (b.DSTRCT_CODE = '"+Dstrct+"' and b.FULL_PERIOD = '"+currPer+"' and a.MIMS_SL_KEY = b.MIMS_SUBLEDG) " +
											"where trim(a.MIMS_SL_KEY) = trim('"+suppNo+cntNo+valNo+"') " +
											"group by a.TRAN_GROUP_KEY,a.MIMS_SL_KEY,a.IMBALANCE,a.CNT_IIP,a.AMT_IIP,a.AMT_CNT,a.NILAI_IIP_BARU,a.TRANS_ID" ;
									sql.eachRow(StrSQL, {
										log.info ("DETECTED  : ");
										String QueryUpdate = it.SCRIPT_UPDATE_900;
										sql.execute(QueryUpdate);
										QueryUpdate = it.SCRIPT_UPDATE_906;
										sql.execute(QueryUpdate);
									})
									GetNowDateTime();
									String StrSQL2 = "select DSTRCT_CODE,PROCESS_DATE||TRANSACTION_NO||USERNO||REC900_TYPE TRANS_ID from msf900 where TRAN_GROUP_KEY in (  " +
											"select b.TRAN_GROUP_KEY " +
											"from msf384 a " +
											"left outer join aca.kpf38f c on (trim(a.CONTRACT_NO) = trim(c.CONTRACT_NO) and c.cic_no = '"+CIC_NO+"') " +
											"left outer join msf900 b on (b.DSTRCT_CODE = '"+Dstrct.trim()+"' and b.PROCESS_DATE = '"+strCrDT+"' and b.REC900_TYPE = 'I' and b.TRAN_TYPE = 'IIP' and trim(MIMS_SL_KEY) = a.SUPPLIER_NO||a.CONTRACT_NO||'"+valNo.trim()+"' and " +
											"                             b.TRAN_AMOUNT < 0 and abs(case when a.currency_type <> 'USD' then b.memo_amount else b.tran_amount end) = abs(c.act_cost)) " +
											"where trim(a.CONTRACT_NO) = trim('"+cntNo+"')) and DSTRCT_CODE = '"+Dstrct+"' and FULL_PERIOD = '"+currPer+"' and TRAN_TYPE = 'CNT'";
									log.info ("StrSQL2 : " + StrSQL2);
									sql.eachRow(StrSQL2, {
										//String QueryUpdate2 = ("update msf900 set MIMS_SL_KEY = '"+cntNo+CIC_NO.trim()+"' where DSTRCT_CODE = '"+Dstrct.trim()+"' and PROCESS_DATE||TRANSACTION_NO||USERNO||REC900_TYPE = '"+it.TRANS_ID+"'");
										String QueryUpdate2 = ("update msf900 set DESCRIPTION = '"+cntNo+CIC_NO.trim()+"' where DSTRCT_CODE = '"+Dstrct.trim()+"' and PROCESS_DATE = substr('"+it.TRANS_ID+"',1,8) and TRANSACTION_NO = substr('"+it.TRANS_ID+"',9,11) and USERNO = substr('"+it.TRANS_ID+"',20,4) and REC900_TYPE = substr('"+it.TRANS_ID+"',24,1)");
										//log.info ("QueryUpdate2 : " + QueryUpdate2);
										sql.execute(QueryUpdate2);
									})
								}
							}
						}
					}
				}
			}

		}
		
		else if (c.getTran877Type().value.equals("PR")){
			String TransactionNo = ""
			String authsdstatusMSF877 = ""
			String authsdstatusMSF230 = ""
			String authsdbyMSF230 = ""
			String authsddateMSF230 = ""
			String authsdtimeMSF230 = ""
			String authsdposMSF230 = ""
			String lastmoddateMSF230 = ""
			String lastmodtimeMSF230 = ""
			String lastmoduserMSF230 = ""
			String lastmodempMSF230 = ""
			
				authsdstatusMSF877 = c.getAuthsdStatus().value.trim();
				def QRYPR230 = sql.firstRow("select * from msf230 " +
							   "where PREQ_NO = '"+c.getTransactionKey().value.trim()+"' and DSTRCT_CODE = '"+tools.commarea.District.trim()+"'");
				
				log.info("Find Valn No :" +QRYPR230);
				if(!QRYPR230.equals(null)){
					authsdstatusMSF230 = QRYPR230.AUTHSD_STATUS;
					log.info("STATUS MSO230/MSE140 : " +authsdstatusMSF230)
					log.info("STATUS MSEAPH : " + authsdstatusMSF877)
					if(authsdstatusMSF230 == authsdstatusMSF877 ){
						log.info("STATUS MSEAPH dan MSO230/MSE140 untuk PR "+c.getTransactionKey().value.trim()+ " sudah sama");
					}
					else{
						log.info("STATUS MSEAPH dan MSO230/MSE140 untuk PR "+c.getTransactionKey().value.trim()+ " tidak sama");
						def QRYPR877 = sql.firstRow("select * from msf877 " +
							"where TRIM(TRANSACTION_KEY) = '"+c.getTransactionKey().value.trim()+"' and TRAN_877_TYPE ='PR' and TRIM(DSTRCT_CODE) = '"+tools.commarea.District.trim()+"'");
						log.info("Find Transaction No :" +QRYPR877);
						if(!QRYPR877.equals(null)){
							def QRYPR87A = sql.firstRow("select * from msf87A " +
										   "where TRIM(TRANSACTION_KEY) = '"+c.getTransactionKey().value.trim()+"' and TRAN_877_TYPE ='PR' and TRIM(DSTRCT_CODE) = '"+tools.commarea.District.trim()+"' AND ACTIVE_ROW_FLAG ='Y' AND MSF877_UUID = '"+QRYPR877.UUID.trim()+"'");
							 if(!QRYPR87A.equals(null)){
								log.info("Find MSEAPH :" +QRYPR87A);
								String SqlUpdate = "UPDATE MSF230 SET AUTHSD_STATUS = '"+authsdstatusMSF877+"',AUTHSD_BY ='"+QRYPR877.AUTHSD_BY.trim()+"',AUTHSD_DATE = '"+QRYPR877.AUTHSD_DATE.trim()+"'";
								SqlUpdate = SqlUpdate + ",AUTHSD_TIME = '"+QRYPR877.AUTHSD_TIME.trim()+"',AUTHSD_POSITION = '"+QRYPR87A.AUTHSD_BY+"',LAST_MOD_DATE='"+QRYPR87A.LAST_MOD_DATE.trim()+"',LAST_MOD_TIME='"+QRYPR87A.LAST_MOD_TIME.trim()+"'";
								SqlUpdate = SqlUpdate + ",LAST_MOD_USER = '"+QRYPR87A.LAST_MOD_USER.trim()+"',LAST_MOD_EMP='"+QRYPR87A.LAST_MOD_USER.trim()+"' WHERE PREQ_NO ='"+c.getTransactionKey().value.trim()+"' AND DSTRCT_CODE = '"+tools.commarea.District.trim()+"'";
								log.info("SQL UPDATE : " + SqlUpdate);
								sql.execute(SqlUpdate);
							  }
					   }
						
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
		LOOPFLAG = false;
		MainMSO(screen);
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
	private def MainMSO(GenericMsoRecord screen){
		log.info("MAIN MSO : ")
		while(LOOPFLAG.equals(false)) {
			screen.setNextAction(GenericMsoRecord.F3_KEY);
			screen = screenService.execute(msoCon, screen);
			if ( screen.mapname.trim().equals(new String("MSM080A")) ) {
				LOOPFLAG = true
			}
		}
		LOOPFLAG = false

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