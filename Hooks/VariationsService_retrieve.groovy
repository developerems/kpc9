package KPC.HOOKS
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
import com.mincom.enterpriseservice.ellipse.contract.ContractServiceCreateReplyDTO
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextService
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextServiceCreateReplyDTO
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextServiceSetTextReplyDTO
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextServiceDeleteReplyDTO
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextServiceAppendReplyDTO
import com.mincom.ellipse.service.ServiceDTO;
import com.mincom.enterpriseservice.exception.*
import groovy.sql.Sql

class VariationsService_retrieve extends ServiceHook {
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
		log.info("Hooks VariationsService_retrieve onPreExecute logging.version: ${hookVersion}")

		VariationsServiceRetrieveRequestDTO c = (VariationsServiceRetrieveRequestDTO) input
		log.info("VariationsServiceRetrieveRequestDTO: " + c)
		if (!c.getContractNumber().equals(null)) {
			def QRY2 = sql.firstRow("select * from msf384 where CONTRACT_NO = '"+c.getContractNumber()+"' and DSTRCT_CODE = '"+tools.commarea.District.trim()+"' ");
			if(QRY2.equals(null)) {
				throw new EnterpriseServiceOperationException(
				new ErrorMessageDTO(
				"9999", "CONTRACT NOT IN THIS DISTRICT !", "", 0, 0))
				return input
			}
		}
		return null
	}
	
	@Override
	public Object onPostExecute(Object input, Object result) {
		log.info("Hooks VariationsService_retrieve onPostExecute logging.version: ${hookVersion}")
		
		return result
	}
}
