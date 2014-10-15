package com.machinelearning.assignment2;

public class TokenizationHelper1 {

	/**
	 * Function that throws away all the special characters and strings from the
	 * tokenized words.
	 * 
	 * @param word
	 *            --> Word tokenized using split()
	 * @return filteredWord --> "word" after special characters and numbers are
	 *         removed.
	 */
	static String eliminateSpecialCharacters(String word) {
		String filteredWord = null;
//		if(word.endsWith("subject:"))
//			return word;
		for (int i = 0; i < word.length(); i++) {
			char ch = word.charAt(i);

//			if (ch >= 97 && ch <= 122) {
			if (Character.isAlphabetic(ch) || Character.isDigit(ch)) {
				if (filteredWord == null)
					filteredWord = "";
				filteredWord += ch;
			}
		}
		return filteredWord;
	}
}
