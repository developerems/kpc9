package KPC.HOOKS
/**
 * @EMS Mar 2019
 *
 * 20190328 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Initial Coding - Forward Fit from Ellipse 5 to validate Full Account code when create WO
 **/

import javax.naming.InitialContext

import com.mincom.ellipse.attribute.Attribute
import com.mincom.enterpriseservice.ellipse.*
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.enterpriseservice.ellipse.workorder.WorkOrderService
import com.mincom.enterpriseservice.ellipse.workorder.WorkOrderServiceChangeAccountRequestDTO;
import com.mincom.ellipse.service.ServiceDTO;
import com.mincom.enterpriseservice.exception.*
import groovy.sql.Sql

class WorkOrderService_changeAccount extends ServiceHook {
	String hookVersion = "1"
	String Acct = "";
	
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	@Override
	public Object onPreExecute(Object input) {
		log.info("Hooks WorkOrderService_changeAccount onPreExecute logging.version: ${hookVersion}")

		WorkOrderServiceChangeAccountRequestDTO c = (WorkOrderServiceChangeAccountRequestDTO) input
		log.info("WorkDTO: " + c)
		Acct = c.getAccountCode();
		log.info("Acct: " + Acct)
		if (!Acct.equals(null)){
			def QueryRes1 = sql.firstRow("select * from msf966 where DSTRCT_CODE = '" + tools.commarea.District + "' and trim(ACCOUNT_CODE) = trim('" + Acct + "') and ACCOUNT_IND in ('2','3') AND ACTIVE_STATUS <> 'I'");
			log.info ("QueryRes1 : " + QueryRes1);
			if (QueryRes1.equals(null)){
				throw new EnterpriseServiceOperationException(
					new ErrorMessageDTO(
					"9999", "INVALID ACCOUNT CODE !", "accountCode", 0, 0))
					return input
			}else {
				if (QueryRes1.ACCOUNT_CODE.trim().length() < 6 ){
					throw new EnterpriseServiceOperationException(
					new ErrorMessageDTO(
					"9999", "SHOULD BE FULL ACCOUNT CODE OR GL !", "accountCode", 0, 0))
					return input
				}
				/*
				if (QueryRes1.ACCOUNT_CODE.trim().equals("060004") ){
					String WoNo = c.getWorkOrder().getPrefix() + c.getWorkOrder().getNo();
					def QueryRes0 = sql.firstRow("select * from msf071 where ENTITY_TYPE = 'WPO' and upper(trim(ENTITY_VALUE)) = upper(trim('"+tools.commarea.District+WoNo+"')) and REF_NO = '001' and SEQ_NUM = '001'");
					log.info ("QueryRes0 : " + QueryRes0);
					if (QueryRes0.equals(null)){
						throw new EnterpriseServiceOperationException(
						new ErrorMessageDTO(
						"9999", "PO NO REQUIRED !", "poNo", 0, 0))
						return input
					}else {
						if (QueryRes0.REF_CODE.trim().equals("")) {
							throw new EnterpriseServiceOperationException(
							new ErrorMessageDTO(
							"9999", "PO NO REQUIRED !", "poNo", 0, 0))
							return input
						}
					}
				}
				*/
			}
		}
		return null
	}
	
	@Override
	public Object onPostExecute(Object input, Object result) {
		log.info("Hooks WorkOrderService_changeAccount onPostExecute logging.version: ${hookVersion}")
		
		return result
	}
}
