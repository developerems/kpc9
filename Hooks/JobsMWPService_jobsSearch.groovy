package KPC.HOOKS
/**
 * @EMS Sep 2019
 *
 * 20191023  - Ricky Afriano  - KPC UPGRADE
 *             add function to show PO no when search in MSEWOT.
 * 20190927  - Eghy Kurniagus - KPC UPGRADE
 *             Initial Coding - calculate MST Hours for MSEWJO.
 **/

import javax.naming.InitialContext

import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.ellipse.service.ServiceDTO;
import com.mincom.enterpriseservice.exception.*
import groovy.sql.Sql
import com.mincom.enterpriseservice.ellipse.*
//import com.mincom.ellipse.types.m8mwp.instances.MSTServiceResult
import com.mincom.ellipse.types.m8mwp.instances.JobsMWPServiceResult

import java.text.DecimalFormat


class JobsMWPService_jobsSearch extends ServiceHook {
	String hookVersion = "1"
	String WO = "";
	String EQUIP_NO = "";
	String STAT_TYPE = "";
	String CompCode = "";
	String TaskNo = "";
	String CompModCode = "";
	String workGroup = "";

	BigDecimal currenctStat
	BigDecimal LastStat
	BigDecimal MST_HOUR
	BigInteger TaskNoNew

	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)

	@Override
	public Object onPostExecute(Object input, Object result) {
		log.info("Hooks Calculate MST Hours onPostExecute logging.version: ${hookVersion}")

		JobsMWPServiceResult[] d = (JobsMWPServiceResult[]) result
		log.info("LENGTH" + d.length)
		//log.info("JobsMWP" + d)

		if(!d.length.equals(0)) {
			Integer i = 0

			for (i = 0;i<d.length;i++) {
				//log.info("MWPJobDTO : " + d[i].getJobsMWPDTO())
				if (!d[i].getJobsMWPDTO().equals(null)) {


					//STAT_TYPE = d.getMSTDTO().getStatType1().value
					EQUIP_NO = d[i].getJobsMWPDTO().getEquipNo().value
					CompCode = d[i].getJobsMWPDTO().getCompCode().value
					TaskNo = d[i].getJobsMWPDTO().getMaintSchTask().value
					CompModCode = d[i].getJobsMWPDTO().getCompModCode().value
					workGroup = d[i].getJobsMWPDTO().getWorkGroup().value

					WO = d[i].getJobsMWPDTO().getWorkOrder().value

					currenctStat = d[i].getJobsMWPDTO().getCurrentStatValue1().value

					log.info("EQUIP_NO : " + EQUIP_NO)
					log.info("CompCode "  + CompCode)
					log.info("TaskNo : " + TaskNo)
					log.info("CompModCode : " + CompModCode)
					log.info("workGroup : " + workGroup)
					log.info("currenctStat : " + currenctStat)
					log.info("WO : " + WO)
					Attribute[] ATT = new Attribute[2];

					ATT[0] = new Attribute();
					ATT[0].setName("MST_Hours");

					DecimalFormat df = new DecimalFormat("#,##0.00;-#,##0");

					if(!TaskNo.trim().equals("") && isNumeric(TaskNo).equals(true)) {
						log.info("Masuk Ke Kondisi 8 Series")
						if(TaskNo.substring(0,1) == "8") {
							log.info("TRUE - Kondisi 8 Series")
							TaskNoNew = TaskNo.toBigInteger() -1
						}else {
							log.info("ELSE - Kondisi 8 Series")
							TaskNoNew = TaskNo.toBigInteger()
						}
						log.info("TASK NO NEW :" + TaskNoNew.toString())

						def QRY1;
						if(TaskNo.substring(0,1) ==  "9") {
							QRY1 = sql.firstRow("select MAX(LAST_PERF_ST_1) Lamt from msf700 "+
									"where EQUIP_NO ='"+ EQUIP_NO +" ' and COMP_CODE = '"+ CompCode +" ' and MAINT_SCH_TASK > '"+ TaskNoNew.toString() + "' and COMP_MOD_CODE = '"+ CompModCode +" ' and WORK_GROUP = '"+ workGroup +" ' order by MAINT_SCH_TASK");
						}else {
							QRY1 = sql.firstRow("select MAX(LAST_PERF_ST_1) Lamt from msf700 "+
									"where EQUIP_NO ='"+ EQUIP_NO +" ' and COMP_CODE = '"+ CompCode +" ' and trim(MAINT_SCH_TASK) = '"+ TaskNoNew.toString()  + "' and COMP_MOD_CODE = '"+ CompModCode +" ' and WORK_GROUP = '"+ workGroup +" '");
						}



						if(!QRY1.equals(null)) {
							if(!QRY1.Lamt.equals(null)) {
								log.info("QWRY : " + QRY1.Lamt)
								ATT[0].setValue("");
								if(!currenctStat.equals(null)) {

									LastStat = QRY1.Lamt
									MST_HOUR = currenctStat - LastStat
									log.info("MST_HOUR" + MST_HOUR.toString())
									log.info("===================================================================================")
									ATT[0].setValue(df.format(MST_HOUR));
								}else {
									ATT[0].setValue(df.format(0.00));
								}
							}else {
								ATT[0].setValue(df.format(0.00));
							}
						}else {
							ATT[0].setValue(df.format(0.00));
						}
					}else {
						ATT[0].setValue(df.format(0.00));
					}
					//log.info("CEK1 : ")
					//show po no
					ATT[1] = new Attribute();
					ATT[1].setName("poNo");
					def QRY2;
					QRY2 = sql.firstRow("select * from MSF071 where ENTITY_TYPE = 'WPO' and upper(trim(ENTITY_VALUE)) = upper(trim('"+tools.commarea.District+WO+"')) and REF_NO = '001' and SEQ_NUM = '001'");
					//log.info ("FIND WO  : " + QRY2);
					if(!QRY2.equals(null)) {
						if (QRY2.REF_CODE.trim().equals("")) {
							ATT[1].setValue("");
						}else {
							ATT[1].setValue(QRY2.REF_CODE.trim());
						}
					}else {
						ATT[1].setValue("");
					}
					//log.info("CEK2 : ")
					d[i].getJobsMWPDTO().setCustomAttributes(ATT)
				}

			}
			log.info("===================================================================================")
		}
		return result
	}
	
	public boolean isNumeric(String str) {
		try {
			str = str.replace(",", "")
			Integer.parseInt(str);
			//Float.parseFloat(str);
			return true;
		  }
		  catch (NumberFormatException e) {
			// s is not numeric
			return false;
		  }
	}

}
