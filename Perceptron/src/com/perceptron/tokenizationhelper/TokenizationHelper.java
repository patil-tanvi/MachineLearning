package com.perceptron.tokenizationhelper;

/**
 * Class to provide additional functionalities to tokenization. (Word
 * sanitization)
 * 
 * @author tanvi
 *
 */
public class TokenizationHelper {

	/**
	 * Sanitize a word. Convert to lowercase and ignore dates, numbers and
	 * special characters.
	 * 
	 * @param word
	 *            -> Type String. Word to be sanitized.
	 * @return newWord -> Type String. Sanitized word.
	 */
	public static String sanitize(String word) {
		String newWord = null;
		word = word.toLowerCase();

		for (char ch : word.toCharArray()) {
			if (Character.isAlphabetic(ch) || Character.isDigit(ch)) {
				if (newWord == null)
					newWord = "";
				newWord = newWord + ch;
			}
		}

		return newWord;
	}
}
