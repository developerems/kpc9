/**
 * @EMS Feb 2019
 *
 * 20190217 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Initial Coding - Finalize CIC by request for Approval in Ellipse standard Valuation 
 **/
package KPC

import com.mincom.ellipse.lsi.buffer.valuationaccrual.ValuationAccrualBufferImpl
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsServiceModifyReplyDTO

import javax.naming.InitialContext

import com.mincom.ellipse.app.security.SecurityToken
import com.mincom.ellipse.errors.exceptions.FatalException
import com.mincom.ellipse.script.plugin.GenericScriptCreate
import com.mincom.ellipse.script.plugin.GenericScriptDelete
import com.mincom.ellipse.script.plugin.GenericScriptExecute
import com.mincom.ellipse.script.plugin.GenericScriptExecuteForCollection
import com.mincom.ellipse.script.plugin.GenericScriptPlugin
import com.mincom.ellipse.script.plugin.GenericScriptResults
import com.mincom.ellipse.script.plugin.GenericScriptUpdate
import com.mincom.ellipse.script.plugin.RequestAttributes
import com.mincom.ellipse.script.plugin.GenericScriptResult
import com.mincom.ellipse.script.plugin.RestartAttributes
import groovy.sql.Sql;
import java.text.DecimalFormat
import com.mincom.enterpriseservice.ellipse.*
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import com.mincom.ellipse.errors.Error
import com.mincom.ellipse.errors.UnlocalisedError
import com.mincom.ellipse.errors.UnlocalisedMessage
import com.mincom.ellipse.errors.CobolMessages
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsServiceApproveReplyDTO
import com.mincom.enterpriseservice.ellipse.WarningMessageDTO

import java.text.SimpleDateFormat

