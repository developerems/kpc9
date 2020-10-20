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
import com.mincom.ellipse.service.ServiceDTO;
import com.mincom.enterpriseservice.exception.*
import groovy.sql.Sql
import com.mincom.enterpriseservice.ellipse.*


class ContractItemService_fetchPortMile extends ServiceHook{

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
		log.info("Hooks ContractItemService_fetchPortMile onPostExecute logging.version: ${hookVersion}")

		ContractItemServiceFetchPortMileReplyDTO c = (ContractItemServiceFetchPortMileReplyDTO) result
		log.info("ContractItemServiceFetchPortMileReplyDTO: " + c)
		String contractNo = c.contractNo
		String portMile = c.portion + "0101"
		String entityValue = "${tools.commarea.District.trim()}${contractNo ? contractNo.trim() : ''}${portMile ? portMile.trim() : ''}"
		String addTaxType = 'CIA'
		String wtTaxType = 'CIW'
		String stdKey = contractNo ? contractNo.trim() : "" + portMile
		String addTaxCode
		String wtCode
		Boolean ATT_FLAG1 = false
		Boolean ATT_FLAG2 = false

		log.info("contractNo: $contractNo / ${c.contractNo}")
		log.info("portMile: $portMile / ${c.portion}")
		log.info("entityValue: $entityValue")
		
		Attribute[] ATT = new Attribute[3]
		log.info("ATT SIZE: " + ATT.size())
		
		String query1 = "SELECT * FROM MSF071 WHERE ENTITY_TYPE = '$addTaxType' and ENTITY_VALUE = '$entityValue' and REF_NO = '001' and SEQ_NUM = '001'"
		log.info("query1: $query1")

		def queryResult1 = sql.firstRow(query1);
		log.info ("FIND ATAX CODE: " + queryResult1)

		if(queryResult1) {
			ATT[0] = new Attribute();
			ATT[0].setName("custAtaxCode");
			ATT[0].setValue(queryResult1.REF_CODE.trim());
			ATT_FLAG1 = true;
		}
		String query2 = "SELECT * FROM MSF071 WHERE ENTITY_TYPE = '$wtTaxType' and ENTITY_VALUE = '$entityValue' and REF_NO = '001' and SEQ_NUM = '001'"
		log.info("query2: $query2")
		def queryResult2 = sql.firstRow(query2);
			log.info ("FIND WHTAX CODE: " + queryResult2);
		if(queryResult2) {
			ATT[1] = new Attribute();
			ATT[1].setName("wtCode");
			ATT[1].setValue(queryResult2.REF_CODE.trim());
			ATT_FLAG2 = true;
		}
		if (ATT_FLAG1.equals(true) && ATT_FLAG2.equals(true)) {
			def queryResult3 = sql.firstRow("select STD_KEY,trim(replace(listagg(VAL, ' ' ON OVERFLOW TRUNCATE) within group (order by STD_KEY,STD_LINE_NO,source),'.HEADING')) as merge_VAL " +
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
					"where STD_TEXT_CODE = 'GT' and trim(val) is not null and trim(std_key) = trim('$stdKey') " +
					"order by STD_KEY,STD_LINE_NO, source) " +
					"group by STD_KEY");
				//log.info ("FIND WHTAX CODE: " + QRY1);
			if(queryResult3) {
				ATT[2] = new Attribute();
				ATT[2].setName("taxText");
				ATT[2].setValue(queryResult3.merge_VAL.trim());
			}else {
				ATT[2] = new Attribute();
				ATT[2].setName("taxText");
				ATT[2].setValue("");
			}
			c.setCustomAttributes(ATT)
			c.set
		}
		return result
	}
}