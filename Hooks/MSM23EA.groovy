/**
 * @EMS Mar 2019
 *
 * 20190314 - 39hyH45g - Eghy Kurniagus - KPC UPGRADE
 *            - mandatory TKDN when finalise into a live PO 
 * 20190307 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Initial Coding - Show TOW data in MSM23EA
 **/
package KPC.HOOKS

import com.mincom.ellipse.ejra.mso.GenericMsoRecord;
import com.mincom.ellipse.ejra.mso.MsoErrorMessage;
import com.mincom.ellipse.hook.hooks.MSOHook;
import com.mincom.enterpriseservice.ellipse.ConnectionId
import com.mincom.enterpriseservice.ellipse.EllipseScreenService
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException;

import groovy.sql.Sql;

import com.mincom.eql.impl.QueryImpl;

import javax.naming.InitialContext;
import javax.persistence.criteria.CriteriaBuilder.Trimspec;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.mincom.ellipse.ejra.mso.MsoField ;

import java.util.Calendar;

import com.mincom.ellipse.edoi.ejb.msf001.MSF001_DC0031Key
import com.mincom.ellipse.edoi.ejb.msf001.MSF001_DC0031Rec
import com.mincom.ellipse.edoi.ejb.msf071.MSF071Key
import com.mincom.ellipse.edoi.ejb.msf071.MSF071Rec
import com.mincom.enterpriseservice.ellipse.*
import com.mincom.ellipse.client.connection.*
import com.mincom.ellipse.ejra.mso.*;
import com.mincom.ellipse.service.ServiceDTO;

public class MSM23EA extends MSOHook{
	String hookVersion = "1";
	
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
	def sql = new Sql(CAISource)
	
	EllipseScreenService screenService = EllipseScreenServiceLocator.ellipseScreenService;
	ConnectionId msoCon = ConnectionHolder.connectionId;
	GenericMsoRecord screen = new GenericMsoRecord();
	Boolean LOOPFLAG = false;
	
	String ErrorMessage = ""
	Boolean changeCurr = false;
	
	@Override
	public GenericMsoRecord onDisplay(GenericMsoRecord screen){
		log.info("Hooks onDisplay MSM23EA logging.version: ${hookVersion}");
		String PR_NO = screen.getField("PREQ_NO1I").getValue().trim();
		String PR_ITM_NO = screen.getField("PREQ_ITEM_NO1I").getValue().trim();
		PR_ITM_NO = String.format("%03d",(Integer.parseInt(PR_ITM_NO)));
		String REQ_TYPE = screen.getField("PREQ_TYPE1I").getValue().trim();
		if(REQ_TYPE.trim().equals("S")) {
			def QRY1 = sql.firstRow("select * from msf071 where ENTITY_TYPE = 'TOW' and trim(ENTITY_VALUE) = trim('"+tools.commarea.District.trim() + PR_NO.trim() + PR_ITM_NO.trim() + "') ");
			if(!QRY1.equals(null)) {
				if(!QRY1.STD_TXT_KEY.equals(null)) {
					if(QRY1.STD_TXT_KEY.trim().equals("OKA")) {
						screen.setCustomFieldValue("OKA", QRY1.REF_CODE.trim());
					}else {
						screen.setCustomFieldValue("T_CODE", QRY1.STD_TXT_KEY.trim());
						def QRY2 = sql.firstRow("select * from msf010 where TABLE_TYPE = 'TOW' and trim(TABLE_CODE) = trim('"+QRY1.STD_TXT_KEY.trim()+"') ");
						if(!QRY2.equals(null)) {
							screen.setCustomFieldValue("T_CODE_DESC", QRY2.TABLE_DESC.trim());
						}
						screen.setCustomFieldValue("SUPP1", QRY1.REF_CODE.substring(0,6).trim());
						def QRY3 = sql.firstRow("select * from msf010 where TABLE_TYPE = 'TOS' and trim(TABLE_CODE) = trim('"+QRY1.STD_TXT_KEY.trim()+QRY1.REF_CODE.substring(0,6).trim()+"') ");
						if(!QRY3.equals(null)) {
							screen.setCustomFieldValue("SUPP1_DESC", QRY3.TABLE_DESC.trim());
						}
						screen.setCustomFieldValue("SUPP2", QRY1.REF_CODE.substring(6,12).trim());
						def QRY4 = sql.firstRow("select * from msf010 where TABLE_TYPE = 'TOS' and trim(TABLE_CODE) = trim('"+QRY1.STD_TXT_KEY.trim()+QRY1.REF_CODE.substring(6,12).trim()+"') ");
						if(!QRY4.equals(null)) {
							screen.setCustomFieldValue("SUPP2_DESC", QRY4.TABLE_DESC.trim());
						}
						screen.setCustomFieldValue("SUPP3", QRY1.REF_CODE.substring(12,18).trim());
						def QRY5 = sql.firstRow("select * from msf010 where TABLE_TYPE = 'TOS' and trim(TABLE_CODE) = trim('"+QRY1.STD_TXT_KEY.trim()+QRY1.REF_CODE.substring(12,18).trim()+"') ");
						if(!QRY5.equals(null)) {
							screen.setCustomFieldValue("SUPP3_DESC", QRY5.TABLE_DESC.trim());
						}
					}
				}
			}
			
		}
		return null;
	}
	
