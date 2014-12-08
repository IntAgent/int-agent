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
	
	public double completionPercentage() {
		int obtainedEssential = 0;
		int totEssential = 0;
		
		for (int key : currentPackage.keySet()) {
		    if (key < 16){
		    	totEssential++;
		    	if (currentPackage.get(key) == 1) {
		    		obtainedEssential++;
		    	}
		    }
		}
		return obtainedEssential*1.0/totEssential;
	}
	
	public double hotelsPercentage() {
		int obtainedEssential = 0;
		int totEssential = 0;
		
		for (int key : currentPackage.keySet()) {
		    if (key < 16 && key >= 8){
		    	totEssential++;
		    	if (currentPackage.get(key) == 1) {
		    		obtainedEssential++;
		    	}
		    }
		}
		return obtainedEssential*1.0/totEssential;
	}
	
	public boolean hasAllHotels() {
		boolean allHotels = true;
		
		for (int key : currentPackage.keySet()) {
		    if (key < 16 && key >= 8){
		    	if (currentPackage.get(key) != 1) {
		    		allHotels = false; break;
		    	}
		    }
		}
		
		return allHotels;
	}
	
	public List<Integer> getElements() {
		List<Integer> res = new ArrayList<Integer>();
		
		for (int key : currentPackage.keySet()) { res.add(key); }

		return res;
	}
	
	public int getInflight() {
		int i = 0;
		boolean found = false;
		while (!found && i < 4){
			if (isInPackage(i)) { found = true; }
			else { i++; }
		}
		
		return i;
	}
	
	public int getOutflight() {
		int i = 4;
		boolean found = false;
		while (!found && i < 8){
			if (isInPackage(i)) { found = true; }
			else { i++; }
		}
		
		return i;
	}
	
	public int getEntertainment(int nbEnt) {
		int[][] id = {{16, 20}, {20, 24}, {24, 28}};
		
		int i = id[nbEnt-1][0];
		boolean found = false;
		while (!found && i < id[nbEnt-1][1]){
			if (isInPackage(i)) { found = true; }
			else { i++; }
		}
		
		if (!found) { i = -1; }
		
		return i;
	}
	
	public boolean goesToGoodHotel() {
		int i = 8;
		boolean found = false;
		while (!found && i < 16){
			if (isInPackage(i)) { found = true; }
			else { i++; }
		}
		
		return (i >= 12);
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
