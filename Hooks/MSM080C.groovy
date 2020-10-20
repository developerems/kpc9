//package KPC
//Eghy Hasugian  : Kondisi parameter report KPR65A 

import com.mincom.ellipse.ejra.mso.GenericMsoRecord;
import com.mincom.ellipse.ejra.mso.MsoErrorMessage;
import com.mincom.ellipse.hook.hooks.MSOHook;
import com.mincom.enterpriseservice.ellipse.ConnectionId
import com.mincom.enterpriseservice.ellipse.EllipseScreenService
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException;
import com.mincom.enterpriseservice.ellipse.*
import groovy.sql.Sql;

import com.mincom.eql.impl.QueryImpl;

//import javax.naming.InitialContext;

import com.mincom.ellipse.ejra.mso.MsoField;
import com.mincom.ellipse.client.connection.*
import com.mincom.ellipse.ejra.mso.*;



public class MSM080C extends MSOHook {

	String hookVersion = "KPC001"
	String district , koderpt ,param1 , param2, RangePar1, RangePar2
	
	GenericMsoRecord onDisplay(GenericMsoRecord screen){
		log.info("Hooks onDisplay MSM080C logging.version: ${hookVersion}");
		return null
	}

	@Override
	GenericMsoRecord onPreSubmit(GenericMsoRecord screen) {
		log.info("Hooks onPreSubmit MSM080B logging.version: ${hookVersion}");
		
		koderpt = screen.getField("REPORT3I").getValue() ? screen.getField("REPORT3I").getValue().trim() : ''
		log.info("ARSIADI REPORT3I: $koderpt")

		if (koderpt == "KPR65A" || koderpt == "KPR65B") {
			param1 = screen.getField("PARM3I1").getValue()
			param2 = screen.getField("PARM3I2").getValue()

			log.info("Param 1 :" + param1 )
			log.info("Param 2 :" + param2 )

			RangePar1 = param1.substring(0,1)
			if (param2) {
				RangePar2 = param2.substring(0,1)
				log.info("RangePar2 ARS:" + RangePar2 )
			} else {
				screen.setErrorMessage(new MsoErrorMessage("Error ", "Error : ",
						"Please Input Parameter: Equipment From",
						MsoErrorMessage.ERR_TYPE_ERROR,
						MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))
				screen.setCurrentCursorField(screen.getField("PARM3I2"))
				return screen
			}

			log.info("RangePar1 :" + RangePar1 )
			log.info("RangePar2 :" + RangePar2 )

			if (!RangePar2.trim().equals(RangePar1)){
				screen.setErrorMessage(new MsoErrorMessage("Error ", "Error : ",
									 "Please Input Parameter (Equipment To) Same As Parameter (Equipment From) at the first digit..!",
									 MsoErrorMessage.ERR_TYPE_ERROR,
									 MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))
				screen.setCurrentCursorField(screen.getField("PARM3I2"))
				return screen
			}
		}
		return null
	}
	
	 @Override
	 public GenericMsoRecord onPostSubmit(GenericMsoRecord input, GenericMsoRecord result) {
		 log.info("Hooks onPostSubmit logging.version: ${hookVersion}")
		  
		 return result
	 }	
}