public class FIN_CIC extends GenericScriptPlugin implements GenericScriptExecute{
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)

	String version = "1";
	String strCrDT = "";
	String strCrTM = "";
	String StrDT = "";
	String StrMT = "";
	String StrYR = "";
	String StrHH = "";
	String StrMM = "";
	String StrSS = "";
	String Errexcpt = "";
	Integer LastIndext = 0;
	String StrErr = "";
	String DST = "";

	public GenericScriptResults execute(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings)
	throws FatalException {
		log.info("Execute FIN_CIC : " + version )
		GenericScriptResults results = new GenericScriptResults()
		GenericScriptResult result = new GenericScriptResult()
		RequestAttributes reqAtt = requestAttributes[0]

		String APPR_BY = reqAtt.getAttributeStringValue("diaApprBy");
		String APPR_BY_POS = reqAtt.getAttributeStringValue("diaApprPos");
		String CNT_NO = reqAtt.getAttributeStringValue("cntNo");
		String CIC_NO = reqAtt.getAttributeStringValue("cicNo");
		log.info ("APPR_BY : " + APPR_BY);
		log.info ("APPR_BY_POS : " + APPR_BY_POS);
		log.info ("CNT_NO : " + CNT_NO);
		log.info ("CIC_NO : " + CIC_NO);
		DST = securityToken.getDistrict();
		String EMP_ID = GetUserEmpID(securityToken.getUserId(), securityToken.getDistrict());

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
		/*
		 def QRY0_1;
		 QRY0_1 = sql.firstRow("select 'CURRENT_PERIOD' DUMMY,a.CURR_ACCT_MN AP_PER, b.CURR_ACCT_MN GL_PER " +
		 "from dual " +
		 "left outer join MSF000_CP a on (a.DSTRCT_CODE = '"+securityToken.getDistrict()+"' and a.CONTROL_REC_NO = '0002') " +
		 "left outer join MSF000_CP b on (b.DSTRCT_CODE = '"+securityToken.getDistrict()+"' and b.CONTROL_REC_NO = '0010')");
		 log.info ("FIND CURRENT PERIOD  : ");
		 if(!QRY0_1.equals(null)) {
		 if(!QRY0_1.AP_PER.trim().equals(QRY0_1.GL_PER.trim())) {
		 StrErr = "AP ACCOUNTING PERIOD NOT EQUAL TO GL !"
		 SetErrMes();
		 com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
		 //err.setFieldId("creCntNo")
		 result.addError(err)
		 results.add(result)
		 RollErrMes();
		 return results
		 }
		 }else {
		 StrErr = "CURRENT PERIOD NOT FOUND!"
		 SetErrMes();
		 com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
		 //err.setFieldId("creCntNo")
		 result.addError(err)
		 results.add(result)
		 RollErrMes();
		 return results
		 }
		 */
		String VAL_MESS = "";
		if(!APPR_BY.equals(null) && APPR_BY_POS.equals(null)) {
			StrErr = "APPROVE BY POSITION REQUIRED !"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("diaApprPos")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		//Validate Contract No
		log.info("Val Contract : ")
		def QRY1;
		QRY1 = sql.firstRow("select * from msf384 where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) ");
		//log.info ("FIND CONTRACT  : " + QRY1);
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
			if(!QRY2.CIC_STATUS.equals("1") && !QRY2.CIC_STATUS.equals("R") && !QRY2.CIC_STATUS.equals("U")) {
				StrErr = "INVALID CIC STATUS SHOULD BE ESTIMATE / REJECTED"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("cicStat")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
		}
		//Validate Actual and create valuation
		def QRY7 = sql.firstRow("select * from msf071 " +
				"where ENTITY_TYPE = 'CIV' and ENTITY_VALUE = '"+securityToken.getDistrict().trim()+CNT_NO.trim()+CIC_NO.trim()+"' and REF_NO = '001' and SEQ_NUM = '001'");
		log.info ("FIND VALN_NO  : " + QRY7);
		if(!QRY7.equals(null)) {
			if(!QRY7.REF_CODE.trim().equals("")) {

				def QRY8 = sql.firstRow("select * from msf38b " +
						"where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and trim(VALN_NO) = trim('" +QRY7.REF_CODE.trim()+ "') ");
				if(!QRY8.equals(null)) {
					log.info ("AMT_TO_CONTRACTOR  : " + QRY8.AMT_TO_CONTRACTOR);
					log.info ("VALUE_THIS_VALN  : " + QRY8.VALUE_THIS_VALN);
					log.info ("EXT_INV_AMT  : " + QRY8.EXT_INV_AMT);
					if(QRY8.AMT_TO_CONTRACTOR == 0 || QRY8.VALUE_THIS_VALN == 0 || QRY8.EXT_INV_AMT == 0) {
						StrErr = "COULD NOT APPROVE ZERO VALUATION"
						SetErrMes();
						com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
						err.setFieldId("cicValnNo")
						result.addError(err)
						results.add(result)
						RollErrMes();
						return results
					}else {

						if(!QRY2.CIC_TYPE.trim().equals("WO")) {
							if(QRY8.AMT_TO_CONTRACTOR != QRY2.EST_COST || QRY8.VALUE_THIS_VALN != QRY2.EST_COST || QRY8.EXT_INV_AMT != QRY2.EST_COST) {
								StrErr = "AMOUNT CIC AND VALN MISMATCH!"
								SetErrMes();
								com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
								//err.setFieldId("cicNo")
								result.addError(err)
								results.add(result)
								RollErrMes();
								return results
							}

						}else {
							if(QRY8.AMT_TO_CONTRACTOR != QRY2.ACT_COST || QRY8.VALUE_THIS_VALN != QRY2.ACT_COST || QRY8.EXT_INV_AMT != QRY2.ACT_COST) {
								StrErr = "AMOUNT CIC AND VALN MISMATCH!"
								SetErrMes();
								com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
								//err.setFieldId("cicNo")
								result.addError(err)
								results.add(result)
								RollErrMes();
								return results
							}
						}

						String DelItemInActive = ("delete msf38d " +
								"where (CONTRACT_NO,VALN_NO,PORTION_NO,ELEMENT_NO,CATEGORY_NO) in ( " +
								"select CONTRACT_NO,VALN_NO,PORTION_NO,ELEMENT_NO,CATEGORY_NO from msf38c " +
								"where trim(CONTRACT_NO) = trim('"+CNT_NO+"') and trim(VALN_NO) = trim('" +QRY7.REF_CODE.trim()+ "') and ACT_VAL = 0 and ACT_PC_QTY = 0)");
						sql.execute(DelItemInActive);

						DelItemInActive = ("delete msf38c " +
								"where trim(CONTRACT_NO) = trim('"+CNT_NO+"') and trim(VALN_NO) = trim('" +QRY7.REF_CODE.trim()+ "') and ACT_VAL = 0 and ACT_PC_QTY = 0");
						sql.execute(DelItemInActive);

						VAL_MESS = "";
						if(!APPR_BY.equals(null) && !APPR_BY.equals("") ){
							VAL_MESS = APP_VAL(CNT_NO,QRY7.REF_CODE.trim(),APPR_BY,APPR_BY_POS,CIC_NO)
						}else {
							VAL_MESS = APP_VAL(CNT_NO,QRY7.REF_CODE.trim(),EMP_ID,securityToken.getRole(),CIC_NO)
						}

						if (!VAL_MESS.equals("")) {
							StrErr = VAL_MESS
							StrErr = StrErr.replace("VALUATION", "CIC")
							//SetErrMes();
							//com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
							//err.setFieldId("cicNo")
							//result.addError(err)
							result.addError(new UnlocalisedError(StrErr))
							results.add(result)
							RollErrMes();
							return results
						}
					}
				}else {
					StrErr = "VALUATION DOES NOT EXIST!"
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					err.setFieldId("cicValnNo")
					result.addError(err)
					results.add(result)
					RollErrMes();
					return results
				}

			}else {
				StrErr = "VALUATION DOES NOT EXIST!"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("cicValnNo")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
		}else {
			StrErr = "VALUATION DOES NOT EXIST!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("cicValnNo")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}

		def QRY3 = sql.firstRow("select * from ACA.KPF38F where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"'");
		if(!QRY3.equals(null)) {
			if (QRY3.CIC_STATUS.trim().equals("1")) {
				result.addAttribute("cicStat", "Estimated");
			}else if (QRY3.CIC_STATUS.trim().equals("2")) {
				result.addAttribute("cicStat", "Accepted");
			}else if (QRY3.CIC_STATUS.trim().equals("3")) {
				result.addAttribute("cicStat", "Cancel");
			}else if (QRY3.CIC_STATUS.trim().equals("4")) {
				result.addAttribute("cicStat", "Invoiced");
			}else if (QRY3.CIC_STATUS.trim().equals("U")) {
				result.addAttribute("cicStat", "Awaiting Approval");
			}else if (QRY3.CIC_STATUS.trim().equals("R")) {
				result.addAttribute("cicStat", "Rejected");
			}
		}
		results.add(result)
		return results
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
	private String APP_VAL(String CNT_NO,String VAL_NO,String EMP_ID,String POSITION,String CIC_NO){
		String MESSAGE = "";
		try
		{
			log.info ("APPROVE VALUATION:")
			log.info ("APP_BY_POSITION:" + POSITION)
			def date = new Date()
			def sdf = new SimpleDateFormat("yyyyMMdd")
			def stf = new SimpleDateFormat("HHmmss")
			String dateNow = sdf.format(date)

			ValuationsServiceModifyReplyDTO modRepDTO = service.get("Valuations").modify({
				it.contractNo = CNT_NO.trim()
				it.valuationNo = VAL_NO.trim()
				it.cntrctrRefRcptDate = date.toCalendar()
				it.cntrctrRefDate = date.toCalendar()
			}, false)

			ValuationsServiceApproveReplyDTO APPR_REP_DTO = service.get("Valuations").approve({
				it.contractNo = CNT_NO.trim()
				it.valuationNo = VAL_NO.trim()
				it.approvedBy = EMP_ID.trim()
				it.approvedByPosition = POSITION.trim()
			}, false)
			WarningMessageDTO[] WR_MESS;
			WR_MESS = APPR_REP_DTO.getWarningsAndInformation();
			log.info ("VAL Status:" + APPR_REP_DTO.getValnStatusDescription());
			log.info ("VAL App Pos:" + APPR_REP_DTO.getApprovedByPosition());
			Integer i = 0;
			//APPR_REP_DTO.getWarningsAndInformation().length
			for (i=0;i<WR_MESS.length;i++) {
				log.info ("VAL WARN:" + WR_MESS[i].getMessage());
			}
			//log.info ("VAL App Pos:" + APPR_REP_DTO.getWarningsAndInformation());

			def QRY8 = sql.firstRow("select trim(VALN_STATUS) VALN_STATUS from msf38b " +
					"where DSTRCT_CODE = '"+DST+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and trim(VALN_NO) = trim('" +VAL_NO+ "') ");
			if(!QRY8.equals(null)) {
				log.info ("FIN CIC VALN_STATUS  : " + QRY8.VALN_STATUS);
				if (QRY8.VALN_STATUS.equals("A")) {
					GetNowDateTime();
					String QueryUpdate = ("update ACA.KPF38F " +
							"set CIC_STATUS = '2', COMPL_BY = '"+EMP_ID+"', COMPL_DATE = '" +strCrDT+ "' " +
							"where upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"'");
					sql.execute(QueryUpdate);
				}
				if (QRY8.VALN_STATUS.equals("U")) {
					GetNowDateTime();
					String QueryUpdate = ("update ACA.KPF38F " +
							"set CIC_STATUS = 'U' " +
							"where upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"'");
					sql.execute(QueryUpdate);
				}
			}

		}catch (EnterpriseServiceOperationException e){
			log.info ("MASUK EXCEPTION APPR VAL:");
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
}