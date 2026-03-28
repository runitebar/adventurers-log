package com.runitebar.adventurerslog;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class LogEntry {
    private final String timestamp; // The full time (e.g., "Oct 25, 14:30")
    private final String date;      // Just the date (e.g., "2024-10-25")
    private final String message;   // The base message (e.g., "Killed Zulrah")
    private final String type;      // LEVEL, BOSS, etc.
    private final String subType;   // The boss/skill name
    private int count;              // How many times this happened today

    public LogEntry(String type, String message, String subType) {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"));
        this.date = LocalDate.now().toString(); // "2024-10-25"
        this.message = message;
        this.type = type;
        this.subType = subType;
        this.count = 1;
    }
}