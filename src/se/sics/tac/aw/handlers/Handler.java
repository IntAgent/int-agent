package se.sics.tac.aw.handlers;

import se.sics.tac.aw.PackageSet;
import se.sics.tac.aw.Quote;
import se.sics.tac.aw.TACAgent;

public abstract class Handler {
	protected TACAgent agent;
	protected float[] prices;

	public abstract void quoteUpdated(Quote quote, int auction, PackageSet packageSet);

	public abstract void sendInitialBids(int i, PackageSet packageSet);
	
}
