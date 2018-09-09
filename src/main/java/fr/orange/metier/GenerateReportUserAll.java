/**
 * 
 */
package fr.orange.metier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.*;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.net.ftp.*;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.inet.report.Engine;
import com.inet.report.ReportException;

import fr.orange.utils.ConnetJdbcDbref;
import fr.orange.utils.ConstanteGR;
import fr.orange.utils.GRUtils;
import fr.orange.utils.LuncherInfo;

import fr.orange.utils.AESencrp;
import java.text.DecimalFormatSymbols;
import org.apache.commons.lang.StringUtils;
/**
 * @author NMCC5676
 * 
 */
public class GenerateReportUserAll extends AbstratGenerateReport {
	//requirement 6 by francis
	//private Logger logger = Logger.getLogger(GenerateReportUserAll.class);
	private Logger logger = Logger.getLogger("fr.orange.GenerateReports");
	private boolean massiveFlag = false, ambiguousTransferFlag = false, emailParamFlag = false, queryCheckFlag=false, ftpParamFlag = false, globalEmailFlag = false, globalFtpFlag = false, fileCheckFlag = false;
	private String timeSlot = "";
	
	//requirement: 10/09/2015: Bad number format for elapsed time 
	private DecimalFormat df = new DecimalFormat("#.##");
	private DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance();
	
	
	//requirement to print uniform error messages on 20-jul-2015
	String startDateLogging="" , endDateLogging="";
	String targetDirStr = "";
	String targetFileStr = "";
	String repareParam = "";
	
	private float tempsExe=0;
	private long valFinal, valInit;
	private String targetPath, sourcePath, sysdateFormat,generateTypeGlobal,startdateFormat, sourceSqlPath;
	private String ambiguousCountSqlAbsolutePath, mysqlDbSrc, emailParamAbsolutePath, emailRecipients, emailSubject, emailContent, emailCopyRecipients, emailBlindCopyRecipients;
	private String ftpIpGlobal, ftpPortGlobal, ftpUserGlobal, ftpPasswordGlobal,ftpParamAbsolutePath;
	private LuncherInfo luncherInfo;
	private int count = 0;
	private int countMassive = 0;
	private int countToPrint = 0;
	//requirement 7 by francis
	private int countSuccess = 0;
	private int countExisting = 0;	//Forge#374803

	private float  timeAllExec=0;
	private float  timeAllMassiveOfOneType=0;

	private String checkLagSQLPath =  "";
	
	private int lagDays = 0;
	private int lagHour = 0;
	private int lagMin = 0;
	private int lagSec = 0;
	
	private String lagDaysStr = "";
	private String lagHourStr = "";
	private String lagMinStr = "";
	private String lagSecStr = "";

	private int checkLagHour = 0;
	private int checkLagMin = 0;
	private int checkLagSec = 0;
	private int checkLagTotalSeconds = 0;
	private int actualLagSeconds = 0;
	
	private int initialWaitSec = 0;
	private int loopTimes = 0;
	private int waitUntilHour = 0;
	private int waitUntilMin = 0;
	private int waitUntilSec = 0;
	
