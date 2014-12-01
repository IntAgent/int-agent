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

		values[0][0]=7;

		for (int i=s-7; i<s; i++)
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


	private void sendSeparateBids(int auction, PackageSet packageSet){

		double gameTime = (double) agent.getGameTime();
		gameTime/=10000;

		auctionHistory[auction][timeInterval] = (int) Math.ceil(agent.getQuote(auction).getAskPrice());

		HermesAgent.addToLog("Price for auction " + auction + " is now: " + auctionHistory[auction][timeInterval]);

		if (gameTime < 1 && auctionHistory[auction][timeInterval]<300)		//Initial <300 buying
		{
			HermesAgent.addToLog("Meets initial criteria of price < 300");

			buyAtCurrentPrice(auction);
		}

		else if (auctionHistory[auction][timeInterval]>420 && gameTime<=7)   	//if the price is above 450 and we still need it - buy b4 too late
		{																		//or if it climbs too fast - do the same

			HermesAgent.addToLog("Meets the criteria of price>450 in the first 70 sec");

			buyAtCurrentPrice(auction);
		}

		else if (gameTime > 7 && gameTime < 48)
		{
			trend[auction] = TrendCalc(auction);

			HermesAgent.addToLog("Trend for auction " + auction + " is now: " + trend[auction]);

			if (auctionHistory[auction][timeInterval]>450 || trend[auction]>4-0.08*timeInterval)   	//if the price is above 450 and we still need it - buy b4 too late
			{																		//or if it climbs too fast - do the same

				HermesAgent.addToLog("Meets the criteria of price>450 and trend going up too fast");

				buyAtCurrentPrice(auction);
			}
		}

		else if (gameTime >= 48)
		{
			HermesAgent.addToLog("Meets the criteria of time>7min");

			buyAtCurrentPrice(auction);
		}
	}

	private void buyAtCurrentPrice(int auction) {

		Bid bid = new Bid(auction);

		HermesAgent.addToLog("Allocation for this auction: " + agent.getAllocation(auction));

		int alloc = agent.getAllocation(auction) - agent.getOwn(auction);

		if (alloc > 0) {
			int price = auctionHistory[auction][timeInterval]; 			// current auction price

			//Bid for that client
			bid.addBidPoint(alloc, price);
		}

		//If there is at least one bid to be sent
		if (bid.getNoBidPoints() > 0){
			HermesAgent.addToLog("-> Sending bid for auction " + auction);
			agent.submitBid(bid);
		}
	}



	@Override
	public void quoteUpdated(Quote quote, int auction, PackageSet packageSet) {
		//we update the current time only once per 10 sec, let's say at the first auction
		if (auction == 0) {
			timeInterval++;
			HermesAgent.addToLog("TimeInterval variable is now " + timeInterval);
		}

		sendSeparateBids(auction, null);
	}

	@Override
	public void sendInitialBids(int i, PackageSet packageSet) {
		// No initial bid for flights
	}


}
