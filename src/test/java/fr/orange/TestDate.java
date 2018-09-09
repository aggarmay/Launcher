/**
 * 
 */
package fr.orange;

import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * @author DLWP2128
 *
 */
public class TestDate {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//		Calendar cal = Calendar.getInstance();
//		String enddateString = dateFormat.format(cal.getTime());
//		System.out.println(enddateString);
		String stringToDate="sysdate-2-14:20:22";
		TestDate testDate=new TestDate();
		testDate.calculateDate(stringToDate, "dd/MM/yyyy");
	}

	public String calculateDate(String stringToDate, String format) {
		String stringSplite = stringToDate;
		int inedxInderScor = stringSplite.indexOf("-");
		int inedxh = stringSplite.lastIndexOf("-");
		String nbOfDay = stringSplite.substring(inedxInderScor+1, inedxh);
		int nbOfDayint= Integer.parseInt(nbOfDay);
		
		String heure=stringSplite.substring(inedxh+1);
		System.out.println("Number of day : " + nbOfDay);
		System.out.println("La valeur heure "+heure);
		Calendar c = Calendar.getInstance();
		SimpleDateFormat formatDate = new SimpleDateFormat(format);
		c.add(Calendar.DAY_OF_MONTH, -nbOfDayint);
		String date=formatDate.format(c.getTime());
		date=date+" "+heure;
		System.out.println("date final "+date);
		return date;
	}
}
