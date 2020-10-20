/**
 * @EMS Mar 2019
 *
 * 20190307 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Initial Coding - Delete TOW data in MSM23BA
 **/
package KPC.HOOKS

import com.mincom.ellipse.ejra.mso.GenericMsoRecord;
import com.mincom.ellipse.ejra.mso.MsoErrorMessage;
import com.mincom.ellipse.hook.hooks.MSOHook;
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException;

import groovy.sql.Sql;

import com.mincom.eql.impl.QueryImpl;

import javax.naming.InitialContext;
import javax.persistence.criteria.CriteriaBuilder.Trimspec;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.mincom.ellipse.ejra.mso.MsoField;

import java.util.Calendar;

import com.mincom.ellipse.edoi.ejb.msf001.MSF001_DC0031Key
import com.mincom.ellipse.edoi.ejb.msf001.MSF001_DC0031Rec
import com.mincom.ellipse.edoi.ejb.msf071.MSF071Key
import com.mincom.ellipse.edoi.ejb.msf071.MSF071Rec

public class MSM23BA extends MSOHook{
	String hookVersion = "1";
	
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
	def sql = new Sql(CAISource)
	
	@Override
	public GenericMsoRecord onDisplay(GenericMsoRecord screen){
		log.info("Hooks onDisplay MSM23BA logging.version: ${hookVersion}");
		
		return null;
	}
	@Override
	public GenericMsoRecord onPreSubmit(GenericMsoRecord screen){
		log.info("Hooks onPreSubmit MSM23BA logging.version: ${hookVersion}");
		
		return null;
	}
	@Override
	public GenericMsoRecord onPostSubmit(GenericMsoRecord input, GenericMsoRecord result) {
		log.info("Hooks onPostSubmit MSM23BA logging.version: ${hookVersion}");
		String ACTION = input.getField("DEL_MOD1I").getValue().trim();
		String PR_NO = input.getField("PREQ_NO1I").getValue().trim();
		String StrFKEYS = input.getFunctionKeyLine().getValue()
		log.info("StrFKEYS : " + StrFKEYS)
		if (!StrFKEYS.trim().equals("")){
			if (StrFKEYS.contains("XMIT-Confirm") == true){
				if (ACTION.trim().equals("Y")) {
					def QRY1 = sql.firstRow("select * from msf071 where ENTITY_TYPE = 'TOW' and trim(ENTITY_VALUE) like trim('"+tools.commarea.District.trim() + PR_NO.trim()+ "%') ");
					if(!QRY1.equals(null)) {
						log.info ("DELETE MSF071:");
						String QueryDelete = ("delete MSF071 where ENTITY_TYPE = 'TOW' and trim(ENTITY_VALUE) like trim('"+tools.commarea.District.trim() + PR_NO.trim()+ "%') ");
						sql.execute(QueryDelete);
					}
				}
			}
		}
		return result
	}
	private boolean isQuestionMarkOnScreen (GenericMsoRecord screen) {
		String screenData = screen.getCurrentScreenDetails().getScreenFields().toString()
		return screenData.contains("?")
	}
}
