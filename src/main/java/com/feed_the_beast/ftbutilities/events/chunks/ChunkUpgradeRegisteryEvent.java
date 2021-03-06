package com.feed_the_beast.ftbutilities.events.chunks;

import com.feed_the_beast.ftbutilities.data.ChunkUpgrade;
import com.feed_the_beast.ftbutilities.events.FTBUtilitiesEvent;

import java.util.function.Consumer;

/**
 * @author LatvianModder
 */
public class ChunkUpgradeRegisteryEvent extends FTBUtilitiesEvent
{
	private final Consumer<ChunkUpgrade> callback;

	public ChunkUpgradeRegisteryEvent(Consumer<ChunkUpgrade> c)
	{
		callback = c;
	}

	public void register(ChunkUpgrade entry)
	{
		callback.accept(entry);
	}
}