/**
 * @EMS Mar 2019
 *
 * 20190619 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Initial Coding - Hooks to show invoice description for contract invoice.  
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

public class MSM263C extends MSOHook{
	String hookVersion = "1";
	
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
	def sql = new Sql(CAISource)
	
	String SUPP
	String INV
	String CONT
	String ValNo
	
	@Override
	public GenericMsoRecord onDisplay(GenericMsoRecord screen){
		log.info("Hooks onDisplay MSM263C logging.version: ${hookVersion}");
		CONT = screen.getField("CONT_PO3I").getValue().trim();
		ValNo = screen.getField("PORT_ITEM3I").getValue().trim();
		
		def QRY7 = sql.firstRow("select * from msf071 where ENTITY_TYPE = 'CIV' and ENTITY_VALUE like '"+tools.commarea.District.trim()+CONT+"%' and REF_NO = '001' and SEQ_NUM = '001' and trim(REF_CODE) = trim('"+ValNo.trim()+"')");
		log.info ("FIND VALN_NO  : " + QRY7);
		if(!QRY7.equals(null)) {
			String CALC_CIC_NO = QRY7.ENTITY_VALUE.toString().replace(tools.commarea.District.trim(), "");
			CALC_CIC_NO = CALC_CIC_NO.replace(CONT.trim(), "");
			if (!CALC_CIC_NO.trim().equals("")) {
				def QRY2;
				QRY2 = sql.firstRow("select * from ACA.KPF38F where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CONT.trim()+"')) and CIC_NO = '"+CALC_CIC_NO+"'");
				if(!QRY2.equals(null)) {
					screen.getField("INV_ITEM_DESC3I").setIsProtected(false);
					screen.getField("INV_ITEM_DESC3I").setValue(QRY2.CIC_DESC)
					screen.getField("INV_ITEM_DESC3I").setIsProtected(true);
				}
			}
		}
		return null;
	}
	@Override
	public GenericMsoRecord onPreSubmit(GenericMsoRecord screen){
		log.info("Hooks onPreSubmit MSM263C logging.version: ${hookVersion}");
		
		return null;
	}
	@Override
	public GenericMsoRecord onPostSubmit(GenericMsoRecord screen, GenericMsoRecord result) {
		log.info("Hooks onPostSubmit MSM263C logging.version: ${hookVersion}");
		
		return result;
	}
	private boolean isQuestionMarkOnScreen (GenericMsoRecord screen) {
		String screenData = screen.getCurrentScreenDetails().getScreenFields().toString()
		return screenData.contains("?")
	}
}
