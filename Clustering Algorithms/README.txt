README

Estimation Maximization
Ensure that the input data is in file “em_data.txt” and that it is placed in the same folder as the EstimationMaximization.java

1. Compile :
javac EstimationMaximization.java 

2. Execute using random initializations for mean and variance
java EstimationMaximization <noOfClusters>
Eg : java EstimationMaximization 3

3. Execute using random initialization for mean and specific value for variance
java EstimationMaximization <noOfClusters> <varianceValue>
java EstimationMaximization 3 1


KMeans
The program is in the KMeans.Java file

1. Command to compile the code :
javac KMeans.java 

2. Command to run the code :
java KMeans <input image full path and filename and extesion> <k> <output image with full path and filename and extesion>

Eg : java KMeans ~/Box\ Sync/Projects/ML/SVM/KMeans/Penguins.jpg 2 ~/Box\ Sync/Projects/ML/SVM/KMeans/Pen.jpg

