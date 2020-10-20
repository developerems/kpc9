/**
 * @EMS Mar 2019
 *
 * 20190314 - a9ra5213 - Eghy Kurniagus - KPC UPGRADE
 *            Initial Coding - Mandaroty TKDN
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

public class MSM24BA extends MSOHook{
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
		String STOCK = screen.getField("STOCK_CODE1I").getValue().trim();
		String RO_NO = screen.getField("RO_NO1I").getValue().trim();
		String Proc = screen.getField("PROCESS_CODE1I").getValue().trim();
		
		log.info("Refrence Code  : " + Proc)
		
		
		
		
		
		if(Proc.trim().equals("L")) {
			if (((screen.getNextAction() == 1) || (screen.getNextAction() == 0))) {
                                RO_NO = String.format("%03d",(Integer.parseInt(RO_NO)));
				def QRY1 = sql.firstRow("select * from msf071 where ENTITY_TYPE = 'PUR' and trim(ENTITY_VALUE) = trim('3"+tools.commarea.District.trim() + STOCK.trim() + RO_NO.trim() + "') ");
				log.info("Query 1 : " + QRY1)
				if(QRY1.equals(null)) {
					screen.setErrorMessage(new MsoErrorMessage("REF", "8542", "TKDN VALUE REQUIRED!", MsoErrorMessage.ERR_TYPE_ERROR, MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))
					screen.setCurrentCursorField(screen.getField("ACTION_11I"))
					return screen
				}
				
				
			}
			
		}
		
		
		return null;
	}
	@Override
	public GenericMsoRecord onPostSubmit(GenericMsoRecord input, GenericMsoRecord result) {
		log.info("Hooks onPostSubmit MSM24BA logging.version: ${hookVersion}");
		
		return result
	}
	private boolean isQuestionMarkOnScreen (GenericMsoRecord screen) {
		String screenData = screen.getCurrentScreenDetails().getScreenFields().toString()
		return screenData.contains("?")
	}
}
