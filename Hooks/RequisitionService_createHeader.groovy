package KPC.HOOKS
/**
 *
 * 20190902   Ricky Afriano
 *            Initial Coding - Prevent transaction For Project that match criteria
 **/
import javax.naming.InitialContext

import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.hook.hooks.ServiceHook
import groovy.sql.Sql
import com.mincom.enterpriseservice.ellipse.requisition.RequisitionServiceCreateItemRequestDTO
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import com.mincom.enterpriseservice.ellipse.requisition.RequisitionServiceCreateHeaderRequestDTO
import com.mincom.enterpriseservice.ellipse.ErrorMessageDTO
import com.mincom.enterpriseservice.ellipse.dependant.dto.RequisitionItemDTO;


class RequisitionService_createHeader extends ServiceHook{

	String hookVersion = "1"
	String projA = "";
	String projB = "";
	
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	
	
	@Override
	public Object onPreExecute(Object input) {
		log.info("Hooks RequisitionService_createHeader onPreExecute logging.version: ${hookVersion}")
				
		RequisitionServiceCreateHeaderRequestDTO e = (RequisitionServiceCreateHeaderRequestDTO) input ;
		projA = "";
		projB = "";
		projA = e.getProjectA();
		projB = e.getProjectB();
		def QRY1;
		if (!projA.equals(null) && !projA.equals("")) {
			//Search Top Parent
			QRY1 = sql.firstRow("SELECT DISTINCT ML.PROJECT_NO FROM msf660 ml WHERE CONNECT_BY_ISLEAF = 1 START WITH ml.DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and trim(ML.PROJECT_NO) = '"+projA.trim()+"' CONNECT BY ML.PROJECT_NO = prior ML.PARENT_PROJ");
			if(!QRY1.equals(null)) {
				projA = QRY1.PROJECT_NO
			}else {
				projA = "";
			}
			
			QRY1 = sql.firstRow("SELECT * FROM MSF071 where ENTITY_TYPE = 'PRJ' and trim(ENTITY_VALUE) = trim('"+tools.commarea.District.trim()+projA.trim()+"') and REF_NO = '004'");
			if(!QRY1.equals(null)) {
				if(QRY1.REF_CODE.trim().equals("Y")) {
					throw new EnterpriseServiceOperationException(
						new ErrorMessageDTO(
						"9999", "PROJECT STATUS HOLD!", "projectA", 0, 0))
						return input
				}
			}
		}
		
		if (!projB.equals(null) && !projB.equals("")) {
			//Search Top Parent
			QRY1 = sql.firstRow("SELECT DISTINCT ML.PROJECT_NO FROM msf660 ml WHERE CONNECT_BY_ISLEAF = 1 START WITH ml.DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and trim(ML.PROJECT_NO) = '"+projB.trim()+"' CONNECT BY ML.PROJECT_NO = prior ML.PARENT_PROJ");
			if(!QRY1.equals(null)) {
				projB = QRY1.PROJECT_NO
			}else {
				projB = "";
			}
			
			QRY1 = sql.firstRow("SELECT * FROM MSF071 where ENTITY_TYPE = 'PRJ' and trim(ENTITY_VALUE) = trim('"+tools.commarea.District.trim()+projB.trim()+"') and REF_NO = '004'");
			if(!QRY1.equals(null)) {
				if(QRY1.REF_CODE.trim().equals("Y")) {
					throw new EnterpriseServiceOperationException(
						new ErrorMessageDTO(
						"9999", "PROJECT STATUS HOLD!", "projectB", 0, 0))
						return input
				}
			}
		}
		return null
	}
}