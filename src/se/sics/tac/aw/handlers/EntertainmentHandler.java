package se.sics.tac.aw.handlers;

import se.sics.tac.aw.Bid;
import se.sics.tac.aw.HermesAgent;
import se.sics.tac.aw.PackageSet;
import se.sics.tac.aw.Quote;
import se.sics.tac.aw.TACAgent;

public class EntertainmentHandler extends Handler {

//protected int[][] maxPrice = new int[8][3];
protected float averageBidPrice;


///////////////////////////////////////////////////////////

	public EntertainmentHandler(TACAgent agent) 
   {
		this.agent = agent;
		averageBidPrice = 80f;
		//calculateMaxPrice();
      
     /* //initializing the bidder matrix
      //as a default value don't bid on any auction
       for(int auction=16; auction<28;auction++)
    	   for(int client=0; client<8; client++)
    		   agent.deleteBidder(client, auction);
*/
	}
 
    //this method is called at the start of the game
   @Override
	public void sendInitialBids(int auction, PackageSet packageSet)
   {
		//try to buy some tickets at price zero.
        Bid bid = new Bid(auction);
        
        bid.addBidPoint(1, 0f);
        
        agent.submitBid(bid);   
        
        HermesAgent.addToLog("@@@@@@@@@ sendBids @@@@@@@@@ at the beginning of the game - auction: " + auction);                  
   }  

	
	//During the game when the quoteUpdated method is called every 10 seconds, do the following
	@Override
	public void quoteUpdated(Quote quote, int auction, PackageSet packageSet) 
   {
		//what is the time now?
		long time = agent.getGameTime();
		
		
		//Find a dynamic strategy for buying and selling , so you consider your benfit against others
		//before 8:00
		if( time <= 480000l)
		{
			buyDuringGame(quote, auction, packageSet);
			//sellDuringGame(quote, auction);
		}
		else//after 8:00
		{
			//if i have extra tickets -->sell
			if( agent.getOwn(auction) - agent.getAllocation(auction) >0)
			
			sellAtTheEnd(quote, auction, packageSet);
			else
				//buyAtTheEnd(quote, auction, packageSet);
				buyDuringGame(quote, auction, packageSet);
			
			//NEED TO MODIFY IT!
			//I want this method to check all the auctions not this specific one!
			//sellAllocatedTickets(quote, auction);			
		}
			
   }

	
	//buy at a low price and increment it by time until client preference
	public void buyDuringGame(Quote quote, int auction, PackageSet packageSet)
	{
		//make a new buying bid with quantity = 1 and  price = time in minutes * clientPrefrence/8;
		
		float buyingPrice=0f;
		Bid buyingBid = new Bid(auction);
		int cp; 
		float percentage=0f;
		float maxBuyingPrice =0f;
		
		
      //find empty slots we need to fill with tickets
		for(int client=0; client<8; client++)
		{
			cp = agent.getClientPreference(client, getAuctionType(auction));
			if (packageSet.get(client).isInPackage(auction) && !packageSet.get(client).hasBeenObtained(auction))   
			{
				buyingPrice = (agent.getGameTime()/60000f) * (cp/9f);
			   
            //update the last bidding price in bidders
			//agent.setBidder(auction, client, (int)buyingPrice);
            
            //dynamic
				percentage = (200 - cp) /2;
				maxBuyingPrice = percentage/100 * cp;
				
				if( quote.getAskPrice() <= maxBuyingPrice )
					buyingBid.addBidPoint(1, buyingPrice);
				
				//TESTING
				HermesAgent.addToLog("@@@@@@@@@ buyDuringGame @@@@@@@@@ auction: " + auction + " - client: " + client + "price: " + buyingPrice + " percentage: "+percentage+ " Max buying price: "+ maxBuyingPrice );
			 }
		}
		
		if(buyingBid.getNoBidPoints()>0)
            agent.replaceBid(agent.getBid(auction), buyingBid);

	}
   


	
	
public void sellAtTheEnd(Quote quote, int auction, PackageSet packageSet)
	{
		//at the end, we try as we could to sell any extra tickets by the bid price , 
      //but this bid price should be less than the cost of this ticket.
		
      //I have to figure out how to save all the cost info.
        
		//find extra tickets we have
		int extraTickets = agent.getOwn(auction) - agent.getAllocation(auction);	
		
      //TESTING
      HermesAgent.addToLog("@@@@@@@@@ sellAtTheEndGame @@@@@@@@@ auction: " + auction +" own :"+ agent.getOwn(auction)+ " Allocated: "+agent.getAllocation(auction) );
         
		if(extraTickets > 0)
		{
			Bid sellingBid = new Bid(auction);
			
			float bidPrice = quote.getBidPrice();
			
			//ticket cost is zero if this ticket is from the endowment, otherwise it is the cost for buying this ticket
			
         //TESTING
            HermesAgent.addToLog("@@@@@@@@@ sellAtTheEndGame @@@@@@@@@ auction: " + auction +" - bid price: " + bidPrice + " - cost: 0 - extraTickets: " + extraTickets );
         
            //bid price should be higher than 100 so I will get benefit in all the situations
			if( bidPrice-1 > 100)
			{
				sellingBid.addBidPoint(-1*extraTickets, bidPrice+1);

				agent.replaceBid(agent.getBid(auction), sellingBid);
			}
	
		}
		
	}

   
private int getAuctionType( int auction)
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
               
               return type;
   
   }
	



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
    

/*
		//I'll try to buy this ticket which I need at the end of the game for the ask price if it is less than the client preference 
	public void buyAtTheEnd(Quote quote, int auction, PackageSet packageSet)
	{ 
		   Bid buyingBid = new Bid(auction);
			float askPrice = quote.getAskPrice();
      	
		//find empty slots we need to fill with tickets
		for(int client=0; client< 8; client++)
			if (packageSet.get(client).isInPackage(auction) && !packageSet.get(client).hasBeenObtained(auction))   
		   { 
			   
			   if( askPrice < agent.getClientPreference(client, getAuctionType(auction)))
			   {
				   buyingBid.addBidPoint(1, askPrice);
            
			      //agent.setBidder(auction, client, (int)askPrice);

            //TESTING
            HermesAgent.addToLog("@@@@@@@@@ buyAtTheEndGame @@@@@@@@@ auction: " + auction + " - client: " + client + "price: " + askPrice );
			 }
      }
			   		   
			if(buyingBid.getNoBidPoints()>0)
         agent.replaceBid(agent.getBid(auction), buyingBid); 
      else
         {
         buyingBid.addBidPoint(1, 0f);
         agent.replaceBid(agent.getBid(auction), buyingBid);
         }
	}
*/


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