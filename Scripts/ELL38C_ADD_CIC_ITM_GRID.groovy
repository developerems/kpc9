/**
 * @EMS Jan 2019
 *
 * 20200810 - a9rj2193 - Ratna Juanita - Case #003340
 *            Add CIC-STATUS 'U' to compute Remaining Plan. 
 * 20190101 - a9ra5213 - Ricky Afriano - KPC UPGRADE Ellipse 8
 *            Initial Coding - Display CIC items in ELL38C Detail Screen 
 **/
package KPC
import java.math.RoundingMode
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
import com.mincom.ellipse.errors.UnlocalisedError
import com.mincom.ellipse.errors.UnlocalisedWarning
import com.mincom.ellipse.errors.CobolMessages
import com.mincom.ellipse.*
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsService
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsServiceCreateReplyDTO
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsServiceModifyReplyDTO
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsServiceModifyAddTaxReplyDTO
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsServiceModItemsReplyDTO
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsServiceCreateRequestDTO
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsServiceApproveReplyDTO
import com.mincom.enterpriseservice.ellipse.contractcosting.ContractCostingService
import com.mincom.enterpriseservice.ellipse.contractcosting.ContractCostingServiceRetrieveReplyDTO
import com.mincom.enterpriseservice.ellipse.contractcosting.ContractCostingServiceRetrieveReplyCollectionDTO
import com.mincom.enterpriseservice.ellipse.contractcosting.ContractCostingServiceModifyReplyDTO
import com.mincom.enterpriseservice.ellipse.contractcosting.ContractCostingServicePreCommitReplyDTO
import com.mincom.ellipse.service.m338a.contractquestionanswers.ContractQuestionAnswersService
import com.mincom.ellipse.types.m338a.instances.ContractQuestionAnswersServiceResult;
import com.mincom.ellipse.types.m0000.instances.ContractNo;
import com.mincom.ellipse.types.m0000.instances.ValnNo;
import com.mincom.ellipse.types.m0000.instances.ContEventType;
import com.mincom.enterpriseservice.ellipse.dependant.dto.WorkOrderDTO
import com.mincom.enterpriseservice.ellipse.WarningMessageDTO

