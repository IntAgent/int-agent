package se.sics.tac.aw.handlers;

import se.sics.tac.aw.Bid;
import se.sics.tac.aw.HermesAgent;
import se.sics.tac.aw.PackageSet;
import se.sics.tac.aw.Quote;
import se.sics.tac.aw.TACAgent;

public class FlightHandler extends Handler {
	
	public FlightHandler(TACAgent agent) {
		this.agent = agent;
		
		//For all flight auctions (0-7), initialize to -1
		for (int i=0 ; i < 8 ; i++){
			for (int j=0 ; j < 8 ; j++){
				agent.setBidder(j, i, -1);
			}
		}
	}

	//New one
	public void sendSeparateBids(int auction, PackageSet packageSet){
		//TODO Decide when to send them
		
		Bid bid = new Bid(auction);
		
		for (int client=0; client < 8 ; client++){
			//If the client wants something from this auction
			if (agent.getBidder(auction, client) != -1) {
				
				//TODO Put here the calculation for the price
				int utilityPackage = packageSet.get(client).getUtility();
				int price = 850;
				
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
	
	//Old one
	public void sendBids(int i) {
	      int alloc = agent.getAllocation(i) - agent.getOwn(i);
	      float price = -1f;
	      
			if (alloc > 0) {
				  price = 850;
				}
		
	      if (price > 0) {
			Bid bid = new Bid(i);
			bid.addBidPoint(alloc, price);

			agent.submitBid(bid);
	      }
	}

	@Override
	public void quoteUpdated(Quote quote, int auction) {
		// TODO Auto-generated method stub
		
	}


}
