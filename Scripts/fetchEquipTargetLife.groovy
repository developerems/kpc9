package Scripts

import com.mincom.ellipse.app.security.SecurityToken
import com.mincom.ellipse.errors.exceptions.FatalException
import com.mincom.ellipse.script.plugin.GenericScriptExecute
import com.mincom.ellipse.script.plugin.GenericScriptPlugin
import com.mincom.ellipse.script.plugin.GenericScriptResult
import com.mincom.ellipse.script.plugin.GenericScriptResults
import com.mincom.ellipse.script.plugin.RequestAttributes
import groovy.sql.Sql

import javax.naming.InitialContext

class fetchEquipTargetLife extends GenericScriptPlugin implements GenericScriptExecute{
    String version = "2"

    InitialContext initial = new InitialContext()
    Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
    def sql = new Sql(CAISource)

    @Override
    GenericScriptResults execute(SecurityToken securityToken, List<RequestAttributes> list, Boolean aBoolean) throws FatalException {
        log.info("--- [ARSIADI] fetchEquipTargetLife.groovy execute version: $version")
        GenericScriptResults results = new GenericScriptResults()
        String districtCode = securityToken.district.trim()
        RequestAttributes requestAttributes = list[0]
        String equipmentNo = requestAttributes.getAttributeStringValue("equipmentNo") ? requestAttributes.getAttributeStringValue("equipmentNo").trim() : null
        log.info("EquipmentNo: $equipmentNo")
        log.info("District: $districtCode")

        String query071_100 = "SELECT REF_CODE FROM MSF071 WHERE ENTITY_TYPE = 'EQP' AND ENTITY_VALUE = '$equipmentNo' AND REF_NO = '100'"
        log.info("query select MSF071: $query071_100")
        def query071_100Result = sql.firstRow(query071_100)
        log.info("--- query071_100: $query071_100")
        log.info("--- query071_100 result: $query071_100Result")
        if (query071_100Result) {
            String equipTargetLife = query071_100Result.REF_CODE ? query071_100Result.REF_CODE != '' ? query071_100Result.REF_CODE.trim() : null : null
            log.info("--- equipTargetLife: --$equipTargetLife--")
            if (equipTargetLife) {
                GenericScriptResult result = new GenericScriptResult()
                result.addAttribute("equipTargetLife", equipTargetLife)
                results.add(result)
            }
        }
        return results
    }
}
