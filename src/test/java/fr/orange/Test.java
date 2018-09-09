/**
 * 
 */
package fr.orange;

/**
 * @author DLWP2128
 * 
 */
public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String plainText = "tootot 1111 aaaaaéàè+/& ";
		System.out.println("initial chainne " + plainText);
		String chars = "àâäéèêëîïôöùûüç/& ";
		String replace = "aaaeeeeiioouuuc___";
		String test = translate(plainText, chars, replace);
		System.out.println(test);

	}

	public static String translate(String src, String chars, String replace) {
		StringBuffer result = new StringBuffer();
		if (src != null && src.length() != 0) {
			int index = -1;
			char c = (char) 0;
			System.out.println("taille chars " + chars.length());
			System.out.println("taille chars " + replace.length());
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
