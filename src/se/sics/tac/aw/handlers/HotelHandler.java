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

	Random r;
	int[] estimatedOthersDemand;
	int[] estimatedCurrentDemand;
	
	int[] historicalPrices = {10, 130, 110, 50, 100, 190, 150, 110};
	
	public HotelHandler(TACAgent agent) {
		this.agent = agent;
		r = new Random();
		estimatedOthersDemand = new int[4];
		estimatedCurrentDemand = new int[4];
		
		//For all hotel auctions (8-15), initialize to -1
		for (int i=0 ; i < 8 ; i++){
			for (int j=8 ; j < 16 ; j++){
				agent.setBidder(j, i, -1);
			}
		}
	}
	
	@Override
	public void quoteUpdated(Quote quote, int auction) {
		HermesAgent.addToLog("HOTELHANDLER.QUOTEUPATED()");
		HermesAgent.addToLog("Quote of auction " + auction + " is now " + quote.getAskPrice());
		
	      int alloc = agent.getAllocation(auction);
	      if (alloc > 0 && quote.hasHQW(agent.getBid(auction)) &&
		  quote.getHQW() < alloc) {
	    	  
			for(int client=0 ; client < 8 ; client++){
				
				if (agent.getBidder(auction, client) != -1) {
			    	
					int askPrice = (int) Math.ceil(quote.getAskPrice());
			    	
					if (agent.getBidder(auction, client) <= askPrice){
			    		
						int diff = utilityDifferenceWithAlternative(auction, client);
						HermesAgent.addToLog("Difference of U is thus " + diff);
						
						//If it's in our interest to go on bidding
						if (diff >= 0){
							if (diff==0) { diff++; }
							int newPrice = askPrice + diff;
				    		HermesAgent.addToLog("<= askPrice -> New price : " + newPrice);
				    		agent.setBidder(auction, client, newPrice);	
						}
			    	} else {HermesAgent.addToLog("Already > askPrice :" + agent.getBidder(auction, client));} 	
				}
			}
			
			Bid bid = new Bid(auction);
			
			for (int client=0; client < 8 ; client++){
				//If the client wants something from this auction
				if (agent.getBidder(auction, client) != -1) {
					
					//Bid for that client
					bid.addBidPoint(1, agent.getBidder(auction, client));
				}
			}
			
			HermesAgent.addToLog("ReplaceBid");
			agent.replaceBid(agent.getBid(auction), bid);
	      }
	}

	@Override
	/**
	 * Initial bid, based on the historical prices
	 */
	public void sendSeparateBids(int auction, PackageSet packageSet) {
		HermesAgent.addToLog("HOTELHANDLER.SENDSEPARATEBIDS()");
		Bid bid = new Bid(auction);
		
		for (int client=0; client < 8 ; client++){
			//If the client wants something from this auction
			if (agent.getBidder(auction, client) != -1) {
				
				int price = historicalPrices[auction-8];
				
				//Put the latest price in the matrix
				agent.setBidder(auction, client, price);
				
				//Bid for that client
				bid.addBidPoint(1, price);
			}
		}
		
		//If there is at least one bid to be sent
		if (bid.getNoBidPoints() > 0){
			agent.submitBid(bid);
		}
		
	}
	
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
	}
	
	public void calculateAvgDemand(){
		estimatedOthersDemand = new int[4];
		
		for (int i=0; i < 4*56 ; i++){
			int PopIndx = r.nextInt(10); //all choices of (arrival, departure) sets
			int inDate=0;
			int outDate=0;
			
			switch (PopIndx)
			{
			case 0:
				inDate=1;
				outDate=2;
				break;
			case 1:
				inDate=1;
				outDate=3;
				break;
			case 2:
				inDate=1;
				outDate=4;
				break;
			case 3:
				inDate=1;
				outDate=5;
				break;
			case 4:
				inDate=2;
				outDate=3;
				break;
			case 5:
				inDate=2;
				outDate=4;
				break;
			case 6:
				inDate=2;
				outDate=5;
				break;
			case 7:
				inDate=3;
				outDate=4;
				break;
			case 8:
				inDate=3;
				outDate=5;
				break;
			case 9:
				inDate=4;
				outDate=5;
				break;
			}
			
			for (int day=inDate; day < outDate ; day++){
				estimatedOthersDemand[day-1]++;
			}
		}
		
		for (int i=0 ; i < 4 ; i++){
			estimatedOthersDemand[i] /= 4;
		}
	}
	
	public int utilityDifferenceWithAlternative(int auction, int client) {
		
		PackageConstructor packageConstructor = new PackageConstructor(agent);
		int[] whatWeHave = agent.getAgent().makeWhatWeHaveVector(client);
		
		Package currentPackage = packageConstructor.makePackage(client, whatWeHave, true);
		int utilityOfCurrentPackage = currentPackage.getUtility();
		
		whatWeHave[auction] = -1;
		Package alternatePackage = packageConstructor.makePackage(client, whatWeHave, true);
		int utilityIfWeDontGetIt = alternatePackage.getUtility();
		
		HermesAgent.addToLog("U of current: " + utilityOfCurrentPackage + " vs U of alt: " + utilityIfWeDontGetIt);
		return (utilityOfCurrentPackage - utilityIfWeDontGetIt);
	}
	
	public void howBadDoWeWantIt(int auction, Package p) {
		int otherHotelForThatDay = 0;
		if (auction >= 12) { otherHotelForThatDay = auction-4; }
		else { otherHotelForThatDay = auction+4; }
		
		boolean otherHotelOpen = (!agent.getQuote(otherHotelForThatDay).isAuctionClosed());
		
		//---------
		
		int nbOfClosedAuctions = 0;
		for (int i=0 ; i < 8 ; i++){
			if (agent.getQuote(8+i).isAuctionClosed()) { nbOfClosedAuctions++; }
		}
		
		//---------

		//---------
		
		int nbOfDays = p.getNbOfDays();
		
		//---------
		
		double percentageCompletion = p.completionPercentage();
	  
	}

	@Override
	public void sendBids(int i) {
		// TODO Auto-generated method stub
		
	}

}
