/**
 * 
 */
package fr.orange.actions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.inet.report.schedule.ExecutedTask;
import com.inet.report.schedule.ScheduleAction;

import fr.orange.metier.GenerateReportUserAll;
import fr.orange.utils.LuncherInfo;

/**
 * @author DLWP2128
 * 
 */
public class ReportUserDaily6_Action extends ScheduleAction {
	private Calendar cal;
	private DateFormat dateFormat;

	public void action(ExecutedTask arg0) {

		GenerateReportUserAll reportUser = new GenerateReportUserAll();
		LuncherInfo luncherInfo = new LuncherInfo();
		String foramtDate = luncherInfo.getDailyDateFormat();
		dateFormat = new SimpleDateFormat(foramtDate);
		cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		String dateString = dateFormat.format(cal.getTime());
		reportUser.generateReportAll("Run_19", dateString);
	}

}
