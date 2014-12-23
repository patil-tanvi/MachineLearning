README

Ensure that the input data is in file “em_data.txt” and that it is placed in the same folder as the EstimationMaximization.java

1. Compile :
javac EstimationMaximization.java 

2. Execute using random initializations for mean and variance
java EstimationMaximization <noOfClusters>
Eg : java EstimationMaximization 3

3. Execute using random initialization for mean and specific value for variance
java EstimationMaximization <noOfClusters> <varianceValue>
java EstimationMaximization 3 1