	public void generateReport(String ChaineAdeCode, String dateString) throws ReportException, Exception {

	String sourceFileName="", targetFileName="",batchFileToExecute="";
	String[] args;
	int indexPoint;
	String exportFormat="";
	Engine engine=null;
	File pdf = new File("");	//dummy initialization
	
	String paraLog = "";
	Path p=null;
	
	String tempDirStr = "";
	String tempFileStr = "";
	
	//requirement: 25-sep: repare functionality
	//Repare=IfMissing/IfEmpty/All (default = All)
	boolean fileMissingFlag = false;
	boolean fileEmptyFlag = false;
	boolean regenerateFlag = false;
	
	try
	{
		//count = count + 1;
		if(countMassive != 0)	//current report is part of massive reports
		{
			countToPrint = countMassive;
			//requirement: 21-sep-15 exectime should be non-cumulative 
			//hence for massive, reset the init (start) time
			valInit = System.currentTimeMillis();
		}
		else
		{
			countToPrint = count;
		}
		
		sourcePath = luncherInfo.getSourcepath();
		sourceSqlPath = luncherInfo.getSqlpath();

		targetPath = luncherInfo.getTagetpath();
		sysdateFormat = luncherInfo.getSysdateFormat();
		startdateFormat = luncherInfo.getStartdateFormat();
		
		args = ChaineAdeCode.split(";");
		sourceFileName = args[0];
		
		//requirement to execute .cmd batch files
		if(sourceFileName.endsWith(".cmd"))		//execute only the batch file
		{
			batchFileToExecute = sourcePath + sourceFileName;
			targetFileName = sourceFileName;	//just for logging purpose in case of exception
			regenerateFlag = true;
		}
		else
		{
			targetFileName = args[1];
			indexPoint = targetFileName.lastIndexOf(".");
			exportFormat = targetFileName.substring(indexPoint + 1);
			
			//requirement 2-sep-15: txt2csv format the file name is first initialized with a .txt2csv extension
			//fix that to initialized the name directly with a .csv extension	
			if(exportFormat.equalsIgnoreCase("txt2csv"))
			{
				targetFileName = targetFileName.replace(".txt2csv",".csv");
			}
			
			//valInit = System.currentTimeMillis();
	
			//logger.info("Start report execution " + sourceFileName);
			//logger.info("** 0 **");
	
			engine = null;
			//try {
				if (exportFormat.equalsIgnoreCase("pdf")) {
					engine = new Engine(Engine.EXPORT_PDF);
				} else if (exportFormat.equalsIgnoreCase("csv")) {
					engine = new Engine(Engine.EXPORT_DATA);
					Properties props = new Properties();
					props.setProperty("quotechar", "");
					engine.setUserProperties(props);
				} else if (exportFormat.equalsIgnoreCase("xls")) {
					engine = new Engine(Engine.EXPORT_XLS);
				} else if (exportFormat.equalsIgnoreCase("txt")) {
					engine = new Engine(Engine.EXPORT_TXT);
				} else if (exportFormat.equalsIgnoreCase("txt2csv")) {	//change to incorporate txt2csv
					engine = new Engine(Engine.EXPORT_TXT);
				} else if (exportFormat.equalsIgnoreCase("html")) {
					engine = new Engine(Engine.EXPORT_HTML);
				} else if (exportFormat.equalsIgnoreCase("xml")) {
					engine = new Engine(Engine.EXPORT_XML);
				} else {
					logger.error(exportFormat + " Not supported");
				}
			/*	
			} catch (ReportException e1) {
				// TODO Auto-generated catch block
				///*	requirement by francis to change error case log messages	
				//logger.error("ReportException: ", e1);
				
				logger.error(count + ";" + generateTypeGlobal + ";FAILURE;" + e1.getErrorCode() + ";"+ targetFileName + ";ExecTime " + (System.currentTimeMillis() - valInit) / 1000 + "(s)" );					
				logger.error("ReportException:", e1);
	
				//requirement by francis to produce correct number of failed reports 
				throw e1;
			}
			*/
			//try {
	
				engine.setReportFile(sourcePath + sourceFileName);
				//logger.info("** 0A **");
				/*
			} catch (ReportException e) {
				// TODO Auto-generated catch block
				//logger.error("ReportException: error to set setReportFile", e);
				
				//requirement by francis to change error case log messages			
				logger.error(count + ";" + generateTypeGlobal + ";FAILURE;" + e.getErrorCode() + ";"+ targetFileName + ";ExecTime " + (System.currentTimeMillis() - valInit) / 1000 + "(s)" );					
				logger.error("ReportException:", e);
	
				//requirement by francis to produce correct number of failed reports 
				throw e;
			}
			*/
			//logger.info("** 1 **");
	
			
			HashMap hm = new HashMap();
			String dateForStartEndDateTags="";
			String dateForStartEndDateLogging="";
	
			
			if (args.length > 2) {
				for (int i = 2; i < args.length; i++) {
	
					String paraValue = args[i];
	
					String[] paraValueSplite = paraValue.split("=");
					
					//logger.info("** 1A paraValue: " + paraValue);
					
					//requirement by francis to process date command parameters, both hard-coded and those containing sysdate etc.
					//if (paraValue.contains("sysdate") || paraValue.contains("sysmonth") || paraValue.contains("sysweek") ) {
					
					//requirement to process other params for head-of-networks like USER_ID , MSISDN
					if (  paraValue.contains("sysdate") || paraValue.contains("sysmonth") || paraValue.contains("sysweek") 
							|| paraValue.contains("startDate") || paraValue.contains("date_a") || paraValue.contains("fromdate") 
							|| paraValue.contains("endDate") || paraValue.contains("date_b") || paraValue.contains("todate") )
					{
	
						paraValueSplite[1] = calculateDate(paraValue, sysdateFormat);
						//logger.info("** 1B paraValueSplite[1]:"+paraValueSplite[1]+ "**");
	
						//hm.put(paraValueSplite[0], calculateDate(paraValue, ));
						
						dateForStartEndDateTags = calculateDate(paraValue, startdateFormat);
						//logger.info("** 1C dateForStartEndDateTags:"+dateForStartEndDateTags+ "**");
	
						// change to improve startDate and endDate formats for logging
						dateForStartEndDateLogging = paraValueSplite[1];
					}
					
					//paraLog = paraLog + ";" + paraValueSplite[0] + "="+ paraValueSplite[1];
					//logger.info("** 2 **, paraLog="+paraLog+"**");
	
				//	try {
					
						if( engine.getFields().getPromptField(paraValueSplite[0]) == null)
						{
							//logger.info("** 2 TWO **, paraValueSplite[0]="+paraValueSplite[0]+"**");
							continue;	//e.g. if USER_ID is NOT a required field on input-prompt of KPI report, 
										// then ignore this 'paraValueSplite[0]' command parameter, and check next parameter in command line
						}
	
						paraLog = paraLog + ";" + paraValueSplite[0] + "="+ paraValueSplite[1];
						//logger.info("** 2 THREE **paraValueSplite[0]="+paraValueSplite[0]+"getvalue:"+hm.get(paraValueSplite[0]));

						engine.getFields().getPromptField(paraValueSplite[0]).setPromptValue(paraValueSplite[1]);
	
						if(!dateForStartEndDateTags.equals(""))
						{
							hm.put(paraValueSplite[0], dateForStartEndDateTags);
						}
						else
						{
							hm.put(paraValueSplite[0], paraValueSplite[1]);
						}
						//logger.info("** 3 **paraValueSplite[0]="+paraValueSplite[0]+"getvalue:"+hm.get(paraValueSplite[0]));
						// change to improve startDate and endDate formats for logging
						if(!dateForStartEndDateLogging.equals(""))
						{
							hm.put(paraValueSplite[0]+"_logging", dateForStartEndDateLogging);
						}
						else
						{
							hm.put(paraValueSplite[0]+"_logging", paraValueSplite[1]);
						}
					/*	
					} catch (ReportException e) {
						// TODO Auto-generated catch block
						//logger.error("ReportException", e);
						
						//	requirement by francis to change error case log messages		
						logger.error(count + ";" + generateTypeGlobal + ";FAILURE;" + e.getErrorCode() + ";"+ targetFileName + ";ExecTime " + (System.currentTimeMillis() - valInit) / 1000 + "(s)" );					
						logger.error("ReportException:", e);
	
						//requirement by francis to produce correct number of failed reports 
						throw e;
					}
					*/
				}	// end of for (int i = 2; i < args.length; i++)
			}	// end of if (args.length > 2)
			
			//requirement 6 by francis
			//logger.info(count + "; "+generateTypeGlobal +"; "+ sourceFileName + "; " + targetFileName+ paraLog);
	
			// to print reportdir and reportfile seperately in log messages, move it little down
			//logger.info(count + "; "+generateTypeGlobal +";RUNING;000; "+ sourceFileName + "; " + targetFileName+ paraLog);
			
			//setConfig();
			
			//valInit = System.currentTimeMillis();
			
			targetFileName = targetFileName.replace("<DateFormat>", dateString);
	
			// to calculate new file-naming tags <StartDate> and <EndDate>
			String startDateTag="" , endDateTag="";
			
	
		/*	try
			{  */
				
				//value to be used for <StartDate> tag
				String startDateParam = (String)hm.get("startDate");		
				String date_aParam = (String)hm.get("date_a");
				String fromdateParam = (String)hm.get("fromdate");
	
				//logger.info("** startDateParam:"+startDateParam+":date_aParam:"+date_aParam+":fromdateParam:"+fromdateParam);
	
				if(startDateParam != null && !startDateParam.equals(""))
				{
					startDateTag = startDateParam;
				}
				else if(date_aParam != null && !date_aParam.equals(""))
				{
					startDateTag = date_aParam;
				}
				else if(fromdateParam != null && !fromdateParam.equals(""))
				{
					startDateTag = fromdateParam;
				}
				//value to be used for <EndDate> tag
				String endDateParam = (String)hm.get("endDate");		
				String date_bParam = (String)hm.get("date_b");	
				String todateParam = (String)hm.get("todate");
	
				if(endDateParam != null && !endDateParam.equals(""))
				{
					endDateTag = endDateParam;
				}
				else if(date_bParam != null && !date_bParam.equals(""))
				{
					endDateTag = date_bParam;
				}
				else if(todateParam != null && !todateParam.equals(""))
				{
					endDateTag = todateParam;
				}
				
				
				// additional code to populate value of startDateLogging and endDateLogging for logging purposes
				
				//value to be used for startDateLogging
				String startDateLoggingParam = (String)hm.get("startDate_logging");		
				String date_aLoggingParam = (String)hm.get("date_a_logging");
				String fromdateLoggingParam = (String)hm.get("fromdate_logging");
	
				//logger.info("** 4 **");
	
				if(startDateLoggingParam != null && !startDateLoggingParam.equals(""))
				{
					startDateLogging = startDateLoggingParam;
				}
				else if(date_aLoggingParam != null && !date_aLoggingParam.equals(""))
				{
					startDateLogging = date_aLoggingParam;
				}
				else if(fromdateLoggingParam != null && !fromdateLoggingParam.equals(""))
				{
					startDateLogging = fromdateLoggingParam;
				}
				
				//value to be used for endDateLogging
				String endDateLoggingParam = (String)hm.get("endDate_logging");		
				String date_bLoggingParam = (String)hm.get("date_b_logging");	
				String todateLoggingParam = (String)hm.get("todate_logging");
	
				if(endDateLoggingParam != null && !endDateLoggingParam.equals(""))
				{
					endDateLogging = endDateLoggingParam;
				}
				else if(date_bLoggingParam != null && !date_bLoggingParam.equals(""))
				{
					endDateLogging = date_bLoggingParam;
				}
				else if(todateLoggingParam != null && !todateLoggingParam.equals(""))
				{
					endDateLogging = todateLoggingParam;
				}
				
				
	
				
				
	/*		} catch (ReportException e) {
				// TODO Auto-generated catch block
				logger.error("ReportException", e);
			}  */
			
			targetFileName = targetFileName.replace("<StartDate>", startDateTag);
			targetFileName = targetFileName.replace("<EndDate>", endDateTag);
			targetFileName = targetFileName.replace("<CurDate>", curDateTag());
			targetFileName = targetFileName.replace("<CurDateTime>", curDateTimeTag());
			targetFileName = targetFileName.replace("<PrevDay>", prevDayTag());
			targetFileName = targetFileName.replace("<PrevWeek>", prevWeekTag());
			targetFileName = targetFileName.replace("<PrevMonth>", prevMonthTag());
			targetFileName = targetFileName.replace("<PrevWeekNumber>", prevWeekNumberTag());
			targetFileName = targetFileName.replace("<PrevMonthNumber>", prevMonthNumberTag());
			targetFileName = targetFileName.replace("<PrevMonthName>", prevMonthNameTag());
	
			//check to put for Channel_User_KPI_Report-1.0.1.rpt
			//change request to include timeslot functionality for GR_Unpaid_Bill_Report and Channel_User_Balance_by_Network_Report too
			//if(	sourceFileName.contains("Channel_User_KPI_Report") && !startDateTag.equals(""))
			//another requirement to replace 'Channel_User_KPI_Report' to 'Channel_User_KPI'
			//if(	( sourceFileName.contains("GR_Unpaid_Bill_Report") || sourceFileName.contains("Channel_User_KPI_Report") ) && !timeSlot.equals("") && !startDateTag.equals(""))
			//logger.info("** massiveFlag:"+massiveFlag+":timeSlot:"+timeSlot+"startDateTag:"+startDateTag);

			//requirement: 22-feb-2016: Location and Report_Name will definitely arrive from results of SQL execution
			//if(	( sourceFileName.contains("GR_Unpaid_Bill_Report") || sourceFileName.contains("Channel_User_KPI") ) && !timeSlot.equals("") && !startDateTag.equals(""))
			if(	massiveFlag && !timeSlot.equals("") && !startDateTag.equals(""))
			{

				int targetFileLastDot = targetFileName.lastIndexOf('.');
				
				String targetFilePartBeforeDot = targetFileName.substring(0,targetFileLastDot);
				String targetFilePartAfterDot = targetFileName.substring(targetFileLastDot);
				
				targetFileName = targetFilePartBeforeDot + "-"+startDateTag + targetFilePartAfterDot;
	
				//another requirement to add a sub-folder corresponding to <StartDate>, after timeslot directory
				/*    */
				int indexOfLastbackslash = targetFileName.lastIndexOf("\\");
				String sourceFileNameDirStr = "";
				String sourceFileNameFileStr = "";
				
				sourceFileNameDirStr = targetFileName.substring(0,indexOfLastbackslash+1);
				sourceFileNameFileStr = targetFileName.substring(indexOfLastbackslash+1);	
								
				//8-sep-15: for only for the channel user synthesis reports generation, remove the subfolder representing startDate in the location
				//targetFileName = sourceFileNameDirStr + startDateTag + "\\" + sourceFileNameFileStr;
				//logger.info("** sourceFileName:"+sourceFileName+"sourceFileNameDirStr:"+sourceFileNameDirStr);

				if(! (sourceFileName.contains("Channel_User_KPI") && sourceFileNameDirStr.contains("_Synthesis\\")) )
				{
					targetFileName = sourceFileNameDirStr + startDateTag + "\\" + sourceFileNameFileStr;
				}
				/*    */
			}
			
			// to print reportdir and reportfile seperately in log messages
			pdf = null;
			
			//use Java7 api, in order to hide and unhide the files
			//this is required by francis to initiate 
			//creation of a file in hidden mode and unhide only if file creation is successful.
			//pdf = new File(targetPath + targetFileName);
			
	
			//try		{			
				//logger.info("** targetPath:"+targetPath+"targetFileName:"+targetFileName);
			
			
				//for head-of-networks , targetFileName already has "C:\R\Reports" pattern
				//Path tempPath = Paths.get(targetPath + targetFileName);
			
			/**
			// go back to old java api for File processing, as new api may be responsible for java.nio.file.FileAlreadyExistsException
			**/
				//Path tempPath = null;
				String tempPath = "";
				if(targetFileName.contains(":"))
				{
					//tempPath = Paths.get(targetFileName);
					tempPath = targetFileName;
					
				}
				else
				{
					//tempPath = Paths.get(targetPath + targetFileName);
					tempPath = targetPath + targetFileName;
				}
				
				//logger.info("** 5 **");
	
				// go back to old java api for File processing, as new api may be responsible for java.nio.file.FileAlreadyExistsException
				//boolean fileDeleted = Files.deleteIfExists(tempPath);
				File tempPathFile = new File(tempPath);
				
				//requirement: 25-sep: repare functionality
				//Repare=IfMissing/IfEmpty/All (default = All)
				fileMissingFlag = !tempPathFile.exists();
				fileEmptyFlag = (tempPathFile.length() == 0) ? true: false;
				regenerateFlag = repareParam.equals("") || repareParam.equals("All") || ( repareParam.equals("IfMissing")  && fileMissingFlag) || ( repareParam.equals("IfEmpty")  && (fileMissingFlag || fileEmptyFlag ));
				pdf = tempPathFile;

				//trying to remove 'FileNotFoundException' by avoiding to delete the file
				/*
				if(tempPathFile.exists())
				{
					tempPathFile.delete();
				}
				*/
				
				//check to eliminate FileAlreadyExistsException issue
				//logger.info("Is file deletion successful :"+fileDeleted);
	
				/* **********/
				//before creating output file, create the directories first, in order to avoid java.nio.file.NoSuchFileException
				// go back to old java api for File processing, as new api may be responsible for java.nio.file.FileAlreadyExistsException
				//String tempPathStr = tempPath.toString();
				
				//requirement: 25-sep: repare functionality
				//Repare=IfMissing/IfEmpty/All (default = All)
				if(regenerateFlag)
				{
						String tempPathStr = tempPath;
						
						int indexOfbackslash = tempPathStr.lastIndexOf("\\");	//LAST backslash
						//int indexEndOfLOCATION =  commandParams.indexOf(";",indexOfLOCATION);
						
					 	//requirement to print uniform error messages on 20-jul-2015
						//String tempDirStr = "";
						//String tempFileStr = "";
						
						tempDirStr = tempPathStr.substring(0,indexOfbackslash+1);
						tempFileStr = tempPathStr.substring(indexOfbackslash+1);	//although tempFileStr is Not going to be used
						
						// go back to old java api for File processing, as new api may be responsible for java.nio.file.FileAlreadyExistsException
						//Path tempDir = Paths.get(tempDirStr);
						//Path tempFile = Paths.get(tempFileStr);
						//Files.createDirectories(tempDir);
						
						String tempDir = tempDirStr;
						String tempFile = tempFileStr;
						File tempDirFile = new File(tempDir);
						tempDirFile.mkdirs();
						boolean isFileCreated = tempPathFile.createNewFile();
				}	//end of first 'if' block:     if(regenerateFlag)

						/* **********/
						
						//check to eliminate FileAlreadyExistsException issue
						/*
						if(!Files.notExists(tempPath))
						{
							logger.info("File "+tempPath.toString()+" exists before creation");
						}
						else
						{
							logger.info("File "+tempPath.toString()+" does Not exist");
						}
						*/
						
						// go back to old java api for File processing, as new api may be responsible for java.nio.file.FileAlreadyExistsException
			
					/*	if(!Files.notExists(tempPath))
						{
							//logger.info("File "+tempPath.toString()+" exists before creation");
							p = tempPath;
						}
						else
						{
							//logger.info("File "+tempPath.toString()+" does Not exist");
							p = Files.createFile(tempPath);
						}
					*/
						
						
						//hide logging 1
						//logger.info("** File did exist initially and was created successfully? "+isFileCreated+" **");
			
						
						//taken to 'if' block above in conditional file-creation
						//p = Files.createFile(tempPath);
						
						
						//logger.info("** 7 **");
						// go back to old java api for File processing, as new api may be responsible for java.nio.file.FileAlreadyExistsException
			
						//p = Files.setAttribute(p, "dos:hidden",true);
						//logger.info("** 8 **");
			
						// go back to old java api for File processing, as new api may be responsible for java.nio.file.FileAlreadyExistsException
						//pdf = p.toFile();
						
						//hide logging 2
						//logger.info("** tempPathFile file exists ? "+ tempPathFile.exists());
						//boolean tempPathFileExists = tempPathFile.exists();
						
						//pdf = tempPathFile;
						//boolean pdfExists = pdf.exists();
				
				//hide logging 3
				//logger.info("** pdf file exists ? "+ pdf.exists());
	
				//logger.info("** 9 **");
				
		}//end of 'if-else' for batch-file '.cmd' execute
			
	} catch (ReportException e1) {
		// TODO Auto-generated catch block
		///*	requirement by francis to change error case log messages	
		//logger.error("ReportException: ", e1);
		
	 	//requirement to print uniform error messages on 20-jul-2015
		//logger.error(countToPrint + ";" + generateTypeGlobal + ";FAILURE;" + e1.getErrorCode() + ";"+ targetFileName + ";ExecTime " + (System.currentTimeMillis() - valInit) / 1000 + "(s)" );					
		
		//requirement: 10/09/2015: Bad number format for elapsed time 
		//logger.error(countToPrint + ";" + generateTypeGlobal + ";FAILURE;" + e1.getErrorCode() + ";"+ tempDirStr + ";" + tempFileStr + ";" + startDateLogging + ";" + endDateLogging+  ";ExecTime " + (System.currentTimeMillis() - valInit) / 1000 + "(s);"+e1.toString() );					
		logger.error(countToPrint + ";" + generateTypeGlobal + ";FAILURE;" + e1.getErrorCode() + ";"+ tempDirStr + ";" + tempFileStr + ";" + startDateLogging + ";" + endDateLogging+  ";ExecTime " + df.format((System.currentTimeMillis() - valInit) / 1000) + "(s);"+e1 );					

		
		
		//it should also be good to add the error description at the end of the line 
		//logger.error("ReportException:", e1);

		//requirement by francis to produce correct number of failed reports 
		throw e1;
	}		
	catch (Exception e1) {
		//logger.error(count + ";" + generateTypeGlobal  + ";FAILURE;-1;"+ pdf.getParent() + ";" + pdf.getName() + ";" + startDateLogging + ";" + endDateLogging+ ";ExecTime " + (System.currentTimeMillis() - valInit) / 1000 + "(s)" );
		
	 	//requirement to print uniform error messages on 20-jul-2015
		//failure preparing the execution (401)
		//logger.error(countToPrint + ";" + generateTypeGlobal + ";FAILURE;-1;" + targetFileName + ";ExecTime " + (System.currentTimeMillis() - valInit) / 1000 + "(s)" );					
		
		//requirement: 10/09/2015: Bad number format for elapsed time 
		//logger.error(countToPrint + ";" + generateTypeGlobal + ";FAILURE;401;" +  tempDirStr + ";" + tempFileStr + ";" + startDateLogging + ";" + endDateLogging+";ExecTime " + (System.currentTimeMillis() - valInit) / 1000 + "(s);"+e1.toString() );					
		logger.error(countToPrint + ";" + generateTypeGlobal + ";FAILURE;401;" +  tempDirStr + ";" + tempFileStr + ";" + startDateLogging + ";" + endDateLogging+";ExecTime " + df.format((System.currentTimeMillis() - valInit) / 1000) + "(s);"+e1 );					
		
		//it should also be good to add the error description at the end of the line 
		//logger.error("Exception:", e1);
		throw e1;
	}
		
	
	// now the 2nd try block with additional info about pdf, startdatelogging, enddatelogging etc.
	try 
	{
		// to print reportdir and reportfile seperately in log messages, moved from up
		//logger.info(count + ";"+generateTypeGlobal +";RUNNING;000;"+ sourceFileName + ";" + pdf.getParent() + ";" + pdf.getName()+ paraLog);
		//to print value of 'pdf.getParent()' correctly in case of batch execution like '.cmd'
		//logger.info(countToPrint + ";"+generateTypeGlobal +";RUNNING;000;"+ sourceFileName + ";" + pdf.getParent() + ";" + pdf.getName()+ paraLog);
		
		//requirement by francis on 20-jul-15: print RUNNING log statement much earlier
		//logger.info(countToPrint + ";"+generateTypeGlobal +";RUNNING;000;"+ sourceFileName + ";" + ((pdf.getParent() == null) ? "": pdf.getParent()) + ";" + pdf.getName()+ paraLog);

		if(!batchFileToExecute.equals(""))	//batch file '.cmd' to be executed
		{
			targetFileName = "";	// to correct the log messages
			//requirement: 18-apr-16: CMD launched by GR-Launcher is not killed after exacution
			//Runtime.getRuntime().exec("cmd /c start "+batchFileToExecute);
			
			//requirement: 26-may-2016: wait the end of the execution of the CMD launched before processing to the next tasks
			//Runtime.getRuntime().exec("cmd /c "+batchFileToExecute);
			Runtime.getRuntime().exec("cmd /c "+batchFileToExecute).waitFor();

		}
		else
		{
			
			//requirement: 25-sep: repare functionality
			//Repare=IfMissing/IfEmpty/All (default = All)
			if(regenerateFlag)
			{
					int k = targetFileName.lastIndexOf("\\");
					String subPath = targetFileName.substring(0, k);
					String tagetPathCreated = targetPath + subPath;
					new File(tagetPathCreated).mkdirs();
			
					//try {
						engine.execute();
					/*	
					} catch (ReportException e1) {
						//logger.error("ReportException: ", e1);
						//requirement by francis to change error case log messages			
						//logger.error(count + ";" + generateTypeGlobal  + ";FAILURE;" + e1.getErrorCode() + ";"+ targetFileName + ";" + startDateTag + ";" + endDateTag+ "; ExecTime " + (System.currentTimeMillis() - valInit) / 1000 + "(s)" );					
						logger.error(count + ";" + generateTypeGlobal  + ";FAILURE;" + e1.getErrorCode() + ";"+ pdf.getParent() + ";" + pdf.getName() + ";" + startDateLogging + ";" + endDateLogging+ ";ExecTime " + (System.currentTimeMillis() - valInit) / 1000 + "(s)" );					
						logger.error("ReportException:", e1);
			
						//requirement by francis to produce correct number of failed reports 
						throw e1;
					}
					*/
					// to print reportdir and reportfile seperately in log messages, take it little up
					//File pdf = null;
					//pdf = new File(targetPath + targetFileName);
					
			
					FileOutputStream fos = null;
					//try {
					//change to use different constructor with second argument as 'append or not'
						fos = new FileOutputStream(pdf,false);
					/*	
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						//logger.error("FileNotFoundException", e);
						
							requirement by francis to change error case log messages			
						logger.error(count + ";" + generateTypeGlobal  + ";FAILURE;-1;"+ pdf.getParent() + ";" + pdf.getName() + ";" + startDateLogging + ";" + endDateLogging + ";ExecTime " + (System.currentTimeMillis() - valInit) / 1000 + "(s)" );					
						logger.error("FileNotFoundException:", e);
			
						//requirement by francis to produce correct number of failed reports 
						throw e;
					}
					*/
					//try {
						for (int i = 1; i <= engine.getPageCount(); i++) {
							fos.write(engine.getPageData(i));
						}
					/*
					} catch (ReportException e) {
						// TODO Auto-generated catch block
						//requirement 6 by francis
						logger.error(count + ";" +generateTypeGlobal + ";FAILURE;" + 
						+ e.getErrorCode() + ";"+pdf.getParent() + ";" + pdf.getName()	+ ";" + startDateLogging + ";" + endDateLogging+ ";ExecTime " + (System.currentTimeMillis() - valInit) / 1000 + "(s)");
						logger.error("ReportException:", e);
						//requirement by francis to produce correct number of failed reports 
						throw e;		
					} catch (IOException e) {
						// TODO Auto-generated catch block
						//requirement 6 by francis
			
						logger.error(count + ";" +generateTypeGlobal + ";FAILURE;-1;"  
						+ pdf.getParent() + ";" + pdf.getName()	+ ";" + startDateLogging + ";" + endDateLogging+ ";ExecTime " + (System.currentTimeMillis() - valInit) / 1000 + "(s)");
						logger.error("IOException:", e);
						//requirement by francis to produce correct number of failed reports 
						throw e;
					}
					 */
						
					//try {
							fos.close();
			}	//end of second 'if'  : if(regenerateFlag)

			
			/*	
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//logger.error("IOException", e);
				//	requirement by francis to change error case log messages			
				logger.error(count + ";" + generateTypeGlobal+ ";FAILURE;-1;"+ pdf.getParent() + ";" + pdf.getName()  + ";" + startDateLogging + ";" + endDateLogging+ ";ExecTime " + (System.currentTimeMillis() - valInit) / 1000 + "(s)" );					
				logger.error("IOException:", e);
	
				//requirement by francis to produce correct number of failed reports 
				throw e;		
			}
			*/

	
	
			/**
			 * remove the line separator from the txt report
			 * and rename the .txt file to .csv format
			 */
			if (exportFormat.equalsIgnoreCase("txt2csv")){
				//requirement by francis to alter the SUCCESS message to have correct name of file i.e. .csv, for case of txt2csv
				//removeLineSeparator(targetPath + targetFileName);
				pdf = new File(removeLineSeparator(targetPath + targetFileName));
			}
			

	
			//countSuccess++;	//incremented only when 1 report is successful
			
	
			//use Java7 api, in order to hide and unhide the files
			//this is required by francis to initiate 
			//creation of a file in hidden mode and unhide only if file creation is successful.
			//try {
			
			// go back to old java api for File processing, as new api may be responsible for java.nio.file.FileAlreadyExistsException
				/*
				p=pdf.toPath();
				if(Files.size(p) > 0)
				{
					Files.setAttribute(p, "dos:hidden",false);
				}	
				*/
			
			/*	
			} catch (IOException e) {
				logger.error(count + ";" + generateTypeGlobal+ ";FAILURE;-1;"+ pdf.getParent() + ";" + pdf.getName()  + ";" + startDateLogging + ";" + endDateLogging+ ";ExecTime " + (System.currentTimeMillis() - valInit) / 1000 + "(s)" );					
				logger.error("IOException:", e);
	
				//requirement by francis to produce correct number of failed reports 
				throw e;		
			}
			*/
	
			//requirement 6 by francis
			//logger.info(count + "; " +generateTypeGlobal+ "; " + targetFileName	+ "; ExecTime " + tempsExe / 1000 + "(s)");
			/*logger.info(count + "; " +generateTypeGlobal+ "; " + startDateTag + ";" + endDateTag + ";SUCCESS;200;" + 
			targetFileName	+ "; ExecTime " + tempsExe / 1000 + "(s)");
			*/
			
			//changes should be made to align status as 3rd field
			//logger.info(count + ";" +generateTypeGlobal + ";SUCCESS;200;" + 
			//pdf.getParent() + ";" + pdf.getName()	+ ";" + startDateLogging + ";" + endDateLogging+ ";ExecTime " + tempsExe / 1000 + "(s)");
			
			//send email only if command-line parameter 'SendEmail' is specified and Not the 'QueryExec' parameter
			
			//latest requirement to send email even for massive reports
			//if(ChaineAdeCode.contains("SendEmail=Y") && massiveFlag == false)
			
			// put condition to ignore ambiguousTransferFlag if queryCheckFlag is false
			//if(ChaineAdeCode.contains("SendEmail=Y") && ambiguousTransferFlag )
			
			if (engine.isFinish()) {
				engine = null;
				System.gc();
			}
			
			//warning if the report is generated but other error occurred with no impact on the report content : 300 (example with error on FTP setting or access)
			try
			{
				//requirement: 18-may-2016: bug (error code 300) in globalsendemail switch when this is absent
				//if( (ChaineAdeCode.contains("SendEmail=Y") && !queryCheckFlag) || (ChaineAdeCode.contains("SendEmail=Y") && queryCheckFlag && ambiguousTransferFlag ) )

				/* 
				//repeat the ambiguousTransferFlag(QueryCheck) according to WaitLoop configuration.
				// fn. computeAmbiguousTransferFlag() was already called before, at line 2083.
				
				if(initialWaitSec > 0)
				{
					
					if(loopTimes > 0)	//'loopTimes' option is active, and Not 'waitUntil'
					{
						int currentLoopNum = 1;
						while(globalEmailFlag && ChaineAdeCode.contains("SendEmail=Y") && queryCheckFlag && !ambiguousTransferFlag )	
						{
							Thread.sleep(initialWaitSec*1000);
							if(currentLoopNum == loopTimes)
							{
								break;
							}
							computeAmbiguousTransferFlag();
							
							currentLoopNum++;
						}

					}	
					else	//'waitUntil' option is active, and Not 'loopTimes'
					{
						Calendar currentCal;
						Calendar waitUntilCal;
						waitUntilCal = Calendar.getInstance();
						waitUntilCal.set(Calendar.HOUR_OF_DAY, waitUntilHour);
						waitUntilCal.set(Calendar.MINUTE, waitUntilMin);
						waitUntilCal.set(Calendar.SECOND, waitUntilSec);

						while(globalEmailFlag && ChaineAdeCode.contains("SendEmail=Y") && queryCheckFlag && !ambiguousTransferFlag)	
						{
							Thread.sleep(initialWaitSec*1000);
							currentCal = Calendar.getInstance();	
							
							if(currentCal.after(waitUntilCal))	//waitUntil time elapsed, and semaphoreFile still Not found
							{
								break;
							}
							computeAmbiguousTransferFlag();								
						}
					}
				}	//end of 'if(initialWaitSec > 0)'
											
				if(globalEmailFlag && ChaineAdeCode.contains("SendEmail=Y") && queryCheckFlag && !ambiguousTransferFlag )
				{	
					throw new Exception("QueryCheck failed");
				}	
			}
			catch(Exception e)
			{
				logger.error(countToPrint + ";" + generateTypeGlobal + ";FAILURE;405;"+((pdf.getParent() == null) ? "": pdf.getParent()) + ";" + pdf.getName() + ";" + startDateLogging + ";" + endDateLogging + ";ExecTime " + df.format((System.currentTimeMillis() - valInit) / 1000) + "(s);"+e  );
			}
			*/	
			//try
			//{
				if( globalEmailFlag && ((ChaineAdeCode.contains("SendEmail=Y") && !queryCheckFlag) || (ChaineAdeCode.contains("SendEmail=Y") && queryCheckFlag && ambiguousTransferFlag )) )
				{
					//logger.info(" * Before calling SendEmail function ");
					//send the email and do FTP too
					sendEmail(pdf);
					//logger.info(" * After calling SendEmail function ");

					//ftp(pdf);
				}
				
				
				//if( ChaineAdeCode.contains("SendFTP=Y")  )
				if( globalFtpFlag && ChaineAdeCode.contains("SendFTP=Y")  )
				{
					ftp(pdf);
				}
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				//logger.error("FileNotFoundException", e);
				
				//logger.error(count + ";" + generateTypeGlobal  + ";FAILURE;-1;"+ pdf.getParent() + ";" + pdf.getName() + ";" + startDateLogging + ";" + endDateLogging + ";ExecTime " + (System.currentTimeMillis() - valInit) / 1000 + "(s)" );					

				//requirement: 10/09/2015: Bad number format for elapsed time 
				//logger.error(countToPrint + ";" + generateTypeGlobal  + ";FAILURE;300;"+ ((pdf.getParent() == null) ? "": pdf.getParent()) + ";" + pdf.getName() + ";" + startDateLogging + ";" + endDateLogging + ";ExecTime " + (System.currentTimeMillis() - valInit) / 1000 + "(s);"+e.toString()  );					
				logger.error(countToPrint + ";" + generateTypeGlobal  + ";FAILURE;300;"+ ((pdf.getParent() == null) ? "": pdf.getParent()) + ";" + pdf.getName() + ";" + startDateLogging + ";" + endDateLogging + ";ExecTime " + df.format((System.currentTimeMillis() - valInit) / 1000) + "(s);"+e  );					
				
				//it should also be good to add the error description at the end of the line 
				//logger.error("Exception:", e);
			}
			
		}	// end of 'else' of batch execution for '.cmd' which starts at line 559
		
		valFinal = System.currentTimeMillis();
		tempsExe = (valFinal - valInit);
		
		//requirement: 10/09/2015: Bad number format for elapsed time 
		//logger.info(countToPrint + ";" +generateTypeGlobal + ";SUCCESS;200;" + 
			//	((pdf.getParent() == null) ? "": pdf.getParent()) + ";" + pdf.getName()	+ ";" + startDateLogging + ";" + endDateLogging+ ";ExecTime " + tempsExe / 1000 + "(s)");
		
		//Change for Forge#374803
		String status=null;
		
		if (!regenerateFlag) {status=";EXISTING;301;";countExisting++;}
		else
		{
			if ((repareParam.equals("IfEmpty") && fileMissingFlag))  status =";SUCCESS;202;";
			else if (repareParam.equals("IfEmpty") && fileEmptyFlag) status=";SUCCESS;201;" ;
			else if ((repareParam.equals("IfMissing"))) status= ";SUCCESS;202;";
			else status= ";SUCCESS;200;";
		}	
		
//		logger.info(countToPrint + ";" +generateTypeGlobal + ";SUCCESS;200;" + 
		logger.info(countToPrint + ";" +generateTypeGlobal + status +
			((pdf.getParent() == null) ? "": pdf.getParent()) + ";" + pdf.getName()	+ ";" + startDateLogging + ";" + endDateLogging+ ";ExecTime " + df.format(tempsExe / 1000) + "(s)");
		
		
				    timeAllExec=timeAllExec+(tempsExe / 1000);
				    
				    timeAllMassiveOfOneType=timeAllMassiveOfOneType+(tempsExe / 1000);
				    


		} catch (ReportException e1) {
			//logger.error("ReportException: ", e1);
			//requirement by francis to change error case log messages			
			//logger.error(count + ";" + generateTypeGlobal  + ";FAILURE;" + e1.getErrorCode() + ";"+ pdf.getParent() + ";" + pdf.getName() + ";" + startDateLogging + ";" + endDateLogging+ ";ExecTime " + (System.currentTimeMillis() - valInit) / 1000 + "(s)" );					
			
			//requirement: 10/09/2015: Bad number format for elapsed time 
			//logger.error(countToPrint + ";" + generateTypeGlobal  + ";FAILURE;" + e1.getErrorCode() + ";"+ ((pdf.getParent() == null) ? "": pdf.getParent()) + ";" + pdf.getName() + ";" + startDateLogging + ";" + endDateLogging+ ";ExecTime " + (System.currentTimeMillis() - valInit) / 1000 + "(s);"+e1.toString() );					
			logger.error(countToPrint + ";" + generateTypeGlobal  + ";FAILURE;" + e1.getErrorCode() + ";"+ ((pdf.getParent() == null) ? "": pdf.getParent()) + ";" + pdf.getName() + ";" + startDateLogging + ";" + endDateLogging+ ";ExecTime " + df.format((System.currentTimeMillis() - valInit) / 1000) + "(s);"+e1 );					

			//it should also be good to add the error description at the end of the line 
			//logger.error("ReportException:", e1);
	
			//requirement by francis to produce correct number of failed reports 
			throw e1;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//logger.error("FileNotFoundException", e);
			
			//logger.error(count + ";" + generateTypeGlobal  + ";FAILURE;-1;"+ pdf.getParent() + ";" + pdf.getName() + ";" + startDateLogging + ";" + endDateLogging + ";ExecTime " + (System.currentTimeMillis() - valInit) / 1000 + "(s)" );					
			
			//requirement: 10/09/2015: Bad number format for elapsed time 
			//logger.error(countToPrint + ";" + generateTypeGlobal  + ";FAILURE;400;"+ ((pdf.getParent() == null) ? "": pdf.getParent()) + ";" + pdf.getName() + ";" + startDateLogging + ";" + endDateLogging + ";ExecTime " + (System.currentTimeMillis() - valInit) / 1000 + "(s);"+e.toString()  );					
			logger.error(countToPrint + ";" + generateTypeGlobal  + ";FAILURE;400;"+ ((pdf.getParent() == null) ? "": pdf.getParent()) + ";" + pdf.getName() + ";" + startDateLogging + ";" + endDateLogging + ";ExecTime " + df.format((System.currentTimeMillis() - valInit) / 1000) + "(s);"+e  );					

			//it should also be good to add the error description at the end of the line 
			//logger.error("Exception:", e);
	
			//requirement by francis to produce correct number of failed reports 
			throw e;
		}
	}
	
