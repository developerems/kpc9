package KPC.HOOKS
/**
 * @EMS Mar 2019
 *
 * 20190317 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Forward Fit from Ellipse 5 to set District based on login district
 * 20190314 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Delete Commitment when change Contract Status to "FC"
 * 20190308 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Initial Coding - Cek for CIC Estimate & Accepted when change status to FC
 **/

import javax.naming.InitialContext

import com.mincom.ellipse.attribute.Attribute
import com.mincom.enterpriseservice.ellipse.*
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.enterpriseservice.ellipse.contractitem.ContractItemService
import com.mincom.enterpriseservice.ellipse.contractitem.ContractItemServiceModifyPortMileRequestDTO
import com.mincom.enterpriseservice.ellipse.contract.ContractServiceModifyRequestDTO
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextService
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextServiceCreateReplyDTO
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextServiceSetTextReplyDTO
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextServiceDeleteReplyDTO
import com.mincom.enterpriseservice.ellipse.stdtext.StdTextServiceAppendReplyDTO
import com.mincom.ellipse.service.ServiceDTO;
import com.mincom.enterpriseservice.exception.*
import groovy.sql.Sql

class ContractService_modify extends ServiceHook {
	String hookVersion = "1"
	String strCrDT = "";
	String strCrTM = "";
	String StrDT = "";
	String StrMT = "";
	String StrYR = "";
	String StrHH = "";
	String StrMM = "";
	String StrSS = "";
	
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	@Override
	public Object onPreExecute(Object input) {
		log.info("Hooks ContractService_modify onPreExecute logging.version: ${hookVersion}")

		ContractServiceModifyRequestDTO c = (ContractServiceModifyRequestDTO) input
		c.setDistrictCode(tools.commarea.District.trim());
		log.info("ContractServiceModifyRequestDTO: " + c)
		if (!c.status384.equals(null)) {
			if (c.status384.trim().equals("FC")) {
				String CNT_NO = c.getContractNo();
				def QRY1 = sql.firstRow("select * from ACA.KPF38F " +
					"where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and upper(trim(CONTRACT_NO)) = upper(trim('"+CNT_NO+"')) and CIC_STATUS in ('1','2')");
					log.info ("FIND CIC  : " + QRY1);
				if(!QRY1.equals(null)) {
					throw new EnterpriseServiceOperationException(
						new ErrorMessageDTO(
						"9999", "OUTSTANDING CIC EXIST!", "status384", 0, 0))
						return input
				}
			}
		}
		return null
	}
	
