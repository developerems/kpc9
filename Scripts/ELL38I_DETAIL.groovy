/**
 * @EMS Feb 2019
 *
 * 20190201 - a9ra5213 - Ricky Afriano - KPC UPGRADE Ellipse 8
 *            Initial Coding - Find Detail for CIC Invoice in ELL38I Detail Screen 
 **/
package KPC

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
import com.mincom.ellipse.errors.Error;
import com.mincom.ellipse.errors.UnlocalisedError
import com.mincom.ellipse.errors.CobolMessages;
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsService
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsServiceCreateReplyDTO
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsServiceCreateRequestDTO
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsServiceDeleteReplyDTO
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextService
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextServiceCreateReplyDTO
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextServiceSetTextReplyDTO
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextServiceDeleteReplyDTO
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextServiceAppendReplyDTO


public class ELL38I_DETAIL extends GenericScriptPlugin implements GenericScriptExecute, GenericScriptUpdate, GenericScriptCreate, GenericScriptDelete{
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
		log.info("Execute ELL38I_DETAIL Execute : " + version );
		GenericScriptResults results = new GenericScriptResults();
		GenericScriptResult result = new GenericScriptResult();
		RequestAttributes reqAtt = requestAttributes[0];
		
		String CNT_NO = "";
		if (!reqAtt.getAttributeStringValue("diaCntNo").equals(null)) {
			CNT_NO = reqAtt.getAttributeStringValue("diaCntNo");
		}
		else if (reqAtt.getAttributeStringValue("grdCntNo").equals(null)) {
			CNT_NO = reqAtt.getAttributeStringValue("detCntNo");
		}else {
			CNT_NO = reqAtt.getAttributeStringValue("grdCntNo");
		}
		
		String INV_NO = "";
		if (!reqAtt.getAttributeStringValue("diaInvNo").equals(null)) {
			INV_NO = reqAtt.getAttributeStringValue("diaInvNo");
		}
		else if (reqAtt.getAttributeStringValue("grdInvNo").equals(null)) {
			INV_NO = reqAtt.getAttributeStringValue("detInvNo");
		}else {
			INV_NO = reqAtt.getAttributeStringValue("grdInvNo");
		}
		
		String PAR_CIC_FROM = "";
		if (reqAtt.getAttributeStringValue("parCicFrom").equals(null)) {
			PAR_CIC_FROM = "";
		}else {
			PAR_CIC_FROM = reqAtt.getAttributeStringValue("parCicFrom");
		}
		
		String PAR_CIC_TO = "";
		if (reqAtt.getAttributeStringValue("parCicTo").equals(null)) {
			PAR_CIC_TO = "";
		}else {
			PAR_CIC_TO = reqAtt.getAttributeStringValue("parCicTo");
		}
		
		log.info("CNT_NO : " + CNT_NO );
		log.info("INV_NO : " + INV_NO );
		String CNT_NO2 = "";
		String contractDesc
		DecimalFormat df = new DecimalFormat("#,##0.00;-#,##0.00");
		def QRY1;
		
		QRY1 = sql.firstRow("select * from aca.kpf38i where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and " +
			"upper(trim(CIC_INVOICE)) = upper(trim('"+INV_NO+"')) and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) ");
		
