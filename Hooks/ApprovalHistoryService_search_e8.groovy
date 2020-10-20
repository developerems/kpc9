/**
 * @EMS Nov 2018
 *
 * 20181217 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Initial Coding
 **/
import javax.naming.InitialContext

import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.ellipse.service.m3875.approvalsmanager.ApprovalsManagerService
import com.mincom.ellipse.types.m3875.instances.TransactionRetrievalCriteriaSearchParam;
import com.mincom.ellipse.types.m3875.instances.TransactionDTO
import com.mincom.ellipse.types.m3875.instances.TransactionServiceResult;
import com.mincom.ellipse.service.m3875.approvalhistory.ApprovalHistoryService;
import com.mincom.ellipse.types.m3875.instances.ApprovalHistorySearchParam;
import com.mincom.ellipse.types.m3875.instances.ApprovalHistoryServiceResult;
import com.mincom.ellipse.types.m3875.instances.ApprovalHistoryDTO;
import com.mincom.ellipse.types.m0000.instances.ActiveRowFlag;
import com.mincom.ellipse.types.m0000.instances.CreationDate;
import com.mincom.ellipse.types.m0000.instances.ReasonCode;
import com.mincom.ellipse.types.m0000.instances.StepAppCode;
import com.mincom.ellipse.service.ServiceDTO;
import com.mincom.enterpriseservice.exception.*
import groovy.sql.Sql
import com.mincom.enterpriseservice.ellipse.*
import com.mincom.ellipse.attribute.Attribute


class ApprovalHistoryService_search_e8 extends ServiceHook{

	String hookVersion = "1"
	String CNT_NO = "";
	String CIC_NO = "";

	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	
	@Override
	public Object onPreExecute(Object input) {
		log.info("Hooks ApprovalHistoryService_retrieve onPreExecute logging.version: ${hookVersion}")

		ApprovalHistorySearchParam c = (ApprovalHistorySearchParam) input
		log.info("TransactionDTO: " + c)
		
		if (c.getTran877Type().getValue().equals("VA")){
			List<Attribute> custAttribs = c.getCustomAttributes()
			custAttribs.each{Attribute customAttribute ->
				log.info ("Attribute Name = ${customAttribute.getName()}")
				log.info ("Attribute Value = ${customAttribute.getValue()}")
	
				if (customAttribute.getName().equals(new String("PAR_CNT_NO"))){
					CNT_NO = customAttribute.getValue()
				}
				if (customAttribute.getName().equals(new String("PAR_CIC_NO"))){
					CIC_NO = customAttribute.getValue()
				}
			}
			def QRY7 = sql.firstRow("select * from msf071 " +
				"where ENTITY_TYPE = 'CIV' and ENTITY_VALUE = '"+tools.commarea.District.trim()+CNT_NO.trim()+CIC_NO.trim()+"' and REF_NO = '001' and SEQ_NUM = '001'");
				log.info ("FIND VALN_NO  : " + QRY7);
			if(!QRY7.equals(null)) {
				c.getDistrictCode().setValue(tools.commarea.District)
				c.getTransactionApprovalkeyPart1().setValue(CNT_NO)
				c.getTransactionApprovalkeyPart2().setValue(QRY7.REF_CODE.trim())
			}else {
				if (!CIC_NO.trim().equals("")) {
					// Raise Error
					throw new EnterpriseServiceOperationException(
					new ErrorMessageDTO(
					"9999", "CIC NUMBER DOES NOT EXIST OR CIC DOES NOT HAVE VALUATION!", "", 0, 0))
	
					return input
				}
			}
		}

		return null
	}

