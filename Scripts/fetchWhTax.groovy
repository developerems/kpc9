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

class fetchWhTax extends GenericScriptPlugin implements GenericScriptExecute {
    String version = '1'

    InitialContext initial = new InitialContext()
    Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
    def sql = new Sql(CAISource)

    @Override
    GenericScriptResults execute(SecurityToken securityToken, List<RequestAttributes> list, Boolean aBoolean) throws FatalException {
        log.info("--- [ARSIADI] fetchWhTax.groovy execute version: $version")
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
        String whTaxType = "CIW"

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

        String queryWhTax = "SELECT * FROM MSF071 WHERE ENTITY_TYPE = '$whTaxType' AND ENTITY_VALUE = '$entityValue' AND REF_NO = '001' and SEQ_NUM = '001'"
        def queryWhTaxResult = sql.firstRow(queryWhTax)
        log.info("--- queryWhTax: $queryWhTax")
        log.info("--- queryWhTax result: $queryWhTaxResult")
        if (queryWhTaxResult) {
            String whTaxCode = queryWhTaxResult.REF_CODE.trim()
            log.info("--- whTaxCode: $whTaxCode")
            GenericScriptResult result2 = new GenericScriptResult()
            result2.addAttribute("wtCode", whTaxCode)
            results.add(result2)
        }
        return results
    }
}