	private String curDateTag()
	{
		DateFormat dateFormat;
		Calendar cal;
		LuncherInfo luncherInfo = new LuncherInfo();
		String foramtDate = luncherInfo.getCurdateFormat();
		dateFormat = new SimpleDateFormat(foramtDate);
		cal = Calendar.getInstance();
		String dateString = dateFormat.format(cal.getTime());
		return dateString;
	}

	private String curDateTimeTag()
	{
		DateFormat dateFormat;
		Calendar cal;
		LuncherInfo luncherInfo = new LuncherInfo();
		String foramtDate = luncherInfo.getCurdatetimeFormat();
		dateFormat = new SimpleDateFormat(foramtDate);
		cal = Calendar.getInstance();
		String dateString = dateFormat.format(cal.getTime());
		return dateString;
	}
	
	private String prevDayTag()
	{
		DateFormat dateFormat;
		Calendar cal;
		LuncherInfo luncherInfo = new LuncherInfo();
		String foramtDate = luncherInfo.getPrevdayFormat();
		dateFormat = new SimpleDateFormat(foramtDate);
		cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		String dateString = dateFormat.format(cal.getTime());
		return dateString;
	}	
	
	private String prevWeekTag()
	{
		DateFormat dateFormat;
		Calendar cal;
		LuncherInfo luncherInfo = new LuncherInfo();
		String foramtDate = luncherInfo.getPrevweekFormat();
		dateFormat = new SimpleDateFormat(foramtDate);
		cal = Calendar.getInstance();
		cal.add(Calendar.WEEK_OF_YEAR, -1);
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		String dateString = dateFormat.format(cal.getTime());
		return dateString;
	}	
	
	
	private String prevMonthTag()
	{
		DateFormat dateFormat;
		Calendar cal;
		LuncherInfo luncherInfo = new LuncherInfo();
		String foramtDate = luncherInfo.getPrevmonthFormat();
		dateFormat = new SimpleDateFormat(foramtDate);
		cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		String dateString = dateFormat.format(cal.getTime());
		return dateString;
	}	
	
	private String prevWeekNumberTag()
	{
		DateFormat dateFormat;
		Calendar cal;
		LuncherInfo luncherInfo = new LuncherInfo();
		String foramtDate = luncherInfo.getPrevweeknumberFormat();
		dateFormat = new SimpleDateFormat(foramtDate);
		cal = Calendar.getInstance();
		cal.add(Calendar.WEEK_OF_YEAR, -1);
		
		String dateString = dateFormat.format(cal.getTime());
		return dateString;
	}	
	
	private String prevMonthNumberTag()
	{
		DateFormat dateFormat;
		Calendar cal;
		LuncherInfo luncherInfo = new LuncherInfo();
		String foramtDate = luncherInfo.getPrevmonthnumberFormat();
		dateFormat = new SimpleDateFormat(foramtDate);
		cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		
		String dateString = dateFormat.format(cal.getTime());
		return dateString;
	}
	
