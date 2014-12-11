package se.sics.tac.aw;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import se.sics.tac.aw.handlers.EntertainmentHandler;
import se.sics.tac.aw.handlers.FlightHandler;
import se.sics.tac.aw.handlers.Handler;
import se.sics.tac.aw.handlers.HotelHandler;
import se.sics.tac.util.ArgEnumerator;

public class HermesAgent extends AgentImpl {
	private HotelHandler hotelHandler;
	private Handler entHandler;
	private Handler flightHandler;
	private PackageConstructor packageConstructor;
	private PackageSet packageSet;
	private int[] hotelsClosed = new int[8];
	private int[] spareResources = new int[28];
	public static int strategy = 1;
	
	private static final Logger log =
			    Logger.getLogger(HermesAgent.class.getName());
	
	public static Logger getLogger() {
		return log;
	}

	  protected void init(ArgEnumerator args) {
		  initForNewGame();
	  }
	  
	  private void initForNewGame() {
		  	hotelsClosed = new int[8];
		  	spareResources = new int[28];
			hotelHandler = new HotelHandler(agent);
			entHandler = new EntertainmentHandler(agent);
			flightHandler = new FlightHandler(agent);
			packageConstructor = new PackageConstructor(agent);
			packageSet = new PackageSet(agent);
	  }
	  
	  /**
	   * Fills the spareResources vector with the initial Entertainment tickets
	   * distributed at the beginning of the game
	   */
	  private void fillInitialSpareResources(){
		  for (int i=16; i <= 27 ; i++){
			  spareResources[i] = agent.getOwn(i);
		  }
		  log.info("SpareResources: " + Arrays.toString(spareResources));
	  }
	  
	  /**
	   * Select a best package out of 10 created by the PackageConstructor
	   * (Multiple calculations considering the genetic algorithm of PackageConstructor can cause variations)
	   * 
	   * @param client The client we want to create a package for
	   * @param whatWeHave The summary of all the resources available (spare, previous package) or not (closed hotels)
	   * @param average Whether the average prices are used in the calculation of the package (if false: current prices)
	   * @return The created package
	   */
	  private Package createBestPackage(int client, int[] whatWeHave, boolean average) {
		  
		  Package bestPackage = packageConstructor.makePackage(client, whatWeHave, average);
		  int bestUtility = bestPackage.getUtility();
		  
		  Package newPackage;
		  
		  for (int i=0 ; i < 9 ; i++){
			  newPackage = packageConstructor.makePackage(client, whatWeHave, average);
			  if (newPackage.getUtility() > bestUtility){
				  bestPackage = newPackage;
				  bestUtility = newPackage.getUtility();
			  }
		  }
		  
		  return bestPackage;
	  }
	  
	  /**
	   * Creates a vector that summarizes all resources available to a client, including:
	   * - The spare resources that currently figure in no package
	   * - The resources that were obtained in the previous package of the client
	   * - The closed hotel auctions that must not be considered as possibilities anymore
	   * (if there is none in our previously acquired resources) 
	   */
	  public int[] makeWhatWeHaveVector(int client) {
			
			int[] whatWeHave = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			for (int j=0 ; j < agent.getAuctionNo() ; j++){
			
				//if that auction is an hotel auction
				if (j >= 8 && j <= 15) {
					//if the hotel auction has already been closed
					if (hotelsClosed[j-8] == 1){
						whatWeHave[j] = -1;
					}
				}
				
				if (spareResources[j] > 0){
					whatWeHave[j] = 1;
				}
				
				//if the client already has one, it overwrites the fact the auction was closed
				if (packageSet.get(client).hasBeenObtained(j)){
						whatWeHave[j] = 1;
				}
				
			}
			
			return whatWeHave;
	  }
	  
	  /**
	   * Displays a package's content into the log
	   * @param i Number of the client whose package must be shown
	   */
	  private void displayPackage(int i){
		  List<Integer> l = packageSet.get(i).getElements();
		  String res = "Package of client " + (i+1) + "\n";
		  for (int a=0 ; a < l.size() ; a++){
				res += agent.getAuctionTypeAsString(l.get(a)) + "\n";
		  }
		  res += "-------\n";
		  log.info(res);
	  }
	  
