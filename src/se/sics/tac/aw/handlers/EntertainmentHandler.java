package se.sics.tac.aw.handlers;

import se.sics.tac.aw.Bid;
import se.sics.tac.aw.HermesAgent;
import se.sics.tac.aw.PackageSet;
import se.sics.tac.aw.Quote;
import se.sics.tac.aw.TACAgent;

public class EntertainmentHandler extends Handler {

	//protected int[][] maxPrice = new int[8][3];
	private float averageBidPrice;


	///////////////////////////////////////////////////////////

	public EntertainmentHandler(TACAgent agent) 
	{
		this.agent = agent;
		averageBidPrice = 80f;
		//calculateMaxPrice();

	}

	///////////////////////////////////////////////////////////
	/*   
   public void calculateMaxPrice()
   {
      for(int client=0; client< 8; client++)
         for( int type=3 ; type< 6; type++)//for the enter. types from 3->6
            maxPrice[client][type] = agent.getClientPreference(client, type);

   }
	 */
	///////////////////////////////////////////////////////////   



	//this method is called at the start of the game
	public void sendInitialBids(int auction, PackageSet packageSet) 
	{
		//try to buy some tickets at price zero.
		Bid bid = new Bid(auction);

		bid.addBidPoint(1, 0f);

		HermesAgent.addToLog("$$$ sendBids $$$ at the beginning of the game - auction: " + auction);

		//update bidders with the last price was bided on
		//agent.setBidder(auction, client, value);
		//cannot use it because I am buying tickets in general NOT for a specific client here

		agent.submitBid(bid);                     
	}  


	//During the game when the quoteUpdated method is called every 10 seconds, do the following
	@Override
	public void quoteUpdated(Quote quote, int auction, PackageSet packageSet) 
	{
		//what is the time now?
		long time = agent.getGameTime();

		//before 8:00
		if( time <= 480000l)
		{
			buyDuringGame(quote, auction, packageSet);
			//sellDuringGame(quote, auction);
		}
		else//after 8:00
		{
			buyAtTheEnd(quote, auction, packageSet);
			sellAtTheEnd(quote, auction);

			//NEED TO MODIFY IT!
			//I want this method to check all the auctions not this specific one!
			//sellAllocatedTickets(quote, auction);			
		}

	}


	//buy at a low price and increment it by time until client preference
	private void buyDuringGame(Quote quote, int auction, PackageSet packageSet)
	{
		//make a new buying bid with quantity = 1 and  price = time in minutes * clientPrefrence/8;

		float buyingPrice=0f;
		Bid buyingBid = new Bid(auction);

		//find empty slots we need to fill with tickets
		for(int client=0; client<8; client++){
			if(packageSet.get(client).isInPackage(auction) && !packageSet.get(client).hasBeenObtained(auction))//is this client interested in this auction   
			{
				//this auction for which entertainment type?
				//16,17,18,19 -> type 3(AW)
				//20,21,22,23 -> type 4 (AP)
				//24,25,26,27 -> type 5 (M)
				int type =3;
				if( auction >= 20 && auction<=23)
					type = 4;
				else if (auction >= 24 && auction<=27 )
					type = 5;

				buyingPrice = (agent.getGameTime()/60000f) * (agent.getClientPreference(client, type)/8f);

				//TESTING
				HermesAgent.addToLog("$$$ buyDuringGame $$$ auction: " + auction + " - client: " + client + "price: " + buyingPrice );

				buyingBid.addBidPoint(1, buyingPrice);
			}
		}

		if (buyingBid.getNoBidPoints() > 0){
			HermesAgent.addToLog("ReplaceBid");
			agent.replaceBid(agent.getBid(auction), buyingBid);
		}

	}


	//I'll try to buy this ticket which I need at the end of the game for the ask price if it is less than the client preference 
	private void buyAtTheEnd(Quote quote, int auction, PackageSet packageSet)
	{ 
		Bid buyingBid = new Bid(auction);

		//find empty slots we need to fill with tickets
		for(int client=0; client< 8; client++){
			if(packageSet.get(client).isInPackage(auction) && !packageSet.get(client).hasBeenObtained(auction))//is this client is interested in this auction   
			{
				float askPrice = quote.getAskPrice();

				//this auction for which entertainment type?
				//16,17,18,19 -> type 3(AW)
				//20,21,22,23 -> type 4 (AP)
				//24,25,26,27 -> type 5 (M)
				int type =3;
				if( auction >= 20 && auction<=23)
					type = 4;
				else if (auction >= 24 && auction<=27 )
					type = 5;


				if( askPrice < agent.getClientPreference(client, type))
				{
					buyingBid.addBidPoint(1, askPrice);

					//TESTING
					HermesAgent.addToLog("$$$ buyAtTheEndGame $$$ auction: " + auction + " - client: " + client + "price: " + askPrice );
				}
			}
		}

		if (buyingBid.getNoBidPoints() > 0){
			HermesAgent.addToLog("ReplaceBid");
			agent.replaceBid(agent.getBid(auction), buyingBid);
		}

	}



