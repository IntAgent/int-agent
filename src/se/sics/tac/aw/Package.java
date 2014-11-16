package se.sics.tac.aw;

import java.util.ArrayList;
import java.util.List;

public class Package {

	/** Package is a List of elements. Each element is a vector containing [category, type, day, flag] **/
	List<int[]> currentPackage = new ArrayList<int[]>();
	int utility;
	
	public Package() {
	}
	
	/**
	 * Add an element to the package
	 */
	public void addElement(int category, int type, int day) {
		int[] v = {category, type, day, 0}; 
		currentPackage.add(v);
	}
	
	public void addUtility(int utility){
		this.utility = utility;
	}
	
	public int getUtility() {
		return this.utility;
	}
	
	public int[] get(int i) {
		return currentPackage.get(i);
	}
	
	public int size() {
		return currentPackage.size();
	}
	
	/**
	 * Returns the ID of the element of the package corresponding to given category, type and day.
	 * If there is no such element in the package, returns -1. 
	 */
	public int findId(int category, int type, int day) {
		boolean found = false;
		int i = 0;
		while (!found && i < currentPackage.size()) {
			if ((currentPackage.get(i)[0] == category) && (currentPackage.get(i)[1] == type) && (currentPackage.get(i)[2] == day)) {
				found = true;
			}
			else {
				i++;
			}
		}
		if (!found) {i = -1;}
		return i;
	}
	
	/**
	 * Returns a list of all the elements that were wanted for this package AND were obtained (their flag is up) 
	 */
	public List<int[]> getObtainedElements() {
		List<int[]> res = new ArrayList<int[]>();
		
		for (int i=0 ; i < currentPackage.size() ; i++) {
			if (currentPackage.get(i)[3] == 1) {
				res.add(currentPackage.get(i));
			}
		}
		return res;
	}
	
}
