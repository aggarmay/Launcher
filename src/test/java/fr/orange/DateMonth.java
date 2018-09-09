/**
 * 
 */
package fr.orange;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author DLWP2128
 *
 */
public class DateMonth {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
 Date date =new Date();
 calculMonth(date);
	 
	}

	public static  void calculMonth(Date date){
		Calendar c = Calendar.getInstance();
		SimpleDateFormat formatDateJour = new SimpleDateFormat("dd/MM/yyyy"); 
		// on se place à la date utilisée comme base de calcul
		c.setTime(date);
		 
		// on se place au premier jour du mois en cours
		c.set(Calendar.DAY_OF_MONTH, c.getActualMinimum(Calendar.DAY_OF_MONTH));
		//Date debutMois = c.getTime();
		 
		// premier jour du mois en cours moins un jour = dernier jour du mois précédent
		c.add(Calendar.DAY_OF_MONTH, -1);
		Date finMoisPrecedent = c.getTime();
		
		// on était au dernier jour du mois précédent, on se place maintenant au premier jour du mois précédent
		c.set(Calendar.DAY_OF_MONTH, c.getActualMinimum(Calendar.DAY_OF_MONTH));
		Date debutMoisPrecedent = c.getTime();
		
		String finMoisPrecedentFormatee = formatDateJour.format(finMoisPrecedent);
		String debutMoisPrecedentFormatee = formatDateJour.format(debutMoisPrecedent); 
		System.out.println(" finMoisPrecedentFormatee: "+finMoisPrecedentFormatee+" 00:00:00");
		System.out.println(" debutMoisPrecedentFormatee: "+debutMoisPrecedentFormatee+" 00:00:00");
		//finMoisPrecedent.setDate(finMoisPrecedent.getDate()+1);
		String finMoisPrecedentFormatee1 = formatDateJour.format(finMoisPrecedent);
		System.out.println(" finMoisPrecedentFormatee: "+finMoisPrecedentFormatee1+" 00:00:00");
		
	}
	
}
