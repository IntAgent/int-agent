package se.sics.tac.aw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Package {

	/** Package is a List of elements. Each element is a vector containing [auction, flag] **/
	HashMap<Integer, Integer> currentPackage = new HashMap<Integer, Integer>();
	int utility;
	int client;
	
	public Package(int client) {
		this.client = client;
	}
	
	/**
	 * Add an element to the package
	 */
	public void addElement(int auction) {
		currentPackage.put(auction, 0);
	}
	
	public void addUtility(int utility){
		this.utility = utility;
	}
	
	public int getUtility() {
		return this.utility;
	}
	
	public int getFlagFor(int i) {
		return currentPackage.get(i);
	}
	
	public int setFlagFor(int i) {
		return currentPackage.put(i, 1);
	}
	
	public int size() {
		return currentPackage.size();
	}
	

	public boolean isInPackage(int auction) {
		return currentPackage.containsKey(auction);
	}
	
	public boolean hasBeenObtained(int auction){
		boolean res = false;
		if (isInPackage(auction)){
			if (getFlagFor(auction) == 1){
				res = true;
			}
		}
		return res;
	}
	
	/**
	 * Returns a list of all the elements that were wanted for this package AND were obtained (their flag is up) 
	 */
	public List<Integer> getObtainedElements() {
		List<Integer> res = new ArrayList<Integer>();
		
		for (int key : currentPackage.keySet()) {
		    if (currentPackage.get(key) == 1) {
		    	res.add(key);
		    }
		}

		return res;
	}
	
	public int getNbOfObtainedElements() {
		int res = 0;
		for (int key : currentPackage.keySet()) {
		    if (currentPackage.get(key) == 1) {
		    	res++;
		    }
		}
		return res;
	}
	
	public List<Integer> getElements() {
		List<Integer> res = new ArrayList<Integer>();
		
		for (int key : currentPackage.keySet()) { res.add(key); }

		return res;
	}
	
	public int getClient(){
		return client;
	}
	
	public int getNbOfDays(){
		int res = 0;
		for (int auction=8 ; auction < 16 ; auction++){
			if (isInPackage(auction)){
				res++;
			}
		}
		return res;
	}
}