	private String prevMonthNameTag()
	{
		DateFormat dateFormat;
		Calendar cal;
		LuncherInfo luncherInfo = new LuncherInfo();
		String foramtDate = luncherInfo.getPrevmonthnameFormat();
		dateFormat = new SimpleDateFormat(foramtDate);
		cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		
		String dateString = dateFormat.format(cal.getTime());
		return dateString;
	}
	
	

	
	public void generateReportAll(String generateType, String DateFormat) {
		
        //PropertyConfigurator.configure(GenerateReportUserAll.class.getClassLoader().getResource("log4jLauncher.properties"));	//added for Forge#374803, to point to local log4j.properties file 
		generateTypeGlobal=generateType;
		
		//requirement: 16-nov-2015: create regenerate01_Action like classes to execute command marked with RegenMark=1 
		//for regenrate action classes, the value of generateType is like 'Regenerate_1'.
		
		//requirement 7 by francis
		//int count = 0;
		int countLocal = 0;

		//requirement: 10/09/2015: Bad number format for elapsed time 
	    sym.setDecimalSeparator('.');
	    df.setDecimalFormatSymbols(sym);
	    
		List<String> keySet = new ArrayList<String> ();;
		
		/* requirement by francis to provide more details in start execution */
		//logger.info("**** Start execution of task  '" + generateType
		//		+ "'  ****");
		
		luncherInfo = new LuncherInfo();

		//requirement by mayank: 30-sep-2015: process commands in reports.properties, in the order they appear in file
		//Properties prop = luncherInfo.getPropeties();
		Properties prop = luncherInfo.getPropetiesNatural();

		// for debugging only
		/*Set s = prop.stringPropertyNames();
		Object[] objit = s.toArray();
		for(int i=0;i<objit.length;i++)
		{
			logger.info("++"+objit[i]);
		}*/
		
		//requirement by mayank: 30-sep-2015: process commands in reports.properties, in the order they appear in file
        Enumeration keyNames = prop.keys();
        
		//requirement: 16-nov-2015: create regenerate01_Action like classes to execute command marked with RegenMark=1 
/*
        while (keyNames.hasMoreElements()){
            String key = (String)keyNames.nextElement();
            //logger.info("++"+key);
			if (!key.contains(generateType+"_")) {	//to remove error of incorrect nb of reports to run
				continue;
			}
			//add the keys from the properties
			keySet.add(key);
			countLocal = countLocal + 1;            
        }
*/      
        //for normal reports other than Regenerate_1/2/3/4
        	if(generateType.contains("Run_"))
        	{
		        while (keyNames.hasMoreElements()){
		            String key = (String)keyNames.nextElement();
		            //logger.info("++"+key);
					if (!key.contains(generateType+"_")) {	//to remove error of incorrect nb of reports to run
						continue;
					}
					//add the keys from the properties
					keySet.add(key);
					countLocal = countLocal + 1;            
		        }
        	}
        	//for Regenerate_1/2/3/4 reports i.e. commands containing pattern RegenMark=1/2/3/4
        	else if(generateType.contains("Regenerate_"))
        	{
	            //pattern to be searched
	            String patternStr = "";
	            Pattern p = null;
	            Matcher m = null;
	            boolean b = false;
          
				int RegenOption = Integer.parseInt(generateType.substring(11));
				
	            switch (RegenOption)
	            {
	            	case 1 :	patternStr = ".*RegenMark=[1234,&&[^;]]*1[,$;]*.*"; break;
	            	case 2 :	patternStr = ".*RegenMark=[1234,&&[^;]]*2[,$;]*.*"; break;
	            	case 3 :	patternStr = ".*RegenMark=[1234,&&[^;]]*3[,$;]*.*"; break;
	            	case 4 :	patternStr = ".*RegenMark=[1234,&&[^;]]*4[,$;]*.*"; break;
	            }
	            p = Pattern.compile(patternStr);
	            
		        while (keyNames.hasMoreElements()){
		            String key = (String)keyNames.nextElement();
		            String val = (String)prop.getProperty(key);
		            
		            m = p.matcher(val);
		            b = m.matches();
		            
		            //logger.info("++"+key);
					if (!b) {	//to remove error of incorrect nb of reports to run
						continue;
					}
					//add the keys from the properties
					keySet.add(key);
					countLocal = countLocal + 1;            
		        }
        	} //end of 'else if(generateType.contains("Regenerate_"))'
        
		//requirement by mayank: 30-sep-2015: process commands in reports.properties, in the order they appear in file
		/*
        Iterator<Object> it = prop.keySet().iterator();
		
		while (it.hasNext()) {

			String key = (String) it.next();
			if (!key.contains(generateType+"_")) {	//to remove error of incorrect nb of reports to run
				continue;
			}
			
			//add the keys from the properties
			keySet.add(key);
			countLocal = countLocal + 1;
		}
		*/
        
		/* requirement by francis to provide more details in start execution message*/
		logger.info("**** Start execution of task '" + generateType+ "';Nb of reports scheduled to run:"+countLocal+" ****");
		
		
		/**
		 * @date 15/09/2014
		 * @author NMCC5676
		 * @brief reports will be generated in the sorted order
		 */

		List<Integer> numportionSet = new ArrayList<Integer> ();
		List<String> resultats;
		//List<String> resultats;

		// requirement to execute reports NOT in ascending order of key number, but in order they appear in properties file
		/*
		for(String intVal:keySet){
			Integer numberportion = Integer.valueOf(intVal.substring( intVal.lastIndexOf("_")+1));
			//logger.info("****  generatereportuserall, BEFORE sorting: numberportion is " + intVal.substring( intVal.lastIndexOf("_")+1));

			numportionSet.add(numberportion);
		}
		
	//	logger.info("*** A ");
		Collections.sort(numportionSet);
		
		for(Integer numportion:numportionSet){
		*/	
		for(String keyportion:keySet){
			Integer numportion = Integer.valueOf(keyportion.substring( keyportion.lastIndexOf("_")+1));
			
			//requirement to print uniform error messages like comment below on 20-jul-2015
			//logger.error(countToPrint + ";" + generateTypeGlobal  + ";FAILURE;" + e1.getErrorCode() + ";"+ ((pdf.getParent() == null) ? "": pdf.getParent()) + ";" + pdf.getName() + ";" + startDateLogging + ";" + endDateLogging+ ";ExecTime " + (System.currentTimeMillis() - valInit) / 1000 + "(s)" );
			valInit = System.currentTimeMillis();

			count++;
			ambiguousTransferFlag = false;
			// put condition to ignore ambiguousTransferFlag if queryCheckFlag is false
			queryCheckFlag = false;
			
			//requirement: 358627: Add a wait time option in GR-Launcher plugin
			fileCheckFlag = false;
			
			
			//requirement: 16-nov-2015: create regenerate01_Action like classes to execute command marked with RegenMark=1 
			//String reportNumBackUp = generateType+"_"+numportion.toString();	//needed in order to print (continued ..)
			
			String reportNumBackUp = "";
			if(generateType.contains("Run_"))
			{
				reportNumBackUp = generateType+"_"+numportion.toString();	//needed in order to print (continued ..)
			}
			else if(generateType.contains("Regenerate_"))
			{
				reportNumBackUp = keyportion;
			}
			
			// second 'start of execution of command ..' logger-info for massive reports	 e.g. 'Run_7_1' etc.
			
			String value = prop.getProperty(reportNumBackUp);
			String valueBackUp = value;	//to be used for exceptions initially raised, when even the targetfilename is not known
			
			//logger.info("****  generatereportuserall, AFTER sorting: numportion is " + numportion.toString());
			try
			{
				/*
				//requirement to execute .cmd batch files
				int indexOfFirstSemiCol = value.indexOf(";");
				String rptPath = "";
				if(indexOfFirstSemiCol == -1)	//char ';' does not exist in the command line
				{
					rptPath = value;
				}
				else
				{
					rptPath = value.substring(0, indexOfFirstSemiCol);
				}					
				*/
				
				//functionality for massive report for head-of-network / billers
				//generateReport(value, DateFormat);
				int indexSqlPath;
				int indexVeryNextSemiColon;
				//String sqlPath = "";
				//logger.info("*** B, value : "+value);
				
				
				
				//checklag functionality (Lag Value)
				//calculate Lag Value from SQL execution 
				sourcePath = luncherInfo.getSourcepath();
				sourceSqlPath = luncherInfo.getSqlpath();

				/* code change to print 
				 * logger.info(countToPrint + ";"+generateTypeGlobal +";RUNNING;000;"+ sourceFileName + ";" + ((pdf.getParent() == null) ? "": pdf.getParent()) + ";" + pdf.getName()+ paraLog);
				 * 
				 */
				targetPath = luncherInfo.getTagetpath();

				String valueForRunning = value;
				String[] argsRunning = valueForRunning.split(";");
				
				String sourceFileNameRunning = argsRunning[0];	//source file name to print first
				
				String targetFileNameRunning = "";
				String targetDirRunning = "";
				String targetFileAbsoluteRunning = "";
				
				//requirement: 27-dec-16: debug: .cmd execution logs show the parameter of the previous command 
				targetDirStr = ""; targetFileStr = "";
				
				if(!valueForRunning.contains("QueryExec=") && !sourceFileNameRunning.endsWith(".cmd") )
				{
					// NON massive reports
					targetFileNameRunning = argsRunning[1];
					
					targetFileAbsoluteRunning = targetPath + targetFileNameRunning;
					
					
					//replace date tags with resulting values
					targetFileAbsoluteRunning = targetFileAbsoluteRunning.replace("<CurDate>", curDateTag());
					targetFileAbsoluteRunning = targetFileAbsoluteRunning.replace("<CurDateTime>", curDateTimeTag());
					targetFileAbsoluteRunning = targetFileAbsoluteRunning.replace("<PrevDay>", prevDayTag());
					targetFileAbsoluteRunning = targetFileAbsoluteRunning.replace("<PrevWeek>", prevWeekTag());
					targetFileAbsoluteRunning = targetFileAbsoluteRunning.replace("<PrevMonth>", prevMonthTag());
					targetFileAbsoluteRunning = targetFileAbsoluteRunning.replace("<PrevWeekNumber>", prevWeekNumberTag());
					targetFileAbsoluteRunning = targetFileAbsoluteRunning.replace("<PrevMonthNumber>", prevMonthNumberTag());
					targetFileAbsoluteRunning = targetFileAbsoluteRunning.replace("<PrevMonthName>", prevMonthNameTag());
					
					int indexOfLastbackslash = targetFileAbsoluteRunning.lastIndexOf("\\");

					targetDirStr = targetFileAbsoluteRunning.substring(0,indexOfLastbackslash+1);
					targetFileStr = targetFileAbsoluteRunning.substring(indexOfLastbackslash+1);
				}
				

				
				//now in order to print paraLog after source file and targetfile names
				valueForRunning = valueForRunning.replace(sourceFileNameRunning,"");
				valueForRunning = valueForRunning.replace(targetFileNameRunning,"");
				valueForRunning = valueForRunning.replace(";;",";");
				
				logger.info(count + ";"+generateTypeGlobal +";RUNNING;000;"+ sourceFileNameRunning + ";" + targetDirStr + ";" + targetFileStr+ valueForRunning);

				//requirement:16-dec-2015: global parameter in GR-Plugin.properties allowing to totally deactivate the lag check
				String urlFilePropertiesGlobal = ConstanteGR.getUrlfileproperties();
				Properties pGlobal = GRUtils.loadProperties(urlFilePropertiesGlobal);
				
				String globalCheckLag = pGlobal.getProperty("global.CheckLag");
				
				//requirement:16-dec-2015: global parameter in GR-Plugin.properties allowing to totally deactivate the lag check
				//if(!value.contains("CheckLag=0") )	//requirement to skip any lag functionality if "CheckLag=0" parameter specified 

				//requirement: 358627: Add a wait time option in GR-Launcher plugin
				initialWaitSec = 0;
				loopTimes = 0;
				waitUntilHour = 0;
				waitUntilMin = 0;
				waitUntilSec = 0;

				
				if(value.contains("WaitLoop=") )	
				{																							
						try
						{							
								// to know value of WaitLoop parameter 
								int indexEndOfWaitLoop = -1;
								String WaitLoop = "";
								int indexOfWaitLoop = value.indexOf("WaitLoop=");
								String WaitLoopAbsolute = "";
								boolean WaitLoopMisconfigured = false;
								
								if(indexOfWaitLoop != -1)	// pattern 'WaitLoop=' exists in commandParams
								{
									indexEndOfWaitLoop =  value.indexOf(";",indexOfWaitLoop);
									if(indexEndOfWaitLoop == -1)	//char ';' does not exist at end of value of WaitLoop parameter
									{
										WaitLoop = value.substring(indexOfWaitLoop+9);
									}
									else
									{
										WaitLoop = value.substring(indexOfWaitLoop+9, indexEndOfWaitLoop);
									}
									
									//remove 'WaitLoop=300,24' pattern from command
									if(indexEndOfWaitLoop != -1)	// ';' char exists after end of value of WaitLoop parameter
									{
										StringBuilder sbValue = new StringBuilder(value);
										value = sbValue.deleteCharAt(indexEndOfWaitLoop).toString();
									}
									value = value.replace("WaitLoop="+WaitLoop,"");
									
									
									int indexOfFirstComma = -1;
									int indexOfLastComma = -1;
									
									indexOfFirstComma = WaitLoop.indexOf(',');
									indexOfLastComma = WaitLoop.lastIndexOf(',');

									if( (indexOfFirstComma == -1) || (indexOfFirstComma != indexOfLastComma) )
									{
										//either comma Not found in value of WaitLoop OR more than 1 comma
										WaitLoopMisconfigured = true;
									}
									else 
									{
										int indexOfFirstColon = -1;
										int indexOfLastColon = -1;
										initialWaitSec = Integer.parseInt( WaitLoop.substring(0, indexOfFirstComma));
										String waitTimes = WaitLoop.substring(indexOfFirstComma+1);	//waitTimes = portion after comma 
																									//in '300,24' OR '900,09:30:00'
																									
										
										indexOfFirstColon = waitTimes.indexOf(':');
										indexOfLastColon = waitTimes.lastIndexOf(':');
										
										if(indexOfFirstColon == -1 )	//'loop times' is Specified and Not the 'loop until'
										{
											loopTimes = Integer.parseInt(waitTimes);
										}
										else	//'loop until' is Specified and Not the 'loop times'
										{
											waitUntilHour = Integer.parseInt( waitTimes.substring(0, indexOfFirstColon));
											waitUntilMin = Integer.parseInt( waitTimes.substring(indexOfFirstColon+1, indexOfLastColon));
											waitUntilSec = Integer.parseInt( waitTimes.substring(indexOfLastColon+1));
										}
									}
									
									if(WaitLoopMisconfigured )
									{	
										throw new Exception("Mis-Configured WaitLoop value: "+WaitLoop);
									}	
								}	// end of 'if(indexOfWaitLoop != -1)'	
									
						}
						catch(Exception e)
						{
							logger.error(count + ";" + generateType + ";FAILURE;409;"+targetDirStr + ";" + targetFileStr+";"+ startDateLogging + ";" + endDateLogging+";ExecTime " + df.format((System.currentTimeMillis() - valInit) / 1000) + "(s);"+e  );

							throw e;
						}
				}	//end of 'if(value.contains("WaitLoop=") )' i.e. wait time functionality
				
				
				
				//requirement: 18-sep-2016: change skip Lag-check parameter from "CheckLag=0" to "CheckLag=N"
				//if(globalCheckLag != null && globalCheckLag.equals("Y") && !value.contains("CheckLag=0") )	//requirement to skip any lag functionality if "CheckLag=0" parameter specified 
				if(globalCheckLag != null && globalCheckLag.equals("Y") && !value.contains("CheckLag=N") )	//requirement to skip any lag functionality if "CheckLag=N" parameter specified 
				{																							// in command-line
						// as per latest requirement, SQLs will now be kept in GR-Sql folder at level of GR-Properties
						//String checkLagSQLPath =  sourcePath + "sql\\CheckLag.sql";
						
						// make following variables global, to allow making of modularized functions computeActualLagJdbc() and lagSecondsSinceMidnight()
						//String checkLagSQLPath =  sourceSqlPath + "GR-Sql\\CheckLag.sql";
						checkLagSQLPath =  sourceSqlPath + "GR-Sql\\CheckLag.sql";
					
						lagDays = 0;
						lagHour = 0;
						lagMin = 0;
						lagSec = 0;
						
						lagDaysStr = "";
						lagHourStr = "";
						lagMinStr = "";
						lagSecStr = "";
						
						
						actualLagSeconds = 0;
						try
						{
							/*
							 * all this commented code put into function computeActualLagJdbc()
							 * 
							ConnetJdbcDbref jdbcDbref = new ConnetJdbcDbref();
							resultats = jdbcDbref.getNewListUserChannel(checkLagSQLPath);	
							String lagRS = resultats.get(0);	//e.g.	"11/05/2015 06:21:03;+00 00:00:00"
							
							if(lagRS == null || lagRS.equals(""))
							{
								throw new Exception("DBref does Not define LAG value");
							}
							
							
							int indexEndOfLAG = -1;
							String LAG = "";
							int indexOfLAG = lagRS.indexOf("LAG=");
							
							if(indexOfLAG != -1)	// pattern 'LAG=' exists in commandParams
							{
								indexEndOfLAG =  lagRS.indexOf(";",indexOfLAG);
								if(indexEndOfLAG == -1)	//char ';' does not exist at end of value of LOCATION parameter
								{
									LAG = lagRS.substring(indexOfLAG+4);
								}
								else
								{
									LAG = lagRS.substring(indexOfLAG+4, indexEndOfLAG);
								}
							}
							//LAG contains value like '11/05/2015 06:21:03;+00 00:00:00'
							
							int indexOfHash = LAG.indexOf('^');
							String lagPortion = LAG.substring(indexOfHash+1);	//lagportion value is like '+00 00:00:00'
							
		
							
							if(lagPortion !=  null && !lagPortion.equals("") && lagPortion.contains(":"))
							{
								int indexOfPlus = lagPortion.indexOf('+');
								
								
								lagDaysStr =  lagPortion.substring(indexOfPlus+1, indexOfPlus+3);
								lagHourStr = lagPortion.substring(indexOfPlus+4, indexOfPlus+6);
								lagMinStr = lagPortion.substring(indexOfPlus+7, indexOfPlus+9);
								lagSecStr = lagPortion.substring(indexOfPlus+10, indexOfPlus+12);
								
								lagDays = Integer.parseInt(lagDaysStr);
								lagHour = Integer.parseInt(lagHourStr );
								lagMin = Integer.parseInt(lagMinStr );
								lagSec = Integer.parseInt(lagSecStr );
								
								actualLagSeconds=  lagDays*86400 + lagHour*3600 + lagMin*60  + lagSec;
							}
							else
							{
								throw new Exception("DBref does Not define LAG value correctly in required format +DD HH:MM:SS");
							}
							*/
							
							// make following variables global, to allow making of modularized functions computeActualLagJdbc() and lagSecondsSinceMidnight()

							checkLagHour = 0;
							checkLagMin = 0;
							checkLagSec = 0;
							checkLagTotalSeconds = 0;
							
							if(value.contains("CheckLag=") && !(value.contains("CheckLag=Y")) )	//compare actual lag against lag specified with 'CheckLag=' parameter 
							{
								// to know value of LOCATION parameter from row from result of SQL execution
								int indexEndOfCheckLag = -1;
								String CheckLag = "";
								int indexOfCheckLag = value.indexOf("CheckLag=");
								if(indexOfCheckLag != -1)	// pattern 'CheckLag=' exists in commandParams
								{
									indexEndOfCheckLag =  value.indexOf(";",indexOfCheckLag);
									if(indexEndOfCheckLag == -1)	//char ';' does not exist at end of value of LOCATION parameter
									{
										CheckLag = value.substring(indexOfCheckLag+9);
									}
									else
									{
										CheckLag = value.substring(indexOfCheckLag+9, indexEndOfCheckLag);
									}
									
									//value of CheckLag is in format 'HH:MM:SS'
									int indexOfFirstColon = CheckLag.indexOf(':');
									int indexOfLastColon = CheckLag.lastIndexOf(':');
		
									checkLagHour = Integer.parseInt( CheckLag.substring(0, indexOfFirstColon));
									checkLagMin = Integer.parseInt( CheckLag.substring(indexOfFirstColon+1, indexOfLastColon));
									checkLagSec = Integer.parseInt( CheckLag.substring(indexOfLastColon+1));
									
									checkLagTotalSeconds=  checkLagHour*3600 + checkLagMin*60  + checkLagSec;
									
								}
							}
							/*
							else	//compare actual lag against the SECONDS SINCE MIDNIGHT
							{
								//this functionality goes into a fn. lagSecondsSinceMidnight,  
								// and sets these global-variables : checkLagHour, checkLagMin,checkLagSec, checkLagTotalSeconds
								
								
								//Calendar cal = Calendar.getInstance();
								//checkLagHour = cal.get(Calendar.HOUR_OF_DAY);
								//checkLagMin = cal.get(Calendar.MINUTE);
								//checkLagSec = cal.get(Calendar.SECOND);
								
								//checkLagTotalSeconds =  checkLagHour*3600 + checkLagMin*60  + checkLagSec;
								
								lagSecondsSinceMidnight();
							}
							*/
							
							
							/* //repeat the CheckLag according to WaitLoop configuration. 

							//compute actual Lag by execution of CheckLag.sql and set global-variable actualLagSeconds
							computeActualLagJdbc();
							
							if(actualLagSeconds > checkLagTotalSeconds )
							{	//do Not generate the report, instead print a message in logs about Lag value
								//throw new Exception("Lag of replication equal to "+actualLagSeconds+" seconds on DBRef is More than the permissible Lag of "+checkLagTotalSeconds+ " seconds");
								throw new Exception("Lag of replication equal to +"+lagDaysStr+" "+lagHourStr+":"+lagMinStr+":"+lagSecStr+" on DBRef is More than the permissible Lag of "+String.format("%02d", checkLagHour)+":"+String.format("%02d", checkLagMin)+":"+String.format("%02d", checkLagSec));
							}
							*/
							computeActualLagJdbc();
							if(!value.contains("CheckLag=") || value.contains("CheckLag=Y") )
								lagSecondsSinceMidnight();
							
							if(initialWaitSec > 0)
							{
								//computeActualLagJdbc();
								//if(!value.contains("CheckLag="))
								//	lagSecondsSinceMidnight();
								
								if(loopTimes > 0)	//'loopTimes' option is active, and Not 'waitUntil'
								{
									int currentLoopNum = 1;
									while(actualLagSeconds > checkLagTotalSeconds)	
									{
										Thread.sleep(initialWaitSec*1000);
										if(currentLoopNum == loopTimes)
										{
											break;
										}
										computeActualLagJdbc(); 
										if(!value.contains("CheckLag=") || value.contains("CheckLag=Y"))
											lagSecondsSinceMidnight();
										
										currentLoopNum++;
									}

								}	
								else	//'waitUntil' option is active, and Not 'loopTimes'
								{
									Calendar currentCal;
									Calendar waitUntilCal;
									waitUntilCal = Calendar.getInstance();
									waitUntilCal.set(Calendar.HOUR_OF_DAY, waitUntilHour);
									waitUntilCal.set(Calendar.MINUTE, waitUntilMin);
									waitUntilCal.set(Calendar.SECOND, waitUntilSec);

									while(actualLagSeconds > checkLagTotalSeconds)	
									{
										Thread.sleep(initialWaitSec*1000);
										currentCal = Calendar.getInstance();	
										
										if(currentCal.after(waitUntilCal))	//waitUntil time elapsed, and semaphoreFile still Not found
										{
											break;
										}
										computeActualLagJdbc();
										if(!value.contains("CheckLag=") || value.contains("CheckLag=Y"))
											lagSecondsSinceMidnight();									
									}
								}
							}	//end of 'if(initialWaitSec > 0)'
														
							if(actualLagSeconds > checkLagTotalSeconds )
							{	
								throw new Exception("Lag of replication equal to +"+lagDaysStr+" "+lagHourStr+":"+lagMinStr+":"+lagSecStr+" on DBRef is More than the permissible Lag of "+String.format("%02d", checkLagHour)+":"+String.format("%02d", checkLagMin)+":"+String.format("%02d", checkLagSec));
							}	
							
						}
						catch(Exception e)
						{
							//incorrect report number was displayed in case of error
							//logger.error(countLocal + ";" + generateType + ";FAILURE;-1;Command line responsible:"+valueBackUp );	
							
							//requirement to print uniform error messages on 20-jul-2015
							//logger.error(numportion.toString() + ";" + generateType + ";FAILURE;-1;Bad arguments in command line:"+valueBackUp );	
							
							//requirement: 10/09/2015: Bad number format for elapsed time 
							//logger.error(count + ";" + generateType + ";FAILURE;403;"+targetDirStr + ";" + targetFileStr+";"+ startDateLogging + ";" + endDateLogging+";ExecTime " + (System.currentTimeMillis() - valInit) / 1000 + "(s);"+e.toString()  );
							logger.error(count + ";" + generateType + ";FAILURE;403;"+targetDirStr + ";" + targetFileStr+";"+ startDateLogging + ";" + endDateLogging+";ExecTime " + df.format((System.currentTimeMillis() - valInit) / 1000) + "(s);"+e  );

							//it should also be good to add the error description at the end of the line 
							//logger.error("Exception:", e);
							
							throw e;
						}
				}	//end of 'if', in order to skip any lag functionality if "CheckLag=N" parameter specified
				
				
				
				//requirement: 358627: Add a wait time option in GR-Launcher plugin
				if(value.contains("FileCheck=") )	
				{																							
						try
						{							
								// to know value of FileCheck parameter 
								int indexEndOfFileCheck = -1;
								String FileCheck = "";
								int indexOfFileCheck = value.indexOf("FileCheck=");
								String FileCheckAbsolute = "";
								boolean fileCheckMissingFlag = false;
								
								if(indexOfFileCheck != -1)	// pattern 'FileCheck=' exists in commandParams
								{
									indexEndOfFileCheck =  value.indexOf(";",indexOfFileCheck);
									if(indexEndOfFileCheck == -1)	//char ';' does not exist at end of value of FileCheck parameter
									{
										FileCheck = value.substring(indexOfFileCheck+10);
									}
									else
									{
										FileCheck = value.substring(indexOfFileCheck+10, indexEndOfFileCheck);
									}
									
									//after parsing, remove pattern 'FileCheck=Reporting_Suspended1.csv' from command-line
									if(indexEndOfFileCheck != -1)	// ';' char exists after end of value of FileCheck parameter
									{
										StringBuilder sbValue = new StringBuilder(value);
										value = sbValue.deleteCharAt(indexEndOfFileCheck).toString();
									}
									value = value.replace("FileCheck="+FileCheck,"");
									
									
									FileCheckAbsolute = FileCheck;
									if(!FileCheck.contains(":"))
									{
										FileCheckAbsolute = targetPath + FileCheck;
									}
									
									//repeat the filecheck according to WaitLoop configuration.
									fileCheckMissingFlag = !(new File(FileCheckAbsolute).exists());

									if(initialWaitSec > 0)
									{
										//fileCheckMissingFlag = !(new File(FileCheckAbsolute).exists());

										if(loopTimes > 0)	//'loopTimes' option is active, and Not 'waitUntil'
										{
											int currentLoopNum = 1;
											while(fileCheckMissingFlag)	//Semaphore File Not present
											{
												Thread.sleep(initialWaitSec*1000);
												if(currentLoopNum == loopTimes)
												{
													break;
												}
												fileCheckMissingFlag = !(new File(FileCheckAbsolute).exists());
												currentLoopNum++;
											}

										}	
										else	//'waitUntil' option is active, and Not 'loopTimes'
										{
											Calendar currentCal;
											Calendar waitUntilCal;
											waitUntilCal = Calendar.getInstance();
											waitUntilCal.set(Calendar.HOUR_OF_DAY, waitUntilHour);
											waitUntilCal.set(Calendar.MINUTE, waitUntilMin);
											waitUntilCal.set(Calendar.SECOND, waitUntilSec);

											while(fileCheckMissingFlag)	//Semaphore File Not present
											{
												Thread.sleep(initialWaitSec*1000);
												currentCal = Calendar.getInstance();	
												
												if(currentCal.after(waitUntilCal))	//waitUntil time elapsed, and semaphoreFile still Not found
												{
													break;
												}
												fileCheckMissingFlag = !(new File(FileCheckAbsolute).exists());
											}
										}
									}	//end of 'if(initialWaitSec > 0)'
									
									//fileCheckMissingFlag = !(new File(FileCheckAbsolute).exists());
									
									if(fileCheckMissingFlag )
									{	
										throw new Exception("FileCheck Semaphore file "+FileCheckAbsolute+" does Not exist");
									}	
								}	
									
						}
						catch(Exception e)
						{
							logger.error(count + ";" + generateType + ";FAILURE;408;"+targetDirStr + ";" + targetFileStr+";"+ startDateLogging + ";" + endDateLogging+";ExecTime " + df.format((System.currentTimeMillis() - valInit) / 1000) + "(s);"+e  );

							throw e;
						}
				}	//end of 'if(value.contains("FileCheck=") )' i.e. wait time functionality

				
				//requirement for RegenMark functionality 
				//e.g. Run_11_1=Basic\\Channel_User_KPI_Report-1.0.1.rpt;Transactions;QuerryExec=sql\\HeadsOfNetwokList.sql;RegenMark=2
				
				if(value.contains("RegenMark=") && !value.contains("RegenMark=0"))	//requirement to skip any RegenMark functionality if "RegenMark=0" parameter specified 
				{			
					try
					{
							int indexEndOfRegenMark = -1;
							String RegenMark = "";
							int indexOfRegenMark = value.indexOf("RegenMark=");
							if(indexOfRegenMark != -1)	// pattern 'RegenMark=' exists in commandParams
							{
								indexEndOfRegenMark =  value.indexOf(";",indexOfRegenMark);
								if(indexEndOfRegenMark == -1)	//char ';' does not exist at end of value of RegenMark parameter
								{
									RegenMark = value.substring(indexOfRegenMark+10);
								}
								else
								{
									RegenMark = value.substring(indexOfRegenMark+10, indexEndOfRegenMark);
								}
							}
						
							String regenStart = "";
							String regenEnd = "";
							
							switch(Integer.parseInt(RegenMark))
							{
								case 1:	
									regenStart = luncherInfo.getRegen01StartDate();
									regenEnd = luncherInfo.getRegen01EndDate();
									break;
								case 2:	
									regenStart = luncherInfo.getRegen02StartDate();
									regenEnd = luncherInfo.getRegen02EndDate();
									break;
								case 3:	
									regenStart = luncherInfo.getRegen03StartDate();
									regenEnd = luncherInfo.getRegen03EndDate();
									break;
								case 4:	
									regenStart = luncherInfo.getRegen04StartDate();
									regenEnd = luncherInfo.getRegen04EndDate();
									break;
								default:
									throw new Exception("RegenMark parameter can have value within 1 to 4");
							}
							
							//String propertyRegenStart = "fr.orange.report.regenerate0"+RegenMark+".startDate";
							//String propertyRegenEnd = "fr.orange.report.regenerate0"+RegenMark+".endDate";
							
							/* start of regenmark functionality for startdate */	

							//replace start-date value with value of regenStart
							int indexEndOfStartDateVal = -1;
							String StartDateVal = "";
							int indexOfStartDateVal = -1;
							int indexOfEqualOprtr = -1;
							
							if(value.contains("startDate=") || value.contains("date_a=") || value.contains("fromdate="))
							{
								indexOfStartDateVal = StringUtils.indexOfAny(value,new String[] {"startDate=","date_a=","fromdate="});
								indexOfEqualOprtr = value.indexOf("=",indexOfStartDateVal);
							}
							
							String newValue = "";
							
							if(indexOfStartDateVal != -1)	// pattern 'startDate=/date_a=/fromdate=' exists in commandParams
							{
								indexEndOfStartDateVal =  value.indexOf(";",indexOfStartDateVal);
								if(indexEndOfStartDateVal == -1)	//char ';' does not exist at end of value of RegenMark parameter
								{
									//StartDateVal = value.substring(indexOfStartDateVal+10);
									//replace these values with the value regenstart.
									newValue = value.substring(0,indexOfEqualOprtr+1) + regenStart;
								}
								else
								{
									//StartDateVal = value.substring(indexOfStartDateVal+10, indexEndOfStartDateVal);
									//replace these values with the value regenstart.
									newValue = value.substring(0,indexOfEqualOprtr+1) + regenStart+value.substring(indexEndOfStartDateVal);
								}
							}//end-of if(indexOfStartDateVal != -1)	i.e. pattern 'startDate=/date_a=/fromdate=' exists in commandParams
							else
							{
								if(value.endsWith(";"))
								{
									newValue = value + "startDate="+regenStart;
								}
								else
								{
									newValue = value +";" +  "startDate="+regenStart;
								}
							}
							
							value = newValue ;
							/* end of regenmark functionality for startdate */	
								
							
							/* start of regenmark functionality for enddate */	

							//replace end-date value with value of regenEnd
							int indexEndOfEndDateVal = -1;
							String EndDateVal = "";
							int indexOfEndDateVal = -1;
							indexOfEqualOprtr = -1;
							
							if(value.contains("endDate=") || value.contains("date_b=") || value.contains("todate="))
							{
								indexOfEndDateVal = StringUtils.indexOfAny(value,new String[] {"endDate=","date_b=","todate="});
								indexOfEqualOprtr = value.indexOf("=",indexOfEndDateVal);
							}
							
							newValue = "";
							
							if(indexOfEndDateVal != -1)	// pattern 'endDate=/date_b=/todate=' exists in commandParams
							{
								indexEndOfEndDateVal =  value.indexOf(";",indexOfEndDateVal);
								if(indexEndOfEndDateVal == -1)	//char ';' does not exist at end of value of RegenMark parameter
								{
									//replace these values with the value regenstart.
									newValue = value.substring(0,indexOfEqualOprtr+1) + regenEnd;
								}
								else
								{
									//replace these values with the value regenstart.
									newValue = value.substring(0,indexOfEqualOprtr+1) + regenEnd+value.substring(indexEndOfEndDateVal);
								}
							}//end-of if(indexOfEndDateVal != -1)	i.e. pattern 'endDate=/date_b=/todate=' exists in commandParams
							else
							{
								if(value.endsWith(";"))
								{
									newValue = value + "endDate="+regenEnd;
								}
								else
								{
									newValue = value +";" +  "endDate="+regenEnd;
								}
							}
							
							value = newValue ;
							/* end of regenmark functionality for enddate */
							
							// now remove the following pattern from variable 'value'
							// 'RegenMark=1;'
							// Re-calculating the index of pattern 'RegenMark=1;', since new values are substituted into 'value' string
							indexEndOfRegenMark = -1;
							RegenMark = "";
							indexOfRegenMark = value.indexOf("RegenMark=");
							if(indexOfRegenMark != -1)	// pattern 'RegenMark=' exists in commandParams
							{
								indexEndOfRegenMark =  value.indexOf(";",indexOfRegenMark);
								if(indexEndOfRegenMark == -1)	//char ';' does not exist at end of value of RegenMark parameter
								{
									RegenMark = value.substring(indexOfRegenMark+10);
								}
								else
								{
									RegenMark = value.substring(indexOfRegenMark+10, indexEndOfRegenMark);
								}
							}
							
							if(indexEndOfRegenMark != -1)	// ';' char exists after end of value of RegenMark parameter
							{
								//value.replace(value.substring(indexEndOfSqlPath,indexEndOfSqlPath+1),"");
								StringBuilder sbValue = new StringBuilder(value);
								value = sbValue.deleteCharAt(indexEndOfRegenMark).toString();
							}
							value = value.replace("RegenMark="+RegenMark,"");
							
						}	//end of 'try'
						catch(Exception e)
						{
							logger.error(count + ";" + generateType + ";FAILURE;402;"+targetDirStr + ";" + targetFileStr+";"+ startDateLogging + ";" + endDateLogging+";ExecTime " + df.format((System.currentTimeMillis() - valInit) / 1000) + "(s);"+e  );
							throw e;
						}	
					}		//end of if(value.contains("RegenMark=") && !value.contains("RegenMark=0"))
				//logger.info(" * value after regenmark is "+value);		
				
				// requirement by francis, 20-jul-15: We could add a global parameter SendEmail=Y or N in GR-Plugin.properties 
				// to activate or deactivate the use of email if it is not requested or if the server is not up
				//String urlFilePropertiesGlobal = ConstanteGR.getUrlfileproperties();
				//Properties pGlobal = GRUtils.loadProperties(urlFilePropertiesGlobal);
				String globalSendEmail = pGlobal.getProperty("email.global.SendEmail");
				
				//requirement: 18-may-2016: change global ftp option's name from 'email.global.SendFTP' to 'ftp.global.SendFTP'
				//String globalSendFTP = pGlobal.getProperty("email.global.SendFTP");
				String globalSendFTP = pGlobal.getProperty("ftp.global.SendFTP");


				//logger.info(" * A ");		

				globalEmailFlag = false;
				if(globalSendEmail != null && globalSendEmail.equals("Y"))
				{
					//requirement: 18-may-2016: bug (error code 300) in globalsendemail switch when this is absent
					globalEmailFlag = true;
					//logger.info(" * AA ");		

						//check the functionality of "<QueryCheck>;QueryCheck=sql\\CheckAmbigousTransfers.sql;"
						// or better check for "<QueryCheck>;QueryCheck=GR-Sql\\irt_CheckAmbigousTransfers.sql;"
						
						//make additional check to do this processing only if "SendEmail=Y" parameter is specified
						if(value.contains("QueryCheck=") && value.contains("<QueryCheck>"))
						//if(value.contains("QueryCheck=") && value.contains("<QueryCheck>") && value.contains("SendEmail=Y"))
						{
							// put condition to ignore ambiguousTransferFlag if queryCheckFlag is false
							queryCheckFlag = true;
							//logger.info(" * AB ");		

							try
							{
								// to know value of QueryCheck parameter i.e. path of sql script (telling count of ambiguous transfers)
								// from a row of reports.properties
								int indexOfAmbiguousCountSqlPath = value.indexOf("QueryCheck=");
								int indexEndOfAmbiguousCountSqlPath =  value.indexOf(";",indexOfAmbiguousCountSqlPath);
								String ambiguousCountSqlPath = "";
								if(indexEndOfAmbiguousCountSqlPath == -1)	//char ';' does not exist at end of value of LOCATION parameter
								{
									ambiguousCountSqlPath = value.substring(indexOfAmbiguousCountSqlPath+11);
								}
								else
								{
									ambiguousCountSqlPath = value.substring(indexOfAmbiguousCountSqlPath+11, indexEndOfAmbiguousCountSqlPath);
								}
								
								// ambiguousCountSqlPath contains 'GR-Sql\\irt_CheckAmbigousTransfers.sql'
								int indexOfDblSlash = ambiguousCountSqlPath.indexOf( "\\");
								int indexOfUndrscr = ambiguousCountSqlPath.indexOf( "_",indexOfDblSlash );
								
								mysqlDbSrc = ambiguousCountSqlPath.substring(indexOfDblSlash+1, indexOfUndrscr);
								String sqlPathWithoutMysqlDbSrc = ambiguousCountSqlPath.substring(indexOfUndrscr+1);
								//mysqlDbSrc contains 'irt'
								//sqlPathWithoutMysqlDbSrc contains 'CheckAmbigousTransfers.sql'
								
								String sqlPath = "";
								if(indexOfUndrscr != -1)	// '_' char exists in ambiguousCountSqlPath string
								{
									StringBuilder sbValue = new StringBuilder(ambiguousCountSqlPath);
									sqlPath = sbValue.deleteCharAt(indexOfUndrscr).toString();
								}
								sqlPath = sqlPath.replaceFirst(mysqlDbSrc, "");
								// sqlPath contains 'GR-Sql\\CheckAmbigousTransfers.sql'
								
								sourceSqlPath = luncherInfo.getSqlpath();
								ambiguousCountSqlAbsolutePath = sourceSqlPath + sqlPath;
								
								// mysqlDbSrc contains 'irt'
								// and the variable ambiguousCountSqlAbsolutePath contains value 
								// 'C:\\Program Files (x86)\\i-net Clear Reports\\GR-Sql\\CheckAmbigousTransfers.sql' 
								
								// now remove the following pattern from variable 'value'
								// '<QueryCheck>;QueryCheck=sql\\CheckAmbigousTransfers.sql;'
								
								if(indexEndOfAmbiguousCountSqlPath != -1)	// ';' char exists after end of value of sqlPath parameter
								{
									//value.replace(value.substring(indexEndOfSqlPath,indexEndOfSqlPath+1),"");
									StringBuilder sbValue = new StringBuilder(value);
									value = sbValue.deleteCharAt(indexEndOfAmbiguousCountSqlPath).toString();
								}
								
								//logger.info(" * AC ; value :" + value);		

								value = value.replace("QueryCheck=","");
								value = value.replace(ambiguousCountSqlPath,"");
								//logger.info(" * AD ; value :" + value);		

								//now remove '<QueryCheck>' 
								value = value.replace("<QueryCheck>;", "");
								value = value.replace("<QueryCheck>", "");
								//logger.info(" * AE ; value :" + value);		

								//comment for time being, while mysql is not available
								

								//repeat the ambiguousTransferFlag(i.e. QueryCheck) according to WaitLoop configuration.
								//set the global-variable AmbiguousTransferFlag
								computeAmbiguousTransferFlag();
								
								if(initialWaitSec > 0)
								{
									
									if(loopTimes > 0)	//'loopTimes' option is active, and Not 'waitUntil'
									{
										int currentLoopNum = 1;
										while( !ambiguousTransferFlag )	
										{
											Thread.sleep(initialWaitSec*1000);
											if(currentLoopNum == loopTimes)
											{
												break;
											}
											computeAmbiguousTransferFlag();
											
											currentLoopNum++;
										}

									}	
									else	//'waitUntil' option is active, and Not 'loopTimes'
									{
										Calendar currentCal;
										Calendar waitUntilCal;
										waitUntilCal = Calendar.getInstance();
										waitUntilCal.set(Calendar.HOUR_OF_DAY, waitUntilHour);
										waitUntilCal.set(Calendar.MINUTE, waitUntilMin);
										waitUntilCal.set(Calendar.SECOND, waitUntilSec);

										while( !ambiguousTransferFlag )	
										{
											Thread.sleep(initialWaitSec*1000);
											currentCal = Calendar.getInstance();	
											
											if(currentCal.after(waitUntilCal))	//waitUntil time elapsed, and semaphoreFile still Not found
											{
												break;
											}
											computeAmbiguousTransferFlag();								
										}
									}
								}	//end of 'if( > 0)'
															
								if( !ambiguousTransferFlag )
								{	
									throw new Exception("QueryCheck failed");
								}							
								
								/*	
								 * all this commented code now put into function computeAmbiguousTransferFlag()

								ConnetJdbcDbref jdbcDbref = new ConnetJdbcDbref();
								resultats = jdbcDbref.getNewListUserChannelMysql(ambiguousCountSqlAbsolutePath, mysqlDbSrc);
								
								String countAmbiguousRS = resultats.get(0);	//e.g.	";count=1"
								
								//logger.info(" ** ambiguousCountSqlAbsolutePath="+ambiguousCountSqlAbsolutePath+", mysqlDbSrc="+ mysqlDbSrc);
								//String countAmbiguousRS = ";count=1";
								
								if(countAmbiguousRS == null || countAmbiguousRS.equals(""))
								{
									throw new Exception("Count of ambiguous transfers could Not be fetched by DB");
								}
								
								
								int indexEndOfCOUNT = -1;
								int COUNT = 0;
								int indexOfCOUNT = countAmbiguousRS.indexOf("count=");
								
								if(indexOfCOUNT != -1)	// pattern 'count=' exists in resultset
								{
									indexEndOfCOUNT =  countAmbiguousRS.indexOf(";",indexOfCOUNT);
									if(indexEndOfCOUNT == -1)	//char ';' does not exist at end of value of LOCATION parameter
									{
										COUNT = Integer.parseInt(countAmbiguousRS.substring(indexOfCOUNT+6));
									}
									else
									{
										COUNT = Integer.parseInt(countAmbiguousRS.substring(indexOfCOUNT+6, indexEndOfCOUNT));
									}
								}	
								
								if(COUNT > 0)
								{
									ambiguousTransferFlag = true;
								}
								*/
							}
							catch(Exception e)
							{
							 	//requirement to print uniform error messages on 20-jul-2015
								//logger.error(numportion.toString() + ";" + generateType + ";FAILURE;-1;Bad arguments in command line:"+valueBackUp );	
							 	
								//requirement: 10/09/2015: Bad number format for elapsed time 
								//logger.error(count + ";" + generateType + ";FAILURE;405;"+targetDirStr + ";" + targetFileStr+";"+ startDateLogging + ";" + endDateLogging+";ExecTime " + (System.currentTimeMillis() - valInit) / 1000 + "(s);"+e.toString()  );
								//Code change for Forge#375827
								if (value.contains("GenReportIfQueryCheckNOK=Y") &&  !value.contains("WaitLoop="))
								{
									logger.info(count + ";" + generateType + ";SUCCESS;203;"+targetDirStr + ";" + targetFileStr+";"+ startDateLogging + ";" + endDateLogging+";ExecTime " + df.format((System.currentTimeMillis() - valInit) / 1000) + "(s);"+ "QueryCheck : Conditions were not ok however report will still be generated"  );
								}
								else if (!value.contains("WaitLoop="))
								{
									logger.error(count + ";" + generateType + ";INFO;500;"+targetDirStr + ";" + targetFileStr+";"+ startDateLogging + ";" + endDateLogging+";ExecTime " + df.format((System.currentTimeMillis() - valInit) / 1000) + "(s);"+ "QueryCheck : Conditions were not ok to generate the report or send the email"  );
									throw e;
								}
								else
								{
									logger.error(count + ";" + generateType + ";FAILURE;405;"+targetDirStr + ";" + targetFileStr+";"+ startDateLogging + ";" + endDateLogging+";ExecTime " + df.format((System.currentTimeMillis() - valInit) / 1000) + "(s);"+e );
								//it should also be good to add the error description at the end of the line 
							 	//logger.error("Exception:", e);									
									throw e;
								}
							}
						}
						
		
						
						
						//check the functionality of ";EmailParam=email\\IRTAmbigousTransfers.properties"
						emailRecipients = ""; emailSubject = ""; emailContent = ""; emailParamFlag = false; emailCopyRecipients = "";emailBlindCopyRecipients = "";
						//make additional check to do this processing only if "SendEmail=Y" parameter is specified
						if(value.contains("EmailParam=") )
						//if(value.contains("EmailParam=") && value.contains("SendEmail=Y"))
						{
							try
							{
								// to know value of EmailParam parameter i.e. path of properties file containing email params
								// from a row of reports.properties
								int indexOfEmailParam = value.indexOf("EmailParam=");
								int indexEndOfEmailParam =  value.indexOf(";",indexOfEmailParam);
								String emailParam = "";
								if(indexEndOfEmailParam == -1)	//char ';' does not exist at end of value of EmailParam parameter
								{
									emailParam = value.substring(indexOfEmailParam+11);
								}
								else
								{
									emailParam = value.substring(indexOfEmailParam+11, indexEndOfEmailParam);
								}
								
								sourceSqlPath = luncherInfo.getSqlpath();
								emailParamAbsolutePath = sourceSqlPath + emailParam;
								
								// now remove the following pattern from variable 'value'
								// ';EmailParam=email\\IRTAmbigousTransfers.properties'
								
								if(indexEndOfEmailParam != -1)	// ';' char exists after end of value of emailParam parameter
								{
									//value.replace(value.substring(indexEndOfSqlPath,indexEndOfSqlPath+1),"");
									StringBuilder sbValue = new StringBuilder(value);
									value = sbValue.deleteCharAt(indexEndOfEmailParam).toString();
								}
								value = value.replace("EmailParam=","");
								value = value.replace(emailParam,"");
								
								// now read emailparams from the specified properties file
								Properties propeties = GRUtils.loadProperties(emailParamAbsolutePath);
								emailRecipients = propeties.getProperty("email.recipient.id");
								emailSubject= propeties.getProperty("email.subject.text");
								emailContent= propeties.getProperty("email.content.text");
								
								//requirement 20-jul to add email CC recipients too
								emailCopyRecipients = propeties.getProperty("email.copyrecipient.id");
								
								//requirement 25-sep to add email BCC recipients too
								emailBlindCopyRecipients = propeties.getProperty("email.blindcopyrecipient.id");

								emailParamFlag = true;
							}
							catch(Exception e)
							{
							 	//requirement to print uniform error messages on 20-jul-2015
								//logger.error(numportion.toString() + ";" + generateType + ";FAILURE;-1;Bad arguments in command line:"+valueBackUp );	
							 	
								//requirement: 10/09/2015: Bad number format for elapsed time 
								//logger.error(count + ";" + generateType + ";FAILURE;406;"+targetDirStr + ";" + targetFileStr+";"+ startDateLogging + ";" + endDateLogging+";ExecTime " + (System.currentTimeMillis() - valInit) / 1000 + "(s);"+e.toString()  );
								logger.error(count + ";" + generateType + ";FAILURE;406;"+targetDirStr + ";" + targetFileStr+";"+ startDateLogging + ";" + endDateLogging+";ExecTime " + df.format((System.currentTimeMillis() - valInit) / 1000 ) + "(s);"+e  );

							 	//it should also be good to add the error description at the end of the line 
								//logger.error("Exception:", e);
								throw e;
							}
						}
				}//end of 'if(globalSendEmail != null && globalSendEmail.equals("Y"))
				
				
				// requirement by francis, 20-jul-15: We could add a global parameter SendFTP =Y or N in GR-Plugin.properties 
				// to activate or deactivate the use of ftp if it is not requested or if the server is not up
				
				//requirement: 18-may-2016: bug (error code 300) in globalsendemail switch when this is absent
				globalFtpFlag = false;
				if(globalSendFTP != null && globalSendFTP.equals("Y"))
				{
						//	requirement: 18-may-2016: bug (error code 300) in globalsendemail switch when this is absent
						globalFtpFlag = true;
					
						//check the functionality of ";FTPParam=GR-FTP\\InterOpTransfers.properties"
						ftpIpGlobal = ""; ftpPortGlobal = ""; ftpUserGlobal = "";ftpPasswordGlobal = ""; ftpParamFlag = false;
						if(value.contains("FTPParam="))
						{
							try
							{
								// to know value of FTPParam parameter i.e. path of properties file containing ftp params
								// from a row of reports.properties e.g. GR-FTP\\InterOpTransfers.properties
								int indexOfFtpParam = value.indexOf("FTPParam=");
								int indexEndOfFtpParam =  value.indexOf(";",indexOfFtpParam);
								String ftpParam = "";
								if(indexEndOfFtpParam == -1)	//char ';' does not exist at end of value of FTPParam parameter
								{
									ftpParam = value.substring(indexOfFtpParam+9);
								}
								else
								{
									ftpParam = value.substring(indexOfFtpParam+9, indexEndOfFtpParam);
								}
								
								sourceSqlPath = luncherInfo.getSqlpath();
								ftpParamAbsolutePath = sourceSqlPath + ftpParam;
								
								// now remove the following pattern from variable 'value'
								// ';FTPParam=GR-FTP\\InterOpTransfers.properties'
								
								if(indexEndOfFtpParam != -1)	// ';' char exists after end of value of ftpParam parameter
								{
									//value.replace(value.substring(indexEndOfSqlPath,indexEndOfSqlPath+1),"");
									StringBuilder sbValue = new StringBuilder(value);
									value = sbValue.deleteCharAt(indexEndOfFtpParam).toString();
								}
								value = value.replace("FTPParam=","");
								value = value.replace(ftpParam,"");
								
								// now read ftpparams from the specified properties file
								Properties propeties = GRUtils.loadProperties(ftpParamAbsolutePath);
								ftpIpGlobal = propeties.getProperty("ftp.target.ip");
								ftpPortGlobal= propeties.getProperty("ftp.target.port");
								ftpUserGlobal= propeties.getProperty("ftp.client.user");
								ftpPasswordGlobal= propeties.getProperty("ftp.client.password");
								
								//requirement:21-sep-2015 the password must be hidden
								ftpPasswordGlobal = AESencrp.decrypt(ftpPasswordGlobal);
		
								ftpParamFlag = true;
							}
							catch(Exception e)
							{
							 	//requirement to print uniform error messages on 20-jul-2015
								//logger.error(numportion.toString() + ";" + generateType + ";FAILURE;-1;Bad arguments in command line:"+valueBackUp );	
							 	
								//requirement: 10/09/2015: Bad number format for elapsed time 
								//logger.error(count + ";" + generateType + ";FAILURE;407;"+targetDirStr + ";" + targetFileStr+";"+ startDateLogging + ";" + endDateLogging+";ExecTime " + (System.currentTimeMillis() - valInit) / 1000 + "(s);"+e.toString()  );
								logger.error(count + ";" + generateType + ";FAILURE;407;"+targetDirStr + ";" + targetFileStr+";"+ startDateLogging + ";" + endDateLogging+";ExecTime " + df.format((System.currentTimeMillis() - valInit) / 1000) + "(s);"+e  );

							 	//it should also be good to add the error description at the end of the line 
								//logger.error("Exception:", e);
								throw e;
							}
						}
				}//end of 'if(globalSendFTP != null && globalSendFTP.equals("Y"))'
				
				//requirement: 25-sep: repare functionality
				//Repare=IfMissing/IfEmpty/All (default = All)
				
				repareParam = "";
				if(value.contains("Repair=") )
				{
					int indexOfRepareParam = value.indexOf("Repair=");
					int indexEndOfRepareParam =  value.indexOf(";",indexOfRepareParam);
					
					if(indexEndOfRepareParam == -1)	//char ';' does not exist at end of value of Repare parameter
					{
						repareParam = value.substring(indexOfRepareParam+7);
					}
					else
					{
						repareParam = value.substring(indexOfRepareParam+7, indexEndOfRepareParam);
					}
					
					/*** put an inner try-catch block to PRINT any exceptions raised in this inner try-catch block **/
					try
					{
						
							if( repareParam == null || ( !repareParam.equals("IfMissing") && !repareParam.equals("IfEmpty")  && !repareParam.equals("All") ) )
							{
								throw new Exception("Command parameter 'Repair' does Not contain value amongst IfMissing, IfEmpty and All");
							}

							if(indexEndOfRepareParam != -1)	// ';' char exists after end of value of sqlPath parameter
							{
								//value.replace(value.substring(indexEndOfSqlPath,indexEndOfSqlPath+1),"");
								StringBuilder sbValue = new StringBuilder(value);
								value = sbValue.deleteCharAt(indexEndOfRepareParam).toString();
							}
							value = value.replace("Repair=","");
							value = value.replace(repareParam,"");
									
					}	
					catch(Exception e)
					{
						logger.error(count + ";" + generateType + ";FAILURE;402;"+targetDirStr + ";" + targetFileStr+";"+ startDateLogging + ";" + endDateLogging+";ExecTime " + df.format((System.currentTimeMillis() - valInit) / 1000) + "(s);"+e  );
						
						//it should also be good to add the error description at the end of the line 
					 	//logger.error("Exception:", e);
						throw e;
					}
				}	//end of 'if(value.contains("Repair=") )'
				
				
				
				if(value.contains("QueryExec=") && value.contains("<QueryExec>") )
				{
					massiveFlag = true;
					//logger.info("*** C ");

					// to know value of QueryExec parameter i.e. path of sql script from a row of reports.properties
					int indexOfSqlPath = value.indexOf("QueryExec=");
					int indexEndOfSqlPath =  value.indexOf(";",indexOfSqlPath);
					String sqlPath = "";
					if(indexEndOfSqlPath == -1)	//char ';' does not exist at end of value of LOCATION parameter
					{
						sqlPath = value.substring(indexOfSqlPath+10);
					}
					else
					{
						sqlPath = value.substring(indexOfSqlPath+10, indexEndOfSqlPath);
					}
					
					
					int indexFirstSemicolon = value.indexOf(";");	//index of first semi-colon in value string 
					//i.e. unexpanded params string from reports.properties
					String rptName = value.substring(0,indexFirstSemicolon+1) ;
					//till now value of rptName is 'Basic\\ Channel_User_Balance_by_Network_Report_G4-1.0.2.rpt;'
					
					// ***IMPORTANT ***
					// remove from 'value' the 1) 'QueryExec=' params, 2) the <QueryExec> and 3) 1st parameter (rpt name) 
					// SO THAT values retrieved from reports.properties should contain only additional params like :-
					// startdate , enddate etc.
					// *****************
					
					
					
				//	logger.info("*** D, sqlPath : "+sqlPath);
					int numUsers = 0;
					
					//requirement by francis on 3-jul-15 to remove startdatetag at end of .xls file in case of GR_Unpaid_Bill_Report
					//String timeSlot = "";	//now this variable is global
					timeSlot = "";
					
					int indexExclaimSqlpath = -1;
					String sqlPathWithoutTimeslot = "";
					/*** put an inner try-catch block to PRINT any exceptions raised in this inner try-catch block **/
					try
					{
							
							//change to process following command from reports.properties (having "QueryExec=sql\\HeadsOfNetwokList.sql!<TimeSlot>" )
							//Run_8_1=Basic\\Channel_User_KPI_Report-1.0.1.rpt;<QueryExec>;startDate=sysdate-1-00:00:00;endDate=sysdate-0-00:00:00;QueryExec=sql\\HeadsOfNetwokList.sql!<TimeSlot>
							
							//<TimeSlot> can have these possible values: Daily, Weekly , Monthly
						
							//as per latest requirement by francis on 26-may, '!<TimeSlot>' can work with all the massive reports.
							/*
							if( !rptName.contains("Channel_User_KPI_Report") && ( sqlPath.contains("!Daily") || sqlPath.contains("!Monthly")  || sqlPath.contains("!Weekly") ) )
							{
								throw new Exception("!<TimeSlot> option is allowed only for Channel User KPI Report");
							}
							else if(rptName.contains("Channel_User_KPI_Report") && sqlPath.contains("!") && !(sqlPath.contains("!Daily") || sqlPath.contains("!Monthly")  || sqlPath.contains("!Weekly")))
							{
								throw new Exception("!<TimeSlot> option for Channel User KPI Report can allow values only among Daily, Weekly and Monthly");
							}
							else if(rptName.contains("Channel_User_KPI_Report") && ( sqlPath.contains("!Daily") || sqlPath.contains("!Monthly")  || sqlPath.contains("!Weekly") ))
							{
								indexExclaimSqlpath = sqlPath.indexOf("!");
								timeSlot = sqlPath.substring(indexExclaimSqlpath+1);
								
								sqlPathWithoutTimeslot = sqlPath.substring(0,indexExclaimSqlpath);
							}
							*/
						
							if( !massiveFlag && ( sqlPath.contains("!Daily") || sqlPath.contains("!Monthly")  || sqlPath.contains("!Weekly") ) )
							{
								throw new Exception("!<TimeSlot> option is allowed only for Massive Reports");
							}
							else if(massiveFlag && sqlPath.contains("!") && !(sqlPath.contains("!Daily") || sqlPath.contains("!Monthly")  || sqlPath.contains("!Weekly")))
							{
								throw new Exception("!<TimeSlot> option for Massive Reports can allow values only among Daily, Weekly and Monthly");
							}
							else if(massiveFlag && ( sqlPath.contains("!Daily") || sqlPath.contains("!Monthly")  || sqlPath.contains("!Weekly") ))
							{
								indexExclaimSqlpath = sqlPath.indexOf("!");
								timeSlot = sqlPath.substring(indexExclaimSqlpath+1);
								
								sqlPathWithoutTimeslot = sqlPath.substring(0,indexExclaimSqlpath);
							}
						
							sourcePath = luncherInfo.getSourcepath();
							sourceSqlPath = luncherInfo.getSqlpath();

							
							
							String sqlAbsolutePath = "";
							if(sqlPathWithoutTimeslot.equals(""))
							{
								sqlAbsolutePath = sourceSqlPath + sqlPath;
							}
							else
							{
								sqlAbsolutePath = sourceSqlPath + sqlPathWithoutTimeslot;
							}
							
							//logger.info("*** E, sqlAbsolutePath : "+sqlAbsolutePath);
		
							
							
							ConnetJdbcDbref jdbcDbref = new ConnetJdbcDbref();
							
							resultats = jdbcDbref.getNewListUserChannel(sqlAbsolutePath);
		
						//	logger.info("** 1 value : "+value);
		
		
							
							
							//first remove the following pattern from value
							//'QueryExec=sql\\HeadsOfNetwokList.sql'
							
						//	logger.info("** 2 rptName : "+rptName);
		
							// move this block after 'if'
							//value = value.replace("QueryExec=","");
							//value = value.replace(sqlPath,"");
							if(indexEndOfSqlPath != -1)	// ';' char exists after end of value of sqlPath parameter
							{
								//value.replace(value.substring(indexEndOfSqlPath,indexEndOfSqlPath+1),"");
								StringBuilder sbValue = new StringBuilder(value);
								value = sbValue.deleteCharAt(indexEndOfSqlPath).toString();
							}
							value = value.replace("QueryExec=","");
							value = value.replace(sqlPath,"");
		
						//	logger.info("** 3 value : "+value);
							
							//now remove '<QueryExec>' and 1st parameter (rpt name)
							value = value.replace(rptName, "");
							value = value.replace("<QueryExec>;", "");
							value = value.replace("<QueryExec>", "");
						//	logger.info("** 4 value : "+value);
		
							numUsers = resultats.size();
							
							// to print second 'start of execution of command ..' logger-info for massive reports
							logger.info("**** Start execution of command '" + reportNumBackUp+ "';Nb of reports scheduled to run:"+numUsers+" ****");

					}	//end of inner try block having JDBC connection statements
					catch(Exception e)
					{
						//incorrect report number was displayed in case of error
						//logger.error(countLocal + ";" + generateType + ";FAILURE;-1;Command line responsible:"+valueBackUp );	
						
					 	//requirement to print uniform error messages on 20-jul-2015
						//logger.error(numportion.toString() + ";" + generateType + ";FAILURE;-1;Bad arguments in command line:"+valueBackUp );	
					 	
						//requirement: 10/09/2015: Bad number format for elapsed time 
						//logger.error(count + ";" + generateType + ";FAILURE;404;"+targetDirStr + ";" + targetFileStr+";"+ startDateLogging + ";" + endDateLogging+";ExecTime " + (System.currentTimeMillis() - valInit) / 1000 + "(s);"+e.toString()  );
						logger.error(count + ";" + generateType + ";FAILURE;404;"+targetDirStr + ";" + targetFileStr+";"+ startDateLogging + ";" + endDateLogging+";ExecTime " + df.format((System.currentTimeMillis() - valInit) / 1000) + "(s);"+e  );
						
						//it should also be good to add the error description at the end of the line 
					 	//logger.error("Exception:", e);
						throw e;
					}
					
					int countMassiveSuccess = 0;
					
					timeAllMassiveOfOneType = 0;
					countMassive = 0;
					
					for(int j=0;j<numUsers; j++)
					{
						countMassive++;
						String commandParams = resultats.get(j);	//params returned by SQL query results
						String finalCommandLine = rptName;	//many more additions to be done to finalCommandLine
						//till now value of finalCommandLine is 'Basic\\ Channel_User_Balance_by_Network_Report_G4-1.0.2.rpt;'
					//	logger.info("** 5 commandParams : "+commandParams);

						
						// to know value of LOCATION parameter from row from result of SQL execution
						int indexEndOfLOCATION = -1;
						String LOCATION = "";
						int indexOfLOCATION = commandParams.indexOf("LOCATION=");
						
						String LOCATIONAbsolute = "";
						if(indexOfLOCATION != -1)	// pattern 'LOCATION=' exists in commandParams
						{
							indexEndOfLOCATION =  commandParams.indexOf(";",indexOfLOCATION);
							if(indexEndOfLOCATION == -1)	//char ';' does not exist at end of value of LOCATION parameter
							{
								LOCATION = commandParams.substring(indexOfLOCATION+9);
							}
							else
							{
								LOCATION = commandParams.substring(indexOfLOCATION+9, indexEndOfLOCATION);
							}
							
							//requirement: 22-feb-2016: Location and Report_Name will definitely arrive from results of SQL execution
							LOCATIONAbsolute = LOCATION;
							if(!LOCATION.contains(":"))
							{
								LOCATIONAbsolute = targetPath + LOCATION;
							}
							
						}
					//logger.info("** 6 LOCATIONAbsolute : "+LOCATIONAbsolute+"**");

						
						//requirement: 22-feb-2016: Location and Report_Name will definitely arrive from results of SQL execution
						// to know value of genReportName parameter from row from result of SQL execution
						int indexEndOfgenReportName = -1;
						String genReportName = "";
						int indexOfgenReportName = commandParams.indexOf("REPORT_NAME=");
						if(indexOfgenReportName != -1)	// pattern 'REPORT_NAME=' exists in commandParams
						{
							indexEndOfgenReportName =  commandParams.indexOf(";",indexOfgenReportName);
							if(indexEndOfgenReportName == -1)	//char ';' does not exist at end of value of REPORT_NAME parameter
							{
								genReportName = commandParams.substring(indexOfgenReportName+12);
							}
							else
							{
								genReportName = commandParams.substring(indexOfgenReportName+12, indexEndOfgenReportName);
							}							
						}
						//logger.info("** 6A *commandParams*"+commandParams+"*");
	
						//logger.info("** 6AA REPORT_NAME: "+genReportName+"**indexOfgenReportName*"+indexOfgenReportName+"*indexEndOfgenReportName*"+indexEndOfgenReportName+"*");

						
						// to know value of MSISDN parameter from row from result of SQL execution
						String MSISDN = "";
						int indexEndOfMSISDN = -1;
						int indexOfMSISDN = commandParams.indexOf("MSISDN=");
						
						if(indexOfMSISDN != -1)
						{
							indexEndOfMSISDN =  commandParams.indexOf(";",indexOfMSISDN);

							if(indexEndOfMSISDN == -1)	//char ';' does not exist at end of value of MSISDN parameter
							{
								MSISDN = commandParams.substring(indexOfMSISDN+7);
							}
							else
							{
								MSISDN = commandParams.substring(indexOfMSISDN+7, indexEndOfMSISDN);
							}
						}
				//		logger.info("** 7 MSISDN : "+MSISDN);

						//fetch more columns from SQL like compCode etc.
						
						// to know value of COMPCODE parameter from row from result of SQL execution
						String COMPCODE = "";
						int indexEndOfCOMPCODE = -1;
						int indexOfCOMPCODE = commandParams.indexOf("COMPCODE=");
						
						if(indexOfCOMPCODE != -1)
						{
							indexEndOfCOMPCODE =  commandParams.indexOf(";",indexOfCOMPCODE);
	
							if(indexEndOfCOMPCODE == -1)	//char ';' does not exist at end of value of COMPCODE parameter
							{
								COMPCODE = commandParams.substring(indexOfCOMPCODE+9);
							}
							else
							{
								COMPCODE = commandParams.substring(indexOfCOMPCODE+9, indexEndOfCOMPCODE);
							}
						}
						//logger.info("** 7A COMPCODE : "+COMPCODE+"**");
						
						
						String timeSlotDirAddendum = "";
						if(!timeSlot.equals(""))
						{
							timeSlotDirAddendum = timeSlot +  "\\";
						}
						
						
						if(indexOfLOCATION != -1)	// 'LOCATION' column is returned in output of SQL; add the directory of output report
						{	
							finalCommandLine += LOCATIONAbsolute + timeSlotDirAddendum;	// timeSlot is added in order to 
						}
						
						//requirement: 22-feb-2016: Location and Report_Name will definitely arrive from results of SQL execution
						/*else if(indexOfCOMPCODE != -1)	//COMPCODE column is returned in output of SQL
						{
							//change request to include timeslot functionality for GR_Unpaid_Bill_Report too
							//finalCommandLine += "Activities\\Unpaid_Bill_Report\\";
							finalCommandLine += "Activities\\Unpaid_Bill_Report\\"+timeSlotDirAddendum;

						}	*/
							
							
						String timeSlotPrepend = "";
						if(!timeSlot.equals(""))
						{
							timeSlotPrepend = timeSlot + "-";
						}
						
						
						//requirement: 22-feb-2016: Location and Report_Name will definitely arrive from results of SQL execution
						/*
						//till now value of finalCommandLine is 
						//'Basic\\Channel_User_Balance_by_Network_Report_G4-1.0.2.rpt;C:\R\Reports\Transactions\OML_SIEGE_OML_COMMERCIAL_SIEGE_77989809\_Synthesis\'
						if(finalCommandLine.contains("Channel_User_Balance_by_Network_Report"))
						{
							finalCommandLine += "ChannelUserBalanceReport-" + MSISDN + ".xls";
						}
						//change made to handle GR_Unpaid_Bill_Report_G4-1.0.0.rpt also
						else if(finalCommandLine.contains("GR_Unpaid_Bill_Report"))
						{
							//change request to include timeslot functionality for GR_Unpaid_Bill_Report too
							//finalCommandLine += COMPCODE + "-Unpaid_Bill_Report.xls";
							finalCommandLine += timeSlotPrepend + COMPCODE + "-Unpaid_Bill_Report.xls";
						}
						//another requirement to replace 'Channel_User_KPI_Report' to 'Channel_User_KPI'
						//else if(finalCommandLine.contains("Channel_User_KPI_Report"))
						else if(finalCommandLine.contains("Channel_User_KPI"))
						{
							finalCommandLine += timeSlotPrepend + "ChannelUserKPIReport-"+MSISDN+".xls";
						}
						*/

						finalCommandLine += timeSlotPrepend + genReportName;
						
						//logger.info("** 8 finalCommandLine : "+finalCommandLine);

						//till now value of finalCommandLine is 
						//'Basic\\Channel_User_Balance_by_Network_Report_G4-1.0.2.rpt;C:\R\Reports\Transactions\OML_SIEGE_OML_COMMERCIAL_SIEGE_77989809\_Synthesis\ChannelUserBalanceReport-77989809.xls'
				
						//remove the following pattern from commandParams
						//'LOCATION=C:\R\Reports\Transactions\OML_SIEGE_OML_COMMERCIAL_SIEGE_77989809\_Synthesis\;'


						if(indexEndOfLOCATION != -1)	// ';' char exists after end of value of LOCATION parameter
						{
							//logger.info("** 8A ");

							//commandParams.replace(commandParams.substring(indexEndOfLOCATION,indexEndOfLOCATION+1),"");
							StringBuilder sbcommandParams = new StringBuilder(commandParams);
							//logger.info("** 8B ");

							commandParams = sbcommandParams.deleteCharAt(indexEndOfLOCATION).toString();
							//logger.info("** 8C ");
						}
						
						//logger.info("** 8D commandParams : "+commandParams);
						commandParams = commandParams.replace("LOCATION=","");
						//logger.info("** 8E commandParams : "+commandParams);

						commandParams = commandParams.replace(LOCATION,"");
						//logger.info("** 9 commandParams : "+commandParams);
						
						
						//requirement: 22-feb-2016: Location and Report_Name will definitely arrive from results of SQL execution

						
						//RECALCULATE the indexEndOfgenReportName, since now commandParams does NOT have LOCATION=<value>
						indexEndOfgenReportName = -1;
						genReportName = "";
						indexOfgenReportName = commandParams.indexOf("REPORT_NAME=");
						if(indexOfgenReportName != -1)	// pattern 'REPORT_NAME=' exists in commandParams
						{
							indexEndOfgenReportName =  commandParams.indexOf(";",indexOfgenReportName);
							if(indexEndOfgenReportName == -1)	//char ';' does not exist at end of value of REPORT_NAME parameter
							{
								genReportName = commandParams.substring(indexOfgenReportName+12);
							}
							else
							{
								genReportName = commandParams.substring(indexOfgenReportName+12, indexEndOfgenReportName);
							}							
						}
						
						//remove the following pattern from commandParams
						//'REPORT_NAME=BillIntegrated.csv;'
						if(indexEndOfgenReportName != -1)	// ';' char exists after end of value of REPORT_NAME parameter
						{
							StringBuilder sbcommandParams = new StringBuilder(commandParams);
							commandParams = sbcommandParams.deleteCharAt(indexEndOfgenReportName).toString();
						}
						
						commandParams = commandParams.replace("REPORT_NAME=","");
						commandParams = commandParams.replace(genReportName,"");
						
						
						
						finalCommandLine += commandParams;
						
						//logger.info("** 10 finalCommandLine : "+finalCommandLine);

						//till now value of finalCommandLine is 
						//'Basic\\Channel_User_Balance_by_Network_Report_G4-1.0.2.rpt;C:\R\Reports\Transactions\OML_SIEGE_OML_COMMERCIAL_SIEGE_77989809\_Synthesis\ChannelUserBalanceReport-77989809.xls;USER_ID=PT100212.0959.000001;MSISDN=77989809'
						
						
						//capture the other parameters supplied in 'value' i.e. supplied by reports.properties
						// and append into finalCommandLine
						if(finalCommandLine.endsWith(";") || value.startsWith(";"))
						{
							finalCommandLine += value;
						}
						else
						{
							finalCommandLine += ";"+value;
						}
						
						//logger.info("** 11 finalCommandLine just before call to generateReport : "+finalCommandLine);

						//ultimately call generateReport(finalCommandLine, DateFormat)
						
						//if 1 of the massive reports fails, continue onto next one 
						try
						{
							generateReport(finalCommandLine, DateFormat);
						}
						catch(Exception e)
						{
							continue;
						}
						
						countMassiveSuccess++;
					}	//end of 'for(int j=0;j<numUsers; j++)'
					
					countMassive = 0;
					logger.info("**** End of execution of command '" + reportNumBackUp
							+ "';Nb of reports to be generated: " + numUsers
							+ ";Nb SUCCESS:"+countMassiveSuccess + ";Nb FAILURE:"+ (numUsers - countMassiveSuccess)
							+ ";Total ExecTime: " + df.format(timeAllMassiveOfOneType) + "(s)");	//change timeAllExec by timeAllMassiveOfOneType
					
							//during massive reports, even if 1 of massive reports fails, then we assume, that type of report failed
							if(numUsers == countMassiveSuccess)
							{
								countSuccess++;
							}
				}	// end of 'if(value.contains("QueryExec=") && value.contains("<QueryExec>") )' i.e. massive reports
				else
				{
					massiveFlag = false;
					generateReport(value, DateFormat);
					// No exception during execution of NON-massive report
					countSuccess++;
				}
			}
			catch(Exception e)
			{
				continue;
			}
			
		}	// end of 'for(Integer numportion:numportionSet){'
		
		//requirement 7 by francis
		/*logger.info("****  End of generation of task '" + generateType
				+ "'  : Nb reports generated:  " + count
				+ "; Total ExecTime : " + timeAllExec + "(s)");
		*/
		//countExisting added for Forge#374803
		logger.info("**** End of execution of task '" + generateType
				+ "';Nb of reports to be generated: " + (countLocal-countExisting)
				+ ";Nb SUCCESS:"+countSuccess + ";Nb FAILURE:"+ (countLocal - countSuccess - countExisting)
				+ ";Nb EXISTING:"+countExisting
				+";Total ExecTime: " + df.format(timeAllExec) + "(s)");
		

	}

