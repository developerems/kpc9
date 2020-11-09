
import com.mincom.ellipse.app.security.SecurityToken
import com.mincom.ellipse.edoi.ejb.msf071.MSF071Key
import com.mincom.ellipse.edoi.ejb.msf071.MSF071Rec
import com.mincom.ellipse.errors.UnlocalisedWarning
import com.mincom.ellipse.errors.exceptions.FatalException
import com.mincom.ellipse.script.plugin.GenericScriptExecute
import com.mincom.ellipse.script.plugin.GenericScriptPlugin
import com.mincom.ellipse.script.plugin.GenericScriptResult
import com.mincom.ellipse.script.plugin.GenericScriptResults
import com.mincom.ellipse.script.plugin.RequestAttributes
import groovy.sql.Sql

import javax.naming.InitialContext

class updateEquipTargetLife extends GenericScriptPlugin implements GenericScriptExecute{
    InitialContext initial = new InitialContext()
    Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
    def sql = new Sql(CAISource)

    @Override
    GenericScriptResults execute(SecurityToken securityToken, List<RequestAttributes> list, Boolean aBoolean) throws FatalException {
        String version = "1"
        String targetLifeUnit
        String equipTargetLife
        String equipmentNo

        log.info("updateEquipTargetLife Version: $version")
        GenericScriptResults results = new GenericScriptResults()
        GenericScriptResult result = new GenericScriptResult()

        RequestAttributes requestAttributes = list[0]

        if (aBoolean) {
            result.addWarning(new UnlocalisedWarning("MAKE SURE ALL DATA IS CORRECT, CONTINUE?"))
            results.add(result)
            return results
        }
        else {
            if (requestAttributes.getAttributeStringValue("equipmentNo")) {
                if (requestAttributes.getAttributeStringValue("equipmentNo").trim() != "") {
                    equipmentNo = requestAttributes.getAttributeStringValue("equipmentNo").trim()
                }
            }

            if (requestAttributes.getAttributeStringValue("newTargetLifeUnit")) {
                if (requestAttributes.getAttributeStringValue("newTargetLifeUnit").trim() != "") {
                    targetLifeUnit = requestAttributes.getAttributeStringValue("newTargetLifeUnit").trim()
                }
            }

            if (requestAttributes.getAttributeStringValue("newTargetLife")) {
                if (requestAttributes.getAttributeStringValue("newTargetLife").trim() != "") {
                    equipTargetLife = requestAttributes.getAttributeStringValue("newTargetLife")
                }
            }

            if (equipmentNo) {
                MSF071Rec msf071Rec = new MSF071Rec()
                MSF071Key msf071Key = new MSF071Key()

                msf071Key.setEntityType("EQP")
                msf071Key.setEntityValue(equipmentNo)
                msf071Key.setSeqNum("001")
                msf071Rec.setLastModUser(securityToken.getUserId())

                if (targetLifeUnit) {
                    if (targetLifeUnit.trim() != "") {
                        msf071Key.setRefNo("110")
                        msf071Rec.setPrimaryKey(msf071Key)
                        def searchResult = sql.firstRow("select * from msf071 where ENTITY_TYPE = 'EQP' and REF_NO = '110' and SEQ_NUM = '001' and ENTITY_VALUE = '$equipmentNo'")
                        if (searchResult) {
                            msf071Rec.setRefCode(targetLifeUnit)
                            edoi.update(msf071Rec)
                        }
                        else {
                            msf071Rec.setRefCode(targetLifeUnit)
                            edoi.create(msf071Rec)
                        }
                    }
                }
                if (equipTargetLife) {
                    if (equipTargetLife != "") {
                        msf071Key.setRefNo("100")
                        msf071Rec.setPrimaryKey(msf071Key)
                        def searchResult = sql.firstRow("select * from msf071 where ENTITY_TYPE = 'EQP' and REF_NO = '100' and SEQ_NUM = '001' and ENTITY_VALUE = '$equipmentNo'")
                        if (searchResult) {
                            msf071Rec.setRefCode(equipTargetLife)
                            edoi.update(msf071Rec)
                        }
                        else {
                            msf071Rec.setRefCode(equipTargetLife)
                            edoi.create(msf071Rec)
                        }
                    }
                }

                log.info("ars targetLifeUnit: $targetLifeUnit")
                log.info("ars equipTargetLife: $equipTargetLife")
                log.info("ars equipmentNo: $equipmentNo")
                log.info("ars UserNo: ${securityToken.getUserId()}")
                
                result.addAttribute("equipTargetLife", equipTargetLife)
                result.addAttribute("targetLifeUnit", targetLifeUnit)
                result.addAttribute("lastModEmp", securityToken.getUserId())
            }
        }
        results.add(result)
        return results
    }
}
