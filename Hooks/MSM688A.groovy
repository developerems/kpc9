/**
 * @EMS Mar 2019
 *
 * 20190307 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Initial Coding - Forward Fit User Exit to change active flag and costing flag when disposal WO fix assets
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
import com.mincom.ellipse.ejra.mso.MsoField ;
import java.util.Calendar;


public class MSM688A extends MSOHook{
	String hookVersion = "1";
	
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
	def sql = new Sql(CAISource)
	
	@Override
	public GenericMsoRecord onDisplay(GenericMsoRecord screen){
		log.info("Hooks onDisplay MSM688A logging.version: ${hookVersion}");
		
		return null;
	}
	
	@Override
	public GenericMsoRecord onPreSubmit(GenericMsoRecord screen){
		log.info("Hooks onPreSubmit MSM688A logging.version: ${hookVersion}");
		
		return null;
	}
	@Override
	public GenericMsoRecord onPostSubmit(GenericMsoRecord input, GenericMsoRecord result) {
		log.info("Hooks onPostSubmit MSM688A logging.version: ${hookVersion}");
		
		if ( ((input.getNextAction() == 1) || (input.getNextAction() == 0))) {
			String StrFKEYS = input.getFunctionKeyLine().getValue();
			log.info("StrFKEYS : " + StrFKEYS);
			if (!StrFKEYS.trim().equals("")){
				if (StrFKEYS.contains("XMIT-Confirm") == true){
					String DISPOSAL_CODE1I = input.getField("DISPOSAL_CODE1I").getValue().trim();
					if (DISPOSAL_CODE1I.equals("WO")) {
						String EQUIP_REF1I = input.getField("EQUIP_REF1I").getValue();
						log.info("EQUIP_REF1I : " + EQUIP_REF1I)
						String SUB_ASSET_NO1I = input.getField("SUB_ASSET_NO1I").getValue();
						SUB_ASSET_NO1I = String.format("%06d",(Integer.parseInt(SUB_ASSET_NO1I)));
						def QRY1 = sql.firstRow("select * from msf685 where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and ASSET_TY = 'E' and RTRIM(ASSET_NO) = RTRIM('"+EQUIP_REF1I+"') and SUB_ASSET_NO = '"+SUB_ASSET_NO1I+"'");
						if(!QRY1.equals(null)) {
							log.info ("UPDATE MSF600:");
							String QueryUpdate = ("update MSF600 set ACTIVE_FLG = 'N',EQUIP_STATUS = 'WO',COSTING_FLG = 'E' where EQUIP_NO = '"+EQUIP_REF1I+"'");
							sql.execute(QueryUpdate);
						}
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
