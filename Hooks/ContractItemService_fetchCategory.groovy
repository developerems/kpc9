/**
 * @EMS Nov 2018
 *
 * 20181217 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Initial Coding
 **/
import javax.naming.InitialContext

import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.enterpriseservice.ellipse.contractitem.ContractItemService
import com.mincom.enterpriseservice.ellipse.contractitem.ContractItemServiceModifyPortMileRequestDTO
import com.mincom.enterpriseservice.ellipse.contractitem.ContractItemServiceFetchPortMileReplyDTO
import com.mincom.enterpriseservice.ellipse.contractitem.ContractItemServiceFetchCategoryReplyDTO
import com.mincom.ellipse.service.ServiceDTO;
import com.mincom.enterpriseservice.exception.*
import groovy.sql.Sql
import com.mincom.enterpriseservice.ellipse.*


class ContractItemService_fetchCategory extends ServiceHook{

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
	public Object onPostExecute(Object input, Object result) {
		log.info("Hooks ContractItemService_fetchCategory onPostExecute logging.version: ${hookVersion}")

		ContractItemServiceFetchCategoryReplyDTO c = (ContractItemServiceFetchCategoryReplyDTO) result
		log.info("ContractItemServiceFetchCategoryReplyDTO: " + c)
		String CNT_NO = "";
		String PORT_MILE = "";
		CNT_NO = c.contractNo;
		PORT_MILE = c.portion + c.element + c.categoryNo
		String ADD_TAX_CODE = "";
		String WT_CODE = "";
		Boolean ATT_FLAG1 = false;
		Boolean ATT_FLAG2 = false;
		
		Attribute[] ATT = new Attribute[3];
		log.info("ATT SIZE: " + ATT.size());
		
		def QRY1 = sql.firstRow("select * from msf071 " +
			"where ENTITY_TYPE = 'CIA' and ENTITY_VALUE = '"+tools.commarea.District.trim()+CNT_NO.trim()+PORT_MILE.trim()+"' and REF_NO = '001' and SEQ_NUM = '001'");
			log.info ("FIND ATAX CODE: " + QRY1);
		if(!QRY1.equals(null)) {
			ATT[0] = new Attribute();
			ATT[0].setName("addTaxCode");
			ATT[0].setValue(QRY1.REF_CODE.trim());
			ATT_FLAG1 = true;
		}
		
		QRY1 = sql.firstRow("select * from msf071 " +
			"where ENTITY_TYPE = 'CIW' and ENTITY_VALUE = '"+tools.commarea.District.trim()+CNT_NO.trim()+PORT_MILE.trim()+"' and REF_NO = '001' and SEQ_NUM = '001'");
			log.info ("FIND WHTAX CODE: " + QRY1);
		if(!QRY1.equals(null)) {
			ATT[1] = new Attribute();
			ATT[1].setName("wtCode");
			ATT[1].setValue(QRY1.REF_CODE.trim());
			ATT_FLAG2 = true;
		}
		if (ATT_FLAG1.equals(true) && ATT_FLAG2.equals(true)) {
			QRY1 = sql.firstRow("select STD_KEY,trim(replace(listagg(VAL, ' ' ON OVERFLOW TRUNCATE) within group (order by STD_KEY,STD_LINE_NO,source),'.HEADING')) as merge_VAL " +
					"from ( " +
					"select STD_KEY,STD_LINE_NO, source, trim(val) val " +
					"from " +
					"  MSF096_STD_STATIC UNPIVOT INCLUDE NULLS " +
					"    ( VAL FOR( SOURCE ) IN " +
					"        ( STD_STATIC_1 AS 'STD_STATIC_1', " +
					"          STD_STATIC_2 AS 'STD_STATIC_2', " +
					"          STD_STATIC_3 AS 'STD_STATIC_3', " +
					"          STD_STATIC_4 AS 'STD_STATIC_4', " +
					"          STD_STATIC_5 AS 'STD_STATIC_5' " +
					"        ) " +
					"    ) " +
					"where STD_TEXT_CODE = 'GT' and trim(val) is not null and trim(std_key) = trim('"+CNT_NO.trim()+PORT_MILE.trim()+"') " +
					"order by STD_KEY,STD_LINE_NO, source) " +
					"group by STD_KEY");
				//log.info ("FIND WHTAX CODE: " + QRY1);
			if(!QRY1.equals(null)) {
				ATT[2] = new Attribute();
				ATT[2].setName("taxText");
				ATT[2].setValue(QRY1.merge_VAL.trim());
			}else {
				ATT[2] = new Attribute();
				ATT[2].setName("taxText");
				ATT[2].setValue("");
			}
			c.setCustomAttributes(ATT);
		}
		
		

		return result
	}
}