	@Override
	public Object onPostExecute(Object input, Object result) {
		log.info("Hooks ApprovalHistoryService_retrieve onPostExecute logging.version: ${hookVersion}")
		ApprovalHistorySearchParam c = (ApprovalHistorySearchParam) input;
		ApprovalHistoryServiceResult[] d = (ApprovalHistoryServiceResult[]) result;
		String ContractNo = c.getTransactionApprovalkeyPart1().getValue();
		String ValnNo = c.getTransactionApprovalkeyPart2().getValue();
		String TransKey = String.format("%1\$-" + "8" + "s", ContractNo) + ValnNo;
		Integer CTR = 0;
		Integer CTR2 = 0;
		if (c.getTran877Type().getValue().equals("VA")){
			//This function to remove incorrect ellipse standard query result
			Integer i = 0
			//set array counter
			for (i = 0;i<d.length;i++) {
				ApprovalHistoryDTO TRANS_DTO = d[i].getApprovalHistoryDTO();
				log.info("getTransactionKey : " + TRANS_DTO.getTransactionKey().value)
				log.info("TransKey : " + TransKey.trim())
				if(TransKey.trim().equals(TRANS_DTO.getTransactionKey().value.trim())) {
					CTR = CTR + 1
				}
			}
			//copy array
			ApprovalHistoryDTO[] TRANS_DTO2 = new ApprovalHistoryDTO[CTR];
			for (i = 0;i<d.length;i++) {
				ApprovalHistoryDTO TRANS_DTO = d[i].getApprovalHistoryDTO();
				if(TransKey.trim().equals(TRANS_DTO.getTransactionKey().value.trim())) {
					TRANS_DTO2[CTR2] = TRANS_DTO;
					log.info("TRANS_DTO2[CTR2] : " + TRANS_DTO2[CTR2])
					CTR2 = CTR2 + 1;
				}
			}
			//replace array result
			ApprovalHistoryDTO TRANS_DTO = new ApprovalHistoryDTO();
			
			ActiveRowFlag act = new ActiveRowFlag();
			act.setValue(false);
			TRANS_DTO.setActiveRowFlag(act);
			
			for (i = 0;i<d.length;i++) {
				log.info("i : " + i)
				log.info("TRANS_DTO2.length : " + TRANS_DTO2.length)
				if (i<TRANS_DTO2.length) {
					d[i].setApprovalHistoryDTO(TRANS_DTO2[i]);
				}else {
					d[i].setApprovalHistoryDTO(TRANS_DTO);
				}
			}
		}
		
		// Set Position Description
		String assFrPos = "";
		String assFrPosDes = "";
		String assToPos = "";
		String assToPosDes = "";
		Integer i = 0;
		for (i = 0;i<d.length;i++) {
			assFrPos = "";
			assFrPosDes = "";
			assToPos = "";
			assToPosDes = "";
			Attribute[] ATT = new Attribute[2];
			log.info("ATT SIZE: " + ATT.size());
			
			ApprovalHistoryDTO TRANS_DTO3 = d[i].getApprovalHistoryDTO()
			assFrPos = TRANS_DTO3.getOrigPosnId().getValue();
			def QRY0;
			QRY0 = sql.firstRow("select * from msf870 where trim(POSITION_ID) = trim('"+assFrPos+"')");
			if(!QRY0.equals(null)) {
				assFrPosDes = QRY0.POS_TITLE.trim();
			}
			ATT[0] = new Attribute();
			ATT[0].setName("assFromPosDesc");
			ATT[0].setValue(assFrPosDes);
			
			assToPos = TRANS_DTO3.getPositionId().getValue();
			QRY0 = sql.firstRow("select * from msf870 where trim(POSITION_ID) = trim('"+assToPos+"')");
			if(!QRY0.equals(null)) {
				assToPosDes = QRY0.POS_TITLE.trim();
			}
			ATT[1] = new Attribute();
			ATT[1].setName("assToPosDesc");
			ATT[1].setValue(assToPosDes);
			TRANS_DTO3.setCustomAttributes(ATT);
			d[i].setApprovalHistoryDTO(TRANS_DTO3);
		}
		return result;
	}
}