	private void sellAtTheEnd(Quote quote, int auction)
	{
		//at the end, we try as we could to sell any extra tickets by the bid price , 
		//but this bid price should be less than the cost of this ticket.

		//I have to figure out how to save all the cost info.

		//find extra tickets we have
		int extraTickets = agent.getOwn(auction) - agent.getAllocation(auction);
		//or by using spareResources
		//int extraTickets = agent.getSpareResources(auction);

		if(extraTickets > 0)
		{
			Bid sellingBid = new Bid(auction);

			float bidPrice = quote.getBidPrice();

			//ticket cost is zero if this ticket is from the endowment, otherwise it is the cost for buying this ticket
			float ticketCost =0;

			//TESTING
			HermesAgent.addToLog("$$$ sellAtTheEndGame $$$ auction: " + auction +" - bid price: " + bidPrice + " - cost: 0 - extraTickets: " + extraTickets );

			if( bidPrice > ticketCost)
			{
				sellingBid.addBidPoint(-1*extraTickets, bidPrice);

				agent.replaceBid(agent.getBid(auction), sellingBid);
			}

		}

	}

}


/*
//this is the case if I cannot have more than one bid in a single auction
int quantity =0;
int averageClientsPrefer =0;

for(int client=0; client< 8; client++)
	if( agent.getBidder(auction, client) == 0 )//is this client interested in this auction   
	   {
	   //this auction for which entertainment type?
	   //16,17,18,19 -> type 3(AW)
	   //20,21,22,23 -> type 4 (AP)
	   //24,25,26,27 -> type 5 (M)
	   int type =3;
	   if( auction >= 20 && auction<=23)
		   type = 4;
	   else if (auction >= 24 && auction<=27 )
		   type = 5;

		averageClientsPrefer = averageClientsPrefer +  agent.getClientPreference(client, type);
	   quantity++;
	   }

averageClientsPrefer = averageClientsPrefer / quantity;

buyingPrice = (agent.getGameTime()/60000f) * (averageClientsPrefer/7f);

buyingBid.addBidPoint(quantity, buyingPrice);

//update the last bidding price in bidders
//agent.setBidder(auction, client, (int)buyingPrice);

agent.submitBid(buyingBid);		
 */


//there should be NO SELLING DURING THE GAME
/*
public void sellDuringGame(Quote quote, int auction)
{
	//I may need this ticket ,so it is better NOT to sell any until nearly the end of the game

	//make a new selling bid with quantity =1 and price = 200 - time in minutes *( 200-average/9);

    float sellingPrice=0f;
	float incAvg = (200f - averageBidPrice)/9;

	Bid sellingBid = new Bid(auction);
    //find extra tickets we have
	int extraTickets = agent.getOwn(auction) - agent.getAllocation(auction);

	if(extraTickets > 0)
	{
		sellingPrice = 200f - (agent.getGameTime()/60000f) * incAvg;

		sellingBid.addBidPoint(-1*extraTickets, sellingPrice);

		agent.submitBid(sellingBid);	
	}
}
 */

/*
	//at the end of the game and when it is guarantee that there will be no more re-calculating packages, 
	//I can sell any allocated tickets at the bid price if that bid price is higher than the benefit that this ticket is going to bring me(in other words the client preference) 
	public void sellAllocatedTickets( Quote quote, int auction )
	{	
		// this is WRONG!


		// check when the allocation process takes place!

		//search through all the tickets that we own..
		for(int client=0; client< 8; client++)
			if( agent.getBidder(auction, client) == 0 )//is this client interested in this auction   
			   {
				   float bidPrice = quote.getBidPrice();

				   //this auction for which entertainment type?
				   //16,17,18,19 -> type 3 (AW)
				   //20,21,22,23 -> type 4 (AP)
				   //24,25,26,27 -> type 5 (M)
				   int type =3;
				   if( auction >= 20 && auction<=23)
					   type = 4;
				   else 
					   if (auction >= 24 && auction<=27 )
					   type = 5;

				   if( bidPrice > agent.getClientPreference(client, type))
				   {
					   Bid sellingBid = new Bid(auction);

					   sellingBid.addBidPoint(-1, bidPrice);

					   agent.submitBid(sellingBid); 
				   }
			   }
	}
 */