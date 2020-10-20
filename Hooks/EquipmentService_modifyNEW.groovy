import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.enterpriseservice.ellipse.equipment.EquipmentServiceModifyRequestDTO
import groovy.sql.Sql

import javax.naming.InitialContext

class EquipmentService_modifyNEW extends ServiceHook {
    String hookVersion = "1"

    InitialContext initial = new InitialContext()
    Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
    def sql = new Sql(CAISource)

    @Override
    Object onPreExecute(Object input) {
        log.info("Arsiadi Hooks EquipmentService_modify onPostExecute logging.version: $hookVersion")
        EquipmentServiceModifyRequestDTO requestDTO = (EquipmentServiceModifyRequestDTO) input
        String equipmentNo = requestDTO ? requestDTO.getEquipmentNo().toString().trim() : ""
        List<Attribute> customAttributes = requestDTO.getCustomAttributes()
        log.info("equipmentNo: $equipmentNo")
        customAttributes.each { Attribute customAttribute ->
            log.info("custom attribute name: ${customAttribute.getName()}")
            log.info("custom attribute value: ${customAttribute.getValue()}")
        }
        return null
    }
}
