/**
 * @EMS Mar 2019
 *
 * 20190317 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Initial Coding - Forward Fit from Ellipse 5 to set District based on login district
 **/

import javax.naming.InitialContext

import com.mincom.ellipse.attribute.Attribute
import com.mincom.enterpriseservice.ellipse.*
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.enterpriseservice.ellipse.contractitem.ContractItemService
import com.mincom.enterpriseservice.ellipse.contractitem.ContractItemServiceModifyPortMileRequestDTO
import com.mincom.enterpriseservice.ellipse.contract.ContractServiceModifyRequestDTO
import com.mincom.enterpriseservice.ellipse.contract.ContractServiceCreateRequestDTO
import com.mincom.enterpriseservice.ellipse.contract.ContractServiceRetrieveRequestDTO
import com.mincom.enterpriseservice.ellipse.variations.VariationsServiceRetrieveRequestDTO
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsServiceRetrieveRequestDTO
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsServiceRetrieveReplyDTO
import com.mincom.enterpriseservice.ellipse.valuations.ValuationsServiceRetrieveReplyCollectionDTO
import com.mincom.enterpriseservice.ellipse.contract.ContractServiceCreateReplyDTO
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextService
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextServiceCreateReplyDTO
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextServiceSetTextReplyDTO
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextServiceDeleteReplyDTO
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextServiceAppendReplyDTO
import com.mincom.ellipse.service.ServiceDTO;
import com.mincom.enterpriseservice.exception.*
import groovy.sql.Sql

class ValuationsService_retrieve extends ServiceHook {
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
	public Object onPreExecute(Object input) {
		log.info("Hooks ValuationsService_retrieve onPreExecute logging.version: ${hookVersion}")

		ValuationsServiceRetrieveRequestDTO c = (ValuationsServiceRetrieveRequestDTO) input
		log.info("ValuationsServiceRetrieveRequestDTO: " + c)
		c.setDistrictCode(tools.commarea.District.trim());
		return null
	}
	
	@Override
	public Object onPostExecute(Object input, Object result) {
		log.info("Hooks ValuationsService_retrieve onPostExecute logging.version: ${hookVersion}")
		ValuationsServiceRetrieveReplyCollectionDTO d = (ValuationsServiceRetrieveReplyCollectionDTO) result
		
		return result
	}
}