		if(!QRY1.equals(null)) {
			CNT_NO2 = QRY1.CONTRACT_NO
			result.addAttribute("detInvVal", QRY1.INVOICE_VAL);
			result.addAttribute("detAccDt", QRY1.ACCEPT_DATE);
			result.addAttribute("detTotCicVal", df.format(QRY1.TOTAL_CIC_VAL));
			result.addAttribute("detInpBy", QRY1.INPUT_BY);
			result.addAttribute("detBal", df.format(QRY1.CIC_BALANCE));
			result.addAttribute("detInvStat", QRY1.CIC_INV_ST);
			
			String MODE = "";
			if(QRY1.CIC_INV_ST.equals(" ")) {
				result.addAttribute("mode", "ENABLE");
			}else {
				result.addAttribute("mode", "DISABLE");
			}
			
			def QRY2 = sql.firstRow("select * from msf071 where ENTITY_TYPE = 'IBC' and upper(trim(ENTITY_VALUE)) = upper(trim('"+INV_NO+"')) ");
			if(!QRY2.equals(null)) {
				result.addAttribute("bkCode", QRY2.REF_CODE.trim());
			}
			QRY2 = sql.firstRow("select * from msf071 where ENTITY_TYPE = 'IBA' and upper(trim(ENTITY_VALUE)) = upper(trim('"+INV_NO+"')) ");
			if(!QRY2.equals(null)) {
				result.addAttribute("bkAcctNo", QRY2.REF_CODE.trim());
			}
			QRY2 = sql.firstRow("select * from msf071 where ENTITY_TYPE = 'IDT' and upper(trim(ENTITY_VALUE)) = upper(trim('"+INV_NO+"')) ");
			if(!QRY2.equals(null)) {
				result.addAttribute("detInvDate", QRY2.REF_CODE.trim());
			}
			
			QRY2 = sql.firstRow("select * from msf071 where ENTITY_TYPE = 'IDR' and upper(trim(ENTITY_VALUE)) = upper(trim('"+INV_NO+"')) ");
			if(!QRY2.equals(null)) {
				result.addAttribute("invRecDate", QRY2.REF_CODE.trim());
			}
			
			QRY2 = sql.firstRow("select * from msf384 where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) ");
			log.info ("FIND CONTRACT  : ");
			if(!QRY2.equals(null)) {
				result.addAttribute("contSupp", QRY2.SUPPLIER_NO.trim());
				result.addAttribute("contractDesc", QRY2.CONTRACT_DESC.trim())
				def QRY3 = sql.firstRow("select * from msf200 where upper(trim(SUPPLIER_NO)) = upper(trim('"+QRY2.SUPPLIER_NO.trim()+"')) ");
				log.info ("FIND Supplier  : ");
				if(!QRY3.equals(null)) {
					result.addAttribute("pmtAddr1", QRY3.PAYMENT_ADDR_1.trim());
					result.addAttribute("pmtAddr2", QRY3.PAYMENT_ADDR_2.trim());
					result.addAttribute("pmtAddr3", QRY3.PAYMENT_ADDR_3.trim());
				}
			}
			
			//Show Internal Command
			QRY1 = sql.firstRow("select STD_KEY,trim(replace(listagg(VAL, '\n' ON OVERFLOW TRUNCATE) within group (order by STD_KEY,STD_LINE_NO,source),'.HEADING')) as merge_VAL " +
				"from ( " +
				"select STD_KEY,STD_LINE_NO, source, trim(val) val " +
				"from " +
				"  MSF096_STD_STATIC UNPIVOT INCLUDE NULLS " +
				"    ( VAL FOR( SOURCE ) IN " +
				"        ( STD_STATIC_1 AS 'STD_STATIC_1', " +
				"          STD_STATIC_2 AS 'STD_STATIC_2', " +
				"          STD_STATIC_3 AS 'STD_STATIC_3', " +
				"          STD_STATIC_4 AS 'STD_STATIC_4', " +
				"          STD_STATIC_5 AS 'STD_STATIC_5' " +
				"        ) " +
				"    ) " +
				"where STD_TEXT_CODE = 'GT' and trim(val) is not null and trim(std_key) = trim('"+"IX" + INV_NO.trim()+"') " +
				"order by STD_KEY,STD_LINE_NO, source) " +
				"group by STD_KEY");
			if(!QRY1.equals(null)) {
				result.addAttribute("intComm", QRY1.merge_VAL.trim());
			}
			//Show External Command
			QRY1 = sql.firstRow("select STD_KEY,trim(replace(listagg(VAL, '\n' ON OVERFLOW TRUNCATE) within group (order by STD_KEY,STD_LINE_NO,source),'.HEADING')) as merge_VAL " +
				"from ( " +
				"select STD_KEY,STD_LINE_NO, source, trim(val) val " +
				"from " +
				"  MSF096_STD_STATIC UNPIVOT INCLUDE NULLS " +
				"    ( VAL FOR( SOURCE ) IN " +
				"        ( STD_STATIC_1 AS 'STD_STATIC_1', " +
				"          STD_STATIC_2 AS 'STD_STATIC_2', " +
				"          STD_STATIC_3 AS 'STD_STATIC_3', " +
				"          STD_STATIC_4 AS 'STD_STATIC_4', " +
				"          STD_STATIC_5 AS 'STD_STATIC_5' " +
				"        ) " +
				"    ) " +
				"where STD_TEXT_CODE = 'GT' and trim(val) is not null and trim(std_key) = trim('"+"EX" + INV_NO.trim()+"') " +
				"order by STD_KEY,STD_LINE_NO, source) " +
				"group by STD_KEY");
			if(!QRY1.equals(null)) {
				result.addAttribute("extComm", QRY1.merge_VAL.trim());
			}
		}else {
			INV_NO = " ";
		}
		
		result.addAttribute("detCntNo", CNT_NO2);
		result.addAttribute("detInvNo", INV_NO);
		result.addAttribute("parCicFrom", PAR_CIC_FROM);
		result.addAttribute("parCicTo", PAR_CIC_TO);

