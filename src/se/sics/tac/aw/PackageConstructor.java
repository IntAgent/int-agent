package se.sics.tac.aw;

import java.util.logging.Logger;

public class PackageConstructor {
	private TACAgent agent;
	private Logger log;

	public PackageConstructor(TACAgent agent) {
		this.agent = agent;
		this.log = HermesAgent.getLogger();
	}

	  public void makePackage(int i) {
		
	      int inFlight = agent.getClientPreference(i, TACAgent.ARRIVAL); //Day of desired inflight
	      int outFlight = agent.getClientPreference(i, TACAgent.DEPARTURE);
	      int hotel = agent.getClientPreference(i, TACAgent.HOTEL_VALUE); //Hotel preference
	      int type; //Choice of hotel

	      // Get the flight preferences auction and remember that we are
	      // going to buy tickets for these days. (inflight=1, outflight=0)
	      int auction = agent.getAuctionFor(TACAgent.CAT_FLIGHT,
						TACAgent.TYPE_INFLIGHT, inFlight);
	      agent.setAllocation(auction, agent.getAllocation(auction) + 1);
		  
	      auction = agent.getAuctionFor(TACAgent.CAT_FLIGHT,
					    TACAgent.TYPE_OUTFLIGHT, outFlight);
	      agent.setAllocation(auction, agent.getAllocation(auction) + 1);

	      // if the hotel value (difference between two hotels) is greater than 70
		  // we will select the expensive hotel (type = 1)
	      if (hotel > 70) {
			type = TACAgent.TYPE_GOOD_HOTEL;
	      } else {
			type = TACAgent.TYPE_CHEAP_HOTEL;
	      }
		  
	      // allocate a hotel night for each day that the agent stays
	      for (int d = inFlight; d < outFlight; d++) {
			auction = agent.getAuctionFor(TACAgent.CAT_HOTEL, type, d);
			log.finer("Adding hotel for day: " + d + " on " + auction);
			agent.setAllocation(auction, agent.getAllocation(auction) + 1);
	      }

		  
	      int eType = -1;
		  
		  // Select the next entertainment type to process
	      while((eType = nextEntType(i, eType)) > 0) {
		  
			// Find the best day for the selected entertainment type
			auction = bestEntDay(inFlight, outFlight, eType);
			log.finer("Adding entertainment " + eType + " on " + auction);
			agent.setAllocation(auction, agent.getAllocation(auction) + 1);
	      }
	  }

	  private int bestEntDay(int inFlight, int outFlight, int type) {
	    for (int i = inFlight; i < outFlight; i++) {
	      int auction = agent.getAuctionFor(TACAgent.CAT_ENTERTAINMENT,
						type, i);
	      if (agent.getAllocation(auction) < agent.getOwn(auction)) {
			return auction;
	      }
	    }
	    // If no left, just take the first...
	    return agent.getAuctionFor(TACAgent.CAT_ENTERTAINMENT,
				       type, inFlight);
	  }

	  private int nextEntType(int client, int lastType) {
	    int e1 = agent.getClientPreference(client, TACAgent.E1);
	    int e2 = agent.getClientPreference(client, TACAgent.E2);
	    int e3 = agent.getClientPreference(client, TACAgent.E3);

	    // At least buy what each agent wants the most!!!
	    if ((e1 > e2) && (e1 > e3) && lastType == -1)
	      return TACAgent.TYPE_ALLIGATOR_WRESTLING;
	    if ((e2 > e1) && (e2 > e3) && lastType == -1)
	      return TACAgent.TYPE_AMUSEMENT;
	    if ((e3 > e1) && (e3 > e2) && lastType == -1)
	      return TACAgent.TYPE_MUSEUM;
	    return -1;
	  }
	  
}
