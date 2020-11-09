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
        log.info("query select MSF600: $query600")
        def query600Result = sql.firstRow(query600)
        log.info("--- query600: $query600")
        log.info("--- query600 result: $query600Result")

        String query071_100 = "SELECT REF_CODE FROM MSF071 WHERE ENTITY_TYPE = 'EQP' AND ENTITY_VALUE = '$equipmentNo' AND REF_NO = '100'"
        log.info("query select MSF071: $query071_100")
        def query071_100Result = sql.firstRow(query071_100)
        log.info("--- query071_100: $query071_100")
        log.info("--- query071_100 result: $query071_100Result")

        String query071_110 = "SELECT REF_CODE FROM MSF071 WHERE ENTITY_TYPE = 'EQP' AND ENTITY_VALUE = '$equipmentNo' AND REF_NO = '110'"
        log.info("query select MSF0712: $query071_110")
        def query071_110Result = sql.firstRow(query071_110)
        log.info("--- query071_110: $query071_110")
        log.info("--- query071_110 result: $query071_110Result")

        GenericScriptResult result = new GenericScriptResult()

        String lastModEmp
        if (query600Result) {
            lastModEmp = query600Result.LAST_MOD_EMP ? query600Result.LAST_MOD_EMP.trim() != "" ? query600Result.LAST_MOD_EMP.trim() : null : null
            log.info("--- lastModEmp: --$lastModEmp--")
            if (lastModEmp) {
                result.addAttribute("lastModEmp", lastModEmp)
            }
        }

        String equipTargetLife
        if (query071_100Result) {
            equipTargetLife = query071_100Result.REF_CODE ? query071_100Result.REF_CODE != "" ? query071_100Result.REF_CODE.trim() : null : null
            log.info("--- equipTargetLife: --$equipTargetLife--")
            if (equipTargetLife) {
                result.addAttribute("equipTargetLife", equipTargetLife)
            }
        }

        String targetLifeUnit
        if (query071_110Result) {
            targetLifeUnit = query071_110Result.REF_CODE ? query071_110Result.REF_CODE != '' ? query071_110Result.REF_CODE.trim() : null : null
            log.info("--- targetLifeUnit: --$targetLifeUnit--")
            if (targetLifeUnit) {
                result.addAttribute("targetLifeUnit", targetLifeUnit)
            }
        }

        results.add(result)
        return results
    }
}
