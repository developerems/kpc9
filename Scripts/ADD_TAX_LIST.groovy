package KPC
/**
* @EMS Jan 2019
*
* 20190101 - a9ra5213 - Ricky Afriano - KPC UPGRADE
*            Initial Coding - Display Additional Tax data into dropdown component in ELL38I Screen
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

public class ADD_TAX_LIST extends GenericScriptPlugin implements GenericScriptExecuteForCollection{
   String version = "1";
		   
   InitialContext initial = new InitialContext()
   Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
   def sql = new Sql(CAISource)

	@Override
   GenericScriptResults executeForCollection(SecurityToken securityToken, RequestAttributes requestAttributes, Integer maxNumberOfObjects,
											 RestartAttributes restartAttributes) throws FatalException {
		log.info("Exec for coll ADD_TAX_LIST: $version")
		RequestAttributes reqAtt = requestAttributes
		String strSearch = reqAtt.getAttributeStringValue("PARAM")
		log.info("strSearch: $strSearch")
		def results = new GenericScriptResults();

		String StrSQL = ""
		if(strSearch.equals(null)){
			log.info("maxNumberOfObjects : " + maxNumberOfObjects );
			if (restartAttributes.equals(null)){
				StrSQL = "SELECT row_number () over(order by ATAX_CODE) NO,ATAX_CODE,DESCRIPTION FROM msf013 " +
						"ORDER BY ATAX_CODE OFFSET 0 ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY" ;
				log.info ("StrSQL : " + StrSQL);
				sql.eachRow(StrSQL, {
					GenericScriptResult result = new GenericScriptResult()
					result.addAttribute("custAtaxCode", it.ATAX_CODE)
					result.addAttribute("custAtaxDesc", it.DESCRIPTION)
					result.addAttribute("lastRow", maxNumberOfObjects.toString())
					results.add(result)
			   })
			} else {
				log.info("restartAttributes : " + restartAttributes.getAttributeStringValue("lastRow") );
				Integer MaxInst = Integer.parseInt(restartAttributes.getAttributeStringValue("lastRow"));
				//MaxInst = MaxInst + maxNumberOfObjects
				StrSQL = "SELECT row_number () over(order by ATAX_CODE) NO,ATAX_CODE,DESCRIPTION FROM msf013 " +
						"ORDER BY ATAX_CODE OFFSET "+MaxInst.toString()+" ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY" ;
				log.info ("StrSQL : " + StrSQL);
				sql.eachRow(StrSQL, {
					GenericScriptResult result = new GenericScriptResult();
				    MaxInst = it.NO
				    result.addAttribute("custAtaxCode", it.ATAX_CODE);
				    result.addAttribute("custAtaxDesc", it.DESCRIPTION);
				    result.addAttribute("lastRow", MaxInst.toString());
				    results.add(result);
				})
			}
		} else {
			log.info("maxNumberOfObjects : " + maxNumberOfObjects );
			if (restartAttributes.equals(null)) {
				StrSQL = "SELECT row_number () over(order by ATAX_CODE) NO,ATAX_CODE,DESCRIPTION FROM msf013 " +
						"where ((upper(ATAX_CODE) like '%'||upper('"+strSearch+"')||'%') or (upper(DESCRIPTION) like '%'||upper('"+strSearch+"')||'%')) " +
						"ORDER BY ATAX_CODE OFFSET 0 ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY" ;
				log.info ("StrSQL : " + StrSQL);
				sql.eachRow(StrSQL, {
					GenericScriptResult result = new GenericScriptResult();
				    result.addAttribute("custAtaxCode", it.ATAX_CODE);
				    result.addAttribute("custAtaxDesc", it.DESCRIPTION);
				    result.addAttribute("lastRow", maxNumberOfObjects.toString());
				    results.add(result);
				})
			} else {
				log.info("restartAttributes : " + restartAttributes.getAttributeStringValue("lastRow") );
			    Integer MaxInst = Integer.parseInt(restartAttributes.getAttributeStringValue("lastRow"));
			    //MaxInst = MaxInst + maxNumberOfObjects
			    StrSQL = "SELECT row_number () over(order by ATAX_CODE) NO,ATAX_CODE,DESCRIPTION FROM msf013 " +
						"where ((upper(ATAX_CODE) like '%'||upper('"+strSearch+"')||'%') or (upper(DESCRIPTION) like '%'||upper('"+strSearch+"')||'%')) " +
						"ORDER BY ATAX_CODE OFFSET "+MaxInst.toString()+" ROWS FETCH NEXT "+maxNumberOfObjects.toString()+" ROWS ONLY" ;
				log.info ("StrSQL : " + StrSQL);
				sql.eachRow(StrSQL, {
					GenericScriptResult result = new GenericScriptResult();
				    MaxInst = it.NO
				    result.addAttribute("custAtaxCode", it.ATAX_CODE);
				    result.addAttribute("custAtaxDesc", it.DESCRIPTION);
				    result.addAttribute("lastRow", MaxInst.toString());
				    results.add(result);
				})
			}
		}
		return results
	}
}
