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

class fetchPlanUseDate extends GenericScriptPlugin implements GenericScriptExecute {
    String version = '1'

    InitialContext initial = new InitialContext()
    Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
    def sql = new Sql(CAISource)

    @Override
    GenericScriptResults execute(SecurityToken securityToken, List<RequestAttributes> list, Boolean aBoolean) throws FatalException {
        log.info("--- [DANOVSKY] fetchPlanUseDate.groovy execute version: $version")
        GenericScriptResults results = new GenericScriptResults()

        RequestAttributes requestAttributes = list[0]
        String ireqNoAtt = requestAttributes.getAttributeStringValue("ireqNo")
        String ireqNo = ireqNoAtt ? ireqNoAtt.trim() : null
        String ireqTypeAtt = requestAttributes.getAttributeStringValue("ireqType")
        String ireqType = ireqTypeAtt ? "${ireqTypeAtt.trim()}" : null
        String districtCode = securityToken.getDistrict()

        log.info("--- ireqNoAtt: $ireqNoAtt")
        log.info("--- ireqTypeAtt: $ireqTypeAtt")

        log.info("--- districtCode: $districtCode")
        log.info("--- ireqNo: $ireqNo")
        log.info("--- ireqType: $ireqType")

        if (!ireqNoAtt) return null
        if (!ireqTypeAtt) return null

        if(ireqType.equals("NI")){
           String queryPUD = "select * from msf071 where ENTITY_TYPE = 'PUD' and upper(trim(ENTITY_VALUE)) = upper(trim('"+securityToken.getDistrict()+ireqNo+"')) and REF_NO = '001' and SEQ_NUM = '001'"

           def QRY1 = sql.firstRow(queryPUD);
           log.info("--- queryTaxText: $queryPUD")
           if (QRY1) {
              String planUseDate = QRY1.REF_CODE.trim()
              log.info("--- planUseDate: $planUseDate")
              GenericScriptResult result3 = new GenericScriptResult()
              result3.addAttribute("planUseDate", planUseDate)
              results.add(result3)
        }
      }
        return results
    }
}
