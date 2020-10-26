/**
 * @EMS Jan 2019
 *
 * 20190101 - a9ra5213 - Ricky Afriano - KPC UPGRADE Ellipse 8
 *            Initial Coding - Find Detail for CIC in ELL38C Detail Screen 
 **/
import javax.naming.InitialContext

import com.mincom.ellipse.app.security.SecurityToken
import com.mincom.ellipse.ejp.exceptions.InteractionNotAuthorisedException
import com.mincom.ellipse.errors.exceptions.FatalException
import com.mincom.ellipse.script.plugin.GenericScriptCreate
import com.mincom.ellipse.script.plugin.GenericScriptDelete
import com.mincom.ellipse.script.plugin.GenericScriptExecute
import com.mincom.ellipse.script.plugin.GenericScriptPlugin
import com.mincom.ellipse.script.plugin.GenericScriptResults
import com.mincom.ellipse.script.plugin.GenericScriptUpdate
import com.mincom.ellipse.script.plugin.RequestAttributes
import com.mincom.ellipse.script.plugin.GenericScriptResult
import groovy.sql.Sql;

import java.text.DecimalFormat
import com.mincom.enterpriseservice.ellipse.*
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import com.mincom.ellipse.errors.UnlocalisedError
import com.mincom.ellipse.errors.UnlocalisedMessage
import com.mincom.ellipse.errors.CobolMessages;
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsServiceCreateReplyDTO
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsServiceDeleteReplyDTO

public class ELL38C_DETAIL extends GenericScriptPlugin implements GenericScriptExecute, GenericScriptUpdate, GenericScriptCreate, GenericScriptDelete{
	String version = "1";
	InitialContext initial = new InitialContext();
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource");
	def sql = new Sql(CAISource);

	String strCrDT = "";
	String strCrTM = "";
	String StrDT = "";
	String StrMT = "";
	String StrYR = "";
	String StrHH = "";
	String StrMM = "";
	String StrSS = "";
	String StrErr = "";


