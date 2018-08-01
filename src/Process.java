import java.util.Scanner;

public class Process {
	// size of process
	public int processSize;
	// number of faults
	public int numFault;
	// number of evictions
	public int numEvict;
	// residency time
	public int resTime;
	// reference to the next word
	public int nextRef;

	// constructor
	public Process(int processSize, int processNum){
		this.processSize = processSize;
		// these variables begin at zero
		this.numFault = 0;
		this.numEvict = 0;
		this.resTime = 0;
		// calculated based on the specs
		this.nextRef = (111 * processNum) % processSize;
	}

	// use A, B, and C values to get reference to the next word
	public void nextRef(double A, double B, double C, Scanner random) {
		// get random value from the text file used in Lab 2
		int randomNum = random.nextInt();
		double ratio = randomNum / (Integer.MAX_VALUE + 1d);
		// entire method constructed based on explicit lab instructions
		if (ratio < A) {
			nextRef = (nextRef + 1) % processSize;
		} else if (ratio < A + B) {
			nextRef = (nextRef - 5 + processSize) % processSize;
		} else if (ratio < A + B + C) {
			nextRef = (nextRef + 4) % processSize;
		} else {
			// reaccess the random number document for getting next word reference
			nextRef = random.nextInt() % processSize;
		}
	}
}