	@Override
	public Object onPostExecute(Object input, Object result) {
		log.info("Hooks ContractService_modify onPostExecute logging.version: ${hookVersion}")
		ContractServiceModifyRequestDTO c = (ContractServiceModifyRequestDTO) input
		if (!c.status384.equals(null)) {
			if (c.status384.trim().equals("FC")) {
				String CNT_NO = c.getContractNo();
				String StrSQL = "select * from msf990 " +
					"where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and COMMITMENT_TY = 'C' and upper(trim(COMMIT_NO)) = upper(trim('"+CNT_NO+"'))";
				log.info ("StrSQL : " + StrSQL);
				Integer i;
				i = 0;
				sql.eachRow(StrSQL, {
					i = i + 1;
					String CCYY_IND = "";
					String MN_IND = "";
					CCYY_IND = it.FULL_ACCT_PER.trim().substring(0, 4);
					MN_IND = it.FULL_ACCT_PER.trim().substring(4, 6);
					def QRY2 = sql.firstRow("select * from msf964 " +
						"where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and account_code = '"+it.account_code.trim()+"' and CCYY_IND = '"+CCYY_IND+"' and COMMITMENT_TY = 'C' and POSTING_TYPE = '00' and CURRENCY_IND = 'L'");
					if(!QRY2.equals(null)) {
						BigDecimal OLD_VAL;
						BigDecimal NEW_VAL;
						String COL_NAME = "";
						String Q_UPD
						if(MN_IND.equals("01")) {
							COL_NAME = "AMOUNT_ITEMX1"
							OLD_VAL = QRY2.AMOUNT_ITEMX1;
							NEW_VAL = OLD_VAL - it.COMMIT_VALUE;
						}else if(MN_IND.equals("02")) {
							COL_NAME = "AMOUNT_ITEMX2"
							OLD_VAL = QRY2.AMOUNT_ITEMX2;
							NEW_VAL = OLD_VAL - it.COMMIT_VALUE;
						}else if(MN_IND.equals("03")) {
							COL_NAME = "AMOUNT_ITEMX3"
							OLD_VAL = QRY2.AMOUNT_ITEMX3;
							NEW_VAL = OLD_VAL - it.COMMIT_VALUE;
						}else if(MN_IND.equals("04")) {
							COL_NAME = "AMOUNT_ITEMX4"
							OLD_VAL = QRY2.AMOUNT_ITEMX4;
							NEW_VAL = OLD_VAL - it.COMMIT_VALUE;
						}else if(MN_IND.equals("05")) {
							COL_NAME = "AMOUNT_ITEMX5"
							OLD_VAL = QRY2.AMOUNT_ITEMX5;
							NEW_VAL = OLD_VAL - it.COMMIT_VALUE;
						}else if(MN_IND.equals("06")) {
							COL_NAME = "AMOUNT_ITEMX6"
							OLD_VAL = QRY2.AMOUNT_ITEMX6;
							NEW_VAL = OLD_VAL - it.COMMIT_VALUE;
						}else if(MN_IND.equals("07")) {
							COL_NAME = "AMOUNT_ITEMX7"
							OLD_VAL = QRY2.AMOUNT_ITEMX7;
							NEW_VAL = OLD_VAL - it.COMMIT_VALUE;
						}else if(MN_IND.equals("08")) {
							COL_NAME = "AMOUNT_ITEMX8"
							OLD_VAL = QRY2.AMOUNT_ITEMX8;
							NEW_VAL = OLD_VAL - it.COMMIT_VALUE;
						}else if(MN_IND.equals("09")) {
							COL_NAME = "AMOUNT_ITEMX9"
							OLD_VAL = QRY2.AMOUNT_ITEMX9;
							NEW_VAL = OLD_VAL - it.COMMIT_VALUE;
						}else if(MN_IND.equals("10")) {
							COL_NAME = "AMOUNT_ITEMX10"
							OLD_VAL = QRY2.AMOUNT_ITEMX10;
							NEW_VAL = OLD_VAL - it.COMMIT_VALUE;
						}else if(MN_IND.equals("11")) {
							COL_NAME = "AMOUNT_ITEMX11"
							OLD_VAL = QRY2.AMOUNT_ITEMX11;
							NEW_VAL = OLD_VAL - it.COMMIT_VALUE;
						}else if(MN_IND.equals("12")) {
							COL_NAME = "AMOUNT_ITEMX12"
							OLD_VAL = QRY2.AMOUNT_ITEMX12;
							NEW_VAL = OLD_VAL - it.COMMIT_VALUE;
						}
						Q_UPD = "UPDATE MSF964 set "+COL_NAME+" = ? where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and ACCOUNT_CODE = '"+it.account_code.trim()+"' and CCYY_IND = '"+CCYY_IND+"' and COMMITMENT_TY = 'C' and POSTING_TYPE = '00' and CURRENCY_IND = 'L'";
						try
						{
							sql.execute(Q_UPD,NEW_VAL);
						} catch (Exception  e) {
							log.info ("Exception is : " + e);
							// Raise Error
							throw new EnterpriseServiceOperationException(
							new ErrorMessageDTO(
							"9999", "ERROR UPDATE MSF964 !", "", 0, 0))
			
							return result
						}
					}
				})
				if (i > 0) {
					String Q_DEL = "DELETE MSF990 where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and COMMITMENT_TY = 'C' and upper(trim(COMMIT_NO)) = upper(trim('"+CNT_NO+"'))";
					try
					{
						sql.execute(Q_DEL);
					} catch (Exception  e) {
						log.info ("Exception is : " + e);
						// Raise Error
						throw new EnterpriseServiceOperationException(
						new ErrorMessageDTO(
						"9999", "ERROR DELETE MSF990 !", "", 0, 0))
		
						return result
					}
				}
			}
		}
		
		return result
	}
}
