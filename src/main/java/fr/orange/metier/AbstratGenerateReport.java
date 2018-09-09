/**
 * 
 */
package fr.orange.metier;

import java.util.Properties;

import com.inet.report.ReportException;
import com.inet.report.config.Configuration;
import com.inet.report.config.ConfigurationManager;

import fr.orange.utils.GRUtils;

/**
 * @author DLWP2128
 * 
 */
public abstract class AbstratGenerateReport {

	protected static Properties propertiesGR;
	protected static Properties propertiesConf;
	
	protected void setConfig() {
		//String urlDefaultProperties ="C:\\GR\\properties\\Default.properties";
		String urlDefaultProperties = "C:\\Program Files (x86)\\i-net Clear Reports\\GR-Properties\\Default.properties";
		
		propertiesConf = GRUtils.loadProperties(urlDefaultProperties);
		ConfigurationManager cfg = ConfigurationManager.getInstance();
		Configuration configuration = cfg.getCurrent();
		configuration.clear();
		cfg.setTemporaryProperties(propertiesConf);
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

	public abstract void generateReport(String chaineAdecode,String formatDate) throws ReportException, Exception;
}
