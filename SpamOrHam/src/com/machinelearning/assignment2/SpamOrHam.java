package com.machinelearning.assignment2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;

class TokenizationHelper {

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


class WordMetadata {

	private String word;
	int countInSpam;
	int countInHam;
	double probabilityInSpam;
	double probabilityInHam;
	static int laplaceSmoothingCount;

	WordMetadata() {

	}

	WordMetadata(String word) {
		this.word = word;
		countInHam = 0;
		countInSpam = 0;
		probabilityInHam = 0.00d;
		probabilityInSpam = 0.00d;
	}

	public void print(){
		System.out.println("Word : " + word);
		System.out.println("Count in ham : " + countInHam);
		System.out.println("Count in spam : " + countInSpam);
		System.out.println("Probability in ham : " + probabilityInHam);
		System.out.println("Probability in spam : " + probabilityInSpam);
	}
	
	public String getWord() {
		return word;
	}

	public int getCountInSpam() {
		return countInSpam;
	}

	public void incrementCountInSpam() {
		this.countInSpam++;
	}

	public int getCountInHam() {
		return countInHam;
	}

	public void incrementCountInHam() {
		this.countInHam++;
	}

	public double getProbabilityInSpam() {
		return probabilityInSpam;
	}

	private void calculateProbabilityInSpam(int totalWordsInSpam,
			int totalDistinctWords) {
//		this.probabilityInSpam = Math.log(
//				(getCountInSpam() + getLaplaceSmoothingCount())
//				/ (double) (totalWordsInSpam + totalDistinctWords));
		this.probabilityInSpam = Math.log(getCountInSpam() + getLaplaceSmoothingCount())
				 - Math.log(totalWordsInSpam + totalDistinctWords);
	}

	public double getProbabilityInHam() {
		return probabilityInHam;
	}

	private void calculateProbabilityInHam(int totalWordsInHam, int totalDistinctWords) {
//		 this.probabilityInHam = Math.log(
//				 (getCountInHam() + getLaplaceSmoothingCount())
//		 / (double) (totalWordsInHam + totalDistinctWords));
		this.probabilityInHam = Math.log(getCountInHam() + getLaplaceSmoothingCount())
				- Math.log(totalWordsInHam + totalDistinctWords);
	}

	private static int getLaplaceSmoothingCount() {
		return laplaceSmoothingCount;
	}

	public static void setLaplaceSmoothingCount(int laplaceSmoothingCount) {
		WordMetadata.laplaceSmoothingCount = laplaceSmoothingCount;
	}

	public void calculateProbabilities(int totalWordsInSpam, int totalWordsInHam,
			int totalDistinctWords) {
		calculateProbabilityInHam(totalWordsInHam, totalDistinctWords);
		calculateProbabilityInSpam(totalWordsInSpam, totalDistinctWords);
	}
}


public class SpamOrHam {
	static private double probabilityOfSpam; // p(spam)
	static private double probabilityOfHam; // p(ham)
	static double spamProbabilityForNewWord;
	static double hamProbabilityForNewWord;

	static private int totalWordsInHam = 0; // Total number of words in ham
											// emails.
	static private int totalWordsInSpam = 0; // Total number of words in spam
												// emails.
	static private int totalDistinctWords = 0; // Total number of distinct words
												// in the spam and ham mails
	static Hashtable<String, WordMetadata> wordStatistics = new Hashtable<String, WordMetadata>();
	// Hashtable that maps every distinct word
	// in the dataset with its corresponding WordMetaData object.

	static private Set<String> words;
	static private File[] listOfSpamFilesInTrainingSet;
	static private File[] listOfHamFilesInTrainingSet;

	static String path = "/Users/tanvi/Documents/MS/ML/Homework/HW2";
	static String TRAIN = "train";
	static String TEST = "test";
	static String HAM = "ham";
	static String SPAM = "spam";

	static double accuracyOfSpamNB;
	static double accuracyOfHamNB;

