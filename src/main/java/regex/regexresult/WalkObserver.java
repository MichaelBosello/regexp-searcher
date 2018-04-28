package regex.regexresult;

import java.util.List;

@Deprecated
@FunctionalInterface
public interface WalkObserver {
    void updateResult(List<String> matchingFile, double percent, double mean, int error);
}