	public String calculateDate(String stringToDate, String format) {
		
		//requirement by francis to process below command-line parameters too
		// i.e. date_a=21/05/2014 00:00:00
		// already handled parameters were like following 
		// date_a=sysdate-1-00:00:00

		String heure; 
		Calendar c;
		SimpleDateFormat formatDate;
		if (stringToDate.contains("sysdate") || stringToDate.contains("sysmonth") || stringToDate.contains("sysweek") ) 
		{
			//logger.info("** 1AAA **");

			String stringSplite = stringToDate;
			int inedxInderScor = stringSplite.indexOf("-");
			int inedxh = stringSplite.lastIndexOf("-");
			String nbOfDay = stringSplite.substring(inedxInderScor + 1, inedxh);
			int nbOfDayint = Integer.parseInt(nbOfDay);
			heure = stringSplite.substring(inedxh + 1);
			c = Calendar.getInstance();
			formatDate = new SimpleDateFormat(format);
			
			//c.add(Calendar.DAY_OF_MONTH, -nbOfDayint);
			
			/* 
			 * NMCC5676 - calculate date if date parameter contains sysdate or sysmonth or sysweek
			 */
			
			if(stringToDate.contains("sysdate"))
			{
				c.add(Calendar.DAY_OF_MONTH, -nbOfDayint);
			}
			else if (stringToDate.contains("sysmonth"))
			{
				c.set(Calendar.DAY_OF_MONTH, 1);
				c.add(Calendar.MONTH, -nbOfDayint);
			}
			else if (stringToDate.contains("sysweek"))
			{
				//c.set(Calendar.DAY_OF_MONTH, 1);
				//c.add(Calendar.MONTH, -nbOfDayint);
	
				c.add(Calendar.WEEK_OF_YEAR, -nbOfDayint);
				c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			}
		}	//end of if (stringToDate.contains("sysdate") || stringToDate.contains("sysmonth") || stringToDate.contains("sysweek") )
		else
		{	//date format with hard-coded dates e.g. "date_a=21/05/2014 00:00:00"
			//logger.info("** 1AA **");

			String stringSplite = stringToDate;
			int equalIndex = stringSplite.indexOf("=");
			
			String completeDate = stringSplite.substring(equalIndex+1);
			
			int index1Slash = completeDate.indexOf("/");
			int index2Slash = completeDate.lastIndexOf("/");
			
			//logger.info("** 1AB **");

			int day = Integer.parseInt(completeDate.substring(0,index1Slash));
			//logger.info("** 1AC day:"+day+" **");
			int month = Integer.parseInt(completeDate.substring(index1Slash+1,index2Slash))-1;
			//logger.info("** 1AC month:"+month+" **");
			
			//change to avoid exception, when hard-coded date does not have heure component e.g. date_a=07/02/2014
			int indexSpace = completeDate.indexOf(" ");
			int year;
			//int year = Integer.parseInt(completeDate.substring(index2Slash+1,completeDate.indexOf(" ")));
			if(indexSpace == -1)
			{
				year = Integer.parseInt(completeDate.substring(index2Slash+1));
			}
			else
			{
				year = Integer.parseInt(completeDate.substring(index2Slash+1,indexSpace));
			}
			//logger.info("** 1AC year:"+year+" **");
	
			//change to avoid exception, when hard-coded date does not have heure component e.g. date_a=07/02/2014
			if(indexSpace != -1)
			{
				heure = completeDate.substring(indexSpace+1);
			}
			else
			{
				heure = "";
			}
			
			c = Calendar.getInstance();
			formatDate = new SimpleDateFormat(format);
			//logger.info("** 1AD heure:"+heure+" **");

			c.set(Calendar.DAY_OF_MONTH, day);
			c.set(Calendar.MONTH, month);
			c.set(Calendar.YEAR, year);
		}
		
		//change to avoid exception, when hard-coded date does not have heure component e.g. date_a=07/02/2014
		int hh=0,mm=0,ss=0;
		
		//requirement by francis to set HH:mm:ss from 'heure'
		if(heure.indexOf(":") != heure.lastIndexOf(":") )
		{
			//logger.info("** 1AE **");

			int indexMin = heure.indexOf(":");
			int indexSec = heure.lastIndexOf(":");
			
			hh = Integer.parseInt(heure.substring(0,indexMin));
			mm = Integer.parseInt(heure.substring(indexMin + 1, indexSec));
			ss = Integer.parseInt(heure.substring(indexSec + 1));
			//logger.info("** 1AF hh:"+hh+"mm:"+mm+"ss:"+ss+" **");
		//change to avoid exception, when hard-coded date does not have heure component e.g. date_a=07/02/2014
		}
		
		c.set(Calendar.HOUR_OF_DAY, hh);
		c.set(Calendar.MINUTE, mm);
		c.set(Calendar.SECOND, ss);
		//change to avoid exception, when hard-coded date does not have heure component e.g. date_a=07/02/2014
		//}
		
		String date = formatDate.format(c.getTime());
		
		// change to explicitly remove heure component from return value, 
		//because as an example report.properties tags <StartDate> and <EndDate> do not require heure component
		// otherwise exception in GR-plugin.log that 
		//20141207_OMG_TRANSACTIONS20141207 00:00:00.csv (The filename, directory name, or volume label syntax is incorrect)
		
		//date = date + " " + heure;
		return date;
	}
	
