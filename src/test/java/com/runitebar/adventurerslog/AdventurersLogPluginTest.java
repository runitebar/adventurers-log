package com.runitebar.adventurerslog;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class AdventurersLogPluginTest {
	public static void main(String[] args) throws Exception {
		ExternalPluginManager.loadBuiltin(com.runitebar.adventurerslog.AdventurersLogPlugin.class);

		RuneLite.main(args);
	}
}