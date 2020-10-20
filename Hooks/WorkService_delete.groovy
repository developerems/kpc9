/**
 * @EMS Apr 2020
 *
 * 20200413 - Danov - Customer Support
 *            Initial Coding - Prevent WO deleted if there is a CIC related to it.
 **/


import javax.naming.InitialContext

import com.mincom.ellipse.attribute.Attribute
import com.mincom.enterpriseservice.ellipse.*
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.ellipse.service.m3620.work.WorkService
import com.mincom.ellipse.types.m3620.instances.WorkDTO;
import com.mincom.ellipse.service.ServiceDTO;
import com.mincom.enterpriseservice.exception.*
import groovy.sql.Sql
import com.mincom.ellipse.types.m3620.instances.WorkServiceResult


class WorkService_delete extends ServiceHook {
	String hookVersion = "1"
	String CIC = "";
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	
	@Override
	public Object onPreExecute(Object input) {
		log.info("Hooks WorkService_create onPreExecute logging.version: ${hookVersion}")
		WorkDTO c = (WorkDTO) input
		WorkServiceResult e = new WorkServiceResult();
		def KPF38F = sql.firstRow("SELECT * FROM ACA.KPF38F WHERE CIC_TYPE ='WO' AND WORK_ORDER ='"+c.getWorkOrder().getValue().trim()+"' AND DSTRCT_CODE = '" + tools.commarea.District + "' AND CIC_STATUS <> '3'");
		if(!KPF38F.equals(null) && c.getWorkOrderStatusM().getValue().trim() != "C"){
			/*throw new EnterpriseServiceOperationException(
				new ErrorMessageDTO(
				"9999", "WORK ORDER CANNOT BE DELETED BECAUSE THERE IS A CIC RELATED. PLEASE CANCEL A CIC FIRST!.", "workOrder", 0, 0))*/
			e.addError("9999", "WORK ORDER CANNOT BE DELETED BECAUSE THERE IS A CIC RELATED. PLEASE CANCEL A CIC FIRST!.");
			return e
		}
		return null
	}
	
	@Override
	public Object onPostExecute(Object input, Object result) {
		log.info("Hooks WorkService_create onPostExecute logging.version: ${hookVersion}")
		return result
	}
}
