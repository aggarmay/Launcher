/**
 * 
 */
package fr.orange.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.inet.report.config.Configuration;
import com.inet.report.config.ConfigurationManager;

import fr.orange.utils.ConstanteGR;
import fr.orange.utils.GRUtils;

/**
 * @author DLWP2128
 * 
 */
public abstract class AbstratGenerateReport {

	protected static Properties propertiesGR;
	protected static Properties propertiesConf;
	private static Logger logger = Logger
			.getLogger(AbstratGenerateReport.class);

	protected void getProperties() {
		propertiesGR = GRUtils.loadProperties(ConstanteGR
				.getUrlfileproperties());

	}

	protected void setConfig() {
		logger.info("Début de Set config");
		String urlDefaultProperties = propertiesGR
				.getProperty("url.inet.clear.report.default.config");
		propertiesConf = GRUtils.loadProperties(urlDefaultProperties);
		ConfigurationManager cfg = ConfigurationManager.getInstance();
		Configuration configuration = cfg.getCurrent();
		configuration.clear();
		cfg.setTemporaryProperties(propertiesConf);
	}

	public static String createQueries(String path) {
		String queryLine, query = null;
		StringBuffer sBuffer = new StringBuffer();

		try {
			//to debug the issue of changed characters in value of LOCATION due to some charset issue
			/* FileReader fr = new FileReader(new File(path));
			BufferedReader br = new BufferedReader(fr);
			*/
			Charset charset = Charset.forName("UTF-8");
			
			BufferedReader br = Files.newBufferedReader(Paths.get(path), charset);

			// read the SQL file line by line
			while ((queryLine = br.readLine()) != null) {
				//Commenting below code as Forge#368712 does not want text written after # in query to be considered as comment.
				// ignore comments beginning with #
				/*int indexOfCommentSign = queryLine.indexOf('#');
				if (indexOfCommentSign != -1) {
					if (queryLine.startsWith("#")) {
						queryLine = new String("");
					} else
						queryLine = new String(queryLine.substring(0,
								indexOfCommentSign - 1));
				}*/
				// ignore comments beginning with --
				int indexOfCommentSign = queryLine.indexOf("--");
				if (indexOfCommentSign != -1) {
					if (queryLine.startsWith("--")) {
						queryLine = new String("");
					} else
						queryLine = new String(queryLine.substring(0,
								indexOfCommentSign - 1));
				}
				// ignore comments surrounded by /* */
				indexOfCommentSign = queryLine.indexOf("/*");
				if (indexOfCommentSign != -1) {
					if (queryLine.startsWith("/*")) {
						queryLine = new String("");
					} else
						queryLine = new String(queryLine.substring(0,
								indexOfCommentSign - 1));

					sBuffer.append(queryLine + " ");
					// ignore all characters within the comment
					do {
						queryLine = br.readLine();
					} while (queryLine != null && !queryLine.contains("*/"));
					indexOfCommentSign = queryLine.indexOf("*/");
					if (indexOfCommentSign != -1) {
						if (queryLine.endsWith("*/")) {
							queryLine = new String("");
						} else
							queryLine = new String(queryLine.substring(
									indexOfCommentSign + 2,
									queryLine.length() - 1));
					}
				}

				// the + " " is necessary, because otherwise the content before
				// and after a line break are concatenated
				// like e.g. a.xyz FROM becomes a.xyzFROM otherwise and can not
				// be executed
				if (queryLine != null)
					sBuffer.append(" " + queryLine.trim());
			}
			br.close();

			// here is our splitter ! We use ";" as a delimiter for each request

			query = sBuffer.toString();
		} catch (Exception e) {

			logger.error("Error : ", e);
		}
		return query;
	}

	/**
	 * @return the propertiesGR
	 */
	protected static Properties getPropertiesGR() {
		return propertiesGR;
	}

	/**
	 * @param propertiesGR
	 *            the propertiesGR to set
	 */
	protected static void setPropertiesGR(Properties propertiesGR) {
		AbstratGenerateReport.propertiesGR = propertiesGR;
	}

	protected boolean isPropertiesNull(Properties p) {
		return p.isEmpty();
	}

	public abstract void generateReport(String timeslot);
}
