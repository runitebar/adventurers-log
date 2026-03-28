package com.runitebar.adventurerslog;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.StatChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.game.SpriteManager;
import java.time.LocalDate;

@Singleton
@PluginDescriptor(
        name = "Adventurer's Log",
        description = "Tracks levels, quests, bosses, and milestones",
        tags = {"history", "log", "boss", "quest"}
)
public class AdventurersLogPlugin extends Plugin {
    @Inject private Client client;
    @Inject private AdventurersLogConfig config;
    @Inject private ConfigManager configManager;
    @Inject private ClientToolbar clientToolbar;
    @Inject private SkillIconManager skillIconManager;
    @Inject private ItemManager itemManager; // Added
    @Inject private Gson gson;
    @Inject private SpriteManager spriteManager;

    private AdventurersLogPanel panel;
    private NavigationButton navButton;
    private List<LogEntry> logEntries = new ArrayList<>();
    private final int[] lastLevels = new int[23];

    @Override
    protected void startUp() {
        logEntries = gson.fromJson(config.logData(), new TypeToken<List<LogEntry>>(){}.getType());
        if (logEntries == null) logEntries = new ArrayList<>();

        panel = new AdventurersLogPanel(skillIconManager, itemManager, spriteManager);
        panel.rebuild(logEntries);

        navButton = NavigationButton.builder()
                .tooltip("Adventurer's Log")
                .icon(ImageUtil.loadImageResource(getClass(), "icon.png"))
                .priority(10)
                .panel(panel)
                .build();

        clientToolbar.addNavigation(navButton);
    }

    @Override
    protected void shutDown() {
        clientToolbar.removeNavigation(navButton);
    }

    @Subscribe
    public void onStatChanged(StatChanged event) {
        int idx = event.getSkill().ordinal();
        int currentLevel = event.getLevel();

        if (lastLevels[idx] != 0 && currentLevel > lastLevels[idx]) {
            String skillName = event.getSkill().getName();
            addEntry(new LogEntry("LEVEL", "Gained level " + currentLevel + " " + skillName, skillName));
        }
        lastLevels[idx] = currentLevel;
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        String msg = event.getMessage();

        // 1. Boss Kill Count
        if ((msg.contains("kill count is:") || msg.contains("completion count is:")) && msg.contains("Your")) {
            String cleaned = msg.replaceAll("<[^>]*>", "");
            String bossName = cleaned.split("Your ")[1].split(" (kill|completion)")[0];

            handleBossKill(bossName);
        }

        // 2. Quest Completion
        if (msg.contains("You have completed the quest:") || msg.contains("You have completed a quest:")) {
            String questName = msg.split(": ")[1].replace(".", "");
            addEntry(new LogEntry("QUEST", "Quest Completed: " + questName, "QUEST_POINT"));
        }

        // 3. Collection Log
        if (msg.contains("New item added to your collection log:")) {
            String item = msg.split(": ")[1];
            addEntry(new LogEntry("CLOG", "Collection Log: " + item, "CLOG"));
        }
    }

    private void handleBossKill(String bossName) {
        String today = LocalDate.now().toString();

        // Look for an existing entry for this boss from today
        LogEntry existing = logEntries.stream()
                .filter(e -> e.getType().equals("BOSS")
                        && e.getSubType().equalsIgnoreCase(bossName)
                        && e.getDate().equals(today))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            // Increment the count of the existing entry
            existing.setCount(existing.getCount() + 1);
            saveAndRefresh();
        } else {
            // Create a new entry for the first kill of the day
            addEntry(new LogEntry("BOSS", "Killed " + bossName, bossName));
        }
    }

    private void addEntry(LogEntry entry) {
        logEntries.add(entry);
        if (logEntries.size() > 150) logEntries.remove(0);
        saveAndRefresh();
    }

    private void saveAndRefresh() {
        configManager.setConfiguration("adventurerslog", "logData", gson.toJson(logEntries));
        SwingUtilities.invokeLater(() -> panel.rebuild(logEntries));
    }

    @Provides
    AdventurersLogConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(AdventurersLogConfig.class);
    }
}