package KPC.HOOKS
/**
 * @EMS Mar 2019
 *
 * 20190328 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Initial Coding - Forward Fit from Ellipse 5 to validate Full Account code when create WO
 **/

import javax.naming.InitialContext

import com.mincom.ellipse.attribute.Attribute
import com.mincom.enterpriseservice.ellipse.*
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.ellipse.service.m3620.work.WorkService
import com.mincom.ellipse.types.m3620.instances.WorkDTO;
import com.mincom.ellipse.service.ServiceDTO;
import com.mincom.enterpriseservice.exception.*
import groovy.sql.Sql
import com.mincom.ellipse.types.m3620.instances.WorkServiceResult

class WorkService_create extends ServiceHook {
	String hookVersion = "1"
	String Acct = "";
	String strCrDT = "";
	String strCrTM = "";
	String StrDT = "";
	String StrMT = "";
	String StrYR = "";
	String StrHH = "";
	String StrMM = "";
	String StrSS = "";
	String StrErr = "";
	String PoNo = "";
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	@Override
	public Object onPreExecute(Object input) {
		log.info("Hooks WorkService_create onPreExecute logging.version: ${hookVersion}")

		WorkDTO c = (WorkDTO) input
		//log.info("WorkDTO: " + c)
		Acct = c.getAccountCode().getValue();
		log.info("Acct: " + Acct)
		if (!Acct.equals(null)){
			def QueryRes1 = sql.firstRow("select * from msf966 where DSTRCT_CODE = '" + tools.commarea.District + "' and trim(ACCOUNT_CODE) = trim('" + Acct + "') and ACCOUNT_IND in ('2','3') AND ACTIVE_STATUS <> 'I'");
			log.info ("QueryRes1 : " + QueryRes1);
			if (QueryRes1.equals(null)){
				throw new EnterpriseServiceOperationException(
				new ErrorMessageDTO(
				"9999", "INVALID ACCOUNT CODE !", "accountCode", 0, 0))
				return input
			}else {
				if (QueryRes1.ACCOUNT_CODE.trim().length() < 6 ){
					throw new EnterpriseServiceOperationException(
					new ErrorMessageDTO(
					"9999", "SHOULD BE FULL ACCOUNT CODE OR GL !", "accountCode", 0, 0))
					return input
				}
				PoNo = "";
				if (QueryRes1.ACCOUNT_CODE.trim().equals("060004") ){
					List<Attribute> custAttribs = c.getCustomAttributes()
					custAttribs.each{Attribute customAttribute ->
						if (customAttribute.getName().equals(new String("poNo"))){
							log.info ("Value poNo : " + customAttribute.getValue());
							if (customAttribute.getValue().equals(null) || customAttribute.getValue().equals("")){
								/*
								throw new EnterpriseServiceOperationException(
								new ErrorMessageDTO(
								"9999", "PO NO REQUIRED !", "poNo", 0, 0))
								return input
								*/
							}else {
								def QueryRes2 = sql.firstRow("select * from msf220 where PO_NO = '" + customAttribute.getValue() + "' and supplier_no = 'CRS10'");
								log.info ("QueryRes2 : " + QueryRes2);
								if (QueryRes2.equals(null)){
									throw new EnterpriseServiceOperationException(
									new ErrorMessageDTO(
									"9999", "INVALID PO NO !", "poNo", 0, 0))
									return input
								}else {
									PoNo = QueryRes2.PO_NO;
								}
							}
						}
					}
				}
			}
		}
		String copyDst = c.getCopyDistrictCode().value
		String copyWo = c.getCopyWorkOrder().value
		log.info ("copyDst : " + copyDst);
		log.info ("copyWo : " + copyWo);
		if (!copyDst.equals(null) && !copyWo.equals(null)) {
			if (!copyDst.trim().equals("") && !copyWo.trim().equals("")) {
				PoNo = "";
				def QueryRes1 = sql.firstRow("select * from msf071 where ENTITY_TYPE = 'WPO' and upper(trim(ENTITY_VALUE)) = upper(trim('"+copyDst+copyWo+"')) and REF_NO = '001' and SEQ_NUM = '001'");
				log.info ("QueryRes1 : " + QueryRes1);
				if (!QueryRes1.equals(null)){
					if (!QueryRes1.REF_CODE.trim().equals("")){
						PoNo = QueryRes1.REF_CODE.trim()
					}else {
						def QueryRes2 = sql.firstRow("select * from msf620 where DSTRCT_CODE = '"+copyDst+"' and WORK_ORDER = '"+copyWo+"'");
						if (!QueryRes2.equals(null)){
							String dstAcct = QueryRes2.DSTRCT_ACCT_CODE.trim();
							dstAcct = dstAcct.replace(copyDst, "");
							if (dstAcct.trim().equals("060004") ){
								/*
								throw new EnterpriseServiceOperationException(
								new ErrorMessageDTO(
								"9999", "PO NO REQUIRED !", "poNo", 0, 0))
								return input
								*/
							}
						}
					}
				}else {
					def QueryRes2 = sql.firstRow("select * from msf620 where DSTRCT_CODE = '"+copyDst+"' and WORK_ORDER = '"+copyWo+"'");
					if (!QueryRes2.equals(null)){
						String dstAcct = QueryRes2.DSTRCT_ACCT_CODE.trim();
						dstAcct = dstAcct.replace(copyDst, "");
						if (dstAcct.trim().equals("060004") ){
							/*
							throw new EnterpriseServiceOperationException(
							new ErrorMessageDTO(
							"9999", "PO NO REQUIRED !", "poNo", 0, 0))
							return input
							*/
						}
					}
				}
			}
		}

		return null
	}

	@Override
	public Object onPostExecute(Object input, Object result) {
		log.info("Hooks WorkService_create onPostExecute logging.version: ${hookVersion}")
		WorkServiceResult e = (WorkServiceResult) result
		WorkDTO d = e.getWorkDTO();
		if (!PoNo.equals("")) {
			try
			{
				String WoNo = d.getWorkOrder().value
				GetNowDateTime();
				String QueryInsert = ("Insert into MSF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY) values ('WPO','"+tools.commarea.District+WoNo+"','001','001','" + strCrDT + "','" + strCrTM + "','" + tools.commarea.UserId + "','"+PoNo+"','            ')");
				sql.execute(QueryInsert);
			} catch (Exception  ex) {
				log.info ("Exception is : " + ex);
			}
		}

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