	public GenericScriptResults execute(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings)
	throws FatalException {
		log.info("Execute ELL38C_DETAIL Execute : " + version );
		GenericScriptResults results = new GenericScriptResults();
		GenericScriptResult result = new GenericScriptResult();
		RequestAttributes reqAtt = requestAttributes[0];

		String CNT_NO = "";
		if (reqAtt.getAttributeStringValue("diaCntNo") != null) {
			CNT_NO = reqAtt.getAttributeStringValue("diaCntNo");
		}
		else if (reqAtt.getAttributeStringValue("parGrdCntNo") == null) {
			CNT_NO = reqAtt.getAttributeStringValue("cntNo");
		}else {
			CNT_NO = reqAtt.getAttributeStringValue("parGrdCntNo");
		}

		if (!CNT_NO) {
			CNT_NO = ""
		} else {
			CNT_NO = CNT_NO.trim()
		}

		String CIC_NO = "";
		if (!reqAtt.getAttributeStringValue("diaCicNo").equals(null)) {
			CIC_NO = reqAtt.getAttributeStringValue("diaCicNo");
		}
		else if (reqAtt.getAttributeStringValue("parGrdCicNo").equals(null)) {
			CIC_NO = reqAtt.getAttributeStringValue("cicNo");
		}else {
			CIC_NO = reqAtt.getAttributeStringValue("parGrdCicNo");
		}

		if (!CIC_NO) {
			CIC_NO = ""
		} else {
			CIC_NO = CIC_NO.trim()
		}

		String WO = "";
		if (!reqAtt.getAttributeStringValue("diaParWo").equals(null)) {
			WO = reqAtt.getAttributeStringValue("diaParWo");
		}
		else if (reqAtt.getAttributeStringValue("parGrdWoNo").equals(null)) {
			WO = reqAtt.getAttributeStringValue("wo");
		}else {
			WO = reqAtt.getAttributeStringValue("parGrdWoNo");
		}

		if (!WO) {
			WO = ""
		} else {
			WO = WO.trim()
		}

		log.info("CNT_NO : " + CNT_NO );
		log.info("CIC_NO : " + CIC_NO );
		log.info("WO : " + WO );
		DecimalFormat df = new DecimalFormat("#,##0.00;-#,##0.00");
		def QRY1;
		String query1 = "select a.*,a.CONTRACT_VAL - b.INV_VAL REM_ACT,(a.CONTRACT_VAL - b.INV_VAL) - EST_VAL REM_PLN,trim(d.table_desc) CCOC_DESC " +
				"from msf384 a " +
				"left outer join ( " +
				"select CONTRACT_NO,sum(case when CIC_STATUS = '1' then EST_COST else 0 end) EST_VAL, " +
				"sum(case when CIC_STATUS in ('2','4') then ACT_COST else 0 end) INV_VAL " +
				"from ACA.KPF38F " +
				"where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) " +
				"group by CONTRACT_NO " +
				") b on (1=1) " +
				"left outer join msf010 d on (trim(a.COND_OF_CNTRCT) = trim(d.table_code) and trim(d.table_type) = 'CCOC') " +
				"where a.DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(a.CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) "

		QRY1 = sql.firstRow("select a.*,a.CONTRACT_VAL - b.INV_VAL REM_ACT,(a.CONTRACT_VAL - b.INV_VAL) - EST_VAL REM_PLN,trim(d.table_desc) CCOC_DESC " +
				"from msf384 a " +
				"left outer join ( " +
				"select CONTRACT_NO,sum(case when CIC_STATUS = '1' then EST_COST else 0 end) EST_VAL, " +
				"sum(case when CIC_STATUS in ('2','4') then ACT_COST else 0 end) INV_VAL " +
				"from ACA.KPF38F " +
				"where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) " +
				"group by CONTRACT_NO " +
				") b on (1=1) " +
				"left outer join msf010 d on (trim(a.COND_OF_CNTRCT) = trim(d.table_code) and trim(d.table_type) = 'CCOC') " +
				"where a.DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(a.CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) ");
		log.info("query1: $query1")
		log.info("QRY1: $QRY1")

		if(QRY1) {
			result.addAttribute("coc", QRY1.CCOC_DESC);
			result.addAttribute("cocCode", QRY1.COND_OF_CNTRCT);
			result.addAttribute("remAct", QRY1.REM_ACT);
			result.addAttribute("remPlan", QRY1.REM_PLN);
			result.addAttribute("contractDesc", QRY1.CONTRACT_DESC)
			String qryCIC = "";
			String qryWO = "";

			if(!CIC_NO || CIC_NO == "") {
				qryCIC = "";
			}else {
				qryCIC = " and upper(trim(a.CIC_NO)) = upper(trim('$CIC_NO'))";
			}

			if(!WO || WO == "") {
				qryWO = "";
			}else {
				qryWO = " and upper(trim(a.WORK_ORDER)) = upper(trim('$WO')) ";
			}
			String MODE = "";
			result.addAttribute("mode", " ");
			String query2 = "select a.*," +
					"case when b.equip_no is null then ' ' else b.equip_no end EQP_NO, " +
					"case when b.dstrct_acct_code is null then ' ' else replace(b.dstrct_acct_code,a.dstrct_code) end ACCT_CODE " +
					"from ACA.KPF38F a " +
					"left outer join msf620 b on (a.dstrct_code = b.dstrct_code and a.work_order = b.work_order and a.contract_no = b.orig_doc_no) " +
					"where a.dstrct_code = '${securityToken.getDistrict()}' " +
					"and upper(trim(a.CONTRACT_NO)) = upper(trim('$CNT_NO')) " +
					"$qryCIC" +
					"$qryWO"
			log.info("queryARS : $query2")
			def QRY2 = sql.firstRow(query2)
			log.info ("FIND CIC  : $QRY2");
			if(QRY2) {
				result.addAttribute("cicInv", QRY2.CIC_INVOICE);
				result.addAttribute("wo", QRY2.WORK_ORDER);
				result.addAttribute("dst", QRY2.DSTRCT_CODE);
				result.addAttribute("estCst", QRY2.EST_COST);
				result.addAttribute("actCst", QRY2.ACT_COST);
				result.addAttribute("originator", QRY2.ORIGINATOR_CIC);
				result.addAttribute("estDate", QRY2.EST_DATE);
				result.addAttribute("acceptBy", QRY2.COMPL_BY);
				result.addAttribute("accDate", QRY2.COMPL_DATE);
				result.addAttribute("eqpNo", QRY2.EQP_NO);
				result.addAttribute("acctCode", QRY2.ACCT_CODE);
				result.addAttribute("cicNo", QRY2.CIC_NO);
				if (QRY2.CUM_COST == 0) {
					result.addAttribute("totVal", QRY2.ACT_COST);
				}else {
					result.addAttribute("totVal", QRY2.CUM_COST);
				}

				if (QRY2.CIC_STATUS.trim().equals("1")) {
					result.addAttribute("cicStat", "Estimated");
				}else if (QRY2.CIC_STATUS.trim().equals("2")) {
					result.addAttribute("cicStat", "Accepted");
				}else if (QRY2.CIC_STATUS.trim().equals("3")) {
					result.addAttribute("cicStat", "Canceled");
				}else if (QRY2.CIC_STATUS.trim().equals("4")) {
					result.addAttribute("cicStat", "Invoiced");
				}else if (QRY2.CIC_STATUS.trim().equals("U")) {
					result.addAttribute("cicStat", "Awaiting Approval");
				}else if (QRY2.CIC_STATUS.trim().equals("R")) {
					result.addAttribute("cicStat", "Rejected");
				}
				result.addAttribute("MODE", QRY2.CIC_TYPE);
				result.addAttribute("cicDesc", QRY2.CIC_DESC.trim());
				def QRY7 = sql.firstRow("select * from msf071 " +
						"where ENTITY_TYPE = 'CIV' and trim(ENTITY_VALUE) = trim('${securityToken.getDistrict()}$CNT_NO$CIC_NO') and REF_NO = '001' and SEQ_NUM = '001'");
				log.info("select * from msf071 " +
						"where ENTITY_TYPE = 'CIV' and trim(ENTITY_VALUE) = trim('${securityToken.getDistrict()}$CNT_NO$CIC_NO') and REF_NO = '001' and SEQ_NUM = '001'")
				log.info ("FIND VALN_NO  : " + QRY7);
				if(QRY7) {
					result.addAttribute("cicValnNo", QRY7.REF_CODE.trim());
				}
			}
		}
//		result.addAttribute("cntNo", QRY1.CONTRACT_NO);
//		result.addAttribute("wo", QRY1.WORK_ORDER);

		result.addAttribute("cntNo", CNT_NO)
//		result.addAttribute("cicNo", CIC_NO)
		result.addAttribute("wo", WO)

		results.add(result);
		return results;
	}