public class ELL38C_ADD_CIC_ITM_GRID extends GenericScriptPlugin implements GenericScriptExecuteForCollection, GenericScriptUpdate, GenericScriptCreate, GenericScriptDelete{
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)

	String version = "2";
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
	String STR_VALN_NO = "";
	String DST = "";
	String WO_NO_GLBL = "";

	public GenericScriptResults executeForCollection(SecurityToken securityToken, RequestAttributes requestAttributes,
			Integer maxNumberOfObjects, RestartAttributes restartAttributes) throws FatalException {
		log.info("Execute Colection ELL38C_ADD_CIC_ITM_GRID : " + version )
		GenericScriptResults results = new GenericScriptResults()
		RequestAttributes reqAtt = requestAttributes

		String CNT_NO = "";
		if (reqAtt.getAttributeStringValue("parGrdCntNo").equals(null)) {
			CNT_NO = reqAtt.getAttributeStringValue("cntNo");
		}else {
			CNT_NO = reqAtt.getAttributeStringValue("parGrdCntNo");
		}

		String CIC_NO = "";
		if (reqAtt.getAttributeStringValue("parGrdCicNo").equals(null)) {
			CIC_NO = reqAtt.getAttributeStringValue("cicNo");
		}else {
			CIC_NO = reqAtt.getAttributeStringValue("parGrdCicNo");
		}

		String WO = "";
		if (reqAtt.getAttributeStringValue("parGrdWoNo").equals(null)) {
			WO = reqAtt.getAttributeStringValue("wo");
		}else {
			WO = reqAtt.getAttributeStringValue("parGrdWoNo");
		}
		DecimalFormat df = new DecimalFormat("#,##0.00;-#,##0.00");

		def QRY1;
		QRY1 = sql.firstRow("select * from msf384 where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) ");
		//log.info ("FIND CONTRACT  : " + QRY1);
		if(QRY1.equals(null)) {
			GenericScriptResult result = new GenericScriptResult();
			StrErr = "INVALID CONTRACT NUMBER / DOESN'T EXIST"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("CRE_CNT_NO")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		//Cek currencies
		String CURR_TYPE = QRY1.CURRENCY_TYPE;
		Boolean FOREIGN_TRANS = false;
		def QRY5;
		QRY5 = sql.firstRow("select * from MSF000_DC0001 where DSTRCT_CODE = '"+securityToken.getDistrict()+"'");
		if(!QRY5.equals(null)) {
			if (!CURR_TYPE.trim().equals(QRY5.LOCAL_CURRENCY.trim())) {
				FOREIGN_TRANS = true;
			}else {
				FOREIGN_TRANS = false;
			}
		}
		log.info("FOREIGN_TRANS : " + FOREIGN_TRANS )
		String CIC_STATUS = "";
		def QRY2;
		QRY2 = sql.firstRow("select * from ACA.KPF38F where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"'");
		if(!QRY2.equals(null)) {
			CIC_STATUS = QRY2.CIC_STATUS;
		}

		String StrSQL = ""
		if (restartAttributes.equals(null)){

			StrSQL = "select row_number () over(order by CIC_ITEM_NO) AS NO,a.*,substr(a.CIC_ITEM_NO,1,2) PORTION,substr(a.CIC_ITEM_NO,3,2) ELEMENT,substr(a.CIC_ITEM_NO,5,2) CATEGORY from ACA.KPF38G a " +
					"where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and upper(trim(CIC_NO)) = '"+CIC_NO+"'" +
					"Order by CIC_ITEM_NO OFFSET 0 ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY";
			log.info ("StrSQL : " + StrSQL);

			sql.eachRow(StrSQL, {

				GenericScriptResult result = new GenericScriptResult();
				result.addAttribute("cicItmNo", it.CIC_ITEM_NO);
				result.addAttribute("cicItmDesc", it.CIC_ITEM_DESC);
				result.addAttribute("cicType", it.CIC_TYPE);
				result.addAttribute("baseRate", it.CATEG_BASE_RATE);
				result.addAttribute("baseUnit", it.CATEG_BASE_UN);
				result.addAttribute("estQty", it.ESTIMATED_QTY);
				result.addAttribute("actQty", it.ACTUAL_QTY);
				if(FOREIGN_TRANS.equals(true)) {
					result.addAttribute("val", it.PROGRESS_F);
				}else {
					result.addAttribute("val", it.PROGRESS);
				}
				result.addAttribute("cicPortion", it.PORTION);
				result.addAttribute("cicElement", it.ELEMENT);
				result.addAttribute("cicCategory", it.CATEGORY);
				result.addAttribute("lastRow", maxNumberOfObjects.toString());
				if((CIC_STATUS.trim().equals("1") || CIC_STATUS.trim().equals("R")) && it.CIC_TYPE.trim().equals("WO")) {
					result.addAttribute("woEdit", "TRUE");
				}else {
					result.addAttribute("woEdit", "FALSE");
				}
				if((CIC_STATUS.trim().equals("1") || CIC_STATUS.trim().equals("R")) && it.CIC_TYPE.trim().equals("LS")) {
					result.addAttribute("lsEdit", "TRUE");
				}else {
					result.addAttribute("lsEdit", "FALSE");
				}
				BigDecimal PERCENTAGE = 0;
				if(it.CIC_TYPE.trim().equals("LS")) {
					if (it.PROGRESS.equals(0)) {
						PERCENTAGE = 0;
					}else {
						PERCENTAGE = it.PROGRESS / QRY1.CONTRACT_VAL * 100
					}
				}else {
					PERCENTAGE = 0;
				}
				PERCENTAGE = PERCENTAGE.setScale(2,RoundingMode.HALF_UP);
				result.addAttribute("percentage", PERCENTAGE);
				results.add(result);

			})
		}else {
			//Integer MaxInst = Integer.parseInt(restartAttributes.getAttributeStringValue("lastRow"));
			//MaxInst = MaxInst + maxNumberOfObjects
			Integer MaxInst = Integer.parseInt(restartAttributes.getAttributeStringValue("lastRow"));
			StrSQL = "select row_number () over(order by CIC_ITEM_NO) AS NO,a.*,substr(a.CIC_ITEM_NO,1,2) PORTION,substr(a.CIC_ITEM_NO,3,2) ELEMENT,substr(a.CIC_ITEM_NO,5,2) CATEGORY from ACA.KPF38G a " +
					"where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and upper(trim(CIC_NO)) = '"+CIC_NO+"'" +
					"Order by CIC_ITEM_NO OFFSET "+MaxInst.toString()+" ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY";
			log.info ("StrSQL : " + StrSQL);

			sql.eachRow(StrSQL, {
				GenericScriptResult result = new GenericScriptResult();
				MaxInst = it.NO
				result.addAttribute("cicItmNo", it.CIC_ITEM_NO);
				result.addAttribute("cicItmDesc", it.CIC_ITEM_DESC);
				result.addAttribute("cicType", it.CIC_TYPE);
				result.addAttribute("baseRate", it.CATEG_BASE_RATE);
				result.addAttribute("baseUnit", it.CATEG_BASE_UN);
				result.addAttribute("estQty", it.ESTIMATED_QTY);
				result.addAttribute("actQty", it.ACTUAL_QTY);
				if(FOREIGN_TRANS.equals(true)) {
					result.addAttribute("val", it.PROGRESS_F);
				}else {
					result.addAttribute("val", it.PROGRESS);
				}
				result.addAttribute("cicPortion", it.PORTION);
				result.addAttribute("cicElement", it.ELEMENT);
				result.addAttribute("cicCategory", it.CATEGORY);
				result.addAttribute("lastRow", MaxInst.toString());
				if((CIC_STATUS.trim().equals("1") || CIC_STATUS.trim().equals("R")) && it.CIC_TYPE.trim().equals("WO")) {
					result.addAttribute("woEdit", "TRUE");
				}else {
					result.addAttribute("woEdit", "FALSE");
				}
				if((CIC_STATUS.trim().equals("1") || CIC_STATUS.trim().equals("R")) && it.CIC_TYPE.trim().equals("LS")) {
					result.addAttribute("lsEdit", "TRUE");
				}else {
					result.addAttribute("lsEdit", "FALSE");
				}
				BigDecimal PERCENTAGE = 0;
				if(it.CIC_TYPE.trim().equals("LS")) {
					if (it.PROGRESS.equals(0)) {
						PERCENTAGE = 0;
					}else {
						PERCENTAGE = it.PROGRESS / QRY1.CONTRACT_VAL * 100
					}
				}else {
					PERCENTAGE = 0;
				}
				result.addAttribute("percentage", PERCENTAGE);
				results.add(result);
			})
		}
		return results
	}
	public GenericScriptResults create(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings)
	throws FatalException {
		log.info("Create ELL38C_ADD_CIC_ITM_GRID : " + version )
		GenericScriptResults results = new GenericScriptResults()
		GenericScriptResult result = new GenericScriptResult()
		RequestAttributes reqAtt = requestAttributes[0]

		results.add(result)
		return results
	}
	public GenericScriptResults update(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings)
	throws FatalException {
		log.info("Update ELL38C_ADD_CIC_ITM_GRID : " + version )
		GenericScriptResults results = new GenericScriptResults()
		GenericScriptResult result = new GenericScriptResult()
		RequestAttributes reqAtt = requestAttributes[0]
		DST = securityToken.getDistrict();
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
		WO_NO_GLBL = "";
		String CIC_NO = "";
		if (reqAtt.getAttributeStringValue("parGrdCicNo").equals(null)) {
			CIC_NO = reqAtt.getAttributeStringValue("cicNo");
		}else {
			CIC_NO = reqAtt.getAttributeStringValue("parGrdCicNo");
		}
		
		def QRY0_A;
		QRY0_A = sql.firstRow("select * from ACA.KPF38F where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"'");
		//log.info ("FIND CIC  : " + QRY2);
		if(!QRY0_A.equals(null)) {
			WO = QRY0_A.WORK_ORDER;
		}
		
		Boolean retnWarn,processItem;
		retnWarn = false;
		processItem = false;
		requestAttributes.eachWithIndex {reqAttItem, index ->
			String CIC_ITEM_NO = ""
			CIC_ITEM_NO = reqAttItem.getAttributeStringValue("cicItmNo").toString().trim();
			def QRY0;
			QRY0 = sql.firstRow("select a.DSTRCT_CODE,a.CONTRACT_NO,a.WORK_ORDER,a.CIC_NO,b.CIC_ITEM_NO " +
					"from aca.kpf38f a " +
					"left outer join  aca.kpf38g b on (a.DSTRCT_CODE = b.DSTRCT_CODE and a.CONTRACT_NO = b.CONTRACT_NO and a.CIC_NO = b.CIC_NO) " +
					"where a.DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(a.CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and upper(trim(a.WORK_ORDER)) = upper(trim('"+WO+"')) and b.CIC_ITEM_NO = '"+CIC_ITEM_NO+"' and a.CIC_NO <> '"+CIC_NO+"'");
			if(!QRY0.equals(null)) {
				retnWarn = true;
				return;
			}
		}
		log.info("retnWarn: " + retnWarn.toString());
		if (retnWarn == true) {
			log.info("returnWarnings: " + returnWarnings.toString());
			if (returnWarnings) {
				log.info("returnWarnings2: " + returnWarnings.toString());
				result.addWarning(new UnlocalisedWarning("ONE OR MORE ITEM DUPLICATE WITH OTHER CIC, CONTINUE?"))
				results.add(result);
				return results;
			}else {
				processItem = true;
			}
		}else {
			processItem = true;
		}
		log.info("processItem: " + processItem.toString());
		if (processItem == true) {
			log.info("processItem2: " + processItem.toString());
			BigDecimal CEK_REM = 0;
			Boolean ACT_FLAG = false;
			String EMP_ID = GetUserEmpID(securityToken.getUserId(), securityToken.getDistrict());
			requestAttributes.eachWithIndex {reqAttItem, index ->
				//Validate Contract No
				log.info("Val Contract : ")
				def QRY1;
				QRY1 = sql.firstRow("select * from msf384 where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) ");
				//log.info ("FIND CONTRACT  : " + QRY1);
				if(QRY1.equals(null)) {
					StrErr = "INVALID CONTRACT NUMBER / DOESN'T EXIST"
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					err.setFieldId("CRE_CNT_NO")
					result.addError(err)
					results.add(result)
					RollErrMes();
					return results
				}
				String CURR_TYPE = QRY1.CURRENCY_TYPE;
				String CIC_TYPE = "";
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
					WO_NO_GLBL = QRY2.WORK_ORDER;
				}

				//Validate CIC Item No
				log.info("Val CIC Item : ")
				String CIC_ITEM_NO = ""
				CIC_ITEM_NO = reqAttItem.getAttributeStringValue("cicItmNo").toString().trim()
				log.info("CIC_ITEM_NO : " + CIC_ITEM_NO )
				def QRY3;
				QRY3 = sql.firstRow("select * from ACA.KPF38G where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"' and CIC_ITEM_NO = '"+CIC_ITEM_NO+"'");
				if(QRY3.equals(null)) {
					StrErr = "CIC ITEM NUMBER DOES NOT EXIST"
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					//err.setFieldId("cicNo")
					result.addError(err)
					results.add(result)
					RollErrMes();
					return results
				}

				//Validate Category
				log.info("Val Cat : ")
				def QRY4;
				QRY4 = sql.firstRow("select * from MSF387 where upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and trim(PORTION_NO)||trim(ELEMENT_NO)||trim(CATEGORY_NO) = '"+CIC_ITEM_NO+"'");
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
				/*
				 //Get CIC Item Desc
				 String CIC_ITM_DESC = "";
				 if(!QRY4.equals(null)) {
				 if(!QRY4.CATEG_DESC.equals(null)) {
				 CIC_ITM_DESC = QRY4.CATEG_DESC
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
				 String CAT_UNIT = "";
				 if (reqAttItem.getAttributeStringValue("CAT_UNIT").equals(null)) {
				 StrErr = "INVALID CATEG BASE UNIT"
				 SetErrMes();
				 com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				 //err.setFieldId("cicNo")
				 result.addError(err)
				 results.add(result)
				 RollErrMes();
				 return results
				 }else {
				 CAT_UNIT = reqAttItem.getAttributeStringValue("CAT_UNIT").toString();
				 }
				 */
				/*
				 GET EX_RATE
				 select * from msf912
				 where LOCAL_CURRENCY = 'USD' and FOREIGN_CURR = 'IDR'
				 order by 99999999-DATE_PER_REVSD desc OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY;
				 */

				BigDecimal CAT_BASE_PR_RT = 0;
				if (reqAttItem.getAttributeBigDecimalValue("baseRate").equals(null)) {
					StrErr = "INVALID CATEG BASE PRICE RATE"
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					//err.setFieldId("cicNo")
					result.addError(err)
					results.add(result)
					RollErrMes();
					return results
				}else {
					CAT_BASE_PR_RT = reqAttItem.getAttributeBigDecimalValue("baseRate");
				}

				BigDecimal EX_RATE = 0;
				Boolean FOREIGN_TRANS = false;
				//Validate Currency
				log.info("Val Currency : ")
				def QRY5;
				QRY5 = sql.firstRow("select * from MSF000_DC0001 where DSTRCT_CODE = '"+securityToken.getDistrict()+"'");
				if(!QRY5.equals(null)) {
					if (!CURR_TYPE.trim().equals(QRY5.LOCAL_CURRENCY.trim())) {
						FOREIGN_TRANS = true;
						def QRY6;
						QRY6 = sql.firstRow("select * from msf912 " +
								"where LOCAL_CURRENCY = '"+QRY5.LOCAL_CURRENCY+"' and FOREIGN_CURR = '"+CURR_TYPE+"' and 99999999-date_per_revsd <= '"+QRY2.EST_DATE+"'" +
								"order by 99999999-DATE_PER_REVSD desc OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY");
						if(!QRY6.equals(null)) {
							EX_RATE = QRY6.BUYING_RATE;
							log.info("EX_RATE : " + EX_RATE );
						}
					}else {
						FOREIGN_TRANS = false;
					}
				}

				BigDecimal ESTIMATED_QTY = 0;
				BigDecimal ACTUAL_QTY = 0;
				BigDecimal PROGRESS = 0;
				BigDecimal TOTAL_EST = 0;
				BigDecimal TOTAL_ACT = 0;
				BigDecimal PROGRESS_F = 0;
				BigDecimal TOTAL_ACT_F = 0;
				BigDecimal TOTAL_EST_L = 0;
				BigDecimal TOTAL_EST_F = 0;
				BigDecimal TOTAL_EST_COST = 0;
				BigDecimal TOTAL_ACT_COST = 0;
				BigDecimal TOTAL_CUM_COST = 0;
				CEK_REM = 0;

				if (reqAttItem.getAttributeBigDecimalValue("estQty").equals(null)) {
					ESTIMATED_QTY = 0
				}else {
					ESTIMATED_QTY = reqAttItem.getAttributeBigDecimalValue("estQty");
				}
				if(ESTIMATED_QTY < 0) {
					StrErr = "COULD NOT INPUT NEGATIVE QUANTITY"
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					err.setFieldId("estQty")
					result.addError(err)
					results.add(result)
					RollErrMes();
					return results
				}
				if (reqAttItem.getAttributeBigDecimalValue("actQty").equals(null)) {
					ACTUAL_QTY = 0
				}else {
					ACTUAL_QTY = reqAttItem.getAttributeBigDecimalValue("actQty");
				}
				if(ACTUAL_QTY < 0) {
					StrErr = "COULD NOT INPUT NEGATIVE QUANTITY"
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					err.setFieldId("actQty")
					result.addError(err)
					results.add(result)
					RollErrMes();
					return results
				}
				if (reqAttItem.getAttributeBigDecimalValue("val").equals(null)) {
					PROGRESS = 0
				}else {
					PROGRESS = reqAttItem.getAttributeBigDecimalValue("val");
				}
				if(PROGRESS < 0) {
					StrErr = "COULD NOT INPUT NEGATIVE VALUE"
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					err.setFieldId("val")
					result.addError(err)
					results.add(result)
					RollErrMes();
					return results
				}
				if (QRY3.CIC_TYPE.trim().equals("WO")) {
					TOTAL_EST = ESTIMATED_QTY * CAT_BASE_PR_RT
					TOTAL_ACT = ACTUAL_QTY * CAT_BASE_PR_RT
					if(FOREIGN_TRANS.equals(false)) {
						TOTAL_EST_L = TOTAL_EST
					}else {
						TOTAL_EST_F = TOTAL_EST
						TOTAL_ACT_F = TOTAL_ACT
						TOTAL_EST_L = TOTAL_EST / EX_RATE
						TOTAL_ACT = TOTAL_ACT / EX_RATE
					}
				}else {
					if(FOREIGN_TRANS.equals(false)) {
						TOTAL_EST_L = PROGRESS
					}else {
						PROGRESS_F = PROGRESS
						TOTAL_EST_F = PROGRESS
						PROGRESS = PROGRESS / EX_RATE
						TOTAL_EST_L = PROGRESS
					}
				}

				TOTAL_EST_COST = TOTAL_EST_COST + TOTAL_EST;

				log.info("ESTIMATED_QTY : " + ESTIMATED_QTY)
				log.info("ACTUAL_QTY : " + ACTUAL_QTY)
				log.info("PROGRESS : " + PROGRESS)
				log.info("TOTAL_EST : " + TOTAL_EST)
				log.info("TOTAL_ACT : " + TOTAL_ACT)
				log.info("PROGRESS_F : " + PROGRESS_F)
				log.info("TOTAL_ACT_F : " + TOTAL_ACT_F)
				log.info("TOTAL_EST_L : " + TOTAL_EST_L)
				log.info("TOTAL_EST_F : " + TOTAL_EST_F)

				//Validate Actual and create valuation
				def QRY7 = sql.firstRow("select * from msf071 " +
						"where ENTITY_TYPE = 'CIV' and ENTITY_VALUE = '"+securityToken.getDistrict().trim()+CNT_NO.trim()+CIC_NO.trim()+"' and REF_NO = '001' and SEQ_NUM = '001'");
				log.info ("FIND VALN_NO  : " + QRY7);
				String VAL_MESS = "";
				if (QRY3.CIC_TYPE.trim().equals("WO")) {
					if (QRY3.ACTUAL_QTY != ACTUAL_QTY) {
						if(!QRY7.equals(null)) {
							VAL_MESS = MOD_ITM_VAL(CNT_NO,CIC_ITEM_NO,QRY7.REF_CODE.trim(),ACTUAL_QTY,QRY3.CIC_TYPE.trim(),EMP_ID,securityToken.getRole(),CIC_NO);
							STR_VALN_NO = QRY7.REF_CODE.trim();
							ACT_FLAG = true;
						}
					}
				}else {
					if (QRY3.PROGRESS != PROGRESS) {
						if(!QRY7.equals(null)) {
							if(FOREIGN_TRANS.equals(true)) {
								VAL_MESS = MOD_ITM_VAL(CNT_NO,CIC_ITEM_NO,QRY7.REF_CODE.trim(),PROGRESS_F,QRY3.CIC_TYPE.trim(),EMP_ID,securityToken.getRole(),CIC_NO);
							}else {
								VAL_MESS = MOD_ITM_VAL(CNT_NO,CIC_ITEM_NO,QRY7.REF_CODE.trim(),PROGRESS,QRY3.CIC_TYPE.trim(),EMP_ID,securityToken.getRole(),CIC_NO);
							}
							STR_VALN_NO = QRY7.REF_CODE.trim();
							ACT_FLAG = true;
						}
					}
				}

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
				}
				//Update KPF38G
				log.info("Update KPF38G : ")
				try
				{
					GetNowDateTime();
					String QueryUpdate = ("update ACA.KPF38G " +
							"set ESTIMATED_QTY = " +ESTIMATED_QTY.toString()+ " , ACTUAL_QTY = "+ACTUAL_QTY.toString()+ " , PROGRESS = " + PROGRESS.toString() + " , " +
							"TOTAL_EST = " + TOTAL_EST.toString() + " , TOTAL_ACT = " +TOTAL_ACT.toString()+ " , PROGRESS_F = "+PROGRESS_F.toString()+ " , TOTAL_ACT_F = " +
							TOTAL_ACT_F.toString() + " , TOTAL_EST_L = " +TOTAL_EST_L.toString()+" , TOTAL_EST_F = "+TOTAL_EST_F.toString()+ " " +
							"where upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"' and CIC_ITEM_NO = '"+CIC_ITEM_NO+"'");
					sql.execute(QueryUpdate);
					/*
					 if(FOREIGN_TRANS.equals(false)) {
					 CEK_REM = TOTAL_EST+PROGRESS
					 }else {
					 CEK_REM = TOTAL_EST+PROGRESS_F
					 }
					 */
				} catch (Exception  e) {
					log.info ("Exception is : " + e);
					StrErr = "EXCEPTION : ERROR WHEN UPDATE ACA.KPF38G";
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541);
					result.addError(err);
					results.add(result);
					RollErrMes();
					return results
				}
				/*
				 TOTAL_EST_COST = TOTAL_EST + QRY2.EST_COST
				 if(FOREIGN_TRANS.equals(false)) {
				 TOTAL_ACT_COST = TOTAL_ACT + QRY2.ACT_COST
				 TOTAL_CUM_COST = PROGRESS + QRY2.CUM_COST
				 TOTAL_EST_COST = TOTAL_EST_COST + PROGRESS
				 }else {
				 TOTAL_ACT_COST = TOTAL_ACT_F + QRY2.ACT_COST
				 TOTAL_CUM_COST = PROGRESS_F + QRY2.CUM_COST
				 TOTAL_EST_COST = TOTAL_EST_COST + PROGRESS_F
				 }*/
				def QRY6;
				QRY6 = sql.firstRow("select sum(TOTAL_EST+PROGRESS) EST_COST_L,sum(TOTAL_EST+PROGRESS_F) EST_COST_F, " +
						"sum(TOTAL_ACT) ACT_COST_L,sum(TOTAL_ACT_F) ACT_COST_F,sum(PROGRESS) CUM_COST_L,sum(PROGRESS_F) CUM_COST_F " +
						"from aca.kpf38g " +
						"where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"'");
				if(QRY6.equals(null)) {
					StrErr = "INVALID CONTARCT / CIC NO";
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541);
					result.addError(err);
					results.add(result);
					RollErrMes();
					return results
				}else {
					if(FOREIGN_TRANS.equals(false)) {
						TOTAL_EST_COST = QRY6.EST_COST_L
						TOTAL_ACT_COST = QRY6.ACT_COST_L
						TOTAL_CUM_COST = QRY6.CUM_COST_L
					}else {
						TOTAL_EST_COST = QRY6.EST_COST_F
						TOTAL_ACT_COST = QRY6.ACT_COST_F
						TOTAL_CUM_COST = QRY6.CUM_COST_F
					}
				}
				log.info("TOTAL_EST_COST : " + TOTAL_EST_COST)
				log.info("TOTAL_ACT_COST : " + TOTAL_ACT_COST)
				log.info("TOTAL_CUM_COST : " + TOTAL_CUM_COST)

				CEK_REM = TOTAL_EST_COST;

				log.info("Update KPF38F : ")
				try
				{
					GetNowDateTime();
					String QueryUpdate = ("update ACA.KPF38F " +
							"set CUM_COST = " +TOTAL_CUM_COST.toString()+ " , ACT_COST = "+TOTAL_ACT_COST.toString()+ " , EST_COST = " + TOTAL_EST_COST.toString() + " , " +
							"EST_BY = '" + EMP_ID + "' , EST_DATE = '" +strCrDT+ "' " +
							"where  DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"'");
					sql.execute(QueryUpdate);
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
			}
			if(ACT_FLAG.equals(true)) {
				String VAL_MESS = "";
				def QRY2;
				QRY2 = sql.firstRow("select * from ACA.KPF38F where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"'");
				if(!QRY2.equals(null)) {

					//Validate Remaining
					def QRY3_A;
					/* Case 003340 -- add 'U' in "select contract_no, sum(case when CIC_STATUS in ('1', 'U')..." */
					QRY3_A = sql.firstRow("select a.*,a.CONTRACT_VAL - b.INV_VAL REM_ACT,((a.CONTRACT_VAL - b.INV_VAL) - EST_VAL + "+CEK_REM+") REM_PLN,trim(d.table_desc) CCOC_DESC " +
							"from msf384 a " +
							"left outer join ( " +
							"select CONTRACT_NO,sum(case when CIC_STATUS in ('1', 'U') then EST_COST else 0 end) EST_VAL, " +
							"sum(case when CIC_STATUS in ('2','4') then ACT_COST else 0 end) INV_VAL " +
							"from ACA.KPF38F " +
							"where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) " +
							"group by CONTRACT_NO " +
							") b on (1=1) " +
							"left outer join msf010 d on (trim(a.COND_OF_CNTRCT) = trim(d.table_code) and trim(d.table_type) = 'CCOC') " +
							"where a.DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(a.CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) ");
					if(!QRY3_A.REM_PLN.equals(null)) {
						log.info("CEK_REMAIN: " + CEK_REM );
						log.info("REM_PLN: " + QRY3_A.REM_PLN );
						log.info("EST_COST: " + QRY2.EST_COST);
						log.info("CUM_COST: " + QRY2.CUM_COST);
						if (QRY2.CIC_TYPE.trim().equals("WO")) {
							if (QRY2.EST_COST > QRY3_A.REM_PLN) {
								DecimalFormat df = new DecimalFormat("#,##0.00;-#,##0.00");
								result.addError(new UnlocalisedError("CIC VALUE WILL EXCEEDS CONTRACT VALUE -->" + " TOTAL NEW CIC = "+ df.format(QRY2.EST_COST) + " REM. PLAN = " + df.format(QRY3_A.REM_PLN)))
								results.add(result)
								RollErrMes();
								return results
							}
						}else {
							if (QRY2.CUM_COST > QRY3_A.REM_PLN) {
								DecimalFormat df = new DecimalFormat("#,##0.00;-#,##0.00");
								result.addError(new UnlocalisedError("CIC VALUE WILL EXCEEDS CONTRACT VALUE -->" + " TOTAL NEW CIC = "+ df.format(QRY2.CUM_COST) + " REM. PLAN = " + df.format(QRY3_A.REM_PLN)))
								results.add(result)
								RollErrMes();
								return results
							}
						}
					}
					/*
					 if(QRY3_A.REM_ACT.equals(QRY3_A.REM_PLN)) {
					 DecimalFormat df = new DecimalFormat("#,##0.00;-#,##0.00");
					 result.addError(new UnlocalisedError("CIC VALUE WILL EXCEEDS CONTRACT VALUE -->" + " REM. ACT. = "+ df.format(QRY3_A.REM_ACT) + " REM. PLAN = " + df.format(QRY3_A.REM_PLN)))
					 results.add(result)
					 RollErrMes();
					 return results
					 }
					 */

					String QueryUpdate;

					VAL_MESS = "";
					if (QRY2.CIC_TYPE.trim().equals("WO")) {
						VAL_MESS = MOD_VAL(CNT_NO,STR_VALN_NO,QRY2.ACT_COST)
					}else {
						VAL_MESS = MOD_VAL(CNT_NO,STR_VALN_NO,QRY2.CUM_COST)
					}

					QRY2 = sql.firstRow("select * from ACA.KPF38F where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"'");
					if(!QRY2.CIC_TYPE.trim().equals("WO")) {
						QueryUpdate = ("update MSF38B " +
								"set AMT_TO_CONTRACTOR = " +QRY2.EST_COST+ " , VALUE_THIS_VALN = "+QRY2.EST_COST+ " , EXT_INV_AMT = " + QRY2.EST_COST + " " +
								"where  upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and VALN_NO = '"+STR_VALN_NO+"'");
						sql.execute(QueryUpdate);
					}else {
						QueryUpdate = ("update MSF38B " +
								"set AMT_TO_CONTRACTOR = " +QRY2.ACT_COST+ " , VALUE_THIS_VALN = "+QRY2.ACT_COST+ " , EXT_INV_AMT = " + QRY2.ACT_COST + " " +
								"where  upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and VALN_NO = '"+STR_VALN_NO+"'");
						sql.execute(QueryUpdate);
					}
				}
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
				}
				/*
				 VAL_MESS = "";
				 VAL_MESS = APP_VAL(CNT_NO,STR_VALN_NO,EMP_ID,securityToken.getRole(),CIC_NO)
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
				 }
				 */
			}
		}

		results.add(result)
		return results
	}
	public GenericScriptResults delete(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings)
	throws FatalException {
		log.info("Delete ELL38C_ADD_CIC_ITM_GRID : " + version )
		GenericScriptResults results = new GenericScriptResults()
		GenericScriptResult result = new GenericScriptResult()
		RequestAttributes reqAtt = requestAttributes[0]
		DST = securityToken.getDistrict();
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
		WO_NO_GLBL = "";
		String CIC_NO = "";
		if (reqAtt.getAttributeStringValue("parGrdCicNo").equals(null)) {
			CIC_NO = reqAtt.getAttributeStringValue("cicNo");
		}else {
			CIC_NO = reqAtt.getAttributeStringValue("parGrdCicNo");
		}

		Boolean ACT_FLAG = false;
		String EMP_ID = GetUserEmpID(securityToken.getUserId(), securityToken.getDistrict());

		requestAttributes.eachWithIndex {reqAttItem, index ->
			//Validate Contract No
			log.info("Val Contract : ")
			def QRY1;
			QRY1 = sql.firstRow("select * from msf384 where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) ");
			//log.info ("FIND CONTRACT  : " + QRY1);
			if(QRY1.equals(null)) {
				StrErr = "INVALID CONTRACT NUMBER / DOESN'T EXIST"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("CRE_CNT_NO")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
			String CURR_TYPE = QRY1.CURRENCY_TYPE;
			String CIC_TYPE = "";
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
				WO_NO_GLBL = QRY2.WORK_ORDER;
			}

			//Validate CIC Item No
			log.info("Val CIC Item : ")
			String CIC_ITEM_NO = ""
			CIC_ITEM_NO = reqAttItem.getAttributeStringValue("cicItmNo").toString().trim()
			log.info("CIC_ITEM_NO : " + CIC_ITEM_NO )
			def QRY3;
			QRY3 = sql.firstRow("select * from ACA.KPF38G where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"' and CIC_ITEM_NO = '"+CIC_ITEM_NO+"'");
			if(QRY3.equals(null)) {
				StrErr = "CIC ITEM NUMBER DOES NOT EXIST"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				//err.setFieldId("cicNo")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}

			//Validate Category
			log.info("Val Cat : ")
			def QRY4;
			QRY4 = sql.firstRow("select * from MSF387 where upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and trim(PORTION_NO)||trim(ELEMENT_NO)||trim(CATEGORY_NO) = '"+CIC_ITEM_NO+"'");
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

			//Update Valuation
			String VAL_MESS = "";
			STR_VALN_NO = "";
			def QRY7 = sql.firstRow("select * from msf071 " +
					"where ENTITY_TYPE = 'CIV' and ENTITY_VALUE = '"+securityToken.getDistrict().trim()+CNT_NO.trim()+CIC_NO.trim()+"' and REF_NO = '001' and SEQ_NUM = '001'");
			log.info ("FIND VALN_NO  : " + QRY7);
			if(!QRY7.equals(null)) {
				VAL_MESS = DEL_ITM_VAL(CNT_NO,CIC_ITEM_NO,QRY7.REF_CODE.trim(),0,QRY3.CIC_TYPE.trim(),EMP_ID,securityToken.getRole(),CIC_NO);
				STR_VALN_NO = QRY7.REF_CODE.trim();
			}
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
			}

			//Delete CIC Item
			log.info("Delete CIC Item : ")
			try
			{
				String QueryDelete = ("delete ACA.KPF38G where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"' and CIC_ITEM_NO = '"+CIC_ITEM_NO+"'");
				sql.execute(QueryDelete);
			} catch (Exception  e) {
				log.info ("Exception is : " + e);
				StrErr = "EXCEPTION : ERROR WHEN DELETE ACA.KPF38G";
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541);
				result.addError(err);
				results.add(result);
				RollErrMes();
				return results
			}

			BigDecimal EX_RATE = 0;
			Boolean FOREIGN_TRANS = false;
			//Validate Currency
			log.info("Val Currency : ")
			def QRY5;
			QRY5 = sql.firstRow("select * from MSF000_DC0001 where DSTRCT_CODE = '"+securityToken.getDistrict()+"'");
			if(!QRY5.equals(null)) {
				if (!CURR_TYPE.trim().equals(QRY5.LOCAL_CURRENCY.trim())) {
					FOREIGN_TRANS = true;
					def QRY6;
					QRY6 = sql.firstRow("select * from msf912 " +
							"where LOCAL_CURRENCY = '"+QRY5.LOCAL_CURRENCY+"' and FOREIGN_CURR = '"+CURR_TYPE+"' and 99999999-date_per_revsd <= '"+QRY2.EST_DATE+"'" +
							"order by 99999999-DATE_PER_REVSD desc OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY");
					if(!QRY6.equals(null)) {
						EX_RATE = QRY6.BUYING_RATE;
						log.info("EX_RATE : " + EX_RATE );
					}
				}else {
					FOREIGN_TRANS = false;
				}
			}

			BigDecimal TOTAL_EST_COST = 0;
			BigDecimal TOTAL_ACT_COST = 0;
			BigDecimal TOTAL_CUM_COST = 0;

			def QRY6;
			QRY6 = sql.firstRow("select case when sum(TOTAL_EST+PROGRESS) is null then 0 else sum(TOTAL_EST+PROGRESS) end EST_COST_L,case when sum(TOTAL_EST+PROGRESS_F) is null then 0 else sum(TOTAL_EST+PROGRESS_F) end EST_COST_F, " +
					"case when sum(TOTAL_ACT) is null then 0 else sum(TOTAL_ACT) end ACT_COST_L,case when sum(TOTAL_ACT_F) is null then 0 else sum(TOTAL_ACT_F) end ACT_COST_F,case when sum(PROGRESS) is null then 0 else sum(PROGRESS) end CUM_COST_L,case when sum(PROGRESS_F) is null then 0 else sum(PROGRESS_F) end CUM_COST_F " +
					"from aca.kpf38g " +
					"where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"'");
			if(QRY6.equals(null)) {
				StrErr = "INVALID CONTARCT / CIC NO";
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541);
				result.addError(err);
				results.add(result);
				RollErrMes();
				return results
			}else {
				if(FOREIGN_TRANS.equals(false)) {
					TOTAL_EST_COST = QRY6.EST_COST_L
					TOTAL_ACT_COST = QRY6.ACT_COST_L
					TOTAL_CUM_COST = QRY6.CUM_COST_L
				}else {
					TOTAL_EST_COST = QRY6.EST_COST_F
					TOTAL_ACT_COST = QRY6.ACT_COST_F
					TOTAL_CUM_COST = QRY6.CUM_COST_F
				}
			}
			log.info("TOTAL_EST_COST : " + TOTAL_EST_COST)
			log.info("TOTAL_ACT_COST : " + TOTAL_ACT_COST)
			log.info("TOTAL_CUM_COST : " + TOTAL_CUM_COST)

			log.info("Update KPF38F : ")
			try
			{
				GetNowDateTime();
				String QueryUpdate = ("update ACA.KPF38F " +
						"set CUM_COST = " +TOTAL_CUM_COST.toString()+ " , ACT_COST = "+TOTAL_ACT_COST.toString()+ " , EST_COST = " + TOTAL_EST_COST.toString() + " , " +
						"EST_BY = '" + EMP_ID + "' , EST_DATE = '" +strCrDT+ "' " +
						"where  DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"'");
				sql.execute(QueryUpdate);

				QRY2 = sql.firstRow("select * from ACA.KPF38F where DSTRCT_CODE = '"+securityToken.getDistrict()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"'");
				if(!QRY2.CIC_TYPE.trim().equals("WO")) {
					QueryUpdate = ("update MSF38B " +
							"set AMT_TO_CONTRACTOR = " +QRY2.EST_COST+ " , VALUE_THIS_VALN = "+QRY2.EST_COST+ " , EXT_INV_AMT = " + QRY2.EST_COST + " " +
							"where  upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and VALN_NO = '"+STR_VALN_NO+"'");
					sql.execute(QueryUpdate);
				}else {
					QueryUpdate = ("update MSF38B " +
							"set AMT_TO_CONTRACTOR = " +QRY2.ACT_COST+ " , VALUE_THIS_VALN = "+QRY2.ACT_COST+ " , EXT_INV_AMT = " + QRY2.ACT_COST + " " +
							"where  upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and VALN_NO = '"+STR_VALN_NO+"'");
					sql.execute(QueryUpdate);
				}
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
		}

		results.add(result)
		return results
	}
	private String SetErrMes(){
		String Qerr = "UPDATE msf010 set TABLE_DESC = ? where TABLE_type = 'ER' and TABLE_CODE = '8541'";
		try
		{
			if (StrErr.length() > 50) {
				StrErr = StrErr.substring(0, 50);
			}
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
	private String MOD_VAL(String CNT_NO,String VAL_NO,BigDecimal CNTR_AMT){
		String MESSAGE = "";
		try
		{
			log.info ("MODIFY VALUATION:");
			ValuationsServiceModifyReplyDTO MOD_REP_DTO = service.get("Valuations").modify({
				it.contractNo = CNT_NO.trim()
				it.valuationNo = VAL_NO.trim()
				it.cntrctrRefAmt = CNTR_AMT
				it.valnTypeFlag = "N"
				it.extInvAsInv = false
				it.finalValn = false
			},false)
			log.info ("cntrctrRefAmt:" + MOD_REP_DTO.getCntrctrRefAmt());
		}catch (EnterpriseServiceOperationException e){
			log.info ("MASUK EXCEPTION MOD VAL:");
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
	private String APP_VAL(String CNT_NO,String VAL_NO,String EMP_ID,String POSITION,String CIC_NO){
		String MESSAGE = "";
		try
		{
			log.info ("APPROVE VALUATION:");
			log.info ("APP_BY_POSITION:" + POSITION);
			ValuationsServiceApproveReplyDTO APPR_REP_DTO = service.get("Valuations").approve({
				it.contractNo = CNT_NO.trim()
				it.valuationNo = VAL_NO.trim()
				it.approvedBy = EMP_ID.trim()
				it.approvedByPosition = POSITION.trim()
				//it.valnTypeFlag = "N"
			},false)
			WarningMessageDTO[] WR_MESS;
			WR_MESS = APPR_REP_DTO.getWarningsAndInformation();
			log.info ("VAL Status:" + APPR_REP_DTO.getValnStatusDescription());
			log.info ("VAL App Pos:" + APPR_REP_DTO.getApprovedByPosition());
			Integer i = 0;
			//APPR_REP_DTO.getWarningsAndInformation().length
			for (i=0;i<WR_MESS.length;i++) {
				log.info ("VAL WARN:" + WR_MESS[i].getMessage());
			}
			log.info ("VAL App Pos:" + APPR_REP_DTO.getWarningsAndInformation());
			if (APPR_REP_DTO.getValnStatus().equals("A")) {
				def QRY2;
				QRY2 = sql.firstRow("select * from ACA.KPF38F upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"'");
				BigDecimal EST_COST = 0;
				EST_COST = QRY2.EST_COST
				log.info ("EST_COST :" + EST_COST);
				GetNowDateTime();
				String QueryUpdate = ("update ACA.KPF38F " +
						"set CIC_STATUS = '2', COMPL_BY = '"+EMP_ID+"', COMPL_DATE = '" +strCrDT+ "', ACT_COST = ? " +
						"where upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"'");
				sql.execute(QueryUpdate,[EST_COST]);
			}
			if (APPR_REP_DTO.getValnStatus().equals("U")) {
				GetNowDateTime();
				String QueryUpdate = ("update ACA.KPF38F " +
						"set CIC_STATUS = 'U' " +
						"where upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"'");
				sql.execute(QueryUpdate);
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
	private String MOD_ITM_VAL(String CNT_NO,String CIC_ITEM_NO,String VAL_NO,BigDecimal ACT_VAL,String CIC_TYPE,String EMP_ID,String POSITION,String CIC_NO){
		String MESSAGE = "";
		try
		{
			log.info ("MOD_ITM_VAL:");
			ValuationsServiceModItemsReplyDTO MOD_REP_DTO = service.get("Valuations").modItems({
				if (CIC_TYPE.equals("LS")) {
					it.calculatedType = "A"
					it.calculatedMethod = "V"
					it.actualValue = ACT_VAL
					//it.actualPcQuantity = 0
				}else {
					it.calculatedType = "A"
					it.calculatedMethod = "Q"
					it.actualPcQuantity = ACT_VAL
					//it.actualValue = 0
				}

				log.info ("MODIFY WH TAX:");
				def QRY1 = sql.firstRow("select * from msf071 " +
						"where ENTITY_TYPE = 'CIW' and ENTITY_VALUE = '"+DST+CNT_NO.trim()+CIC_ITEM_NO.trim()+"' and REF_NO = '001' and SEQ_NUM = '001'");
				log.info ("FIND WHTAX CODE: " + QRY1);
				if(!QRY1.equals(null)) {
					it.whTaxCode = QRY1.REF_CODE.trim()
				}else {
					it.whTaxCode = "";
				}
				it.portionNo = CIC_ITEM_NO.substring(0, 2)
				it.elementNo = CIC_ITEM_NO.substring(2, 4)
				it.categoryNo = CIC_ITEM_NO.substring(4, 6)
				it.contractNo = CNT_NO
				it.valuationNo = VAL_NO
				it.valnTypeFlag = "N"
			},false)
			log.info ("MODIFY ADD TAX:");
			ValuationsServiceModifyAddTaxReplyDTO MOD_ATAX_REP_DTO = service.get("Valuations").modifyAddTax({
				it.contractNo = CNT_NO
				it.valuationNo = VAL_NO
				it.valnTypeFlag = "N"
				it.portionNo = CIC_ITEM_NO.substring(0, 2)
				it.elementNo = CIC_ITEM_NO.substring(2, 4)
				it.categoryNo = CIC_ITEM_NO.substring(4, 6)
				it.expandATax = true
				def QRY1 = sql.firstRow("select * from msf071 " +
						"where ENTITY_TYPE = 'CIA' and ENTITY_VALUE = '"+DST+CNT_NO.trim()+CIC_ITEM_NO.trim()+"' and REF_NO = '001' and SEQ_NUM = '001'");
				log.info ("FIND ADD TAX CODE: " + QRY1);
				if(!QRY1.equals(null)) {
					it.expandATaxCode = QRY1.REF_CODE.trim()
				}else {
					it.expandATaxCode = "";
				}
			},false)
			//log.info ("CONTRACT No:" + MOD_REP_DTO.getContractNo());
			//log.info ("VAL No:" + MOD_REP_DTO.getValuationNo());
			GetNowDateTime();
			String QueryUpdate = ("update ACA.KPF38F " +
					"set ACT_BY = '"+EMP_ID+"', ACTUAL_DATE = '" +strCrDT+ "' " +
					"where upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_NO = '"+CIC_NO+"'");
			sql.execute(QueryUpdate);

			//ContractCostingServiceReadReplyDTO
			log.info ("CONTRACT COSTING READ:");
			if (CIC_TYPE.equals("WO")) {
				String QryDel = ("delete msf38d " +
						"where upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and trim(valn_no) = '"+VAL_NO.trim()+"' and PORTION_NO = '"+CIC_ITEM_NO.substring(0, 2)+"' " +
						"and ELEMENT_NO = '"+CIC_ITEM_NO.substring(2, 4)+"' and CATEGORY_NO = '"+CIC_ITEM_NO.substring(4, 6)+"' and ALLOC_COUNT <> '01' ");
				sql.execute(QryDel);
				def QRY2 = sql.firstRow("select * from msf38c " +
						" where upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO.trim()+"')) and VALN_NO = '"+VAL_NO.trim()+"' and PORTION_NO = '"+CIC_ITEM_NO.substring(0, 2)+"' " +
						" and ELEMENT_NO = '"+CIC_ITEM_NO.substring(2, 4)+"' and CATEGORY_NO = '"+CIC_ITEM_NO.substring(4, 6)+"' ");
				//log.info ("FIND ACT VAL: " + QRY2);
				if(!QRY2.equals(null)) {
					log.info ("UPD ALLOCATED VAL: " + QRY2.ACT_VAL);
					String QryUpd = ("update msf38d " +
							"set ALLOC_VAL = " + QRY2.ACT_VAL + " " +
							"where upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and trim(valn_no) = '"+VAL_NO.trim()+"' and PORTION_NO = '"+CIC_ITEM_NO.substring(0, 2)+"' " +
							"and ELEMENT_NO = '"+CIC_ITEM_NO.substring(2, 4)+"' and CATEGORY_NO = '"+CIC_ITEM_NO.substring(4, 6)+"' and ALLOC_COUNT = '01' ");
					sql.execute(QryUpd);
				}
			}
			/*
			 ContractCostingServiceRetrieveReplyCollectionDTO RET_COST_REP_DTO = service.get("ContractCosting").retrieve({
			 it.categoryNo = CIC_ITEM_NO.substring(4, 6)
			 it.contractNo = CNT_NO.trim()
			 it.costingInd = "V";
			 it.element = CIC_ITEM_NO.substring(2, 4)
			 it.portion = CIC_ITEM_NO.substring(0, 2)
			 it.valuationNo = VAL_NO.trim()
			 },false)
			 */
			String StrSQL = "select * from msf38d where upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO.trim()+"')) and VALN_NO = '"+VAL_NO.trim()+"' and PORTION_NO = '"+CIC_ITEM_NO.substring(0, 2)+"' " +
					"and ELEMENT_NO = '"+CIC_ITEM_NO.substring(2, 4)+"' and CATEGORY_NO = '"+CIC_ITEM_NO.substring(4, 6)+"' and ALLOC_REC_TYPE = 'V'";
			log.info ("StrSQL : " + StrSQL);

			sql.eachRow(StrSQL, {row ->

				ContractCostingServiceModifyReplyDTO MOD_COST_REP_DTO = service.get("ContractCosting").modify({
					it.contractNo = CNT_NO.trim()
					it.valuationNo = VAL_NO.trim()
					it.portion = CIC_ITEM_NO.substring(0, 2)
					it.element = CIC_ITEM_NO.substring(2, 4)
					it.categoryNo = CIC_ITEM_NO.substring(4, 6)
					log.info ("WO_NO_GLBL :" + WO_NO_GLBL);
					if(!WO_NO_GLBL.trim().equals("")) {

						def QRY1 = sql.firstRow("select trim(substr(DSTRCT_ACCT_CODE,5,28)) ACCOUNT_CODE from msf620 where DSTRCT_CODE = '"+DST+"' and trim(work_order) = trim('"+WO_NO_GLBL+"')");
						log.info ("FIND ACCT CODE: " + QRY1);
						if(!QRY1.equals(null)) {
							it.accountCode = QRY1.ACCOUNT_CODE.trim()
						}
					}else {
						it.accountCode = row.GL_ACCOUNT;
					}

					WorkOrderDTO WO_DTO = new WorkOrderDTO();
					WO_DTO.setNo(WO_NO_GLBL.substring(2, 8));
					WO_DTO.setPrefix(WO_NO_GLBL.substring(0, 2));
					it.workOrder = WO_DTO
					log.info ("it.workOrder:" + it.workOrder);
					it.allocCount = new BigDecimal(row.ALLOC_COUNT);
					it.allocationDistrict = row.ALLOC_DSTRCT;
					if (CIC_TYPE.trim().equals("WO")) {
						it.allocatedPercent = 100;
						def QRY2 = sql.firstRow("select * from msf38c " +
								" where upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO.trim()+"')) and VALN_NO = '"+VAL_NO.trim()+"' and PORTION_NO = '"+CIC_ITEM_NO.substring(0, 2)+"' " +
								" and ELEMENT_NO = '"+CIC_ITEM_NO.substring(2, 4)+"' and CATEGORY_NO = '"+CIC_ITEM_NO.substring(4, 6)+"' ");
						log.info ("FIND ACT VAL: " + QRY2);
						if(!QRY2.equals(null)) {
							log.info ("MOD ALLOCATED VAL: " + QRY2.ACT_VAL);
							it.allocatedValue = new BigDecimal(QRY2.ACT_VAL);
						}
					}else {
						it.allocatedPercent = new BigDecimal(row.ALLOC_PC);
						it.allocatedValue = new BigDecimal(row.ALLOC_VAL);
					}
					it.costingInd = "V";

				},false)
			})
			/*
			 Integer i;
			 ContractCostingServiceRetrieveReplyDTO[] READ_COST_REP_DTO = RET_COST_REP_DTO.getReplyElements();
			 log.info ("COSTING COUNT :" + READ_COST_REP_DTO.length);
			 for(i=0;i<READ_COST_REP_DTO.length;i++) {
			 log.info ("index :" + i.toString());
			 log.info ("COSTING district :" + READ_COST_REP_DTO[i].getAllocationDistrict());
			 log.info ("COSTING account :" + READ_COST_REP_DTO[i].getAccountCode());
			 log.info ("COSTING percentage :" + READ_COST_REP_DTO[i].getAllocatedPercent());
			 log.info ("COSTING value :" + READ_COST_REP_DTO[i].getAllocatedValue());
			 ContractCostingServiceModifyReplyDTO MOD_COST_REP_DTO = service.get("ContractCosting").modify({
			 it.contractNo = CNT_NO.trim()
			 it.valuationNo = VAL_NO.trim()
			 it.portion = CIC_ITEM_NO.substring(0, 2)
			 it.element = CIC_ITEM_NO.substring(2, 4)
			 it.categoryNo = CIC_ITEM_NO.substring(4, 6)
			 log.info ("WO_NO_GLBL :" + WO_NO_GLBL);
			 if(!WO_NO_GLBL.trim().equals("")) {
			 def QRY1 = sql.firstRow("select trim(substr(DSTRCT_ACCT_CODE,5,28)) ACCOUNT_CODE from msf620 where DSTRCT_CODE = '"+DST+"' and trim(work_order) = trim('"+WO_NO_GLBL+"')");
			 log.info ("FIND WHTAX CODE: " + QRY1);
			 if(!QRY1.equals(null)) {
			 it.accountCode = QRY1.ACCOUNT_CODE.trim()
			 }
			 }else {
			 it.accountCode = READ_COST_REP_DTO[i].getAccountCode();
			 }
			 WorkOrderDTO WO_DTO = new WorkOrderDTO();
			 WO_DTO.setNo(WO_NO_GLBL.substring(2, 8));
			 WO_DTO.setPrefix(WO_NO_GLBL.substring(0, 2));
			 it.workOrder = WO_DTO
			 log.info ("it.workOrder:" + it.workOrder);
			 it.allocCount = READ_COST_REP_DTO[i].getAllocCount();
			 it.allocationDistrict = READ_COST_REP_DTO[i].getAllocationDistrict();
			 it.allocatedPercent = READ_COST_REP_DTO[i].getAllocatedPercent();
			 it.allocatedValue = READ_COST_REP_DTO[i].getAllocatedValue();
			 it.costingInd = READ_COST_REP_DTO[i].getCostingInd();
			 },false)
			 }
			 */
			log.info ("CONTRACT COSTING READ - END:");
			/*
			 log.info ("CONTRACT COSTING MODIFY:");
			 ContractCostingServiceModifyReplyDTO MOD_COST_REP_DTO = service.get("ContractCosting").modify({
			 it.contractNo = CNT_NO.trim()
			 it.valuationNo = VAL_NO.trim()
			 it.portion = CIC_ITEM_NO.substring(0, 2)
			 it.element = CIC_ITEM_NO.substring(2, 4)
			 it.categoryNo = CIC_ITEM_NO.substring(4, 6)
			 it.allocatedPercent = 100;
			 it.costingInd = "V";
			 it.workOrder = WO_NO_GLBL
			 it.allocCount = 1
			 },false)
			 log.info ("CONTRACT COSTING MODIFYT - END:");
			 */
			/*
			 log.info ("CONTRACT QESTION PRECOMMIT:");
			 ContractQuestionAnswersServiceResult QUE_PRE_COM_REP_DTO = service.get("ContractQuestionAnswers").preCommit({
			 ContractNo ObjCon = new ContractNo()
			 ObjCon.setValue(CNT_NO.trim())
			 it.contractNumber = ObjCon
			 ValnNo ObjValn = new ValnNo()
			 ObjValn.setValue(VAL_NO.trim())
			 it.valuationNumber = ObjValn
			 ContEventType ObjContEventTy = new ContEventType()
			 ObjContEventTy.setValue("V")
			 it.contractEventType = ObjContEventTy
			 },false)
			 log.info ("COTARCT QESTION PRECOMMIT - END:");
			 */
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
	private String DEL_ITM_VAL(String CNT_NO,String CIC_ITEM_NO,String VAL_NO,BigDecimal ACT_VAL,String CIC_TYPE,String EMP_ID,String POSITION,String CIC_NO){
		String MESSAGE = "";
		try
		{
			log.info ("DEL_ITM_VAL:");
			ValuationsServiceModItemsReplyDTO MOD_REP_DTO = service.get("Valuations").modItems({
				if (CIC_TYPE.equals("LS")) {
					it.calculatedType = "A"
					it.calculatedMethod = "V"
					it.actualValue = 0
					//it.actualPcQuantity = 0
				}else {
					it.calculatedType = "A"
					it.calculatedMethod = "Q"
					it.actualPcQuantity = 0
					//it.actualValue = 0
				}

				/*
				 log.info ("MODIFY WH TAX:");
				 def QRY1 = sql.firstRow("select * from msf071 " +
				 "where ENTITY_TYPE = 'CIW' and ENTITY_VALUE = '"+DST+CNT_NO.trim()+CIC_ITEM_NO.trim()+"' and REF_NO = '001' and SEQ_NUM = '001'");
				 log.info ("FIND WHTAX CODE: " + QRY1);
				 if(!QRY1.equals(null)) {
				 it.whTaxCode = QRY1.REF_CODE.trim()
				 }else {
				 it.whTaxCode = "";
				 }
				 */
				it.portionNo = CIC_ITEM_NO.substring(0, 2)
				it.elementNo = CIC_ITEM_NO.substring(2, 4)
				it.categoryNo = CIC_ITEM_NO.substring(4, 6)
				it.contractNo = CNT_NO
				it.valuationNo = VAL_NO
				it.valnTypeFlag = "N"
			},false)
			//log.info ("MODIFY ADD TAX:");

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