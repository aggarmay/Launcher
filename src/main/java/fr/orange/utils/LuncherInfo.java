package fr.orange.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

public class LuncherInfo {

	// singleton LuncherInfo
	private LuncherInfo luncherInfo = null;
	private String Sourcepath, sqlpath;
	private String tagetpath, sysdateFormat,startdateFormat, curdateFormat,curdatetimeFormat,prevdayFormat,prevweekFormat,prevmonthFormat,prevweeknumberFormat,prevmonthnumberFormat,prevmonthnameFormat;
	// RegenMark functionality 
	private String regen01StartDate, regen01EndDate,regen02StartDate, regen02EndDate,regen03StartDate, regen03EndDate,regen04StartDate, regen04EndDate;
	private String hourlyDateFormat, dailyDateFormat, weeklyDateFormat,
			monthlyDateFormat;

	/**
	 * @return the hourlyDateFormat
	 */
	public String getHourlyDateFormat() {
		return hourlyDateFormat;
	}

	/**
	 * @param hourlyDateFormat
	 *            the hourlyDateFormat to set
	 */
	public void setHourlyDateFormat(String hourlyDateFormat) {
		this.hourlyDateFormat = hourlyDateFormat;
	}

	/**
	 * @return the dailyDateFormat
	 */
	public String getDailyDateFormat() {
		return dailyDateFormat;
	}

	/**
	 * @param dailyDateFormat
	 *            the dailyDateFormat to set
	 */
	public void setDailyDateFormat(String dailyDateFormat) {
		this.dailyDateFormat = dailyDateFormat;
	}

	/**
	 * @return the weeklyDateFormat
	 */
	public String getWeeklyDateFormat() {
		return weeklyDateFormat;
	}

	/**
	 * @param weeklyDateFormat
	 *            the weeklyDateFormat to set
	 */
	public void setWeeklyDateFormat(String weeklyDateFormat) {
		this.weeklyDateFormat = weeklyDateFormat;
	}

	/**
	 * @return the monthlyDateFormat
	 */
	public String getMonthlyDateFormat() {
		return monthlyDateFormat;
	}

	/**
	 * @param monthlyDateFormat
	 *            the monthlyDateFormat to set
	 */
	public void setMonthlyDateFormat(String monthlyDateFormat) {
		this.monthlyDateFormat = monthlyDateFormat;
	}

	private Properties propeties;

	/**
	 * @return the propeties
	 */
	public Properties getPropeties() {
		return propeties;
	}

	/**
	 * @param propeties
	 *            the propeties to set
	 */
	public void setPropeties(Properties propeties) {
		this.propeties = propeties;
	}

	//requirement by mayank: 30-sep-2015: process commands in reports.properties, in the order they appear in file
	private Properties propetiesNatural;

	public Properties getPropetiesNatural() {
		return propetiesNatural;
	}

	public void setPropetiesNatural(Properties propeties) {
		this.propetiesNatural = propeties;
	}
	// ends
	
