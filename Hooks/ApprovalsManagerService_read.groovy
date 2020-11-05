/**
 * @EMS Nov 2018
 *
 * 20181217 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Initial Coding
 **/
import javax.naming.InitialContext

import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.ellipse.service.m3875.approvalsmanager.ApprovalsManagerService
import com.mincom.ellipse.types.m3875.instances.TransactionRetrievalCriteriaSearchParam;
import com.mincom.ellipse.types.m3875.instances.TransactionDTO
import com.mincom.ellipse.types.m3875.instances.TransactionServiceResult;
import com.mincom.ellipse.service.ServiceDTO;
import com.mincom.enterpriseservice.exception.*
import groovy.sql.Sql
import com.mincom.enterpriseservice.ellipse.*

class ApprovalsManagerService_read extends ServiceHook{
	String hookVersion = "1"
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	
	String CNT_NO = "";
	String CIC_NO = "";
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
	String strCrDT = "";
	String strCrTM = "";
	String StrDT = "";
	String StrMT = "";
	String StrYR = "";
	String StrHH = "";
	String StrMM = "";
	String StrSS = "";
	
	@Override
	public Object onPreExecute(Object input) {
		log.info("Hooks ApprovalsManagerService_read onPreExecute logging.version: ${hookVersion}")

		TransactionDTO c = (TransactionDTO) input
		log.info("TransactionDTO: " + c)
		
		return null
	}
	
	@Override
	public Object onPostExecute(Object input, Object result) {
		log.info("Hooks ApprovalsManagerService_read onPostExecute logging.version: ${hookVersion}")
		TransactionServiceResult[] d = (TransactionServiceResult[]) result
		Integer i = 0
		for (i = 0;i<d.length;i++) {
			log.info("RES_LENGTH: " + d.length);
			log.info("RES_LENGTH1: " + d);
			log.info("RES_LENGTH2: " + d[i]);
			if (!d[i].getTransactionDTO().equals(null)) {
				TransactionDTO TRANS_DTO = d[i].getTransactionDTO();
				log.info ("RES VAL : " + TRANS_DTO.getTransactionKey().value);
				//TransactionDTO TRANS_DTO_SET = new TransactionDTO();
				Attribute[] ATT = new Attribute[2];
				log.info("ATT SIZE: " + ATT.size());
				ATT[0] = new Attribute();
				ATT[0].setName("DET_APM_CIC");
				if (TRANS_DTO.tran877Type.value.equals("VA")) {
					ATT[0].setValue(TRANS_DTO.getTransactionKey().value.substring(0,8));
					def QRY7 = sql.firstRow("select * from msf071 " +
						"where ENTITY_TYPE = 'CIV' and ENTITY_VALUE like '"+tools.commarea.District.trim()+TRANS_DTO.getTransactionKey().value.substring(0,8).trim()+"%' and length(trim(replace(ENTITY_VALUE,'"+tools.commarea.District.trim()+TRANS_DTO.getTransactionKey().value.substring(0,8).trim()+"'))) = 8 and REF_NO = '001' and SEQ_NUM = '001' and trim(REF_CODE) = trim('"+TRANS_DTO.getTransactionKey().value.substring(8,16).trim()+"')");
						log.info ("FIND VALN_NO  : " + QRY7);
					if(!QRY7.equals(null)) {
						String CALC_CIC_NO = QRY7.ENTITY_VALUE.toString().replace(tools.commarea.District.trim(), "");
						CALC_CIC_NO = CALC_CIC_NO.replace(TRANS_DTO.getTransactionKey().value.substring(0,8).trim(), "");
						ATT[0].setValue(CALC_CIC_NO.trim());
					}
				}else {
					ATT[0].setValue("N/A");
				}
				
				ATT[1] = new Attribute();
				ATT[1].setName("DET_APM_CON");
				ATT[1].setValue(TRANS_DTO.getTransactionKey().value.substring(0,8));
				
				TRANS_DTO.setCustomAttributes(ATT);
				d[i].setTransactionDTO(TRANS_DTO);
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