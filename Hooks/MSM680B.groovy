/**
 * @EMS Oct 2019
 *
 * 20191023 - a9ra5213 - Ricky Afriano - KPC UPGRADE
 *            Initial Coding - Hooks to validate custodian id as cost center in MSF920 and MSF810
 **/
package KPC.HOOKS

import com.mincom.ellipse.ejra.mso.GenericMsoRecord;
import com.mincom.ellipse.ejra.mso.MsoErrorMessage;
import com.mincom.ellipse.hook.hooks.MSOHook;
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException;
import groovy.sql.Sql;
import com.mincom.eql.impl.QueryImpl;
import javax.naming.InitialContext;
import javax.persistence.criteria.CriteriaBuilder.Trimspec;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.mincom.ellipse.ejra.mso.MsoField ;
import java.util.Calendar;


public class MSM680B extends MSOHook{
	String hookVersion = "1";

	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
	def sql = new Sql(CAISource)

	@Override
	public GenericMsoRecord onDisplay(GenericMsoRecord screen){
		log.info("Hooks onDisplay MSM680B logging.version: ${hookVersion}");

		return null;
	}

	@Override
	public GenericMsoRecord onPreSubmit(GenericMsoRecord screen){
		log.info("Hooks onPreSubmit MSM680B logging.version: ${hookVersion}");
		if ( ((screen.getNextAction() == 1) || (screen.getNextAction() == 0))) {

			String custId = screen.getField("CUSTODIAN2I").getValue().trim();
			log.info("custId:" + custId);
			if (!custId.trim().equals("")){
				log.info ("isNumeric : " + isNumeric(custId));
				if (isNumeric(custId.trim()).equals(true)) {
					custId = String.format("%010d", Integer.parseInt(custId));
					log.info("custId:" + custId);
				}
				def QRY1 = sql.firstRow("select a.*,b.surname from ( " +
						"select case when LENGTH(TRIM(TRANSLATE(a.COST_CTRE_SEG, ' +-.0123456789', ' '))) is null then LPAD(trim(a.COST_CTRE_SEG),10,'0') " +
						"else trim(a.COST_CTRE_SEG) end NEW_COST_CTRE " +
						"from msf920 a  " +
						"where a.DSTRCT_CODE = '"+tools.commarea.District.trim()+"' and a.ACTIVE_STATUS <> 'I') a " +
						"join msf810 b on (trim(a.NEW_COST_CTRE) = trim(b.EMPLOYEE_ID)) " +
						"where trim(NEW_COST_CTRE) = trim('"+custId+"')");
				if(QRY1.equals(null)) {
					screen.setErrorMessage(new MsoErrorMessage("CUSTODIAN2I", "8541", "INVALID COST CENTER (MSE920/MSE81S) OR INACTIVE !", MsoErrorMessage.ERR_TYPE_ERROR, MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))
					MsoField B_CODE = new MsoField()
					B_CODE.setName("CUSTODIAN2I")
					screen.setCurrentCursorField(B_CODE)
					return screen
				}
			}
		}
		return null;
	}
	@Override
	public GenericMsoRecord onPostSubmit(GenericMsoRecord input, GenericMsoRecord result) {
		log.info("Hooks onPostSubmit MSM680B logging.version: ${hookVersion}");

		return result
	}
	public boolean isNumeric(String str) {
		try {
			str = str.replace(",", "")
			//Integer.parseInt(str);
			Float.parseFloat(str);
			return true;
		}
		catch (NumberFormatException e) {
			// s is not numeric
			return false;
		}
	}
}
