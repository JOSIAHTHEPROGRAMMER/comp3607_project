package comp3607_project.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import comp3607_project.compile.CompileErrorClassifier.Category;

/** Accumulates session totals as mappers report events. */
public class SummaryBuilder {
    private final Map<Long, Long> attemptStart = new LinkedHashMap<>();
    private final Set<Long> succeeded = new HashSet<>();
    private final Set<Long> failed = new HashSet<>();
    private final Map<String, Integer> categories = new LinkedHashMap<>();
    private int runs = 0;
    private int exceptions = 0;

    public SummaryBuilder() {
        for (Category c : Category.values()) {
            categories.put(c.name(), 0);
        }
    }

    /**
     * One compile row. category is only used for failed rows; null counts as OTHER.
     */
    public void compileRow(long attemptId, long timestamp, boolean success, String category) {
        attemptStart.putIfAbsent(attemptId, timestamp);
        if (success) {
            succeeded.add(attemptId);
        } else {
            failed.add(attemptId);
            categories.merge(category == null ? Category.OTHER.name() : category, 1, Integer::sum);
        }
    }

    public void run(boolean threwException) {
        runs++;
        if (threwException) {
            exceptions++;
        }
    }

    public SessionSummary build() {
        List<Long> starts = new ArrayList<>(attemptStart.values());
        Collections.sort(starts);
        List<Long> intervals = new ArrayList<>();
        for (int i = 1; i < starts.size(); i++) {
            intervals.add(starts.get(i) - starts.get(i - 1));
        }
        return new SessionSummary(attemptStart.size(), succeeded.size(), failed.size(),
                runs, exceptions, Collections.unmodifiableMap(new LinkedHashMap<>(categories)),
                Collections.unmodifiableList(intervals));
    }
}