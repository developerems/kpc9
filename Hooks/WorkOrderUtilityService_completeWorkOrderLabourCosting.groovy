package KPC.HOOKS

/**
 * @EMS 2019
 *
 * 20191024 - Ricky Afriano - KPC Upgrade - Initial code - Mandatory PO Number when close WO
 *
 **/

import groovy.sql.Sql
import javax.naming.InitialContext
import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.enterpriseservice.exception.*
import com.mincom.enterpriseservice.ellipse.*
import com.mincom.ellipse.service.m3620.workorderutility.WorkOrderUtilityService
import com.mincom.ellipse.types.m3620.instances.WorkOrderUtilityDTO;
import com.mincom.ellipse.edoi.ejb.msf620.MSF620Key
import com.mincom.ellipse.edoi.ejb.msf620.MSF620Rec
import com.mincom.ellipse.script.util.EDOIWrapper;
import com.mincom.eql.impl.*
import com.mincom.eql.*
import com.mincom.ellipse.edoi.ejb.msf627.MSF627Key
import com.mincom.ellipse.edoi.ejb.msf627.MSF627Rec
import com.mincom.enterpriseservice.ellipse.workorder.WorkOrderServiceCreateRequestDTO
import com.mincom.enterpriseservice.ellipse.workorder.WorkOrderServiceCreateReplyDTO
import com.mincom.enterpriseservice.ellipse.dependant.dto.WOUserStatHistDTO
import com.mincom.enterpriseservice.ellipse.dependant.dto.WorkOrderDTO

class WorkOrderUtilityService_completeWorkOrderLabourCosting extends ServiceHook{

	String hookVersion = "1"
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	@Override
	public Object onPreExecute(Object input) {
		log.info("Hooks WorkOrderUtilityService_completeWorkOrderLabourCosting onPreExecute logging.version: ${hookVersion}")
		WorkOrderUtilityDTO  c = (WorkOrderUtilityDTO) input

		String dis_code = tools.commarea.District;
		String work_order = c.getWorkOrder().getValue();
		String PoNo = "";
		log.info ("dis_code : " + dis_code);
		log.info ("work_order : " + work_order);
		if (!dis_code.equals(null) && !work_order.equals(null)) {
			if (!dis_code.trim().equals("") && !work_order.trim().equals("")) {
				PoNo = "";
				def QueryRes1 = sql.firstRow("select * from msf071 where ENTITY_TYPE = 'WPO' and upper(trim(ENTITY_VALUE)) = upper(trim('"+dis_code+work_order+"')) and REF_NO = '001' and SEQ_NUM = '001'");
				log.info ("QueryRes1 : " + QueryRes1);
				if (!QueryRes1.equals(null)){
					if (!QueryRes1.REF_CODE.trim().equals("")){
						PoNo = QueryRes1.REF_CODE.trim()
					}else {
						def QueryRes2 = sql.firstRow("select * from msf620 where DSTRCT_CODE = '"+dis_code+"' and WORK_ORDER = '"+work_order+"'");
						if (!QueryRes2.equals(null)){
							String dstAcct = QueryRes2.DSTRCT_ACCT_CODE.trim();
							dstAcct = dstAcct.replace(dis_code, "");
							if (!work_order.substring(0,2).equals("OR")) {
								if (dstAcct.trim().equals("060004") ){
									throw new EnterpriseServiceOperationException(
									new ErrorMessageDTO(
									"9999", "PO NO REQUIRED !", "poNo", 0, 0))
									return input
								}
							}
						}
					}
				}else {
					def QueryRes2 = sql.firstRow("select * from msf620 where DSTRCT_CODE = '"+dis_code+"' and WORK_ORDER = '"+work_order+"'");
					if (!QueryRes2.equals(null)){
						String dstAcct = QueryRes2.DSTRCT_ACCT_CODE.trim();
						dstAcct = dstAcct.replace(dis_code, "");
						if (!work_order.substring(0,2).equals("OR")) {
							if (dstAcct.trim().equals("060004") ){
								throw new EnterpriseServiceOperationException(
								new ErrorMessageDTO(
								"9999", "PO NO REQUIRED !", "poNo", 0, 0))
								return input
							}
						}
					}
				}
			}
		}

		return null;
	}

	@Override
	public Object onPostExecute(Object input, Object result){

		log.info("Hooks WorkOrderUtilityService_completeWorkOrderLabourCosting onPostExecute logging.version: ${hookVersion}")

		return result
	}
}