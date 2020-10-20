import javax.naming.InitialContext

import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.service.m3110.envoygather.EnvoyGatherService
import com.mincom.ellipse.types.m3110.instances.GatherableDTO
import com.mincom.ellipse.types.m3110.instances.GatherableServiceResult
import com.mincom.ellipse.errors.Error;
import com.mincom.ellipse.errors.Message;
import com.mincom.ellipse.errors.Warning;
import com.mincom.ellipse.service.ServiceDTO;
import com.mincom.enterpriseservice.exception.*
import groovy.sql.Sql
import com.mincom.enterpriseservice.ellipse.*

class EnvoyGatherService_multipleCreateItem extends ServiceHook{
	String hookVersion = "1"
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	String pesan = ""
	String[] ireqno
	String preqitemno=""
	String tampung = ""
	String ireqtype=""
	String dstrctcode=""
	String planUseDate =""
	String TOW=""
	String stdTextKey=""
	String creationdate=""
	String creationuser=""
	String creationtime=""
	String creationemp=""
	String projectnoA=""
	String projectnoB=""
	String projectA=""
	String projectB=""
	String dstrctcodeA="";
	
	String StrErr=""
	@Override
	public Object onPreExecute(Object input) {
		log.info("Hooks EnvoyGatherService_multipleCreateItem onPreExecute logging.version: $hookVersion")
		GatherableDTO[] e = (GatherableDTO[]) input
		//int panjang = 
		int i = 0;
		for (i=0;i<e.length;i++){
			List<Attribute> custAttribs = e[i].getCustomAttributes()
				custAttribs.each{Attribute customAttribute ->
				log.info ("attrName : " + customAttribute.getName());
			    log.info ("attrValue : " + customAttribute.getValue());
				}
		}
		return null;
	}
	@Override
	public Object onPostExecute(Object input, Object result) {
		log.info("Hooks EnvoyGatherService_multipleCreateItem onPostExecute logging.version: ${hookVersion}")
		log.info("planUseDatenya adalah " +planUseDate)
		return result;
		
	}

}
