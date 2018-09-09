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
public class regenerate04_Action extends ScheduleAction {
	private Calendar cal;
	private DateFormat dateFormat;

	public void action(ExecutedTask arg0) {
		GenerateReportUserAll reportUser = new GenerateReportUserAll();
		LuncherInfo luncherInfo = new LuncherInfo();
		String foramtDate = luncherInfo.getHourlyDateFormat();
		dateFormat = new SimpleDateFormat(foramtDate);
		cal = Calendar.getInstance();

		String dateString = dateFormat.format(cal.getTime());

		//reportUser.generateReportAll(".*RegenMark=[1234,&&[^;]]*4[,$;]*.*", dateString);
		reportUser.generateReportAll("Regenerate_4", dateString);

	}

}
