package com.mincom.ellipse.script.custom
import javax.naming.InitialContext

import org.slf4j.LoggerFactory;
import com.mincom.batch.environment.*;
import com.mincom.batch.script.*;
import com.mincom.ellipse.script.util.*;
import groovy.sql.Sql;
import com.mincom.ellipse.types.m3140.instances.ReactivationDTO;
import com.mincom.ellipse.types.m3140.instances.ReactivationIssueRequisitionDTO;
import com.mincom.ellipse.types.m3140.instances.ReactivationSearchParam;
import com.mincom.ellipse.service.m3140.reactivation.*
import com.mincom.ellipse.types.m3140.instances.ReactivationIssueRequisitionsSearchParam;
import com.mincom.ellipse.types.m3140.instances.ReactivationServiceResult;
import com.mincom.ellipse.types.m3140.instances.ReactivationIssueRequisitionServiceResult;
import com.mincom.ews.service.connectivity.OperationContext;
import com.mincom.ellipse.types.m0000.instances.BooleanReal;
import com.mincom.ellipse.types.m0000.instances.WhouseId;
import com.mincom.ellipse.types.m0000.instances.StockCode;
import com.mincom.ellipse.types.m0000.instances.IreqNo;
import com.mincom.ellipse.types.m0000.instances.IreqItem;
import com.mincom.ellipse.types.m0000.instances.DstrctCode;
import com.mincom.ellipse.types.m0000.instances.PrinterName;
import com.mincom.ellipse.types.m0000.instances.QtySoh;
import com.mincom.ellipse.types.m0000.instances.PartIssue;
import com.mincom.ellipse.types.m0000.instances.Security;
import com.mincom.ellipse.app.security.*;
import com.mincom.ellipse.security.SecurityTokenService;
import com.mincom.ellipse.security.SecurityTokenProvider;
import com.mincom.ellipse.app.security.login.EllipseLoginToUserProvider;

class ParamsKPB1RE{
	String Warehouse_Id
}

