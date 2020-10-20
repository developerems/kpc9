package KPC.HOOKS
/**
 * @EMS Mar 2019
 *
 * 20190307 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Initial Coding - Vaildate TOW Data and Make sure user input it.
 **/
import javax.naming.InitialContext

import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.enterpriseservice.ellipse.requisition.RequisitionService
import com.mincom.enterpriseservice.ellipse.requisition.RequisitionServiceRetrieveItemReplyDTO
import com.mincom.enterpriseservice.ellipse.requisition.RequisitionServiceRetrieveItemRequestDTO
import com.mincom.enterpriseservice.ellipse.requisition.RequisitionServiceDeleteItemRequestDTO
import com.mincom.enterpriseservice.ellipse.requisition.RequisitionServiceDeleteItemReplyDTO
import com.mincom.enterpriseservice.ellipse.requisition.RequisitionServiceFinaliseRequestDTO
import com.mincom.enterpriseservice.ellipse.dependant.dto.RequisitionItemDTO;
import com.mincom.ellipse.service.ServiceDTO;
import com.mincom.enterpriseservice.exception.*
import groovy.sql.Sql
import com.mincom.enterpriseservice.ellipse.*


class RequisitionService_finalise extends ServiceHook{

	String hookVersion = "1"
	String PREQ_NO = "";
	RequisitionItemDTO[] itm;
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	
	@Override
	public Object onPreExecute(Object input) {
		log.info("Hooks RequisitionService_finalise onPreExecute logging.version: ${hookVersion}")
		RequisitionServiceFinaliseRequestDTO c = (RequisitionServiceFinaliseRequestDTO) input;
		PREQ_NO = "";
		PREQ_NO = c.getPreqNo();
		log.info("PREQ_NO : " + PREQ_NO);
		String StrSQL = "select * from msf231 where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and trim(PREQ_NO) = trim('"+PREQ_NO+"') and REQ_TYPE = 'S'";
		//log.info ("StrSQL : " + StrSQL);
		sql.eachRow(StrSQL, {
			def QRY1 = sql.firstRow("select * from msf071 where ENTITY_TYPE = 'TOW' and trim(ENTITY_VALUE) = trim('"+tools.commarea.District.trim() + PREQ_NO.trim() + it.PREQ_ITEM_NO.trim() + "') ");
			if(QRY1.equals(null)) {
				// Raise Error
				throw new EnterpriseServiceOperationException(
				new ErrorMessageDTO(
				"9999", "TOW DATA REQUIRED ! ITEM " + it.PREQ_ITEM_NO.trim(), "", 0, 0))
				return input
			}
		})
		
		return null
	}

	@Override
	public Object onPostExecute(Object input, Object result) {
		log.info("Hooks RequisitionService_finalise onPostExecute logging.version: ${hookVersion}");
						
		return result
	}
}