/**
 * @EMS Mar 2019
 *
 * 20190314 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Initial Coding - Recalculate PO Auth Value in order to fix PO Value in MSEAPM
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

public class MSM22CA extends MSOHook{
	String hookVersion = "1";

	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
	def sql = new Sql(CAISource)

	@Override
	public GenericMsoRecord onDisplay(GenericMsoRecord screen){
		log.info("Hooks onDisplay MSM22CA logging.version: ${hookVersion}");

		return null;
	}
	@Override
	public GenericMsoRecord onPreSubmit(GenericMsoRecord screen){
		log.info("Hooks onPreSubmit MSM22CA logging.version: ${hookVersion}");
		String PO_NO = screen.getField("PO_NO1I").getValue().trim();
		if (((screen.getNextAction() == 1) || (screen.getNextAction() == 0))) {
			String ERRMESS1I = "";
			ERRMESS1I = screen.getField("ERRMESS1I").getValue().trim();
			log.info("ERRMESS1I : " + ERRMESS1I);
			if (ERRMESS1I.contains("WARNING - TOTAL VALUE REQUIRES AUTHORISATION")) {
				Integer intStart = ERRMESS1I.indexOf("WARNING - TOTAL VALUE REQUIRES AUTHORISATION");
				Integer intEnd = ERRMESS1I.length();
				String ErrMess = ERRMESS1I.substring(intStart-1,intEnd);
				String StrPoVal = ErrMess.replace("WARNING - TOTAL VALUE REQUIRES AUTHORISATION", "").trim();
				log.info("PO_NO : " + PO_NO);
				log.info("StrPoVal : " + StrPoVal);
				String QryUpd = ("update msf220 set AUTHSD_TOT_AMT = ? where trim(PO_NO) = trim('"+PO_NO+"')");
				sql.execute(QryUpd,StrPoVal);
			}
		}
		return null;
	}
	@Override
	public GenericMsoRecord onPostSubmit(GenericMsoRecord input, GenericMsoRecord result) {
		log.info("Hooks onPostSubmit MSM22CA logging.version: ${hookVersion}");

		return result
	}
	private boolean isQuestionMarkOnScreen (GenericMsoRecord screen) {
		String screenData = screen.getCurrentScreenDetails().getScreenFields().toString()
		return screenData.contains("?")
	}
}
