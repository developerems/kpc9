import com.mincom.ellipse.app.security.SecurityToken
import com.mincom.ellipse.errors.exceptions.FatalException
import com.mincom.ellipse.script.plugin.GenericScriptExecuteForCollection
import com.mincom.ellipse.script.plugin.GenericScriptPlugin
import com.mincom.ellipse.script.plugin.GenericScriptResult
import com.mincom.ellipse.script.plugin.GenericScriptResults
import com.mincom.ellipse.script.plugin.RequestAttributes
import com.mincom.ellipse.script.plugin.RestartAttributes
import groovy.sql.Sql

import javax.naming.InitialContext

class readPrevYearPrk extends GenericScriptPlugin implements GenericScriptExecuteForCollection {
    @Override
    GenericScriptResults executeForCollection(SecurityToken securityToken, RequestAttributes requestAttributes, Integer integer, RestartAttributes restartAttributes) throws FatalException {
        GenericScriptResults results = new GenericScriptResults()

        String districtCode = securityToken.district.trim()
        String workOrder = requestAttributes.getAttributeStringValue("workOrder").trim()
        String entityValue = "1$districtCode$workOrder"

        log.info("districtCode: $districtCode")
        log.info("workOrder: $workOrder")
        log.info("entityValue: $entityValue")

        InitialContext initial = new InitialContext()
        Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
        def sql = new Sql(CAISource)

        String query = "SELECT REF_CODE, LAST_MOD_DATE FROM MSF071 WHERE ENTITY_TYPE = '+PK' AND ENTITY_VALUE = '$entityValue'"
        log.info("query select MSF071: $query")

        sql.eachRow(query, {
            GenericScriptResult result = new GenericScriptResult()
            result.addAttribute("oldPrk", it.REF_CODE)
            result.addAttribute("changedDate", it.LAST_MOD_DATE)
            results.add(result)
        })

        return results
    }
}
