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

class fetchTaxText extends GenericScriptPlugin implements GenericScriptExecute {
    String version = '1'

    InitialContext initial = new InitialContext()
    Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
    def sql = new Sql(CAISource)

    @Override
    GenericScriptResults execute(SecurityToken securityToken, List<RequestAttributes> list, Boolean aBoolean) throws FatalException {
        log.info("--- [ARSIADI] fetchPortMileTax.groovy execute version: $version")
        GenericScriptResults results = new GenericScriptResults()

        RequestAttributes requestAttributes = list[0]
        String contractAtt = requestAttributes.getAttributeStringValue("contractNo")
        String contractNo = contractAtt ? contractAtt.trim() : null
        String portionAtt = requestAttributes.getAttributeStringValue("portion")
        log.info("--- portionAtt: $portionAtt")
        String custItemType = requestAttributes.getAttributeStringValue("custItemType")
        log.info("--- custItemType: $custItemType")
        String element = custItemType == "C" ? requestAttributes.getAttributeStringValue("element") ? requestAttributes.getAttributeStringValue("element").trim() != "" ? requestAttributes.getAttributeStringValue("element").trim() : "" : "" : "01"
        log.info("--- element number: $element")
        String categoryNo = custItemType == "C" ? requestAttributes.getAttributeStringValue("categoryNo") ? requestAttributes.getAttributeStringValue("categoryNo").trim() != "" ? requestAttributes.getAttributeStringValue("categoryNo").trim() : "" : "" : "01"
        log.info("--- category number: $categoryNo")
        String portion = portionAtt ? "${portionAtt.trim()}$element$categoryNo" : null
        String districtCode = securityToken.getDistrict()
        String stdKey = "$contractNo$portion"

        log.info("--- contractAtt: $contractAtt")
        log.info("--- portionAtt: $portionAtt")

        log.info("--- districtCode: $districtCode")
        log.info("--- contractNo: $contractNo")
        log.info("--- portion: $portion")
        log.info("--- stdKey: $stdKey")

        if (!contractAtt) return null
        if (!portionAtt) return null

        String entityValue = "$districtCode$contractNo$portion"
        log.info("--- entityValue: $entityValue")

        String queryTaxText = "SELECT STD_KEY, TRIM(REPLACE(listagg(VAL, ' ' ON OVERFLOW TRUNCATE) within group (order by STD_KEY,STD_LINE_NO,source),'.HEADING')) " +
                "as merge_VAL " +
                "from ( " +
                "select STD_KEY,STD_LINE_NO, source, trim(val) val " +
                "from " +
                "  MSF096_STD_STATIC UNPIVOT INCLUDE NULLS " +
                "    ( VAL FOR( SOURCE ) IN " +
                "        ( STD_STATIC_1 AS 'STD_STATIC_1', " +
                "          STD_STATIC_2 AS 'STD_STATIC_2', " +
                "          STD_STATIC_3 AS 'STD_STATIC_3', " +
                "          STD_STATIC_4 AS 'STD_STATIC_4', " +
                "          STD_STATIC_5 AS 'STD_STATIC_5' " +
                "        ) " +
                "    ) " +
                "where STD_TEXT_CODE = 'GT' and trim(val) is not null and trim(std_key) = trim('$stdKey') " +
                "order by STD_KEY,STD_LINE_NO, source) " +
                "group by STD_KEY"

        def queryTaxTextResult = sql.firstRow(queryTaxText)
        log.info("--- queryTaxText: $queryTaxText")
        log.info("--- queryTaxTextResult: $queryTaxTextResult")
        if (queryTaxTextResult) {
            String taxText = queryTaxTextResult.merge_VAL.trim()
            log.info("--- taxText: $taxText")
            GenericScriptResult result3 = new GenericScriptResult()
            result3.addAttribute("taxText", taxText)
            results.add(result3)
        }
        return results
    }
}
