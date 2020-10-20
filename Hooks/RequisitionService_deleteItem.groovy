package KPC.HOOKS
/**
 * @EMS Mar 2019
 *
 * 20190307 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Initial Coding - Delete TOW data When delete Preq Item
 **/
import javax.naming.InitialContext

import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.enterpriseservice.ellipse.requisition.RequisitionService
import com.mincom.enterpriseservice.ellipse.requisition.RequisitionServiceRetrieveItemReplyDTO
import com.mincom.enterpriseservice.ellipse.requisition.RequisitionServiceRetrieveItemRequestDTO
import com.mincom.enterpriseservice.ellipse.requisition.RequisitionServiceDeleteItemRequestDTO
import com.mincom.enterpriseservice.ellipse.requisition.RequisitionServiceDeleteItemReplyDTO
import com.mincom.enterpriseservice.ellipse.dependant.dto.RequisitionItemDTO;
import com.mincom.ellipse.service.ServiceDTO;
import com.mincom.enterpriseservice.exception.*
import groovy.sql.Sql
import com.mincom.enterpriseservice.ellipse.*


class RequisitionService_deleteItem extends ServiceHook{

	String hookVersion = "1"
	String PREQ_NO = "";
	RequisitionItemDTO[] itm;
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	
	@Override
	public Object onPreExecute(Object input) {
		log.info("Hooks RequisitionService_deleteItem onPreExecute logging.version: ${hookVersion}")
		RequisitionServiceDeleteItemRequestDTO c = (RequisitionServiceDeleteItemRequestDTO) input;
		PREQ_NO = "";
		PREQ_NO = c.getPreqNo();
		itm = c.getRequisitionItems(); 
		return null
	}

	@Override
	public Object onPostExecute(Object input, Object result) {
		log.info("Hooks RequisitionService_deleteItem onPostExecute logging.version: ${hookVersion}");
		log.info("PREQ_NO : " + PREQ_NO);
		RequisitionServiceDeleteItemReplyDTO d = (RequisitionServiceDeleteItemReplyDTO) result
		if (!PREQ_NO.equals(null)) {
			if (!PREQ_NO.equals("")) {
				Integer i = 0
				log.info("itm.length : " + itm.length);
				for (i = 0;i<itm.length;i++) {
					String ItmType = "";
					ItmType = itm[i].getItemType();
					BigDecimal ItmNo = itm[i].getIssueRequisitionItem();
					//log.info("ItmNo : " + ItmNo);
					String ItemNo = "";
					ItemNo = String.format("%03d",(Integer.parseInt(ItmNo.toString())));
					log.info("ItemNo : " + ItemNo);
					def QRY1 = sql.firstRow("select * from msf071 where ENTITY_TYPE = 'TOW' and trim(ENTITY_VALUE) = trim('"+tools.commarea.District.trim() + PREQ_NO.trim() + ItemNo.trim() + "') ");
					if(!QRY1.equals(null)) {
						log.info ("DELETE MSF071:");
						String QueryDelete = ("delete MSF071 where ENTITY_TYPE = 'TOW' and trim(ENTITY_VALUE) = trim('"+tools.commarea.District.trim() + PREQ_NO.trim() + ItemNo.trim() + "') ");
						sql.execute(QueryDelete);
					}
				}
			}
		}
			
		return result
	}
}