	public GenericScriptResults create(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings)
	throws FatalException {
		log.info("Create ELL38C_DETAIL : " + version )
		GenericScriptResults results = new GenericScriptResults();
		GenericScriptResult result = new GenericScriptResult();
		RequestAttributes reqAtt = requestAttributes[0];

		String CNT_NO = "";
		if (!reqAtt.getAttributeStringValue("diaCntNo").equals(null)) {
			CNT_NO = reqAtt.getAttributeStringValue("diaCntNo");
		}
		else if (reqAtt.getAttributeStringValue("parGrdCntNo").equals(null)) {
			CNT_NO = reqAtt.getAttributeStringValue("cntNo");
		}else {
			CNT_NO = reqAtt.getAttributeStringValue("parGrdCntNo");
		}

		String WO = "";
		if (!reqAtt.getAttributeStringValue("diaParWo").equals(null)) {
			WO = reqAtt.getAttributeStringValue("diaParWo");
		}
		else if (reqAtt.getAttributeStringValue("parGrdWoNo").equals(null)) {
			WO = reqAtt.getAttributeStringValue("wo");
		}else {
			WO = reqAtt.getAttributeStringValue("parGrdWoNo");
		}

		log.info("CNT_NO : " + CNT_NO );
		log.info("WO : " + WO );
		String EMP_ID = GetUserEmpID(securityToken.getUserId(), securityToken.getDistrict())
		//Validate Security

		log.info("Val Security : ")
		def QRY0;
		QRY0 = sql.firstRow("select a.EMPLOYEE_ID,a.POSITION_ID, " +
				"case when b.AUTHTY_TYPE is null then 'FALSE' else 'TRUE' END ACLK, " +
				"case when c.AUTHTY_TYPE is null then 'FALSE' else 'TRUE' END CPAU, " +
				"case when d.AUTHTY_TYPE is null then 'FALSE' else 'TRUE' END VAPP " +
				"from msf878 a " +
				"left outer join ( " +
				"select * from MSF872 " +
				"where AUTHTY_TYPE = 'ACLK') b on (a.POSITION_ID = b.POSITION_ID) " +
				"left outer join ( " +
				"select * from MSF872 " +
				"where AUTHTY_TYPE = 'CPAU') c on (a.POSITION_ID = c.POSITION_ID) " +
				"left outer join ( " +
				"select * from MSF872 " +
				"where AUTHTY_TYPE = 'VAPP') d on (a.POSITION_ID = d.POSITION_ID) " +
				"left outer join msf870 e on (a.POSITION_ID = e.POSITION_ID) " +
				"where a.EMPLOYEE_ID = '"+EMP_ID+"' and (a.POS_STOP_DATE = '00000000' or a.POS_STOP_DATE = ' ' or (case when a.POS_STOP_DATE not in ('00000000',' ') then to_date(a.POS_STOP_DATE,'YYYYMMDD') else null end >= sysdate) ) and trim(a.POSITION_ID) = trim('"+securityToken.getRole()+"')");
		if(!QRY0.equals(null)) {
			if(QRY0.ACLK.trim().equals("FALSE")) {
				StrErr = "YOU DO NOT HAVE AUTHORITY ACLK"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				//err.setFieldId("creCntNo")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
			if(QRY0.CPAU.trim().equals("FALSE")) {
				StrErr = "YOU DO NOT HAVE AUTHORITY CPAU"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				//err.setFieldId("creCntNo")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
			if(QRY0.VAPP.trim().equals("FALSE")) {
				StrErr = "YOU DO NOT HAVE AUTHORITY VAPP"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				//err.setFieldId("creCntNo")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
		}else {
			StrErr = "USER SECURITY NOT FOUND"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			//err.setFieldId("creCntNo")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		StrErr = "";
		String StrSQL = ""
		StrSQL = "select a.CLASS_NAME,a.CLASS_METHOD,b.SECURITY_ACCESS " +
				"from MSF02P a " +
				"left outer join ( " +
				"select  " +
				"case when a.GLOBAL_PROFILE = ' ' then b.GLOBAL_PROFILE else a.GLOBAL_PROFILE end GLOBAL_PROFILE " +
				"from msf878 a " +
				"left outer join msf870 b on (a.POSITION_ID = b.POSITION_ID) " +
				"where a.EMPLOYEE_ID = '"+EMP_ID+"' and trim(a.POSITION_ID) = trim('"+securityToken.getRole()+"') and (a.POS_STOP_DATE = '00000000' or a.POS_STOP_DATE = ' ' or (case when a.POS_STOP_DATE not in ('00000000',' ') then to_date(a.POS_STOP_DATE,'YYYYMMDD') else null end >= sysdate) ) " +
				")c on (1=1) " +
				"left outer join MSF02e b on (a.CLASS_NAME = b.CLASS_NAME and a.CLASS_METHOD = b.CLASS_METHOD and b.ENTRY_TYPE = 'G' and b.ENTITY = c.GLOBAL_PROFILE) " +
				"where a.CLASS_NAME = 'VALUATIONS' and (b.SECURITY_ACCESS is null or b.SECURITY_ACCESS = '0') " +
				"OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY";
		log.info ("StrSQL : " + StrSQL);
		sql.eachRow(StrSQL, {
			if(it.SECURITY_ACCESS.equals(null) || it.SECURITY_ACCESS.equals("0")) {
				StrErr = "YOU DON'T HAVE ACCESS TO " + it.CLASS_NAME.trim() + "." + it.CLASS_METHOD.trim()
			}
		})

		if (!StrErr.equals("")) {
			com.mincom.ellipse.errors.Error err = new UnlocalisedError(StrErr)
			result.addError(err)
			results.add(result)
			log.info ("StrErr  : " + StrErr);
			return results
		}

		//Validate Contract No
		def QRY1;
		QRY1 = sql.firstRow("select * from msf384 where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) ");
		log.info ("FIND CONTRACT  : ");
		if(QRY1.equals(null)) {
			StrErr = "INVALID CONTRACT NUMBER / DOESN'T EXIST"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("creCntNo")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}else {
			log.info ("CONTRACT STATUS : " + QRY1.status_384.trim());
			if(QRY1.status_384.trim().equals("FC")) {
				StrErr = "CONTRACT STATUS FINAL COMPLETION"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("creCntNo")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
			if(QRY1.status_384.trim().equals("HL")) {
				StrErr = "CONTARCT STATUS ON HOLD"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("creCntNo")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
		}
		
		//Validate VALN No
		def QRY1_A;
		QRY1_A = sql.firstRow("select max(VALN_NO) MAX_VALN_NO from msf38b where upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) ");
		log.info ("FIND MAX VALN  : ");
		if(!QRY1_A.equals(null)) {
			if(!QRY1_A.MAX_VALN_NO.equals(null)) {
				if(QRY1_A.MAX_VALN_NO.equals("9999") || QRY1_A.MAX_VALN_NO.equals("0000")) {
					StrErr = "MAXIMUM VALUATION NUMBER REACH"
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					err.setFieldId("creCntNo")
					result.addError(err)
					results.add(result)
					RollErrMes();
					return results
				}
			}
		}

		//Validate WO NO
		if(!WO.equals(null)) {
			def QRY2;
			QRY2 = sql.firstRow("select * from MSF620 where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(WORK_ORDER)) = upper(trim('"+WO+"')) ");
			log.info ("FIND WO  : ");
			if(QRY2.equals(null)) {
				StrErr = "INVALID WO NUMBER / DOESN'T EXIST"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("wo")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}else {
				/*
				 if(!QRY2.ORIG_DOC_NO.equals(null)) {
				 if(!CNT_NO.trim().equals(QRY2.ORIG_DOC_NO.trim())) {
				 StrErr = "CONTRACT AND WORK ORDER NOT MATCH"
				 SetErrMes();
				 com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				 err.setFieldId("wo")
				 result.addError(err)
				 results.add(result)
				 RollErrMes();
				 return results
				 }
				 }
				 if(!QRY2.wo_status_m.trim().equals("A")) {
				 StrErr = "WORK ORDER STATUS UNAUTHORIZED OR CLOSED"
				 SetErrMes();
				 com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				 err.setFieldId("wo")
				 result.addError(err)
				 results.add(result)
				 RollErrMes();
				 return results
				 }
				 */
				if(QRY2.final_costs.trim().equals("Y")) {
					StrErr = "WORK ORDER COSTS HAS BEEN FINALIZED"
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					err.setFieldId("wo")
					result.addError(err)
					results.add(result)
					RollErrMes();
					return results
				}
			}
		}
		log.info ("VALIDATE WO  : ");
		if ((WO.equals(null) && ( QRY1.COND_OF_CNTRCT.trim().equals("UM")))) {
			StrErr = "WO NUMBER REQUIRED"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("wo")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}

		//Validate CIC TYPE
		String CIC_TYPE = "";
		if(QRY1.COND_OF_CNTRCT.trim().equals("NU")) {
			CIC_TYPE = "LS";
		}
		else if ((!WO.equals(null) && (QRY1.COND_OF_CNTRCT.trim().equals("CB") || QRY1.COND_OF_CNTRCT.trim().equals("UM")))) {
			CIC_TYPE = "WO";
		}else {
			CIC_TYPE = "LS";
		}

		if (WO.equals(null)) {
			WO = " ";
		}

		//Validate CIC DESC
		String CIC_DESC = reqAtt.getAttributeStringValue("cicDesc");

		if (CIC_DESC.equals(null) || CIC_DESC.equals("")){
			CIC_DESC = " "
		}else {
			def QRYDESC = sql.firstRow("select CONVERT( ?, 'US7ASCII', 'AL32UTF8' ) CIC_DESC from dual",[CIC_DESC.trim()])
			CIC_DESC = QRYDESC.CIC_DESC;
		}
		log.info ("VALIDATE WO DESC : ");
		if (CIC_DESC.length() > 40){
			StrErr = "INVALID LENGTH CIC DESCRIPTION"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("cicDesc")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}

		//Validate Remaining
		/*
		 def QRY3;
		 QRY3 = sql.firstRow("select a.*,a.CONTRACT_VAL - b.INV_VAL REM_ACT,(a.CONTRACT_VAL - b.INV_VAL) - EST_VAL REM_PLN,trim(d.table_desc) CCOC_DESC " +
		 "from msf384 a " +
		 "left outer join ( " +
		 "select CONTRACT_NO,sum(case when CIC_STATUS = '1' then EST_COST else 0 end) EST_VAL, " +
		 "sum(case when CIC_STATUS in ('2','4') then ACT_COST else 0 end) INV_VAL " +
		 "from ACA.KPF38F " +
		 "where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) " +
		 "group by CONTRACT_NO " +
		 ") b on (1=1) " +
		 "left outer join msf010 d on (trim(a.COND_OF_CNTRCT) = trim(d.table_code) and trim(d.table_type) = 'CCOC') " +
		 "where a.DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(a.CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) ");
		 log.info ("CEK REMAINING  : ");
		 log.info ("QRY3.REM_ACT  : " + QRY3.REM_ACT);
		 log.info ("QRY3.REM_PLN  : " + QRY3.REM_PLN);
		 if(!QRY3.REM_ACT.equals(null) && !QRY3.REM_PLN.equals(null)) {
		 if(QRY3.REM_ACT.equals(QRY3.REM_PLN)) {
		 DecimalFormat df = new DecimalFormat("#,##0.00;-#,##0.00");
		 result.addError(new UnlocalisedError("CIC VALUE WILL EXCEEDS CONTRACT VALUE -->" + " REM. ACT. = "+ df.format(QRY3.REM_ACT) + " REM. PLAN = " + df.format(QRY3.REM_PLN)))
		 results.add(result)
		 RollErrMes();
		 return results
		 }
		 }
		 */
		//Find Last CIC
		def QRY4;
		String LastCIC = "";
		String NewCIC = "";
		log.info ("FIND LAST CIC : ");
		QRY4 = sql.firstRow("select * from msf071 where ENTITY_TYPE = 'CIC' and upper(trim(ENTITY_VALUE)) = upper(trim('"+CNT_NO+"')) and REF_NO = '999' and SEQ_NUM = '001'");
		if(QRY4.equals(null)) {
			LastCIC = "1";
			log.info ("LastCIC A : " + LastCIC);
			NewCIC = String.format("%08d",(Integer.parseInt(LastCIC)));
			try
			{
				GetNowDateTime();
				String QueryInsert = ("Insert into MSF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY) values ('CIC','"+CNT_NO+"','999','001','" + strCrDT + "','" + strCrTM + "','" + securityToken.getUserId() + "','"+NewCIC+"','            ')");
				sql.execute(QueryInsert);
			} catch (Exception  e) {
				log.info ("Exception is : " + e);
				StrErr = "EXCEPTION : ERROR WHEN INSERT MSF071";
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541);
				result.addError(err);
				results.add(result);
				RollErrMes();
				return results
			}
		}else {
			LastCIC = QRY4.REF_CODE.trim();
			log.info ("LastCIC B : " + LastCIC);
			NewCIC = String.format("%08d",(Integer.parseInt(LastCIC) + 1));
			try
			{
				GetNowDateTime();
				String QueryUpdate = ("update msf071 set ref_code = '"+NewCIC+"',LAST_MOD_DATE = '"+strCrDT+"',LAST_MOD_TIME = '"+strCrTM+"',LAST_MOD_USER = '"+securityToken.getUserId()+"' where ENTITY_TYPE = 'CIC' and upper(trim(ENTITY_VALUE)) = upper(trim('"+CNT_NO+"')) and REF_NO = '999' and SEQ_NUM = '001'");
				sql.execute(QueryUpdate);
			} catch (Exception  e) {
				log.info ("Exception is : " + e);
				StrErr = "EXCEPTION : ERROR WHEN UPDATE MSF071";
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541);
				result.addError(err);
				results.add(result);
				RollErrMes();
				return results
			}
		}
		log.info ("CREATE KPF38F : ");
		//Create ACA.KPF38F data
		def QRY5;
		QRY5 = sql.firstRow("select * from ACA.KPF38F where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+NewCIC+"'");
		if(QRY5.equals(null)) {
			GetNowDateTime();
			String QueryInsert = ("Insert into ACA.KPF38F (DSTRCT_CODE,CONTRACT_NO,CIC_NO,CIC_TYPE,CIC_STATUS,CIC_DESC,WORK_ORDER,CUM_COST,ACT_COST,EST_COST,CIC_INVOICE,EST_BY,EST_DATE,ACT_BY,ACTUAL_DATE,COMPL_BY,COMPL_DATE,CUSTODIAN,ORIGINATOR_CIC) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			sql.execute(QueryInsert,[securityToken.getDistrict(), CNT_NO, NewCIC, CIC_TYPE, "1", CIC_DESC, WO, 0, 0, 0, "                    ", EMP_ID, strCrDT, " ", " ", " ", " ", "          ", EMP_ID]);
			//Create VALUATION
			String VAL_MESS = "";
			def QRY6 = sql.firstRow("select * from msf071 " +
					"where ENTITY_TYPE = 'CIV' and ENTITY_VALUE = '"+securityToken.getDistrict()+CNT_NO+NewCIC+"' and REF_NO = '001' and SEQ_NUM = '001'");
			log.info ("FIND VALN_NO  : " + QRY6);
			if(QRY6.equals(null)) {
				VAL_MESS = CREATE_VAL(CNT_NO,QRY1.SUPPLIER_NO,EMP_ID,0,securityToken.getDistrict(),NewCIC);
				if (!VAL_MESS.equals("")) {
					StrErr = VAL_MESS
					StrErr = StrErr.replace("VALUATION", "CIC")
					//SetErrMes();
					//com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					//err.setFieldId("cicNo")
					//result.addError(err)
					result.addError(new UnlocalisedError(StrErr))
					results.add(result)
					//RollErrMes();
					return results
				}
			}
		}else {
			StrErr = "CIC ALREADY EXIST";
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541);
			result.addError(err);
			results.add(result);
			RollErrMes();
			return results
		}



		result.addAttribute("cntNo", CNT_NO);
		result.addAttribute("cicNo", NewCIC);
		result.addAttribute("wo", WO);

		results.add(result);
		return results;
	}

	public GenericScriptResults update(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings)
	throws FatalException {
		log.info("Update ELL38C_DETAIL : " + version );
		GenericScriptResults results = new GenericScriptResults();
		GenericScriptResult result = new GenericScriptResult();
		RequestAttributes reqAtt = requestAttributes[0];

		String CNT_NO = "";
		if (!reqAtt.getAttributeStringValue("diaCntNo").equals(null)) {
			CNT_NO = reqAtt.getAttributeStringValue("diaCntNo");
		}
		else if (reqAtt.getAttributeStringValue("parGrdCntNo").equals(null)) {
			CNT_NO = reqAtt.getAttributeStringValue("cntNo");
		}else {
			CNT_NO = reqAtt.getAttributeStringValue("parGrdCntNo");
		}

		String CIC_NO = "";
		if (!reqAtt.getAttributeStringValue("diaCicNo").equals(null)) {
			CIC_NO = reqAtt.getAttributeStringValue("diaCicNo");
		}
		else if (reqAtt.getAttributeStringValue("parGrdCicNo").equals(null)) {
			CIC_NO = reqAtt.getAttributeStringValue("cicNo");
		}else {
			CIC_NO = reqAtt.getAttributeStringValue("parGrdCicNo");
		}

		String WO = "";
		if (!reqAtt.getAttributeStringValue("diaParWo").equals(null)) {
			WO = reqAtt.getAttributeStringValue("diaParWo");
		}
		else if (reqAtt.getAttributeStringValue("parGrdWoNo").equals(null)) {
			WO = reqAtt.getAttributeStringValue("wo");
		}else {
			WO = reqAtt.getAttributeStringValue("parGrdWoNo");
		}

		log.info("CNT_NO : " + CNT_NO );
		log.info("WO : " + WO );
		//Validate Contract No
		def QRY1;
		QRY1 = sql.firstRow("select * from msf384 where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) ");
		log.info ("FIND CONTRACT  : " + QRY1);
		if(QRY1.equals(null)) {
			StrErr = "INVALID CONTRACT NUMBER / DOESN'T EXIST"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("creCntNo")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}

		//Validate WO NO
		if(!WO.equals(null)) {
			if(!WO.trim().equals("")) {
				def QRY2;
				QRY2 = sql.firstRow("select * from MSF620 where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(WORK_ORDER)) = upper(trim('"+WO+"')) ");
				log.info ("FIND WO  : " + QRY2);
				if(QRY2.equals(null)) {
					StrErr = "INVALID WO NUMBER / DOESN'T EXIST"
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					err.setFieldId("wo")
					result.addError(err)
					results.add(result)
					RollErrMes();
					return results
				}else {
					/*
					 if(!QRY2.ORIG_DOC_NO.equals(null)) {
					 if(!CNT_NO.trim().equals(QRY2.ORIG_DOC_NO.trim())) {
					 StrErr = "CONTRACT AND WORK ORDER NOT MATCH"
					 SetErrMes();
					 com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					 err.setFieldId("wo")
					 result.addError(err)
					 results.add(result)
					 RollErrMes();
					 return results
					 }
					 }
					 if(!QRY2.wo_status_m.trim().equals("A")) {
					 StrErr = "WORK ORDER STATUS UNAUTHORIZED OR CLOSED"
					 SetErrMes();
					 com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					 err.setFieldId("wo")
					 result.addError(err)
					 results.add(result)
					 RollErrMes();
					 return results
					 }
					 */
					if(QRY2.final_costs.trim().equals("Y")) {
						StrErr = "WORK ORDER COSTS HAS BEEN FINALIZED"
						SetErrMes();
						com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
						err.setFieldId("wo")
						result.addError(err)
						results.add(result)
						RollErrMes();
						return results
					}
				}
			}
		}

		if ((WO.equals(null) && ( QRY1.COND_OF_CNTRCT.trim().equals("UM")))) {
			StrErr = "WO NUMBER REQUIRED"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("wo")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}

		String CIC_TYPE = "";
		//Validate CIC No
		def QRY2;
		QRY2 = sql.firstRow("select * from ACA.KPF38F where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"'");
		//log.info ("FIND CIC  : " + QRY2);
		if(QRY2.equals(null)) {
			StrErr = "INVALID CIC NUMBER / DOESN'T EXIST"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("cicNo")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}else {
			//Validate CIC Status
			if(!QRY2.CIC_STATUS.equals("1") && !QRY2.CIC_STATUS.equals("R")) {
				StrErr = "CIC STATUS MUST IN ESTIMATE"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				//err.setFieldId("cicNo")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
			CIC_TYPE = QRY2.CIC_TYPE;
		}

		//Validate CIC DESC
		String CIC_DESC = reqAtt.getAttributeStringValue("cicDesc");
		if (CIC_DESC.equals(null) || CIC_DESC.equals("")){
			CIC_DESC = " "
		}else {
			def QRYDESC = sql.firstRow("select CONVERT( ?, 'US7ASCII', 'AL32UTF8' ) CIC_DESC from dual",[CIC_DESC.trim()])
			CIC_DESC = QRYDESC.CIC_DESC;
		}
		if (CIC_DESC.length() > 40){
			StrErr = "INVALID LENGTH CIC DESCRIPTION"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("cicDesc")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}

		try
		{
			GetNowDateTime();
			String QueryUpdate = ("update ACA.KPF38F set CIC_DESC = ? where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"'");
			sql.execute(QueryUpdate,[CIC_DESC]);
		} catch (Exception  e) {
			log.info ("Exception is : " + e);
			StrErr = "EXCEPTION : ERROR WHEN UPDATE ACA.KPF38F";
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541);
			result.addError(err);
			results.add(result);
			RollErrMes();
			return results
		}

		results.add(result);
		return results;
	}

	public GenericScriptResults delete(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings)
	throws FatalException {
		log.info("Delete ELL38C_DETAIL : " + version )
		GenericScriptResults results = new GenericScriptResults();
		GenericScriptResult result = new GenericScriptResult();
		RequestAttributes reqAtt = requestAttributes[0];

		String CNT_NO = "";
		if (!reqAtt.getAttributeStringValue("diaCntNo").equals(null)) {
			CNT_NO = reqAtt.getAttributeStringValue("diaCntNo");
		}
		else if (reqAtt.getAttributeStringValue("parGrdCntNo").equals(null)) {
			CNT_NO = reqAtt.getAttributeStringValue("cntNo");
		}else {
			CNT_NO = reqAtt.getAttributeStringValue("parGrdCntNo");
		}

		String CIC_NO = "";
		if (!reqAtt.getAttributeStringValue("diaCicNo").equals(null)) {
			CIC_NO = reqAtt.getAttributeStringValue("diaCicNo");
		}
		else if (reqAtt.getAttributeStringValue("parGrdCicNo").equals(null)) {
			CIC_NO = reqAtt.getAttributeStringValue("cicNo");
		}else {
			CIC_NO = reqAtt.getAttributeStringValue("parGrdCicNo");
		}

		//Validate Contract No
		def QRY1;
		QRY1 = sql.firstRow("select * from msf384 where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) ");
		log.info ("FIND CONTRACT  : " + QRY1);
		if(QRY1.equals(null)) {
			StrErr = "INVALID CONTRACT NUMBER / DOESN'T EXIST"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("cntNo")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		//Validate CIC No
		log.info("Val CIC : ")
		def QRY2;
		QRY2 = sql.firstRow("select * from ACA.KPF38F where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"'");
		//log.info ("FIND CIC  : " + QRY2);
		if(QRY2.equals(null)) {
			StrErr = "INVALID CIC NUMBER / DOESN'T EXIST"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("cicNo")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}else {
			//Validate CIC Status
			if(!QRY2.CIC_STATUS.equals("1") && !QRY2.CIC_STATUS.equals("R")) {
				StrErr = "CIC STATUS MUST IN ESTIMATE / REJECT"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				//err.setFieldId("cicNo")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}else {
				//Delete CIC
				String VAL_MESS = "";
				def QRY6 = sql.firstRow("select * from msf071 " +
						"where ENTITY_TYPE = 'CIV' and trim(ENTITY_VALUE) = trim('"+securityToken.getDistrict()+CNT_NO.trim()+CIC_NO+"') and REF_NO = '001' and SEQ_NUM = '001'");
				log.info ("FIND VALN_NO  : " + QRY6);
				if(!QRY6.equals(null)) {
					if(!QRY6.REF_CODE.trim().equals("")) {
						log.info ("CEK 1  : ");
						VAL_MESS = DELETE_VAL(CNT_NO,QRY6.REF_CODE.trim(),CIC_NO,securityToken.getDistrict())
						log.info ("CEK 2  : ");
						if (!VAL_MESS.equals("")) {
							StrErr = VAL_MESS
							StrErr = StrErr.replace("VALUATION", "CIC")
							SetErrMes();
							com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
							//err.setFieldId("cicNo")
							result.addError(err)
							results.add(result)
							RollErrMes();
							return results
						}else {

							try
							{
								String QueryUpdate = ("update MSF071 set REF_CODE = ? where ENTITY_TYPE = 'CIV' and trim(ENTITY_VALUE) = trim('"+securityToken.getDistrict()+CNT_NO.trim()+CIC_NO.trim()+"') and REF_NO = '001' and SEQ_NUM = '001'");
								sql.execute(QueryUpdate,[" "]);
								//result.addAttribute("CIC_VALN_NO", " ");
							} catch (Exception  e) {
								log.info ("Exception is : " + e);
								StrErr = "EXCEPTION UPDATE MSF071 : ";
								SetErrMes();
								com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541);
								result.addError(err);
								results.add(result);
								RollErrMes();
								return results
							}
						}
					}else {
						String QueryDelete = ""
						try
						{
							log.info ("DELETE ACA.KPF38G:");
							QueryDelete = (
									"delete ACA.KPF38G where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and trim(CONTRACT_NO) = trim('"+CNT_NO+"') and trim(CIC_NO) = trim('"+CIC_NO+"')");
							sql.execute(QueryDelete);

							log.info ("DELETE ACA.KPF38F:");
							QueryDelete = (
									"delete ACA.KPF38F where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and trim(CONTRACT_NO) = trim('"+CNT_NO+"') and trim(CIC_NO) = trim('"+CIC_NO+"')");
							sql.execute(QueryDelete);
						} catch (Exception  e) {
							log.info ("Exception is : " + e);
							StrErr = "EXCEPTION UPDATE MSF38G : ";
							SetErrMes();
							com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541);
							result.addError(err);
							results.add(result);
							RollErrMes();
							return results
						}
					}
				}else {
					String QueryDelete = ""
					try
					{
						log.info ("DELETE ACA.KPF38G:");
						QueryDelete = (
								"delete ACA.KPF38G where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and trim(CONTRACT_NO) = trim('"+CNT_NO+"') and trim(CIC_NO) = trim('"+CIC_NO+"')");
						sql.execute(QueryDelete);

						log.info ("DELETE ACA.KPF38F:");
						QueryDelete = (
								"delete ACA.KPF38F where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and trim(CONTRACT_NO) = trim('"+CNT_NO+"') and trim(CIC_NO) = trim('"+CIC_NO+"')");
						sql.execute(QueryDelete);
					} catch (Exception  e) {
						log.info ("Exception is : " + e);
						StrErr = "EXCEPTION UPDATE MSF38G : ";
						SetErrMes();
						com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541);
						result.addError(err);
						results.add(result);
						RollErrMes();
						return results
					}
				}
				result.addInformationalMessage(new UnlocalisedMessage("CIC DELETED"))
			}
		}

		results.add(result);
		return results;
	}

	private String SetErrMes(){
		String Qerr = "UPDATE msf010 set TABLE_DESC = ? where TABLE_type = 'ER' and TABLE_CODE = '8541'";
		try
		{
			def QueryRes5 = sql.execute(Qerr,StrErr);
		} catch (Exception  e) {
			log.info ("Exception is : " + e);
		}
	}
	private String RollErrMes(){
		StrErr = "ERROR -"
		String Qerr = "UPDATE msf010 set TABLE_DESC = ? where TABLE_type = 'ER' and TABLE_CODE = '8541'";
		try
		{
			def QueryRes5 = sql.execute(Qerr,StrErr);
		} catch (Exception  e) {
			log.info ("Exception is : " + e);
		}
	}
	private String CREATE_VAL(String CNT_NO,String CONTRACTOR,String VALUED_BY,BigDecimal CON_AMT,String District,String CIC_NO){
		String MESSAGE = "";
		try
		{
			log.info ("CREATE_VAL:");
			ValuationsServiceCreateReplyDTO CRE_REP_DTO = service.get("Valuations").create({
				it.valnTypeFlag = "N"
				//it.getLoggedOnDistrict = "ID"
				//it.valuedByUpdate = "N"
				//it.multiValnAllwd = "N"
				//it.ctlAccountsEnabledSw = "N"
				//it.authSecSW = "N"
				it.extInvAsInv = false
				it.finalValn = false
				//it.paidByClientFlg = "N"
				it.copyPrevValnFlag = false
				//it.extCommentExists = "N"
				//it.intCommentExists = "N"
				//it.displayLDFlag = "N"
				it.contractNo = CNT_NO
				it.contractor = CONTRACTOR
				it.valuedBy = VALUED_BY
				it.cntrctrRefAmt = CON_AMT
				//it.intComment = "CV"
				//it.extComment = "XV"
			},false)
			try
			{
				GetNowDateTime();
				String QueryInsert = (
						"Insert into MSF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY) values ('CIV','"+District.trim()+CNT_NO.trim()+CIC_NO.trim()+"','001','001','"+strCrDT+"','"+strCrTM+"','"+VALUED_BY+"','"+CRE_REP_DTO.getValuationNo()+"','            ')");
				sql.execute(QueryInsert);
			} catch (Exception  e) {
				log.info ("Exception is : " + e);
				log.info ("INSERT EXCEPTION MSF071 CIV:");
				MESSAGE = "INSERT EXCEPTION MSF071 CIV:";
			}
			log.info ("CONTRACT No:" + CRE_REP_DTO.getContractNo());
			log.info ("VAL No:" + CRE_REP_DTO.getValuationNo());
			log.info ("VAL Status:" + CRE_REP_DTO.getValnStatusDescription());
		}catch (EnterpriseServiceOperationException e){
			log.info ("MASUK EXCEPTION:");
			List <ErrorMessageDTO> listError = e.getErrorMessages()
			listError.each{ErrorMessageDTO errorDTO ->
				log.info ("Erorr Code:" + errorDTO.getCode())
				log.info ("Error Message:" + errorDTO.getMessage())
				log.info ("Error Fields: " + errorDTO.getFieldName())
				MESSAGE = errorDTO.getMessage();
			}
		}catch (InteractionNotAuthorisedException e1) {
			log.info ("MASUK EXCEPTION2:");
			MESSAGE = e1.getMessage();
		}
		return MESSAGE;
	}
	private String DELETE_VAL(String CNT_NO,String VALN_NO,String CIC_NO,String District){
		String MESSAGE = "";
		try
		{
			log.info ("DELETE_VAL:");
			ValuationsServiceDeleteReplyDTO DEL_REP_DTO = service.get("Valuations").delete({
				it.contractNo = CNT_NO
				it.valuationNo = VALN_NO
			},false)
			try
			{
				log.info ("DELETE MSF877 & MSF87A:");
				String QueryDelete = (
						"delete msf87A where DSTRCT_CODE = '"+District.trim()+"' and trim(transaction_key) = trim(RPAD('"+CNT_NO+"',8)||'"+VALN_NO+"')");
				sql.execute(QueryDelete);

				QueryDelete = (
						"delete msf877 where DSTRCT_CODE = '"+District.trim()+"' and trim(transaction_key) = trim(RPAD('"+CNT_NO+"',8)||'"+VALN_NO+"')");
				sql.execute(QueryDelete);

				log.info ("DELETE MSF071 CIV:");
				QueryDelete = (
						"delete msf071 where ENTITY_TYPE = 'CIV' and trim(ENTITY_VALUE) = trim('"+District.trim()+CNT_NO.trim()+CIC_NO.trim()+"')");
				sql.execute(QueryDelete);

				log.info ("DELETE ACA.KPF38G:");
				QueryDelete = (
						"delete ACA.KPF38G where DSTRCT_CODE = '"+District.trim()+"' and trim(CONTRACT_NO) = trim('"+CNT_NO+"') and trim(CIC_NO) = trim('"+CIC_NO+"')");
				sql.execute(QueryDelete);

				log.info ("DELETE ACA.KPF38F:");
				QueryDelete = (
						"delete ACA.KPF38F where DSTRCT_CODE = '"+District.trim()+"' and trim(CONTRACT_NO) = trim('"+CNT_NO+"') and trim(CIC_NO) = trim('"+CIC_NO+"')");
				sql.execute(QueryDelete);
			} catch (Exception  e) {
				log.info ("Exception is : " + e);
				log.info ("DELETE EXCEPTION MSF071 CIV:");
				MESSAGE = "DELETE EXCEPTION";
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
	public String GetUserEmpID(String user, String district) {
		String EMP_ID = "";
		def QRY1;
		QRY1 = sql.firstRow("select * From msf020 where ENTRY_TYPE = 'S' and trim(ENTITY) = trim('"+user+"') and DSTRCT_CODE = '"+district+"'");
		if(!QRY1.equals(null)) {
			EMP_ID = QRY1.EMPLOYEE_ID;
		}
		return EMP_ID;
	}
}
