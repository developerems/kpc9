import com.abb.screen.meta.bean.component.AccountCode
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.ellipse.types.m0000.instances.AccountCodeNW
import com.mincom.ellipse.types.m0000.instances.GatherableCostingType
import com.mincom.ellipse.types.m3110.instances.EnvoyGatherCostingDTO
import groovy.sql.Sql

import javax.naming.InitialContext

class EnvoyGatherCostingService_multipleCreate extends ServiceHook {
    String version = "2"
    InitialContext initial = new InitialContext()
    Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
    def sql = new Sql(CAISource)

    @Override
    Object onPreExecute(Object input){
        log.info("Hooks EnvoyGatherCostingService_multipleCreate onPreExecute version: $version")

        EnvoyGatherCostingDTO[] inp = (EnvoyGatherCostingDTO[]) input
        log.info("inp: $inp")
        log.info("inpLength: ${inp.length}")

        for (Integer i = 0 ;  i < inp.length ; i++){
            String generalLedgerAccount = inp[i].getGeneralLedgerAccount().getValue()
            String workOrder = inp[i].getWorkOrderNumber().getValue()
            log.info("--- ARS generalLedgerAccount: $generalLedgerAccount")
            log.info("--- ARS workOrder: $workOrder")
            log.info("--- ARS CostingType: ${inp[i].getCostingType().getValue()}")

            if (workOrder){
                String queryWO = "select * from msf620 where work_order = '$workOrder'"
                log.info("queryWO: $queryWO")
                def queryWOResult = sql.firstRow(queryWO)
                log.info("queryWOResult: $queryWOResult")
                if (queryWOResult){
                    String dstrctAcctCode = queryWOResult.DSTRCT_ACCT_CODE
                    log.info("dstrctAcctCode: $dstrctAcctCode")
                    if (dstrctAcctCode){
                        String woAccountCode = dstrctAcctCode.substring(4).trim()
                        log.info("--- ARS woAccountCode: $woAccountCode")
                        log.info("--- ARS generalLedgerAccount: $generalLedgerAccount")
                        if (woAccountCode != generalLedgerAccount){
                            AccountCodeNW aaa = new AccountCodeNW()
                            log.info("--- ARS aaa")
                            aaa.setValue(woAccountCode, true)
                            log.info("--- ARS aaa: $aaa")
                            inp[i].setGeneralLedgerAccount(aaa)
                        }
                    }
                }
            }
        }
        return null
    }
}
