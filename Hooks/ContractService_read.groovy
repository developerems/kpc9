/**
 *
 * 20200310   Ricky Afriano  - Fix Approved Valuation Value in Cost summary Tab.
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
import com.mincom.enterpriseservice.ellipse.contract.ContractServiceReadRequestDTO

class ContractService_read extends ServiceHook{

	String hookVersion = "1"
	
	
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	
	
	@Override
	public Object onPreExecute(Object input) {
		log.info("Hooks ContractService_read onPreExecute logging.version: ${hookVersion}")
		ContractServiceReadRequestDTO c = (ContractServiceReadRequestDTO) input
		
		def QRY7 = sql.firstRow("select sum(VALUE_THIS_VALN) TOT_APPR_VAL from msf38b where trim(CONTRACT_NO) = trim('"+c.getContractNo().trim()+"') and VALN_STATUS = 'A'");
		if(!QRY7.TOT_APPR_VAL.equals(null)) {
			def QRY8 = sql.firstRow("select max(VALN_NO) MAX_VALN from msf38b where trim(CONTRACT_NO) = trim('"+c.getContractNo().trim()+"') and VALN_STATUS = 'A'");
			if(!QRY8.MAX_VALN.equals(null)) {
				String UpdateMSF38B = ("update msf38b set TOTAL_VALUATION = ? where trim(CONTRACT_NO) = trim('"+c.getContractNo().trim()+"') and trim(VALN_NO) = trim('"+QRY8.MAX_VALN+"')");
				sql.execute(UpdateMSF38B,QRY7.TOT_APPR_VAL);
			}
		}
		return null;
	}
}