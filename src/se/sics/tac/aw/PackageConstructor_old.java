package se.sics.tac.aw;

public class PackageConstructor_old {
	private TACAgent agent;

	public PackageConstructor_old(TACAgent agent) {
		this.agent = agent;
	}

	  public Package makePackage(int i) {
		  
		  Package currentPackage = new Package();
		
	      int inFlight = agent.getClientPreference(i, TACAgent.ARRIVAL); //Day of desired inflight
	      int outFlight = agent.getClientPreference(i, TACAgent.DEPARTURE);
	      int hotel = agent.getClientPreference(i, TACAgent.HOTEL_VALUE); //Hotel preference
	      int type; //Choice of hotel

	      currentPackage.addElement(TACAgent.CAT_FLIGHT, TACAgent.TYPE_INFLIGHT, inFlight);
	      currentPackage.addElement(TACAgent.CAT_FLIGHT, TACAgent.TYPE_OUTFLIGHT, outFlight);

	      HermesAgent.addToLog("Adding inflight for day " + inFlight + " to package " + (i+1));
	      HermesAgent.addToLog("Adding outflight for day " + outFlight + " to package " + (i+1));
	      
	      // if the hotel value (difference between two hotels) is greater than 70
		  // we will select the expensive hotel (type = 1)
	      if (hotel > 70) {
			type = TACAgent.TYPE_GOOD_HOTEL;
	      } else {
			type = TACAgent.TYPE_CHEAP_HOTEL;
	      }
		  
	      // allocate a hotel night for each day that the agent stays
	      for (int d = inFlight; d < outFlight; d++) {
	    	currentPackage.addElement(TACAgent.CAT_HOTEL, type, d);
			HermesAgent.addToLog("Adding hotel for day " + d + " to package " + (i+1));
	      }

		  
	      int eType = -1;
		  int d;
		  // Select the next entertainment type to process
	      while((eType = nextEntType(i, eType)) > 0) {
		  
			// Find the best day for the selected entertainment type
	    	  
			d = bestEntDay(inFlight, outFlight, eType);
			HermesAgent.addToLog("Adding entertainment " + eType + " to package " + (i+1));
			currentPackage.addElement(TACAgent.CAT_ENTERTAINMENT, eType, d);
	      }
	      
	      return currentPackage;
	  }

	  private int bestEntDay(int inFlight, int outFlight, int type) {
	    for (int i = inFlight; i < outFlight; i++) {
	      int auction = agent.getAuctionFor(TACAgent.CAT_ENTERTAINMENT, type, i);
	      if (agent.getAllocation(auction) < agent.getOwn(auction)) {
			return i;
	      }
	    }
	    // If no left, just take the first...
	    return inFlight;
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
