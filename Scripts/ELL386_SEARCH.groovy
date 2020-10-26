/**
 * @EMS Jan 2019
 *
 * 20190101 - a9ra5213 - Ricky Afriano - KPC UPGRADE Ellipse 8
 *            Initial Coding - Display Contract items in ELL38C Detail Screen 
 **/
package KPC
import java.text.DecimalFormat
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
import com.mincom.ellipse.errors.Error
import com.mincom.ellipse.errors.CobolMessages
import com.mincom.ellipse.*

public class ELL386_SEARCH extends GenericScriptPlugin implements GenericScriptExecuteForCollection, GenericScriptUpdate, GenericScriptCreate, GenericScriptDelete, GenericScriptExecute{
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
	
	public GenericScriptResults executeForCollection(SecurityToken securityToken, RequestAttributes requestAttributes,
		Integer maxNumberOfObjects, RestartAttributes restartAttributes) throws FatalException {
		log.info("Execute Colection ELL386_SEARCH : " + version )
		GenericScriptResults results = new GenericScriptResults()
		RequestAttributes reqAtt = requestAttributes
		
		String CNT_NO = "";
		if (reqAtt.getAttributeStringValue("parCntNo").equals(null)) {
			CNT_NO = reqAtt.getAttributeStringValue("cntNo");
		}else {
			CNT_NO = reqAtt.getAttributeStringValue("parCntNo");
		}
		
		String WO = "";
		if (reqAtt.getAttributeStringValue("parWo").equals(null)) {
			WO = reqAtt.getAttributeStringValue("wo");
		}else {
			WO = reqAtt.getAttributeStringValue("parWo");
		}
		String qryCateg1 = "";
		String qryCateg2 = "";
		if (WO.equals(null)) {
			qryCateg1 = "left outer join msf385 f on (f.CONTRACT_NO = a.CONTRACT_NO and a.PORTION_NO = f.PORTION_NO) " +
			            "left outer join msf010 e on (e.TABLE_TYPE = 'CE' and trim(e.TABLE_CODE) = trim(b.categ_code)) ";
			qryCateg2 = " and (case when trim(b.categ_code) = 'NUA' and f.PORTION_TYPE = 'A' then 'TRUE' " + 
                        " when f.PORTION_TYPE = 'M' and b.CATEG_BASE_PRICE <> 0 then 'TRUE' else 'FALSE' end) = 'TRUE' ";
		}else {
			qryCateg1 = "left outer join msf010 e on (e.TABLE_TYPE = 'CE' and trim(e.TABLE_CODE) = trim(b.categ_code)) ";
			qryCateg2 = " and substr(e.ASSOC_REC,5,1) = 'N' ";
		}
		DecimalFormat df = new DecimalFormat("#,##0.00;-#,##0.00");
		String StrSQL = ""
		log.info("maxNumberOfObjects : " + maxNumberOfObjects );
		log.info("restartAttributes : " + restartAttributes );
		if (restartAttributes.equals(null)){
			
			StrSQL = "select row_number () over(order by a.PORTION_NO,a.ELEMENT_NO,b.CATEGORY_NO) AS NO,a.PORTION_NO,a.ELEMENT_NO,b.CATEGORY_NO,regexp_replace(b.CATEG_DESC,'�|','') CATEG_DESC,b.CATEG_BASE_QTY,b.CATEG_BASE_UN, " +
				"case when trim(c.TABLE_DESC) is null then ' ' else trim(c.TABLE_DESC) end CATEG_BASE_UN_DESC, " +
				"b.CATEG_BASE_PRC_RT,b.CATEG_BASE_VAL,b.CATEG_BASE_DT,b.CATEG_BASE_PRICE,b.CATEGORY_387_TYPE,d.COND_OF_CNTRCT " +
				"from msf386 a " +
				"left outer join msf387 b on (a.CONTRACT_NO = b.CONTRACT_NO and a.PORTION_NO = b.PORTION_NO and a.ELEMENT_NO = b.ELEMENT_NO) " +
				"left outer join msf010 c on (c.table_type = 'EU' and trim(table_code) = trim(b.CATEG_BASE_UN)) " +
				"left outer join msf384 d on (a.CONTRACT_NO = d.CONTRACT_NO) " +
				qryCateg1 +
				"where upper(trim(a.CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and d.dstrct_code = '"+securityToken.getDistrict()+"'" +qryCateg2+ //b.CATEG_BASE_PRICE <> 0 and b.CATEG_BASE_PRC_RT <> 0 " +
				"Order by a.PORTION_NO,a.ELEMENT_NO,b.CATEGORY_NO OFFSET 0 ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY";
			log.info ("StrSQL : " + StrSQL);
			
			sql.eachRow(StrSQL, {
				if ((!WO.equals(null) && it.COND_OF_CNTRCT.trim().equals("UM")) || (it.COND_OF_CNTRCT.trim().equals("CB") || it.COND_OF_CNTRCT.trim().equals("NU"))) {
					GenericScriptResult result = new GenericScriptResult();
					result.addAttribute("noUrut", it.NO);
					result.addAttribute("portion", it.PORTION_NO);
					result.addAttribute("element", it.ELEMENT_NO);
					result.addAttribute("category", it.CATEGORY_NO);
					result.addAttribute("catDesc", it.CATEG_DESC);
					result.addAttribute("catQty", it.CATEG_BASE_QTY);
					result.addAttribute("catUnit", it.CATEG_BASE_UN);
					result.addAttribute("catUnitDesc", it.CATEG_BASE_UN_DESC);
					result.addAttribute("catBasePrRt", it.CATEG_BASE_PRC_RT);
					result.addAttribute("catBaseVal", it.CATEG_BASE_VAL);
					result.addAttribute("catBaseDt", it.CATEG_BASE_DT);
					result.addAttribute("catBasePr", it.CATEG_BASE_PRICE);
					result.addAttribute("catType", it.CATEGORY_387_TYPE);
					result.addAttribute("itemType", it.CATEGORY_387_TYPE);
					result.addAttribute("lastRow", maxNumberOfObjects.toString());
					results.add(result);
				}
			})
			log.info("results.getServiceResults().size() CEK 1: " + results.getServiceResults().size() );
			if(results.getServiceResults().size() == 0) {
				log.info("CEK : " );
				GenericScriptResult result = new GenericScriptResult();
				result.addAttribute("lastRow", "0");
				results.add(result);
			}
		}else {
			log.info("restartAttributes : " + restartAttributes.getAttributeStringValue("lastRow") );
//		    Integer MaxInst = Integer.parseInt(restartAttributes.getAttributeStringValue("lastRow"));
			String lastRowString = restartAttributes.getAttributeStringValue("lastRow")
			log.info("lastRowString: $lastRowString")
			Integer MaxInst = lastRowString.toInteger()
			log.info("MaxInst: $MaxInst")
		    //MaxInst = MaxInst + maxNumberOfObjects
			StrSQL = "select row_number () over(order by a.PORTION_NO,a.ELEMENT_NO,b.CATEGORY_NO) AS NO,a.PORTION_NO,a.ELEMENT_NO,b.CATEGORY_NO,regexp_replace(b.CATEG_DESC,'�|','') CATEG_DESC,b.CATEG_BASE_QTY,b.CATEG_BASE_UN, " +
				"case when trim(c.TABLE_DESC) is null then ' ' else trim(c.TABLE_DESC) end CATEG_BASE_UN_DESC, " +
				"b.CATEG_BASE_PRC_RT,b.CATEG_BASE_VAL,b.CATEG_BASE_DT,b.CATEG_BASE_PRICE,b.CATEGORY_387_TYPE,d.COND_OF_CNTRCT " +
				"from msf386 a " +
				"left outer join msf387 b on (a.CONTRACT_NO = b.CONTRACT_NO and a.PORTION_NO = b.PORTION_NO and a.ELEMENT_NO = b.ELEMENT_NO) " +
				"left outer join msf010 c on (c.table_type = 'EU' and trim(table_code) = trim(b.CATEG_BASE_UN)) " +
				"left outer join msf384 d on (a.CONTRACT_NO = d.CONTRACT_NO) " +
				qryCateg1 +
				"where upper(trim(a.CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and d.dstrct_code = '"+securityToken.getDistrict()+"'" +qryCateg2+
				"Order by a.PORTION_NO,a.ELEMENT_NO,b.CATEGORY_NO OFFSET "+MaxInst.toString()+" ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY";
			log.info ("StrSQL : " + StrSQL);
			sql.eachRow(StrSQL, {
				if ((!WO.equals(null) && it.COND_OF_CNTRCT.trim().equals("UM")) || (it.COND_OF_CNTRCT.trim().equals("CB") || it.COND_OF_CNTRCT.trim().equals("NU"))) {
					GenericScriptResult result = new GenericScriptResult();
					String maxInstArs = it.NO
					MaxInst = maxInstArs.toInteger()
					log.info("maxInstArs: $maxInstArs")
					log.info("MaxInst2: $MaxInst")
//					MaxInst = Integer.parseInt(it.NO);
					result.addAttribute("noUrut", it.NO);
					result.addAttribute("portion", it.PORTION_NO);
					result.addAttribute("element", it.ELEMENT_NO);
					result.addAttribute("category", it.CATEGORY_NO);
					result.addAttribute("catDesc", it.CATEG_DESC);
					result.addAttribute("catQty", it.CATEG_BASE_QTY);
					result.addAttribute("catUnit", it.CATEG_BASE_UN);
					result.addAttribute("catUnitDesc", it.CATEG_BASE_UN_DESC);
					result.addAttribute("catBasePrRt", it.CATEG_BASE_PRC_RT);
					result.addAttribute("catBaseVal", it.CATEG_BASE_VAL);
					result.addAttribute("catBaseDt", it.CATEG_BASE_DT);
					result.addAttribute("catBasePr", it.CATEG_BASE_PRICE);
					result.addAttribute("catType", it.CATEGORY_387_TYPE);
					result.addAttribute("itemType", it.CATEGORY_387_TYPE);
					result.addAttribute("lastRow", MaxInst.toString());
					results.add(result);
				}
			})
			log.info("results.getServiceResults().size() CEK 2: " + results.getServiceResults().size() );
			if(results.getServiceResults().size() == 0) {
				log.info("CEK : " );
				GenericScriptResult result = new GenericScriptResult();
				result.addAttribute("lastRow", "0");
				results.add(result);
			}
		}
		return results
	}
	public GenericScriptResults create(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings)
	throws FatalException {
		log.info("Create ELL386_SEARCH : " + version )
		GenericScriptResults results = new GenericScriptResults()
		GenericScriptResult result = new GenericScriptResult()
		RequestAttributes reqAtt = requestAttributes[0]
		
		results.add(result)
		return results
	}
	public GenericScriptResults update(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings)
	throws FatalException {
		log.info("Update ELL386_SEARCH : " + version )
		GenericScriptResults results = new GenericScriptResults()
		GenericScriptResult result = new GenericScriptResult()
		RequestAttributes reqAtt = requestAttributes[0]
		
		results.add(result)
		return results
	}
	public GenericScriptResults delete(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings)
	throws FatalException {
		log.info("Delete ELL386_SEARCH : " + version )
		GenericScriptResults results = new GenericScriptResults()
		GenericScriptResult result = new GenericScriptResult()
		
		results.add(result)
		return results
	}
	public GenericScriptResults execute(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings)
	throws FatalException {
		log.info("Execute ADD_CIC : " + version )
		GenericScriptResults results = new GenericScriptResults()
		GenericScriptResult result = new GenericScriptResult()
		RequestAttributes reqAtt = requestAttributes[0]
		String CNT_NO = "";
		if (reqAtt.getAttributeStringValue("parCntNo").equals(null)) {
			CNT_NO = reqAtt.getAttributeStringValue("cntNo");
		}else {
			CNT_NO = reqAtt.getAttributeStringValue("parCntNo");
		}
		
		String WO = "";
		if (reqAtt.getAttributeStringValue("parWo").equals(null)) {
			WO = reqAtt.getAttributeStringValue("wo");
		}else {
			WO = reqAtt.getAttributeStringValue("parWo");
		}
		String CIC_NO = "";
		if (reqAtt.getAttributeStringValue("parGrdCicNo").equals(null)) {
			CIC_NO = reqAtt.getAttributeStringValue("cicNo");
		}else {
			CIC_NO = reqAtt.getAttributeStringValue("parGrdCicNo");
		}
		
		requestAttributes.eachWithIndex {reqAttItem, index ->
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
			String CURR_TYPE = QRY1.CURRENCY_TYPE;
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
				if(!QRY2.CIC_STATUS.equals("1")) {
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
			
			def QRY0;
			QRY0 = sql.firstRow("select CIC_TYPE,to_char(count(CIC_ITEM_NO)) JML_ITEM from ACA.KPF38G where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and CONTRACT_NO = '"+CNT_NO+"' and CIC_NO = '"+CIC_NO+"' group by CIC_TYPE");
			if(!QRY0.equals(null)) {
				if(QRY0.CIC_TYPE.trim().equals("LS")) {
					if(QRY0.JML_ITEM.trim().equals("1")) {
						StrErr = "NON UMBRELLA CIC COULD NOT MORE THAN ONE ITEM";
						SetErrMes();
						com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541);
						result.addError(err);
						results.add(result);
						RollErrMes();
						return results
					}
				}
			}
			
			//Validate CIC Item No
			String CIC_ITEM_NO = ""
			CIC_ITEM_NO = reqAttItem.getAttributeStringValue("portion").toString().trim() + reqAttItem.getAttributeStringValue("element").toString().trim() + reqAttItem.getAttributeStringValue("category").toString().trim();
			def QRY3;
			QRY3 = sql.firstRow("select * from ACA.KPF38G where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"' and CIC_ITEM_NO = '"+CIC_ITEM_NO+"'");
			if(!QRY3.equals(null)) {
				StrErr = "CIC ITEM NUMBER ALREADY EXIST"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				//err.setFieldId("cicNo")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
			
			//Validate Category
			def QRY4;
			QRY4 = sql.firstRow("select a.*,substr(replace(trim(replace(a.CATEG_DESC,'�','')),'’',''''),1,40) CATEG_DESC2 from MSF387 a where upper(trim(a.CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and trim(a.PORTION_NO)||trim(a.ELEMENT_NO)||trim(a.CATEGORY_NO) = '"+CIC_ITEM_NO+"'");
			if(QRY4.equals(null)) {
				StrErr = "CONTRACT USER NO UNMATCH WITH CIC ITEM NO"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				//err.setFieldId("cicNo")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
			
			//Get CIC Item Desc
			String CIC_ITM_DESC = "";
			if(!QRY4.equals(null)) {
				if(!QRY4.CATEG_DESC2.equals(null)) {
					CIC_ITM_DESC = QRY4.CATEG_DESC2.trim()
				}
			}else {
				StrErr = "INVALID CIC ITEM DESC."
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				//err.setFieldId("cicNo")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
			log.info ("CIC_ITM_DESC is : " + CIC_ITM_DESC);
			String CAT_BASE_PR_RT = "";
			if (reqAttItem.getAttributeBigDecimalValue("catBasePrRt").equals(null)) {
				StrErr = "INVALID CATEG BASE PRICE RATE"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				//err.setFieldId("cicNo")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}else {
				CAT_BASE_PR_RT = reqAttItem.getAttributeBigDecimalValue("catBasePrRt").toString();
			}
			String CAT_UNIT = "";
			if (reqAttItem.getAttributeStringValue("catUnit").equals(null)) {
				StrErr = "INVALID CATEG BASE UNIT"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				//err.setFieldId("cicNo")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}else {
				CAT_UNIT = reqAttItem.getAttributeStringValue("catUnit").toString();
			}
			if(CAT_UNIT.equals("")) {
				CAT_UNIT = " "
			}
			/*
			BigDecimal EX_RATE = 0;
			Boolean FOREIGN_TRANS = false;
			//Validate Currency
			def QRY5;
			QRY5 = sql.firstRow("select * from MSF000_DC0001 where DSTRCT_CODE = '"+securityToken.getDistrict()+"'");
			if(!QRY5.equals(null)) {
				if (!CURR_TYPE.trim().equals(QRY5.LOCAL_CURRENCY)) {
					FOREIGN_TRANS = true;
					
				}else {
					FOREIGN_TRANS = false;
				}
			}
			GET EX_RATE
			select * from msf912
			where LOCAL_CURRENCY = 'USD' and FOREIGN_CURR = 'IDR'
			order by 99999999-DATE_PER_REVSD desc OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY;
			*/
			try
			{
				GetNowDateTime();
				//log.info ("CIC_ITM_DESC 2 : " + CIC_ITM_DESC);
				String QueryInsert = ("Insert into ACA.KPF38G (DSTRCT_CODE,CONTRACT_NO,CIC_NO,CIC_ITEM_NO,CIC_TYPE,CIC_ITEM_DESC,CATEG_BASE_RATE,CATEG_BASE_UN,ESTIMATED_QTY,ACTUAL_QTY,PROGRESS,TOTAL_EST,TOTAL_ACT,PROGRESS_S,TOTAL_ACT_S,PROGRESS_F,TOTAL_ACT_F,TOTAL_EST_L,TOTAL_EST_S,TOTAL_EST_F) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
				//log.info ("QueryInsert : " + QueryInsert);
				sql.executeInsert(QueryInsert,[securityToken.getDistrict(),CNT_NO,CIC_NO,CIC_ITEM_NO,CIC_TYPE,CIC_ITM_DESC,CAT_BASE_PR_RT,CAT_UNIT,0,0,0,0,0,0,0,0,0,0,0,0]);
			} catch (Exception  e) {
				log.info ("Exception is : " + e);
				StrErr = "EXCEPTION : ERROR WHEN INSERT ACA.KPF38G";
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541);
				result.addError(err);
				results.add(result);
				RollErrMes();
				return results
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
}