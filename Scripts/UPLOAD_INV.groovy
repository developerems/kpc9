/**
* @EMS Feb 2019
*
* 20190217 - a9ra5213 - Ricky Afriano - KPC UPGRADE
*            Initial Coding - Upload Invoice using MSO260 in ELL38I Screen 
**/
package KPC
import java.util.Date
import java.util.List

import javax.naming.InitialContext

import com.mincom.ellipse.app.security.SecurityToken
import com.mincom.ellipse.errors.exceptions.FatalException
import com.mincom.ellipse.script.plugin.GenericScriptExecuteForCollection
import com.mincom.ellipse.script.plugin.GenericScriptExecute
import com.mincom.ellipse.script.plugin.GenericScriptCreate
import com.mincom.ellipse.script.plugin.GenericScriptUpdate
import com.mincom.ellipse.script.plugin.GenericScriptDelete
import com.mincom.ellipse.script.plugin.GenericScriptPlugin
import com.mincom.ellipse.script.plugin.GenericScriptResult
import com.mincom.ellipse.script.plugin.GenericScriptResults
import com.mincom.ellipse.script.plugin.RequestAttributes
import com.mincom.ellipse.script.plugin.RestartAttributes
import com.mincom.ellipse.script.util.CommAreaScriptWrapper;

import groovy.sql.Sql

