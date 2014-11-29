package se.sics.tac.aw;


import java.util.Arrays;
import java.util.Random;

//Some indexation not to get lost...
//[0] - 1-2			
//[1] - 1-3
//[2] - 1-4
//[3] - 1-5
//[4] - 2-3
//[5] - 2-4
//[6] - 2-5
//[7] - 3-4
//[8] - 3-5
//[9] - 4-5
///////////////////////////////////////
//[0-GH day1][1-GH day2][2-GH day3][3-GH day4][4-BH day1][5-BH day2][6-BH day3][7-BH day4]
//[8-E1 day1][9-E1 day2][10-E1 day3][11-E1 day4][12-E2 day1][13-E2 day2][14-E2 day3][15-E2 day4][16-E3 day1][17-E3 day2][18-E3 day3][19-E3 day4]

public class PackageConstructor {
	
	private TACAgent agent;
	
	private final int pSize = 10; //Size of Population
	private final int length = 20; //Length of bit string for individual
	
	private int avrEntertainment = 80; // not sure there is a difference in price between different entert's...
	
	public int [] curFlight = new int[8];
	public int [] curHotel = new int[8];
	
	
	public PackageConstructor(TACAgent agent) {
		this.agent = agent;
	}
	
	private void createFlagString(int[] FlagString, int[] WhatWeHave) // Copies stuff from what we have(including -1 for hotels we couldn't get) to FlagString; Counts number of restrictions
	{
		for (int i=0; i<length; i++)
		{
			FlagString[i] = WhatWeHave[i+8];
		}
		//System.out.println(Arrays.toString(FlagString));
		//System.out.println(Arrays.toString(WhatWeHave));
	}
	
	/**
	 * Creates initial 'all 1' population
	 * Restrictions are represented as "-1" in the string
	 */
	private void createPop(int[] FlagString, int[][] Pop)
	{
		for (int i=0; i<pSize; i++)
		{
			for (int j=0; j<length; j++)
			{
				if (FlagString[j]!=-1)
					Pop[i][j] = 1;
				else 
					Pop[i][j] = -1;
			}
		}
	}

	/**
	 * Checks if there is at least one room booked for every night between inDate and outDate
	 * either in the good hotel or bad hotel
	 */
	private boolean checkFeasible (int[][] Pop, int inDate, int outDate, int PopIndx) // Checks if the package is feasible
	{
		// Good and Bad hotel counters
		int cG=0;
		int cB=0;
		
		for (int i=inDate; i<outDate; i++) {
			if (Pop[PopIndx][i+3] == 1)
				cG++;
			if (Pop[PopIndx][i-1] == 1) //shift through string to get to BH guys.
				cB++;
		}
		
		if (cG == outDate-inDate || cB == outDate-inDate)
		{
			return true;
			//cout << "True" << endl;
		}
		else
		{
			//cout << "False" << endl;
			return false;
		}
	}
	