	static private ArrayList<String> wordsArrayList;
	static double wordStatisticsMatrix[][]; // 2-D matrix to store the P(Y),
											// frequency of a word,
											// and the class (spam or ham) for
											// every document.
	static int OUTPUT_INDEX;
	static double accuracyOfSpamLR;
	static double accuracyOfHamLR;
	static double weights[];
	static double eta = 1, lambda = 0.5;
	static int hardLimit = 100;

	static String operatingSystem = System.getProperty("os.name");

	static ArrayList<String> stopWords;

	/**
	 * function to set the path of training set for spam files
	 */
	private static void setListOfSpamFilesInTrainingSet() {
		File train_spam = new File(path + "/" + TRAIN + "/" + SPAM + "/");
		listOfSpamFilesInTrainingSet = train_spam.listFiles();
	}

	/**
	 * function to set the path of training set for ham files
	 */
	private static void setListOfHamFilesInTrainingSet() {

		File train_ham = new File(path + "/" + TRAIN + "/" + HAM + "/");
		listOfHamFilesInTrainingSet = train_ham.listFiles();
	}

	/**
	 * Function to calculate p(spam) and p(ham)
	 */
	private static void calculateProbabilitiesOfSpamAndHamNB() {

		try {
			int noOfSpamMails = listOfSpamFilesInTrainingSet.length;
			int noOfHamMails = listOfHamFilesInTrainingSet.length;

			int totalMails = (noOfHamMails + noOfSpamMails);

			if (operatingSystem.contains("OS X")) { // Ignore the two DS_Store
													// files
				totalMails -= 2;
				noOfHamMails--;
				noOfSpamMails--;
			}

			probabilityOfSpam = Math.log(noOfSpamMails) - Math.log(totalMails);
			probabilityOfHam = Math.log(noOfHamMails) - Math.log(totalMails);

			// System.out.println("probabilityOfHam : " + probabilityOfHam);
			// System.out.println("probabilityOfSpam : " + probabilityOfSpam);
		} catch (NullPointerException e) {
			System.out.println("Exception in setProbabilitiesofSpamAndHam");
			e.printStackTrace();
		}
	}

