package se.sics.tac.aw.handlers;

import se.sics.tac.aw.Bid;
import se.sics.tac.aw.HermesAgent;
import se.sics.tac.aw.PackageSet;
import se.sics.tac.aw.Quote;
import se.sics.tac.aw.TACAgent;

public class FlightHandler extends Handler {
	
	public FlightHandler(TACAgent agent) {
		
		int[][] auctionHistory = new int[8][55]; 		//price history
		int timeInterval;								//current time interval
		double trend;
		
		this.agent = agent;
		
		//For all flight auctions (0-7), initialize to -1
		for (int i=0 ; i < 8 ; i++){
			for (int j=0 ; j < 8 ; j++){
				agent.setBidder(j, i, -1);
			}
		}
		timeInterval = (int) agent.getGameTime()/10000;		//Try to get how many 10s has passed since the beginning
		for (int i=0; i<8; i++)
		{
			auctionHistory[i][timeInterval]=0;				//?????Current auction price... Not sure how to get that...=/
		}
		
	}
	
	
	
	//       *******************Trend prediction (CAUTION!! A lot of mess detected!! Brace yourself b4 proceeding!!)*********************************
	
	void Solver(double values[2][3], double& w, double& c)		// Not sure about how it works in java... This function gives us our inclanation and starting value
{
    double delta = values[0][0] * values[1][1] - values[1][0] * values[0][1];
    double delta_w = values[0][1] * values[1][2] - values[1][1] * values[0][2];
    double delta_c = values[0][0] * values[1][2] - values[1][0] * values[0][2];
    c = -delta_w / delta;
    w = delta_c / delta;
	//c=values[1][0]*values[0][2]/(values[1][0]*values[0][1]+values[1][1])-values[1][2]*values[0][0]/(values[1][0]*values[0][1]+values[1][1]);
	//w=values[0][2]/values[0][0]-values[0][1]/values[0][0]*c;
}
 
double TrendCalc()												// Calculates and returns inclanation
{
    int[][] PriceMat = new int[8][55];				//should be our auctionHistory from above. 
	int s=0;										//counter of how many idexies are already filled
	double[][] values = new double[2][3];			//Equation solver values
	double[][] EMat = new double[8][2];				//matrix of linear parameters for all auctions


	for (int i=0; i<8; i++)				
		{
			for (int j=0; j<55; j++)
			{
				PriceMat[i][j]=0;
			}
		}

	//  Test filling
	PriceMat[0][0]=219;
	PriceMat[0][1]=231;
	PriceMat[0][2]=228;
	PriceMat[0][3]=233;
	PriceMat[0][4]=245;
	PriceMat[0][5]=257;
	PriceMat[0][6]=254;
	PriceMat[0][7]=265;





	for (int i=0; i<55; i++) 			//finding how many values we have
	{
		if (PriceMat[0][i]==0)
		{
			s=i;
			break;						
		}
	}

	for (int k=0; k<1; k++)
	{
		double w, c;
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
			values[0][2]+=PriceMat[k][i];
			values[1][0]+=i+1;
			values[1][1]+=(i+1)*(i+1);
			values[1][2]+=PriceMat[k][i]*(i+1);
		}
		//values[1][2]*=s;

		Solver(values, w, c);								//Not sure how to convert that one
		EMat[k][0]=w;
		EMat[k][1]=c;

		
	}

	return EMat[0][0];										// we actually need only inclanation, not initial shift
}
	

	
	/* "Inflight 1", "Inflight 2", "Inflight 3", "Inflight 4",
	    "Outflight 2", "Outflight 3", "Outflight 4", "Outflight 5",*/
	
	//New one
	public void sendSeparateBids(int auction, PackageSet packageSet){
		//TODO Decide when to send them
		
		Bid bid = new Bid(auction);
		
		
		
		for (int client=0; client < 8 ; client++){
			//If the client wants something from this auction
			if (agent.getBidder(auction, client) != -1) {
				if (auctionHistory[client][timeInterval]>450 || trend>5)   //i'm so lost in what can be used where...
				{
					//TODO Put here the calculation for the price
					//int utilityPackage = packageSet.get(client).getUtility();
					int price = auctionHistory[client][timeInteral]; 			// current auction price
				
				
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
