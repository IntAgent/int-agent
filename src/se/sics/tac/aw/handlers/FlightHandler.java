package se.sics.tac.aw.handlers;

import se.sics.tac.aw.Bid;
import se.sics.tac.aw.HermesAgent;
import se.sics.tac.aw.PackageSet;
import se.sics.tac.aw.Quote;
import se.sics.tac.aw.TACAgent;

public class FlightHandler extends Handler {
	
	int[][] auctionHistory;
	int timeInterval;								//current time interval
	double[] trend;
	
	public FlightHandler(TACAgent agent) {
	
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
	
	void Initialisation ()
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
	
	double[] Solver(double [][]values, double w, double c)
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
 
	double TrendCalc(int auction)												// Calculates and returns inclanation
{
    //int[][] PriceMat = new int[8][55];				//should be our auctionHistory from above. 
	int s=0;										//counter of how many idexies are already filled
	double[][] values = new double[2][3];			//Equation solver values
	double[] EMat = new double[2];				//matrix of linear parameters for all auctions


	
	//  Test filling
	auctionHistory[0][0]=219;
	auctionHistory[0][1]=231;
	auctionHistory[0][2]=228;
	auctionHistory[0][3]=233;
	auctionHistory[0][4]=245;
	auctionHistory[0][5]=257;
	auctionHistory[0][6]=254;
	auctionHistory[0][7]=265;





	for (int i=0; i<55; i++) 			//finding how many values we have
	{
		if (auctionHistory[0][i]==0)
		{
			s=i;
			break;						
		}
	}

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
		//TODO Decide when to send them
		
		Bid bid = new Bid(auction);
		
		timeInterval = (int) agent.getGameTime()/10000;		//Try to get how many 10s has passed since the beginning
		for (int i=0; i<8; i++)
		{
			auctionHistory[i][timeInterval]=(int) agent.getQuote(i).getAskPrice();		//Hope it works
		}
		for (int i=0; i<8; i++)
		{
			trend[i]=TrendCalc(i);
		}
		
		for (int client=0; client < 8 ; client++){
			//If the client wants something from this auction
			if (agent.getBidder(auction, client) != -1) {
				if (auctionHistory[client][timeInterval]>450 || trend[auction]>5)   	//if the price is above 450 and we still need it - buy b4 too late
				{																		//or if it climbs too fast - do the same
					//TODO Put here the calculation for the price
					//int utilityPackage = packageSet.get(client).getUtility();
					int price = auctionHistory[client][timeInterval]; 			// current auction price
				
				
					//FlightHandler.TrendCalc();
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
