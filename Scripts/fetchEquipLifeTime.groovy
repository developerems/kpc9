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

class fetchEquipLifeTime extends GenericScriptPlugin implements GenericScriptExecute{
    String version = "2"

    InitialContext initial = new InitialContext()
    Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
    def sql = new Sql(CAISource)

    @Override
    GenericScriptResults execute(SecurityToken securityToken, List<RequestAttributes> list, Boolean aBoolean) throws FatalException {
        log.info("--- [ARSIADI] fetchEquipLifeTime.groovy execute version: $version")
        GenericScriptResults results = new GenericScriptResults()
        String districtCode = securityToken.district.trim()
        RequestAttributes requestAttributes = list[0]
        String equipmentNo = requestAttributes.getAttributeStringValue("equipmentNo") ? requestAttributes.getAttributeStringValue("equipmentNo").trim() : null
        log.info("EquipmentNo: $equipmentNo")
        log.info("District: $districtCode")

        String query600 = "SELECT LAST_MOD_EMP FROM MSF600 WHERE DSTRCT_CODE = '$districtCode' AND EQUIP_NO = '$equipmentNo'"

        def query600Result = sql.firstRow(query600)
        log.info("--- query600: $query600")
        log.info("--- query600 result: $query600Result")

        if (query600Result) {
            String lastModEmp = query600Result.LAST_MOD_EMP ? query600Result.LAST_MOD_EMP.trim() != '' ? query600Result.LAST_MOD_EMP.trim() : null : null
            log.info("--- lastModEmp: --$lastModEmp--")
            if (lastModEmp) {
                GenericScriptResult result = new GenericScriptResult()
                result.addAttribute("lastModEmp", lastModEmp)
                results.add(result)
            }
        }
        return results
    }
}
