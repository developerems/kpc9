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

class fetchMSE65AWoDesc extends GenericScriptPlugin implements GenericScriptExecute{
    String version = "1"

    InitialContext initial = new InitialContext()
    Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
    def sql = new Sql(CAISource)

    @Override
    GenericScriptResults execute(SecurityToken securityToken, List<RequestAttributes> list, Boolean aBoolean) throws FatalException {
        log.info("--- [ARSIADI] fetchMSE65AWoDesc.groovy execute version: $version")
        GenericScriptResults results = new GenericScriptResults()
        String districtCode = securityToken.district.trim()
        RequestAttributes requestAttributes = list[0]
        String origRefNum = requestAttributes.getAttributeStringValue("origRefNum")

        String query620 = "select * from msf620 where WORK_ORDER = '$origRefNum'"

        def query620Result = sql.firstRow(query620)
        log.info("--- query620: $query620")
        log.info("--- query620 result: $query620Result")

        if (query620Result) {
            String workOrder = query620Result.WO_DESC ? query620Result.WO_DESC.trim() != '' ? query620Result.WO_DESC.trim() : null : null
            log.info("--- workOrder: --$workOrder--")
            if (workOrder) {
                GenericScriptResult result = new GenericScriptResult()
                result.addAttribute("woDesc", workOrder)
                results.add(result)
            }
        }
        return results
    }
}
