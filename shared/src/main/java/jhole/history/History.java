package jhole.history;

import java.util.*;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public class History {
    private static final History instance = Boolean.getBoolean("jhole.history.enabled") ? new History() : null;
    private List<HistoryEntry> entries = new ArrayList<HistoryEntry>();

    private History() {
    }

    public static History getInstance() {
        return instance;
    }

    public synchronized HistoryEntry addEntry(String name) {
        HistoryEntry entry = new HistoryEntryImpl(name);
        entries.add(entry);
        return entry;
    }

    public synchronized List<HistoryEntry> getEntries() {
        return new ArrayList<HistoryEntry>(entries);
    }
}
