package se.sics.tac.aw.handlers;

import java.util.logging.Logger;

import se.sics.tac.aw.Quote;
import se.sics.tac.aw.TACAgent;

public abstract class Handler {
	protected TACAgent agent;
	protected float[] prices;
	protected Logger log;

	public abstract void quoteUpdated(Quote quote, int auction);

	public abstract void sendBids(int i);
	
}
