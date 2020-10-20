import javax.naming.InitialContext

import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.service.m3110.envoygather.EnvoyGatherService
import com.mincom.ellipse.types.m3110.instances.GatheringDTO
import com.mincom.ellipse.types.m3110.instances.GatheringServiceResult
import com.mincom.ellipse.types.m3110.instances.GatherableDTO
import com.mincom.ellipse.types.m3110.instances.GatherableServiceResult
import com.mincom.ellipse.errors.Error;
import com.mincom.ellipse.errors.Message;
import com.mincom.ellipse.errors.Warning;
import com.mincom.ellipse.service.ServiceDTO;
import com.mincom.enterpriseservice.exception.*
import groovy.sql.Sql
import com.mincom.enterpriseservice.ellipse.*
import java.text.SimpleDateFormat
import java.util.Date

class EnvoyGatherService_preCommit extends ServiceHook{
	String hookVersion = "1"
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	String pesan = ""
	String[] ireqno
	String preqitemno=""
	String tampung = ""
	String ireqtype=""
	String dstrctcode=""
	String planUseDate =""
	String stdTextKey=""
	String refCode=""
	String creationdate=""
	String creationuser=""
	String creationtime=""
	String creationemp=""
	String projectnoA=""
	String projectnoB=""
	String projectA=""
	String projectB=""
	String dstrctcodeA="";
	String hariini=""
	
