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

public class MSM232A extends MSOHook{
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
	String workOrdProj4 = "";
	String wpInd4 = "";
	String workOrdProj5 = "";
	String wpInd5 = "";
	String workOrdProj6 = "";
	String wpInd6 = "";
	String workOrdProj7 = "";
	String wpInd7 = "";
	@Override
	public GenericMsoRecord onDisplay(GenericMsoRecord screen){
		log.info("Hooks onDisplay MSM232A logging.version: ${hookVersion}");
		
		return null;
	}
	@Override
	public GenericMsoRecord onPreSubmit(GenericMsoRecord screen){
		log.info("Hooks onPreSubmit MSM232A logging.version: ${hookVersion}");
		def QRY1;
		workOrdProj1 = "";
		wpInd1 = "";
		workOrdProj2 = "";
		wpInd2 = "";
		workOrdProj3 = "";
		wpInd3 = "";
		workOrdProj4 = "";
		wpInd4 = "";
		workOrdProj5 = "";
		wpInd5 = "";
		workOrdProj6 = "";
		wpInd6 = "";
		workOrdProj7 = "";
		wpInd7 = "";
		
		workOrdProj1 = screen.getField("WO_PROJECT1I1").getValue();
		wpInd1 = screen.getField("PROJECT_IND1I1").getValue();
		workOrdProj2 = screen.getField("WO_PROJECT1I2").getValue();
		wpInd2 = screen.getField("PROJECT_IND1I2").getValue();
		workOrdProj3 = screen.getField("WO_PROJECT1I3").getValue();
		wpInd3 = screen.getField("PROJECT_IND1I3").getValue();
		workOrdProj4 = screen.getField("WO_PROJECT1I4").getValue();
		wpInd4 = screen.getField("PROJECT_IND1I4").getValue();
		workOrdProj5 = screen.getField("WO_PROJECT1I5").getValue();
		wpInd5 = screen.getField("PROJECT_IND1I5").getValue();
		workOrdProj6 = screen.getField("WO_PROJECT1I6").getValue();
		wpInd6 = screen.getField("PROJECT_IND1I6").getValue();
		workOrdProj7 = screen.getField("WO_PROJECT1I7").getValue();
		wpInd7 = screen.getField("PROJECT_IND1I7").getValue();
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
							PROJ_NO.setName("WO_PROJECT1I1")
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
							PROJ_NO.setName("WO_PROJECT1I2")
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
							PROJ_NO.setName("WO_PROJECT1I3")
							screen.setCurrentCursorField(PROJ_NO)
							return screen
						}
					}
				}
			}
			if (!workOrdProj4.equals(null) && !workOrdProj4.equals("") && !wpInd4.equals(null) && !wpInd4.equals("")) {
				if (wpInd4.equals("P")) {
					//Search Top Parent
					QRY1 = sql.firstRow("SELECT DISTINCT ML.PROJECT_NO FROM msf660 ml WHERE CONNECT_BY_ISLEAF = 1 START WITH ml.DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and trim(ML.PROJECT_NO) = '"+workOrdProj4.trim()+"' CONNECT BY ML.PROJECT_NO = prior ML.PARENT_PROJ");
					if(!QRY1.equals(null)) {
						workOrdProj4 = QRY1.PROJECT_NO
					}else {
						workOrdProj4 = "";
					}
					
					QRY1 = sql.firstRow("SELECT * FROM MSF071 where ENTITY_TYPE = 'PRJ' and trim(ENTITY_VALUE) = trim('"+tools.commarea.District.trim()+workOrdProj4.trim()+"') and REF_NO = '004'");
					if(!QRY1.equals(null)) {
						if(QRY1.REF_CODE.trim().equals("Y")) {
							screen.setErrorMessage(new MsoErrorMessage("", "9999", "PROJECT STATUS HOLD!", MsoErrorMessage.ERR_TYPE_ERROR, MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))
							MsoField PROJ_NO = new MsoField()
							PROJ_NO.setName("WO_PROJECT1I4")
							screen.setCurrentCursorField(PROJ_NO)
							return screen
						}
					}
				}
			}
			if (!workOrdProj5.equals(null) && !workOrdProj5.equals("") && !wpInd5.equals(null) && !wpInd5.equals("")) {
				if (wpInd5.equals("P")) {
					//Search Top Parent
					QRY1 = sql.firstRow("SELECT DISTINCT ML.PROJECT_NO FROM msf660 ml WHERE CONNECT_BY_ISLEAF = 1 START WITH ml.DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and trim(ML.PROJECT_NO) = '"+workOrdProj5.trim()+"' CONNECT BY ML.PROJECT_NO = prior ML.PARENT_PROJ");
					if(!QRY1.equals(null)) {
						workOrdProj5 = QRY1.PROJECT_NO
					}else {
						workOrdProj5 = "";
					}
					
					QRY1 = sql.firstRow("SELECT * FROM MSF071 where ENTITY_TYPE = 'PRJ' and trim(ENTITY_VALUE) = trim('"+tools.commarea.District.trim()+workOrdProj5.trim()+"') and REF_NO = '004'");
					if(!QRY1.equals(null)) {
						if(QRY1.REF_CODE.trim().equals("Y")) {
							screen.setErrorMessage(new MsoErrorMessage("", "9999", "PROJECT STATUS HOLD!", MsoErrorMessage.ERR_TYPE_ERROR, MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))
							MsoField PROJ_NO = new MsoField()
							PROJ_NO.setName("WO_PROJECT1I5")
							screen.setCurrentCursorField(PROJ_NO)
							return screen
						}
					}
				}
			}
			if (!workOrdProj6.equals(null) && !workOrdProj6.equals("") && !wpInd6.equals(null) && !wpInd6.equals("")) {
				if (wpInd6.equals("P")) {
					//Search Top Parent
					QRY1 = sql.firstRow("SELECT DISTINCT ML.PROJECT_NO FROM msf660 ml WHERE CONNECT_BY_ISLEAF = 1 START WITH ml.DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and trim(ML.PROJECT_NO) = '"+workOrdProj6.trim()+"' CONNECT BY ML.PROJECT_NO = prior ML.PARENT_PROJ");
					if(!QRY1.equals(null)) {
						workOrdProj6 = QRY1.PROJECT_NO
					}else {
						workOrdProj6 = "";
					}
					
					QRY1 = sql.firstRow("SELECT * FROM MSF071 where ENTITY_TYPE = 'PRJ' and trim(ENTITY_VALUE) = trim('"+tools.commarea.District.trim()+workOrdProj6.trim()+"') and REF_NO = '004'");
					if(!QRY1.equals(null)) {
						if(QRY1.REF_CODE.trim().equals("Y")) {
							screen.setErrorMessage(new MsoErrorMessage("", "9999", "PROJECT STATUS HOLD!", MsoErrorMessage.ERR_TYPE_ERROR, MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))
							MsoField PROJ_NO = new MsoField()
							PROJ_NO.setName("WO_PROJECT1I6")
							screen.setCurrentCursorField(PROJ_NO)
							return screen
						}
					}
				}
			}
			if (!workOrdProj7.equals(null) && !workOrdProj7.equals("") && !wpInd7.equals(null) && !wpInd7.equals("")) {
				if (wpInd7.equals("P")) {
					//Search Top Parent
					QRY1 = sql.firstRow("SELECT DISTINCT ML.PROJECT_NO FROM msf660 ml WHERE CONNECT_BY_ISLEAF = 1 START WITH ml.DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and trim(ML.PROJECT_NO) = '"+workOrdProj7.trim()+"' CONNECT BY ML.PROJECT_NO = prior ML.PARENT_PROJ");
					if(!QRY1.equals(null)) {
						workOrdProj7 = QRY1.PROJECT_NO
					}else {
						workOrdProj7 = "";
					}
					
					QRY1 = sql.firstRow("SELECT * FROM MSF071 where ENTITY_TYPE = 'PRJ' and trim(ENTITY_VALUE) = trim('"+tools.commarea.District.trim()+workOrdProj7.trim()+"') and REF_NO = '004'");
					if(!QRY1.equals(null)) {
						if(QRY1.REF_CODE.trim().equals("Y")) {
							screen.setErrorMessage(new MsoErrorMessage("", "9999", "PROJECT STATUS HOLD!", MsoErrorMessage.ERR_TYPE_ERROR, MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))
							MsoField PROJ_NO = new MsoField()
							PROJ_NO.setName("WO_PROJECT1I7")
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
		log.info("Hooks onPostSubmit MSM232A logging.version: ${hookVersion}");
		
		return result
	}
	private boolean isQuestionMarkOnScreen (GenericMsoRecord screen) {
		String screenData = screen.getCurrentScreenDetails().getScreenFields().toString()
		return screenData.contains("?")
	}
}
