package regex.regexresult;

import java.util.List;

public interface RegexResult {

    List<String> getMatchingFiles();

    double matchingFilePercent();

    double matchMean();

    int getError();

    RegexUpdate getUpdate();

    void addMatchingFile(String file, int matches);

    void addNonMatchingFile(String file);

    void incrementIOException();

    @Deprecated
    void addObserver(WalkObserver observer);
}
