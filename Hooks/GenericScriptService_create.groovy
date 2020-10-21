/**
* @EMS 2019
*
* Revision History
* 06-Jul-2019.............a9ra5213 - Initial code
* * */

import groovy.sql.Sql
import javax.naming.InitialContext
import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.ellipse.types.m1000.instances.GenericScriptDTO
import com.mincom.ellipse.types.m1000.instances.ScriptName

class GenericScriptService_create extends ServiceHook{

	String hookVersion = "1"
	
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	
	@Override
	Object onPreExecute(Object input) {
		log.info("Hooks onPreExecute logging.version: ${hookVersion}")
		Boolean readAction;
		readAction = false;
		String scrptName = "";
		
		GenericScriptDTO inp = (GenericScriptDTO) input
		
		String scName = inp.getScriptName().value;
		List<Attribute> custAttribs = inp.getCustomAttributes()
		custAttribs.each{Attribute customAttribute ->
			log.info ("attrName : " + customAttribute.getName());
			log.info ("attrValue : " + customAttribute.getValue());
			if (customAttribute.getName() == "sGp") {
				readAction = true;
				scrptName = "ell38sDetail";
			}
			if (customAttribute.getName() == "parGrdCicNo") {
				readAction = true;
				scrptName = "ELL38C_DETAIL";
			}
			
			if (customAttribute.getName() == "cntNo") {
				scrptName = "ELL38C_DETAIL";
			}
			
			if (customAttribute.getName() == "scrName" && customAttribute.getValue() == "ell38sDetail") {
				readAction = true;
				scrptName = "ell38sDetail";
			}
			
			if (customAttribute.getName() == "scrName" && customAttribute.getValue() == "ELL38C_DETAIL") {
				readAction = true;
				scrptName = "ELL38C_DETAIL";
			}
			if (customAttribute.getName() == "assetNo") {
				readAction = true;
				scrptName = "ell685Detail";
			}
			if (customAttribute.getName() == "scrName" && customAttribute.getValue() == "ell685Detail") {
				readAction = true;
				scrptName = "ell685Detail";
			}
		}
		if ((scName == null || scName == "") && (scrptName != "")) {
			ScriptName seScName = new ScriptName();
			seScName.setValue(scrptName);
			inp.setScriptName(seScName);
		}
		log.info("Script Name : " + inp.getScriptName().value);
		return null
	}
	@Override
	Object onPostExecute(Object input, Object result) {
		log.info("Hooks onPostExecute logging.version: ${hookVersion}")
		return result
	}
	
}