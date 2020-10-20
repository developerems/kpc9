/**
 * @EMS Mar 2019
 *
 * 20190413 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Initial Coding - Hooks to Set CIC Status to Accepted when Invoice Cancelled.  
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

public class MSM262B extends MSOHook{
	String hookVersion = "1";
	
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
	def sql = new Sql(CAISource)
	
	String SUPP
	String INV
	@Override
	public GenericMsoRecord onDisplay(GenericMsoRecord screen){
		log.info("Hooks onDisplay MSM262B logging.version: ${hookVersion}");
		
		return null;
	}
	@Override
	public GenericMsoRecord onPreSubmit(GenericMsoRecord screen){
		log.info("Hooks onPreSubmit MSM262B logging.version: ${hookVersion}");
		SUPP = screen.getField("SUPPLIER_NO2I").getValue().trim();
		INV = screen.getField("INV_NO2I").getValue().trim();
		/*if ( ((screen.getNextAction() == 1) || (screen.getNextAction() == 0))) {
			def QRY1 = sql.firstRow("select * from msf260 where trim(supplier_no) = trim('"+SUPP+"') and trim(EXT_INV_NO) = trim('"+INV+"')");
			if(!QRY1.equals(null)) {
				if (QRY1.PMT_STATUS.equals("00") && QRY1.INV_TYPE.equals("2")) {
					screen.setErrorMessage(new MsoErrorMessage("PMT_STATUS2I", "8541", "COULD NOT CANCEL SERVICE INVOICE WITH LOADED STATUS!", MsoErrorMessage.ERR_TYPE_ERROR, MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))
					MsoField B_CODE = new MsoField()
					B_CODE.setName("PMT_STATUS2I")
					screen.setCurrentCursorField(B_CODE)
					return screen
				}
				List acpTran = new ArrayList();
				String StrSQL = "";
				BigDecimal apprAcctPay = 0;
				BigDecimal tranAmt = 0;
				apprAcctPay = QRY1.APPR_ACCT_PAY;
				StrSQL = "select a.*,b.TRAN_TYPE,b.TRAN_AMOUNT,b.TRAN_GROUP_KEY from msfx9b a " +
							"left outer join msf900 b on (a.DSTRCT_CODE = b.DSTRCT_CODE and a.FULL_PERIOD = b.FULL_PERIOD and a.PROCESS_DATE = b.PROCESS_DATE and " +
							"a.REC900_TYPE = b.REC900_TYPE and a.TRANSACTION_NO = b.TRANSACTION_NO and a.USERNO = b.USERNO) " +
							"where (a.DSTRCT_CODE,a.INV_NO,a.SUPPLIER_NO) in ( " +
							"select DSTRCT_CODE,INV_NO,SUPPLIER_NO from msf260 " +
							"where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and SUPPLIER_NO = '"+SUPP+"' and EXT_INV_NO = '"+INV+"' and INV_TYPE in ('2','6')) " +
							"and b.TRAN_TYPE = 'ACP' and b.TRAN_AMOUNT < 0 " +
							"ORDER BY a.DSTRCT_CODE,a.SUPPLIER_NO,a.INV_NO,a.FULL_PERIOD,a.PROCESS_DATE,a.TRANSACTION_NO,a.USERNO,a.REC900_TYPE" ;
				log.info ("StrSQL : " + StrSQL);
				sql.eachRow(StrSQL, {
					tranAmt = it.TRAN_AMOUNT;
					if (apprAcctPay == tranAmt) {
						acpTran.add(it.TRAN_GROUP_KEY);
					}
				})
				
				String lastTranGrpKey = "";
				List<String> invItem = new ArrayList<String>();
				if (acpTran.size() > 0) {
					lastTranGrpKey = acpTran.get(acpTran.size() - 1);
					StrSQL = "select * from msf26a " +
								"where (DSTRCT_CODE,INV_NO,SUPPLIER_NO) in ( " +
								"select DSTRCT_CODE,INV_NO,SUPPLIER_NO from msf260 " +
								"where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and SUPPLIER_NO = '"+SUPP+"' and EXT_INV_NO = '"+INV+"' and INV_TYPE in ('2','6')) and PP_AMT_LOC > 0" ;
					log.info ("StrSQL : " + StrSQL);
					sql.eachRow(StrSQL, {
						invItem.add(it.INV_ITEM_NO);
					})
				}
				
				List<String> wrongTrnGrpKey = new ArrayList<String>();
				List<String> loopTrnGrpKey = new ArrayList<String>();
				
				if (invItem.size() > 0) {
					for (int i = 0; i < invItem.size(); i++) {
						log.info ("Invoice Item : " + invItem.get(i));
						StrSQL = "select a.*,b.TRAN_TYPE,b.TRAN_AMOUNT,b.TRAN_GROUP_KEY,b.INV_ITEM_NO from msfx9b a " +
							"left outer join msf900 b on (a.DSTRCT_CODE = b.DSTRCT_CODE and a.FULL_PERIOD = b.FULL_PERIOD and a.PROCESS_DATE = b.PROCESS_DATE and " +
							"a.REC900_TYPE = b.REC900_TYPE and a.TRANSACTION_NO = b.TRANSACTION_NO and a.USERNO = b.USERNO) " +
							"where (a.DSTRCT_CODE,a.INV_NO,a.SUPPLIER_NO) in ( " +
							"select DSTRCT_CODE,INV_NO,SUPPLIER_NO from msf260 " +
							"where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and SUPPLIER_NO = '"+SUPP+"' and EXT_INV_NO = '"+INV+"' and INV_TYPE in ('2','6')) " +
							"and b.TRAN_TYPE = 'WIP' and b.TRAN_AMOUNT > 0 and b.INV_ITEM_NO = '"+invItem.get(i)+"' " +
							"ORDER BY a.DSTRCT_CODE,a.SUPPLIER_NO,a.INV_NO,a.FULL_PERIOD,a.PROCESS_DATE,a.TRANSACTION_NO,a.USERNO,a.REC900_TYPE" ;
						log.info ("StrSQL : " + StrSQL);
						sql.eachRow(StrSQL, {
							loopTrnGrpKey.add(it.TRAN_GROUP_KEY);
						})
						if (loopTrnGrpKey.size() > 0) {
							wrongTrnGrpKey.add(loopTrnGrpKey.get(loopTrnGrpKey.size() - 1));
							loopTrnGrpKey.clear();
						}
					}
				}
				
				if (wrongTrnGrpKey.size() > 0) {
					for (int i = 0; i < wrongTrnGrpKey.size(); i++) {
						log.info ("wrongTrnGrpKey : " + wrongTrnGrpKey.get(i));
						log.info ("lastTranGrpKey : " + lastTranGrpKey);
						if (!lastTranGrpKey.equals(null) && !lastTranGrpKey.equals("")) {
							String QueryUpdate = ("update MSF900 set TRAN_GROUP_KEY = '"+lastTranGrpKey+"' where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and TRAN_GROUP_KEY = '"+wrongTrnGrpKey.get(i)+"' ");
							sql.execute(QueryUpdate);
						}
					}
				}
			}
		}*/
		return null;
	}
	@Override
	public GenericMsoRecord onPostSubmit(GenericMsoRecord input, GenericMsoRecord result) {
		log.info("Hooks onPostSubmit MSM262B logging.version: ${hookVersion}");
		if ( ((result.getNextAction() == 1) || (result.getNextAction() == 0))) {
			def QRY1 = sql.firstRow("select * from msf260 where trim(supplier_no) = trim('"+SUPP+"') and trim(EXT_INV_NO) = trim('"+INV+"')");
			if(!QRY1.equals(null)) {
				log.info ("UPDATE ACA.KPF38I:");
				String QueryDelete = ("delete ACA.KPF38I where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and trim(upper(CIC_INVOICE)) = trim(upper('"+INV+"')) and trim(upper(CONTRACT_NO)) = trim(upper('"+QRY1.CONTRACT_NO+"')) ");
				sql.execute(QueryDelete);
				QueryDelete = ("delete MSF071 where ENTITY_TYPE in ('IBC','IBA','IDT','IDR') and trim(upper(entity_value)) = trim(upper('"+INV+"')) ");
				sql.execute(QueryDelete);
				String QueryUpdate = ("update ACA.KPF38f set CIC_INVOICE = ' ',CIC_STATUS = '2' where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and trim(upper(CONTRACT_NO)) = trim(upper('"+QRY1.CONTRACT_NO+"')) and trim(upper(CIC_INVOICE)) = trim(upper('"+INV+"')) ");
				sql.execute(QueryUpdate);
				if (!INV.trim().equals("")) {
					QueryDelete = ("delete MSF096 where (STD_KEY like 'IX"+INV.trim()+"%' or STD_KEY like 'EX"+INV.trim()+"%') and STD_TEXT_CODE = 'GT' ");
					sql.execute(QueryDelete);
					QueryDelete = ("delete MSF096_STD_STATIC where (STD_KEY like 'IX"+INV.trim()+"%' or STD_KEY like 'EX"+INV.trim()+"%') and STD_TEXT_CODE = 'GT' ");
					sql.execute(QueryDelete);
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
