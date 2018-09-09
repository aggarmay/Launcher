/**
 * 
 */
package fr.orange;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author DLWP2128
 *
 */
public class RunSqlFromFile {

	/**
	 * @param args
	 * @throws SQLException 
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws SQLException, ParseException {
		
		RunSqlFromFile sqlFromFile=new RunSqlFromFile();
		sqlFromFile.stringToDate("2012-06-12 10:45:56.355");
		
	}
    
	public Date stringToDate(String lastTransON) throws ParseException{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Date d = sdf.parse(lastTransON);
		System.out.println("la valeur lastTransON "+d);
		return d;
	}
  
	
}
