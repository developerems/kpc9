package KPC

import javax.naming.InitialContext

import com.mincom.ellipse.app.security.SecurityToken
import com.mincom.ellipse.errors.exceptions.FatalException
import com.mincom.ellipse.script.plugin.GenericScriptExecuteForCollection
import com.mincom.ellipse.script.plugin.GenericScriptPlugin
import com.mincom.ellipse.script.plugin.GenericScriptResult
import com.mincom.ellipse.script.plugin.GenericScriptResults
import com.mincom.ellipse.script.plugin.RequestAttributes
import com.mincom.ellipse.script.plugin.RestartAttributes
import groovy.sql.Sql
import com.mincom.ellipse.errors.CobolMessages

public class ell38sSearch extends GenericScriptPlugin implements GenericScriptExecuteForCollection {
	String version = "1";
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	String StrErr = "";
	GenericScriptResults executeForCollection(SecurityToken securityToken, RequestAttributes requestAttributes,
		Integer maxNumberOfObjects, RestartAttributes restartAttributes) throws FatalException{
			log.info("ell38sSearch executeForCollection : " + version );
			def results = new GenericScriptResults();
			RequestAttributes reqAtt = requestAttributes;
			String app = reqAtt.getAttributeStringValue("app");
			StrErr = "";
			if (app.equals(null)) {
				GenericScriptResult result = new GenericScriptResult()
				StrErr = "APPLICATION REQUIRED!"
				SetErrMes();
				com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
				err.setFieldId("app")
				result.addError(err)
				results.add(result)
				RollErrMes();
				return results
			}else {
				if (app.trim().equals("")) {
					GenericScriptResult result = new GenericScriptResult()
					StrErr = "APPLICATION REQUIRED!"
					SetErrMes();
					com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
					err.setFieldId("app")
					result.addError(err)
					results.add(result)
					RollErrMes();
					return results
				}else {
					if (!app.trim().equals("ELL38C") && !app.trim().equals("ELL38I")) {
						GenericScriptResult result = new GenericScriptResult()
						StrErr = "INVALID APPLICATION!"
						SetErrMes();
						com.mincom.ellipse.errors.Error err = new com.mincom.ellipse.errors.Error(CobolMessages.ID_8541)
						err.setFieldId("app")
						result.addError(err)
						results.add(result)
						RollErrMes();
						return results
					}
				}
			}
			
			String StrSQL = ""
			StrSQL = "select * from msf02a where APPLICATION_NAME = '"+app+"' and ENTRY_TYPE = 'G' order by ENTITY";
			log.info ("StrSQL : " + StrSQL);
			Integer CNT = 0;
			sql.eachRow(StrSQL, {
				CNT = CNT + 1;
				GenericScriptResult result = new GenericScriptResult();
				
				if (restartAttributes.equals(null)){
					result.addAttribute("sGp", it.ENTITY);
					result.addAttribute("gp", it.ENTITY);
					result.addAttribute("appDetail", app);
					results.add(result);
				}
			})
			
			return results;
	}
	private String SetErrMes(){
		String Qerr = "UPDATE msf010 set TABLE_DESC = ? where TABLE_type = 'ER' and TABLE_CODE = '8541'";
		try
		{
			def QueryRes5 = sql.execute(Qerr,StrErr);
		} catch (Exception  e) {
			log.info ("Exception is : " + e);
		}
	}
	private String RollErrMes(){
		StrErr = "ERROR -"
		String Qerr = "UPDATE msf010 set TABLE_DESC = ? where TABLE_type = 'ER' and TABLE_CODE = '8541'";
		try
		{
			def QueryRes5 = sql.execute(Qerr,StrErr);
		} catch (Exception  e) {
			log.info ("Exception is : " + e);
		}
	}
}