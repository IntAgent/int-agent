package se.sics.tac.aw;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

public class PackageSet {
	
	private Package[] packageList = new Package[8];
	private TACAgent agent;

	public PackageSet(TACAgent agent) {
		this.agent = agent;
		for (int i=0 ; i < packageList.length ; i++){
			packageList[i] = new Package(i);
		}
	}
	
	public Package get(int i) {
		return packageList[i];
	}
	
	public void set(int i, Package p){
		packageList[i] = p;
	}
	
	public int size(){
		return packageList.length;
	}
	
	
	/**
	 * Distributes newly obtained tickets between the packages, giving priority
	 * to packages with a higher utility
	 */
	public synchronized void distribute(int nbToDispatch, int auction){
		PriorityQueue<Package> queue =  new PriorityQueue<Package>(8, new PackageComparator());
		for (int i=0 ; i < packageList.length ; i++){
			if (packageList[i].isInPackage(auction)){
				queue.add(packageList[i]);
			}
		}
		
		int i = 0;
		while ((nbToDispatch > 0) && (i < 8)) {
			
			Package p = queue.poll();
	    	//If this package hasn't the ticket yet
			if (p != null) {
		    	if (p.getFlagFor(auction) == 0) {
		    		
		    		//If either not a flight or a flight asked by a nearly-ready package
		    		if (!(auction < 8) || (HermesAgent.strategy != 1 || p.hotelsPercentage() == 1.0)){
		    			
		    		p.setFlagFor(auction);
		    		HermesAgent.addToLog("Yay! Client " + p.getClient() + " got " + agent.getAuctionTypeAsString(auction) + " (" + auction +") U=" + p.getUtility());
		    		
		    		HermesAgent.addToLog("Allocation is:" + agent.getAllocation(auction));
		    		HermesAgent.addToLog("Owns is now:" + agent.getOwn(auction));
		    		HermesAgent.addToLog("-------------------------------------------------");
		    		nbToDispatch--;
		    		}
		    	}
			}
	    	i++;
	    }
	} 

}

/**
 * Compare the packages' respective utility to determine which package
 * has the priority during the dispatch of newly obtained resources
 */
class PackageComparator implements Comparator<Package> {

    public int compare(Package p1, Package p2) {
        if (p1.getUtility() > p2.getUtility()){
            return -1;
        }
        else if (p1.getUtility() < p2.getUtility()){
            return 1;
        }
        else {
            return 0;
        }
    }
}