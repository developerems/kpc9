package KPC.HOOKS
/**
 * @EMS Mar 2019
 *
 * 20190920  - Eghy Kurniagus - KPC UPGRADE
 *            Initial Coding - calculate total IR&PR for new addition coloumn MSEWOT Tab Cost.
 **/

import javax.naming.InitialContext

import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.ellipse.service.ServiceDTO;
import com.mincom.enterpriseservice.exception.*
import groovy.sql.Sql
import com.mincom.enterpriseservice.ellipse.*
import com.mincom.ellipse.types.m3620.instances.WorkDTO;
import com.mincom.ellipse.service.m3620.work.WorkService
import com.mincom.ellipse.types.m3620.instances.WorkServiceResult

import java.text.DecimalFormat


class WorkService_read extends ServiceHook {
	String hookVersion = "1"
	String Acct = "";
	String AMTAcr = "";
	String StdJob = "";

	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	@Override
	public Object onPreExecute(Object input) {
		log.info("Hooks WorkService_read onPreExecute logging.version: ${hookVersion}")

		WorkDTO c = (WorkDTO) input
		//log.info("WorkDTO: " + c)

		return null
	}

	@Override
	public Object onPostExecute(Object input, Object result) {
		log.info("Hooks WorkService_read onPostExecute logging.version: ${hookVersion}")

		WorkServiceResult e = (WorkServiceResult) result
		WorkDTO d = e.getWorkDTO();
		String WoNo = d.getWorkOrder().value
		Attribute[] ATT = new Attribute[2];
		ATT[0] = new Attribute();
		ATT[0].setName("poNo");
		ATT[1] = new Attribute();
		ATT[1].setName("OtherTotal");
		def QRY1;
		QRY1 = sql.firstRow("select * from MSF071 where ENTITY_TYPE = 'WPO' and upper(trim(ENTITY_VALUE)) = upper(trim('"+tools.commarea.District+WoNo+"')) and REF_NO = '001' and SEQ_NUM = '001'");
		log.info ("FIND WO  : ");
		if(!QRY1.equals(null)) {
			if (QRY1.REF_CODE.trim().equals("")) {
				ATT[0].setValue("");
			}else {
				ATT[0].setValue(QRY1.REF_CODE.trim());
			}
		}else {
			ATT[0].setValue("");
		}


		def QRY2 = sql.firstRow("select NVL(TO_CHAR(sum(AMT),'999,999,999,999.99'),0) AMT from(select distinct case when REQ_232_TYPE = 'I' then B.AUTHSD_TOT_AMT else C.AUTHSD_TOT_AMT end  AMT  from msf232 A LEFT OUTER JOIN MSF140 B ON (A.DSTRCT_CODE = B.DSTRCT_CODE AND substr(trim(A.REQUISITION_NO),1,6) = trim(B.IREQ_NO)) LEFT OUTER JOIN MSF230 C ON (A.DSTRCT_CODE = C.DSTRCT_CODE AND substr(trim(A.REQUISITION_NO),1,6) = trim(C.PREQ_NO))"+
				"where A.REQ_232_TYPE in ('I','P') AND TRIM(A.WORK_ORDER) = '" + WoNo.trim()+"')");

		if(!QRY2.equals(null)) {
			AMTAcr = QRY2.AMT
			ATT[1].setValue(AMTAcr.trim());
		}else {
			ATT[1].setValue(0.00);
		}
		d.setCustomAttributes(ATT);
		return result
	}
}