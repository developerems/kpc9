/**
 * @EMS Mar 2019
 *
 * 20180319 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Initial Coding - This hooks to show last modified by in equipment Data 
 **/

import com.mincom.enterpriseservice.ellipse.equipment.EquipmentServiceReadReplyCollectionDTO
import com.mincom.enterpriseservice.ellipse.equipment.EquipmentServiceReadRequestDTO

import javax.naming.InitialContext

import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.enterpriseservice.ellipse.equipment.EquipmentServiceReadReplyDTO
import groovy.sql.Sql

class EquipmentService_read extends ServiceHook{
	String hookVersion = "2"
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	@Override
	Object onPreExecute(Object input) {
		log.info("Hooks EquipmentService_read onPreExecute logging.version: ${hookVersion}")
		return null;
	}
	@Override
	Object onPostExecute(Object input, Object result) {
		log.info("Hooks EquipmentService_read onPostExecute logging.version: ${hookVersion}")
		EquipmentServiceReadReplyDTO d = (EquipmentServiceReadReplyDTO) result

		String EQP_NO = d.getEquipmentNo().trim()
		if (EQP_NO) {
			Attribute[] ATT = new Attribute[3]
			ATT[0] = new Attribute()
			ATT[0].setName("lastModBy")
			ATT[0].setNamespace("DEFAULT")
			def QRY1 = sql.firstRow("select * from msf600 " +
				"where trim(EQUIP_NO) = '"+EQP_NO+"' ")
			if (QRY1) {
				log.info ("FIND EQUIPMENT : " + QRY1.EQUIP_NO)
				log.info ("LAST_MOD_EMP : " + QRY1.LAST_MOD_EMP.trim())
				ATT[0].setValue(QRY1.LAST_MOD_EMP.trim())
			}

			ATT[1] = new Attribute()
			ATT[1].setName("targetLifeUnit")
			ATT[1].setNamespace("DEFAULT")
			def QRY2 = sql.firstRow("select * from msf071 where ENTITY_TYPE = 'EQP' and trim(ENTITY_VALUE) = '"+EQP_NO+"' and REF_NO = '110' and SEQ_NUM = '001' ")
			if(QRY2) {
				log.info ("TAR_LIFE_UNIT : " + QRY2.REF_CODE.trim())
				ATT[1].setValue(QRY2.REF_CODE.trim())
			}

			ATT[2] = new Attribute()
			ATT[2].setName("equipTargetLife")
			ATT[2].setNamespace("DEFAULT")
			def QRY3 = sql.firstRow("select * from msf071 where ENTITY_TYPE = 'EQP' and trim(ENTITY_VALUE) = '"+EQP_NO+"' and REF_NO = '100' and SEQ_NUM = '001' ")
			if(QRY3) {
				log.info ("EQP_TAR_LIFE : " + QRY3.REF_CODE.trim())
				ATT[2].setValue(QRY3.REF_CODE.trim())
			}
			log.info("ATT: $ATT")
			d.setCustomAttributes(ATT)
		}
		return result
	}
}