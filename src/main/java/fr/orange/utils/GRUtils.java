/**
 * 
 */
package fr.orange.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.File;
import java.io.InputStream;

import org.apache.log4j.Logger;

/**
 * @author DLWP2128
 * 
 */

public class GRUtils {

	private static Logger logger = Logger.getLogger(GRUtils.class);

	public GRUtils() {
		super();
		// TODO Auto-generated constructor stub
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

	public static Properties loadProperties(String urlFileProperties) {
		BufferedReader input = null;
		try {
			//f = new FileInputStream(urlFileProperties);
			input =  new BufferedReader(new FileReader(urlFileProperties)); 
		} catch (FileNotFoundException e) {
			logger.error("FileNotFoundException", e);
		}
		
		Properties p = new Properties();
		try {
			p.load(input);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("IOException ", e);
		}
		return p;
	}

	
	public static Properties loadPropertiesNatural(String urlFileProperties) {
        File file = null;
        InputStream inputStream = null;
        LinkedProperties linkedProperties = null;
        try
        {
        	file = new File(urlFileProperties);
        	inputStream = 	new FileInputStream(file);
        	linkedProperties = new LinkedProperties();
        	linkedProperties.load(inputStream);
		}
        catch (IOException e) {
			logger.error("IOException ", e);
		}
		return linkedProperties;
	}
	
	public static String getFormatDate() {
		Format format = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
		Date date = new Date();
		return format.format(date);

	}

	public static String correcteNameFile(String nameFile) {

		if (nameFile.contains(" ")) {

			return nameFile.replace(" ", "(20)");
		}
		if (nameFile.contains("/")) {
			return nameFile.replace("/", "(2F)");
		}
		if (nameFile.contains(";")) {
			return nameFile.replace(";", "(3B)");
		}
		if (nameFile.contains("*")) {
			return nameFile.replace("*", "(2A)");
		}
		if (nameFile.contains("?")) {
			return nameFile.replace("?", "(2C)");
		}

		if (nameFile.contains(">")) {
			return nameFile.replace(">", "(3E)");
		}
		if (nameFile.contains("<")) {
			return nameFile.replace("<", "(3C)");
		}
		if (nameFile.contains(":")) {
			return nameFile.replace(":", "(3A)");
		}
		if (nameFile.contains("\\")) {
			return nameFile.replace("\\", "(5C)");
		}
		if (nameFile.contains("'")) {
			return nameFile.replace("'", "(60)");
		}

		if (nameFile.contains("\t")) {
			return nameFile.replace("\t", "(09)");
		}

		return nameFile;
	}

	public static String translate(String src, String chars, String replace) {
		StringBuffer result = new StringBuffer();
		if (src != null && src.length() != 0) {
			int index = -1;
			char c = (char) 0;
			for (int i = 0; i < src.length(); i++) {
				c = src.charAt(i);
				if ((index = chars.indexOf(c)) != -1)
					result.append(replace.charAt(index));
				else
					result.append(c);
			}
		}
		;
		return result.toString();
	}

}
