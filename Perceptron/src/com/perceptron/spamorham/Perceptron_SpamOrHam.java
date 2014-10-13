package com.perceptron.spamorham;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

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
	HashMap<String, Integer> wordToIndexMapping = new HashMap<String, Integer>();

	// A 2-d integer matrix which stores the frequency of each word in each
	// document.
	// 0th column stores 0 for each entry (x0 = 0)
	// The last column stores the output. Spam is 1 and Ham is 0.
	int dataMatrix[][];

	// Weight array that stores the weight of each word. weights[0] is the bias.
	double weights[];

	// Learning rate
	double eta;

	// Variables to store the accuracy of spam and ham test data.
	double spamAccuracy, hamAccuracy;
	
	// File arrays to store the respective files;
	File[] spam_training_files, ham_training_files, spam_test_files, ham_test_files;

	/**
	 * Scan through all files in the folder and update the data in dataMatrix;
	 * @param folder_path -> Type File. Specifies the folder from which the files are to be read.
	 * @param spam -> Type boolean. Specifies whether the folder contains spam or ham files.
	 */
	void readFilesInFolder(File folder_path, boolean spam){
		
	}
	
	/**
	 * Get the distinct words from the files contained in the folder and populate the HashMap wordToIndexMapping.
	 * 
	 * @return wordNo -> Type int. Returns the count of distinctWords.
	 */
	int getDistinctWords(){
		int wordNo = 0;
		
		//Scan through all the files in the train/spam folder
		for(File f : spam_training_files){
			
		}
		
		return wordNo;
	}
	
	/**
	 * Function to populate data in the dataMatrix.
	 */
	private void populateData() {

		BufferedReader console = null;
		String folderPath_spamTrain, folderPath_hamTrain;

		try {
			console = new BufferedReader(new InputStreamReader(System.in));
			System.out
					.println("Enter the path of the folder which contains spam training files : ");
			folderPath_spamTrain = console.readLine();

			System.out
					.println("Enter the path of the folder which contains ham training files : ");
			folderPath_hamTrain = console.readLine();

			File folderTrainSpam = new File(folderPath_spamTrain);
			File folderTrainHam = new File(folderPath_hamTrain);
			spam_training_files = folderTrainSpam.listFiles();
			spam_test_files = folderTrainHam.listFiles();
			
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				console.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public static void main(String args[]) {

		Perceptron_SpamOrHam spamOrHam = new Perceptron_SpamOrHam();
		spamOrHam.populateData();
	}
}