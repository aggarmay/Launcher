package fr.orange.log;

import org.apache.log4j.PatternLayout; 
import org.apache.log4j.spi.LoggingEvent; 
/** 
 * <p> 
 * This class is a drop in replacement for a log4j PatternLayout 
class. It can be referenced via a log4j properties file 
 * just like an ordinary PatternLayout. 
 * 
 * <p> 
 * The only difference between this class and the parent class is that 
newlines are replace by a replacement string. 
 * This allows downstream log viewers to receive a single log message 
that contains multiple lines and correctly 
 * reconstruct it as a single, viewable message. 
 * 
 * @author Manish Gupta 
 */ 
//New class added for Forge#374803
public class NewLinePatternLayout extends PatternLayout
{
	public static final String NL_REPLACEMENT= ". ";
	public NewLinePatternLayout()  { } 

	public NewLinePatternLayout(String pattern) 
	{ 
	      super(pattern); 
	} 
	
	public String format(LoggingEvent event)
	{
		return super.format(event).replaceAll("\n", NL_REPLACEMENT).concat("\n");// This is done to place a new line character between two messages
	}
}
