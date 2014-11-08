package se.sics.tac.aw.handlers;

import se.sics.tac.aw.Bid;
import se.sics.tac.aw.HermesAgent;
import se.sics.tac.aw.Quote;
import se.sics.tac.aw.TACAgent;

public class EntertainmentHandler extends Handler {

	public EntertainmentHandler(TACAgent agent) {
		this.agent = agent;
		this.log = HermesAgent.getLogger();
		prices = new float[agent.getAuctionNo()];
	}
	
	@Override
	public void quoteUpdated(Quote quote, int auction) {
	      int alloc = agent.getAllocation(auction) - agent.getOwn(auction);
	      if (alloc != 0) {
			Bid bid = new Bid(auction);
			if (alloc < 0)
			  prices[auction] = 200f - (agent.getGameTime() * 120f) / 720000;
			else
			  prices[auction] = 50f + (agent.getGameTime() * 100f) / 720000;
			bid.addBidPoint(alloc, prices[auction]);
		
			agent.submitBid(bid);
	      }
	}
	
	public void sendBids(int i) {
	      int alloc = agent.getAllocation(i) - agent.getOwn(i);
	      float price = -1f;
	      
	      if (alloc < 0) {
			  price = 200;
			  prices[i] = 200f;
			} else if (alloc > 0) {
			  price = 50;
			  prices[i] = 50f;
			}
		
	      if (price > 0) {
			Bid bid = new Bid(i);
			bid.addBidPoint(alloc, price);

			agent.submitBid(bid);
	      }
	}

}
