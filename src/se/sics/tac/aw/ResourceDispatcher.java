package se.sics.tac.aw;

/**
 * Thread in charge of dispatching the result of an auction between the packages
 */
public class ResourceDispatcher implements Runnable {

	private PackageSet packageSet;
	private int nbToDispatch;
	private int auction;
	
	public ResourceDispatcher(PackageSet packageSet, int nbToDispatch, int auction) {
			this.packageSet = packageSet;
			this.nbToDispatch = nbToDispatch;
			this.auction = auction;
		}

	public void run() {
		this.packageSet.distribute(nbToDispatch, auction);
	}
	
	
}