	/**
	 * @date - 26/09/2014
	 * @author NMCC5676
	 * @brief - calculate date if date parameter contains sysmonth
	 */
	public String calculateDateSysmonth(String stringToDate, String format) {
		String stringSplite = stringToDate;
		int inedxInderScor = stringSplite.indexOf("-");
		int inedxh = stringSplite.lastIndexOf("-");
		String nbOfDay = stringSplite.substring(inedxInderScor + 1, inedxh);
		int nbOfDayint = Integer.parseInt(nbOfDay);
		String heure = stringSplite.substring(inedxh + 1);
		Calendar c = Calendar.getInstance();
		SimpleDateFormat formatDate = new SimpleDateFormat(format);
		
		
		//c.add(Calendar.DAY_OF_MONTH, -nbOfDayint);
		c.add(Calendar.DAY_OF_MONTH, 1);
		c.add(Calendar.MONTH, -nbOfDayint);
		
		String date = formatDate.format(c.getTime());
		date = date + " " + heure;
		return date;
	}
	
	/**
	 * @date - 08/07/2014
	 * @author DHQK7762
	 * @brief - remove line separator from the txt files
	 */
	public String removeLineSeparator (String fileName) throws IOException{
		
		File file = new File(fileName);
		String tempFileName=fileName.substring(0,fileName.lastIndexOf('.')) + "temp.csv";
		File tempFile = new File(tempFileName);
		StringBuilder stringBuffer = new StringBuilder();
		String currLine=null;
		FileReader fileReader = null;
		BufferedReader reader = null;
		BufferedWriter writer = null;
		int iterator =0;
		while(!file.exists() && iterator<20){
			try{
				Thread.sleep(5000);
				iterator ++;
			}
			catch(Exception e){}
		}
		if(iterator >= 20){
			logger.error(count + ";" + generateTypeGlobal + ";FAILURE;400;"+targetDirStr + ";" + targetFileStr+";"+ startDateLogging + ";" + endDateLogging+";ExecTime " + df.format((System.currentTimeMillis() - valInit) / 1000) + "(s);"+"Report is not created even after 1 min.");
		}
		try{
			fileReader = new FileReader(file);
			reader = new BufferedReader(fileReader);		
			
			while((currLine=reader.readLine()) != null){
				if(currLine.startsWith("=")){
					continue;
				}
				stringBuffer.append(currLine).append(System.getProperty("line.separator"));				
			}
			reader.close();
		}
		catch(IOException ex){
			ex.printStackTrace();
		}
		
		
		try{
			writer = new BufferedWriter(new FileWriter(tempFile,false));
			writer.write(stringBuffer.toString());
			writer.flush();
			writer.close();
		}
		catch(IOException e){
			logger.error(count + ";" + generateTypeGlobal + ";FAILURE;400;"+targetDirStr + ";" + targetFileStr+";"+ startDateLogging + ";" + endDateLogging+";ExecTime " + df.format((System.currentTimeMillis() - valInit) / 1000) + "(s);"+e   );
			throw e;
		}
		
		file.delete();
		tempFile.renameTo(new File(fileName));
		return fileName;
	}
	
	
	
	
	public void sendEmail(File fileToSend) throws Exception {

		String recipientId="";String subjectText="";String contentText=""; String copyRecipientId=""; String blindCopyRecipientId="";
		String urlFileProperties = ConstanteGR.getUrlfileproperties();
		Properties p = GRUtils.loadProperties(urlFileProperties);
		String senderId = p.getProperty("email.sender.id");
		String host = p.getProperty("email.host.ip");
		//logger.info(" * CA ");

		if(!emailParamFlag)	//emailparam properties is NOT specified in command-line
		{	
			recipientId = p.getProperty("email.recipient.id");
			subjectText = p.getProperty("email.subject.text");
			contentText = p.getProperty("email.content.text");
			copyRecipientId = p.getProperty("email.copyrecipient.id");
			
			blindCopyRecipientId = p.getProperty("email.blindcopyrecipient.id");
		}
		else
		{
			recipientId = emailRecipients;
			subjectText = emailSubject;
			contentText = emailContent;
			copyRecipientId = emailCopyRecipients;
			
			blindCopyRecipientId = emailBlindCopyRecipients;
		}
	
		//logger.info(" * CB ");

	    Properties properties = System.getProperties();
		//logger.info(" * CC ");

	    properties.setProperty("mail.smtp.host", host);
	    Session session = Session.getDefaultInstance(properties);
		//logger.info(" * CD ");

	         MimeMessage message = new MimeMessage(session);
	         message.setFrom(new InternetAddress(senderId));
	         
	         //process multiple recipient IDs
	         String[] recipientIds = recipientId.split(",");
	         for (int x=0; x<recipientIds.length; x++)
	         {
	        	 message.addRecipient(Message.RecipientType.TO,new InternetAddress(recipientIds[x]));
	         }
	 		//logger.info(" * CE ");

	 		
	         //process multiple copy recipient IDs
	         //put following 'if', to avoid 300 error in email transfer :java.lang.NullPointerException
	 		if(copyRecipientId != null && !copyRecipientId.equals(""))
	 		{	
		 		 String[] copyRecipientIds = copyRecipientId.split(",");
		         for (int x=0; x<copyRecipientIds.length; x++)
		         {
		        	 message.addRecipient(Message.RecipientType.CC,new InternetAddress(copyRecipientIds[x]));
		         }
	 		}

	 		//process multiple blind copy recipient IDs
	         //put following 'if', to avoid 300 error in email transfer :java.lang.NullPointerException
	 		if(blindCopyRecipientId != null && !blindCopyRecipientId.equals(""))
	 		{
		         String[] blindCopyRecipientIds = blindCopyRecipientId.split(",");
		         for (int x=0; x<blindCopyRecipientIds.length; x++)
		         {
		        	 message.addRecipient(Message.RecipientType.BCC,new InternetAddress(blindCopyRecipientIds[x]));
		         }
	 		}
	         
	         message.setSubject(subjectText);
	         
	 		//logger.info(" * CF ");

	         BodyPart messageBodyPart = new MimeBodyPart();
	         //change to send email in html format , and NOT plain text
	         //messageBodyPart.setText(contentText);
	         messageBodyPart.setContent(contentText, "text/html");

	         
	         
	         // Create a multipar message
	         Multipart multipart = new MimeMultipart();
	         multipart.addBodyPart(messageBodyPart);
	 		//logger.info(" * CG ");

	         // Part two is attachment
	         messageBodyPart = new MimeBodyPart();
	         String filename = fileToSend.toString();
	         DataSource source = new FileDataSource(filename);
	         messageBodyPart.setDataHandler(new DataHandler(source));
	         messageBodyPart.setFileName(fileToSend.getName());
	         multipart.addBodyPart(messageBodyPart);
	 		//logger.info(" * CH ");

	         // Send the complete message parts
	         message.setContent(multipart );
	 		//logger.info(" * CI ");

	         // Send message
	         Transport.send(message);
	        // System.out.println("Sent message successfully....");	
	 		//logger.info(" * CJ ");

	}

