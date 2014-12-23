package com.estimationmaximization;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Estimation maximization algorithm for clustering of 1-d data
 * 
 * @author tanvi
 *
 */
public class EstimationMaximization {

	private static int K; // Number of clusters
	private static int N; // Input data size
	private static double[] alpha = null; // The probability
											// distribution of
											// which class the
											// point belongs to.
	private static double[] variance = null; // Variance of the
												// clusters.
	private static double[] meu = null; // Mean of the clusters.
	private static ArrayList<Double> x = null; // Input values

	private static double[][] pOfxGivenTheta = null; // Stores probability of x
														// belonging to cluster
														// k, given the values
														// of mean and variance
														// for that cluster
	private static double[][] weight = null;
	private static double[] Nk = null;

	private static DecimalFormat decimalFormat = new DecimalFormat("#.##");

	/**
	 * Initialize variables
	 * 
	 * @param numberOfClusters
	 *            -> Type int.
	 */
	private static void initializeVariables(int numberOfClusters) {
		K = numberOfClusters;
		alpha = new double[K];
		meu = new double[K];
		variance = new double[K];

		pOfxGivenTheta = new double[N][K];
		weight = new double[N][K];
		Nk = new double[K];
	}

	/**
	 * Read the 1-D input data and store in x;
	 */
	private static void readInput() {
		final String FILE_NAME = "em_data.txt";
		File inputFile = new File(FILE_NAME);
		Scanner scanInput;
		x = new ArrayList<Double>();

		try {
			scanInput = new Scanner(inputFile);

			while (scanInput.hasNextDouble()) {
				x.add(scanInput.nextDouble());
				N++;
			}

		} catch (FileNotFoundException e) {
			System.out
					.println("File Not found. The file name should be em_data.txt. And it should be in the same path as the current folder.");
			e.printStackTrace();
		}
	}

	/**
	 * Print the mean and the variance for K clusters.
	 */
	private static void printGMMParameters(String s) {
		System.out.println("\n");
		System.out.println(s);
		for (int i = 0; i < K; i++) {
			System.out.print("Mean[" + (i + 1) + "] : "
					+ decimalFormat.format(meu[i]) + "\t\t");
		}
		System.out.println();

		for (int i = 0; i < K; i++) {
			System.out.print("Variance[" + (i + 1) + "] : "
					+ decimalFormat.format(variance[i]) + "\t");
		}
	}

	/**
	 * Initialize the GMM parameters (mean and variance)
	 * 
	 * @param varianceGiven
	 *            : boolean. Conveys whether the value of variance is given
	 * @param givenVarianceValue
	 *            : double. If the variance is given, specifies its value.
	 */
	private static void intializeParameters(boolean varianceGiven,
			double givenVarianceValue) {
		double meanOverEntireData = 0; // Mean of the original data
		double varianceOverEntireData = 0; // Variance of the original data
		double sum = 0;
		double varianceSummation = 0;

		for (int i = 0; i < N; i++) {
			sum += x.get(i);
		}
		meanOverEntireData = sum / N;

		// System.out.println("Mean : " + mean);

		for (int i = 0; i < N; i++) {
			varianceSummation += Math.pow(x.get(i) - meanOverEntireData, 2);
		}
		varianceOverEntireData = varianceSummation / N;

		Arrays.fill(alpha, 1 / (double) K);

		// Initializetion of variance to random value
		if (!varianceGiven) {
			for (int i = 0; i < K; i++) {
				int randomNum;
				randomNum = (int) Math.floor(Math.random() * 10);
				// variance[i] = (i + 1) * varianceOverEntireData;
				variance[i] = randomNum * varianceOverEntireData;
//				variance[i] = randomNum * 3;
			}
		}
		// Initialization of variance to given value
		else if (varianceGiven) {
			Arrays.fill(variance, givenVarianceValue);
		}

		// Random intialization of the mean values of the three clusters
		for (int i = 0; i < K; i++) {
			int randomNum = (int) (Math.random() * N);
			meu[i] = x.get(randomNum);
		}
	}

