package se.sics.tac.aw;

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
	    	//... and hasn't the ticket yet
			if (p != null) {
		    	if (p.getFlagFor(auction) == 0) {
		    			
		    		p.setFlagFor(auction);
		    		nbToDispatch--;
		    		HermesAgent.addToLog("Yay! Client " + p.getClient() + " got " + agent.getAuctionTypeAsString(auction) + " (" + auction +") U=" + p.getUtility());
		    	}
			}
	    	i++;
	    }
	} 

}

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