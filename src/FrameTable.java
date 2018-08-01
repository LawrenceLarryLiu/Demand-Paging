import java.util.Scanner;

public class FrameTable {
	// creating all variables needed
	public int frameNum;
	// first index refers to the frames
	// second index refers to an array that holds page number, process number, and time
	public int[][] table;
	public String type;
	Scanner random;

	// construct the frame table dependent on user input
	public FrameTable(int frameNum, String type, Scanner random) {
		// number of frames
		this.frameNum = frameNum;
		// which algorithm used
		this.type = type;
		// the fourth spot of the second index is solely for LRU
		// to hold the loading time
		this.table = new int[frameNum][4];
		// keep instance variable so no need to keep reopening or passing file
		this.random = random;
	}

	// check if there's a page fault
	public boolean hasFault(int pageNum, int processNum, int time) {
		boolean sentinel = true;
		// differentiate depending on user input
		for (int[] page : table) {
			// check if the page in demand is already in the table
			if (page[0] == pageNum && page[1] == processNum) {
				if (type.equals("lru")) {
					page[2] = time;
				}
				// if it's there, then there's no fault
				sentinel = false;
			}
		}
		// if it's not there, then there's a fault
		return sentinel;
	}

	public void replace(Process[] pList, int pageNum, int processNum, int time) {
		// differentiate depending on algorithm
		if (type.equals("fifo")) {
			// index used to look for empty frames
			int fifoIndex = -1;
			// iterate through all frames in the table
			for (int i = 0; i < table.length; i++) {
				if (table[i][0] == 0 && table[i][1] == 0) {
					// store index of empty frame if there is one
					fifoIndex = i;
					// exit loop in order to get earliest empty frame
					break;
				}
			}
			// if there are no empty spots, then begin eviction
			if (fifoIndex == -1) {
				// get the frame to evict
				int[] evictedF = table[0];
				// get process that will be evicted
				Process evictedP = pList[evictedF[1] - 1];
				// calculate the residency time to add later
				int resTime = time - evictedF[2];
				// increment everything you need to
				evictedP.numEvict += 1;
				evictedP.resTime += resTime;
				// create temporary array to hold previous values
				int[][] temp = new int[table.length][table[0].length];
				// iterate through the table
				for (int i = 0; i < table.length - 1; i++) {
					// copy every value except for the first frame of the table
					temp[i] = table[i + 1];
				}
				// set table to be identical but shifted one unit earlier
				// same effect as removing first frame
				table = temp;
				// set the index for adding new page to the end of the table
				fifoIndex = table.length - 1;
			}
			// add new values to the table (replace {0, 0, 0, 0})
			// fourth index doesn't matter, it's only used in LRU
			table[fifoIndex] = new int[] {pageNum, processNum, time, time};
		} else {
			int LRUTime = time;
			int evictedF = 0;
			// start looking for unused frames by beginning with the highest address decrementing
			for (int i = frameNum - 1; i > -1; i--) {
				if (table[i][0] == 0 && table[i][1] == 0) {
					// use frame if it's available
					table[i] = new int[] {pageNum, processNum, time, time};
					// lruLoadTime to be used soon
					return;
				} else if (type.equals("lru") && LRUTime > table[i][2]) {
					// keep track of the index of the evicted frame
					evictedF = i;
					// get the Least Recently Used frame if none are available
					LRUTime = table[i][2];
				}
			}

			Process evictedP;
			int resTime;
			if (type.equals("lru")) {
				// get the process to evict
				evictedP = pList[table[evictedF][1] - 1];
				// calculate the residency time to add later
				resTime = time - table[evictedF][3];
			} else {
				// get a random value
				evictedF = random.nextInt() % frameNum;
				// get the process to evict
				evictedP = pList[table[evictedF][1] - 1];
				// no need for the load time like in LRU
				resTime = time - table[evictedF][2];
			}
			// increment everything you need to
			evictedP.numEvict += 1;
			evictedP.resTime += resTime;
			// add new values to the table (replace {0, 0, 0})
			table[evictedF] = new int[] {pageNum, processNum, time, time};
		}
	}
}
