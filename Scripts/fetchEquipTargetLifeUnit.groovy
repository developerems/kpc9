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

class fetchEquipTargetLifeUnit extends GenericScriptPlugin implements GenericScriptExecute{
    String version = "2"

    InitialContext initial = new InitialContext()
    Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
    def sql = new Sql(CAISource)

    @Override
    GenericScriptResults execute(SecurityToken securityToken, List<RequestAttributes> list, Boolean aBoolean) throws FatalException {
        log.info("--- [ARSIADI] fetchEquipTargetLifeUnit.groovy execute version: $version")
        GenericScriptResults results = new GenericScriptResults()
        String districtCode = securityToken.district.trim()
        RequestAttributes requestAttributes = list[0]
        String equipmentNo = requestAttributes.getAttributeStringValue("equipmentNo") ? requestAttributes.getAttributeStringValue("equipmentNo").trim() : null
        log.info("EquipmentNo: $equipmentNo")
        log.info("District: $districtCode")

        String query071_110 = "SELECT REF_CODE FROM MSF071 WHERE ENTITY_TYPE = 'EQP' AND ENTITY_VALUE = '$equipmentNo' AND REF_NO = '110'"
        log.info("query select MSF0712: $query071_110")
        def query071_110Result = sql.firstRow(query071_110)
        log.info("--- query071_110: $query071_110")
        log.info("--- query071_110 result: $query071_110Result")
        if (query071_110Result) {
            String targetLifeUnit = query071_110Result.REF_CODE ? query071_110Result.REF_CODE != '' ? query071_110Result.REF_CODE.trim() : null : null
            log.info("--- targetLifeUnit: --$targetLifeUnit--")
            if (targetLifeUnit) {
                GenericScriptResult result = new GenericScriptResult()
                result.addAttribute("targetLifeUnit", targetLifeUnit)
                results.add(result)
            }
        }
        return results
    }
}