	  /**
	   * Calculates the allocation of a given client.
	   * 
	   * @param i Number of the client whose allocation is calculated
	   * @param average Whether the average prices are used in the calculation of the package (if false: current prices)
	   * @param exception Auction ID of an auction that must not be considered as available
	   * for this calculation (used for alternative packages calculation ; if no exception : -1)
	   */
	  private void calculateSeparateAllocation(int i, boolean average, int exception){
			//Construct vector of what we have
			int[] whatWeHave = makeWhatWeHaveVector(i);
			
			if (exception != -1){ whatWeHave[exception] = -1; }
			
			log.info("WhatWeHave: " + Arrays.toString(whatWeHave));
			
			// Create a package
			packageSet.set(i, createBestPackage(i, whatWeHave, average));
			
			//Display the new package in the log
			displayPackage(i);
			
			// Get the list of all auctions the package is interested in
			List<Integer> l = packageSet.get(i).getElements();
			
			// Add every element of the package to the list of things we need to get
			for (int a=0 ; a < l.size() ; a++){
				int auction = l.get(a);
				agent.setAllocation(auction, agent.getAllocation(auction) + 1);
				log.info("Allocation: auction " + auction + " is added to the sum");
			}
			
			//Take off the spareResources anything that can be added to the package
			for (int a=0 ; a < l.size() ; a++){
				int auction = l.get(a);
				if (spareResources[auction] > 0) {
					log.info("We had a spare: auction " + auction + " is added to the package.");
					packageSet.get(i).setFlagFor(auction);	
					spareResources[auction]--;
				}
			}

			displayAllocations();
	  }
	  
	  /**
	   * Calculate the allocations for all 8 client
	   */
	  public void calculateAllocation() {
		  
		// For each of the eight clients
		for (int i = 0 ; i < 8 ; i++) {
			calculateSeparateAllocation(i, true, -1);
		}

	  }

	  /**
	   * Calls the respective handler of the auction whose quote was updated
	   */
	  public void quoteUpdated(Quote quote) {
	    int auction = quote.getAuction();
	    int auctionCategory = agent.getAuctionCategory(auction);

		//1) HOTEL
	    if (auctionCategory == TACAgent.CAT_HOTEL) {
	    	hotelHandler.quoteUpdated(quote, auction, packageSet);
	    }
		
		//2) ENTERTAINMENT
		else if (auctionCategory == TACAgent.CAT_ENTERTAINMENT) {
			entHandler.quoteUpdated(quote, auction, packageSet);
	    }
	    
	    //3) FLIGHT
		else {
			flightHandler.quoteUpdated(quote, auction, packageSet);
		}
	  }

	  /**
	   * Displays the allocations into the log
	   */
	  private void displayAllocations() {
			String allocations = "ALLOCATIONS VECTOR:\n";
			for (int i=0 ; i < 28 ; i++){
				allocations += "Auction " + i + ":" + agent.getAllocation(i) + "\n";
			}
			log.info(allocations);
	  }
	  
	  public void gameStarted() {
		log.fine("Game " + agent.getGameID() + " started!");
		
		displayAllocations();
		
		//Displays the preferences of all clients
		String res = "\n";
		for (int i=0 ; i < 8 ; i++){
			res += "Preferences: Client " + (i+1) + ":\n";
			res += "Arrival on day " + agent.getClientPreference(i, agent.ARRIVAL) + "\n";
			res += "Departure on day " + agent.getClientPreference(i, agent.DEPARTURE) + "\n";
			res += "Hotel preference: " + agent.getClientPreference(i, agent.HOTEL_VALUE) + "\n";
			res += "Entertainment 1: " + agent.getClientPreference(i, agent.E1) + "\n";
			res += "Entertainment 2: " + agent.getClientPreference(i, agent.E2) + "\n";
			res += "Entertainment 3: " + agent.getClientPreference(i, agent.E3) + "\n";
		}
		
		log.info(res);
		
		fillInitialSpareResources();
	    calculateAllocation();
	    dispatchDefaultEntertainment();
	    sendInitialBids();
	  }

	  /**
	   * Sends the first bids at the beginning of the game
	   * (No initial bid sent for flights)
	   */
	  private void sendInitialBids() {
	    for (int i = 0, n = agent.getAuctionNo(); i < n; i++) {
		  
	      switch (agent.getAuctionCategory(i)) {

			  case TACAgent.CAT_HOTEL:
				  hotelHandler.sendInitialBids(i, packageSet);
			break;
			
			  case TACAgent.CAT_ENTERTAINMENT:
				  entHandler.sendInitialBids(i, packageSet);
			break;
			
			  default:
			break;
	      }
		  
	    }
	  }


	  // -------------------------------------------------------------------
	  // Only for backward compability
	  // -------------------------------------------------------------------

	  public static void main (String[] args) {
	    TACAgent.main(args);
	  }

	  
	  
	  
	  // -------------------------------------------------------------------
	  // Log messages
	  // -------------------------------------------------------------------
 
	  public void bidUpdated(Bid bid) {
		    log.fine("Bid Updated: id=" + bid.getID() + " auction="
			     + bid.getAuction() + " state="
			     + bid.getProcessingStateAsString());
		    log.fine("       Hash: " + bid.getBidHash());
		    
	  }
	 
