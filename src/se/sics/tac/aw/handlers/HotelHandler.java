package se.sics.tac.aw.handlers;

import java.util.Random;

import se.sics.tac.aw.Bid;
import se.sics.tac.aw.HermesAgent;
import se.sics.tac.aw.PackageConstructor;
import se.sics.tac.aw.PackageSet;
import se.sics.tac.aw.Package;
import se.sics.tac.aw.Quote;
import se.sics.tac.aw.TACAgent;

public class HotelHandler extends Handler {

	private Random r;
	
	private int[] historicalPrices = {10, 130, 110, 50, 100, 190, 150, 110};
	
	public HotelHandler(TACAgent agent) {
		this.agent = agent;
		r = new Random();
	}
	
	@Override
	public void quoteUpdated(Quote quote, int auction, PackageSet packageSet) {
		
		HermesAgent.addToLog("Quote of auction " + auction + " is now " + quote.getAskPrice());
		
		Bid bid = new Bid(auction);
		
	    int alloc = agent.getAllocation(auction);
	    
	    if (alloc > 0 && quote.hasHQW(agent.getBid(auction)) &&
		  quote.getHQW() < alloc) {
	    	  
			for(int client=0 ; client < 8 ; client++){
				
				//If the client wants something from this auction
				if (packageSet.get(client).isInPackage(auction) && !packageSet.get(client).hasBeenObtained(auction)) {
			    	
					int askPrice = (int) Math.ceil(quote.getAskPrice());
				
					//Compare the utility of our current package with that of an alternate package
					int diff = utilityDifferenceWithAlternative(auction, packageSet.get(client));
					HermesAgent.addToLog("Difference of U is thus " + diff);
					
					//If it's in our interest to go on bidding
					if (diff >= 0){
						if (diff==0) { diff++; }
						int newPrice = askPrice + diff;
						
						if (HermesAgent.strategy == 2) {
							newPrice += 500;
						}
						
			    		HermesAgent.addToLog("We go on bidding -> New price : " + newPrice);
			    		bid.addBidPoint(1, newPrice);
					}
					else {
						HermesAgent.addToLog("We give up on this package");
						packageSet.get(client).setGiveUp(auction);
					}
				}
			}

			// We place a bunch of bids in case we can grab cheap extra hotel rooms 
			bid.addBidPoint(8-bid.getNoBidPoints(), 2);
			
			if (bid.getNoBidPoints() > 0){
				agent.replaceBid(agent.getBid(auction), bid);
			}
	      }
	}

	@Override
	/**
	 * Initial bid, based on the historical prices
	 */
	public void sendInitialBids(int auction, PackageSet packageSet) {
		Bid bid = new Bid(auction);
		
		for (int client=0; client < 8 ; client++){
			//If the client wants something from this auction
			if (packageSet.get(client).isInPackage(auction)) {
				
				int price = historicalPrices[auction-8];
				
				if (HermesAgent.strategy == 2) {
					price += 500;
				}
				
				//Bid for that client
				bid.addBidPoint(1, price);
			}
		}
		
		bid.addBidPoint(8-bid.getNoBidPoints(), 2);
		
		//If there is at least one bid to be sent
		if (bid.getNoBidPoints() > 0){
			agent.submitBid(bid);
		}
		
	}
	
	/*
	public void addOwnDemand(PackageSet packageSet){
	    //"Cheap Hotel 1", "Cheap Hotel 2", "Cheap Hotel 3", "Cheap Hotel 4",
	    //"Good Hotel 1", "Good Hotel 2", "Good Hotel 3", "Good Hotel 4",
		
		System.arraycopy(estimatedOthersDemand, 0, estimatedCurrentDemand, 0, estimatedOthersDemand.length);
		
	    for (int p=0 ; p < packageSet.size() ; p++){
			for (int day=1 ; day <= 4 ; day++){
				int auction1 = 8+day-1;
				int auction2 = 8+4+day-1;
				if (packageSet.get(p).isInPackage(auction1) || packageSet.get(p).isInPackage(auction2)){
					HermesAgent.addToLog("Package " + (p+1) + " wants an hotel on day " + (day));
					estimatedCurrentDemand[day-1]++;
				}
			}
	    }
	}*/
	
	/**
	 * Calculates the difference in current utility (score we could obtain by completing this package,
	 * considering the current auction prices for the tickets we still need to get) between the current
	 * version of this client's package, and an alternate package if we decided not to bid more on the
	 * current auction
	 */
	private int utilityDifferenceWithAlternative(int auction, Package currentPackage) {
		
		PackageConstructor packageConstructor = new PackageConstructor(agent);
		
		//Retrieves all resources (spare resources and acquired tickets from the current package)
		int[] whatWeHave = agent.getAgent().makeWhatWeHaveVector(currentPackage.getClient());
		
		//Calculates the current utility of the current package
		int utilityOfCurrentPackage = packageConstructor.calculateCurrentUtility(currentPackage, whatWeHave);
		
		//Calculates the current utility of the alternate package, created by putting an exception on the current auction
		whatWeHave[auction] = -1;
		Package alternatePackage = packageConstructor.makePackage(currentPackage.getClient(), whatWeHave, true);
		int utilityIfWeDontGetIt = packageConstructor.calculateCurrentUtility(alternatePackage, whatWeHave);
		
		HermesAgent.addToLog("U of current: " + utilityOfCurrentPackage + " vs U of alt: " + utilityIfWeDontGetIt);
		
		return (utilityOfCurrentPackage - utilityIfWeDontGetIt);
	}
	
	
	/**
	 * Calculation of the risk involved for this package when it comes to the hotels
	 */
	private void howBadDoWeWantIt(int auction, Package p) {
		
		//Get the ID of the auction of the other hotel, whether we start with Cheap hotel or Good one
		int otherHotelForThatDay = 0;
		if (auction >= 12) { otherHotelForThatDay = auction-4; }
		else { otherHotelForThatDay = auction+4; }
		
		//Is the auction for the other hotel still open?
		boolean otherHotelOpen = (!agent.getQuote(otherHotelForThatDay).isAuctionClosed());
		
		//---------
		
		//Counts the number of closed auctions to determine how much flexibility is left
		int nbOfClosedAuctions = 0;
		for (int i=0 ; i < 8 ; i++){
			if (agent.getQuote(8+i).isAuctionClosed()) { nbOfClosedAuctions++; }
		}
		
		//---------
		
		//Number of days the package would make the client stay (the longer the more risky)
		int nbOfDays = p.getNbOfDays();
		
		//---------
		
		//Percentage of completion of the package in term of feasibility (entertainment not included)
		double percentageCompletion = p.completionPercentage();
	  
	}

}