	String StrErr=""
	@Override
	public Object onPreExecute(Object input) {
		log.info("Hooks EnvoyGatherService_preCommit onPreExecute logging.version: $hookVersion")
		GatheringDTO d = (GatheringDTO)input
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd")
		//int panjang = 
		List<Attribute> custAttribs = d.getCustomAttributes()
		custAttribs.each{Attribute customAttribute ->
			log.info ("attrName : " + customAttribute.getName());
			log.info ("attrValue : " + customAttribute.getValue());
			if (customAttribute.getName() == new String("planUseDate")) {
				planUseDate = customAttribute.getValue()
				log.info("planUseDatenya adalah  $planUseDate")
			}
		}
		if(!planUseDate.equals(null) && !planUseDate.equals("") ){
			log.info("planUseDatenya adalah $planUseDate")
			//def qryx =  sql.firstRow("select case when '"+planUseDate+"' < to_char(sysdate,'YYYYMMDD') then 'TRUE' else 'FALSE' end FLAG from dual")
			//def qryz = sql.
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd")
			Date date = new Date()
			hariini = formatter.format(date)
			
			if(Integer.parseInt(planUseDate) < Integer.parseInt(hariini)) {
				StrErr = "PLAN USE DATE CAN'T BE BEFORE TODAYS DATE"
				throw new EnterpriseServiceOperationException(
				new ErrorMessageDTO(
			   "9999", StrErr, "PLAN USE DATE", 0, 0))
				return input
			}
		}
		return null;
	}
	@Override
	public Object onPostExecute(Object input, Object result) {
		log.info("Hooks EnvoyGatherService_preCommit onPostExecute logging.version: ${hookVersion}")
		log.info("planUseDatenya adalah " +planUseDate)
		GatheringServiceResult r = (GatheringServiceResult) result
		String gathid = r.getGatheringDTO().getGatheringId().getValue()
		String gathid2 = gathid.substring(0, 10) //inclusive , exclusive
		//GatherableDTO[] e = new GatherableDTO()
		/*GatherableServiceResult[] e = new GatherableServiceResult()
		String gathid = r.getGatheringDTO().getGatheringId().getValue()
		String gatheid = e[0].getGatherableDTO().getGatheringId().getValue()*/
		
		
		//e = R
		int i = 0
		int j = 0
		def qry1
		projectnoA = r.getGatheringDTO().getIssueProjectA().getValue()
		projectnoB = r.getGatheringDTO().getIssueProjectB().getValue()
		dstrctcodeA = r.getGatheringDTO().getDistrictCode().getValue().trim()
		log.info("projectnoA adalah " +projectnoA)
		log.info("projectnoB adalah " +projectnoB)
		
		Message[] msg = r.getInformationalMessages()
		if(r.hasInformationalMessages()){
			//if(!projectnoA.equals(null) && !projectnoA.equals("")){
				/*def qry2 = sql.firstRow("SELECT * FROM msf660 WHERE trim(PROJECT_NO) ='"+projectnoA.trim()+"' AND trim(DSTRCT_CODE) ='"+dstrctcodeA+"'")
				log.info("FIND QUERY2A "+qry2)*/
				//if(!qry2.equals(null)) {
				/*	def qry3 = sql.firstRow("SELECT * FROM MSF071 where ENTITY_TYPE = 'PRJ' and trim(ENTITY_VALUE) = trim('"+dstrctcodeA+projectnoA.trim()+"') and REF_NO = '004'")
					log.info("FIND QUERY3A "+qry3)
					if(!qry3.equals(null)){
						if(qry3.REF_CODE.trim().equals("Y")){
							throw new EnterpriseServiceOperationException(
								new ErrorMessageDTO(
								"9999", "PROJECT STATUS HOLD!", "projectA", 0, 0))
								return result
						}
					}*/
				//}else {
					//projectnoA = "";
				//}
			//}
			
			//if(!projectnoB.equals(null) && !projectnoB.equals("")){
				/*def qry2 = sql.firstRow("SELECT * FROM msf660 WHERE trim(PROJECT_NO) ='"+projectnoB.trim()+"' AND trim(DSTRCT_CODE) ='"+dstrctcodeA+"'")
				log.info("FIND QUERY2B "+qry2)*/
				//if(!qry2.equals(null)) {
				/*	def qry3 = sql.firstRow("SELECT * FROM MSF071 where ENTITY_TYPE = 'PRJ' and trim(ENTITY_VALUE) = trim('"+dstrctcodeA+projectnoB.trim()+"') and REF_NO = '004'")
					log.info("FIND QUERY3B "+qry3)
					if(!qry3.equals(null)){
						if(qry3.REF_CODE.trim().equals("Y")){
							throw new EnterpriseServiceOperationException(
								new ErrorMessageDTO(
								"9999", "PROJECT STATUS HOLD!", "projectB", 0, 0))
								return result
						}
					}*/
				//}else {
					//projectnoB = "";
				//}
			//}
			ireqno = new String[msg.length]
			for(i=0;i<msg.length;i++){
				pesan = msg[i].getMessageText().trim();
				String[] parts = pesan.split(" ")
				if(!tampung.contains(parts[2])){
					ireqno[i] = parts[2]
					tampung = tampung + parts[2]
					log.info("Ireq Number ke " +i.toString()+" adalah "+ireqno[i])
					log.info("Pesannya adalah " +pesan)
				}	
			}
			for(i=0;i<ireqno.length;i++){
				if(!ireqno[i].equals(null)){
					qry1 = sql.firstRow("SELECT IREQ_NO,ireq_type,dstrct_code,LAST_MOD_USER,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_EMP FROM MSF140 WHERE ireq_no= '"+ireqno[i]+"'")
					if(!qry1.equals(null)){
						ireqtype = qry1.IREQ_TYPE
						dstrctcode = qry1.DSTRCT_CODE
						creationdate = qry1.LAST_MOD_DATE
						creationuser = qry1.LAST_MOD_USER
						creationtime = qry1.LAST_MOD_TIME
						creationemp = qry1.LAST_MOD_EMP
						if (ireqtype.trim()=="NI" && planUseDate!="") {
							log.info("Ireq Typenya adalah " +ireqtype)
							log.info("District Codenya adalah " +dstrctcode)
							log.info("Plan Use Datenya adalah "+planUseDate)
							String QueryInsert = ("Insert into MSF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY,LAST_MOD_EMP) values ('PUD','"+dstrctcode+ireqno[i]+"','001','001','" + creationdate + "','" + creationtime + "','" + creationuser + "','"+planUseDate+"','            ','"+creationemp+"')");
							log.info("Query adalah " +QueryInsert)
							sql.execute(QueryInsert);
						}
					}
					else{
						if(!ireqno[i].equals(null)){
							def qry12 = sql.firstRow("SELECT PREQ_NO,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,LAST_MOD_EMP FROM MSF230 WHERE PREQ_NO ='"+ireqno[i]+"'")
							if(!qry12.equals(null)){
								creationdate = qry12.LAST_MOD_DATE
								creationuser = qry12.LAST_MOD_USER
								creationtime = qry12.LAST_MOD_TIME
								creationemp = qry12.LAST_MOD_EMP
								qry1 = sql.rows("SELECT ENTITY_TYPE,ENTITY_VALUE,REF_CODE,STD_TXT_KEY FROM MSF071 WHERE entity_type ='TOW' and ENTITY_VALUE LIKE '"+gathid2+"%'").each {row->
									//ireqtype = qry1.REQ_TYPE
									dstrctcode = dstrctcodeA
									stdTextKey = row[3].toString()
									refCode = row[2].toString()
									preqitemno = row[1].toString().substring(10)
									log.info("Preq Typenya adalah " +ireqtype)
									log.info("District Codenya adalah " +dstrctcode)
									String QueryInsert = ("Insert into MSF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY,LAST_MOD_EMP) values ('TOW','"+dstrctcode+ireqno[i]+preqitemno+"','000','000','" + creationdate + "','" + creationtime + "','" + creationuser + "','"+refCode+"','"+stdTextKey+"','"+creationemp+"')");
									log.info("Query adalah " +QueryInsert)
									sql.execute(QueryInsert);
									String QueryDelete = ("Delete from MSF071 Where entity_type ='TOW' and entity_value = '"+gathid2+preqitemno+"'")
									sql.execute(QueryDelete);
								}
							}
						}
					}
				}
			}
		}
		return result;
	}

}