		results.add(result);
		return results;
	}
	public GenericScriptResults create(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings)
	throws FatalException {
		log.info("Create ELL38I_DETAIL : " + version )
		GenericScriptResults results = new GenericScriptResults();
		GenericScriptResult result = new GenericScriptResult();
		RequestAttributes reqAtt = requestAttributes[0];
		
		String CNT_NO = "";
		if (!reqAtt.getAttributeStringValue("diaCntNo").equals(null)) {
			CNT_NO = reqAtt.getAttributeStringValue("diaCntNo");
		}
		else if (reqAtt.getAttributeStringValue("grdCntNo").equals(null)) {
			CNT_NO = reqAtt.getAttributeStringValue("detCntNo");
		}else {
			CNT_NO = reqAtt.getAttributeStringValue("grdCntNo");
		}
		
		String INV_NO = "";
		if (!reqAtt.getAttributeStringValue("diaInvNo").equals(null)) {
			INV_NO = reqAtt.getAttributeStringValue("diaInvNo");
		}
		else if (reqAtt.getAttributeStringValue("grdInvNo").equals(null)) {
			INV_NO = reqAtt.getAttributeStringValue("detInvNo");
		}else {
			INV_NO = reqAtt.getAttributeStringValue("grdInvNo");
		}
		
		String PAR_CIC_FROM = "";
		if (reqAtt.getAttributeStringValue("parCicFrom").equals(null)) {
			PAR_CIC_FROM = "";
		}else {
			PAR_CIC_FROM = reqAtt.getAttributeStringValue("parCicFrom");
		}
		
		String PAR_CIC_TO = "";
		if (reqAtt.getAttributeStringValue("parCicTo").equals(null)) {
			PAR_CIC_TO = "";
		}else {
			PAR_CIC_TO = reqAtt.getAttributeStringValue("parCicTo");
		}
		
		String BK_CODE = "";
		BK_CODE = reqAtt.getAttributeStringValue("bkCode");
		String BK_ACCT_NO = "";
		BK_ACCT_NO = reqAtt.getAttributeStringValue("bkAcctNo");
		String DET_INV_DATE = "";
		DateToString(reqAtt.getAttributeDateValue("detInvDate"));
		DET_INV_DATE = strCrDT;
		
		String INV_REC_DATE = "";
		if (!reqAtt.getAttributeDateValue("invRecDate").equals(null)) {
			DateToString(reqAtt.getAttributeDateValue("invRecDate"));
			INV_REC_DATE = strCrDT;
		}else {
			INV_REC_DATE = " "
		}
		
		INV_NO = INV_NO.toUpperCase();
		BigDecimal INV_VAL = 0;
		INV_VAL = reqAtt.getAttributeBigDecimalValue("detInvVal");
		
		log.info("CNT_NO : " + CNT_NO );
		log.info("INV_NO : " + INV_NO );
		//Validate Contract No
		String CNT_NO2 = "";
		def QRY1;
		QRY1 = sql.firstRow("select * from msf384 where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) ");
		log.info ("FIND CONTRACT  : ");
		if(QRY1.equals(null)) {
			StrErr = "INVALID CONTRACT NUMBER / DOESN'T EXIST"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("detCntNo")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}else {
			CNT_NO2 = QRY1.CONTRACT_NO
		}
		
		//Validate INV_NO
		if(INV_NO.equals(null)) {
			StrErr = "INVOICE NUMBER REQUIRED !"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("diaInvNo")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}else {
			if(INV_NO.length() > 20) {
				StrErr = "INVALID LENGTH"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("diaInvNo")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
			def QRY2;
			QRY2 = sql.firstRow("select * from msf260 where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(SUPPLIER_NO)) = upper(trim('"+QRY1.SUPPLIER_NO+"')) and upper(trim(EXT_INV_NO)) = upper(trim('"+INV_NO+"')) ");
			log.info ("FIND MSF260  : ");
			if(!QRY2.equals(null)) {
				StrErr = "INVOICE NUMBER ALREADY EXIST"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("detInvNo")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
		}
		
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
		//Validate Input By
		String EMP_ID = GetUserEmpID(securityToken.getUserId(), securityToken.getDistrict())
		
		//Validate Input By
		String INV_STAT = " ";
		
		//Validate Branch Code
		def QRY5;
		QRY5 = sql.firstRow("select * from msf000_bk " +
							"where DSTRCT_CODE = ' ' and OWNED_BY = '"+securityToken.getDistrict()+"' and BRANCH_CODE = '"+BK_CODE+"'");
		log.info ("FIND Branch Code  : ");
		if(QRY5.equals(null)) {
			StrErr = "INVALID BRANCH CODE"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("bkCode")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		//Validate Bank Account No
		def QRY6;
		QRY6 = sql.firstRow("select * from msf000_bk " +
							"where DSTRCT_CODE = ' ' and OWNED_BY = '"+securityToken.getDistrict()+"' and trim(BRANCH_CODE) = trim('"+BK_CODE+"') and trim(BANK_ACCT_NO) = trim('"+BK_ACCT_NO+"') ");
		log.info ("FIND BANK ACCT NO : ");
		if(QRY6.equals(null)) {
			StrErr = "INVALID BANK ACCOUNT NO"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("bkAcctNo")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		
		def QRY3;
		QRY3 = sql.firstRow("select * from aca.kpf38i where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CIC_INVOICE)) = upper(trim('"+INV_NO+"')) ");
		log.info ("FIND CIC INV  : ");
		if(!QRY3.equals(null)) {
			StrErr = "INVOICE NUMBER ALREADY EXIST"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("detInvNo")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}else {
			try
			{
				GetNowDateTime();
				String QueryInsert = ("Insert into ACA.KPF38I (DSTRCT_CODE,CIC_INVOICE,CIC_INV_ST,CONTRACT_NO,INVOICE_VAL,ACCEPT_DATE,CNTRCT_REM,CNTRCT_REM_VAL,TOTAL_CIC_VAL,INPUT_BY,CIC_BALANCE,TOTAL_ACT) values (?,?,?,?,?,?,?,?,?,?,?,?)");
				sql.execute(QueryInsert,[securityToken.getDistrict(),INV_NO,INV_STAT,CNT_NO,INV_VAL," ",0,0,0,EMP_ID,0,0]);
				
				QueryInsert = ("Insert into MSF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY) values ('IBC','"+INV_NO.trim()+"','001','001','"+strCrDT+"','"+strCrTM+"','"+EMP_ID+"','"+BK_CODE.trim()+"','            ')");
				sql.execute(QueryInsert);
				QueryInsert = ("Insert into MSF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY) values ('IBA','"+INV_NO.trim()+"','001','001','"+strCrDT+"','"+strCrTM+"','"+EMP_ID+"','"+BK_ACCT_NO.trim()+"','            ')");
				sql.execute(QueryInsert);
				QueryInsert = ("Insert into MSF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY) values ('IDT','"+INV_NO.trim()+"','001','001','"+strCrDT+"','"+strCrTM+"','"+EMP_ID+"','"+DET_INV_DATE.trim()+"','            ')");
				sql.execute(QueryInsert);
				QueryInsert = ("Insert into MSF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY) values ('IDR','"+INV_NO.trim()+"','001','001','"+strCrDT+"','"+strCrTM+"','"+EMP_ID+"','"+INV_REC_DATE+"','            ')");
				sql.execute(QueryInsert);
			} catch (Exception  e) {
				log.info ("Exception is : " + e);
				StrErr = "EXCEPTION : ERROR WHEN INSERT ACA.KPF38I";
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541);
				result.addError(err);
				results.add(result);
				RollErrMes();
				return results
			}
		}
		
		//RECALCULATE BALANCE
		BigDecimal CIC_INV_VAL = 0;
		BigDecimal BALANCE = 0;
		log.info ("RECALCULATE BALANCE: ");
		def QRY4;
		QRY4 = sql.firstRow("select sum(ACT_COST) AS SUM_ACT_COST from ACA.KPF38F where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and upper(trim(CIC_INVOICE)) = upper(trim('"+INV_NO+"')) ");
		CIC_INV_VAL = 0;
		CIC_INV_VAL = QRY4.SUM_ACT_COST
		log.info ("TOTAL_CIC_VAL: " + CIC_INV_VAL);
		if(QRY4.SUM_ACT_COST.equals(null)) {
			CIC_INV_VAL = 0
			BALANCE = 0;
			BALANCE = INV_VAL - CIC_INV_VAL
		}else {
			BALANCE = 0;
			BALANCE = INV_VAL - CIC_INV_VAL
		}
		
		try
		{
			log.info ("BALANCE: " + BALANCE);
			String QueryUpdate = ("update ACA.KPF38i set TOTAL_CIC_VAL = ?,CIC_BALANCE = ? where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and upper(trim(CIC_INVOICE)) = upper(trim('"+INV_NO+"')) ");
			sql.execute(QueryUpdate,[CIC_INV_VAL,BALANCE]);
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
		result.addAttribute("detCntNo", CNT_NO2);
		result.addAttribute("detInvNo", INV_NO);
		result.addAttribute("parCicFrom", PAR_CIC_FROM);
		result.addAttribute("parCicTo", PAR_CIC_TO);
		
		results.add(result);
		return results;
	}
	public GenericScriptResults update(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings)
	throws FatalException {
		log.info("Update ELL38I_DETAIL : " + version );
		GenericScriptResults results = new GenericScriptResults();
		GenericScriptResult result = new GenericScriptResult();
		RequestAttributes reqAtt = requestAttributes[0];
		
		String CNT_NO = "";
		if (!reqAtt.getAttributeStringValue("diaCntNo").equals(null)) {
			CNT_NO = reqAtt.getAttributeStringValue("diaCntNo");
		}
		else if (reqAtt.getAttributeStringValue("grdCntNo").equals(null)) {
			CNT_NO = reqAtt.getAttributeStringValue("detCntNo");
		}else {
			CNT_NO = reqAtt.getAttributeStringValue("grdCntNo");
		}
		
		String INV_NO = "";
		if (!reqAtt.getAttributeStringValue("diaInvNo").equals(null)) {
			INV_NO = reqAtt.getAttributeStringValue("diaInvNo");
		}
		else if (reqAtt.getAttributeStringValue("grdInvNo").equals(null)) {
			INV_NO = reqAtt.getAttributeStringValue("detInvNo");
		}else {
			INV_NO = reqAtt.getAttributeStringValue("grdInvNo");
		}
		INV_NO = INV_NO.toUpperCase();
		
		String PAR_CIC_FROM = "";
		if (reqAtt.getAttributeStringValue("parCicFrom").equals(null)) {
			PAR_CIC_FROM = "";
		}else {
			PAR_CIC_FROM = reqAtt.getAttributeStringValue("parCicFrom");
		}
		log.info("PAR_CIC_FROM : " + PAR_CIC_FROM );
		
		String PAR_CIC_TO = "";
		if (reqAtt.getAttributeStringValue("parCicTo").equals(null)) {
			PAR_CIC_TO = "";
		}else {
			PAR_CIC_TO = reqAtt.getAttributeStringValue("parCicTo");
		}
		log.info("PAR_CIC_TO : " + PAR_CIC_TO );
		
		BigDecimal INV_VAL = 0;
		INV_VAL = reqAtt.getAttributeBigDecimalValue("detInvVal");
		
		BigDecimal CIC_INV_VAL = 0;
		BigDecimal BALANCE = 0;
		
		log.info("CNT_NO : " + CNT_NO );
		log.info("INV_NO : " + INV_NO );
		
		String BK_CODE = "";
		BK_CODE = reqAtt.getAttributeStringValue("bkCode");
		String BK_ACCT_NO = "";
		BK_ACCT_NO = reqAtt.getAttributeStringValue("bkAcctNo");
		String DET_INV_DATE = "";
		if (!reqAtt.getAttributeDateValue("detInvDate").equals(null)) {
			DateToString(reqAtt.getAttributeDateValue("detInvDate"));
			DET_INV_DATE = strCrDT;
		}
		String INV_REC_DATE = "";
		if (!reqAtt.getAttributeDateValue("invRecDate").equals(null)) {
			DateToString(reqAtt.getAttributeDateValue("invRecDate"));
			INV_REC_DATE = strCrDT;
		}else {
			INV_REC_DATE = " "
		}
		
		//Validate Contract No
		def QRY1;
		QRY1 = sql.firstRow("select * from msf384 where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) ");
		log.info ("FIND CONTRACT  : ");
		if(QRY1.equals(null)) {
			StrErr = "INVALID CONTRACT NUMBER / DOESN'T EXIST"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("detCntNo")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		
		//Validate INV_NO
		if(INV_NO.equals(null)) {
			StrErr = "INVOICE NUMBER REQUIRED !"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("diaInvNo")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		
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
		
		//Validate Branch Code
		def QRY5;
		QRY5 = sql.firstRow("select * from msf000_bk " +
							"where DSTRCT_CODE = ' ' and OWNED_BY = '"+securityToken.getDistrict()+"' and BRANCH_CODE = '"+BK_CODE+"'");
		log.info ("FIND Branch Code  : ");
		if(QRY5.equals(null)) {
			StrErr = "INVALID BRANCH CODE"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("bkCode")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		//Validate Bank Account No
		def QRY6;
		QRY6 = sql.firstRow("select * from msf000_bk " +
							"where DSTRCT_CODE = ' ' and OWNED_BY = '"+securityToken.getDistrict()+"' and trim(BRANCH_CODE) = trim('"+BK_CODE+"') and trim(BANK_ACCT_NO) = trim('"+BK_ACCT_NO+"') ");
		log.info ("FIND BANK ACCT NO : ");
		if(QRY6.equals(null)) {
			StrErr = "INVALID BANK ACCOUNT NO"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("bkAcctNo")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		
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
		}else {
			if(!QRY3.CIC_INV_ST.trim().equals("")) {
				StrErr = "CIC INVOICE STATUS SHOULD BE SPACE"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("detInvNo")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
			try
			{
				String QueryUpdate = ("update ACA.KPF38I set INVOICE_VAL = ? where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and upper(trim(CIC_INVOICE)) = upper(trim('"+INV_NO+"')) ");
				sql.execute(QueryUpdate,[INV_VAL]);
				String QueryInsert = "";
				String EMP_ID = GetUserEmpID(securityToken.getUserId(), securityToken.getDistrict())
				GetNowDateTime();
				def QRY_UPD;
				QRY_UPD = sql.firstRow("select * from MSF071 where ENTITY_TYPE = 'IBC' and upper(trim(ENTITY_VALUE)) = upper(trim('"+INV_NO+"')) ");
				if(QRY_UPD.equals(null)) {
					QueryInsert = ("Insert into MSF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY) values ('IBC','"+INV_NO.trim()+"','001','001','"+strCrDT+"','"+strCrTM+"','"+EMP_ID+"','"+BK_CODE.trim()+"','            ')");
					sql.execute(QueryInsert);
				}else {
					QueryUpdate = ("update MSF071 set REF_CODE = ? where ENTITY_TYPE = 'IBC' and upper(trim(ENTITY_VALUE)) = upper(trim('"+INV_NO+"')) ");
					sql.execute(QueryUpdate,[BK_CODE]);
				}
				
				QRY_UPD = sql.firstRow("select * from MSF071 where ENTITY_TYPE = 'IBA' and upper(trim(ENTITY_VALUE)) = upper(trim('"+INV_NO+"')) ");
				if(QRY_UPD.equals(null)) {
					QueryInsert = ("Insert into MSF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY) values ('IBA','"+INV_NO.trim()+"','001','001','"+strCrDT+"','"+strCrTM+"','"+EMP_ID+"','"+BK_ACCT_NO.trim()+"','            ')");
					sql.execute(QueryInsert);
				}else {
					QueryUpdate = ("update MSF071 set REF_CODE = ? where ENTITY_TYPE = 'IBA' and upper(trim(ENTITY_VALUE)) = upper(trim('"+INV_NO+"')) ");
					sql.execute(QueryUpdate,[BK_ACCT_NO]);
				}
				
				QRY_UPD = sql.firstRow("select * from MSF071 where ENTITY_TYPE = 'IDT' and upper(trim(ENTITY_VALUE)) = upper(trim('"+INV_NO+"')) ");
				if(QRY_UPD.equals(null)) {
					QueryInsert = ("Insert into MSF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY) values ('IDT','"+INV_NO.trim()+"','001','001','"+strCrDT+"','"+strCrTM+"','"+EMP_ID+"','"+DET_INV_DATE.trim()+"','            ')");
					sql.execute(QueryInsert);
				}else {
					QueryUpdate = ("update MSF071 set REF_CODE = ? where ENTITY_TYPE = 'IDT' and upper(trim(ENTITY_VALUE)) = upper(trim('"+INV_NO+"')) ");
					sql.execute(QueryUpdate,[DET_INV_DATE]);
				}
				
				QRY_UPD = sql.firstRow("select * from MSF071 where ENTITY_TYPE = 'IDR' and upper(trim(ENTITY_VALUE)) = upper(trim('"+INV_NO+"')) ");
				if(QRY_UPD.equals(null)) {
					QueryInsert = ("Insert into MSF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY) values ('IDR','"+INV_NO.trim()+"','001','001','"+strCrDT+"','"+strCrTM+"','"+EMP_ID+"','"+INV_REC_DATE.trim()+"','            ')");
					sql.execute(QueryInsert);
				}else {
					QueryUpdate = ("update MSF071 set REF_CODE = ? where ENTITY_TYPE = 'IDR' and upper(trim(ENTITY_VALUE)) = upper(trim('"+INV_NO+"')) ");
					sql.execute(QueryUpdate,[INV_REC_DATE]);
				}
				
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
			
			//RECALCULATE BALANCE
			log.info ("RECALCULATE BALANCE: ");
			def QRY4;
			QRY4 = sql.firstRow("select sum(ACT_COST) AS SUM_ACT_COST from ACA.KPF38F where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and upper(trim(CIC_INVOICE)) = upper(trim('"+INV_NO+"')) ");
			CIC_INV_VAL = 0;
			CIC_INV_VAL = QRY4.SUM_ACT_COST
			log.info ("TOTAL_CIC_VAL: " + CIC_INV_VAL);
			if(QRY4.SUM_ACT_COST.equals(null)) {
				CIC_INV_VAL = 0
				BALANCE = 0;
				BALANCE = INV_VAL - CIC_INV_VAL
			}else {
				BALANCE = 0;
				BALANCE = INV_VAL - CIC_INV_VAL
			}
			
			try
			{
				log.info ("BALANCE: " + BALANCE);
				String QueryUpdate = ("update ACA.KPF38i set TOTAL_CIC_VAL = ?,CIC_BALANCE = ? where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and upper(trim(CIC_INVOICE)) = upper(trim('"+INV_NO+"')) ");
				sql.execute(QueryUpdate,[CIC_INV_VAL,BALANCE]);
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
			
			//Insert Comments
			String INT_CMT = "";
			if (reqAtt.getAttributeStringValue("intComm").equals(null)) {
				INT_CMT = "";
			}else {
				INT_CMT = reqAtt.getAttributeStringValue("intComm");
			}
			String EXT_CMT = "";
			if (reqAtt.getAttributeStringValue("extComm").equals(null)) {
				EXT_CMT = "";
			}else {
				EXT_CMT = reqAtt.getAttributeStringValue("extComm");
			}
			
			String SUPP = "";
			if (reqAtt.getAttributeStringValue("contSupp").equals(null)) {
				SUPP = "";
			}else {
				SUPP = reqAtt.getAttributeStringValue("contSupp");
			}
			
			if(!INT_CMT.equals("")) {
				String[] TemporaryArray;
				//log.info ("INT_CMT1 :" + INT_CMT);
				INT_CMT = INT_CMT.replaceAll("(.{60})", "\$1\n");
				//log.info ("INT_CMT2 :" + INT_CMT);
				//TemporaryArray = INT_CMT.split("(?<=\\G.{61})");
				TemporaryArray = INT_CMT.split("\\r?\\n");
				log.info ("TemporaryArray :" + TemporaryArray);
				log.info ("TemporaryArray Length :" + TemporaryArray.length);
				String VAL_MESS = "";
				//log.info ("SUPP :" + SUPP);
				VAL_MESS = CREATE_TEXT("IX" + INV_NO.trim(),TemporaryArray);
				if (!VAL_MESS.equals("")) {
					// Raise Error
					StrErr = VAL_MESS;
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541);
					result.addError(err);
					results.add(result);
					RollErrMes();
					return results
				}
			}else {
				String VAL_MESS = "";
				VAL_MESS = DELETE_TEXT("IX" + INV_NO.trim());
				if (!VAL_MESS.equals("")) {
					// Raise Error
					StrErr = VAL_MESS;
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541);
					result.addError(err);
					results.add(result);
					RollErrMes();
					return results
				}
			}
			
			if(!EXT_CMT.equals("")) {
				String[] TemporaryArray;
				EXT_CMT = EXT_CMT.replaceAll("(.{60})", "\$1\n");
				TemporaryArray = EXT_CMT.split("\\r?\\n");
				log.info ("TemporaryArray Length :" + TemporaryArray.length)
				String VAL_MESS = "";
				//log.info ("SUPP :" + SUPP);
				VAL_MESS = CREATE_TEXT("EX" + INV_NO.trim(),TemporaryArray);
				if (!VAL_MESS.equals("")) {
					// Raise Error
					StrErr = VAL_MESS;
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541);
					result.addError(err);
					results.add(result);
					RollErrMes();
					return results
				}
			}else {
				String VAL_MESS = "";
				VAL_MESS = DELETE_TEXT("EX" + INV_NO.trim());
				if (!VAL_MESS.equals("")) {
					// Raise Error
					StrErr = VAL_MESS;
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541);
					result.addError(err);
					results.add(result);
					RollErrMes();
					return results
				}
			}
			
		}
		result.addAttribute("detCntNo", CNT_NO);
		result.addAttribute("detInvNo", INV_NO);
		
		result.addAttribute("parCicFrom", PAR_CIC_FROM);
		result.addAttribute("parCicTo", PAR_CIC_TO);
		
		results.add(result);
		return results;
	}
	public GenericScriptResults delete(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings)
	throws FatalException {
		log.info("Delete ELL38I_DETAIL : " + version )
		GenericScriptResults results = new GenericScriptResults();
		GenericScriptResult result = new GenericScriptResult();
		RequestAttributes reqAtt = requestAttributes[0];
		
		String CNT_NO = "";
		if (!reqAtt.getAttributeStringValue("diaCntNo").equals(null)) {
			CNT_NO = reqAtt.getAttributeStringValue("diaCntNo");
		}
		else if (reqAtt.getAttributeStringValue("grdCntNo").equals(null)) {
			CNT_NO = reqAtt.getAttributeStringValue("detCntNo");
		}else {
			CNT_NO = reqAtt.getAttributeStringValue("grdCntNo");
		}
		
		String INV_NO = "";
		if (!reqAtt.getAttributeStringValue("diaInvNo").equals(null)) {
			INV_NO = reqAtt.getAttributeStringValue("diaInvNo");
		}
		else if (reqAtt.getAttributeStringValue("grdInvNo").equals(null)) {
			INV_NO = reqAtt.getAttributeStringValue("detInvNo");
		}else {
			INV_NO = reqAtt.getAttributeStringValue("grdInvNo");
		}
		INV_NO = INV_NO.toUpperCase();
		
		String PAR_CIC_FROM = "";
		if (reqAtt.getAttributeStringValue("parCicFrom").equals(null)) {
			PAR_CIC_FROM = "";
		}else {
			PAR_CIC_FROM = reqAtt.getAttributeStringValue("parCicFrom");
		}
		
		String PAR_CIC_TO = "";
		if (reqAtt.getAttributeStringValue("parCicTo").equals(null)) {
			PAR_CIC_TO = "";
		}else {
			PAR_CIC_TO = reqAtt.getAttributeStringValue("parCicTo");
		}
		
		BigDecimal INV_VAL = 0;
		INV_VAL = reqAtt.getAttributeBigDecimalValue("detInvVal");
		
		log.info("CNT_NO : " + CNT_NO );
		log.info("INV_NO : " + INV_NO );
		//Validate Contract No
		def QRY1;
		QRY1 = sql.firstRow("select * from msf384 where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) ");
		log.info ("FIND CONTRACT  : ");
		if(QRY1.equals(null)) {
			StrErr = "INVALID CONTRACT NUMBER / DOESN'T EXIST"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("detCntNo")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		
		//Validate INV_NO
		if(INV_NO.equals(null)) {
			StrErr = "INVOICE NUMBER REQUIRED !"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("diaInvNo")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		
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
		}else {
			if(!QRY3.CIC_INV_ST.trim().equals("")) {
				StrErr = "CIC INVOICE STATUS SHOULD BE SPACE"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("detInvStat")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
			try
			{
				String QueryDelete = ("delete ACA.KPF38I where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and upper(trim(CIC_INVOICE)) = upper(trim('"+INV_NO+"')) ");
				sql.execute(QueryDelete);
				
				String QueryUpdate = ("update ACA.KPF38F set CIC_INVOICE = ? where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and upper(trim(CIC_INVOICE)) = upper(trim('"+INV_NO+"')) ");
				sql.execute(QueryUpdate,[" "]);
				
				QueryDelete = ("delete MSF071 where ENTITY_TYPE = 'IBC' and upper(trim(ENTITY_VALUE)) = upper(trim('"+INV_NO+"')) ");
				sql.execute(QueryDelete);
				QueryDelete = ("delete MSF071 where ENTITY_TYPE = 'IBA' and upper(trim(ENTITY_VALUE)) = upper(trim('"+INV_NO+"')) ");
				sql.execute(QueryDelete);
				QueryDelete = ("delete MSF071 where ENTITY_TYPE = 'IDT' and upper(trim(ENTITY_VALUE)) = upper(trim('"+INV_NO+"')) ");
				sql.execute(QueryDelete);
				QueryDelete = ("delete MSF071 where ENTITY_TYPE = 'IDR' and upper(trim(ENTITY_VALUE)) = upper(trim('"+INV_NO+"')) ");
				sql.execute(QueryDelete);
			} catch (Exception  e) {
				log.info ("Exception is : " + e);
				StrErr = "EXCEPTION DELETE ACA.KPF38I : ";
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541);
				result.addError(err);
				results.add(result);
				RollErrMes();
				return results
			}
		}
		result.addAttribute("detCntNo", CNT_NO);
		result.addAttribute("detInvNo", INV_NO);
		result.addAttribute("parCicFrom", PAR_CIC_FROM);
		result.addAttribute("parCicTo", PAR_CIC_TO);
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
	private String DELETE_TEXT(String KEY) {
		String MESSAGE = "";
		try
		{
			log.info ("DELETE_TEXT:");
			StdTextServiceDeleteReplyDTO DEL_REP_DTO = service.get("StdText").delete({
				it.stdTextId = "GT" + KEY;
				},false);
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
					StdTextServiceCreateReplyDTO CRE_REP_DTO = service.get("StdText").create({
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
							StdTextServiceCreateReplyDTO CRE_REP_DTO = service.get("StdText").create({
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
							StdTextServiceAppendReplyDTO APPEND_REP_DTO = service.get("StdText").append({
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
				StdTextServiceDeleteReplyDTO DEL_REP_DTO = service.get("StdText").delete({
					it.stdTextId = "GT" + KEY;
					},false)
				if (CONTENT.length <= 20) {
					StdTextServiceCreateReplyDTO CRE_REP_DTO = service.get("StdText").create({
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
							StdTextServiceCreateReplyDTO CRE_REP_DTO = service.get("StdText").create({
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
							StdTextServiceAppendReplyDTO APPEND_REP_DTO = service.get("StdText").append({
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
}