class ProcessKPB1RE{
	private version = 1
	private ParamsKPB1RE batchParams
	private EDOIWrapper edoi
	private EROIWrapper eroi
	private ServiceWrapper services
	private CommAreaScriptWrapper commarea
	private Reports report
	private Params params
	private RequestInterface request
	private BatchEnvironment env
	private String uuid
	private String Taskuuid
	private String ErrorMessage = ""
	private String district = ""
	private String stockcode =""
	private String whouseid=""
	private String partnumber = ""
	private String printer = ""
	private String daterequired=""
	private String ireqno=""
	private String ireqitem=""
	private String partissue=""
	private String prioritycode =""
	private String quantityOutstanding=""
	private String quantityReactivate =""
	private String quantityrequested=""
	private String requestby = ""
	private String requiredbydate=""
	private int qtyreact=0
	private String hasil = ""
	private int i = 0
	private int j = 0

	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)

	public static OperationContext context = new OperationContext();

	void runBatch(Binding b){
		init(b)
		info("runBatch Version : " + version)
		batchParams = params.fill(new ParamsKPB1RE())
		try {
			processBatch();
		} finally {
			printBatchReport();
		}
	}

	private void init(Binding b) {
		edoi = b.getVariable("edoi");
		eroi = b.getVariable("eroi");
		services = b.getVariable("service");
		commarea = b.getVariable("commarea");
		report = b.getVariable("report");
		request = b.getVariable("request");
		params = b.getVariable("params");
		env = b.getVariable("env");

		// gets the uuid from the request, in case the vm 'argument mincom.groovy.classes' is true the uuid will be blank
		uuid = request.getUUID()
		Taskuuid = request.getTaskUuid()
	}

	private void retrieve(){
		info("TES")
		try
		{
			String QueryRetrieve = ("SELECT a.DSTRCT_CODE,a.STOCK_CODE,A.WHOUSE_ID,A.IREQ_NO,A.IREQ_ITEM,(A.QTY_REQ-A.QTY_ISSUED) FROM MSF141 A inner join MSF180_SOH B ON (A.STOCK_CODE = B.STOCK_CODE AND A.DSTRCT_CODE = B.DSTRCT_CODE) inner join MSF001_WCWWWW C ON (A.WHOUSE_ID = C.CONTROL_REC_NO AND A.DSTRCT_CODE = C.DSTRCT_CODE AND C.STOCKLESS_WH ='N') inner join MSF140 D ON (A.IREQ_NO = D.IREQ_NO AND A.DSTRCT_CODE = D.DSTRCT_CODE)  WHERE a.ITEM_141_STAT = '3' AND B.SOH > 0 and D.authsd_status = 'A' and A.AUTHSD_STATUS = 'A' and A.PART_ISSUE = 'Y' AND A.WHOUSE_ID = '${batchParams.Warehouse_Id}'") //ORDER BY a.STOCK_CODE");
			info("QueryRetrieve: $QueryRetrieve")
			sql.eachRow(QueryRetrieve){row ->
				district = row[0];
				stockcode = row[1];
				whouseid = row[2];
				ireqno = row[3];
				ireqitem = row[4];
				qtyreact = row[5];
				info("---From Database-----");
				info("District "+district);
				info("Stockcode " + stockcode);
				info("Warehouse " + whouseid);
				info("Ireq No "+ ireqno);
				info("Ireq Item " + ireqitem);
				reactivate(district,stockcode,whouseid,ireqno,ireqitem,qtyreact);
			}
		}
		catch (Exception  e){
			info(e.getMessage())
			hasil = hasil + e.getMessage();
			//ErrorMessage = e.getMessage();
		}
	}

	private void reactivate(String district,String stockcode,String whouseid,String ireqno,String ireqitem,int Qtyreact){
		try{
			ReactivationService React;
			ReactivationDTO dtos = new ReactivationDTO();
			ReactivationIssueRequisitionDTO dtox = new ReactivationIssueRequisitionDTO();
			//ArrayOfReactivationDTO dtoss = new ArrayOfReactivationDTO();
			ReactivationSearchParam paramx = new ReactivationSearchParam();
			ReactivationIssueRequisitionsSearchParam paramss = new ReactivationIssueRequisitionsSearchParam();
			ReactivationServiceResult[] searchresponse = new ReactivationServiceResult();
			ReactivationIssueRequisitionServiceResult[] searchresponses = new ReactivationIssueRequisitionServiceResult();
			ReactivationIssueRequisitionServiceResult[] searchresponsess = new ReactivationIssueRequisitionServiceResult();

			SecurityToken scr;
			/*scr.district = request.getDistrict();
            scr.userId = request.getUser();
            scr.role = request.getPosition();*/
			EllipseLoginToUserProvider etp;

			SecurityTokenService tst;
			SecurityTokenProvider stp;
			Security srt = new Security();
			SecurityTokenImpl sti;

			/*sti.setDistrict(request.getDistrict());
            sti.setRole(request.getUser());
            sti.setUserId(request.getUser());
            srt.getAsEllipseValue()
            tst.getSecurityToken().getDistrict();
            //srt.setAsEllipseValue(true)*/



			info("User Id "+request.getUser());
			/*info("User Id " + scr.getUserId());
            info("District " + scr.getDistrict());
            info("Position " + scr.getRole());*/
			BooleanReal bool = new BooleanReal();
			BooleanReal bools = new BooleanReal();
			WhouseId whouse = new WhouseId();
			StockCode stock = new StockCode();
			DstrctCode dstrct = new DstrctCode();
			IreqNo ir = new IreqNo();
			IreqItem itm = new IreqItem();
			PrinterName prt = new PrinterName();
			QtySoh soh = new QtySoh();
			PartIssue ptr = new PartIssue();
			//ShowPartialIssueRequisitions shows = new ShowPartialIssueRequisitions();

			//setting
			info("MASUK");
			bool.setValue(false);
			bools.setValue(true);
			whouse.setValue(whouseid);
			stock.setValue(stockcode);
			dstrct.setValue(district);
			ir.setValue(ireqno);
			itm.setValue(ireqitem);
			prt.setValue("NO_PRINT");
			soh.setValue(Qtyreact);
			ptr.setValue("Y");

			//params
			paramx.setWarehouseId(whouse);
			paramx.setStockCode(stock);
			paramx.setIncludeAdditionalAttributes(true);
			paramx.setShowPartialIssueRequisitions(bools);
			paramx.setIsSearchBySession(bool);
			//dtos
			dtos.setDistrictCode(dstrct);
			dtos.setStockCode(stock);
			dtos.setWarehouseId(whouse);
			dtos.setIncludeAdditionalAttributes(true);



			//search stockcode
			searchresponse = services.get("Reactivation").search({
				it.isSearchBySession = bool;
				it.showPartialIssueRequisitions=bools;
				it.stockCode = stock;
				it.warehouseId = whouse;
			},false);

			//search ireq
			for(i = 0;i<= searchresponse.length-1;i++){
				info("Value " + searchresponse[i].getReactivationDTO().getDuesOut().getValue())
				// info("Values " +searchresponse[i].getReactivationDTO().getDocumentNumber().getValue())
				searchresponses = services.get("Reactivation").searchIssueRequisitions({
					it.districtCode= dstrct;
					it.stockCode = stock;
					it.warehouseId = whouse;
					it.stockOnHand = searchresponse[i].getReactivationDTO().getStockOnHand();
					it.showPartialIssueRequisitions=bools;
				},false);
			}

			//reactivate
			/* for(j = 0;j<=searchresponses.length-1;j++){
                 info("Nilai " + j);
                 info("Stock Code "+ searchresponses[j].getReactivationIssueRequisitionDTO().getStockCode().getValue())
                 info("Ireq No " + searchresponses[j].getReactivationIssueRequisitionDTO().getIssueRequisitionNumber().getValue())
                 info("Ireq Item No " + searchresponses[j].getReactivationIssueRequisitionDTO().getIssueRequisitionItem().getValue())
                 searchresponses[j].getReactivationIssueRequisitionDTO().setPrinter(prt);
              }*/

			searchresponsess =  services.get("Reactivation").reactivate({
				it.stockCode = stock;
				it.warehouseId = whouse;
				it.printer = prt;
				it.districtCode = dstrct;
				it.issueRequisitionNumber = ir;
				it.issueRequisitionItem = itm;
				it.quantityReactivate = soh;
				it.issueDistrictCode = dstrct;
				it.partIssue = ptr;
			},true);

			info("Length " + searchresponsess.length);
			info("Message " +searchresponsess[0].getInformationalMessages())
			info("CEK ");
			info("CEK 2");

			hasil = hasil + ireqno + " - "+ireqitem +" " +searchresponsess[0].getInformationalMessages()+"\r\n";


		}catch (Exception ex){
			info(ex.message);
		}
	}

	private void processBatch(){
		info("processBatch");
		retrieve();
	}

	private void printBatchReport(){
		info("printBatchReport");
		//print batch report
		def PSBMOPA = report.open("KPB1RE");
		PSBMOPA.write( hasil + " Batch Finished Write Data\r\n");
		if (ErrorMessage.trim() != ""){
			PSBMOPA.write("Error : " + ErrorMessage.trim());
		}
		//report
		PSBMOPA.close();
	}

	private void info(String value){
		def logObject = LoggerFactory.getLogger(getClass());
		logObject.info("------------- " + value)
	}

	private void debug(String value){
		def logObject = LoggerFactory.getLogger(getClass());
		logObject.debug("------------- " + value)
	}

	private String getUUID() {
		return uuid
	}

	private String getTASK_UUID() {
		return Taskuuid
	}
}

/*runscript*/
ProcessKPB1RE process = new ProcessKPB1RE();
process.runBatch(binding);