	@Override
	public GenericMsoRecord onPreSubmit(GenericMsoRecord screen){
		log.info("Hooks onPreSubmit MSM23EA logging.version: ${hookVersion}");
		String CURR = screen.getField("CURRENCY_TYPE1I").getValue().trim();
		String PR_NO = screen.getField("PREQ_NO1I").getValue().trim();
		String PR_ITM_NO = screen.getField("PREQ_ITEM_NO1I").getValue().trim();
		String ProcItem = screen.getField("PROCESS_ITEM1I").getValue().trim();
		log.info("CURR : " + CURR);
		changeCurr = false;
		String ERRMESS = screen.getField("ERRMESS1I").getValue().trim();
		if (!CURR.equals("")) {
			def QRYCURR = sql.firstRow("select * from msf231 where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and PREQ_NO = '"+PR_NO+"' and PREQ_ITEM_NO = '"+PR_ITM_NO+"'");
			if(!QRYCURR.equals(null)) {
				if(!QRYCURR.CURRENCY_TYPE.trim().equals(CURR)) {
					log.info("CURRENCY CHANGED : ");
					if (ERRMESS.contains("WARNING: CURRENCY TYPE HAS CHANGED")) {
						changeCurr = true;
					}
				}
			}
		}
		
		log.info("Refrence Code Value : " + ProcItem)
		
		PR_ITM_NO = String.format("%03d",(Integer.parseInt(PR_ITM_NO)));
		String REQ_TYPE = screen.getField("PREQ_TYPE1I").getValue().trim();
		if(REQ_TYPE.trim().equals("S")) {
			if ( ((screen.getNextAction() == 1) || (screen.getNextAction() == 0))) {
				def QRY1 = sql.firstRow("select * from msf071 where ENTITY_TYPE = 'TOW' and trim(ENTITY_VALUE) = trim('"+tools.commarea.District.trim() + PR_NO.trim() + PR_ITM_NO.trim() + "') ");
				if(!QRY1.equals(null)) {
					if(!QRY1.STD_TXT_KEY.equals(null)) {
						if(QRY1.STD_TXT_KEY.trim().equals("OKA")) {
							//screen.setCustomFieldValue("OKA", QRY1.REF_CODE.trim());
							String OKA = screen.getCustomFieldValue("OKA");
							if(OKA.trim().equals("")) {
								screen.setErrorMessage(new MsoErrorMessage("OKA", "8541", "TOW DATA REQUIRED!", MsoErrorMessage.ERR_TYPE_ERROR, MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))
								MsoField B_CODE = new MsoField()
								B_CODE.setName("OKA")
								screen.setCurrentCursorField(B_CODE)
								return screen
							}
						}else {
							String SUPP = screen.getField("SUPPLIER_NO1I").getValue().trim();
							String SUPP1 = screen.getCustomFieldValue("SUPP1");
							String SUPP2 = screen.getCustomFieldValue("SUPP2");
							String SUPP3 = screen.getCustomFieldValue("SUPP3");
							if(SUPP1.trim().equals("") && SUPP2.trim().equals("") && SUPP3.trim().equals("")) {
								screen.setErrorMessage(new MsoErrorMessage("T_CODE", "8541", "TOW DATA REQUIRED!", MsoErrorMessage.ERR_TYPE_ERROR, MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))
								MsoField B_CODE = new MsoField()
								B_CODE.setName("T_CODE")
								screen.setCurrentCursorField(B_CODE)
								return screen
							}
							if(!SUPP.trim().equals("")) {
								log.info("SUPP : " + SUPP)
								log.info("SUPP1 : " + SUPP1)
								log.info("SUPP2 : " + SUPP2)
								log.info("SUPP3 : " + SUPP3)

								
								if (isNumeric(SUPP).equals(true)) {
									SUPP = String.format("%06d",(Integer.parseInt(SUPP)));
								}
								if (isNumeric(SUPP1).equals(true)) {
									SUPP1 = String.format("%06d",(Integer.parseInt(SUPP1)));
								}
								if (isNumeric(SUPP2).equals(true)) {
									SUPP2 = String.format("%06d",(Integer.parseInt(SUPP2)));
								}
								if (isNumeric(SUPP3).equals(true)) {
									SUPP3 = String.format("%06d",(Integer.parseInt(SUPP3)));
								}
								
								Boolean CekTOS = false;
								
								if (SUPP.trim().equals(SUPP1.trim())) {
									CekTOS = true
								}
								if (SUPP.trim().equals(SUPP2.trim())) {
									CekTOS = true
								}
								if (SUPP.trim().equals(SUPP3.trim())) {
									CekTOS = true
								}
								if (CekTOS.equals(false)) {
									screen.setErrorMessage(new MsoErrorMessage("SUPPLIER_NO1I", "8541", "SUPPLIER NOT IN TOW CODE", MsoErrorMessage.ERR_TYPE_ERROR, MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))
									MsoField B_CODE = new MsoField()
									B_CODE.setName("SUPPLIER_NO1I")
									screen.setCurrentCursorField(B_CODE)
									return screen
								}
							}
						}
					}
				}else {
					screen.setErrorMessage(new MsoErrorMessage("T_CODE", "8541", "TOW DATA REQUIRED!", MsoErrorMessage.ERR_TYPE_ERROR, MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))
					MsoField B_CODE = new MsoField()
					B_CODE.setName("T_CODE")
					screen.setCurrentCursorField(B_CODE)
					return screen
				}
			}
		}
		String PR_NO2 = String.format("%1\$-" + "6" + "s", PR_NO);
		log.info("PR_NO2 : " + PR_NO2)
		if(ProcItem.trim().equals("L") && REQ_TYPE.trim().equals("G")) {
			if (((screen.getNextAction() == 1) || (screen.getNextAction() == 0))) {
				def QRY2 = sql.firstRow("select * from msf071 where ENTITY_TYPE = 'PUR' and trim(ENTITY_VALUE) = trim('1"+tools.commarea.District.trim() + PR_NO2 + PR_ITM_NO.trim() + "') ");
				log.info("Query 2 : " + QRY2)
				if(QRY2.equals(null)) {
					screen.setErrorMessage(new MsoErrorMessage("REF", "8542", "TKDN VALUE REQUIRED!", MsoErrorMessage.ERR_TYPE_ERROR, MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))
					screen.setCurrentCursorField(screen.getField("ACTION_A1I"))
					return screen
				}
				
				
			}
			
		}
		
		
		
		return null;
	}
	@Override
	public GenericMsoRecord onPostSubmit(GenericMsoRecord input, GenericMsoRecord result) {
		log.info("Hooks onPostSubmit MSM23EA logging.version: ${hookVersion}");
		String ACTION = input.getField("PROCESS_ITEM1I").getValue().trim();
		String PR_NO = input.getField("PREQ_NO1I").getValue().trim();
		String PR_ITM_NO = input.getField("PREQ_ITEM_NO1I").getValue().trim();
		PR_ITM_NO = String.format("%03d",(Integer.parseInt(PR_ITM_NO)));
		String REQ_TYPE = input.getField("PREQ_TYPE1I").getValue().trim();
		String StrFKEYS = input.getFunctionKeyLine().getValue()
		log.info("StrFKEYS : " + StrFKEYS)
		if (!StrFKEYS.trim().equals("")){
			if (StrFKEYS.contains("XMIT-Confirm") == true){
				if(REQ_TYPE.trim().equals("S")) {
					if (ACTION.trim().equals("D")) {
						def QRY1 = sql.firstRow("select * from msf071 where ENTITY_TYPE = 'TOW' and trim(ENTITY_VALUE) = trim('"+tools.commarea.District.trim() + PR_NO.trim() + PR_ITM_NO.trim() + "') ");
						if(!QRY1.equals(null)) {
							log.info ("DELETE MSF071:");
							String QueryDelete = ("delete MSF071 where ENTITY_TYPE = 'TOW' and trim(ENTITY_VALUE) = trim('"+tools.commarea.District.trim() + PR_NO.trim() + PR_ITM_NO.trim() + "') ");
							sql.execute(QueryDelete);
						}
					}
				}
			}
		}
		//DANOV-UNCOMMENT OUT
		if(changeCurr.equals(true)) {
			def QRY2 = sql.firstRow("select to_char(sysdate,'YYYYMMDD') V_DATE,to_char(sysdate,'HH24MISS') V_TIME,to_char(sysdate,'HH24MI')||'00' V_TIME2,to_char(sysdate,'YYMMDDHH24MISS') V_TIME3 from dual");
			String V_DATE = QRY2.V_DATE;
			String V_TIME = QRY2.V_TIME;
			String V_TIME2 = QRY2.V_TIME2;
			String V_TIME3 = QRY2.V_TIME3;
			String QueryInsert = ("Insert into MSF080 (PROG_NAME,DEFER_DATE,DEFER_TIME,REQUEST_DATA,REQUEST_REC_NO,APPSERVER_ID,BANNER,BATCH_QUEUE_NAME,COMMIT_COUNT,DISTRIB_CODE,DSTRCT_CODE,EMPLOYEE_ID,GROUP_NAME,JOB_ID,LANGUAGE_CODE,LAST_COMMIT_DT,LAST_COMMIT_TM,LAST_MOD_DATE,LAST_MOD_EMP,LAST_MOD_TIME,LAST_MOD_USER,MAX_READS,MAX_UPDATES,MEDIUM,MEDIUM_PRT_IND,NOTIFY_SW,NO_OF_COPIES1,NO_OF_COPIES10,NO_OF_COPIES2,NO_OF_COPIES3,NO_OF_COPIES4,NO_OF_COPIES5,NO_OF_COPIES6,NO_OF_COPIES7,NO_OF_COPIES8,NO_OF_COPIES9,NO_PROCESS_SPL,NO_TO_PROCESS,OVERRIDE_FLAG,POSITION_ID,PRINTER1,PRINTER10,PRINTER2,PRINTER3,PRINTER4,PRINTER5,PRINTER6,PRINTER7,PRINTER8,PRINTER9,PROCESS_STATUS,PROG_GRP_IDENT,PROG_REPORT_ID,PUB_TYPE,REQUEST_BY,REQUEST_DSTRCT,REQUEST_NO,REQUEST_PARAMS,RESTART_FLAG,RETENTION_DAYS,START_DATE,START_OPTION,START_TIME,STREAM_NAME,SYNC_KEY,SYS_JOBNO,TASK_UUID,TIMESLOT_END,TRACE_FLG,USERNO,USER_ID,UUID) values ('KPB230  ','"+V_DATE+"','"+V_TIME2+"','"+V_TIME3+"','01','                                ','                   ','                                        ','000000000','          ','"+tools.commarea.District.trim()+"','ADMIN     ','        ','          ','en_AU','        ','000000','"+V_DATE+"','ADMIN     ','"+V_TIME+"','ADMIN     ','00000000','00000000','P','N','N','01','00','00','00','00','00','00','00','00','00','001','001',' ','ADMIN     ','00','  ','  ','  ','  ','  ','  ','  ','  ','  ','I','P','A','TXT ','SYSTEM ADMINISTRATOR R        ','"+tools.commarea.District.trim()+"','01','"+PR_NO.trim() + PR_ITM_NO.trim()+"',' ','002','        ','I','0000','                                        ','                    ','     ','"+V_TIME3+"','0000','N','G6T5','ADMIN     ','"+V_TIME3+"')");
			sql.execute(QueryInsert);
			log.info ("RUN KPB230:");
			changeCurr = false;
		}
		return result
	}
	private boolean isQuestionMarkOnScreen (GenericMsoRecord screen) {
		String screenData = screen.getCurrentScreenDetails().getScreenFields().toString()
		return screenData.contains("?")
	}
	public static boolean isNumeric(String str)
	{
	  try
	  {
		double d = Double.parseDouble(str.trim());
	  }
	  catch(NumberFormatException nfe)
	  {
		return false;
	  }
	  return true;
	}
}
