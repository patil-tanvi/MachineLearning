package com.perceptron.spamorham;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashMap;

import com.perceptron.tokenizationhelper.TokenizationHelper;

/**
 * Implements the perceptron algorithm using the perceptron training rule for
 * detection of an email as a ham or spam, with and without stop words.
 * 
 * @author tanvi
 *
 */
public class Perceptron_SpamOrHam {

	// A hashmap which maps every distinct word to a distinct number. The
	// distinct number varies from 1 to n where n is the number of distinct
	// words.
	HashMap<String, Integer> wordToIndexMapping;

	// A 2-d integer matrix which stores the frequency of each word in each
	// document.
	// 0th column stores 0 for each entry (x0 = 1)
	// The last column stores the output. Spam is 1 and Ham is 0.
	int dataMatrix[][];

	// Weight array that stores the weight of each word. weights[0] is the bias.
	double weights[];

	// Learning rate
	double eta;

	// Variables to store the accuracy of spam and ham test data.
	double spamAccuracy, hamAccuracy;

	// File arrays to store the respective files;
	File[] spam_training_files, ham_training_files, spam_test_files,
			ham_test_files;

	// Regular expression to get tokens from a string.
	final String regex = "\\s+";

	int noOfDistinctWords;
	int totalNoOfTrainingFiles;

	String stopWords;
	String folderPath_spamTrain = null, folderPath_hamTrain = null;
	String folderPath_testSpam = null, folderPath_testHam = null;
	int hard_limit = -1;
	double learning_rate = -1;

	/**
	 * Scan through all files in the folder and update the data in dataMatrix;
	 * 
	 */
	void scanFilesAndPopulateData(boolean ignoreStopWords) {

		int fileNo = 0;

		for (File f : spam_training_files) {
			if (f.toString().contains("DS_Store")) { // Ignore System Files
				continue;
			}
			dataMatrix[fileNo][0] = 1; // Value for X0 (bias = x0 * w0)
			String fileContents = getFileContents(f);
			String words[] = fileContents.split(regex);

			for (String word : words) {
				word = TokenizationHelper.sanitize(word);
				if (!ignoreStopWords) {
					if (word != null) {
						dataMatrix[fileNo][wordToIndexMapping.get(word)] += 1;
					}
				} else {
					if (word != null && !stopWords.contains(word)) {
						dataMatrix[fileNo][wordToIndexMapping.get(word)] += 1;
					}
				}
			}
			dataMatrix[fileNo][noOfDistinctWords + 1] = 1;
			fileNo++;
		}

		for (File f : ham_training_files) {
			if (f.toString().contains("DS_Store")) { // Ignore System Files
				continue;
			}
			String fileContents = getFileContents(f);
			String words[] = fileContents.split(regex);

			for (String word : words) {
				word = TokenizationHelper.sanitize(word);

				if (!ignoreStopWords) {
					if (word != null) {
						dataMatrix[fileNo][wordToIndexMapping.get(word)] += 1;
					}
				} else {
					if (word != null && !stopWords.contains(word)) {
						dataMatrix[fileNo][wordToIndexMapping.get(word)] += 1;
					}
				}
			}
			dataMatrix[fileNo][noOfDistinctWords + 1] = -1;
			fileNo++;
		}

		// No of files is correct
		// System.out.println(fileNo);

		// Count of words is correct
		// int indexOfSubject = wordToIndexMapping.get("subject");
		// for (int i = 0; i < fileNo; i++) {
		// System.out.println(dataMatrix[i][indexOfSubject]);
		// }
	}