	/**
	 * Function to calculate the probability matrix given the values of mean and
	 * variance.
	 */
	private static void calculatePOfxGivenTheta() {

		for (int inputNo = 0; inputNo < N; inputNo++) {
			for (int clusterNo = 0; clusterNo < K; clusterNo++) {
				double varianceFori = Math.pow(x.get(inputNo) - meu[clusterNo],
						2);
				double numerator = Math.exp(-0.5 * varianceFori
						/ variance[clusterNo]);
				double denominator = Math.sqrt(2 * Math.PI)
						* Math.sqrt(Math.abs(variance[clusterNo]));

				if (denominator != 0) {
					pOfxGivenTheta[inputNo][clusterNo] = numerator
							/ denominator;
				} else {
					pOfxGivenTheta[inputNo][clusterNo] = 0.0;
				}

			}
		}
	}

	/**
	 * Function to calculate the weightage of kth value
	 */
	private static void calculateWeight() {

		for (int inputNo = 0; inputNo < N; inputNo++) {
			for (int clusterNo = 0; clusterNo < K; clusterNo++) {

				double summationOfPOfX = 0;

				for (int k = 0; k < K; k++) {
					summationOfPOfX += pOfxGivenTheta[inputNo][k] * alpha[k];
				}

				if (summationOfPOfX != 0) {
					weight[inputNo][clusterNo] = (pOfxGivenTheta[inputNo][clusterNo] * alpha[clusterNo])
							/ summationOfPOfX;
				} else {
					weight[inputNo][clusterNo] = 0.0;
				}

			}
		}
	}

	/**
	 * The expectation step of the 'E'M algorithm
	 */
	private static void expectationStep() {

		calculatePOfxGivenTheta();
		calculateWeight();
	}

	private static void calculateNk() {

		for (int clusterNo = 0; clusterNo < K; clusterNo++) {
			Nk[clusterNo] = 0;
			for (int inputNo = 0; inputNo < N; inputNo++) {

				Nk[clusterNo] += weight[inputNo][clusterNo];
			}
		}
	}

	private static void updateAlpha() {
		for (int clusterNo = 0; clusterNo < K; clusterNo++) {
			alpha[clusterNo] = Nk[clusterNo] / (double) N;
		}
	}

	private static void updateMeu() {

		for (int clusterNo = 0; clusterNo < K; clusterNo++) {
			meu[clusterNo] = 0.0d;
			for (int inputNo = 0; inputNo < N; inputNo++) {
				meu[clusterNo] += weight[inputNo][clusterNo] * x.get(inputNo)
						/ Nk[clusterNo];			
			}
		}

	}

	private static void updateError() {
		for (int clusterNo = 0; clusterNo < K; clusterNo++) {
			variance[clusterNo] = 0;
			for (int inputNo = 0; inputNo < N; inputNo++) {
				variance[clusterNo] += (weight[inputNo][clusterNo] * Math.pow(
						(x.get(inputNo) - meu[clusterNo]), 2)) / Nk[clusterNo];
			}
		}
	}

	/**
	 * The maximization step of the E'M' algorithm
	 */
	private static void maximizationStep() {
		calculateNk();
		updateAlpha();
		updateMeu();
		updateError();
	}

	private static double getNewLogVal() {
		double logVal = 0;
		for (int inputNo = 0; inputNo < N; inputNo++) {
			double a = 0;
			for (int clusterNo = 0; clusterNo < K; clusterNo++) {
				a = a + (alpha[clusterNo] * pOfxGivenTheta[inputNo][clusterNo]);
			}
			logVal = logVal + Math.log10(a);

		}

		return logVal;
	}

	public static void main(String args[]) {

		if(args.length == 0){
            System.out.println("Please enter the number of clusters as the first argument.");
            System.exit(0);
        }
		
		readInput();
		initializeVariables(Integer.parseInt(args[0]));

		if (args.length != 2) {
			intializeParameters(false, -1.00);
		} else {
			intializeParameters(true, Double.parseDouble(args[1]));
		}

		boolean converged = false;
		double oldLogVal = 0;
		double newLogVal = 0;
		printGMMParameters("Initial GMM Parameters : ");
		do {
			oldLogVal = newLogVal;

			expectationStep();
			maximizationStep();

			newLogVal = getNewLogVal();

			if (newLogVal == oldLogVal) {
				converged = true;
			}

		} while (!converged);
		
		printGMMParameters("Final GMM Parameters : ");
	}
}