	public void ftp(File fileToSend) throws Exception {
		String ftpIp = "";String ftpPort = "";String ftpUser="";String ftpPassword ="";
		String urlFileProperties = ConstanteGR.getUrlfileproperties();
		Properties p = GRUtils.loadProperties(urlFileProperties);
		
		//coincidentally, params from specific ftp param file like GR-FTP\\InterOpTransfers.properties are captured into ftpIpGlobal etc.
		// and, params from GR-Plugin.properties are captured into -- p.getProperty("ftp.target.ip") -- etc.
		
		if(!ftpParamFlag)	//'FTPParam=' property is NOT specified in command-line, hence from "GR-Plugin.properties"
		{
			ftpIp = p.getProperty("ftp.target.ip");
			ftpPort = p.getProperty("ftp.target.port");
			ftpUser= p.getProperty("ftp.client.user");
			ftpPassword = p.getProperty("ftp.client.password");
			
			//requirement:21-sep-2015 the password must be hidden
			ftpPassword = AESencrp.decrypt(ftpPassword);
		}
		else	//from a particular ftp param file specified after 'FTPParam=' property. e.g. GR-FTP\\InterOpTransfers.properties
		{
			ftpIp = ftpIpGlobal;
			ftpPort = ftpPortGlobal;
			ftpUser= ftpUserGlobal;
			ftpPassword = ftpPasswordGlobal;
		}
		
		FTPClient ftpClient = new FTPClient();
		ftpClient.connect(ftpIp, Integer.parseInt(ftpPort));
		ftpClient.login(ftpUser, ftpPassword);
		ftpClient.enterLocalPassiveMode();
		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		
		File firstLocalFile = fileToSend;
		String firstRemoteFile = fileToSend.getName();
		InputStream inputStream = new FileInputStream(firstLocalFile);
				boolean done = ftpClient.storeFile(firstRemoteFile, inputStream);
		inputStream.close();
		/*
		if (done) {
			System.out.println("The first file is uploaded successfully.");
		}
		*/
	}
	
	
	private void computeActualLagJdbc() throws Exception
	{
		ConnetJdbcDbref jdbcDbref = new ConnetJdbcDbref();
		//logger.info("checkLagSQLPath :"+checkLagSQLPath);
		
		List<String> resultats = jdbcDbref.getNewListUserChannel(checkLagSQLPath);	
		String lagRS = resultats.get(0);	//e.g.	"11/05/2015 06:21:03;+00 00:00:00"
		
		//logger.info("lagRS :"+lagRS);

		if(lagRS == null || lagRS.equals(""))
		{
			throw new Exception("DBref does Not define LAG value");
		}
		
		
		int indexEndOfLAG = -1;
		String LAG = "";
		int indexOfLAG = lagRS.indexOf("LAG=");
		
		if(indexOfLAG != -1)	// pattern 'LAG=' exists in commandParams
		{
			indexEndOfLAG =  lagRS.indexOf(";",indexOfLAG);
			if(indexEndOfLAG == -1)	//char ';' does not exist at end of value of LOCATION parameter
			{
				LAG = lagRS.substring(indexOfLAG+4);
			}
			else
			{
				LAG = lagRS.substring(indexOfLAG+4, indexEndOfLAG);
			}
		}
		//LAG contains value like '11/05/2015 06:21:03;+00 00:00:00'
		
		int indexOfHash = LAG.indexOf('^');
		String lagPortion = LAG.substring(indexOfHash+1);	//lagportion value is like '+00 00:00:00'
		
		//logger.info("checkLagSQLPath :"+checkLagSQLPath);

		
		if(lagPortion !=  null && !lagPortion.equals("") && lagPortion.contains(":"))
		{
			int indexOfPlus = lagPortion.indexOf('+');
			
			
			lagDaysStr =  lagPortion.substring(indexOfPlus+1, indexOfPlus+3);
			lagHourStr = lagPortion.substring(indexOfPlus+4, indexOfPlus+6);
			lagMinStr = lagPortion.substring(indexOfPlus+7, indexOfPlus+9);
			lagSecStr = lagPortion.substring(indexOfPlus+10, indexOfPlus+12);
			
			lagDays = Integer.parseInt(lagDaysStr);
			lagHour = Integer.parseInt(lagHourStr );
			lagMin = Integer.parseInt(lagMinStr );
			lagSec = Integer.parseInt(lagSecStr );
			
			actualLagSeconds=  lagDays*86400 + lagHour*3600 + lagMin*60  + lagSec;
		}
		else
		{
			throw new Exception("DBref does Not define LAG value correctly in required format +DD HH:MM:SS");
		}
	}
	