	private int evaluationFunction (int[] WhatWeHave, int[][] Pop, int[] FlagString, int[] ClientPref, int PopIndx) //THE FUNCTION!!!!
	{
		////////////////////////////////////////////////////////INITIALIZATION///////////////////////////////////////////////////////
		int cH=0;							// another Hotel counter... that is the last one, i promise...
		int[] cE = new int[4];				// entertainment counters array. We have 4 and not 3 because... well... it doesn't work otherwise...
		int[][] E = new int[3][4];			// small matrix. 3 - number of entr, 4 - day
		int[] EVal = new int[3];			// preference values for entr for client
		int inDate=0;
		int outDate=0;
		int Utility=0;						//That's the thing we're looking for=)
		
		////////////////////////////////////////////////////DETERMINING POP FEATURES/////////////////////////////////////////////////

		switch (PopIndx) 					// Get in/out flight dates into corresponding var's... Looks bad, i know...
		{
		case 0:
			inDate=1;
			outDate=2;
			if (WhatWeHave[0]==1)			
				Utility+=curFlight[0]-5;		
			if (WhatWeHave[4]==1)
				Utility+=curFlight[4]-5;
			break;
		case 1:
			inDate=1;
			outDate=3;
			if (WhatWeHave[0]==1)
				Utility+=curFlight[0]-5;
			if (WhatWeHave[5]==1)
				Utility+=curFlight[5]-5;
			break;
		case 2:
			inDate=1;
			outDate=4;
			if (WhatWeHave[0]==1)
				Utility+=curFlight[0]-5;
			if (WhatWeHave[6]==1)
				Utility+=curFlight[6]-5;
			break;
		case 3:
			inDate=1;
			outDate=5;
			if (WhatWeHave[0]==1)
				Utility+=curFlight[0]-5;
			if (WhatWeHave[7]==1)
				Utility+=curFlight[7]-5;
			break;
		case 4:
			inDate=2;
			outDate=3;
			if (WhatWeHave[1]==1)
				Utility+=curFlight[1]-5;
			if (WhatWeHave[5]==1)
				Utility+=curFlight[5]-5;
			break;
		case 5:
			inDate=2;
			outDate=4;
			if (WhatWeHave[1]==1)
				Utility+=curFlight[1]-5;
			if (WhatWeHave[6]==1)
				Utility+=curFlight[6]-5;
			break;
		case 6:
			inDate=2;
			outDate=5;
			if (WhatWeHave[1]==1)
				Utility+=curFlight[1]-5;
			if (WhatWeHave[7]==1)
				Utility+=curFlight[7]-5;
			break;
		case 7:
			inDate=3;
			outDate=4;
			if (WhatWeHave[2]==1)
				Utility+=curFlight[2]-5;
			if (WhatWeHave[6]==1)
				Utility+=curFlight[6]-5;
			break;
		case 8:
			inDate=3;
			outDate=5;
			if (WhatWeHave[2]==1)
				Utility+=curFlight[2]-5;
			if (WhatWeHave[7]==1)
				Utility+=curFlight[7]-5;
			break;
		case 9:
			inDate=4;
			outDate=5;
			if (WhatWeHave[3]==1)
				Utility+=curFlight[3]-5;
			if (WhatWeHave[7]==1)
				Utility+=curFlight[7]-5;
			break;
		}
		
		/////////////////////////////////////////////////////CHECKING FOR FEASIBILITY/////////////////////////////////////////////////////
		if (checkFeasible(Pop, inDate,outDate,PopIndx))	// if feasible - add 1000, break otherwise
			Utility+=1000;
		else
			return -2000;							// we really don't want infeasible packages, do we=)
		
		
		//////////////////////////////////////////////////////////////ADDING BONUSES///////////////////////////////////////////////////////////
		
		Utility -= 100*(Math.abs(inDate - ClientPref[0]) + Math.abs(outDate - ClientPref[1])); // FLIGHTS // substract penalty for in/out dates differences
		
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		for (int i=inDate; i<outDate; i++)										// HOTELS // Check if we have a full package for a good one
		{
			if (Pop[PopIndx][i+3] == 1)
				cH++;
		}
		if (cH == outDate-inDate)												// Add pref bonus if we do...
				// Substract avr price difference between good and bad hotel*#of days
			for (int i=inDate; i<outDate; i++) {
			  		Utility -= (curHotel[i+3]-curHotel[i-1]);
			}
			Utility+=ClientPref[2];
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		for (int i=0; i<4; i++)							// ENTERTAINMENT // CAUTION!!! This part looks like shit! Filling out arrays according to our individual. 
		{
			E[0][i]= Pop[PopIndx][i+8];					// 4 days, 4 iterations, different start place on the string...
			E[1][i]= Pop[PopIndx][i+12];
			E[2][i]= Pop[PopIndx][i+16];
		}
		
		for (int i=0; i<3; i++)	
		{
			EVal[i] = ClientPref [i+3];					// saving client pref
		}
		
		for (int i=0; i<3; i++)	
		{
			cE[i] = 1;					// initializing entr counter with number of unused entr
		}
		cE[3]=0;						//filling out that useless member of cE array

		for (int i=inDate; i<outDate; i++)				// step by step because that is some really bad looking code here....
		{
			int EUMax=0;								// for each day between in/out we initialise entr Utility max at 0
			int EUsedCounter=3;							// Set to 4th unneeded value of int cE[4] at the begining. If non of the following if's work, it will stay as 3.
			//System.out.println("i=" + i);
			//System.out.println(E[0][i]);
			if (E[0][i-1]==1 && cE[0]==1)					// check if our package have this entr that day, check if we have not used 1st entr before 
			{
				EUMax=EVal[0];							// if "if" worked - copy bonus value to EUMax. would remain 0 if if didn't work
				EUsedCounter=0;							// say that we currently use entr 1 [0 is cE index]
			}
			if (E[1][i-1]==1 && EUMax<EVal[1] && cE[1]==1) // same but we also check if bonus value for that entr is bigger than our current MAX
			{
				EUMax=EVal[1];
				EUsedCounter=1;
			}
			if (E[2][i-1]==1 && EUMax<EVal[2] && cE[2]==1)
			{
				EUMax=EVal[2];
				EUsedCounter=2;
			}
			Utility+=EUMax;								// Update utility with our max bonus 
			cE[EUsedCounter]--;							// remove that entr type from avaliable
		}
	/////////////////////////////////////////////////////SUBSTRACTING FROM UTILITY FOR THINGS WE GOT/////////////////////////////////////////////////////
		Utility-= curFlight[inDate-1]+curFlight[outDate+2];//Punish flights!=)
		
		for (int i=4; i<8; i++)						// Punish good hotel!
		{
			if (Pop[PopIndx][i] == 1)				// Check if we have it in package
			{
				if (FlagString[i] == 0)				// Chek if we already own it
					Utility-=curHotel[i];			// If we don't - substract avr price
				else
					Utility-= 5;					// If we do - substract small value
			}										// Note: we do not worry about flagstring having -1 as Pop[PopIndx][i] == 1 will be false in that case
			
		}
		for (int i=0; i<4; i++)						// Punish bad hotel! (Same)
		{
			if (Pop[PopIndx][i] == 1)
			{
				if (FlagString[i] == 0)				
					Utility-=curHotel[i];
				else
					Utility-= 5;			
			}
		}

		for (int i=8; i<20; i++)					// Punish entertainment! (Same)
		{
			if (Pop[PopIndx][i] == 1)
			{
				if (FlagString[i] == 0)				
					Utility-= avrEntertainment;			
				else
					Utility-= 5;			
			}
		}
		// hm... everything seems to be punished... 
		// and looks like it is ready... THE FUNCTION is ready to be safely returned where it belongs....
		return Utility;
	}
	
