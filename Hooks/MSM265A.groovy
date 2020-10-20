/**
 *
 * 20190902 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Initial Coding - Prevent transaction For Project that match criteria.  
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

public class MSM265A extends MSOHook{
	String hookVersion = "1";
	
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
	def sql = new Sql(CAISource)
	
	String workOrdProj1 = "";
	String wpInd1 = "";
	String workOrdProj2 = "";
	String wpInd2 = "";
	String workOrdProj3 = "";
	String wpInd3 = "";
	@Override
	public GenericMsoRecord onDisplay(GenericMsoRecord screen){
		log.info("Hooks onDisplay MSM265A logging.version: ${hookVersion}");
		
		return null;
	}
	@Override
	public GenericMsoRecord onPreSubmit(GenericMsoRecord screen){
		log.info("Hooks onPreSubmit MSM265A logging.version: ${hookVersion}");
		def QRY1;
		workOrdProj1 = "";
		wpInd1 = "";
		workOrdProj2 = "";
		wpInd2 = "";
		workOrdProj3 = "";
		wpInd3 = "";
		workOrdProj1 = screen.getField("WORK_ORDER1I1").getValue();
		wpInd1 = screen.getField("PROJECT_IND1I1").getValue();
		workOrdProj2 = screen.getField("WORK_ORDER1I2").getValue();
		wpInd2 = screen.getField("PROJECT_IND1I2").getValue();
		workOrdProj3 = screen.getField("WORK_ORDER1I3").getValue();
		wpInd3 = screen.getField("PROJECT_IND1I3").getValue();
		if ( ((screen.getNextAction() == 1) || (screen.getNextAction() == 0))) {
			if (!workOrdProj1.equals(null) && !workOrdProj1.equals("") && !wpInd1.equals(null) && !wpInd1.equals("")) {
				if (wpInd1.equals("P")) {
					//Search Top Parent
					QRY1 = sql.firstRow("SELECT DISTINCT ML.PROJECT_NO FROM msf660 ml WHERE CONNECT_BY_ISLEAF = 1 START WITH ml.DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and trim(ML.PROJECT_NO) = '"+workOrdProj1.trim()+"' CONNECT BY ML.PROJECT_NO = prior ML.PARENT_PROJ");
					if(!QRY1.equals(null)) {
						workOrdProj1 = QRY1.PROJECT_NO
					}else {
						workOrdProj1 = "";
					}
					
					QRY1 = sql.firstRow("SELECT * FROM MSF071 where ENTITY_TYPE = 'PRJ' and trim(ENTITY_VALUE) = trim('"+tools.commarea.District.trim()+workOrdProj1.trim()+"') and REF_NO = '004'");
					if(!QRY1.equals(null)) {
						if(QRY1.REF_CODE.trim().equals("Y")) {
							screen.setErrorMessage(new MsoErrorMessage("", "9999", "PROJECT STATUS HOLD!", MsoErrorMessage.ERR_TYPE_ERROR, MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))
							MsoField PROJ_NO = new MsoField()
							PROJ_NO.setName("WORK_ORDER1I1")
							screen.setCurrentCursorField(PROJ_NO)
							return screen
						}
					}
				}
			}
			if (!workOrdProj2.equals(null) && !workOrdProj2.equals("") && !wpInd2.equals(null) && !wpInd2.equals("")) {
				if (wpInd2.equals("P")) {
					//Search Top Parent
					QRY1 = sql.firstRow("SELECT DISTINCT ML.PROJECT_NO FROM msf660 ml WHERE CONNECT_BY_ISLEAF = 1 START WITH ml.DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and trim(ML.PROJECT_NO) = '"+workOrdProj2.trim()+"' CONNECT BY ML.PROJECT_NO = prior ML.PARENT_PROJ");
					if(!QRY1.equals(null)) {
						workOrdProj2 = QRY1.PROJECT_NO
					}else {
						workOrdProj2 = "";
					}
					
					QRY1 = sql.firstRow("SELECT * FROM MSF071 where ENTITY_TYPE = 'PRJ' and trim(ENTITY_VALUE) = trim('"+tools.commarea.District.trim()+workOrdProj2.trim()+"') and REF_NO = '004'");
					if(!QRY1.equals(null)) {
						if(QRY1.REF_CODE.trim().equals("Y")) {
							screen.setErrorMessage(new MsoErrorMessage("", "9999", "PROJECT STATUS HOLD!", MsoErrorMessage.ERR_TYPE_ERROR, MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))
							MsoField PROJ_NO = new MsoField()
							PROJ_NO.setName("WORK_ORDER1I2")
							screen.setCurrentCursorField(PROJ_NO)
							return screen
						}
					}
				}
			}
			if (!workOrdProj3.equals(null) && !workOrdProj3.equals("") && !wpInd3.equals(null) && !wpInd3.equals("")) {
				if (wpInd3.equals("P")) {
					//Search Top Parent
					QRY1 = sql.firstRow("SELECT DISTINCT ML.PROJECT_NO FROM msf660 ml WHERE CONNECT_BY_ISLEAF = 1 START WITH ml.DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and trim(ML.PROJECT_NO) = '"+workOrdProj3.trim()+"' CONNECT BY ML.PROJECT_NO = prior ML.PARENT_PROJ");
					if(!QRY1.equals(null)) {
						workOrdProj3 = QRY1.PROJECT_NO
					}else {
						workOrdProj3 = "";
					}
					
					QRY1 = sql.firstRow("SELECT * FROM MSF071 where ENTITY_TYPE = 'PRJ' and trim(ENTITY_VALUE) = trim('"+tools.commarea.District.trim()+workOrdProj3.trim()+"') and REF_NO = '004'");
					if(!QRY1.equals(null)) {
						if(QRY1.REF_CODE.trim().equals("Y")) {
							screen.setErrorMessage(new MsoErrorMessage("", "9999", "PROJECT STATUS HOLD!", MsoErrorMessage.ERR_TYPE_ERROR, MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))
							MsoField PROJ_NO = new MsoField()
							PROJ_NO.setName("WORK_ORDER1I3")
							screen.setCurrentCursorField(PROJ_NO)
							return screen
						}
					}
				}
			}
		}
		return null;
	}
	@Override
	public GenericMsoRecord onPostSubmit(GenericMsoRecord input, GenericMsoRecord result) {
		log.info("Hooks onPostSubmit MSM265A logging.version: ${hookVersion}");
		
		return result
	}
	private boolean isQuestionMarkOnScreen (GenericMsoRecord screen) {
		String screenData = screen.getCurrentScreenDetails().getScreenFields().toString()
		return screenData.contains("?")
	}
}
