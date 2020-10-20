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
import com.mincom.enterpriseservice.ellipse.contract.ContractServiceCreateReplyDTO
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextService
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextServiceCreateReplyDTO
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextServiceSetTextReplyDTO
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextServiceDeleteReplyDTO
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextServiceAppendReplyDTO
import com.mincom.ellipse.service.ServiceDTO;
import com.mincom.enterpriseservice.exception.*
import groovy.sql.Sql

class ContractService_create extends ServiceHook {
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
		log.info("Hooks ContractService_create onPreExecute logging.version: ${hookVersion}")

		ContractServiceCreateRequestDTO c = (ContractServiceCreateRequestDTO) input
		log.info("ContractServiceCreateRequestDTO: " + c)
		return null
	}
	
	@Override
	public Object onPostExecute(Object input, Object result) {
		log.info("Hooks ContractService_create onPostExecute logging.version: ${hookVersion}")
		ContractServiceCreateReplyDTO d = (ContractServiceCreateReplyDTO) result
		try
		{
			String QueryUpdate = ("update msf384 set DSTRCT_CODE = ? where upper(trim(CONTRACT_NO)) = upper(trim('"+d.getContractNo()+"'))");
			sql.execute(QueryUpdate,[tools.commarea.District.trim()]);
		} catch (Exception  e) {
			log.info ("Exception is : " + e);
			throw new EnterpriseServiceOperationException(
					new ErrorMessageDTO(
					"9999", "EXCEPTION : ERROR WHEN UPDATE MSF384 !", "", 0, 0))
					return input
		}
		return result
	}
}
