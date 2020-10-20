//package KPC.HOOKS
package com.mincom.ellipse.script.custom
/**
 * @EMS Mar 2019
 *
 * 20190626 - a9ra5213 - Eghy Kurniagus - KPC UPGRADE
 *            Initial Coding - Forward Fit from Ellipse 5 to validate Full Account code when create WO From Plan Std Job
 **/

import javax.naming.InitialContext

import com.mincom.ellipse.attribute.Attribute
import com.mincom.enterpriseservice.ellipse.*
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.ellipse.service.m3620.parentwowrapper.ParentWoWrapperService
import com.mincom.ellipse.types.m3620.instances.ParentWoWrapperDTO
import com.mincom.ellipse.types.m3620.instances.ParentWoWrapperServiceResult
//import com.mincom.ellipse.errors.Error



import com.mincom.enterpriseservice.exception.*

//import groovy.sql.Sql

class ParentWoWrapperService_multipleCreate extends ServiceHook {	
	String hookVersion = "1"
	String Acct = "";
	String StdJob = "";
	
	String StdJobKK = "";
	String StdJobKKK = "";
	
//	InitialContext initial = new InitialContext()
//	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
//	def sql = new Sql(CAISource)
//	
	@Override
	public Object onPreExecute(Object input) {
		
		log.info("WELOCMEEEEEE:")
		log.info("HAIHAIHAIHAIXXLKL UIUI:")
		log.info("Hooks WorkService_create onPreExecute logging.version: ${hookVersion}")
		
		ParentWoWrapperDTO[] e = (ParentWoWrapperDTO[]) input ;
		
		StdJobKKK = e[0].getParentStandardJob()
		
		ParentWoWrapperServiceResult[] results = new ParentWoWrapperServiceResult();
		log.info("OIIIIIIIIIIIIIIIIIIKK:" + StdJobKKK)

		/*throw new EnterpriseServiceOperationException(
			new ErrorMessageDTO(
			"9999", "INVALID ACCOUNT CODE !", "accountCode", 0, 0))
			return input*/
		if(e[0].getOverrideAccountCode().getValue().length()< 7){
			results[0].addError("9999","INVALID ACCOUNT CODE!");
			return results;
		}
      
		
		return null
	}
	
	@Override
	public Object onPostExecute(Object input, Object result) {
		log.info("Hooks WorkService_create onPostExecute logging.version: ${hookVersion}")
		
//		
		return result
	}
}