	private void lagSecondsSinceMidnight()
	{
		Calendar cal = Calendar.getInstance();
		checkLagHour = cal.get(Calendar.HOUR_OF_DAY);
		checkLagMin = cal.get(Calendar.MINUTE);
		checkLagSec = cal.get(Calendar.SECOND);
		
		checkLagTotalSeconds =  checkLagHour*3600 + checkLagMin*60  + checkLagSec;
	}
	
	private void computeAmbiguousTransferFlag() throws Exception
	{
		ConnetJdbcDbref jdbcDbref = new ConnetJdbcDbref();
		List<String> resultats = jdbcDbref.getNewListUserChannelMysql(ambiguousCountSqlAbsolutePath, mysqlDbSrc);
		
		String countAmbiguousRS = resultats.get(0);	//e.g.	";count=1"
		
		//logger.info(" ** ambiguousCountSqlAbsolutePath="+ambiguousCountSqlAbsolutePath+", mysqlDbSrc="+ mysqlDbSrc);
		//String countAmbiguousRS = ";count=1";
		
		if(countAmbiguousRS == null || countAmbiguousRS.equals(""))
		{
			throw new Exception("Count of ambiguous transfers could Not be fetched by DB");
		}
		
		
		int indexEndOfCOUNT = -1;
		int COUNT = 0;
		int indexOfCOUNT = countAmbiguousRS.indexOf("count=");
		
		if(indexOfCOUNT != -1)	// pattern 'count=' exists in resultset
		{
			indexEndOfCOUNT =  countAmbiguousRS.indexOf(";",indexOfCOUNT);
			if(indexEndOfCOUNT == -1)	//char ';' does not exist at end of value of LOCATION parameter
			{
				COUNT = Integer.parseInt(countAmbiguousRS.substring(indexOfCOUNT+6));
			}
			else
			{
				COUNT = Integer.parseInt(countAmbiguousRS.substring(indexOfCOUNT+6, indexEndOfCOUNT));
			}
		}	
		
		if(COUNT > 0)
		{
			ambiguousTransferFlag = true;
		}
	}
	
	public static void main(String args[])
	{
		GenerateReportUserAll gall = new GenerateReportUserAll();
		System.out.println(gall.calculateDate("startDate=sysdate-0-00:00:00" , "dd/MM/yy HH:mm:ss"));
		
		//System.out.println();
	}

}
