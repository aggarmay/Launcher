package fr.orange.log;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

import fr.orange.utils.ConstanteGR;
import fr.orange.utils.GRUtils;


public class DailyRollingCustomisedAppender extends DailyRollingFileAppender{

	/**
    All files will be backed up by default.
	*/
 protected static int maxBackupIndex  = -1;

 static{
	 
	 String urlFilePropertiesGlobal = ConstanteGR.getUrlfileproperties();
	 Properties prop = GRUtils.loadProperties(urlFilePropertiesGlobal);	 
	 maxBackupIndex = Integer.parseInt(prop.getProperty("global.logpolicy.days"));
 }
 
 protected boolean deleteStatus = false;
	 /**
    The default constructor does nothing. */
 public DailyRollingCustomisedAppender() {
	 super();
 }

 /**
   Instantiate a <code>DailyRollingFileAppender</code> and open the
   file designated by <code>filename</code>. The opened filename will
   become the ouput destination for this appender.

   */
 public DailyRollingCustomisedAppender (Layout layout, String filename,
				   String datePattern) throws IOException {
   super(layout, filename, datePattern);
 }
 
 protected void logArchiveMaintenance(int maxBackupIndex){
	 File file;
	 Calendar cal = Calendar.getInstance();
	 cal.add(cal.DATE,(0-maxBackupIndex));
	 SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");
	 String date = dateFormat.format(cal.getTime());
	 
	 
	 file = new File(fileName + '.' + date);

	 if(file.exists()){
		  deleteStatus = file.delete();
	 }
	 
 }
 
 @Override
 protected void subAppend(LoggingEvent event) {
	    super.subAppend(event);
	    if (maxBackupIndex > 0){
	    	logArchiveMaintenance(maxBackupIndex);
	    }
 }

}
