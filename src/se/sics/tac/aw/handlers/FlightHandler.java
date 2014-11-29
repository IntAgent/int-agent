package se.sics.tac.aw.handlers;

import se.sics.tac.aw.Bid;
import se.sics.tac.aw.HermesAgent;
import se.sics.tac.aw.PackageSet;
import se.sics.tac.aw.Quote;
import se.sics.tac.aw.TACAgent;

public class FlightHandler extends Handler {
	
	private int[][] auctionHistory;
	private int timeInterval;								//current time interval
	private double[] trend;
	
	public FlightHandler(TACAgent agent) {
	
		timeInterval = -1;
		auctionHistory = new int[8][55]; 		//price history
		trend = new double[8];
		this.agent = agent;
		
		//For all flight auctions (0-7), initialize to -1
		for (int i=0 ; i < 8 ; i++){
			for (int j=0 ; j < 8 ; j++){
				agent.setBidder(j, i, -1);
			}
		}
		
		Initialisation();
	}
	
	private void Initialisation ()
	{
		//Initialise auctionHistory matrix
				for (int i=0; i<8; i++)				
				{
					for (int j=0; j<55; j++)
					{
						auctionHistory[i][j]=0;
					}
				}
				HermesAgent.addToLog("TryInitialisation");
	}	
	
	//       *******************Trend prediction (CAUTION!! A lot of mess detected!! Brace yourself b4 proceeding!!)*********************************
	
	private double[] Solver(double [][]values, double w, double c)
{
    double delta = values[0][0] * values[1][1] - values[1][0] * values[0][1];
    double delta_w = values[0][1] * values[1][2] - values[1][1] * values[0][2];
    double delta_c = values[0][0] * values[1][2] - values[1][0] * values[0][2];
    double[] param = new double [2];
    c = -delta_w / delta;
    w = delta_c / delta;
	param[0]=w;
	param[1]=c;
	return param;
}
 
	private double TrendCalc(int auction) {			// Calculates and returns inclination
    //int[][] PriceMat = new int[8][55];				//should be our auctionHistory from above. 
	int s=0;										//counter of how many idexies are already filled
	double[][] values = new double[2][3];			//Equation solver values
	double[] EMat = new double[2];				//matrix of linear parameters for all auctions

	
	//  Test filling
	/*
	auctionHistory[0][0]=219;
	auctionHistory[0][1]=231;
	auctionHistory[0][2]=228;
	auctionHistory[0][3]=233;
	auctionHistory[0][4]=245;
	auctionHistory[0][5]=257;
	auctionHistory[0][6]=254;
	auctionHistory[0][7]=265;
	 */
	
	s = timeInterval;
	
	double w = 0;
	double c = 0;
	// Filing out our values									
	for (int i=0; i<2; i++)				
	{
		for (int j=0; j<3; j++)
		{
			values[i][j]=0;
		}
	}
	
	values[0][0]=s;

	for (int i=0; i<s; i++)
	{
		values[0][1]+=i+1;
		values[0][2]+=auctionHistory[auction][i];
		values[1][0]+=i+1;
		values[1][1]+=(i+1)*(i+1);
		values[1][2]+=auctionHistory[auction][i]*(i+1);
	}
	//values[1][2]*=s;

	EMat=Solver(values, w, c);								//Not sure how to convert that one
	//EMat[client][0]=w;
	//EMat[client][1]=c;
	//trend[auction];
	return EMat[0];
	}

	

	
	/* "Inflight 1", "Inflight 2", "Inflight 3", "Inflight 4",
	    "Outflight 2", "Outflight 3", "Outflight 4", "Outflight 5",*/
	
	//New one
	public void sendSeparateBids(int auction, PackageSet packageSet){
		
		Bid bid = new Bid(auction);

		auctionHistory[auction][timeInterval] = (int) Math.ceil(agent.getQuote(auction).getAskPrice());
		
		HermesAgent.addToLog("Price for auction " + auction + " is now: " + auctionHistory[auction][timeInterval]);
		
		if (timeInterval==0 && auctionHistory[auction][timeInterval]<300)		//Initial <300 buying
		{
			HermesAgent.addToLog("Meets initial criteria of <300");
			
			for (int client=0; client < 8 ; client++){
			//If the client wants something from this auction
				
			if (agent.getBidder(auction, client) != -1) {

					int price = auctionHistory[auction][timeInterval]; 			// current auction price				
					
					//Put the latest price in the matrix
					agent.setBidder(auction, client, price);
				
					//Bid for that client
					bid.addBidPoint(1, price);
				}
			}
		}
		
		if (auctionHistory[auction][timeInterval]>450 && timeInterval<7 && timeInterval!=0)   	//if the price is above 450 and we still need it - buy b4 too late
		{																		//or if it climbs too fast - do the same
		
			HermesAgent.addToLog("Meets the criteria of b4 sec 70 buy!");
			
			for (int client=0; client < 8 ; client++){
			//If the client wants something from this auction
				
			if (agent.getBidder(auction, client) != -1) {

					int price = auctionHistory[auction][timeInterval]; 			// current auction price				
					
					//Put the latest price in the matrix
					agent.setBidder(auction, client, price);
				
					//Bid for that client
					bid.addBidPoint(1, price);
				}
			}
		}
		
		if (timeInterval >= 7 && timeInterval < 48)
		{
			trend[auction] = TrendCalc(auction);
		
			HermesAgent.addToLog("Trend for auction " + auction + " is now: " + trend[auction]);
		
			if (auctionHistory[auction][timeInterval]>450 || trend[auction]>6-0.25*timeInterval)   	//if the price is above 450 and we still need it - buy b4 too late
			{																		//or if it climbs too fast - do the same
		
				HermesAgent.addToLog("Meets the criteria to buy!");
			
				for (int client=0; client < 8 ; client++){
					//If the client wants something from this auction
				
					if (agent.getBidder(auction, client) != -1) {

						int price = auctionHistory[auction][timeInterval]; 			// current auction price				
					
						//Put the latest price in the matrix
						agent.setBidder(auction, client, price);
				
						//Bid for that client
						bid.addBidPoint(1, price);
					}
				}
			}
		}
		
		if (timeInterval >= 48)
		{
			HermesAgent.addToLog("Meets the criteria to buy!");
			
			for (int client=0; client < 8 ; client++){
				//If the client wants something from this auction
			
				if (agent.getBidder(auction, client) != -1) {

					int price = auctionHistory[auction][timeInterval]; 			// current auction price				
				
					//Put the latest price in the matrix
					agent.setBidder(auction, client, price);
			
					//Bid for that client
					bid.addBidPoint(1, price);
				}
			}
		}
		
		//If there is at least one bid to be sent
		if (bid.getNoBidPoints() > 0){
			agent.submitBid(bid);
		}
	}

	@Override
	public void quoteUpdated(Quote quote, int auction) {
		//we update the current time only once per 10 sec, let's say at the first auction
		if (auction == 0) {
			timeInterval++;
			HermesAgent.addToLog("TimeInterval variable is now " + timeInterval);
		}
		
		sendSeparateBids(auction, null);
	}

	@Override
	public void sendBids(int i) {}


}
