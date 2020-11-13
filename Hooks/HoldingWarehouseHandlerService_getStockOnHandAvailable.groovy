package kpc9.Hooks

import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.ellipse.types.m3140.instances.HoldingWarehouseHandlerDTO
import com.mincom.ellipse.types.m3140.instances.HoldingWarehouseHandlerServiceResult
import groovy.sql.Sql

import javax.naming.InitialContext

class HoldingWarehouseHandlerService_getStockOnHandAvailable extends ServiceHook{
    String hookVersion = "1"

    InitialContext initial = new InitialContext()
    Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
    def sql = new Sql(CAISource)

    @Override
    Object onPostExecute(Object input, Object searchResult){
        log.info("Hooks HoldingWarehouseHandlerService_getStockOnHandAvailable onPostExecute logging.version: $hookVersion")
        HoldingWarehouseHandlerServiceResult result = (HoldingWarehouseHandlerServiceResult) searchResult
        HoldingWarehouseHandlerDTO holdingWarehouseHandlerDTO = result.getHoldingWarehouseHandlerDTO()
        log.info("StockCode: ${holdingWarehouseHandlerDTO.getStockCode().getValue()}")
        log.info("districtCode: ${holdingWarehouseHandlerDTO.getDistrictCode().getValue()}")
        log.info("Warehouse: ${holdingWarehouseHandlerDTO.getWarehouseId().getValue()}")
        if (holdingWarehouseHandlerDTO.getStockCode()){
            String query = "select * from msf170 where stock_code = '${holdingWarehouseHandlerDTO.getStockCode().getValue()}' and dstrct_code = '${holdingWarehouseHandlerDTO.getDistrictCode().getValue()}'"
            log.info("query: $query")
            def query1 = sql.firstRow("select * from msf170 where stock_code = '${holdingWarehouseHandlerDTO.getStockCode().getValue()}' and dstrct_code = '${holdingWarehouseHandlerDTO.getDistrictCode().getValue()}'")
            log.info(("whouseID: ${query1.HOME_WHOUSE}"))
            String homeWhouse = query1.HOME_WHOUSE
            if (homeWhouse){
                holdingWarehouseHandlerDTO.getWarehouseId().setValue(homeWhouse)
                result.setHoldingWarehouseHandlerDTO(holdingWarehouseHandlerDTO)
            }
        }
        return result
    }
}
