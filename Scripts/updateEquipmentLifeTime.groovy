import com.mincom.ellipse.app.security.SecurityToken
import com.mincom.ellipse.errors.exceptions.FatalException
import com.mincom.ellipse.script.plugin.GenericScriptPlugin
import com.mincom.ellipse.script.plugin.GenericScriptResults
import com.mincom.ellipse.script.plugin.GenericScriptUpdate
import com.mincom.ellipse.script.plugin.RequestAttributes

class updateEquipmentLifeTime extends GenericScriptPlugin implements GenericScriptUpdate {
    @Override
    GenericScriptResults update(SecurityToken securityToken, List<RequestAttributes> list, Boolean aBoolean) throws FatalException {
        String version = "1"
        String district = securityToken.district
        RequestAttributes requestAttributes1 = list[0]

        String equipmentNo = requestAttributes1.getAttributeStringValue("equipmentNo")
        String lastModEmp = requestAttributes1.getAttributeStringValue("lastModEmp")
        String equipTargetLife = requestAttributes1.getAttributeStringValue("equipTargetLife")
        String targetLifeUnit = requestAttributes1.getAttributeStringValue("targetLifeUnit")

        log.info("equipmentNo: $equipmentNo")
        log.info("lastModEmp2: $lastModEmp")
        log.info("equipTargetLife2: $equipTargetLife")
        log.info("targetLifeUnit2: $targetLifeUnit")

        return null
    }
}
