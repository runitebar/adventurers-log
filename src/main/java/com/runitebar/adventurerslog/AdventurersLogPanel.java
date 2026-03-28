package com.runitebar.adventurerslog;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.inject.Singleton;

import net.runelite.api.Skill;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.api.SpriteID;
import net.runelite.client.game.SpriteManager;

import javax.swing.ImageIcon;

@Singleton
public class AdventurersLogPanel extends PluginPanel {
    private final JPanel logContainer = new JPanel();
    private final SkillIconManager skillIconManager;
    private final ItemManager itemManager;
    private final SpriteManager spriteManager;

    public AdventurersLogPanel(SkillIconManager skillIconManager, ItemManager itemManager, SpriteManager spriteManager) {
        super();
        this.skillIconManager = skillIconManager;
        this.itemManager = itemManager;
        this.spriteManager = spriteManager;

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        // Header Section
        JPanel header = new JPanel(new GridBagLayout());
        header.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        header.setPreferredSize(new Dimension(0, 40));
        header.setBorder(new MatteBorder(0, 0, 1, 0, ColorScheme.DARK_GRAY_COLOR));

        JLabel title = new JLabel("Adventurer's Log");
        title.setForeground(ColorScheme.BRAND_ORANGE);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));

        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setVerticalAlignment(SwingConstants.CENTER);

        header.add(title);
        add(header, BorderLayout.NORTH);

        // Log Container
        logContainer.setLayout(new BoxLayout(logContainer, BoxLayout.Y_AXIS));
        logContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

        JScrollPane scrollPane = new JScrollPane(logContainer);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void rebuild(List<LogEntry> entries) {
        logContainer.removeAll();

        logContainer.add(Box.createRigidArea(new Dimension(0, 5)));

        for (int i = entries.size() - 1; i >= 0; i--) {
            logContainer.add(createEntryPanel(entries.get(i)));

            logContainer.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        revalidate();
        repaint();
    }

    private JPanel createEntryPanel(LogEntry entry) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        panel.setBorder(new CompoundBorder(
                new MatteBorder(1, 1, 1, 1, ColorScheme.DARK_GRAY_COLOR),
                new EmptyBorder(8, 8, 8, 8)
        ));

        JLabel iconLabel = new JLabel();
        iconLabel.setPreferredSize(new Dimension(32, 32));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

        if (entry.getType().equals("LEVEL")) {
            try {
                Skill skill = Skill.valueOf(entry.getSubType().toUpperCase());
                iconLabel.setIcon(new ImageIcon(skillIconManager.getSkillImage(skill, true)));
            } catch (Exception e) {}
        }
        else if (entry.getType().equals("QUEST")) {
            java.awt.image.BufferedImage img = spriteManager.getSprite(SpriteID.TAB_QUESTS, 0);
            if (img != null) {
                iconLabel.setIcon(new ImageIcon(img));
            }
        }
        else if (entry.getType().equals("CLOG")) {
            iconLabel.setIcon(new ImageIcon(itemManager.getImage(22711)));
        }

// Text Content
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        String displayMsg = entry.getMessage();
        if (entry.getCount() > 1) {
            displayMsg += " <span style='color:#ffff00'>(x" + entry.getCount() + ")</span>";
        }

        JLabel msgLabel = new JLabel("<html><body style='width: 130px'>" + displayMsg + "</body></html>");
        msgLabel.setForeground(Color.WHITE);
        msgLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));

        JLabel dateLabel = new JLabel(entry.getTimestamp());
        dateLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
        dateLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);

        textPanel.add(msgLabel);
        textPanel.add(dateLabel);

        panel.add(iconLabel, BorderLayout.WEST);
        panel.add(textPanel, BorderLayout.CENTER);

        panel.setMaximumSize(new Dimension(PluginPanel.PANEL_WIDTH, panel.getPreferredSize().height));

        return panel;
    }
}