	/**
	 * Function to return the contents of a file
	 * 
	 * @param f
	 *            -> File under consideration
	 * @return File contents in the form of String
	 */
	private static String getFileContents(File f) {

		String content = null;
		try {
			FileInputStream fis = new FileInputStream(f.toString());
			FileChannel fc = fis.getChannel();
			ByteBuffer buff = ByteBuffer.allocate((int) fc.size());
			fc.read(buff);
			fc.close();
			content = new String(buff.array());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}

	/**
	 * Function to scan through the list of files and update the values in
	 * wordStatistics
	 * 
	 * @param spamOrHam
	 *            --> Value set to "spam" if scanning through spam folder.
	 *            Otherwise, scan through ham folder.
	 * @param filterStopWords
	 *            --> boolean value specifying whether stop words should be
	 *            filtered out or not.
	 */
	private static void scanThroughFilesAndPopulateStatisticsNB(
			String spamOrHam, boolean filterStopWords) {
		boolean isSpam = spamOrHam.equalsIgnoreCase("spam") ? true : false;
		File[] listOfFiles;
		String fileContents, tokensInAFile[];

		try {
			if (isSpam)
				listOfFiles = listOfSpamFilesInTrainingSet;
			else
				listOfFiles = listOfHamFilesInTrainingSet;

			for (File f : listOfFiles) {
				if (f.toString().contains("DS_Store")) // Ignore OS generated
														// files
					continue;
				else {
					fileContents = getFileContents(f);
					tokensInAFile = fileContents.split("\\s+");

					for (String word : tokensInAFile) {
						word = word.toLowerCase();
						word = TokenizationHelper
								.eliminateSpecialCharacters(word);

						if (filterStopWords == true) {
							if (stopWords.contains(word)) {
								continue;
							}
						}

						if (word == null) // Skip if the word is null
							continue;

						// If a new word is encountered
						if (!wordStatistics.containsKey(word)) {
							WordMetadata newWord = new WordMetadata(word);

							// Increment the count of the word depending on
							// whether the "ham" or "spam" file is being
							// scanned.
							if (isSpam) {
								newWord.incrementCountInSpam();
								totalWordsInSpam++;
							} else {
								newWord.incrementCountInHam();
								totalWordsInHam++;
							}

							totalDistinctWords++;
							wordStatistics.put(word, newWord);

						} else { // If word already exists in wordStatistics
									// update count
							WordMetadata existingWord = wordStatistics
									.get(word);

							// Increment the count of the word depending on
							// whether the "ham" or "spam" file is being
							// scanned.
							if (isSpam) {
								existingWord.incrementCountInSpam();
								totalWordsInSpam++;
							} else {
								existingWord.incrementCountInHam();
								totalWordsInHam++;
							}
							wordStatistics.put(word, existingWord);
						}
					}
				}
			}
		} catch (NullPointerException e) {
			System.out.println("Exception in scanThroughFiles");
			e.printStackTrace();
		}

		// WordMetadata w = wordStatistics.get("subject:");
		// System.out.println(w.getCountInSpam() + w.getCountInHam());
	}

	/**
	 * Function to scan through the wordStatistics Hashtable and set the
	 * probability for each word
	 */
	private static void calculateProbabilityForEachWordNB() {
		words = wordStatistics.keySet();
		for (String word : words) {
			WordMetadata wordMetadata = wordStatistics.get(word);
			wordMetadata.calculateProbabilities(totalWordsInSpam,
					totalWordsInHam, totalDistinctWords);
			wordStatistics.put(word, wordMetadata);
		}

		spamProbabilityForNewWord = Math
				.log(WordMetadata.laplaceSmoothingCount)
				- Math.log(totalWordsInSpam + totalDistinctWords);
		hamProbabilityForNewWord = Math.log(WordMetadata.laplaceSmoothingCount)
				- Math.log(totalWordsInHam + totalDistinctWords);

	}

	private static void calculateProbabilitiesNB() {
		calculateProbabilitiesOfSpamAndHamNB();
		calculateProbabilityForEachWordNB();
	}

	/**
	 * Populate statistical data for the files in ham and spam folder of
	 * training set
	 * 
	 * @param filterStopWords
	 *            --> boolean value specifying whether stop words should be
	 *            filtered out or not.
	 */
	private static void dataPopulationNB(boolean filterStopWords) {

		scanThroughFilesAndPopulateStatisticsNB(SPAM, filterStopWords);
		scanThroughFilesAndPopulateStatisticsNB(HAM, filterStopWords);

		// System.out.println("Total Distinct words: " + totalDistinctWords);
		// System.out.println("Total Ham words: " + totalWordsInHam);
		// System.out.println("Total Spam words: " + totalWordsInSpam);
		// System.out.println("wordStatistics Size : " + wordStatistics.size());
		// WordMetadata subjectStatistics = wordStatistics.get("subject");
		// subjectStatistics.print();
	}

	/**
	 * Function to classify given file f
	 * 
	 * @param f
	 *            -> file to be classified
	 * @param filterStopWords
	 *            --> boolean value specifying whether stop words should be
	 *            filtered out or not to identify whether the mail is spam or
	 *            not.
	 * @return true is file is classified as spam. False, otherwise.
	 */
	private static boolean isSpamNB(File f, boolean filterStopWords) {
		boolean spam = false;

		double spamProbabilityForF = probabilityOfSpam;
		double hamProbabilityForF = probabilityOfHam;

		String content = getFileContents(f);
		String[] contentTokens = content.split("\\s+");

		for (String word : contentTokens) {
			word = word.toLowerCase();
			word = TokenizationHelper.eliminateSpecialCharacters(word);

			if (filterStopWords == true) {
				if (stopWords.contains(word)) {
					continue;
				}
			}

			if (word == null)
				continue;

			WordMetadata wordMetadata = wordStatistics.get(word);

			if (wordMetadata == null) {
				spamProbabilityForF += spamProbabilityForNewWord;
				hamProbabilityForF += hamProbabilityForNewWord;
			} else {
				spamProbabilityForF += wordMetadata.getProbabilityInSpam();
				hamProbabilityForF += wordMetadata.getProbabilityInHam();
			}
		}

		if (spamProbabilityForF > hamProbabilityForF)
			spam = true;

		return spam;
	}

	/**
	 * Function to calculate the accuracy of the NaiveBayes algorithm
	 * 
	 * @param filterStopWords
	 *            --> boolean value specifying whether stop words should be
	 *            filtered out or not to calculate accuracy.
	 */
	private static void calculateAccuracyNB(boolean filterStopWords) {
		File test_spam = new File(path + "/" + TEST + "/" + SPAM + "/");
		File test_ham = new File(path + "/" + TEST + "/" + HAM + "/");
		File[] listOfSpamFilesInTestSet = test_spam.listFiles();
		File[] listOfHamFilesInTestSet = test_ham.listFiles();

		int totalSpamFiles = listOfSpamFilesInTestSet.length;
		int totalHamFiles = listOfHamFilesInTestSet.length;

		if (operatingSystem.contains("OS X")) {
			totalHamFiles--;
			totalSpamFiles--;
		}

		int correctlyClassifiedAsSpam = 0;
		int correctlyClassifiedAsHam = 0;

		for (File f : listOfSpamFilesInTestSet) {
			if (f.toString().contains("DS_Store"))
				continue;
			if (isSpamNB(f, filterStopWords)) {
				correctlyClassifiedAsSpam++;
			}
		}
		accuracyOfSpamNB = (correctlyClassifiedAsSpam / (double) totalSpamFiles) * 100.00d;

		for (File f : listOfHamFilesInTestSet) {
			if (f.toString().contains("DS_Store"))
				continue;
			if (!isSpamNB(f, filterStopWords)) {
				correctlyClassifiedAsHam++;
			}
		}
		accuracyOfHamNB = (correctlyClassifiedAsHam / (double) totalHamFiles) * 100.00d;

		System.out.println("Accuracy of spam : " + accuracyOfSpamNB
				+ "\nAccuracy of ham : " + accuracyOfHamNB);

	}

	/**
	 * Function to populate the wordStatisticsMatrix
	 * 
	 * @param startIndex
	 *            --> No of docs that have already been scanned
	 * @param spamOrHam
	 *            --> Paramter specifying whether the given set of files if spam
	 *            or ham (To enter 0/1 in output field)
	 * @return docNo --> No of files scanned. (To keep track of where to start
	 *         from for next scan)
	 */
	private static int scanThroughFilesAndPopulateStatisticsLR(int startIndex,
			String spamOrHam, boolean filterStopWords) {
		int outputClass;
		int docNo = startIndex;
		OUTPUT_INDEX = wordStatistics.size() + 1;
		String fileContents, tokensInAFile[];
		File[] listOfFiles;

		if (spamOrHam.equalsIgnoreCase(SPAM)) {
			listOfFiles = listOfSpamFilesInTrainingSet;
			outputClass = 1;
		} else {
			listOfFiles = listOfHamFilesInTrainingSet;
			outputClass = 0;
		}

		for (File f : listOfFiles) {
			if (f.toString().contains("DS_Store"))
				continue;

			fileContents = getFileContents(f);
			tokensInAFile = fileContents.split("\\s+");
			wordStatisticsMatrix[docNo][OUTPUT_INDEX] = outputClass;
			for (String word : tokensInAFile) {
				word = word.toLowerCase();
				word = TokenizationHelper.eliminateSpecialCharacters(word);

				if (filterStopWords == true)
					if (stopWords.contains(word))
						continue;

				if (word == null)
					continue;
				wordStatisticsMatrix[docNo][wordsArrayList.indexOf(word) + 1] += 1;
				// + 1 in col index --> To leave the first column to store
				// the probabilities in the matrix.
			}
			docNo++;
		}

		return docNo;
	}

	private static void dataPopulationLR(boolean filterStopWords) {
		wordsArrayList = new ArrayList<>();
		wordsArrayList.addAll(wordStatistics.keySet());

		int totalDocs = listOfHamFilesInTrainingSet.length
				+ listOfSpamFilesInTrainingSet.length;

		if (operatingSystem.contains("OS X"))
			totalDocs -= 2;

		wordStatisticsMatrix = new double[totalDocs][wordStatistics.size() + 2];
		int startIndexForHamFiles = scanThroughFilesAndPopulateStatisticsLR(0,
				SPAM, filterStopWords);
		totalDocs = scanThroughFilesAndPopulateStatisticsLR(
				startIndexForHamFiles, HAM, filterStopWords);

		// Correct count
		// int indexOfSubject = wordsArrayList.indexOf("subject");
		// for (int i = 0; i < totalDocs; i++) {
		// System.out.println(wordStatisticsMatrix[i][indexOfSubject + 1]);
		// }

		// Correct output
		// int indexOfOutput = wordStatistics.size() + 1;
		// for (int i = 0; i < totalDocs; i++) {
		// System.out.println(wordStatisticsMatrix[i][indexOfOutput]);
		// }
	}

	/**
	 * Function to learn the weight value for each word.
	 */
	private static void setWeightsLR() {
		weights = new double[wordStatistics.size() + 1]; // w0, w1, w2,.., wn
		Arrays.fill(weights, 0.05d); // Initialize the weight vector with random
										// values

		int totalDocs = listOfHamFilesInTrainingSet.length
				+ listOfSpamFilesInTrainingSet.length;

		if (operatingSystem.contains("OS X"))
			totalDocs -= 2;

		for (int iterations = 0; iterations < hardLimit; iterations++) {
			double weightDiff[] = new double[weights.length];
			double z = 0.00d;

			// Calculate and store the probability from the current weights.
			for (int docNo = 0; docNo < totalDocs; docNo++) {
				for (int weightIndex = 0; weightIndex < weights.length; weightIndex++) {
					if (weightIndex == 0)
						z = weights[0];
					else
						z += weights[weightIndex]
								* wordStatisticsMatrix[docNo][weightIndex];
				}
				wordStatisticsMatrix[docNo][0] = 1 / (double) (1 + Math.exp(-z));
			}

			for (int docNo = 0; docNo < totalDocs; docNo++) {
				for (int weightIndex = 1; weightIndex < weights.length; weightIndex++) {
					weightDiff[weightIndex] = weightDiff[weightIndex]
							+ (wordStatisticsMatrix[docNo][weightIndex] * (wordStatisticsMatrix[docNo][OUTPUT_INDEX] - wordStatisticsMatrix[docNo][0]));
				}
				// System.out.println(weights[10]);
			}

			for (int weightIndex = 0; weightIndex < weights.length; weightIndex++) {
				weights[weightIndex] += eta
						* (weightDiff[weightIndex] - weights[weightIndex]
								* lambda);
				// weights[weightIndex] += eta * weightDiff[weightIndex] ;
			}

		}

		// Correct count
		// int indexOfSubject = wordsArrayList.indexOf("subject");
		// for (int i = 0; i < totalDocs; i++) {
		// System.out.println(weights[indexOfSubject + 1]);
		// }

		// for (int weightIndex = 0; weightIndex < weights.length;
		// weightIndex++) {
		// System.out.println(weights[weightIndex]);
		// }
	}

	private static boolean isSpamLR(File f, boolean filterStopWords) {
		boolean spam = true;

		String fileContents = getFileContents(f);
		String[] tokenInFile = fileContents.split("\\s+");
		double pSpam = 0.00d;
		double pHam = 0.00d;
		double z = weights[0];
		int index;
		int[] freq = new int[wordStatistics.size()];

		for (String word : tokenInFile) {
			word = word.toLowerCase();
			word = TokenizationHelper.eliminateSpecialCharacters(word);

			if (filterStopWords == true)
				if (stopWords.contains(word))
					continue;

			if (word == null)
				continue;

			index = wordsArrayList.indexOf(word);
			if (index != -1)
				freq[index]++;
		}

		for (int i = 0; i < freq.length; i++) {
			z += freq[i] * weights[i + 1];
		}

		// System.out.println(z + "   " + Math.exp(z));

		pHam = 1 / (double) (1 + Math.exp(z));
		pSpam = Math.exp(z) / (double) (1 + Math.exp(z));

		if (pSpam < pHam)
			// if(z < 0)
			spam = false;

		return spam;
	}

	private static void calculateAccuracyLR(boolean filterStopWords) {
		File test_spam = new File(path + "/" + TEST + "/" + SPAM + "/");
		File test_ham = new File(path + "/" + TEST + "/" + HAM + "/");
		File[] listOfTestSpamFiles = test_spam.listFiles();
		File[] listOfTestHamFiles = test_ham.listFiles();

		int totalSpamFiles = listOfTestSpamFiles.length;
		int totalHamFiles = listOfTestHamFiles.length;

		if (operatingSystem.contains("OS X")) {
			totalHamFiles--;
			totalSpamFiles--;
		}

		int correctlyClassifiedAsSpam = 0;
		int correctlyClassifiedAsHam = 0;
		// System.out.println("weight[0] : " + weights[0]);
		for (File f : listOfTestSpamFiles) {
			if (f.toString().contains("DS_Store"))
				continue;
			if (isSpamLR(f, filterStopWords)) {
				correctlyClassifiedAsSpam++;
			}
		}

		accuracyOfSpamLR = (correctlyClassifiedAsSpam / (double) totalSpamFiles) * 100.00d;

		for (File f : listOfTestHamFiles) {
			if (f.toString().contains("DS_Store"))
				continue;
			if (!isSpamLR(f, filterStopWords)) {
				correctlyClassifiedAsHam++;
			}
		}
		accuracyOfHamLR = (correctlyClassifiedAsHam / (double) totalHamFiles) * 100.00d;

		System.out.println("Accuracy of spam : " + accuracyOfSpamLR
				+ "\nAccuracy of ham : " + accuracyOfHamLR);

	}

	/**
	 * Read stop words from the stopwords.txt
	 */
	private static void readStopWords() {
		String fileName = "StopWords.txt";
		String filePath = path + "/" + fileName;
		String fileContents = getFileContents(new File(filePath));
		String[] stopWordsArray;
		stopWordsArray = fileContents.split("\\s+");
		stopWords = new ArrayList<String>(Arrays.asList(stopWordsArray));
	}

	public static void main(String[] args) {

		WordMetadata.setLaplaceSmoothingCount(1);
		boolean filterStopWords = false;


			path = args[0];
			eta = Double.parseDouble(args[1]);
			lambda = Double.parseDouble(args[2]);
			hardLimit = Integer.parseInt(args[3]);
			
			setListOfHamFilesInTrainingSet();
			setListOfSpamFilesInTrainingSet();

			totalDistinctWords = 0;
			totalWordsInHam = 0;
			totalWordsInSpam = 0;

			dataPopulationNB(filterStopWords);
			calculateProbabilitiesNB();
			calculateAccuracyNB(filterStopWords);

			dataPopulationLR(filterStopWords);
			setWeightsLR();
			calculateAccuracyLR(filterStopWords);

			totalDistinctWords = 0;
			totalWordsInHam = 0;
			totalWordsInSpam = 0;
			wordStatistics = new Hashtable<String, WordMetadata>();
			readStopWords();
			filterStopWords = true;
			dataPopulationNB(filterStopWords);
			calculateProbabilitiesNB();
			calculateAccuracyNB(filterStopWords);

			readStopWords();
			filterStopWords = true;
			dataPopulationLR(filterStopWords);
			setWeightsLR();
			calculateAccuracyLR(filterStopWords);

	}
}
