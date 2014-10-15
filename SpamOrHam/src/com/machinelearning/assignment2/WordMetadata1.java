package com.machinelearning.assignment2;

public class WordMetadata1 {

	private String word;
	int countInSpam;
	int countInHam;
	double probabilityInSpam;
	double probabilityInHam;
	static int laplaceSmoothingCount;

	WordMetadata1() {

	}

	WordMetadata1(String word) {
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
