package KPC.HOOKS

/**
 * @EMS Mar 2019
 *
 * 20180319 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Initial Coding - This hooks to show last modified by in equipment Data 
 **/
import javax.naming.InitialContext

import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.ellipse.service.m3875.approvalsmanager.ApprovalsManagerService
import com.mincom.ellipse.types.m3875.instances.TransactionRetrievalCriteriaSearchParam;
import com.mincom.ellipse.types.m3875.instances.TransactionDTO
import com.mincom.ellipse.types.m3875.instances.TransactionServiceResult;
import com.mincom.enterpriseservice.ellipse.equipment.EquipmentServiceReadReplyDTO;
import com.mincom.enterpriseservice.ellipse.equipment.EquipmentServiceFetchReplyDTO;
import com.mincom.ellipse.service.ServiceDTO;
import com.mincom.enterpriseservice.exception.*
import groovy.sql.Sql
import com.mincom.enterpriseservice.ellipse.*

class EquipmentService_fetch extends ServiceHook{
	String hookVersion = "1"
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	@Override
	public Object onPreExecute(Object input) {
		log.info("Hooks EquipmentService_fetch onPostExecute logging.version: ${hookVersion}")
		return null;
	}
	@Override
	public Object onPostExecute(Object input, Object result) {
		log.info("Hooks EquipmentService_fetch onPostExecute logging.version: ${hookVersion}")
		EquipmentServiceFetchReplyDTO d = (EquipmentServiceFetchReplyDTO) result
		String EQP_NO = d.getEquipmentNo().trim();
		if (!EQP_NO.equals(null)) {
			Attribute[] ATT = new Attribute[3];
			//log.info("ATT SIZE: " + ATT.size());
			ATT[0] = new Attribute();
			ATT[0].setName("lastModBy");
			def QRY1 = sql.firstRow("select * from msf600 " +
				"where trim(EQUIP_NO) = '"+EQP_NO+"' ");
			if(!QRY1.equals(null)) {
				//log.info ("QRY1 : " + QRY1);
				log.info ("FIND EQUIPMENT : " + QRY1.EQUIP_NO);
				log.info ("LAST_MOD_EMP : " + QRY1.LAST_MOD_EMP.trim());
				ATT[0].setValue(QRY1.LAST_MOD_EMP.trim());
			}
			ATT[1] = new Attribute();
			ATT[1].setName("tarLifeUnit");
			def QRY2 = sql.firstRow("select * from msf071 where ENTITY_TYPE = 'EQP' and trim(ENTITY_VALUE) = '"+EQP_NO+"' and REF_NO = '110' and SEQ_NUM = '001' ");
			if(!QRY2.equals(null)) {
				log.info ("TAR_LIFE_UNIT : " + QRY2.REF_CODE.trim());
				ATT[1].setValue(QRY2.REF_CODE.trim());
			}
			ATT[2] = new Attribute();
			ATT[2].setName("eqpTarLife");
			def QRY3 = sql.firstRow("select * from msf071 where ENTITY_TYPE = 'EQP' and trim(ENTITY_VALUE) = '"+EQP_NO+"' and REF_NO = '100' and SEQ_NUM = '001' ");
			if(!QRY3.equals(null)) {
				log.info ("TAR_LIFE_UNIT : " + QRY3.REF_CODE.trim());
				ATT[2].setValue(QRY3.REF_CODE.trim());
			}
			d.setCustomAttributes(ATT);
		}
		return result
	}
}