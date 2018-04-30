package regex.regexresult;

import java.util.List;

public interface Result {

    List<String> getMatchingFiles();

    List<String> getNotConsumedFiles();

    double matchingFilePercent();

    double matchMean();

    int getError();

    Update getUpdate();

    void addMatchingFile(String file, int matches);

    void addNonMatchingFile(String file);

    void incrementIOException();

    @Deprecated
    void addObserver(WalkObserver observer);
}
