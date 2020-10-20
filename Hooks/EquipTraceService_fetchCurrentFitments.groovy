/**
 * @EMS Nov 2019
 *
 * 20190731 - XXXXX - Eghy Kurniagus - KPC UPGRADE
 *            Initial Coding
 **/
import javax.naming.InitialContext

import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.enterpriseservice.ellipse.equiptrace.EquipTraceService
import com.mincom.enterpriseservice.ellipse.equiptrace.EquipTraceServiceFetchCurrentFitmentsRequestDTO
import com.mincom.enterpriseservice.ellipse.equiptrace.EquipTraceServiceFetchCurrentFitmentsReplyDTO
import com.mincom.ellipse.service.ServiceDTO;
import com.mincom.enterpriseservice.exception.*
import groovy.sql.Sql
import com.mincom.enterpriseservice.ellipse.*


class EquipTraceService_fetchCurrentFitments extends ServiceHook{

	String hookVersion = "1"
	String strCrDT = "";
	String strCrTM = "";
	String StrDT = "";
	String StrMT = "";
	String StrYR = "";
	String StrHH = "";
	String StrMM = "";
	String StrSS = "";

	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)

	@Override
	Object onPostExecute(Object input, Object result) {
		log.info("Hooks EquipTraceService_fetchCurrentFitments onPostExecute logging.version: ${hookVersion}")

		EquipTraceServiceFetchCurrentFitmentsReplyDTO c = (EquipTraceServiceFetchCurrentFitmentsReplyDTO) result
		log.info("EquipTraceServiceFetchCurrentFitmentsReplyDTO: " + c)
		
		String OrigRefNum = c.origRefNum;
		log.info("Hooks EquipTraceService_fetchCurrentFitments onPostExecute logging.version: " + OrigRefNum)
		
		if (OrigRefNum) {
			Attribute[] ATT = new Attribute[1]
			ATT[0] = new Attribute()
			ATT[0].setName("woDesc")
			def QRY1 = sql.firstRow("select * from msf620 where WORK_ORDER = '$OrigRefNum'")
			if(QRY1) {
				log.info ("WO Desc : " + QRY1.WO_DESC)
				ATT[0].setValue(QRY1.WO_DESC.trim())
			}
			c.setCustomAttributes(ATT)
		}
		return result
	}
}