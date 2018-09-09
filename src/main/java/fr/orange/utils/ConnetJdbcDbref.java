/**
 * 
 */
package fr.orange.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;


/**
 * @author DLWP2128
 * 
 */
public class ConnetJdbcDbref {

	private Connection conn;
	private Logger logger = Logger.getLogger(ConnetJdbcDbref.class);
	private Long valInit = System.currentTimeMillis();
	private Long valFinal, TimeExeQuerey;
	private float TimeExeQuereyFloat;

	private LuncherInfo luncherInfo;
	private String sourcePath;

	/*
	 * get all channel users
	 */
	public List<String> getNewListUserChannel(String pathMiseAjour) throws Exception {

		//logger.info("*** F, pathMiseAjour : "+pathMiseAjour);

		String urlFileProperties = ConstanteGR.getUrlfileproperties();
		Properties p = GRUtils.loadProperties(urlFileProperties);
		String url = p.getProperty("url.oracle.database.dbref");
		String driver = p.getProperty("driver.database.debref");
		String user = p.getProperty("user.oracle.database.dbref");
		//String pathMiseAjour = p.getProperty("orange.money.transaction.update");
		String pdw = p.getProperty("pdw.oracle.database.dbref");
		try {
			pdw = AESencrp.decrypt(pdw);
		} catch (Exception e) {

			logger.error("Password : " + pdw + " can not decrypted ", e);
		}
		//String path = p.getProperty("orange.money.file.sql");
		String path = pathMiseAjour;

		//List<UserChannel> resultats = new ArrayList<UserChannel>();
		List<String> resultats = new ArrayList<String>();

		
			/* try { */

				Class.forName(driver).newInstance();
				conn = DriverManager.getConnection(url, user, pdw);
				//Statement st = conn.createStatement();
				
				PreparedStatement st = null;
						
				String query;
				/*
				if (timeSlot.contains("Current")) {
					query = getQueryByTimeSlote(timeSlot, pathMiseAjour);
				} else {
					query = getQueryByTimeSlote(timeSlot, path);
				}
				*/
				query = getQueryByTimeSlote(pathMiseAjour);
				//logger.info("*** G, query : "+query);

				//change requested by francis: 5 nov
				//logger.info("Query for " + timeSlot + " : " + query);
				
				/*
				String queryFinal = query;
				int fromIndex=0, whereIndex=0;
				fromIndex = query.indexOf("FROM");
				whereIndex = query.lastIndexOf("WHERE");
				queryFinal = query.substring(0,fromIndex)+"... "+ query.substring(whereIndex);
				*/

				//to be removed later
				//query = "SELECT U.USER_ID as USER_ID, \'C:\R\Reports\Transactions'|| SYS_CONNECT_BY_PATH (translate(u.last_name,'ýÂà âäéèêëîïôöùûüç/&+','yAa_aaeeeeiioouuuc___')||'_'|| translate(u.user_name,'ýÂà âäéèêëîïôöùûüç/&+','yAa_aaeeeeiioouuuc___')||'_'||u.msisdn, '\')|| '\_Synthesis\' as Location, U.MSISDN as MSISDN FROM mmoney.USERS U WHERE U.USER_TYPE='CHANNEL' AND U.STATUS ='Y' AND CONNECT_BY_ISLEAF = 0 AND LEVEL = 1 connect by nocycle prior u.user_id = u.parent_id start with u.user_id = u.owner_id";
				//query = "SELECT U.USER_ID as USER_ID, 'C:\\R\\Reports\\Transactions'";
				//query = "SELECT U.USER_ID as USER_ID,'C:\\R\\Reports\\Transactions'||SYS_CONNECT_BY_PATH (translate(u.last_name,'ýÂà âäéèêëîïôöùûüç/&+','yAa_aaeeeeiioouuuc___')||'_'||translate(u.user_name,'ýÂà âäéèêëîïôöùûüç/&+','yAa_aaeeeeiioouuuc___')||'_'||u.msisdn, '\')||  '\\_Synthesis\' as Location,   U.MSISDN as MSISDNFROM mmoney.USERS UWHERE U.USER_TYPE='CHANNEL'   AND U.STATUS ='Y'   AND CONNECT_BY_ISLEAF = 0   AND LEVEL = 1   connect by nocycle prior u.user_id = u.parent_id   start with u.user_id = u.owner_id";

				//remove sundry binary characters from beginning of Query string
				int indexOfSELECT = query.toLowerCase().indexOf("select");
				if(indexOfSELECT != -1 && indexOfSELECT < 6)	//'select' exists within first 6 characters of the query
				{
					query = query.substring(indexOfSELECT);
				}
				
						
						
				//logger.info("Query : " + queryFinal);
				//logger.info("Query : " + query);
				
				st = conn.prepareStatement(query);
				// filter out empty statements
				//ResultSet rs = st.executeQuery(query);
				ResultSet rs = st.executeQuery();

				
				// logger.info("Generating channel-user list");
				
				ResultSetMetaData rsmd = rs.getMetaData();
				int numberOfColumns = rsmd.getColumnCount();
				//logger.info("numberOfColumns : " + numberOfColumns);

				String columnNames[] = new String[numberOfColumns];	//array containing column names
				for(int i=0;i<numberOfColumns;i++)
				{
					columnNames[i] = rsmd.getColumnLabel(i+1);
				//	logger.info("columnNames["+i+"] : " + columnNames[i]);

				}
				
				while (rs.next()) {
					String params="";
					
					for(String colName: columnNames)
					{
						params += ";" + colName + "="+rs.getString(colName);
					}
					
					resultats.add(params);

					//logger.info("params : " + params);

					/*
					String pathChannel = rs.getString("CHANNEL_USER_Full_Path");
					String userDomainCode = rs
							.getString("CHANNEL_USER_DOMAIN_CODE");
					String lastLtransactionOn = rs
							.getString("LAST_TRANSACTION_ON");
					String userMSISDN = rs.getString("CHANNEL_USER_MSISDN");
					UserChannel userChannel = new UserChannel();
					userChannel.setUserMSISDN(userMSISDN);
					userChannel.setUserDomainCode(userDomainCode);
					userChannel.setLastLtransactionOn(lastLtransactionOn);
					userChannel.setPath(pathChannel);
					resultats.add(userChannel);
					*/

				}
				st.close();
				rs.close();
			/*	
			} catch (ClassNotFoundException ex) {
				logger.error("ClassNotFoundException ", ex);
			} catch (IllegalAccessException ex) {
				logger.error("IllegalAccessException ", ex);
			} catch (InstantiationException ex) {
				logger.error("InstantiationException", ex);
			} catch (SQLException ex) {
				logger.error("SQLException", ex);
			} catch (Exception ex) {
				logger.error("Exception", ex);
			}
			*/

		conn.close();
		
		/* required by francis to comment-out
		valFinal = System.currentTimeMillis();
		TimeExeQuerey = valFinal - valInit;
		TimeExeQuereyFloat = TimeExeQuerey;
		logger.info("Number of channel-users for " + timeSlot + " : "
				+ resultats.size() + "; ExecTime : " + TimeExeQuereyFloat
				/ 1000);
		*/
		
		// logger.info("Execution time of the SQL query : " + TimeExeQuereyFloat
		// / 1000 + " (s)");
		return resultats;
	}

	
	public List<String> getNewListUserChannelMysql(String pathMiseAjour,String mysqlDBSrc) throws Exception {

		String urlFileProperties = ConstanteGR.getUrlfileproperties();
		Properties p = GRUtils.loadProperties(urlFileProperties);
		
		//requirement: 24-nov-16: remove pattern 'mysql' from global config-parameters like 
		//'irt.mysql.database.url', 'irt.mysql.database.driver' , 'irt.mysql.database.user' , 'irt.mysql.database.pdw'
		/*
		String url = p.getProperty(mysqlDBSrc+".mysql.database.url");
		String driver = p.getProperty(mysqlDBSrc+".mysql.database.driver");
		String user = p.getProperty(mysqlDBSrc+".mysql.database.user");
		String pdw = p.getProperty(mysqlDBSrc+".mysql.database.pdw");
		*/
		String url = p.getProperty(mysqlDBSrc+".database.url");
		String driver = p.getProperty(mysqlDBSrc+".database.driver");
		String user = p.getProperty(mysqlDBSrc+".database.user");
		String pdw = p.getProperty(mysqlDBSrc+".database.pdw");
		//requirement:21-sep-2015 the password must be hidden
		pdw = AESencrp.decrypt(pdw);

		String path = pathMiseAjour;
		List<String> resultats = new ArrayList<String>();

				Class.forName(driver).newInstance();
				conn = DriverManager.getConnection(url, user, pdw);
				
				PreparedStatement st = null;
						
				String query;
				query = getQueryByTimeSlote(pathMiseAjour);
	
				int indexOfSELECT = query.toLowerCase().indexOf("select");
				if(indexOfSELECT != -1 && indexOfSELECT < 6)	//'select' exists within first 6 characters of the query
				{
					query = query.substring(indexOfSELECT);
				}

				
				st = conn.prepareStatement(query);

				ResultSet rs = st.executeQuery();

								
				ResultSetMetaData rsmd = rs.getMetaData();
				int numberOfColumns = rsmd.getColumnCount();

				String columnNames[] = new String[numberOfColumns];	//array containing column names
				for(int i=0;i<numberOfColumns;i++)
				{
					columnNames[i] = rsmd.getColumnLabel(i+1);
				}
				
				while (rs.next()) {
					String params="";
					
					for(String colName: columnNames)
					{
						params += ";" + colName + "="+rs.getString(colName);
					}
					
					resultats.add(params);


				}
				st.close();
				rs.close();

		conn.close();
		return resultats;
	}
	
	
	public String getQueryByTimeSlote( String path) {
		/*
		int indexIderScor = TimeSlot.indexOf("_");
		String processValue = TimeSlot.substring(indexIderScor + 1);
		String timeSlotValue = TimeSlot.substring(0, indexIderScor);
		path = path.replace("<timeslot>", timeSlotValue); 
		*/
		// logger.info("Task  " + TimeSlot+ " Index Process:  " + processValue);
		String query = AbstratGenerateReport.createQueries(path);
		/*String queryTraited = query.replace("NUM_PROCESS", processValue);
		return queryTraited;*/
		return query;
	}

}