	public LuncherInfo() {
		//String path = "C:\\GR\\GR-Properties\\";
		String path = "C:\\Program Files (x86)\\i-net Clear Reports\\GR-Properties\\";
		String urlFileProperties = path + "Reports.properties";
		propeties = GRUtils.loadProperties(urlFileProperties);
		
		//requirement by mayank: 30-sep-2015: process commands in reports.properties, in the order they appear in file
		propetiesNatural = GRUtils.loadPropertiesNatural(urlFileProperties);
		
		Sourcepath = propeties.getProperty("fr.orange.report.path");
		sqlpath = propeties.getProperty("fr.orange.report.sql.path");
		
		tagetpath = propeties.getProperty("fr.orange.report.target.path");
		hourlyDateFormat = propeties
				.getProperty("fr.orange.date.format.current");
		dailyDateFormat = propeties.getProperty("fr.orange.date.format.daily");
		weeklyDateFormat = propeties
				.getProperty("fr.orange.date.format.weekly");
		monthlyDateFormat = propeties
				.getProperty("fr.orange.date.format.monthly");
		sysdateFormat = propeties
				.getProperty("fr.orange.parameter.sysdate.format");
		startdateFormat = propeties
				.getProperty("fr.orange.date.format.StartDate");
		
		curdateFormat = propeties.getProperty("fr.orange.date.format.CurDate");		
		curdatetimeFormat = propeties.getProperty("fr.orange.date.format.CurDateTime");
		prevdayFormat = propeties.getProperty("fr.orange.date.format.PrevDay");
		prevweekFormat = propeties.getProperty("fr.orange.date.format.PrevWeek");
		prevmonthFormat = propeties.getProperty("fr.orange.date.format.PrevMonth");
		prevweeknumberFormat = propeties.getProperty("fr.orange.date.format.PrevWeekNumber");
		prevmonthnumberFormat = propeties.getProperty("fr.orange.date.format.PrevMonthNumber");
		prevmonthnameFormat = propeties.getProperty("fr.orange.date.format.PrevMonthName");

		// RegenMark functionality 
		regen01StartDate = propeties.getProperty("fr.orange.report.regenerate01.startDate");
		regen01EndDate = propeties.getProperty("fr.orange.report.regenerate01.endDate");
		regen02StartDate = propeties.getProperty("fr.orange.report.regenerate02.startDate");
		regen02EndDate = propeties.getProperty("fr.orange.report.regenerate02.endDate");
		regen03StartDate = propeties.getProperty("fr.orange.report.regenerate03.startDate");
		regen03EndDate = propeties.getProperty("fr.orange.report.regenerate03.endDate");
		regen04StartDate = propeties.getProperty("fr.orange.report.regenerate04.startDate");
		regen04EndDate = propeties.getProperty("fr.orange.report.regenerate04.endDate");
	}

	public String getCurdateFormat() {
		return curdateFormat;
	}
	
	public void setCurdateFormat(String curdateFormat) {
		this.curdateFormat = curdateFormat;
	}
	
	public String getCurdatetimeFormat() {
		return curdatetimeFormat;
	}
	
	public void setCurdatetimeFormat(String curdatetimeFormat) {
		this.curdatetimeFormat = curdatetimeFormat;
	}	
	
	public String getPrevdayFormat() {
		return prevdayFormat;
	}
	
	public void setPrevdayFormat(String prevdayFormat) {
		this.prevdayFormat = prevdayFormat;
	}
	
	
	public String getPrevweekFormat() {
		return prevweekFormat;
	}
	
	public void setPrevweekFormat(String prevweekFormat) {
		this.prevweekFormat = prevweekFormat;
	}
	
	
	public String getPrevmonthFormat() {
		return prevmonthFormat;
	}
	
	public void setPrevmonthFormat(String prevmonthFormat) {
		this.prevmonthFormat = prevmonthFormat;
	}
	
	
	public String getPrevweeknumberFormat() {
		return prevweeknumberFormat;
	}
	
	public void setPrevweeknumberFormat(String prevweeknumberFormat) {
		this.prevweeknumberFormat = prevweeknumberFormat;
	}
	
	
	public String getPrevmonthnumberFormat() {
		return prevmonthnumberFormat;
	}
	
	public void setPrevmonthnumberFormat(String prevmonthnumberFormat) {
		this.prevmonthnumberFormat = prevmonthnumberFormat;
	}
	
	
	public String getPrevmonthnameFormat() {
		return prevmonthnameFormat;
	}
	
	public void setPrevmonthnameFormat(String prevmonthnameFormat) {
		this.prevmonthnameFormat = prevmonthnameFormat;
	}
	
	public String getRegen01StartDate() {
		return regen01StartDate;
	}
	
	public void setRegen01StartDate(String regen01StartDate) {
		this.regen01StartDate = regen01StartDate;
	}
	
	public String getRegen02StartDate() {
		return regen02StartDate;
	}
	
