package com.runitebar.adventurerslog;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("adventurerslog")
public interface AdventurersLogConfig extends Config {
    @ConfigItem(
            keyName = "logData",
            name = "Log Data",
            description = "Stored log entries",
            hidden = true
    )
    default String logData() { return "[]"; }
}