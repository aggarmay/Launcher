/**
 * 
 */
package fr.orange.actions;

import com.inet.report.schedule.ExecutedTask;
import com.inet.report.schedule.ScheduleAction;

import fr.orange.metier.GenerateReportUserAll;
import fr.orange.utils.LuncherInfo;

/**
 * @author DLWP2128
 * 
 */
public class ReportUserMonthly0_Action extends ScheduleAction {

		public void action(ExecutedTask arg0) {
		GenerateReportUserAll reportUser = new GenerateReportUserAll();
		LuncherInfo luncherInfo = new LuncherInfo();
		String foramtDate = luncherInfo.getMonthlyDateFormat();

		String dateString = luncherInfo.caculateDate(foramtDate);
		reportUser.generateReportAll("Run_13", dateString);
	}
}
