/**
 * 
 */
package fr.orange;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author DLWP2128
 * 
 */
public class DateGenerateReport {

	/**
	 * @param args
	 */
	private String startDate, endDate;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DateGenerateReport dateGenerateReport = new DateGenerateReport();
		dateGenerateReport.caculateDate("current");

	}

	private void caculateDate(String timeslotTmp) {
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Calendar cal = Calendar.getInstance();

		startDate = dateFormat.format(cal.getTime());
		dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		String[] timeslotT = { "current", "daily", "weekly", "monthly" };

		for (int i = 0; i < timeslotT.length; i++) {
			String timeslot = timeslotT[i];

			System.out.println(timeslot + " startDate " + startDate);
			if (timeslot.equals("daily")) {
				cal.add(Calendar.DATE, -1);
			}
			if (timeslot.equals("weekly")) {
				cal.add(Calendar.WEEK_OF_MONTH, -1);
			}
			if (timeslot.equals("monthly")) {
				cal.add(Calendar.MONTH, -1);
			}
			endDate = dateFormat.format(cal.getTime())+ " 00:00:00";
			System.out.println(timeslot + " endDate " + endDate);
		}
	}

	public static String subDate(String mois, String day) {
		int moisInt = Integer.parseInt(mois);
		int dayInt = Integer.parseInt(day);

		Calendar cal = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
		cal.add(Calendar.MONTH, moisInt);
		cal.add(Calendar.DATE, dayInt);
		return dateFormat.format(cal.getTime());

	}

}
