/**
 * 
 */
package fr.orange.main;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.inet.report.schedule.ScheduleException;

import fr.orange.metier.GenerateReportUserAll;
import fr.orange.utils.LuncherInfo;

/**
 * @author YACINE Rafik
 * 
 */
public class GenerateReportAllMain {

	/**
	 * 
	 */
	public GenerateReportAllMain() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 * @throws ScheduleException
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */

	public static void main(String[] args) throws ScheduleException,
			SQLException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		// TODO Auto-generated method stub
		Calendar cal;
		DateFormat dateFormat;
		
		GenerateReportUserAll reportUser = new GenerateReportUserAll();
		LuncherInfo luncherInfo = new LuncherInfo();
		String foramtDate = luncherInfo.getHourlyDateFormat();
		dateFormat = new SimpleDateFormat(foramtDate);
		cal = Calendar.getInstance();

		String dateString = dateFormat.format(cal.getTime());
		
		reportUser.generateReportAll("report.daily_0", dateString);
		
		/*GenerateReportUserAll reportUser = new GenerateReportUserAll();
		LuncherInfo luncherInfo = new LuncherInfo();
		String foramtDate = luncherInfo.getMonthlyDateFormat();

		String dateString = luncherInfo.caculateDate(foramtDate);
		System.out.println("dateString "+dateString);
		
		
		reportUser.generateReportAll("report.monthly_1", dateString);*/
	}

}