	private void mutate(int[] FlagString, int[][] Pop, int PopInd)			// mutate given Pop individual
	{
		Random rd = new Random();
		int c = rd.nextInt(length);	// Get exactly 1 mutation point (we don't need to have more or less than 1 mutation at a time)
		if (FlagString[c] != -1)		// See if mutation point landed on restriction
		{
			if (Pop[PopInd][c]==1)		// do the swapping gene thing if "if" is true
					Pop[PopInd][c] = 0;
				else 
					Pop[PopInd][c]=1;
		}
		else
			mutate(FlagString, Pop, PopInd);			// if "if" is not true, recall this function and hope it won't land on -1 that time (saves us some time)
	}

	private Package stringToPackage(int[] s, int client, int utility) {
		Package p = new Package(client);
		for (int i=0 ; i < s.length ; i++){
			if (s[i]==1){
				p.addElement(i);
				//HermesAgent.addToLog("Adding " + agent.getAuctionTypeAsString(i) + " to package " + (client+1));
			}
		}
		p.addUtility(utility);
		//HermesAgent.addToLog("Utility of package " + (client+1) + ": " + utility);
		return p;
	}
	
	public Package makePackage(int client, int[] w, boolean useAverage) // That is our global mega-super-thing... 
	{	
		//If we're using average prices
		if (useAverage) {
			int avrFlight = 350;	// Our estimations of avr prices...
			int avrGHotel = 70;
			int avrBHotel = 40;
			
			//Working with average prices
			for (int i=0; i < 8 ; i++){
				
				curFlight[i] = avrFlight;
				
				if (i<4){ curHotel[i] = avrBHotel;}
				else { curHotel[i] = avrGHotel; }
			}
		}
		
		// If we're using the actual current prices
		else {
			for (int i=0; i < 8 ; i++){
				curFlight[i] = (int) Math.ceil(agent.getQuote(i).getAskPrice());
			}
			for (int i=8; i < 16 ; i++){
				curHotel[i] = (int) Math.ceil(agent.getQuote(i).getAskPrice());
			}
		}
		
		int[][] Pop = new int[pSize][length]; //Population of hillclimbers
		int[] FlagString = new int[length]; // Flag constraint line (same for whole population/ different between clients)

		//Input
		int[] ClientPref = new int[6];//Input the client we need to calc for with 6 Pref param (in,out,HB,E1,E2,E3)
		int[] WhatWeHave = new int[28]; //Line from our have/can't have structure for our client
			
		//Output
		int[] BestPackage = new int[28];
		
		System.arraycopy(agent.getClientPreferences(client), 0, ClientPref, 0, ClientPref.length);
		System.arraycopy(w, 0, WhatWeHave, 0, WhatWeHave.length);
		
		int UMax=0;
		int U=0;
		int Winner=0;				// for the survival of the fittest
		
		int[] TempInd = new int[length];		//Temp Individual
		createFlagString(FlagString, WhatWeHave);						//	
		createPop(FlagString, Pop);							//
		
		for (int i=0; i<500; i++)				// 100 will be replaced by time or just a number of how many iterations we need (u can use "while" there)
		{
			for (int j=0; j<pSize; j++)			// for every 1 of 10 hillclimbers
			{
				int U1=0; int U2=0;					//Utility vars for comparrison
				
				for (int k=0; k<length; k++)	// for every gene in [jth] hillclimber
				{
					TempInd[k]=Pop[j][k];		// copy to tempInd
				}
				
				U1=evaluationFunction(WhatWeHave, Pop, FlagString, ClientPref, j);	// get Utility 
				
				mutate (FlagString, Pop, j);					// mutate that Hillclimber
				
				U2=evaluationFunction(WhatWeHave, Pop, FlagString, ClientPref, j);	// Get its new Utility
				
				if (U1>U2)					// If we made things worse
				{
					for (int k=0; k<length; k++)	// revert
					{
						Pop[j][k]=TempInd[k];		// copy back
					}
				}									 
			}
		}								

		for (int i=0; i<pSize; i++)				// now we have 10 packages... Lets do survival of the fittest contest!!!!
		{
			U=evaluationFunction(WhatWeHave, Pop, FlagString, ClientPref, i);
			//System.out.println(U);
			if (U>UMax)
			{
				UMax=U;
				Winner=i;
			}
		}
		switch (Winner)							//Beginning to form winner package... 
		{							
		case 0:									//Start by filling out first 8 bits with flights
			BestPackage[0]=1;
			BestPackage[4]=1;
			break;
		case 1:
			BestPackage[0]=1;
			BestPackage[5]=1;
			break;
		case 2:
			BestPackage[0]=1;
			BestPackage[6]=1;
			break;
		case 3:
			BestPackage[0]=1;
			BestPackage[7]=1;
			break;
		case 4:
			BestPackage[1]=1;
			BestPackage[5]=1;
			break;
		case 5:
			BestPackage[1]=1;
			BestPackage[6]=1;
			break;
		case 6:
			BestPackage[1]=1;
			BestPackage[7]=1;
			break;
		case 7:
			BestPackage[2]=1;
			BestPackage[6]=1;
			break;
		case 8:
			BestPackage[2]=1;
			BestPackage[7]=1;
			break;
		case 9:
			BestPackage[3]=1;
			BestPackage[7]=1;
			break;
		}
		for (int i=0; i<length; i++)				// fill out the rest of 20 bits.
		{
			BestPackage[i+8] = Pop[Winner][i];
		}
														// Happy end!!! Our winner is forged through struggle and impossible intellectual effort!
														// Special thanks to anyone reading this for going with it to the end!=)  You're welcome!
		
		/*
		System.out.println("Winner");	// Just checking the winner... Just in case... 			
		
		for (int i=0; i<28; i++)
		{
			System.out.print(BestPackage[i]);
		}
		System.out.println();
		 */
		
		return stringToPackage(BestPackage, client, UMax);
	}

}
