package regex.regexresult;

import java.util.List;
import java.util.Map;

public interface Result {

    List<String> getMatchingFiles();

    List<String> getNotConsumedFiles();

    double matchingFilePercent();

    Map.Entry<Long, Long> matchMean();

    int getError();

    Update getUpdate();

    void addMatchingFile(String file, long matches);

    void addNonMatchingFile(String file);

    void incrementIOException();

    @Deprecated
    void addObserver(WalkObserver observer);
}
