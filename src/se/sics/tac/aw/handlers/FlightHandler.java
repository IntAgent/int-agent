package se.sics.tac.aw.handlers;

import se.sics.tac.aw.Bid;
import se.sics.tac.aw.HermesAgent;
import se.sics.tac.aw.Quote;
import se.sics.tac.aw.TACAgent;

public class FlightHandler extends Handler {
	
	public FlightHandler(TACAgent agent) {
		this.agent = agent;
		this.log = HermesAgent.getLogger();
		prices = new float[agent.getAuctionNo()];
	}

	
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
