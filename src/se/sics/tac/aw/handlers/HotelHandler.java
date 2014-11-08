package se.sics.tac.aw.handlers;

import se.sics.tac.aw.Bid;
import se.sics.tac.aw.HermesAgent;
import se.sics.tac.aw.Quote;
import se.sics.tac.aw.TACAgent;

public class HotelHandler extends Handler {

	public HotelHandler(TACAgent agent) {
		this.agent = agent;
		this.log = HermesAgent.getLogger();
		prices = new float[agent.getAuctionNo()];
	}
	
	@Override
	public void quoteUpdated(Quote quote, int auction) {
	      int alloc = agent.getAllocation(auction);
	      if (alloc > 0 && quote.hasHQW(agent.getBid(auction)) &&
		  quote.getHQW() < alloc) {
			Bid bid = new Bid(auction);
			// Can not own anything in hotel auctions...
			prices[auction] = quote.getAskPrice() + 50;
			bid.addBidPoint(alloc, prices[auction]);
			
			agent.submitBid(bid);
	      }
	}
	
	public void sendBids(int i) {
	      int alloc = agent.getAllocation(i) - agent.getOwn(i);
	      float price = -1f;
	      
		if (alloc > 0) {
			  price = 200;
			  prices[i] = 200f;
			}
		
	      if (price > 0) {
			Bid bid = new Bid(i);
			bid.addBidPoint(alloc, price);

			agent.submitBid(bid);
	      }
	}

}
