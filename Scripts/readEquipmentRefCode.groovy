import com.mincom.ellipse.app.security.SecurityToken
import com.mincom.ellipse.errors.exceptions.FatalException
import com.mincom.ellipse.script.plugin.GenericScriptExecute
import com.mincom.ellipse.script.plugin.GenericScriptPlugin
import com.mincom.ellipse.script.plugin.GenericScriptResult
import com.mincom.ellipse.script.plugin.GenericScriptResults
import com.mincom.ellipse.script.plugin.RequestAttributes
import groovy.sql.Sql

import javax.naming.InitialContext

class readEquipmentRefCode extends GenericScriptPlugin implements GenericScriptExecute{
    String version = "1"

    @Override
    GenericScriptResults execute(SecurityToken securityToken, List<RequestAttributes> list, Boolean aBoolean) throws FatalException {
        log.info("--- [ARSIADI] readEquipmentRefCode.groovy execute version: $version")
        GenericScriptResults results = new GenericScriptResults()
        String districtCode = securityToken.district.trim()
        RequestAttributes requestAttributes = list[0]
        String equipmentNo = requestAttributes.getAttributeStringValue("equipmentNo") ? requestAttributes.getAttributeStringValue("equipmentNo").trim() : null
        log.info("EquipmentNo: $equipmentNo")
        log.info("District: $districtCode")

        InitialContext initial = new InitialContext()
        Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
        def sql = new Sql(CAISource)

        String query600 = "SELECT LAST_MOD_EMP FROM MSF600 WHERE DSTRCT_CODE = '$districtCode' AND EQUIP_NO = '$equipmentNo'"
        log.info("query select MSF600: $query600")
        sql.eachRow(query600, {
            GenericScriptResult result = new GenericScriptResult()
            result.addAttribute("lastModEmp", it.LAST_MOD_EMP)
            results.add(result)
        })

        String query071_100 = "SELECT REF_CODE FROM MSF071 WHERE ENTITY_TYPE = 'EQP' AND ENTITY_VALUE = '$equipmentNo' AND REF_NO = '100'"
        log.info("query select MSF071: $query071_100")
        sql.eachRow(query071_100, {
            GenericScriptResult result2 = new GenericScriptResult()
            result2.addAttribute("equipTargetLife", it.REF_CODE)
            results.add(result2)
        })

        String query071_110 = "SELECT REF_CODE FROM MSF071 WHERE ENTITY_TYPE = 'EQP' AND ENTITY_VALUE = '$equipmentNo' AND REF_NO = '110'"
        log.info("query select MSF071: $query071_110")
        sql.eachRow(query071_100, {
            GenericScriptResult result3 = new GenericScriptResult()
            result3.addAttribute("targetLifeUnit", it.REF_CODE)
            results.add(result3)
        })
        return null
    }
}
