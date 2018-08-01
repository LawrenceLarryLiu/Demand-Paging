import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

@SuppressWarnings("unused")
public class DemandPaging {
	// create needed variables
	public static int machineSize;
	public static int pageSize;
	public static int processSize;
	public static int jobMix;
	public static int numRef;
	public static String replacementAlgo;
	public static int debug;
	public static Scanner random;
	public static int QUANTUM = 3;
	// frame table which we will perform most operations upon
	public static FrameTable frameTable;
	// data structure to store the processes
	public static Process[] pList;
	// variable later used to run all processes
	public static int maxIteration;
	// data structure to store A, B, and C values
	public static double[] A;
	public static double[] B;
	public static double[] C;
	// calculated number of frames
	public static int frameNum;
	// important variables for later display
	public static int totalFault = 0;
	public static int totalEvict = 0;
	public static int totalRes = 0;

	public static void main(String[] args) throws IOException {
		// initialize variables
		machineSize = Integer.parseInt(args[0]);
		pageSize = Integer.parseInt(args[1]);
		processSize = Integer.parseInt(args[2]);
		jobMix = Integer.parseInt(args[3]);
		numRef = Integer.parseInt(args[4]);
		replacementAlgo = args[5];
		debug = 0;
		// large file containing hundreds of random numbers
		random = new Scanner(new FileReader("random-numbers.txt"));
		maxIteration = numRef / QUANTUM;
		frameNum = machineSize / pageSize;
		frameTable = new FrameTable(frameNum, replacementAlgo, random);

		// print out initial messages
		System.out.println("The machine size is " + machineSize +".");
		System.out.println("The page size is " + pageSize +".");
		System.out.println("The process size is " + processSize +".");
		System.out.println("The job mix number is " + jobMix +".");
		System.out.println("The number of references per process is " + numRef +".");
		System.out.println("The replacement algorithm is " + replacementAlgo +".");
		System.out.println("The level of debugging output is " + debug + ".");
		System.out.println();

		// perform certain tasks if the jobMix is 1
		if (jobMix == 1) {
			pList = new Process[1];
			pList[0] = new Process(processSize, 1);
			// only one process so we can use the 0th index
			for (int i = 0; i < numRef; i++) {
				int pageNumber = pList[0].nextRef / pageSize;
				// check if there's a page fault and replace if there is
				if (frameTable.hasFault(pageNumber, 1, i + 1)) {
					frameTable.replace(pList, pageNumber, 1, i + 1);
					pList[0].numFault += 1;
				}
				// get the next reference
				pList[0].nextRef(1, 0, 0, random);
			}
		} else {
			// account for all three other cases:
			// jobMix = 2, 3, or 4
			pList = new Process[4];
			for (int i = 0; i < 4; i++) {
				// initiate new processes with their sizes and numbers
				pList[i] = new Process(processSize, i + 1);
			}

			// precise numbers used according to the spec
			if (jobMix == 2) {
				A = new double[] {1, 1, 1, 1};
				B = new double[] {0, 0, 0, 0};
				C = new double[] {0, 0, 0, 0};
			} else if (jobMix == 3) {
				A = new double[] {0, 0, 0, 0};
				B = new double[] {0, 0, 0, 0};
				C = new double[] {0, 0, 0, 0};
			} else {
				// A, B, and C values start to vary with four processes
				A = new double[] {.75, .75, .75, .5};
				B = new double[] {.25, 0, .125, .125};
				C = new double[] {0, .25, .125, .125};
			}

			// loop through every process
			for (int i = 0; i <= maxIteration; i++) {
				for (int j = 0; j < 4; j++) {
					//how many times will produce a reference word in one quantum
					int counter;
					// check if the loop is in its final iteration
					if (i == maxIteration) {
						counter = numRef % QUANTUM;
					} else {
						// otherwise use the full quantum of three
						counter = QUANTUM;
					}
					// iterate depending on the just-initialized counter
					for (int k = 0; k < counter; k++) {
						int pageNumber = pList[j].nextRef / pageSize;
						int time = (QUANTUM * i * 4) + k + 1 + (j * counter);
						// check if there's a page fault and replace if there is
						if (frameTable.hasFault(pageNumber, j + 1, time)) {
							frameTable.replace(pList, pageNumber, j + 1, time);
							pList[j].numFault += 1;
						}
						// use A, B, and C values to reference the next word
						pList[j].nextRef(A[j], B[j], C[j], random);
					}
				}
			}
		}

		// keep track of the process number
		int indexTrack = 1;
		// go through each process in the pList
		for (Process p : pList) {
			// print specific message if a process has no evictions
			if (p.numEvict == 0) {
				System.out.println("Process " + indexTrack + " had " + p.numFault + " fault(s).");
				// undefined due to a divide-by-zero error
				System.out.println("With no evictions, the average residence is undefined.");
			} else {
				// calculate and show average residency
				System.out.println("Process " + indexTrack + " had " + p.numFault + " fault(s) and " + (double) p.resTime / p.numEvict + " average residency.");
			}
			// keep track of total values for later
			totalFault += p.numFault;
			totalEvict += p.numEvict;
			totalRes += p.resTime;
			indexTrack += 1;
		}
		System.out.println();

		// print specific message if it runs into the same divide-by-zero error mentioned above
		if (totalEvict == 0) {
			System.out.println("The total number of fault(s) is " + totalFault + ".");
			System.out.println("With no evictions, the overall average residency is undefined.");
		} else {
			// display the total number of faults and the overall average residency time
			System.out.println("The total number of fault(s) is "+ totalFault + " and the overall average residency is " + (double) totalRes / totalEvict + ".");
		}
		System.out.println();
	}
}