import com.mincom.eql.impl.*
import com.mincom.eql.*
import com.mincom.enterpriseservice.ellipse.*
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import com.mincom.enterpriseservice.screen.ScreenNameValueDTO
import com.mincom.ellipse.errors.Error
import com.mincom.ellipse.errors.CobolMessages
import com.mincom.ellipse.errors.UnlocalisedError
import com.mincom.ellipse.errors.UnlocalisedMessage
import com.mincom.ellipse.*
import com.mincom.ellipse.edoi.ejb.msf071.MSF071Key
import com.mincom.ellipse.edoi.ejb.msf071.MSF071Rec
import com.mincom.ellipse.ejra.mso.GenericMsoRecord
import com.mincom.ellipse.client.connection.*
import com.mincom.ellipse.ejra.mso.*;
import com.mincom.enterpriseservice.exception.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UPLOAD_INV extends GenericScriptPlugin implements GenericScriptExecute{
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	
	EllipseScreenService screenService = EllipseScreenServiceLocator.ellipseScreenService;
	ConnectionId msoCon = ConnectionHolder.connectionId;
	GenericMsoRecord screen = new GenericMsoRecord();
	Boolean LOOPFLAG = false;
	String ErrorMessage = ""
	
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
	String CONTRACT_NO = "";
	ArrayList VALN_NO = new ArrayList();
	BigDecimal ATAX_VAL = 0;
	String District = "";
	String INT_CMT = "";
	String EXT_CMT = "";
	
	public GenericScriptResults execute(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings)
	throws FatalException {
		log.info("Execute UPLOAD_INV : " + version )
		GenericScriptResults results = new GenericScriptResults();
		GenericScriptResult result = new GenericScriptResult();
		RequestAttributes reqAtt = requestAttributes[0];
		District = securityToken.getDistrict();
		String CNT_NO,INV_NO,CIC_NO,CONT_SUPP;
		INV_NO = reqAtt.getAttributeStringValue("detInvNo");
		CNT_NO = reqAtt.getAttributeStringValue("detCntNo");
		CONT_SUPP = reqAtt.getAttributeStringValue("contSupp");
		String EMP_ID = GetUserEmpID(securityToken.getUserId(), securityToken.getDistrict());
		//Cek Security
        def QRY0;
        QRY0 = sql.firstRow("select a.EMPLOYEE_ID,a.POSITION_ID, " +
			"case when b.AUTHTY_TYPE is null then 'FALSE' else 'TRUE' END IAPP, " +
			"case when c.AUTHTY_TYPE is null then 'FALSE' else 'TRUE' END INIT, " +
			"case when d.AUTHTY_TYPE is null then 'FALSE' else 'TRUE' END AUINTO, " +
			"e.GLOBAL_PROFILE, " +
			"case when substr(f.PROFILE,IDX_260,1) <> NILAI_GP_260 then 'FALSE' else 'TRUE' end SEC_260, " +
			"case when substr(f.PROFILE,IDX_261,1) <> '9' then 'FALSE' else 'TRUE' end SEC_261, " +
			"case when substr(f.PROFILE,IDX_904,1) <> '9' then 'FALSE' else 'TRUE' end SEC_904 " +
			"from msf878 a " +
			"left outer join ( " +
			"select * from MSF872 " +
			"where AUTHTY_TYPE = 'IAPP') b on (a.POSITION_ID = b.POSITION_ID) " +
			"left outer join ( " +
			"select * from MSF872 " +
			"where AUTHTY_TYPE = 'INIT') c on (a.POSITION_ID = c.POSITION_ID) " +
			"left outer join ( " +
			"select * from MSF872 " +
			"where AUTHTY_TYPE = 'INTO') d on (a.POSITION_ID = d.POSITION_ID) " +
			"left outer join msf870 e on (a.POSITION_ID = e.POSITION_ID) " +
			"left outer join msf020 f on (trim(e.GLOBAL_PROFILE) = trim(f.entity) and entry_type = 'G') " +
			"left outer join ( " +
			"select substr(trim(PROFILE),LENGTH(trim(PROFILE)),1) NILAI_GP_260,LENGTH(trim(PROFILE)) IDX_260 from msf020 " +
			"where ENTRY_TYPE = 'P' and entity = 'MSO260' " +
			") g on (1=1) " +
			"left outer join ( " +
			"select substr(trim(PROFILE),LENGTH(trim(PROFILE)),1) NILAI_GP_261,LENGTH(trim(PROFILE)) IDX_261 from msf020 " +
			"where ENTRY_TYPE = 'P' and entity = 'MSO261' " +
			") h on (1=1) " +
			"left outer join ( " +
			"select substr(trim(PROFILE),LENGTH(trim(PROFILE)),1) NILAI_GP_904,LENGTH(trim(PROFILE)) IDX_904 from msf020 " +
			"where ENTRY_TYPE = 'P' and entity = 'MSO904' " +
			") i on (1=1) " +
			"where a.EMPLOYEE_ID = '"+EMP_ID+"' and (a.POS_STOP_DATE = '00000000' or a.POS_STOP_DATE = ' ' or (case when a.POS_STOP_DATE not in ('00000000',' ') then to_date(a.POS_STOP_DATE,'YYYYMMDD') else null end >= sysdate) ) and trim(a.POSITION_ID) = trim('"+securityToken.getRole()+"')") ;
		if(!QRY0.equals(null)) {
			log.info("QRY0 : " + QRY0 );
			if(QRY0.IAPP.trim().equals("FALSE")) {
				StrErr = "YOU DO NOT HAVE AUTHORITY IAPP"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				//err.setFieldId("creCntNo")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
			if(QRY0.INIT.trim().equals("FALSE")) {
				StrErr = "YOU DO NOT HAVE AUTHORITY INIT"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				//err.setFieldId("creCntNo")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
			if(QRY0.AUINTO.trim().equals("FALSE")) {
				StrErr = "YOU DO NOT HAVE AUTHORITY INTO"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				//err.setFieldId("creCntNo")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
			if(QRY0.SEC_260.trim().equals("FALSE")) {
				StrErr = "YOUR SECURITY PROFILE VALUE FOR MSO260 SHOULD BE 1"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				//err.setFieldId("creCntNo")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
			if(QRY0.SEC_261.trim().equals("FALSE")) {
				StrErr = "YOUR SECURITY PROFILE VALUE FOR MSO261 SHOULD BE 9"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				//err.setFieldId("creCntNo")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
			if(QRY0.SEC_904.trim().equals("FALSE")) {
				StrErr = "YOUR SECURITY PROFILE VALUE FOR MSO904 SHOULD BE 9"
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
		
		
		String DET_INV_DATE = "";
		if(reqAtt.getAttributeDateValue("detInvDate").equals(null)) {
			StrErr = "INVOICE DATE REQUIRED!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("detInvDate")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		DateToString(reqAtt.getAttributeDateValue("detInvDate"));
		DET_INV_DATE = strCrDT;
		String INV_REC_DATE = "";
		if (!reqAtt.getAttributeDateValue("invRecDate").equals(null)) {
			DateToString(reqAtt.getAttributeDateValue("invRecDate"));
			INV_REC_DATE = strCrDT;
		}else {
			INV_REC_DATE = " ";
		}
		String BK_CODE = "";
		BK_CODE = reqAtt.getAttributeStringValue("bkCode");
		if(BK_CODE.equals(null)) {
			StrErr = "BANK BRANCH CODE REQUIRED!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("bkCode")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		String BK_ACCT_NO = "";
		BK_ACCT_NO = reqAtt.getAttributeStringValue("bkAcctNo");
		if(BK_ACCT_NO.equals(null)) {
			StrErr = "BANK ACCOUNT NO REQUIRED!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("bkAcctNo")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		BigDecimal INV_VAL = 0;
		BigDecimal CIC_INV_VAL = 0;
		BigDecimal BALANCE = 0;
		
		//Validate Contract No
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
		//Validate CIC INV No
		def QRY3;
		QRY3 = sql.firstRow("select * from aca.kpf38i where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CIC_INVOICE)) = upper(trim('"+INV_NO+"')) ");
		log.info ("FIND CIC INV  : ");
		if(QRY3.equals(null)) {
			StrErr = "INVOICE NUMBER DOES NOT EXIST"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("detInvNo")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		if(!QRY3.CIC_INV_ST.trim().equals("")) {
			StrErr = "CIC INVOICE STATUS MUST BE SPACE"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("detInvStat")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		INV_VAL = QRY3.INVOICE_VAL
		//Validate INV_VAL
		if(INV_VAL.equals(null) || INV_VAL == 0) {
			StrErr = "INVOICE VALUE REQUIRED!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("detInvVal")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		BALANCE = QRY3.CIC_BALANCE
		//Validate CIC_BAL
		if(BALANCE.equals(null) || BALANCE != 0) {
			StrErr = "CIC INVOICE NOT BALANCE!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("detBal")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		//Update Inv Date to make sure only valuation that will be invoiced should be appear in MSM260B
		def QRYZZ = sql.firstRow("select * from msf38b " +
				"where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and INV_DATE = ' '");
		log.info ("FIND MSF38B_1  : " + QRYZZ);
		if(!QRYZZ.equals(null)) {
			if(!QRYZZ.INV_DATE.equals("")) {
				String QueryUpdate = ("update MSF38B set INV_DATE = ? where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and INV_DATE = ' ' ");
				sql.execute(QueryUpdate,["00000000"]);
			}
		}
		String StrSQL = "select * from ACA.KPF38F " +
					"where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and upper(trim(CIC_INVOICE)) = upper(trim('"+INV_NO+"')) ";
		log.info ("StrSQL : " + StrSQL);
		VALN_NO.clear();
		ATAX_VAL = 0;
		sql.eachRow(StrSQL, {
			if(it.CIC_STATUS.equals("3")) {
				StrErr = "ONE OF CIC HAS BEEN CANCELED : " + it.CIC_NO
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("grd1CicNo")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
			CIC_NO = it.CIC_NO;
			def QRY6 = sql.firstRow("select * from msf071 " +
				"where ENTITY_TYPE = 'CIV' and ENTITY_VALUE = '"+securityToken.getDistrict()+CNT_NO+CIC_NO+"' and REF_NO = '001' and SEQ_NUM = '001'");
				//log.info ("FIND VALN_NO  : " + QRY6);
			if(!QRY6.equals(null)) {
				if (!QRY6.REF_CODE.trim().equals("")) {
					log.info ("VALN NO : " + QRY6.REF_CODE);
					VALN_NO.add(QRY6.REF_CODE.trim())
					/*
					def QRY7 = sql.firstRow("select sum(case when b.ATAX_RATE_9 is null or b.ATAX_RATE_9 = 0 then 0 else (a.ACT_VAL * b.ATAX_RATE_9 / 100) end) NEW_ATAX_VAL " +
						"from msf38c a " +
						"left outer join msf013 b on (trim(a.ATAX_CODE) = trim(b.ATAX_CODE)) " +
						"where upper(trim(a.CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and trim(a.VALN_NO) = trim('"+QRY6.REF_CODE.trim()+"')");
					//log.info ("FIND ATAX VAL  : " + QRY7);
					if(!QRY7.equals(null)) {
						if(!QRY7.NEW_ATAX_VAL.equals(null)) {
							ATAX_VAL = ATAX_VAL + QRY7.NEW_ATAX_VAL;
						}
					}
					*/
					def QRY7A = sql.firstRow("select * from msf38b " +
							"where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and trim(VALN_NO) = trim('"+QRY6.REF_CODE.trim()+"') and INV_DATE = '00000000'");
					//log.info ("FIND MSF38B  : " + QRY7A);
					if(!QRY7A.equals(null)) {
						if(!QRY7A.VALN_STATUS.equals("A")) {
							StrErr = "VALUATION FOR CIC "+CIC_NO+" NOT APPROVED!"
							SetErrMes();
							com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
							//err.setFieldId("detBal")
							result.addError(err)
							results.add(result)
							RollErrMes();
							return results
						}
						if(!QRY7A.INV_DATE.equals(null)) {
							String QueryUpdate = ("update MSF38B set INV_DATE = ? where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and trim(VALN_NO) = trim('"+QRY6.REF_CODE.trim()+"') and INV_DATE = '00000000' ");
							sql.execute(QueryUpdate,[" "]);
						}
					}
					def QRY7C = sql.firstRow("select * from msf38b " +
						"where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and trim(VALN_NO) = trim('"+QRY6.REF_CODE.trim()+"')");
					if(!QRY7C.equals(null)) {
						log.info ("EXT_INV_NO : " + QRY7C.EXT_INV_NO);
						if(!QRY7C.EXT_INV_NO.equals(null)) {
							if(!QRY7C.EXT_INV_NO.trim().equals("")) {
								//FIX For Case that valuation used to be invoiced and status loaded, when the invoice has been canceled, the valutaion still remain to be invoiced
								def QRY7B = sql.firstRow("select * from msf260 where trim(SUPPLIER_NO) = trim('"+QRY7C.SUPPLIER_NO+"') and trim(EXT_INV_NO) = trim('"+QRY7C.EXT_INV_NO+"') ");
								log.info ("CEK INVOICE : " + QRY7B);
								if(QRY7B.equals(null)) {
									log.info ("VALN ALREADY INVOICED: ");
									String QueryUpdate = ("update MSF38B set INV_DATE = ?,EXT_INV_NO = ? where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and trim(VALN_NO) = trim('"+QRY6.REF_CODE.trim()+"') ");
									log.info ("UPDATE EXT INV NO: " + QueryUpdate);
									sql.execute(QueryUpdate,[" "," "]);
								}
							}
						}
					}
					
					def QRY8 = sql.firstRow("select * from msf071 " +
						"where ENTITY_TYPE = 'ICT' and REF_NO = '001' and SEQ_NUM = '001' and ENTITY_VALUE = '"+securityToken.getDistrict()+INV_NO.trim()+CIC_NO.trim()+"'");
					if(QRY8.equals(null)) {
						StrErr = "ADD TAX / WH TAX REQUIRED!"
						SetErrMes();
						com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
						//err.setFieldId("detBal")
						result.addError(err)
						results.add(result)
						RollErrMes();
						return results
					}
				}else {
					StrErr = "VALUATION DOES NOT EXIST!"
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					//err.setFieldId("CIC_VALN_NO")
					result.addError(err)
					results.add(result)
					RollErrMes();
					return results
				}
			}else {
				StrErr = "VALUATION DOES NOT EXIST!"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				//err.setFieldId("CIC_VALN_NO")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
		})
		
		
		ErrorMessage = "";
		INIT();
		CONTRACT_NO1I = CNT_NO;
		log.info("CONTRACT_NO1I : " + CONTRACT_NO1I )
		INV_NO1I = INV_NO;
		log.info("INV_NO1I : " + INV_NO1I )
		ACCOUNTANT1I = EMP_ID;
		log.info("ACCOUNTANT1I : " + ACCOUNTANT1I )
		INV_AMT1I = INV_VAL;
		log.info("INV_AMT1I : " + INV_AMT1I )
		INV_DATE1I = DET_INV_DATE;
		log.info("INV_DATE1I : " + INV_DATE1I )
		INV_RCPT_DATE1I = INV_REC_DATE
		log.info("INV_RCPT_DATE1I : " + INV_RCPT_DATE1I )
		BRANCH_CODE1I = BK_CODE;
		log.info("BRANCH_CODE1I : " + BRANCH_CODE1I )
		BANK_ACCT_NO1I = BK_ACCT_NO;
		log.info("BANK_ACCT_NO1I : " + BANK_ACCT_NO1I )
		
		if (reqAtt.getAttributeStringValue("intComm2").equals(null)) {
			INT_CMT = "";
		}else {
			INT_CMT = reqAtt.getAttributeStringValue("intComm2");
		}
		
		if (reqAtt.getAttributeStringValue("extComm2").equals(null)) {
			EXT_CMT = "";
		}else {
			EXT_CMT = reqAtt.getAttributeStringValue("extComm2");
		}
		
		invoke_MSO260();
		if(!ErrorMessage.trim().equals("") && !ErrorMessage.trim().contains("Loaded")) {
			StrErr = ErrorMessage
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}else {
			invoke_MSO261();
			invoke_MSO261_A();
			//RECLASS TAX ACCOUNT CODE
			String StrSQL2 = "select a.EXT_INV_NO,b.inv_item_no,a.CONTRACT_NO,b.VALN_NO,substr(trim(c.ENTITY_VALUE),-8) CIC_NO,d.WORK_ORDER,d.CIC_TYPE " +
				"from msf260 a " +
				"left outer join msf26a b on (a.supplier_no = b.supplier_no and a.inv_no = b.inv_no) " +
				"left outer join msf071 c on (trim(c.ENTITY_TYPE) = 'CIV' and trim(c.ENTITY_VALUE) like trim(a.dstrct_code)||trim(a.CONTRACT_NO)||'%' and length(trim(replace(c.ENTITY_VALUE,trim(a.dstrct_code)||trim(a.CONTRACT_NO)))) = 8 and trim(ref_code) = trim(b.VALN_NO)) " +
				"left outer join ACA.KPF38F d on (a.CONTRACT_NO = d.CONTRACT_NO and substr(trim(c.ENTITY_VALUE),-8) = d.CIC_NO) " +
				"where a.supplier_no = '"+QRY1.SUPPLIER_NO+"' and a.EXT_INV_NO = '"+INV_NO+"' and a.DSTRCT_CODE = '"+securityToken.getDistrict()+"' ";
			log.info ("StrSQL2 : " + StrSQL2);
			sql.eachRow(StrSQL2, {
				if(it.CIC_TYPE.equals("LS")) {
					def QRY2B;
					QRY2B = sql.firstRow("select a.EXT_INV_NO,b.inv_item_no,a.CONTRACT_NO,b.VALN_NO,substr(trim(c.ENTITY_VALUE),-8) CIC_NO,d.WORK_ORDER, " +
						"replace(e.DSTRCT_ACCT_CODE,a.DSTRCT_CODE,'') WO_ACCT_CODE,d.CIC_TYPE,f.CIC_ITEM_NO, " +
						"g.GL_ACCOUNT,h.account_code OLD_ACCT,a.LOADED_DATE,h.ATAX_CODE, " +
						"h.DSTRCT_CODE,h.PROCESS_DATE||h.TRANSACTION_NO||h.USERNO||h.REC900_TYPE TRANS_ID " +
						"from msf260 a " +
						"left outer join msf26a b on (a.supplier_no = b.supplier_no and a.inv_no = b.inv_no) " +
						"left outer join msf071 c on (trim(c.ENTITY_TYPE) = 'CIV' and trim(c.ENTITY_VALUE) like trim(a.dstrct_code)||trim(a.CONTRACT_NO)||'%' and length(trim(replace(c.ENTITY_VALUE,trim(a.dstrct_code)||trim(a.CONTRACT_NO)))) = 8 and trim(ref_code) = trim(b.VALN_NO)) " +
						"left outer join ACA.KPF38F d on (a.CONTRACT_NO = d.CONTRACT_NO and substr(trim(c.ENTITY_VALUE),-8) = d.CIC_NO) " +
						"left outer join msf620 e on (a.DSTRCT_CODE = e.DSTRCT_CODE and d.WORK_ORDER = e.WORK_ORDER) " +
						"left outer join ACA.KPF38G f on (a.CONTRACT_NO = f.CONTRACT_NO and f.CIC_NO = d.CIC_NO) " +
						"left outer join msf38d g on (a.CONTRACT_NO = g.CONTRACT_NO and b.VALN_NO = g.VALN_NO and f.CIC_ITEM_NO = g.PORTION_NO||g.ELEMENT_NO||g.CATEGORY_NO) " +
						"left outer join msf900 h on (a.DSTRCT_CODE = h.DSTRCT_CODE and a.LOADED_DATE = h.process_date and a.EXT_INV_NO = h.EXT_INV_NO and " +
						"a.supplier_no = h.supplier_no and h.ATAX_CODE <> ' ' and b.INV_ITEM_NO = h.INV_ITEM_NO and h.TRAN_AMOUNT >= 0) " +
						"where a.supplier_no = '"+QRY1.SUPPLIER_NO+"' and a.EXT_INV_NO = '"+INV_NO+"' and a.DSTRCT_CODE = '"+securityToken.getDistrict()+"' and b.INV_ITEM_NO = '"+it.inv_item_no+"'");
					if(!QRY2B.equals(null)) {
						if(!QRY2B.TRANS_ID.equals(null)) {
							invoke_MSO904(securityToken.getDistrict(),QRY2B.TRANS_ID,QRY2B.GL_ACCOUNT,INV_NO,it.inv_item_no);
						}
					}
				}else {
					def QRY2A;
					QRY2A = sql.firstRow("select a.EXT_INV_NO,b.inv_item_no,a.CONTRACT_NO,b.VALN_NO,substr(trim(c.ENTITY_VALUE),-8) CIC_NO,d.WORK_ORDER, " +
						"replace(e.DSTRCT_ACCT_CODE,a.DSTRCT_CODE,'') WO_ACCT_CODE,d.CIC_TYPE,h.account_code OLD_ACCT, " +
						"h.DSTRCT_CODE,h.PROCESS_DATE||h.TRANSACTION_NO||h.USERNO||h.REC900_TYPE TRANS_ID " +
						"from msf260 a " +
						"left outer join msf26a b on (a.supplier_no = b.supplier_no and a.inv_no = b.inv_no) " +
						"left outer join msf071 c on (trim(c.ENTITY_TYPE) = 'CIV' and trim(c.ENTITY_VALUE) like trim(a.dstrct_code)||trim(a.CONTRACT_NO)||'%' and length(trim(replace(c.ENTITY_VALUE,trim(a.dstrct_code)||trim(a.CONTRACT_NO)))) = 8 and trim(ref_code) = trim(b.VALN_NO)) " +
						"left outer join ACA.KPF38F d on (a.CONTRACT_NO = d.CONTRACT_NO and substr(trim(c.ENTITY_VALUE),-8) = d.CIC_NO) " +
						"left outer join msf620 e on (a.DSTRCT_CODE = e.DSTRCT_CODE and d.WORK_ORDER = e.WORK_ORDER) " +
						"left outer join msf900 h on (a.DSTRCT_CODE = h.DSTRCT_CODE and a.LOADED_DATE = h.process_date and a.EXT_INV_NO = h.EXT_INV_NO and " + 
						"a.supplier_no = h.supplier_no and h.ATAX_CODE <> ' ' and b.INV_ITEM_NO = h.INV_ITEM_NO and h.TRAN_AMOUNT >= 0) " +
						"where a.supplier_no = '"+QRY1.SUPPLIER_NO+"' and a.EXT_INV_NO = '"+INV_NO+"' and a.DSTRCT_CODE = '"+securityToken.getDistrict()+"' and b.INV_ITEM_NO = '"+it.inv_item_no+"'");
					if(!QRY2A.equals(null)) {
						if(!QRY2A.TRANS_ID.equals(null)) {
							invoke_MSO904(securityToken.getDistrict(),QRY2A.TRANS_ID,QRY2A.WO_ACCT_CODE,INV_NO,it.inv_item_no);
						}
					}
				}
			})
			
			def QRY2;
			QRY2 = sql.firstRow("select * from msf260 where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(SUPPLIER_NO)) = upper(trim('"+QRY1.SUPPLIER_NO+"')) and upper(trim(EXT_INV_NO)) = upper(trim('"+INV_NO+"')) ");
			log.info ("FIND MSF260  : ");
			if(!QRY2.equals(null)) {
				result.addInformationalMessage(new UnlocalisedMessage(ErrorMessage));
				try
				{
					log.info ("INV_STAT: " + QRY2.APPR_STATUS);
					String CALC_STAT = "";
					if(QRY2.APPR_STATUS.equals("00") || QRY2.APPR_STATUS.equals("02") || QRY2.APPR_STATUS.equals("05") ||
						QRY2.APPR_STATUS.equals("06")) {
						CALC_STAT = "U";
					}else if(QRY2.APPR_STATUS.equals("60")) {
						CALC_STAT = "C";
					}else {
						CALC_STAT = "4";
					}
					log.info ("CALC_STAT: " + CALC_STAT);
					GetNowDateTime();
					String QueryUpdate = ("update ACA.KPF38i set CIC_INV_ST = ?,ACCEPT_DATE = ? where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and upper(trim(CIC_INVOICE)) = upper(trim('"+INV_NO+"')) ");
					sql.execute(QueryUpdate,[CALC_STAT,strCrDT]);
					
					QueryUpdate = ("update ACA.KPF38F set CIC_STATUS = ? where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and upper(trim(CIC_INVOICE)) = upper(trim('"+INV_NO+"')) ");
					sql.execute(QueryUpdate,["4"]);
				} catch (Exception  e) {
					log.info ("Exception is : " + e);
					StrErr = "EXCEPTION UPDATE ACA.KPF38I : ";
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541);
					result.addError(err);
					results.add(result);
					RollErrMes();
					return results
				}
			}else {
				StrErr = "FAILED TO LOAD INVOICE"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("detInvNo")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
		}
		
		results.add(result)
		return results
	}
	String CONTRACT_NO1I = "";
	String INV_NO1I = "";
	String ACCOUNTANT1I = "";
	String INV_AMT1I = "";
	String INV_DATE1I = "";
	String INV_RCPT_DATE1I = "";
	String BRANCH_CODE1I = "";
	String BANK_ACCT_NO1I = "";
	private String INIT(){
		CONTRACT_NO1I = "";
		INV_NO1I = "";
		ACCOUNTANT1I = "";
		INV_AMT1I = "";
		INV_DATE1I = "";
		BRANCH_CODE1I = "";
		BANK_ACCT_NO1I = "";
	}
	
	private String invoke_MSO261() {
		log.info("----------------------------------------------")
		log.info("how_to_invoke_service - screen service - Start")
		log.info("----------------------------------------------")

		screen = screenService.executeByName(msoCon, "MSO261");
		log.info("MSO ID : " + msoCon.getId())
		log.info("MSO SCREEN1 : " + screen.mapname.trim())
		MainMSO2();
		log.info("MSO SCREEN2 : " + screen.mapname.trim())
		def QRY2
		if ( screen.mapname.trim().equals(new String("MSM261A")) ) {
			screen.setFieldValue("OPTION1I", "1");
			screen.setFieldValue("DSTRCT_CODE1I", District);
			QRY2 = sql.firstRow("select * from msf384 where DSTRCT_CODE = '"+District+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CONTRACT_NO1I+"')) ");
			log.info ("FIND CONTRACT  : ");
			if(!QRY2.equals(null)) {
				screen.setFieldValue("SUPPLIER_NO1I", QRY2.SUPPLIER_NO.trim());
			}
			
			screen.setFieldValue("INV_NO1I", INV_NO1I);
			screen.nextAction = GenericMsoRecord.TRANSMIT_KEY;
			screen = screenService.execute(msoCon, screen);
			
			if (isError(screen) ) {
				log.info("Error Message:" + screen.getErrorString())
				ErrorMessage = screen.getErrorString();
				return ErrorMessage
			}
		}
		
		if ( screen.mapname.trim().equals(new String("MSM261B")) ) {
			screen.setFieldValue("INV_NO2I", INV_NO1I);
			screen.nextAction = GenericMsoRecord.TRANSMIT_KEY;
			screen = screenService.execute(msoCon, screen);
			
			if (isError(screen) ) {
				log.info("Error Message:" + screen.getErrorString())
				ErrorMessage = screen.getErrorString();
				return ErrorMessage
			}
		}
		
		String MaxItem;
		def QRY2C = sql.firstRow("select NO_OF_ITEMS from msf260 where DSTRCT_CODE = '"+District+"' and SUPPLIER_NO = '"+QRY2.SUPPLIER_NO.trim()+"' and EXT_INV_NO = '"+INV_NO1I+"'");
		if(!QRY2C.equals(null)) {
			MaxItem = QRY2C.NO_OF_ITEMS;
		}
		
		String StrFKEYS = screen.getFunctionKeyLine().getValue();
		String StrSQL = "select * from msf26a " +
			"where (DSTRCT_CODE,SUPPLIER_NO,INV_NO) in ( " +
			"select DSTRCT_CODE,SUPPLIER_NO,INV_NO from msf260 " +
			"where DSTRCT_CODE = '"+District+"' and SUPPLIER_NO = '"+QRY2.SUPPLIER_NO.trim()+"' and EXT_INV_NO = '"+INV_NO1I+"') " +
			"order by INV_ITEM_NO";
		log.info ("StrSQL : " + StrSQL);
		String ADD_TAX;
		String WH_TAX;
		Integer Ctr = 0
		sql.eachRow(StrSQL, {
			Ctr = Ctr + 1;
			ADD_TAX = "";
			WH_TAX = "";
			String ValNo = screen.getField("PORT_ITEM3I").value
			String CIC_NO = "";
			log.info ("ValNo : " + ValNo);
			def QRY2B = sql.firstRow("select * from msf071 where ENTITY_TYPE = 'CIV' and ENTITY_VALUE like '"+District+CONTRACT_NO1I+"%' and length(trim(replace(ENTITY_VALUE,'"+District+CONTRACT_NO1I.trim()+"'))) = 8 and REF_CODE = '"+ValNo+"'");
			if(!QRY2B.equals(null)) {
				CIC_NO = right(QRY2B.ENTITY_VALUE.trim(), 8);
			}
			
			def QRY2A = sql.firstRow("select * from msf071 where ENTITY_TYPE = 'ICT' and ENTITY_VALUE = '"+District+INV_NO1I+CIC_NO+"' and REF_NO = '001' and SEQ_NUM = '001' ");
			if(!QRY2A.equals(null)) {
				ADD_TAX = QRY2A.REF_CODE.substring(0,4);
				WH_TAX = QRY2A.REF_CODE.substring(4,6);
			}
			log.info ("ADD_TAX : " + ADD_TAX);
			log.info ("WH_TAX : " + WH_TAX);
			if ( screen.mapname.trim().equals(new String("MSM261C")) ) {
				screen.setFieldValue("PRESC_PMT_IND3I", WH_TAX);
				screen.setFieldValue("ATAX_CODE3I", ADD_TAX);
				screen.nextAction = GenericMsoRecord.TRANSMIT_KEY;
				screen = screenService.execute(msoCon, screen);
				
				if (isError(screen) ) {
					log.info("Error Message:" + screen.getErrorString())
					ErrorMessage = screen.getErrorString();
					return ErrorMessage
				}
				if ( screen.mapname.trim().equals(new String("MSM261C")) ) {
					StrFKEYS = screen.getFunctionKeyLine().getValue()
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
				log.info("MSO SCREEN4 : " + screen.mapname.trim())
				if ( screen.mapname.trim().equals(new String("MSM26JA")) ) {
					screen.nextAction = GenericMsoRecord.TRANSMIT_KEY;
					screen = screenService.execute(msoCon, screen);
					
					if (isError(screen) ) {
						log.info("Error Message:" + screen.getErrorString())
						ErrorMessage = screen.getErrorString();
						return ErrorMessage
					}
				}
				log.info("MSO SCREEN4A : " + screen.mapname.trim())
				if ( screen.mapname.trim().equals(new String("MSM261C")) ) {
					StrFKEYS = screen.getFunctionKeyLine().getValue()
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
				log.info("Error Message:" + screen.getErrorMessage().getErrorString().toString())
				if(screen.getErrorMessage().getErrorString().toString().trim().contains("INVOICE ITEM MODIFIED")) {
					ErrorMessage = screen.getErrorMessage().getErrorString().toString().trim();
					screen.nextAction = GenericMsoRecord.TRANSMIT_KEY;
					screen = screenService.execute(msoCon, screen);
										
					if (isError(screen) ) {
						log.info("Error Message:" + screen.getErrorString())
						ErrorMessage = screen.getErrorString();
						return ErrorMessage
					}
				}
			}
		})
		
		log.info("MSO SCREEN5 : " + screen.mapname.trim())
		log.info("StrFKEYS : " + StrFKEYS)
		
		log.info ("-----------------------------");
		return ErrorMessage
	}
	private String invoke_MSO261_A() {
		log.info("----------------------------------------------")
		log.info("how_to_invoke_service - screen service - Start")
		log.info("----------------------------------------------")

		screen = screenService.executeByName(msoCon, "MSO261");
		log.info("MSO ID : " + msoCon.getId())
		log.info("MSO SCREEN1 : " + screen.mapname.trim())
		MainMSO2();
		log.info("MSO SCREEN2 : " + screen.mapname.trim())
		def QRY2
		if ( screen.mapname.trim().equals(new String("MSM261A")) ) {
			screen.setFieldValue("OPTION1I", "1");
			screen.setFieldValue("DSTRCT_CODE1I", District);
			QRY2 = sql.firstRow("select * from msf384 where DSTRCT_CODE = '"+District+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CONTRACT_NO1I+"')) ");
			log.info ("FIND CONTRACT  : ");
			if(!QRY2.equals(null)) {
				screen.setFieldValue("SUPPLIER_NO1I", QRY2.SUPPLIER_NO.trim());
			}
			
			screen.setFieldValue("INV_NO1I", INV_NO1I);
			screen.nextAction = GenericMsoRecord.TRANSMIT_KEY;
			screen = screenService.execute(msoCon, screen);
			
			if (isError(screen) ) {
				log.info("Error Message:" + screen.getErrorString())
				ErrorMessage = screen.getErrorString();
				return ErrorMessage
			}
		}
		
		if ( screen.mapname.trim().equals(new String("MSM261B")) ) {
			screen.setFieldValue("INV_NO2I", INV_NO1I);
			screen.nextAction = GenericMsoRecord.F8_KEY;
			screen = screenService.execute(msoCon, screen);
			
			if (isError(screen) && !screen.getErrorMessage().getErrorString().toString().trim().contains("MODIFICATIONS MADE TO INVOICE")) {
				log.info("Error Message:" + screen.getErrorString())
				ErrorMessage = screen.getErrorString();
				return ErrorMessage
			}
		}
		
		if(screen.getErrorMessage().getErrorString().toString().trim().contains("MODIFICATIONS MADE TO INVOICE")) {
			ErrorMessage = "INVOICE CREATED";
		}
		log.info("MSO SCREEN3 : " + screen.mapname.trim());
		String StrFKEYS = screen.getFunctionKeyLine().getValue();
		log.info("StrFKEYS : " + StrFKEYS)
		
		log.info ("-----------------------------");
		return ErrorMessage
	}
	private String invoke_MSO904(String DSTRCT,String TRANS_ID,String NEW_ACCT,String INV_NO,String INV_ITEM_NO) {
		log.info("----------------------------------------------")
		log.info("how_to_invoke_service - screen service MSO904 - Start")
		log.info("----------------------------------------------")

		screen = screenService.executeByName(msoCon, "MSO904");
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
			screen.setFieldValue("ACCOUNT21I", NEW_ACCT);
			screen.setFieldValue("REASON1I", "INV:" + INV_NO + " ITM:"+INV_ITEM_NO);
			
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
	public static String right(String value, int length) {
		// To get right characters from a string, change the begin index.
		return value.substring(value.length() - length);
	}
	private String invoke_MSO260(){
		
		log.info("----------------------------------------------")
		log.info("how_to_invoke_service - screen service - Start")
		log.info("----------------------------------------------")
		
		screen = screenService.executeByName(msoCon, "MSO260");
		log.info("MSO ID : " + msoCon.getId())
		log.info("MSO SCREEN1 : " + screen.mapname.trim())
		MainMSO();
		log.info("MSO SCREEN2 : " + screen.mapname.trim())
		if ( screen.mapname.trim().equals(new String("MSM260A")) ) {
			screen.setFieldValue("CONTRACT_NO1I", CONTRACT_NO1I);
			screen.setFieldValue("INV_NO1I", INV_NO1I);
			screen.setFieldValue("ACCOUNTANT1I", ACCOUNTANT1I);
			//screen.setFieldValue("INV_AMT1I", INV_AMT1I + ATAX_VAL);
			screen.setFieldValue("INV_AMT1I", INV_AMT1I);
			screen.setFieldValue("INV_DATE1I", INV_DATE1I);
			screen.setFieldValue("INV_RCPT_DATE1I", INV_RCPT_DATE1I);
			screen.setFieldValue("BRANCH_CODE1I", BRANCH_CODE1I);
			screen.setFieldValue("BANK_ACCT_NO1I", BANK_ACCT_NO1I);
			screen.setFieldValue("INV_COMM_TYPE1I", "B");
			//if (ATAX_VAL != 0) {
			//	screen.setFieldValue("ADD_TAX_AMOUNT1I", ATAX_VAL.toString().trim());
			//}
				
			screen.nextAction = GenericMsoRecord.TRANSMIT_KEY;
			screen = screenService.execute(msoCon, screen);
			
			if (isError(screen) ) {
				log.info("Error Message:" + screen.getErrorString())
				ErrorMessage = screen.getErrorString();
				return ErrorMessage
			}
			if (isWarning(screen) ) {
				log.info("Warning Message:" + screen.getErrorString())
				screen.nextAction = GenericMsoRecord.TRANSMIT_KEY;
				screen = screenService.execute(msoCon, screen);
			}
			if (isError(screen) ) {
				log.info("Error Message:" + screen.getErrorString())
				ErrorMessage = screen.getErrorString();
				return ErrorMessage
			}
			if (isWarning(screen) ) {
				log.info("Warning Message:" + screen.getErrorString())
				screen.nextAction = GenericMsoRecord.TRANSMIT_KEY;
				screen = screenService.execute(msoCon, screen);
			}
		}
		
		log.info("MSO SCREEN3 : " + screen.mapname.trim())
		if ( screen.mapname.trim().equals(new String("MSM260B")) ) {
			String FieldVal = "";
			Collections.sort(VALN_NO);
			log.info("VALN_NO.size() : " + VALN_NO.size())
			for (int i=0; i<VALN_NO.size(); i++){
				log.info("VALN_NO : " + VALN_NO.get(i).toString().trim())
				for(Integer j=1;j<=10;j++) {
					if (! screen.mapname.trim().equals(new String("MSM260B")) ) {
						log.info("MSO SCREEN4 : " + screen.mapname.trim())
						break;
					}
					FieldVal = "";
					FieldVal = screen.getField("RECEIPT_REF2I" + j.toString().trim()).value
					//log.info("FieldVal : " + FieldVal)
					if(FieldVal.contains(VALN_NO.get(i).toString().trim())) {
						screen.setFieldValue("ACTN2I" + j.toString().trim(), "M");
						log.info("ACTION:" + screen.getField("ACTN2I" + j.toString().trim()).value);
						if (i != VALN_NO.size() - 1){
							i = i + 1
						}
					}
					
					if(j==10) {
						screen.nextAction = GenericMsoRecord.TRANSMIT_KEY;
						screen = screenService.execute(msoCon, screen);
						log.info("SCREEN MESSAGE : " + screen.getErrorMessage().getErrorString().toString().trim())
						if(screen.getErrorMessage().getErrorString().toString().trim().contains("Loaded")) {
							ErrorMessage = screen.getErrorMessage().getErrorString().toString().trim()
						}
						if (isError(screen) ) {
							log.info("Error Message:" + screen.getErrorString())
							ErrorMessage = screen.getErrorString();
							return ErrorMessage
						}
						if (isWarning(screen) ) {
							log.info("Warning Message:" + screen.getErrorString())
							screen.nextAction = GenericMsoRecord.TRANSMIT_KEY;
							screen = screenService.execute(msoCon, screen);
						}
						j = 0
					}
				}
			}
			
			log.info("MSO SCREEN3_A : " + screen.mapname.trim())
			if (screen.mapname.trim().equals(new String("MSM096B"))) {
				log.info("TEXT_CODE1 : " + screen.getField("STD_TEXT_CODE2I").value)
				if(screen.getField("STD_TEXT_CODE2I").value.trim().equals("IX")) {
					//List<String> res1 = new ArrayList<String>();
					//res1 = splitString(INT_CMT,60);
					
					//String keterangan = INT_CMT.replaceAll("(.{61})", "\$1\r");
					String keterangan = EXT_CMT
					log.info("keterangan : " + keterangan)
					//String[] strKeteranganUpload = new String[res1.size()];
					//res1.toArray(strKeteranganUpload);
					String[] strKeteranganUpload = keterangan.split("\\r?\\n");
					int counter = 0;
					int data = strKeteranganUpload.length > 22 ? 22 : strKeteranganUpload.length;
					int maxLoop = 10;
					int cLoop = 0;
					int i = data > maxLoop ? maxLoop : data;
					
					int n = 1;
					Integer idx = 1;
					log.info("strKeteranganUpload.length : " + strKeteranganUpload.length)
					while (counter < strKeteranganUpload.length && counter < 22)
					{
						int x = idx - 1;
						log.info("strKeteranganUpload[counter] : " + strKeteranganUpload[counter])
						screen.setFieldValue("STD_TEXT_C2I"+ idx.toString().trim(), strKeteranganUpload[counter]);
						counter++;
						n++;
						String ScreenName = screen.mapname.trim().trim();
						log.info("counter : " + counter)
						log.info("data : " + data)
						log.info("ScreenName : " + ScreenName)
	
						if ((counter == data || counter % 10 == 0) && ScreenName.equals("MSM096B"))
						{
							screen.nextAction = GenericMsoRecord.TRANSMIT_KEY;
							screen = screenService.execute(msoCon, screen);
							
							if (isError(screen) ) {
								log.info("Error Message:" + screen.getErrorString())
								ErrorMessage = screen.getErrorString();
								return ErrorMessage
							}
							
							//i = (strKeteranganUpload.Length - ((maxLoop * loop) + cLoop)) > maxLoop ? maxLoop : (strKeteranganUpload.Length - ((maxLoop * loop) + cLoop));
							i = (data - (maxLoop + cLoop)) > maxLoop ? maxLoop : (data - (maxLoop + cLoop));
							cLoop = cLoop + i;
							
							idx = 1;
						}
						else
						{
							idx++;
						}
					}
				}
				
				if (screen.mapname.trim().equals(new String("MSM096B"))) {
					screen.setFieldValue("CODENAME2I", "");
					screen.nextAction = GenericMsoRecord.F9_KEY;
					screen = screenService.execute(msoCon, screen);
					
					if (isError(screen) ) {
						log.info("Error Message:" + screen.getErrorString())
						ErrorMessage = screen.getErrorString();
						return ErrorMessage
					}
					if(screen.getField("STD_TEXT_CODE2I").value.trim().equals("IX")) {
						screen.setFieldValue("CODENAME2I", "");
						screen.nextAction = GenericMsoRecord.F9_KEY;
						screen = screenService.execute(msoCon, screen);
						
						if (isError(screen) ) {
							log.info("Error Message:" + screen.getErrorString())
							ErrorMessage = screen.getErrorString();
							return ErrorMessage
						}
					}
				}
				
				log.info("TEXT_CODE2 : " + screen.getField("STD_TEXT_CODE2I").value)
				if(screen.getField("STD_TEXT_CODE2I").value.trim().equals("II")) {
					String keterangan = INT_CMT
					log.info("keterangan : " + keterangan)
					//String[] strKeteranganUpload = new String[res1.size()];
					//res1.toArray(strKeteranganUpload);
					String[] strKeteranganUpload = keterangan.split("\\r?\\n");
					int counter = 0;
					int data = strKeteranganUpload.length > 22 ? 22 : strKeteranganUpload.length;
					int maxLoop = 10;
					int cLoop = 0;
					int i = data > maxLoop ? maxLoop : data;
					
					int n = 1;
					Integer idx = 1;
					log.info("strKeteranganUpload.length : " + strKeteranganUpload.length)
					while (counter < strKeteranganUpload.length && counter < 22)
					{
						int x = idx - 1;
						log.info("strKeteranganUpload[counter] : " + strKeteranganUpload[counter])
						screen.setFieldValue("STD_TEXT_C2I"+ idx.toString().trim(), strKeteranganUpload[counter]);
						counter++;
						n++;
						String ScreenName = screen.mapname.trim().trim();
						log.info("counter : " + counter)
						log.info("data : " + data)
						log.info("ScreenName : " + ScreenName)
	
						if ((counter == data || counter % 10 == 0) && ScreenName.equals("MSM096B"))
						{
							screen.nextAction = GenericMsoRecord.TRANSMIT_KEY;
							screen = screenService.execute(msoCon, screen);
							
							if (isError(screen) ) {
								log.info("Error Message:" + screen.getErrorString())
								ErrorMessage = screen.getErrorString();
								return ErrorMessage
							}
							
							//i = (strKeteranganUpload.Length - ((maxLoop * loop) + cLoop)) > maxLoop ? maxLoop : (strKeteranganUpload.Length - ((maxLoop * loop) + cLoop));
							i = (data - (maxLoop + cLoop)) > maxLoop ? maxLoop : (data - (maxLoop + cLoop));
							cLoop = cLoop + i;
							
							idx = 1;
						}
						else
						{
							idx++;
						}
					}
				}
				
				if (screen.mapname.trim().equals(new String("MSM096B"))) {
					screen.setFieldValue("CODENAME2I", "");
					screen.nextAction = GenericMsoRecord.F9_KEY;
					screen = screenService.execute(msoCon, screen);
					
					if (isError(screen) ) {
						log.info("Error Message:" + screen.getErrorString())
						ErrorMessage = screen.getErrorString();
						return ErrorMessage
					}
				}
				if (screen.mapname.trim().equals(new String("MSM096B"))) {
					screen.setFieldValue("CODENAME2I", "");
					screen.nextAction = GenericMsoRecord.F9_KEY;
					screen = screenService.execute(msoCon, screen);
					
					if (isError(screen) ) {
						log.info("Error Message:" + screen.getErrorString())
						ErrorMessage = screen.getErrorString();
						return ErrorMessage
					}
				}
			}
			
		}
		log.info("MSO SCREEN5 : " + screen.mapname.trim())
		String StrFKEYS = screen.getFunctionKeyLine().getValue()
		log.info("StrFKEYS : " + StrFKEYS)
		
		log.info ("-----------------------------");
		return ErrorMessage
	}
	
	public static List<String> splitString(String msg, int lineSize) {
		List<String> res = new ArrayList<String>();

		Pattern p = Pattern.compile("\\b.{1," + (lineSize-1) + "}\\b\\W?");
		Matcher m = p.matcher(msg);

		while(m.find()) {
				System.out.println(m.group().trim());   // Debug
				res.add(m.group());
		}
		return res;
	}

	
	private def MainMSO(){
		Integer i = 0;
		while(LOOPFLAG.equals(false)) {
			i = i + 1;
			log.info("MAIN MSO1 :" + screen.mapname.trim())
			screen.setNextAction(GenericMsoRecord.F3_KEY)
			//screen.nextAction = GenericMsoRecord.F3_KEY;
			screen = screenService.execute(msoCon, screen);
			log.info("MAIN MSO2 :" + screen.mapname.trim())
			if ( screen.mapname.trim().equals(new String("MSM260A")) ) {
				LOOPFLAG = true
			}
			if (i == 20) {
				LOOPFLAG = false
			}
		}
		LOOPFLAG = false
		
	}
	private def MainMSO2(){
		Integer i = 0;
		while(LOOPFLAG.equals(false)) {
			i = i + 1;
			log.info("MAIN MSO1 :" + screen.mapname.trim())
			screen.setNextAction(GenericMsoRecord.F3_KEY)
			//screen.nextAction = GenericMsoRecord.F3_KEY;
			screen = screenService.execute(msoCon, screen);
			log.info("MAIN MSO2 :" + screen.mapname.trim())
			if ( screen.mapname.trim().equals(new String("MSM261A")) ) {
				LOOPFLAG = true
			}
			if (i == 20) {
				LOOPFLAG = false
			}
		}
		LOOPFLAG = false
		
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
	private boolean isError(GenericMsoRecord screen) {
		
		return ((char)screen.errorType) == MsoErrorMessage.ERR_TYPE_ERROR ;
	}
	private boolean isWarning(GenericMsoRecord screen) {
		
		((char)screen.errorType) == MsoErrorMessage.ERR_TYPE_WARNING ;
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
	public def DateToString(Date ParDate) {
		Date InPer = ParDate;
		log.info("Hasil InPer : " + InPer.toString())
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(InPer);
		int iyear = cal.get(Calendar.YEAR);
		int imonth = cal.get(Calendar.MONTH);
		int iday = cal.get(Calendar.DAY_OF_MONTH);
		int iHH = cal.get(Calendar.HOUR);
		int iMM = cal.get(Calendar.MINUTE);
		int iSS = cal.get(Calendar.SECOND);
		
		if (iday.toString().trim().length() < 2){
			StrDT = "0" + iday.toString().trim()
		}else{
			StrDT = iday.toString().trim()
		}
		//log.info("StrDT : " + StrDT )
		
		"(imonth + 1) untuk membuat bulan sesuai"
		if ((imonth + 1).toString().trim().length() < 2){
			StrMT = "0" + (imonth + 1).toString().trim()
		}else{
			StrMT = (imonth + 1).toString().trim()
		}
		//log.info("StrMT : " + StrMT )
		if (iyear.toString().trim().length() < 3){
			StrYR = "20" + iyear.toString().trim()
		}else{
			StrYR = iyear.toString().trim()
		}
		//log.info("StrYR : " + StrYR )
		strCrDT = StrYR + StrMT + StrDT
		//log.info("strCrDT : " + strCrDT )
		
		if (iHH.toString().trim().length() < 2){
			StrHH = "0" + iHH.toString().trim()
		}else{
			StrHH = iHH.toString().trim()
		}
		//log.info("StrHH : " + StrHH )
		
		if (iMM.toString().trim().length() < 2){
			StrMM = "0" + iMM.toString().trim()
		}else{
			StrMM = iMM.toString().trim()
		}
		//log.info("StrMM : " + StrMM )
		
		if (iSS.toString().trim().length() < 2){
			StrSS = "0" + iSS.toString().trim()
		}else{
			StrSS = iSS.toString().trim()
		}
		//log.info("StrSS : " + StrSS )
		
		strCrTM = StrHH + StrMM + StrSS
		//log.info("strCrTM : " + strCrTM )
	}
}