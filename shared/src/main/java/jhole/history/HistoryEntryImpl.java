package jhole.history;

import java.util.*;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public class HistoryEntryImpl implements HistoryEntry {
    private final String name;
    private Map<String,HistorySection> sections = new LinkedHashMap<String, HistorySection>();

    public HistoryEntryImpl(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public synchronized HistorySection createSection(String name) {
        HistorySectionImpl section = new HistorySectionImpl(name);
        if (sections.containsKey(name)) {
            throw new IllegalStateException("section name already in use: "+name);
        }
        sections.put(name, section);
        return section;
    }

    @Override
    public synchronized Collection<HistorySection> getSections() {
        return new ArrayList<HistorySection>(sections.values());
    }

    @Override
    public synchronized HistorySection getSection(String name) {
        return sections.get(name);
    }
}
