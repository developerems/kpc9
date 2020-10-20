package KPC.HOOKS
/**
 *
 * 20190922   Ricky Afriano  - add function to show plan use date
 **/
import javax.naming.InitialContext

import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.hook.hooks.ServiceHook
import groovy.sql.Sql
import com.mincom.enterpriseservice.ellipse.requisition.RequisitionServiceCreateItemRequestDTO
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import com.mincom.enterpriseservice.ellipse.requisition.RequisitionServiceCreateHeaderRequestDTO
import com.mincom.enterpriseservice.ellipse.requisition.RequisitionServiceModifyHeaderRequestDTO
import com.mincom.enterpriseservice.ellipse.requisition.RequisitionServiceReadReplyDTO
import com.mincom.enterpriseservice.ellipse.ErrorMessageDTO
import com.mincom.enterpriseservice.ellipse.dependant.dto.RequisitionItemDTO;


class RequisitionService_read extends ServiceHook{

	String hookVersion = "1"
	
	
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	
	
	@Override
	public Object onPostExecute(Object input, Object result){
		log.info("Hooks RequisitionService_read onPostExecute logging.version: ${hookVersion}")
		RequisitionServiceReadReplyDTO e = (RequisitionServiceReadReplyDTO) result ;
		String ireqNo = e.getIreqNo();
		String ireqType = e.getIreqType();
		if (ireqType.trim().equals("NI")) {
			def QRY1;
			QRY1 = sql.firstRow("select * from msf071 where ENTITY_TYPE = 'PUD' and upper(trim(ENTITY_VALUE)) = upper(trim('"+tools.commarea.district+ireqNo+"')) and REF_NO = '001' and SEQ_NUM = '001'");
			log.info ("FIND Plan Use Date  : ");
			if(!QRY1.equals(null)) {
				Attribute[] ATT = new Attribute[1];
				log.info("ATT SIZE: " + ATT.size());
				ATT[0] = new Attribute();
				ATT[0].setName("planUseDate");
				ATT[0].setValue(QRY1.REF_CODE.trim());
				e.setCustomAttributes(ATT);
			}
		}
		return result
	}
}