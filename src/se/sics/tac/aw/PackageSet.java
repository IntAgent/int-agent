package se.sics.tac.aw;

public class PackageSet {
	
	private Package[] packageList = new Package[8];
	private TACAgent agent;

	public PackageSet(TACAgent agent) {
		this.agent = agent;
		for (int i=0 ; i < packageList.length ; i++){
			packageList[i] = new Package();
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
		int i = 0;
		while ((nbToDispatch > 0) && (i < 8)) {
			
			//Id of the element in the package corresponding to the auction (or -1 if no such element)
			int id = packageList[i].findId(agent.getAuctionCategory(auction), agent.getAuctionType(auction), agent.getAuctionDay(auction));
	    	
	    	//If the package is interested in a ticket...
	    	if (id != -1) {
	    		
	    		//... and hasn't the ticket yet
	    		if (packageList[i].get(id)[3] == 0) {
	    			
	    			packageList[i].get(id)[3] = 1;
	    			nbToDispatch--;
	    			HermesAgent.addToLog("Yay! Client " + (i+1) + " wanted " + agent.getAuctionTypeAsString(auction) + " (" + auction +") and got it");
	    		}
	    	}
	    	
	    	i++;
	    }
	} 

}
