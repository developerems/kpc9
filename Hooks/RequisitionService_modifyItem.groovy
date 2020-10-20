package KPC.HOOKS
/**
 * @EMS Mar 2019
 *
 * 20190902 - Prevent transaction For Project that match criteria
 * 20190718 - add function to set default issuing WH for stock code
 * 20190312 - Eghy Kurniagus - KPC UPGRADE
 *            Initial Coding - UpperCase Descriptions
 **/
import javax.naming.InitialContext

import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.hook.hooks.ServiceHook
import groovy.sql.Sql
import com.mincom.enterpriseservice.ellipse.requisition.RequisitionServiceModifyItemRequestDTO
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import com.mincom.enterpriseservice.ellipse.ErrorMessageDTO
import com.mincom.enterpriseservice.ellipse.dependant.dto.RequisitionItemDTO;


class RequisitionService_modifyItem extends ServiceHook{

	String hookVersion = "1"
	String PREQ_TYPE = "";
	String descA = "";
	String descB = "";
	String descC = "";
	String descD = "";
	String stk = "";
	String projA = "";
	String projB = "";
	String projC = "";
	String projD = "";
	String projE = "";
	String projF = "";
	String projG = "";
	
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	
	@Override
	public Object onPreExecute(Object input) {
		log.info("Hooks RequisitionService_deleteItem onPreExecute logging.version: ${hookVersion}")
				
		RequisitionServiceModifyItemRequestDTO e = (RequisitionServiceModifyItemRequestDTO) input ;
		RequisitionItemDTO[] c;
		
		c = e.getRequisitionItems();
		
		Integer i = 0
		log.info("itm.length : " + c.length);		
		for (i = 0 ; i < c.length ; i++) {	
			
			projA = c[i].getProjectA();
			projB = c[i].getProjectB();
			projC = c[i].getProjectC();
			projD = c[i].getProjectDescription();
			projE = c[i].getProjectE();
			projF = c[i].getProjectF();
			projG = c[i].getProjectG();
			
			def QRY1;
			if (!projA.equals(null) && !projA.equals("")) {
				//Search Top Parent
				QRY1 = sql.firstRow("SELECT DISTINCT ML.PROJECT_NO FROM msf660 ml WHERE CONNECT_BY_ISLEAF = 1 START WITH ml.DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and trim(ML.PROJECT_NO) = '"+projA.trim()+"' CONNECT BY ML.PROJECT_NO = prior ML.PARENT_PROJ");
				if(!QRY1.equals(null)) {
					projA = QRY1.PROJECT_NO
				}else {
					projA = "";
				}
				
				QRY1 = sql.firstRow("SELECT * FROM MSF071 where ENTITY_TYPE = 'PRJ' and trim(ENTITY_VALUE) = trim('"+tools.commarea.District.trim()+projA.trim()+"') and REF_NO = '004'");
				if(!QRY1.equals(null)) {
					if(QRY1.REF_CODE.trim().equals("Y")) {
						throw new EnterpriseServiceOperationException(
							new ErrorMessageDTO(
							"9999", "PROJECT STATUS HOLD!", "projectA", 0, 0))
							return input
					}
				}
			}
			
			if (!projB.equals(null) && !projB.equals("")) {
				//Search Top Parent
				QRY1 = sql.firstRow("SELECT DISTINCT ML.PROJECT_NO FROM msf660 ml WHERE CONNECT_BY_ISLEAF = 1 START WITH ml.DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and trim(ML.PROJECT_NO) = '"+projB.trim()+"' CONNECT BY ML.PROJECT_NO = prior ML.PARENT_PROJ");
				if(!QRY1.equals(null)) {
					projB = QRY1.PROJECT_NO
				}else {
					projB = "";
				}
				
				QRY1 = sql.firstRow("SELECT * FROM MSF071 where ENTITY_TYPE = 'PRJ' and trim(ENTITY_VALUE) = trim('"+tools.commarea.District.trim()+projB.trim()+"') and REF_NO = '004'");
				if(!QRY1.equals(null)) {
					if(QRY1.REF_CODE.trim().equals("Y")) {
						throw new EnterpriseServiceOperationException(
							new ErrorMessageDTO(
							"9999", "PROJECT STATUS HOLD!", "projectB", 0, 0))
							return input
					}
				}
			}
			
			if (!projC.equals(null) && !projC.equals("")) {
				//Search Top Parent
				QRY1 = sql.firstRow("SELECT DISTINCT ML.PROJECT_NO FROM msf660 ml WHERE CONNECT_BY_ISLEAF = 1 START WITH ml.DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and trim(ML.PROJECT_NO) = '"+projC.trim()+"' CONNECT BY ML.PROJECT_NO = prior ML.PARENT_PROJ");
				if(!QRY1.equals(null)) {
					projC = QRY1.PROJECT_NO
				}else {
					projC = "";
				}
				
				QRY1 = sql.firstRow("SELECT * FROM MSF071 where ENTITY_TYPE = 'PRJ' and trim(ENTITY_VALUE) = trim('"+tools.commarea.District.trim()+projC.trim()+"') and REF_NO = '004'");
				if(!QRY1.equals(null)) {
					if(QRY1.REF_CODE.trim().equals("Y")) {
						throw new EnterpriseServiceOperationException(
							new ErrorMessageDTO(
							"9999", "PROJECT STATUS HOLD!", "projectC", 0, 0))
							return input
					}
				}
			}
			
			if (!projD.equals(null) && !projD.equals("")) {
				//Search Top Parent
				QRY1 = sql.firstRow("SELECT DISTINCT ML.PROJECT_NO FROM msf660 ml WHERE CONNECT_BY_ISLEAF = 1 START WITH ml.DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and trim(ML.PROJECT_NO) = '"+projD.trim()+"' CONNECT BY ML.PROJECT_NO = prior ML.PARENT_PROJ");
				if(!QRY1.equals(null)) {
					projD = QRY1.PROJECT_NO
				}else {
					projD = "";
				}
				
				QRY1 = sql.firstRow("SELECT * FROM MSF071 where ENTITY_TYPE = 'PRJ' and trim(ENTITY_VALUE) = trim('"+tools.commarea.District.trim()+projD.trim()+"') and REF_NO = '004'");
				if(!QRY1.equals(null)) {
					if(QRY1.REF_CODE.trim().equals("Y")) {
						throw new EnterpriseServiceOperationException(
							new ErrorMessageDTO(
							"9999", "PROJECT STATUS HOLD!", "projectDescription", 0, 0))
							return input
					}
				}
			}
			
			if (!projE.equals(null) && !projE.equals("")) {
				//Search Top Parent
				QRY1 = sql.firstRow("SELECT DISTINCT ML.PROJECT_NO FROM msf660 ml WHERE CONNECT_BY_ISLEAF = 1 START WITH ml.DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and trim(ML.PROJECT_NO) = '"+projE.trim()+"' CONNECT BY ML.PROJECT_NO = prior ML.PARENT_PROJ");
				if(!QRY1.equals(null)) {
					projE = QRY1.PROJECT_NO
				}else {
					projE = "";
				}
				
				QRY1 = sql.firstRow("SELECT * FROM MSF071 where ENTITY_TYPE = 'PRJ' and trim(ENTITY_VALUE) = trim('"+tools.commarea.District.trim()+projE.trim()+"') and REF_NO = '004'");
				if(!QRY1.equals(null)) {
					if(QRY1.REF_CODE.trim().equals("Y")) {
						throw new EnterpriseServiceOperationException(
							new ErrorMessageDTO(
							"9999", "PROJECT STATUS HOLD!", "projectE", 0, 0))
							return input
					}
				}
			}
			
			if (!projF.equals(null) && !projF.equals("")) {
				//Search Top Parent
				QRY1 = sql.firstRow("SELECT DISTINCT ML.PROJECT_NO FROM msf660 ml WHERE CONNECT_BY_ISLEAF = 1 START WITH ml.DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and trim(ML.PROJECT_NO) = '"+projF.trim()+"' CONNECT BY ML.PROJECT_NO = prior ML.PARENT_PROJ");
				if(!QRY1.equals(null)) {
					projF = QRY1.PROJECT_NO
				}else {
					projF = "";
				}
				
				QRY1 = sql.firstRow("SELECT * FROM MSF071 where ENTITY_TYPE = 'PRJ' and trim(ENTITY_VALUE) = trim('"+tools.commarea.District.trim()+projF.trim()+"') and REF_NO = '004'");
				if(!QRY1.equals(null)) {
					if(QRY1.REF_CODE.trim().equals("Y")) {
						throw new EnterpriseServiceOperationException(
							new ErrorMessageDTO(
							"9999", "PROJECT STATUS HOLD!", "projectF", 0, 0))
							return input
					}
				}
			}
			
			if (!projG.equals(null) && !projG.equals("")) {
				//Search Top Parent
				QRY1 = sql.firstRow("SELECT DISTINCT ML.PROJECT_NO FROM msf660 ml WHERE CONNECT_BY_ISLEAF = 1 START WITH ml.DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and trim(ML.PROJECT_NO) = '"+projG.trim()+"' CONNECT BY ML.PROJECT_NO = prior ML.PARENT_PROJ");
				if(!QRY1.equals(null)) {
					projG = QRY1.PROJECT_NO
				}else {
					projG = "";
				}
				
				QRY1 = sql.firstRow("SELECT * FROM MSF071 where ENTITY_TYPE = 'PRJ' and trim(ENTITY_VALUE) = trim('"+tools.commarea.District.trim()+projG.trim()+"') and REF_NO = '004'");
				if(!QRY1.equals(null)) {
					if(QRY1.REF_CODE.trim().equals("Y")) {
						throw new EnterpriseServiceOperationException(
							new ErrorMessageDTO(
							"9999", "PROJECT STATUS HOLD!", "projectG", 0, 0))
							return input
					}
				}
			}
			
			descA = c[i].getItemDescriptionA().toUpperCase()
			descB = c[i].getItemDescriptionB().toUpperCase()
			descC = c[i].getItemDescriptionC().toUpperCase()
			descD = c[i].getItemDescriptionDescription().toUpperCase()
			
			c[i].setItemDescriptionA(descA)
			c[i].setItemDescriptionB(descB)
			c[i].setItemDescriptionC(descC)
			c[i].setItemDescriptionDescription(descD)
		
			stk = c[i].getStockCode();
			if (!stk.equals("")) {
				QRY1 = sql.firstRow("select * from msf170 where DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and trim(STOCK_CODE) = trim('"+stk+"')");
				if(!QRY1.equals(null)) {
					c[i].setIssueWarehouseId(QRY1.HOME_WHOUSE)
				}
			}
		}	
		return null
	}

	
}