/**
 * @EMS Mar 2019
 *
 * 20190413 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Initial Coding - Hooks to change inv type in order to display internal and external comments.  
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

public class MSM263A extends MSOHook{
	String hookVersion = "1";
	
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
	def sql = new Sql(CAISource)
	
	String SUPP
	String INV
	String OPT
	@Override
	public GenericMsoRecord onDisplay(GenericMsoRecord screen){
		log.info("Hooks onDisplay MSM263A logging.version: ${hookVersion}");
		
		return null;
	}
	@Override
	public GenericMsoRecord onPreSubmit(GenericMsoRecord screen){
		log.info("Hooks onPreSubmit MSM263A logging.version: ${hookVersion}");
		SUPP = screen.getField("SUPPLIER_NO1I").getValue().trim();
		INV = screen.getField("INV_NO1I").getValue().trim();
		OPT = screen.getField("OPTION1I").getValue().trim();
		if (OPT.trim().equals("3")) {
			if (((screen.getNextAction() == 1) || (screen.getNextAction() == 0))) {
				def QRY1 = sql.firstRow("select * from msf260 where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and trim(supplier_no) = trim('"+SUPP+"') and trim(EXT_INV_NO) = trim('"+INV+"')");
				log.info ("QRY1: " + QRY1);
				if(!QRY1.equals(null)) {
					if (QRY1.INV_TYPE.trim().equals("6")) {
						//log.info ("UPDATE MSF260:");
						String QueryUpdate = ("update MSF260 set INV_TYPE = '4' where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and trim(supplier_no) = trim('"+SUPP+"') and trim(EXT_INV_NO) = trim('"+INV+"') ");
						sql.execute(QueryUpdate);
					}
				}
			}
		}
		return null;
	}
	@Override
	public GenericMsoRecord onPostSubmit(GenericMsoRecord screen, GenericMsoRecord result) {
		log.info("Hooks onPostSubmit MSM263A logging.version: ${hookVersion}");
		SUPP = screen.getField("SUPPLIER_NO1I").getValue().trim();
		INV = screen.getField("INV_NO1I").getValue().trim();
		if (OPT.trim().equals("3")) {
			if (((screen.getNextAction() == 1) || (screen.getNextAction() == 0))) {
				def QRY1 = sql.firstRow("select * from msf260 where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and trim(supplier_no) = trim('"+SUPP+"') and trim(EXT_INV_NO) = trim('"+INV+"')");
				log.info ("QRY1: " + QRY1);
				if(!QRY1.equals(null)) {
					if (QRY1.INV_TYPE.trim().equals("4") && !QRY1.CONTRACT_NO.trim().equals("")) {
						//log.info ("UPDATE MSF260:");
						String QueryUpdate = ("update MSF260 set INV_TYPE = '6' where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and trim(supplier_no) = trim('"+SUPP+"') and trim(EXT_INV_NO) = trim('"+INV+"') ");
						sql.execute(QueryUpdate);
					}
				}
			}
		}
		return result;
	}
	private boolean isQuestionMarkOnScreen (GenericMsoRecord screen) {
		String screenData = screen.getCurrentScreenDetails().getScreenFields().toString()
		return screenData.contains("?")
	}
}
