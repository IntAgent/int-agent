package se.sics.tac.aw.handlers;

import se.sics.tac.aw.Bid;
import se.sics.tac.aw.HermesAgent;
import se.sics.tac.aw.PackageSet;
import se.sics.tac.aw.Quote;
import se.sics.tac.aw.TACAgent;

public class EntertainmentHandler extends Handler 
{

	protected static float averageBidPrice;
	
	

	public EntertainmentHandler(TACAgent agent) 
   {
		this.agent = agent;
		averageBidPrice = 80f;
   }
 
    
	//this method is called at the start of the game
   @Override
	public void sendInitialBids(int auction, PackageSet packageSet)
   {
        Bid bid = new Bid(auction);
      
        bid.addBidPoint(1, 5f);
        
        bid.addBidPoint(-1, 9000f);
        
        agent.submitBid(bid);   
        
        HermesAgent.addToLog("@@@@@@@@@ sendIniialBids @@@@@@@@@ at the beginning of the game - auction: " + auction);                  
   }  

	
	//During the game when the quoteUpdated method is called every 10 seconds, do the following
	@Override
	public void quoteUpdated(Quote quote, int auction, PackageSet packageSet) 
   {
		//long time = agent.getGameTime();
		
		int extraTickets = agent.getOwn(auction) - agent.getAllocation(auction);
		
		//in strategy 1 , time matters
		if(HermesAgent.strategy == 1)
		{
			if(extraTickets > 0)
				sellDuringGame1(quote, auction, packageSet, extraTickets);
			else
				buyDuringGame(quote, auction, packageSet);
			
			
		}
		//strategy = 2
		//time does not matter here
		else
		{
			if(extraTickets > 0)
				sellDuringGame(quote, auction, packageSet, extraTickets);
			else
				buyDuringGame(quote, auction, packageSet);	
		}
	
			
   }
/*
 if(HermesAgent.strategy == 1)
		{
			//before 8:00
			if( time <= 480000l)
				buyDuringGame(quote, auction, packageSet);
			
			else//after 8:00
			{
				//if i have extra tickets --> sell
				if( extraTickets > 0)
					sellAtTheEnd(quote, auction, packageSet, extraTickets);
				else
					buyDuringGame(quote, auction, packageSet);
			}
			
		}
  */
 
	
	//buy at a low price and increment it by time until client preference
	public void buyDuringGame(Quote quote, int auction, PackageSet packageSet)
	{
		//make a new buying bid with quantity = 1 and  price = time in minutes * clientPrefrence/8;
		Bid bid = new Bid(auction);
		
		int cp=0;
		
		float price=0f; 
		float percentage=0f;
		float maxBuyingPrice =0f;
		
		long time = agent.getGameTime();
		
		float slope =0;
		
		
		for(int client=0; client<8; client++)
		{
			cp = agent.getClientPreference(client, getAuctionType(auction));
			
			//if this client needs a ticket
			if (packageSet.get(client).isInPackage(auction) && !packageSet.get(client).hasBeenObtained(auction))   
			{
				//dynamic
				percentage = 1-(cp/450);
				maxBuyingPrice = percentage * cp;
				
				slope = (maxBuyingPrice - 5) / (540000 - 0);
				//price = slope*time + 0
				price = slope * time +5;

				bid.addBidPoint(1, price);
				
				//TESTING
				HermesAgent.addToLog("@@@@@@@@@ buyDuringGame @@@@@@@@@ auction: " + auction + " - client: " + client + "price: " + price + " Max buying price: "+ maxBuyingPrice );
			 }
			
		}
		
		
		//if there is no bidding point
		//if(bid.getNoBidPoints()<=0)
			bid.addBidPoint(1, 5f);
			bid.addBidPoint(-1, 9000f);
			

		//is there a previous bid?
		//if( quote.getBid() != null)
			//agent.replaceBid(agent.getBid(auction), bid);
		
		//else
			agent.submitBid(bid);
		

	}
   	

	public void sellAtTheEnd(Quote quote, int auction, PackageSet packageSet, int extraTickets)
	{
		Bid bid = new Bid(auction);
		
		float price = averageBidPrice -10;
		
		bid.addBidPoint(-1*extraTickets, price);
		
		bid.addBidPoint(1, 5f);
		bid.addBidPoint(-1, 9000f);

		//is there a previous bid?
		//if( quote.getBid() != null)
			//agent.replaceBid(agent.getBid(auction), bid);
		
		//else
			agent.submitBid(bid);
		
		//bid price should be higher than 100 so I will get benefit in all the situations
	
	//TESTING
    HermesAgent.addToLog("@@@@@@@@@ sellAtTheEndGame @@@@@@@@@ auction: " + auction +" - price: " + price + " extraTickets: " + extraTickets );
  
}


	public void sellDuringGame(Quote quote, int auction, PackageSet packageSet, int extraTickets)
	{
		Bid bid = new Bid(auction);

		//float minPrice = averageBidPrice -10;
		//float maxPrice = 200;
		
		//long minTime = 0;
		//long maxTime = 540000;//9 min
		
		//long time = agent.getGameTime();
		
		int interval = (int) agent.getGameTime()/30000;
		
		//float slope =  (maxPrice - minPrice)/(maxTime - minTime);
		
		//price = slope * current time + maxPrice
		//this is a linear equation better to use a curve
		//float price = -1*slope * time + maxPrice;
		
		float price = (1000/(interval + 6)) + 30;
			
		bid.addBidPoint(-1*extraTickets, price);

		
		bid.addBidPoint(1, 5f);
		bid.addBidPoint(-1, 9000f);
		
		
		//is there a previous bid?
				//if( quote.getBid() != null)
					//agent.replaceBid(agent.getBid(auction), bid);
				
				//else
					agent.submitBid(bid);
				
		//TESTING
	    HermesAgent.addToLog("@@@@@@@@@ sellDuringGame @@@@@@@@@ auction: " + auction +" - price: " + price + " extraTickets: " + extraTickets );
			  	  
	  
	    //I divided the whole game into 30 sec intervals
	     
	  		
	    //this formula Alexey invented ,, 
	    //check where is the avg price int it?
	    
	  		
	    
	    //bid price should be higher than 100 so I will get benefit in all the situations
		//formula to calculate the selling price dynamically
		//sellingPrice = lessThanAverage + ((200-lessThanAverage)/18) * (1- ((18-time)/18));
	    
	}

	
	public void sellDuringGame1(Quote quote, int auction, PackageSet packageSet, int extraTickets)
	{
		Bid bid = new Bid(auction);
		
		int interval = (int) agent.getGameTime()/30000;
		
		//y=65*cos(x/6)+135
		//float price = 65*(float)(Math.cos(interval/6))+135;

		float price = 135/(1+(float)Math.exp(interval/2.5-4))+65;
	
		bid.addBidPoint(-1*extraTickets, price);
		
		bid.addBidPoint(1, 5f);
		bid.addBidPoint(-1, 9000f);

		agent.submitBid(bid);
				
		//TESTING
	    HermesAgent.addToLog("@@@@@@@@@ sellDuringGame1 @@@@@@@@@ auction: " + auction +" - price: " + price + " extraTickets: " + extraTickets );
		 
	}
	
	private int getAuctionType( int auction)
	{
		//this auction for which entertainment type?
		//16,17,18,19 -> type 3(AW)
		//20,21,22,23 -> type 4 (AP)
		//24,25,26,27 -> type 5 (M)
		
		int type=3;
            
		if( auction >= 20 && auction<=23)
			type = 4;
		else 
			if (auction >= 24 && auction<=27 )
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