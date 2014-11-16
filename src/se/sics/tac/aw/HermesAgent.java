package se.sics.tac.aw;

import java.util.List;
import java.util.logging.Logger;

import se.sics.tac.aw.handlers.EntertainmentHandler;
import se.sics.tac.aw.handlers.FlightHandler;
import se.sics.tac.aw.handlers.Handler;
import se.sics.tac.aw.handlers.HotelHandler;
import se.sics.tac.util.ArgEnumerator;

public class HermesAgent extends AgentImpl {
	private Handler hotelHandler;
	private Handler entHandler;
	private Handler flightHandler;
	private PackageConstructor packageConstructor;
	private PackageSet packageSet;
	//TODO int[] hotelsclosed = new int[8];
	
	private static final Logger log =
			    Logger.getLogger(HermesAgent.class.getName());
	
	public static Logger getLogger() {
		return log;
	}

	  protected void init(ArgEnumerator args) {
		hotelHandler = new HotelHandler(agent);
		entHandler = new EntertainmentHandler(agent);
		flightHandler = new FlightHandler(agent);
		packageConstructor = new PackageConstructor(agent);
		packageSet = new PackageSet(agent);
	  }
	  
	  public Package createBestPackage(int client, int[] whatWeHave) {
		  
		  Package bestPackage = packageConstructor.makePackage(client, whatWeHave);
		  int bestUtility = bestPackage.getUtility();
		  
		  Package newPackage;
		  
		  for (int i=0 ; i < 9 ; i++){
			  newPackage = packageConstructor.makePackage(client, whatWeHave);
			  if (newPackage.getUtility() > bestUtility){
				  bestPackage = newPackage;
				  bestUtility = newPackage.getUtility();
			  }
		  }
		  
		  return bestPackage;
	  }
	  
	  public void calculateAllocation() {
		  
		// For each of the eight clients
		for (int i = 0 ; i < 8 ; i++) {
			
			// Create a package
			//TODO Give the actual "what we already have" vector (with Entertainment)
			int[] whatWeHave = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			packageSet.set(i, createBestPackage(i, whatWeHave));
			
			//Display the new package in the log
			Package p = packageSet.get(i);
			String res = "Package created for client " + (i+1) + "\n";
			for (int a=0 ; a < p.size() ; a++){
				int auction = agent.getAuctionFor(p.get(a)[0], p.get(a)[1], p.get(a)[2]);
				res += agent.getAuctionTypeAsString(auction) + "\n";
			}
			res += "-------\n";
			log.fine(res);
			
			// Add every element of the package to the list of things we need to get
			for (int j=0 ; j < packageSet.get(i).size() ; j++) {
				int[] element = packageSet.get(i).get(j);
				int auction = agent.getAuctionFor(element[0], element[1], element[2]);
				agent.setAllocation(auction, agent.getAllocation(auction) + 1);
			}
		}
		
	  }

	  public void quoteUpdated(Quote quote) {
	    int auction = quote.getAuction();
	    int auctionCategory = agent.getAuctionCategory(auction);

		//1) HOTEL
	    if (auctionCategory == TACAgent.CAT_HOTEL) {
	    	hotelHandler.quoteUpdated(quote, auction);
	    }
		
		//2) ENTERTAINMENT
		else if (auctionCategory == TACAgent.CAT_ENTERTAINMENT) {
			entHandler.quoteUpdated(quote, auction);
	    }
	  }


	  public void gameStarted() {
		log.fine("Game " + agent.getGameID() + " started!");
		
		String res = "";
		for (int i=0 ; i < 8 ; i++){
			res += "Client " + (i+1) + ":\n";
			res += "Arrival on day " + agent.getClientPreference(i, agent.ARRIVAL) + "\n";
			res += "Departure on day " + agent.getClientPreference(i, agent.DEPARTURE) + "\n";
			res += "Hotel preference: " + agent.getClientPreference(i, agent.HOTEL_VALUE) + "\n";
			res += "Entertainment 1: " + agent.getClientPreference(i, agent.E1) + "\n";
			res += "Entertainment 2: " + agent.getClientPreference(i, agent.E2) + "\n";
			res += "Entertainment 3: " + agent.getClientPreference(i, agent.E3) + "\n";
			res += "---------------" + "\n";
		}
		
		log.fine(res);
		
	    calculateAllocation();
	    dispatchDefaultEntertainment();
	    sendBids();
	  }

	  private void sendBids() {
	    for (int i = 0, n = agent.getAuctionNo(); i < n; i++) {
		  
	      switch (agent.getAuctionCategory(i)) {
			
			case TACAgent.CAT_FLIGHT:
				flightHandler.sendBids(i);
			break;
			
			  case TACAgent.CAT_HOTEL:
				  hotelHandler.sendBids(i);
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
					dispatch(nbOwned, auction);
				}
			}
		}
		
	 }
	 
	 public void dispatch(int nbToDispatch, int auction) {
		log.fine("**** Dispatching " + nbToDispatch + " tickets of auction " + auction);
	    
		Thread t = new Thread(new ResourceDispatcher(packageSet, nbToDispatch, auction));
		t.start();

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
			    
			    List<int[]> elements = packageSet.get(c-1).getObtainedElements();
			    for (int i=0 ; i < elements.size() ; i++) {
			    	int auction = agent.getAuctionFor(elements.get(i)[0],elements.get(i)[1],elements.get(i)[2]);
			    	res += agent.getAuctionTypeAsString(auction) + "\n";
			    }
			    
			    res += "-------\n";
		    }
		    
		    log.fine(res);
	  }

	  public void auctionClosed(int auction) {
		    log.fine("*** Auction " + auction + " closed!");
		    
		    //TODO update hotelsClosed
		    
		    /*//TODO clean this but the part with the "Oh no!" could still be useful
		    int oldNb = oldOwns[auction];
		    int newNb = agent.getOwn(auction);
		    
		    int nbToDispatch = newNb - oldNb;
		    
		    for(int i=0 ; i < packageList.length ; i++){
		    	
		    	int id = packageList[i].findId(agent.getAuctionCategory(auction), agent.getAuctionType(auction), agent.getAuctionDay(auction));
		    	
		    	//If the client wanted one
		    	if (id > 0) {
		    		//And if we got some :D
		    		if (packageList[i].get(id)[3] == 0 && nbToDispatch > 0) {
		    			packageList[i].get(id)[3] = 1;
		    			nbToDispatch--;
		    			log.fine("Yay! Client " + i + "wanted one and got it :)");
		    		}
		    		//And if we didn't get enough for him
		    		else if (packageList[i].get(id)[3] == 0) {
		    			log.fine("Oh no! Client " + i + "wanted one and didn't win :(");
		    			//TODO for all those who couldn't get it: PackageConstructor(client, currentPackage, hotelsClosed)
		    		}
		    	}
		    }
		    
		    System.arraycopy(agent.getOwns(), 0, oldOwns, 0, agent.getAuctionNo());
		    */
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
