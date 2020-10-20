/**
 * @EMS 2019
 *
 * 20190901 - a9ra5213 - Ricky Afriano - KPC UPGRADE Ellipse 8
 *            Initial Coding - Custom Manual Depreciation 
 **/
package KPC

import javax.naming.InitialContext
import com.mincom.ellipse.app.security.SecurityToken
import com.mincom.ellipse.ejp.exceptions.InteractionNotAuthorisedException
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
import java.math.RoundingMode;
import java.text.DecimalFormat
import com.mincom.enterpriseservice.ellipse.*
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import com.mincom.ellipse.errors.Error;
import com.mincom.ellipse.errors.UnlocalisedError
import com.mincom.ellipse.errors.UnlocalisedMessage
import com.mincom.ellipse.errors.UnlocalisedWarning
import com.mincom.ellipse.errors.CobolMessages;
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsService
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsServiceCreateReplyDTO
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsServiceCreateRequestDTO
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsServiceDeleteReplyDTO


public class ell685GridRate extends GenericScriptPlugin implements GenericScriptExecuteForCollection, GenericScriptUpdate, GenericScriptCreate, GenericScriptDelete{
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
	
	
	GenericScriptResults executeForCollection(SecurityToken securityToken, RequestAttributes requestAttributes,
		Integer maxNumberOfObjects, RestartAttributes restartAttributes) throws FatalException{
			log.info("ell685GridRate executeForCollection : " + version );
			def results = new GenericScriptResults();
			
			RequestAttributes reqAtt = requestAttributes;
			String parAssetNo = reqAtt.getAttributeStringValue("assetNumber");
			String parAssetType = reqAtt.getAttributeStringValue("assetTy");
			String parSubAssetNo = reqAtt.getAttributeStringValue("subAssetNo");
			String parBookType = reqAtt.getAttributeStringValue("bookTy");
			String parStartPeriod = reqAtt.getAttributeStringValue("startPeriod");
			
			if (parAssetNo.equals(null)) {
				parAssetNo = "";
			}else {
				parAssetNo = " and upper(trim(a.ASSET_NO)) = upper(trim('" + String.format("%1\$" + "12" + "s", parAssetNo) + "')) ";
			}
			
			if (parSubAssetNo.equals(null)) {
				parSubAssetNo = "";
			}else {
				parSubAssetNo = " and upper(trim(a.SUB_ASSET_NO)) = upper(trim('" + parSubAssetNo + "')) ";
			}
			
			if (parAssetType.equals(null)) {
				parAssetType = "";
			}else {
				parAssetType = " and upper(trim(a.ASSET_TY)) = upper(trim('" + parAssetType + "')) ";
			}
			
			if (parBookType.equals(null)) {
				parBookType = "";
			}else {
				parBookType = " and upper(trim(a.BOOK_TY)) = upper(trim('" + parBookType + "')) ";
			}
			/*
			if (parStartPeriod.equals(null)) {
				parStartPeriod = "";
			}else {
				parStartPeriod = " and upper(trim(a.START_PERIOD)) = upper(trim('" + parStartPeriod + "')) ";
			}
			*/
			String StrSQL = "";
			log.info("maxNumberOfObjects : " + maxNumberOfObjects );
			DecimalFormat df = new DecimalFormat("#,##0.00;-#,##0.00");
			if (restartAttributes.equals(null)){
				   StrSQL = "select row_number () over(order by a.ASSET_NO) NO,a.*,trim(b.SUB_ASSET_DESC) SUB_ASSET_DESC from (select " +
								"substr(ENTITY_VALUE,1,4) DSTRCT_CODE,substr(ENTITY_VALUE,5,12) ASSET_NO, " +
								"substr(ENTITY_VALUE,17,6) SUB_ASSET_NO,substr(ENTITY_VALUE,23,1) ASSET_TY, " +
								"substr(ENTITY_VALUE,24,2) BOOK_TY,trim(REF_CODE) PCT,SEQ_NUM " +
								"from msf071 where ENTITY_TYPE = 'CMD' and REF_NO = '002') a " +
								"left outer join msf685 b on (a.DSTRCT_CODE = b.DSTRCT_CODE and a.ASSET_NO = b.ASSET_NO and a.SUB_ASSET_NO = b.SUB_ASSET_NO) " +
							   "where a.DSTRCT_CODE = '"+securityToken.getDistrict()+"' " + parAssetNo + parSubAssetNo + parAssetType + parBookType +
							   "ORDER BY a.ASSET_NO,a.SUB_ASSET_NO,a.SEQ_NUM OFFSET 0 ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY" ;
				   log.info ("StrSQL : " + StrSQL);
				   sql.eachRow(StrSQL, {
					   GenericScriptResult result = new GenericScriptResult()
					   result.addAttribute("yearPercent", "YEAR " + it.NO);
					   result.addAttribute("pct", it.PCT + "%");
					   result.addAttribute("LAST_ROW", maxNumberOfObjects.toString());
					   results.add(result);
				   })
			}else {
				   log.info("restartAttributes : " + restartAttributes.getAttributeStringValue("LAST_ROW") );
				   Integer MaxInst = Integer.parseInt(restartAttributes.getAttributeStringValue("LAST_ROW"));
				   
				   StrSQL = "select row_number () over(order by a.ASSET_NO) NO,a.*,trim(b.SUB_ASSET_DESC) SUB_ASSET_DESC from (select " +
								"substr(ENTITY_VALUE,1,4) DSTRCT_CODE,substr(ENTITY_VALUE,5,12) ASSET_NO, " +
								"substr(ENTITY_VALUE,17,6) SUB_ASSET_NO,substr(ENTITY_VALUE,23,1) ASSET_TY, " +
								"substr(ENTITY_VALUE,24,2) BOOK_TY,trim(REF_CODE) PCT,SEQ_NUM " +
								"from msf071 where ENTITY_TYPE = 'CMD' and REF_NO = '002') a " +
								"left outer join msf685 b on (a.DSTRCT_CODE = b.DSTRCT_CODE and a.ASSET_NO = b.ASSET_NO and a.SUB_ASSET_NO = b.SUB_ASSET_NO) " +
							   "where a.DSTRCT_CODE = '"+securityToken.getDistrict()+"' " + parAssetNo + parSubAssetNo + parAssetType + parBookType +
							   "ORDER BY a.ASSET_NO,a.SUB_ASSET_NO,a.SEQ_NUM OFFSET "+MaxInst.toString()+" ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY" ;
				   log.info ("StrSQL : " + StrSQL);
				   MaxInst = MaxInst + maxNumberOfObjects
				   sql.eachRow(StrSQL, {
					   GenericScriptResult result = new GenericScriptResult()
					   result.addAttribute("yearPercent", "YEAR " + it.NO);
					   result.addAttribute("pct", it.PCT + "%");
					   result.addAttribute("LAST_ROW", MaxInst.toString());
					   results.add(result);
				   })
			}
			
			return results;
	}
	public GenericScriptResults create(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings)
	throws FatalException {
		log.info("Create ell685GridRate : " + version )
		GenericScriptResults results = new GenericScriptResults();
		GenericScriptResult result = new GenericScriptResult();
		RequestAttributes reqAtt = requestAttributes[0];
		
		String assetNo = "";
		if (!reqAtt.getAttributeStringValue("assetNumber").equals(null)) {
			assetNo = reqAtt.getAttributeStringValue("assetNumber");
		}else {
			assetNo = reqAtt.getAttributeStringValue("assetNo");
		}
		assetNo = String.format("%-12s", assetNo )
		String subAssetNo = "";
		if (!reqAtt.getAttributeStringValue("subAssetNo").equals(null)) {
			subAssetNo = reqAtt.getAttributeStringValue("subAssetNo");
		}else {
			subAssetNo = reqAtt.getAttributeStringValue("parSubAssetNo");
		}
		subAssetNo = String.format("%6s", subAssetNo ).replace(' ', '0')
		String bookTy = "";
		if (!reqAtt.getAttributeStringValue("bookTy").equals(null)) {
			bookTy = reqAtt.getAttributeStringValue("bookTy");
		}else {
			bookTy = reqAtt.getAttributeStringValue("parBookTy");
		}
		String startPeriod = "";
		if (!reqAtt.getAttributeStringValue("startPeriod").equals(null)) {
			startPeriod = reqAtt.getAttributeStringValue("startPeriod");
		}else {
			startPeriod = reqAtt.getAttributeStringValue("parStartPeriod");
		}
		
		String assetTy = "";
		if (!reqAtt.getAttributeStringValue("assetTy").equals(null)) {
			assetTy = reqAtt.getAttributeStringValue("assetTy");
		}else {
			assetTy = reqAtt.getAttributeStringValue("parAssetTy");
		}
		
		log.info("assetNo : " + assetNo );
		log.info("subAssetNo : " + subAssetNo );
		log.info("bookTy : " + bookTy );
		log.info("startPeriod : " + startPeriod );
		log.info("assetTy : " + assetTy );
		
		if (assetNo.equals(null) || assetNo.equals("")) {
			StrErr = "ASSET NUMBER REQUIRED!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("assetNumber")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		
		if (subAssetNo.equals(null) || subAssetNo.equals("")) {
			StrErr = "SUB ASSET NUMBER REQUIRED!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("subAssetNo")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		
		if (bookTy.equals(null) || bookTy.equals("")) {
			StrErr = "BOOK TYPE REQUIRED!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("bookTy")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		
		def QRY0;
		QRY0 = sql.firstRow("select * from msf685 where dstrct_code = '"+securityToken.getDistrict()+"' and trim(asset_no) = upper(trim('"+assetNo+"')) and trim(SUB_ASSET_NO) = upper(trim('"+subAssetNo+"')) and trim(ASSET_TY) = trim('"+assetTy+"')");
		if(QRY0.equals(null)) {
			StrErr = "INVALID ASSET NO / SUB ASSET NUMBER / ASSET TYPE!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			//err.setFieldId("CRE_CNT_NO")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		QRY0 = sql.firstRow("select * from msf010 where table_type = 'TX' and trim(table_code) = upper(trim('"+bookTy+"')) ");
		if(QRY0.equals(null)) {
			StrErr = "INVALID BOOK TYPE!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			//err.setFieldId("CRE_CNT_NO")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		
		if (startPeriod.equals(null) || startPeriod.equals("")) {
			StrErr = "START PERIOD REQUIRED!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			//err.setFieldId("CRE_CNT_NO")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}else {
			if(isNumeric(startPeriod).equals(false)) {
				StrErr = "START PERIOD SHOULD BE NUMERIC!"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				//err.setFieldId("CRE_CNT_NO")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}else {
				Integer minStrPer = 200401;
				Integer maxStrPer = 220001;
				Integer intStrPer = Integer.parseInt(startPeriod);
				if (intStrPer < minStrPer || intStrPer > 220001) {
					StrErr = "START PERIOD BETWEEN 200401 AND 220001!"
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					//err.setFieldId("CRE_CNT_NO")
					result.addError(err)
					results.add(result)
					RollErrMes();
					return results
				}
			}
		}
		
		if(assetTy.equals(null) || assetTy.equals("")) {
			StrErr = "ASSET TYPE REQUIRED!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("assetTy")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}else {
			if(!assetTy.equals("A") && !assetTy.equals("E")) {
				StrErr = "INVALID ASSET TYPE!"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("assetTy")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
		}
		
		requestAttributes.eachWithIndex {reqAttItem, index ->
			String pct = "";
			if (!reqAttItem.getAttributeStringValue("pct").equals(null)) {
				pct = reqAttItem.getAttributeStringValue("pct");
			}else {
				pct = "";
			}
			log.info("pct : " + pct );
			if (pct.equals("")) {
				StrErr = "PERCENTAGE REQUIRED!"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("pct")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
			if (isNumeric2(pct).equals(false)) {
				StrErr = "INVALID NUMBER PERCENTAGE!"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("pct")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
			
			String StrSeqNum = "";
			QRY0 = sql.firstRow("select to_number(max(SEQ_NUM)) + 1 MAX_SEQ_NUM from msf071 where ENTITY_TYPE = 'CMD' and trim(ENTITY_VALUE) = upper(trim('"+securityToken.getDistrict()+assetNo+subAssetNo+assetTy+bookTy+"')) and ref_no = '002' ");
			if(!QRY0.MAX_SEQ_NUM.equals(null)) {
				StrSeqNum = (String.format("%1\$" + "3" + "s", QRY0.MAX_SEQ_NUM)).replace(" ", "0")
			}else {
				StrSeqNum = "001"
			}
			log.info ("StrSeqNum : " + StrSeqNum);
			
			QRY0 = sql.firstRow("select sum(case when to_number(trim(REF_CODE)) is null then 0 else to_number(trim(REF_CODE)) end) ACT_PCT from msf071 where ENTITY_TYPE = 'CMD' and REF_NO = '002' and trim(ENTITY_VALUE) = trim('"+securityToken.getDistrict()+assetNo+subAssetNo+assetTy+bookTy+"') ");
			log.info ("QRY ACT PCT : " + QRY0);
			if(!QRY0.ACT_PCT.equals(null)) {
				BigDecimal actPct = QRY0.ACT_PCT;
				Float remPct1 = Float.parseFloat(pct);
				BigDecimal remPct = new BigDecimal(remPct1);
				remPct = remPct.setScale(2, RoundingMode.HALF_UP);
				log.info ("TOTAL PCT : " + (actPct + remPct));
				if ((actPct + remPct) > 100.00) {
					StrErr = "TOTAL PERCENTAGE MUST BE 100.00!"
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					err.setFieldId("pct")
					result.addError(err)
					results.add(result)
					RollErrMes();
					return results
				}
			}
			
			String EMP_ID = GetUserEmpID(securityToken.getUserId(), securityToken.getDistrict())
			try
			{
				GetNowDateTime();
				String QueryInsert = (
					"Insert into MSF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY) values ('CMD','"+securityToken.getDistrict()+assetNo+subAssetNo+assetTy+bookTy+"','002','"+StrSeqNum+"','"+strCrDT+"','"+strCrTM+"','"+EMP_ID+"','"+pct+"','            ')");
				sql.execute(QueryInsert);
			} catch (Exception  e) {
				log.info ("Exception is : " + e);
				log.info ("INSERT EXCEPTION MSF071 CMD:");
				StrErr = "INSERT EXCEPTION MSF071 CMD!"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				//err.setFieldId("CRE_CNT_NO")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
		}
				
		result.addAttribute("assetNumber", assetNo);
		result.addAttribute("subAssetNo", subAssetNo);
		result.addAttribute("bookTy", bookTy);
		
		results.add(result);
		return results;
	}
	public GenericScriptResults update(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings)
	throws FatalException {
		log.info("Update ell685GridRate : " + version );
		GenericScriptResults results = new GenericScriptResults();
		GenericScriptResult result = new GenericScriptResult();
		RequestAttributes reqAtt = requestAttributes[0];
		
		String assetNo = "";
		if (!reqAtt.getAttributeStringValue("assetNumber").equals(null)) {
			assetNo = reqAtt.getAttributeStringValue("assetNumber");
		}else {
			assetNo = reqAtt.getAttributeStringValue("assetNo");
		}
		assetNo = String.format("%-12s", assetNo )
		String subAssetNo = "";
		if (!reqAtt.getAttributeStringValue("subAssetNo").equals(null)) {
			subAssetNo = reqAtt.getAttributeStringValue("subAssetNo");
		}else {
			subAssetNo = reqAtt.getAttributeStringValue("parSubAssetNo");
		}
		subAssetNo = String.format("%6s", subAssetNo ).replace(' ', '0')
		String bookTy = "";
		if (!reqAtt.getAttributeStringValue("bookTy").equals(null)) {
			bookTy = reqAtt.getAttributeStringValue("bookTy");
		}else {
			bookTy = reqAtt.getAttributeStringValue("parBookTy");
		}
		String startPeriod = "";
		if (!reqAtt.getAttributeStringValue("startPeriod").equals(null)) {
			startPeriod = reqAtt.getAttributeStringValue("startPeriod");
		}else {
			startPeriod = reqAtt.getAttributeStringValue("parStartPeriod");
		}
		
		String assetTy = "";
		if (!reqAtt.getAttributeStringValue("assetTy").equals(null)) {
			assetTy = reqAtt.getAttributeStringValue("assetTy");
		}else {
			assetTy = reqAtt.getAttributeStringValue("parAssetTy");
		}
		
		log.info("assetNo : " + assetNo );
		log.info("subAssetNo : " + subAssetNo );
		log.info("bookTy : " + bookTy );
		log.info("startPeriod : " + startPeriod );
		log.info("assetTy : " + assetTy );
		
		if (assetNo.equals(null) || assetNo.equals("")) {
			StrErr = "ASSET NUMBER REQUIRED!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("assetNumber")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		
		if (subAssetNo.equals(null) || subAssetNo.equals("")) {
			StrErr = "SUB ASSET NUMBER REQUIRED!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("subAssetNo")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		
		if (bookTy.equals(null) || bookTy.equals("")) {
			StrErr = "BOOK TYPE REQUIRED!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("bookTy")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		
		def QRY0;
		QRY0 = sql.firstRow("select * from msf685 where dstrct_code = '"+securityToken.getDistrict()+"' and trim(asset_no) = upper(trim('"+assetNo+"')) and trim(SUB_ASSET_NO) = upper(trim('"+subAssetNo+"')) and trim(ASSET_TY) = trim('"+assetTy+"')");
		if(QRY0.equals(null)) {
			StrErr = "INVALID ASSET NO / SUB ASSET NUMBER / ASSET TYPE!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			//err.setFieldId("CRE_CNT_NO")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		QRY0 = sql.firstRow("select * from msf010 where table_type = 'TX' and trim(table_code) = upper(trim('"+bookTy+"')) ");
		if(QRY0.equals(null)) {
			StrErr = "INVALID BOOK TYPE!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			//err.setFieldId("CRE_CNT_NO")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		
		if (startPeriod.equals(null) || startPeriod.equals("")) {
			StrErr = "START PERIOD REQUIRED!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			//err.setFieldId("CRE_CNT_NO")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}else {
			if(isNumeric(startPeriod).equals(false)) {
				StrErr = "START PERIOD SHOULD BE NUMERIC!"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				//err.setFieldId("CRE_CNT_NO")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}else {
				Integer minStrPer = 200401;
				Integer maxStrPer = 220001;
				Integer intStrPer = Integer.parseInt(startPeriod);
				if (intStrPer < minStrPer || intStrPer > 220001) {
					StrErr = "START PERIOD BETWEEN 200401 AND 220001!"
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					//err.setFieldId("CRE_CNT_NO")
					result.addError(err)
					results.add(result)
					RollErrMes();
					return results
				}
			}
		}
		
		if(assetTy.equals(null) || assetTy.equals("")) {
			StrErr = "ASSET TYPE REQUIRED!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("assetTy")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}else {
			if(!assetTy.equals("A") && !assetTy.equals("E")) {
				StrErr = "INVALID ASSET TYPE!"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("assetTy")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
		}
		
		String EMP_ID = GetUserEmpID(securityToken.getUserId(), securityToken.getDistrict())
		
		requestAttributes.eachWithIndex {reqAttItem, index ->
			String yearPercent = ""
			if (!reqAttItem.getAttributeStringValue("yearPercent").equals(null)) {
				yearPercent = reqAttItem.getAttributeStringValue("yearPercent");
				yearPercent = (String.format("%1\$" + "3" + "s", yearPercent.replace("YEAR ", ""))).replace(" ", "0");
				
			}else {
				yearPercent = "";
			}
			log.info("yearPercent : " + yearPercent );
			if (yearPercent.equals("")) {
				StrErr = "YEAR PERCENTAGE REQUIRED!"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("yearPercent")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
			
			String pct = "";
			if (!reqAttItem.getAttributeStringValue("pct").equals(null)) {
				pct = reqAttItem.getAttributeStringValue("pct");
				pct = pct.replace("%", "")
			}else {
				pct = "";
			}
			log.info("pct : " + pct );
			if (pct.equals("")) {
				StrErr = "PERCENTAGE REQUIRED!"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("pct")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
			if (isNumeric2(pct).equals(false)) {
				StrErr = "INVALID NUMBER PERCENTAGE!"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("pct")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
			QRY0 = sql.firstRow("select * from msf071 where ENTITY_TYPE = 'CMD' and trim(ENTITY_VALUE) = upper(trim('"+securityToken.getDistrict()+assetNo+subAssetNo+assetTy+bookTy+"')) and ref_no = '002' and seq_num = '"+yearPercent+"'");
			//log.info ("select * from msf071 where ENTITY_TYPE = 'CMD' and trim(ENTITY_VALUE) = upper(trim('"+securityToken.getDistrict()+assetNo+subAssetNo+assetTy+bookTy+"')) and ref_no = '002' and seq_num = '"+yearPercent+"'");
			//log.info ("QRY0 : " + QRY0);
			if(QRY0.equals(null)) {
				StrErr = "INVALID YEAR PERCENTAGE!"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("yearPercent")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
			try
			{
				String QueryUpdate = (
					"update msf071 set ref_code = '"+pct+"' where ENTITY_TYPE = 'CMD' and trim(ENTITY_VALUE) = trim('"+securityToken.getDistrict()+assetNo+subAssetNo+assetTy+bookTy+"') and ref_no = '002' and trim(seq_num) = trim('"+yearPercent+"')");
				sql.execute(QueryUpdate);
			} catch (Exception  e) {
				log.info ("Exception is : " + e);
				log.info ("UPDATE EXCEPTION MSF071 CMD:");
				StrErr = "UPDATE EXCEPTION MSF071 CMD!"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				//err.setFieldId("CRE_CNT_NO")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
		}
		
		
		result.addAttribute("assetNumber", assetNo);
		result.addAttribute("subAssetNo", subAssetNo);
		result.addAttribute("bookTy", bookTy);
		
		results.add(result);
		return results;
	}
	public GenericScriptResults delete(SecurityToken securityToken, List<RequestAttributes> requestAttributes, Boolean returnWarnings)
	throws FatalException {
		log.info("Delete ell685GridRate : " + version )
		GenericScriptResults results = new GenericScriptResults();
		GenericScriptResult result = new GenericScriptResult();
		RequestAttributes reqAtt = requestAttributes[0];
		
		String assetNo = "";
		if (!reqAtt.getAttributeStringValue("assetNumber").equals(null)) {
			assetNo = reqAtt.getAttributeStringValue("assetNumber");
		}else {
			assetNo = reqAtt.getAttributeStringValue("assetNo");
		}
		assetNo = String.format("%-12s", assetNo )
		String subAssetNo = "";
		if (!reqAtt.getAttributeStringValue("subAssetNo").equals(null)) {
			subAssetNo = reqAtt.getAttributeStringValue("subAssetNo");
		}else {
			subAssetNo = reqAtt.getAttributeStringValue("parSubAssetNo");
		}
		subAssetNo = String.format("%6s", subAssetNo ).replace(' ', '0')
		String bookTy = "";
		if (!reqAtt.getAttributeStringValue("bookTy").equals(null)) {
			bookTy = reqAtt.getAttributeStringValue("bookTy");
		}else {
			bookTy = reqAtt.getAttributeStringValue("parBookTy");
		}
		String startPeriod = "";
		if (!reqAtt.getAttributeStringValue("startPeriod").equals(null)) {
			startPeriod = reqAtt.getAttributeStringValue("startPeriod");
		}else {
			startPeriod = reqAtt.getAttributeStringValue("parStartPeriod");
		}
		
		String assetTy = "";
		if (!reqAtt.getAttributeStringValue("assetTy").equals(null)) {
			assetTy = reqAtt.getAttributeStringValue("assetTy");
		}else {
			assetTy = reqAtt.getAttributeStringValue("parAssetTy");
		}
		
		log.info("assetNo : " + assetNo );
		log.info("subAssetNo : " + subAssetNo );
		log.info("bookTy : " + bookTy );
		log.info("startPeriod : " + startPeriod );
		log.info("assetTy : " + assetTy );
		
		if (assetNo.equals(null) || assetNo.equals("")) {
			StrErr = "ASSET NUMBER REQUIRED!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("assetNumber")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		
		if (subAssetNo.equals(null) || subAssetNo.equals("")) {
			StrErr = "SUB ASSET NUMBER REQUIRED!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("subAssetNo")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		
		if (bookTy.equals(null) || bookTy.equals("")) {
			StrErr = "BOOK TYPE REQUIRED!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("bookTy")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		
		def QRY0;
		QRY0 = sql.firstRow("select * from msf685 where dstrct_code = '"+securityToken.getDistrict()+"' and trim(asset_no) = upper(trim('"+assetNo+"')) and trim(SUB_ASSET_NO) = upper(trim('"+subAssetNo+"')) and trim(ASSET_TY) = trim('"+assetTy+"')");
		if(QRY0.equals(null)) {
			StrErr = "INVALID ASSET NO / SUB ASSET NUMBER / ASSET TYPE!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			//err.setFieldId("CRE_CNT_NO")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		QRY0 = sql.firstRow("select * from msf010 where table_type = 'TX' and trim(table_code) = upper(trim('"+bookTy+"')) ");
		if(QRY0.equals(null)) {
			StrErr = "INVALID BOOK TYPE!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			//err.setFieldId("CRE_CNT_NO")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}
		
		if (startPeriod.equals(null) || startPeriod.equals("")) {
			StrErr = "START PERIOD REQUIRED!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			//err.setFieldId("CRE_CNT_NO")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}else {
			if(isNumeric(startPeriod).equals(false)) {
				StrErr = "START PERIOD SHOULD BE NUMERIC!"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				//err.setFieldId("CRE_CNT_NO")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}else {
				Integer minStrPer = 200401;
				Integer maxStrPer = 220001;
				Integer intStrPer = Integer.parseInt(startPeriod);
				if (intStrPer < minStrPer || intStrPer > 220001) {
					StrErr = "START PERIOD BETWEEN 200401 AND 220001!"
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					//err.setFieldId("CRE_CNT_NO")
					result.addError(err)
					results.add(result)
					RollErrMes();
					return results
				}
			}
		}
		
		if(assetTy.equals(null) || assetTy.equals("")) {
			StrErr = "ASSET TYPE REQUIRED!"
			SetErrMes();
			com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
			err.setFieldId("assetTy")
			result.addError(err)
			results.add(result)
			RollErrMes();
			return results
		}else {
			if(!assetTy.equals("A") && !assetTy.equals("E")) {
				StrErr = "INVALID ASSET TYPE!"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("assetTy")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
		}
		
		String EMP_ID = GetUserEmpID(securityToken.getUserId(), securityToken.getDistrict())
		
		requestAttributes.eachWithIndex {reqAttItem, index ->
			String yearPercent = ""
			if (!reqAttItem.getAttributeStringValue("yearPercent").equals(null)) {
				yearPercent = reqAttItem.getAttributeStringValue("yearPercent");
				yearPercent = (String.format("%1\$" + "3" + "s", yearPercent.replace("YEAR ", ""))).replace(" ", "0");
				
			}else {
				yearPercent = "";
			}
			log.info("yearPercent : " + yearPercent );
			if (yearPercent.equals("")) {
				StrErr = "YEAR PERCENTAGE REQUIRED!"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("yearPercent")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
			
			String pct = "";
			if (!reqAttItem.getAttributeStringValue("pct").equals(null)) {
				pct = reqAttItem.getAttributeStringValue("pct");
				pct = pct.replace("%", "")
			}else {
				pct = "";
			}
			log.info("pct : " + pct );
			if (pct.equals("")) {
				StrErr = "PERCENTAGE REQUIRED!"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("pct")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
			if (isNumeric2(pct).equals(false)) {
				StrErr = "INVALID NUMBER PERCENTAGE!"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("pct")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
			QRY0 = sql.firstRow("select * from msf071 where ENTITY_TYPE = 'CMD' and trim(ENTITY_VALUE) = upper(trim('"+securityToken.getDistrict()+assetNo+subAssetNo+assetTy+bookTy+"')) and ref_no = '002' and seq_num = '"+yearPercent+"'");
			//log.info ("select * from msf071 where ENTITY_TYPE = 'CMD' and trim(ENTITY_VALUE) = upper(trim('"+securityToken.getDistrict()+assetNo+subAssetNo+assetTy+bookTy+"')) and ref_no = '002' and seq_num = '"+yearPercent+"'");
			//log.info ("QRY0 : " + QRY0);
			if(QRY0.equals(null)) {
				StrErr = "INVALID YEAR PERCENTAGE!"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("yearPercent")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
			try
			{
				String QueryDelete = (
					"delete msf071 where ENTITY_TYPE = 'CMD' and trim(ENTITY_VALUE) = trim('"+securityToken.getDistrict()+assetNo+subAssetNo+assetTy+bookTy+"') and ref_no = '002' and trim(seq_num) = trim('"+yearPercent+"')");
				sql.execute(QueryDelete);
			} catch (Exception  e) {
				log.info ("Exception is : " + e);
				log.info ("INSERT EXCEPTION MSF071 CMD:");
				StrErr = "INSERT EXCEPTION MSF071 CMD!"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				//err.setFieldId("CRE_CNT_NO")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}
		}
		
		
		result.addAttribute("assetNumber", assetNo);
		result.addAttribute("subAssetNo", subAssetNo);
		result.addAttribute("bookTy", bookTy);
		
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
	public boolean isNumeric(String str) {
		try {
			str = str.replace(",", "")
			Integer.parseInt(str);
			//Float.parseFloat(str);
			return true;
		  }
		  catch (NumberFormatException e) {
			// s is not numeric
			return false;
		  }
	}
	public boolean isNumeric2(String str) {
		try {
			str = str.replace(",", "")
			//Integer.parseInt(str);
			Float.parseFloat(str);
			return true;
		  }
		  catch (NumberFormatException e) {
			// s is not numeric
			return false;
		  }
	}
}
