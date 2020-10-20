/**
 * 20201010 - Validate TOW Input for Item Service
 * 20201010 - Add function to prevent the costing in item level to a project that still on hold
 */
import javax.naming.InitialContext

import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.service.m3110.envoygather.EnvoyGatherService
import com.mincom.ellipse.types.m3110.instances.GatherableDTO
import com.mincom.ellipse.types.m3110.instances.GatherableServiceResult
import com.mincom.ellipse.types.m3110.instances.GatheringDTO
import com.mincom.ellipse.types.m3110.instances.GatheringServiceResult
import com.mincom.ellipse.errors.Error;
import com.mincom.ellipse.errors.Message;
import com.mincom.ellipse.errors.Warning;
import com.mincom.ellipse.service.ServiceDTO;
import com.mincom.enterpriseservice.exception.*
import groovy.sql.Sql
import com.mincom.enterpriseservice.ellipse.*
import java.text.SimpleDateFormat
import java.util.Date

class EnvoyGatherService_multipleCreateItem extends ServiceHook{
	String hookVersion = "1"
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	String pesan = ""
	String[] preqno
	String preqitemno=""
	String tampung = ""
	String preqtype=""
	String dstrctcode=""
	String planUseDate =""
	String[] towType
	String[] refcode
	String[] itemno
	String[] stdtextkey
	String[] inpoutkpc
	String[] towSupp1
	String[] towSupp2
	String[] towSupp3
	String[] oka
	String[] towCode =""
	String[] itemType
	String stdTextKey=""
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
	String jamini=""
	String StrErr=""
	String projA = "";
	String projB = "";
	String projC = "";
	String projD = "";
	String projE = "";
	String projF = "";
	String projG = "";
	@Override
	public Object onPreExecute(Object input) {
		log.info("Hooks EnvoyGatherService_multipleCreateItem onPreExecute logging.version: $hookVersion")
		GatherableDTO[] e = (GatherableDTO[]) input
		//int panjang = 
		int i = 0;
		itemno = new String[e.length]
		refcode = new String[e.length]
		towType = new String[e.length]
		stdtextkey = new String[e.length]
		inpoutkpc = new String[e.length]
		towSupp1 = new String[e.length]
		towSupp2 = new String[e.length]
		towSupp3 = new String[e.length]
		oka = new String[e.length]
		towCode = new String[e.length]
		itemType = new String[e.length]
		
		def QRY2;
		for (i=0;i<e.length;i++){
			List<Attribute> custAttribs = e[i].getCustomAttributes()
				custAttribs.each{Attribute customAttribute ->
				log.info ("attrName : " + customAttribute.getName());
			    log.info ("attrValue : " + customAttribute.getValue());
				if (customAttribute.getName() == new String("rowNumber")) {
					itemno[i] = customAttribute.getValue()
					itemno[i] = itemno[i].padLeft(3, "0")
					log.info("rowNumbernya adalah  "+itemno[i])
				}
				if (customAttribute.getName() == new String("inpTowCode")) {
					towCode[i] = customAttribute.getValue()
					stdtextkey[i] = towCode[i]
					log.info("stdtextkeynya adalah  "+towCode[i])
				}
				if (customAttribute.getName() == new String("towType")) {
					towType[i] = customAttribute.getValue()
					if (customAttribute.getValue() == "O"){
						stdtextkey[i] = "OKA"
						log.info("stdtextkeynya adalah  "+stdtextkey[i])
					}
				}
				if (customAttribute.getName() == new String("inpTowSupp1")) {
					towSupp1[i] = customAttribute.getValue()
					refcode[i] = customAttribute.getValue()
					log.info("TOWSuppnya adalah  "+refcode[i])
				}
				if (customAttribute.getName() == new String("inpTowSupp2")) {
					towSupp2[i] = customAttribute.getValue()
					if(towSupp2[i].equals(null) || towSupp2[i].equals(" ")){
						towSupp2[i] = ""			
					}
					refcode[i]  = refcode[i] + towSupp2[i]
					log.info("TOWSuppnya adalah  "+refcode[i])
				}
				if (customAttribute.getName() == new String("inpTowSupp3")) {
					towSupp3[i] = customAttribute.getValue()
					if(towSupp3[i].equals(null) || towSupp3[i].equals(" ")){
						towSupp3[i] = ""
					}
					refcode[i]  = refcode[i] + towSupp3[i]
					log.info("TOWSuppnya adalah  "+refcode[i])
				}
				if (customAttribute.getName() == new String("inpOutKpc")) {
					oka[i] = customAttribute.getValue()
					refcode[i]  = customAttribute.getValue()
					log.info("OKAnya adalah  "+refcode[i])
				}
			}
			if(e[i].getItemType().getValue().trim()=="V"){
				if (towType[i] == "" || towType[i].equals(null)){
					throw new EnterpriseServiceOperationException(
						new ErrorMessageDTO(
						"9999", "TOW DATA REQUIRED ! ITEM " + itemno[i], "", 0, 0))
						return input
				}
				else{
					if (towType[i] == "O"){
						if(oka[i] == "" || oka[i].equals(null)){
							throw new EnterpriseServiceOperationException(
								new ErrorMessageDTO(
								"9999", "INPUT REQUIRED!", "OKA", 0, 0))
								return input
						}
					}
					else if (towType[i] == "T"){
						if(towCode[i] == "" || towCode[i].equals(null)){
							throw new EnterpriseServiceOperationException(
								new ErrorMessageDTO(
								"9999", "INPUT REQUIRED!", "TYPE OF WORK", 0, 0))
								return input
						}
						else{
							QRY2 = sql.firstRow("select * from msf010 where TABLE_TYPE = 'TOW' and trim(upper(TABLE_CODE)) = trim(upper('"+towCode[i].trim()+"'))");
							//log.info ("FIND PR  : " + QRY1);
							if(QRY2.equals(null)) {
								throw new EnterpriseServiceOperationException(
									new ErrorMessageDTO(
									"9999", "TOW CODE DOESN'T EXIST", "TOW CODE", 0, 0))
									return input
							}
							else{
								if(towSupp1[i] == "" || towSupp1[i].equals(null)){
									throw new EnterpriseServiceOperationException(
										new ErrorMessageDTO(
										"9999", "INPUT REQUIRED!", "SUPPLIER 1", 0, 0))
										return input
								}
								else{
									QRY2 = sql.firstRow("select * from msf010 where TABLE_TYPE = 'TOS' and trim(upper(TABLE_CODE)) = trim(upper('"+towCode[i].trim()+towSupp1[i].trim()+"'))");
									//log.info ("FIND PR  : " + QRY1);
									if(QRY2.equals(null)) {
										throw new EnterpriseServiceOperationException(
											new ErrorMessageDTO(
											"9999", "SUPPLIER DOESN'T EXIST IN TOW", "SUPPLIER 1", 0, 0))
											return input
									}
								}
								
								if(towSupp2[i] != "" && !towSupp2[i].equals(null)){
									QRY2 = sql.firstRow("select * from msf010 where TABLE_TYPE = 'TOS' and trim(upper(TABLE_CODE)) = trim(upper('"+towCode[i].trim()+towSupp2[i].trim()+"'))");
									//log.info ("FIND PR  : " + QRY1);
									if(QRY2.equals(null)) {
										throw new EnterpriseServiceOperationException(
											new ErrorMessageDTO(
											"9999", "SUPPLIER DOESN'T EXIST IN TOW", "SUPPLIER 2", 0, 0))
											return input
									}
								}
								
								if((towSupp2[i] == "" || towSupp2[i].equals(null)) && (towSupp3[i] != "" && !towSupp3[i].equals(null))){
									throw new EnterpriseServiceOperationException(
										new ErrorMessageDTO(
										"9999", "INPUT REQUIRED!", "SUPPLIER 2", 0, 0))
										return input
								}
								
								if((towSupp1[i].equals(towSupp2[i]) && !towSupp1[i].equals("") && !towSupp2[i].equals("") && !towSupp1[i].equals(null) && !towSupp2[i].equals(null)) ||
									(towSupp1[i].equals(towSupp3[i]) && !towSupp1[i].equals("") && !towSupp3[i].equals("") && !towSupp1[i].equals(null) && !towSupp3[i].equals(null)) ||
									(towSupp2[i].equals(towSupp3[i]) && !towSupp2[i].equals("") && !towSupp3[i].equals("") && !towSupp2[i].equals(null) && !towSupp3[i].equals(null))) {
									throw new EnterpriseServiceOperationException(
										new ErrorMessageDTO(
										"9999", "SUPPLIER ALREADY EXIST", "", 0, 0))
										return input
								}
								
								if(towSupp3[i] != "" && !towSupp3[i].equals(null)){
									QRY2 = sql.firstRow("select * from msf010 where TABLE_TYPE = 'TOS' and trim(upper(TABLE_CODE)) = trim(upper('"+towCode[i].trim()+towSupp3[i].trim()+"'))");
									//log.info ("FIND PR  : " + QRY1);
									if(QRY2.equals(null)) {
										throw new EnterpriseServiceOperationException(
											new ErrorMessageDTO(
											"9999", "SUPPLIER DOESN'T EXIST IN TOW", "SUPPLIER 3", 0, 0))
											return input
									}
								}
							}
						}
					}
					else{
						throw new EnterpriseServiceOperationException(
							new ErrorMessageDTO(
							"9999", "INVALID TOW TYPE" + itemno[i], "TOW TYPE", 0, 0))
							return input
					}
				}
			}
		  itemType[i] = e[i].getItemType().getValue()
		  projA = e[i].getCostingProjectNumberA().getValue()
		  projB = e[i].getCostingProjectNumberB().getValue()
		  projC = e[i].getCostingProjectNumberC().getValue()
		  projD = e[i].getCostingProjectNumberD().getValue()
		  projE = e[i].getCostingProjectNumberE().getValue()
		  projF = e[i].getCostingProjectNumberF().getValue()
		  projG = e[i].getCostingProjectNumberG().getValue()
		  
		  def QRY3;
		  if (!projA.equals(null) && !projA.equals("")) {
			  //Search Top Parent
			  QRY3 = sql.firstRow("SELECT DISTINCT ML.PROJECT_NO FROM msf660 ml WHERE CONNECT_BY_ISLEAF = 1 START WITH ml.DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and trim(ML.PROJECT_NO) = '"+projA.trim()+"' CONNECT BY ML.PROJECT_NO = prior ML.PARENT_PROJ");
			  if(!QRY3.equals(null)) {
				  projA = QRY3.PROJECT_NO
			  }else {
				  projA = "";
			  }

			  QRY3 = sql.firstRow("SELECT * FROM MSF071 where ENTITY_TYPE = 'PRJ' and trim(ENTITY_VALUE) = trim('"+tools.commarea.District.trim()+projA.trim()+"') and REF_NO = '004'");
			  if(!QRY3.equals(null)) {
				  if(QRY3.REF_CODE.trim().equals("Y")) {
					  throw new EnterpriseServiceOperationException(
					  new ErrorMessageDTO(
					  "9999", "PROJECT STATUS HOLD!", "projectA", 0, 0))
					  return input
				  }
			  }
		  }

		  if (!projB.equals(null) && !projB.equals("")) {
			  //Search Top Parent
			  QRY3 = sql.firstRow("SELECT DISTINCT ML.PROJECT_NO FROM msf660 ml WHERE CONNECT_BY_ISLEAF = 1 START WITH ml.DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and trim(ML.PROJECT_NO) = '"+projB.trim()+"' CONNECT BY ML.PROJECT_NO = prior ML.PARENT_PROJ");
			  if(!QRY3.equals(null)) {
				  projB = QRY3.PROJECT_NO
			  }else {
				  projB = "";
			  }

			  QRY3 = sql.firstRow("SELECT * FROM MSF071 where ENTITY_TYPE = 'PRJ' and trim(ENTITY_VALUE) = trim('"+tools.commarea.District.trim()+projB.trim()+"') and REF_NO = '004'");
			  if(!QRY3.equals(null)) {
				  if(QRY3.REF_CODE.trim().equals("Y")) {
					  throw new EnterpriseServiceOperationException(
					  new ErrorMessageDTO(
					  "9999", "PROJECT STATUS HOLD!", "projectB", 0, 0))
					  return input
				  }
			  }
		  }

		  if (!projC.equals(null) && !projC.equals("")) {
			  //Search Top Parent
			  QRY3 = sql.firstRow("SELECT DISTINCT ML.PROJECT_NO FROM msf660 ml WHERE CONNECT_BY_ISLEAF = 1 START WITH ml.DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and trim(ML.PROJECT_NO) = '"+projC.trim()+"' CONNECT BY ML.PROJECT_NO = prior ML.PARENT_PROJ");
			  if(!QRY3.equals(null)) {
				  projC = QRY3.PROJECT_NO
			  }else {
				  projC = "";
			  }

			  QRY3 = sql.firstRow("SELECT * FROM MSF071 where ENTITY_TYPE = 'PRJ' and trim(ENTITY_VALUE) = trim('"+tools.commarea.District.trim()+projC.trim()+"') and REF_NO = '004'");
			  if(!QRY3.equals(null)) {
				  if(QRY3.REF_CODE.trim().equals("Y")) {
					  throw new EnterpriseServiceOperationException(
					  new ErrorMessageDTO(
					  "9999", "PROJECT STATUS HOLD!", "projectC", 0, 0))
					  return input
				  }
			  }
		  }

		  if (!projD.equals(null) && !projD.equals("")) {
			  //Search Top Parent
			  QRY3 = sql.firstRow("SELECT DISTINCT ML.PROJECT_NO FROM msf660 ml WHERE CONNECT_BY_ISLEAF = 1 START WITH ml.DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and trim(ML.PROJECT_NO) = '"+projD.trim()+"' CONNECT BY ML.PROJECT_NO = prior ML.PARENT_PROJ");
			  if(!QRY3.equals(null)) {
				  projD = QRY3.PROJECT_NO
			  }else {
				  projD = "";
			  }

			  QRY3 = sql.firstRow("SELECT * FROM MSF071 where ENTITY_TYPE = 'PRJ' and trim(ENTITY_VALUE) = trim('"+tools.commarea.District.trim()+projD.trim()+"') and REF_NO = '004'");
			  if(!QRY3.equals(null)) {
				  if(QRY3.REF_CODE.trim().equals("Y")) {
					  throw new EnterpriseServiceOperationException(
					  new ErrorMessageDTO(
					  "9999", "PROJECT STATUS HOLD!", "projectDescription", 0, 0))
					  return input
				  }
			  }
		  }

		  if (!projE.equals(null) && !projE.equals("")) {
			  //Search Top Parent
			  QRY3 = sql.firstRow("SELECT DISTINCT ML.PROJECT_NO FROM msf660 ml WHERE CONNECT_BY_ISLEAF = 1 START WITH ml.DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and trim(ML.PROJECT_NO) = '"+projE.trim()+"' CONNECT BY ML.PROJECT_NO = prior ML.PARENT_PROJ");
			  if(!QRY3.equals(null)) {
				  projE = QRY3.PROJECT_NO
			  }else {
				  projE = "";
			  }

			  QRY3 = sql.firstRow("SELECT * FROM MSF071 where ENTITY_TYPE = 'PRJ' and trim(ENTITY_VALUE) = trim('"+tools.commarea.District.trim()+projE.trim()+"') and REF_NO = '004'");
			  if(!QRY3.equals(null)) {
				  if(QRY3.REF_CODE.trim().equals("Y")) {
					  throw new EnterpriseServiceOperationException(
					  new ErrorMessageDTO(
					  "9999", "PROJECT STATUS HOLD!", "projectE", 0, 0))
					  return input
				  }
			  }
		  }

		  if (!projF.equals(null) && !projF.equals("")) {
			  //Search Top Parent
			  QRY3 = sql.firstRow("SELECT DISTINCT ML.PROJECT_NO FROM msf660 ml WHERE CONNECT_BY_ISLEAF = 1 START WITH ml.DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and trim(ML.PROJECT_NO) = '"+projF.trim()+"' CONNECT BY ML.PROJECT_NO = prior ML.PARENT_PROJ");
			  if(!QRY3.equals(null)) {
				  projF = QRY3.PROJECT_NO
			  }else {
				  projF = "";
			  }

			  QRY3 = sql.firstRow("SELECT * FROM MSF071 where ENTITY_TYPE = 'PRJ' and trim(ENTITY_VALUE) = trim('"+tools.commarea.District.trim()+projF.trim()+"') and REF_NO = '004'");
			  if(!QRY3.equals(null)) {
				  if(QRY3.REF_CODE.trim().equals("Y")) {
					  throw new EnterpriseServiceOperationException(
					  new ErrorMessageDTO(
					  "9999", "PROJECT STATUS HOLD!", "projectF", 0, 0))
					  return input
				  }
			  }
		  }

		  if (!projG.equals(null) && !projG.equals("")) {
			  //Search Top Parent
			  QRY3 = sql.firstRow("SELECT DISTINCT ML.PROJECT_NO FROM msf660 ml WHERE CONNECT_BY_ISLEAF = 1 START WITH ml.DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and trim(ML.PROJECT_NO) = '"+projG.trim()+"' CONNECT BY ML.PROJECT_NO = prior ML.PARENT_PROJ");
			  if(!QRY3.equals(null)) {
				  projG = QRY3.PROJECT_NO
			  }else {
				  projG = "";
			  }

			  QRY3 = sql.firstRow("SELECT * FROM MSF071 where ENTITY_TYPE = 'PRJ' and trim(ENTITY_VALUE) = trim('"+tools.commarea.District.trim()+projG.trim()+"') and REF_NO = '004'");
			  if(!QRY3.equals(null)) {
				  if(QRY3.REF_CODE.trim().equals("Y")) {
					  throw new EnterpriseServiceOperationException(
					  new ErrorMessageDTO(
					  "9999", "PROJECT STATUS HOLD!", "projectG", 0, 0))
					  return input
				  }
			  }
		  }
	    }
		return null;
	}
	@Override
	public Object onPostExecute(Object input, Object result) {
		log.info("Hooks EnvoyGatherService_multipleCreateItem onPostExecute logging.version: ${hookVersion}")
		//GatheringServiceResult r = new GatheringServiceResult()
		GatherableServiceResult[] r = (GatherableServiceResult[]) result
		String gathid = r[0].getGatherableDTO().getGatheringId().getValue()
		String gathid2 = gathid.substring(0, 10)
		SimpleDateFormat formatter1 = new SimpleDateFormat("yyyyMMdd")
		Date date1 = new Date()
		hariini = formatter1.format(date1)
		SimpleDateFormat formatter2 = new SimpleDateFormat("HHmmss")
		Date date2 = new Date()
		jamini = formatter2.format(date2)
		creationtime = jamini
		creationdate = hariini
		creationuser = "ADMIN"
		creationemp = "ADMIN"
		int i = 0
		//String preqno
		for(i=0;i<itemno.length;i++){
			if(itemType[i] == "V"){
				String QueryInsert = ("Insert into MSF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY,LAST_MOD_EMP) values ('TOW','"+gathid2+itemno[i]+"','000','000','" + creationdate + "','" + creationtime + "','" + creationuser + "','"+refcode[i]+"','"+stdtextkey[i]+"','"+creationemp+"')");
				log.info("Query adalah " +QueryInsert)
				sql.execute(QueryInsert);
			}
		}
		//log.info("planUseDatenya adalah " +planUseDate)
		return result;
		
	}

}
