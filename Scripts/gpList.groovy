package KPC
/**
 * @EMS Mei 2019
 *
 * a9ra5213 - Ricky Afriano - KPC - Ellipse Upgrade
 **/
import java.util.List

import javax.naming.InitialContext

import com.mincom.ellipse.app.security.SecurityToken
import com.mincom.ellipse.errors.exceptions.FatalException
import com.mincom.ellipse.script.plugin.GenericScriptExecuteForCollection
import com.mincom.ellipse.script.plugin.GenericScriptExecute
import com.mincom.ellipse.script.plugin.GenericScriptCreate
import com.mincom.ellipse.script.plugin.GenericScriptUpdate
import com.mincom.ellipse.script.plugin.GenericScriptDelete
import com.mincom.ellipse.script.plugin.GenericScriptPlugin
import com.mincom.ellipse.script.plugin.GenericScriptResult
import com.mincom.ellipse.script.plugin.GenericScriptResults
import com.mincom.ellipse.script.plugin.RequestAttributes
import com.mincom.ellipse.script.plugin.RestartAttributes
import com.mincom.ellipse.script.util.CommAreaScriptWrapper;

import groovy.sql.Sql

import com.mincom.eql.impl.*
import com.mincom.eql.*
import com.mincom.enterpriseservice.ellipse.*
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import com.mincom.ellipse.*

public class gpList extends GenericScriptPlugin implements GenericScriptExecuteForCollection{
	String version = "1";
			
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
	def sql = new Sql(CAISource)
	
	public GenericScriptResults executeForCollection(SecurityToken securityToken, RequestAttributes requestAttributes,
		Integer maxNumberOfObjects, RestartAttributes restartAttributes) throws FatalException {
		log.info("Exec for coll GP_LIST : " + version )
		RequestAttributes reqAtt = requestAttributes
		String strGP = reqAtt.getAttributeStringValue("gp");
		def results = new GenericScriptResults()
		
		String StrSQL = ""
		
		if(strGP.equals(null)){
			StrSQL = "select trim(ENTITY) ENTITY from msf020 where ENTRY_TYPE = 'G' order by ENTITY";
			log.info ("StrSQL : " + StrSQL);
			sql.eachRow(StrSQL, {
				GenericScriptResult result = new GenericScriptResult()
				result.addAttribute("gp", it.ENTITY.trim());
				results.add(result)
			})
		}else{
			StrSQL = "select trim(ENTITY) ENTITY from msf020 " +
					 "where ENTRY_TYPE = 'G' AND (upper(ENTITY) like '%'||upper('"+strGP+"')||'%') " +
					 "order by ENTITY";
			log.info ("StrSQL : " + StrSQL);
			sql.eachRow(StrSQL, {
				GenericScriptResult result = new GenericScriptResult()
				result.addAttribute("gp", it.ENTITY.trim());
				results.add(result)
			})
		}
		
		return results
	}
}