	public void setRegen02StartDate(String regen02StartDate) {
		this.regen02StartDate = regen02StartDate;
	}
	
	public String getRegen03StartDate() {
		return regen03StartDate;
	}
	
	public void setRegen03StartDate(String regen03StartDate) {
		this.regen03StartDate = regen03StartDate;
	}
	
	public String getRegen04StartDate() {
		return regen04StartDate;
	}
	
	public void setRegen04StartDate(String regen04StartDate) {
		this.regen04StartDate = regen04StartDate;
	}
	
	public String getRegen01EndDate() {
		return regen01EndDate;
	}
	
	public void setRegen01EndDate(String regen01EndDate) {
		this.regen01EndDate = regen01EndDate;
	}
	
	public String getRegen02EndDate() {
		return regen02EndDate;
	}
	
	public void setRegen02EndDate(String regen02EndDate) {
		this.regen02EndDate = regen02EndDate;
	}
	
	public String getRegen03EndDate() {
		return regen03EndDate;
	}
	
	public void setRegen03EndDate(String regen03EndDate) {
		this.regen03EndDate = regen03EndDate;
	}
	
	public String getRegen04EndDate() {
		return regen04EndDate;
	}
	
	public void setRegen04EndDate(String regen04EndDate) {
		this.regen04EndDate = regen04EndDate;
	}
	
	public LuncherInfo getLuncherInfo() {
		return luncherInfo;
	}

	public void setLuncherInfo(LuncherInfo luncherInfo) {
		this.luncherInfo = luncherInfo;
	}

	public String getSourcepath() {
		return Sourcepath;
	}
	
	public String getSqlpath() {
		return sqlpath;
	}

	public void setSourcepath(String sourcepath) {
		Sourcepath = sourcepath;
	}
	
	public void setSqlpath(String sqlpathlocal) {
		sqlpath = sqlpathlocal;
	}
	
	public String getTagetpath() {
		return tagetpath;
	}

	public void setTagetpath(String tagetpath) {
		this.tagetpath = tagetpath;
	}

	public String caculateDate(String Format) {
		Date date = new Date();
		Calendar c = Calendar.getInstance();
		SimpleDateFormat formatDateJour = new SimpleDateFormat(Format);
		c.setTime(date);

		// on se place au premier jour du mois en cours
		//c.set(Calendar.DAY_OF_MONTH, c.getActualMinimum(Calendar.DAY_OF_MONTH));
		// Date debutMois = c.getTime();

		// premier jour du mois en cours moins un jour = dernier jour du mois
		// précédent
		//c.add(Calendar.DAY_OF_MONTH, -1);
		//Date finMoisPrecedent = c.getTime();

		// on était au dernier jour du mois précédent, on se place maintenant au
		// premier jour du mois précédent
		//c.set(Calendar.DAY_OF_MONTH, c.getActualMinimum(Calendar.DAY_OF_MONTH));
		//Date debutMoisPrecedent = c.getTime();

		/**
		 * @date 08/07/2014
		 * @author DHQK7762
		 * @brief set the first day of the previous month
		 */
		c.set(Calendar.DATE, 1);
		c.add(Calendar.DAY_OF_MONTH, -1);
		c.set(Calendar.DATE, 1);
		Date firstDateOfPreviousMonth = c.getTime();

		String enddateString = formatDateJour.format(firstDateOfPreviousMonth);

		return enddateString;
	}

	/**
	 * @return the sysdateFormat
	 */
	public String getSysdateFormat() {
		return sysdateFormat;
	}
	
	
	public String getStartdateFormat() {
		return startdateFormat;
	}

	/**
	 * @param sysdateFormat
	 *            the sysdateFormat to set
	 */
	public void setSysdateFormat(String sysdateFormat) {
		this.sysdateFormat = sysdateFormat;
	}
	
	public void setStartdateFormat(String startdateFormat) {
		this.startdateFormat = startdateFormat;
	}

}