	/**
	 * Returns the contents of the file
	 * 
	 * @param f
	 *            -> Type File. The file whose contents are to be returned
	 * @return content -> Type String.
	 */
	private String getFileContents(File f) {
		FileInputStream fis = null;
		String content = null;
		try {
			fis = new FileInputStream(f);
			FileChannel fc = fis.getChannel();
			ByteBuffer buff = ByteBuffer.allocate((int) fc.size());
			fc.read(buff);
			fc.close();
			content = new String(buff.array());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return content;
	}

	/**
	 * Get the distinct words from the files contained in the folder and
	 * populate the HashMap wordToIndexMapping.
	 * 
	 * @return wordCount -> Type int. Returns the count of distinctWords.
	 */
	private int getDistinctWords(boolean ignoreStopWords) {
		int wordCount = 0;
		String fileContents = null;
		String words[] = null;
		wordToIndexMapping = new HashMap<String, Integer>();

		// Scan through all the files in the train/spam folder
		for (File f : spam_training_files) {
			if (f.toString().contains("DS_Store")) {
				continue;
			}
			fileContents = getFileContents(f);
			words = fileContents.split(regex);

			for (String word : words) {
				word = TokenizationHelper.sanitize(word);

				// If word is not a special character string.
				if (!ignoreStopWords) {
					if (word != null) {
						wordToIndexMapping.put(word, wordCount);
						wordCount++;
					}
				} else {
					if (word != null && !stopWords.contains(word)) {
						wordToIndexMapping.put(word, wordCount);
						wordCount++;
					}
				}
			}
		}

		// Scan through all the files in the train/spam folder
		for (File f : ham_training_files) {
			if (f.toString().contains("DS_Store")) {
				continue;
			}
			fileContents = getFileContents(f);
			words = fileContents.split(regex);

			for (String word : words) {
				word = TokenizationHelper.sanitize(word);

				// If word is not a special character string.
				if (!ignoreStopWords) {
					if (word != null) {
						if (!wordToIndexMapping.containsKey(word)) {
							wordToIndexMapping.put(word, wordCount);
							wordCount++;
						}
					}
				} else {
					if (word != null && !stopWords.contains(word)) {
						if (!wordToIndexMapping.containsKey(word)) {
							wordToIndexMapping.put(word, wordCount);
							wordCount++;
						}
					}
				}
			}
		}

		return wordCount;
	}

	/**
	 * Function to populate data in the dataMatrix.
	 */
	private void populateData(boolean ignoreStopWords) {

		BufferedReader console = null;

		try {
			console = new BufferedReader(new InputStreamReader(System.in));

			if (folderPath_hamTrain == null && folderPath_spamTrain == null) {

				System.out
						.println("Enter the path of the folder which contains spam training files : ");
				folderPath_spamTrain = console.readLine();

				System.out
						.println("Enter the path of the folder which contains ham training files : ");
				folderPath_hamTrain = console.readLine();
			}
			File folderTrainSpam = new File(folderPath_spamTrain);
			File folderTrainHam = new File(folderPath_hamTrain);
			spam_training_files = folderTrainSpam.listFiles();
			ham_training_files = folderTrainHam.listFiles();

			noOfDistinctWords = getDistinctWords(ignoreStopWords);
			totalNoOfTrainingFiles = spam_training_files.length
					+ ham_training_files.length;

			dataMatrix = new int[totalNoOfTrainingFiles][noOfDistinctWords + 2];

			scanFilesAndPopulateData(ignoreStopWords);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// try {
			// console.close();
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
		}
	}

	/**
	 * Train the weight associated with individual words based on the perceptron
	 * training rule. Change in weight = learning rate * difference between
	 * calculated output and desired output * xi
	 */
	private void trainWeights(boolean ignoreStopWords) {

		BufferedReader br = null;
		weights = new double[noOfDistinctWords + 1];
		Arrays.fill(weights, 0.15d);

		try {
			br = new BufferedReader(new InputStreamReader(System.in));

			if (hard_limit == -1 && learning_rate == -1) {

				System.out.println("Enter the number of iterations: ");
				hard_limit = Integer.parseInt(br.readLine());
				System.out.println("Enter the learning rate : ");
				learning_rate = Double.parseDouble(br.readLine());
			}
			for (int iteration = 0; iteration < hard_limit; iteration++) {

				for (int fileNo = 0; fileNo < totalNoOfTrainingFiles; fileNo++) {
					double calculated_output = 0;
					double weightDiff[] = new double[weights.length];

					for (int i = 0; i < noOfDistinctWords + 1; i++) {
						calculated_output += dataMatrix[fileNo][i] * weights[i];
					}

					if (calculated_output > 0)
						calculated_output = 1;
					else
						calculated_output = -1;

					// System.out.println("Calculated Output : "
					// + calculated_output);

					for (int i = 0; i < noOfDistinctWords + 1; i++) {
						weightDiff[i] = learning_rate
								* (dataMatrix[fileNo][noOfDistinctWords + 1] - calculated_output)
								* dataMatrix[fileNo][i];

						weights[i] += weightDiff[i];
					}

				}
			}

		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void testAccuracy(boolean ignoreStopWords) {

		BufferedReader console = null;

		try {
			console = new BufferedReader(new InputStreamReader(System.in));

			if (folderPath_testHam == null && folderPath_testSpam == null) {
				System.out
						.println("Enter the path of the folder which contains the test filed for spam : ");
				folderPath_testSpam = console.readLine();
				System.out
						.println("Enter the path of the folder which contains the test filed for ham : ");
				folderPath_testHam = console.readLine();
			}
			
			spam_test_files = new File(folderPath_testSpam).listFiles();
			ham_test_files = new File(folderPath_testHam).listFiles();

			int totalTestSpamFiles = spam_test_files.length;
			int totalTestHamFiles = ham_test_files.length;

			int detectedAsSpam = 0;
			int detectedAsHam = 0;

			for (File f : spam_test_files) {

				String contents = getFileContents(f);
				String[] words = contents.split(regex);

				double output = 0;

				int wordCount[] = new int[weights.length];
				wordCount[0] = 1;

				for (String word : words) {
					word = TokenizationHelper.sanitize(word);

					if (ignoreStopWords) {
						if (word != null && !stopWords.contains(word)) {
							if (wordToIndexMapping.containsKey(word)) {
								int index = wordToIndexMapping.get(word);
								wordCount[index] += 1;
							}
						}
					} else {
						if (word != null) {
							if (wordToIndexMapping.containsKey(word)) {
								int index = wordToIndexMapping.get(word);
								wordCount[index] += 1;
							}
						}
					}

				}

				for (int i = 0; i < weights.length; i++) {
					output += weights[i] * wordCount[i];
				}

				if (output > 0) {
					detectedAsSpam++;
				}
			}

			spamAccuracy = ((double) detectedAsSpam / spam_test_files.length) * 100;

			for (File f : ham_test_files) {

				String contents = getFileContents(f);
				String[] words = contents.split(regex);

				double output = 0;

				int wordCount[] = new int[weights.length];
				wordCount[0] = 1;

				for (String word : words) {
					word = TokenizationHelper.sanitize(word);

					if (ignoreStopWords) {
						if (word != null && !stopWords.contains(word)) {
							if (wordToIndexMapping.containsKey(word)) {
								int index = wordToIndexMapping.get(word);
								wordCount[index] += 1;
							}
						}
					} else {
						if (word != null) {
							if (wordToIndexMapping.containsKey(word)) {
								int index = wordToIndexMapping.get(word);
								wordCount[index] += 1;
							}
						}
					}
				}

				for (int i = 0; i < weights.length; i++) {
					output += weights[i] * wordCount[i];
				}

				if (output <= 0) {
					detectedAsHam++;
				}
			}

			hamAccuracy = ((double) detectedAsHam / ham_test_files.length) * 100;

			System.out.println("Spam Accuracy : " + spamAccuracy
					+ "\nHam Accuracy : " + hamAccuracy);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Initialize the string stopWords with the list of stop words.
	 */
	private void setStopWords() {
		System.out
				.println("Enter the path of the file containing stop words : ");

		try {
			BufferedReader console = new BufferedReader(new InputStreamReader(
					System.in));
			String fileName = console.readLine();
			File stopWordsFile = new File(fileName);
			stopWords = getFileContents(stopWordsFile);

			// System.out.println(stopWords);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {

		Perceptron_SpamOrHam spamOrHam = new Perceptron_SpamOrHam();
		spamOrHam.populateData(false);
		spamOrHam.trainWeights(false);
		spamOrHam.testAccuracy(false);

		spamOrHam.setStopWords();
		spamOrHam.populateData(true);
		spamOrHam.trainWeights(true);
		spamOrHam.testAccuracy(true);

	}
}
