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
	  
	  private void fillInitialSpareResources(){
		  //Fill the spareResources with initial Entertainement tickets
		  for (int i=16; i <= 27 ; i++){
			  spareResources[i] = agent.getOwn(i);
		  }
		  log.fine("SpareResources: " + Arrays.toString(spareResources));
	  }
	  
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
	  
	  private void displayPackage(int i){
		  List<Integer> l = packageSet.get(i).getElements();
		  String res = "Package of client " + (i+1) + "\n";
		  for (int a=0 ; a < l.size() ; a++){
				res += agent.getAuctionTypeAsString(l.get(a)) + "\n";
		  }
		  res += "-------\n";
		  log.fine(res);
	  }
	  
	  private void calculateSeparateAllocation(int i, boolean average){
			//Construct vector of what we have
			int[] whatWeHave = makeWhatWeHaveVector(i);
			
			log.fine("WhatWeHave: " + Arrays.toString(whatWeHave));
			
			// Create a package
			packageSet.set(i, createBestPackage(i, whatWeHave, average));
			
			//Display the new package in the log
			displayPackage(i);
			
			//Take off the spareResources anything that was added to the package
			List<Integer> l = packageSet.get(i).getElements();
			for (int a=0 ; a < l.size() ; a++){
				int auction = l.get(a);
				if (spareResources[auction] > 0) {
					log.info("We had a spare (auction "+ auction + ": added to the package.");
					packageSet.get(i).setFlagFor(auction);
					log.info("Allocation: auction " + auction + " is added to the sum (from spare)");
					agent.setAllocation(auction, agent.getAllocation(auction) + 1);
					spareResources[auction]--;
				}
			}
			//log.fine("SpareResources: " + Arrays.toString(spareResources));
			
			// Add every element of the package to the list of things we need to get
			log.info("All elements of the package that we don't have yet are added to allocations:");
			for (int j=0 ; j < l.size() ; j++) {
				int auction = l.get(j);

				if (!packageSet.get(i).hasBeenObtained(auction)){
					log.info("Allocation: auction " + auction + " is added to the sum (still need to get)");
					agent.setAllocation(auction, agent.getAllocation(auction) + 1);
				}
			}
			displayAllocations();
	  }
	  
	  public void calculateAllocation() {
		  
		// For each of the eight clients
		for (int i = 0 ; i < 8 ; i++) {
			calculateSeparateAllocation(i, true);
		}
		
		// hotelHandler.addOwnDemand(packageSet); //TODO gerer updates
	  }

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

	  public void displayAllocations() {
			String allocations = "ALLOCATIONS VECTOR:\n";
			for (int i=0 ; i < 28 ; i++){
				allocations += "Auction " + i + ":" + agent.getAllocation(i) + "\n";
			}
			log.info(allocations);
	  }
	  
	  public void gameStarted() {
		log.fine("Game " + agent.getGameID() + " started!");
		
		displayAllocations();
		
		String res = "\n";
		for (int i=0 ; i < 8 ; i++){
			res += "Preferences: Client " + (i+1) + ":\n";
			res += "Arrival on day " + agent.getClientPreference(i, agent.ARRIVAL) + "\n";
			res += "Departure on day " + agent.getClientPreference(i, agent.DEPARTURE) + "\n";
			res += "Hotel preference: " + agent.getClientPreference(i, agent.HOTEL_VALUE) + "\n";
			res += "Entertainment 1: " + agent.getClientPreference(i, agent.E1) + "\n";
			res += "Entertainment 2: " + agent.getClientPreference(i, agent.E2) + "\n";
			res += "Entertainment 3: " + agent.getClientPreference(i, agent.E3) + "\n";
			res += "---------------" + "\n";
		}
		
		log.fine(res);
		
		fillInitialSpareResources();
	    calculateAllocation();
	    dispatchDefaultEntertainment();
	    sendBids();
	  }

	  private void sendBids() {
	    for (int i = 0, n = agent.getAuctionNo(); i < n; i++) {
		  
	      switch (agent.getAuctionCategory(i)) {

			  case TACAgent.CAT_HOTEL:
				  hotelHandler.sendSeparateBids(i, packageSet);
			break;
			
			  case TACAgent.CAT_ENTERTAINMENT:
				  entHandler.sendBids(i);
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
	  
	 public void dispatchDefaultEntertainment() {
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
	 
	 public void dispatch(int nbToDispatch, int auction) {
		log.fine("**** Dispatching " + nbToDispatch + " tickets of auction " + auction);
	    
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
		    
		    log.fine(res);
		    
			initForNewGame();

	  }

	  public void auctionClosed(int auction) {
		    log.fine("*** Auction " + auction + " closed!");
		    
		    //Hotel auctions range from 8 to 15
		    if (auction >= 8 && auction <= 15) {
		    	hotelsClosed[auction-8] = 1;
		    }
		    			
			//log.fine("WhatWeHave of client 1: " + Arrays.toString(makeWhatWeHaveVector(0)));
		
			for (int c=0 ; c < 8 ; c++){
				
				//if this client needed it and didn't get it
				if (packageSet.get(c).isInPackage(auction)){
					if (!packageSet.get(c).hasBeenObtained(auction)){
						
						log.fine("Oh no! Client " + (c+1) + "wanted one :(");
						
						log.fine("------OLD PACKAGE------");
						displayPackage(c);
						
						displayAllocations();
						
						List<Integer> elements = packageSet.get(c).getElements();
						for (int i=0 ; i < elements.size() ; i++){
							//Take the elements off the Allocations
							
				    		HermesAgent.addToLog("Allocation: Deleting auction " + elements.get(i) + " from client " + c);
							agent.setAllocation(elements.get(i), agent.getAllocation(elements.get(i)) - 1);
							
							//Add elements obtained from currentPackage to SpareResources
							if (packageSet.get(c).hasBeenObtained(elements.get(i))){
								spareResources[elements.get(i)]++;
							}
						}
						
						//Calculate new package and allocations
						calculateSeparateAllocation(c, false);
						
						log.fine("------NEW PACKAGE------");
						displayPackage(c);
						
					}
				}
			}
			
			//TODO manage the bidUpdate in the AuctionHandlers
		  }
		
	  public void quoteUpdated(int auctionCategory) {
		    log.fine("All quotes for "
			     + agent.auctionCategoryToString(auctionCategory)
			     + " has been updated");
		  }
	  
	  public static void addToLog(String msg) {
		  log.fine(msg);
	  }
	 
}
