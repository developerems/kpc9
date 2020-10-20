import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.ellipse.types.m3101.instances.CatalogueDTO
import com.mincom.ellipse.types.m3101.instances.CatalogueServiceResult

class CatalogueService_read extends ServiceHook{
    String hookVersion = "1"

    @Override
    Object onPostExecute(Object input, Object result){
        log.info("ARSIADI")

        CatalogueServiceResult reply = (CatalogueServiceResult) result

        CatalogueDTO catalogueDTO = reply.getCatalogueDTO()

        Attribute[] attributes = new Attribute[1]
        attributes[0] = new Attribute()
        attributes[0].setName("equipTargetLife")
        attributes[0].setValue("test")

        catalogueDTO.setCustomAttributes(attributes)
        reply.setCatalogueDTO(catalogueDTO)

        return reply
    }

}