	 /**
	  * Distribute the initial Entertainment tickets distributed at the beginning of the game
	  * to the freshly created packages that might need them
	  */
	 private void dispatchDefaultEntertainment() {
		int[] type = {agent.TYPE_ALLIGATOR_WRESTLING, agent.TYPE_AMUSEMENT, agent.TYPE_MUSEUM};
		int[] day = {1, 2, 3, 4};
		int nbOwned;
		int auction;
		for (int i=0 ; i < type.length ; i++) {
			for (int j=0 ; j < day.length ; j++) {
				auction = agent.getAuctionFor(agent.CAT_ENTERTAINMENT, type[i], day[j]);
				nbOwned = agent.getOwn(auction);
				if (nbOwned > 0) {
					this.packageSet.distribute(nbOwned, auction);
				}
			}
		}
		
	 }
	 
	 /**
	  * Dispatches the quantity of tickets obtained for an auction between packages
	  */
	 public void dispatch(int nbToDispatch, int auction) {
		log.info("**** Dispatching " + nbToDispatch + " tickets of auction " + auction);
	    
		this.packageSet.distribute(nbToDispatch, auction);

	  }

	  public void bidRejected(Bid bid) {
	    log.warning("Bid Rejected: " + bid.getID());
	    log.warning("      Reason: " + bid.getRejectReason()
			+ " (" + bid.getRejectReasonAsString() + ')');
	  }

	  public void bidError(Bid bid, int status) {
	    log.warning("Bid Error in auction " + bid.getAuction() + ": " + status
			+ " (" + agent.commandStatusToString(status) + ')');
	  }

	  public void gameStopped() {
		    log.fine("Game Stopped!");
		    
		    // Displays the final packages we obtained in the log
		    String res = "Final results: \n";
		    for (int c=1 ; c <= 8 ; c++){
		    	
			    res += "Client " + c + ":\n";
			    
			    List<Integer> elements = packageSet.get(c-1).getObtainedElements();
			    for (int i=0 ; i < elements.size() ; i++) {
			    	res += agent.getAuctionTypeAsString(elements.get(i)) + "\n";
			    }
			    
			    res += "-------\n";
		    }
		    
		    log.info(res);
		    
		    //Reinitialize all vectors and parameters for the next game
			initForNewGame();

	  }

	  public void auctionClosed(int auction) {
		    log.fine("*** Auction " + auction + " closed!");
		    
		    //If hotel auction, marks it as closed
		    if (auction >= 8 && auction <= 15) {
		    	hotelsClosed[auction-8] = 1;
		    }
		
			for (int c=0 ; c < 8 ; c++){
				
				//If this client needed it and didn't get it
				boolean changed = false;
				if (packageSet.get(c).isInPackage(auction)){
					if (!packageSet.get(c).hasBeenObtained(auction)){
						
						log.info("Oh no! Client " + (c+1) + "wanted one :(");
						
						//We recalculate another package for him
						recalculatePackage(c, -1);
						
						changed = true;
					}
				}
				
				//If the package was given up because one of its auction's ask price went too far
				if (!changed && packageSet.get(c).hasBeenGivenUp()){
					
					log.info("Oh no! Package of client " + (c+1) + "was given up :(");
					
					//We recalculate another package for him AND put an exception on the auction in question
					recalculatePackage(c, packageSet.get(c).getGiveUpReason());
				}
			}
		  }
	
	  public void recalculatePackage(int c, int exception){
			log.info("------OLD PACKAGE------");
			displayPackage(c);
			
			List<Integer> elements = packageSet.get(c).getElements();
			
			for (int i=0 ; i < elements.size() ; i++){
				
				//Take the elements off the Allocations
	    		HermesAgent.addToLog("Allocation: Deleting auction " + elements.get(i) + " from client " + (c+1));
				agent.setAllocation(elements.get(i), agent.getAllocation(elements.get(i)) - 1);
				
				//Add elements obtained from currentPackage to SpareResources
				if (packageSet.get(c).hasBeenObtained(elements.get(i))){
					spareResources[elements.get(i)]++;
				}
			}
			
			//Calculate new package and allocations
			calculateSeparateAllocation(c, false, exception);
			
			log.info("------NEW PACKAGE------");
			displayPackage(c);
	  }
	  
	  public void quoteUpdated(int auctionCategory) {
		    log.fine("All quotes for "
			     + agent.auctionCategoryToString(auctionCategory)
			     + " has been updated");
		  }
	  
	  public static void addToLog(String msg) {
		  log.info(msg);
	  }
	 
}
