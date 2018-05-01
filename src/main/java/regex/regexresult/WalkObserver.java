package regex.regexresult;

import java.util.List;
import java.util.Map;

@Deprecated
@FunctionalInterface
public interface WalkObserver {
    void updateResult(List<String> matchingFile, double percent, Map.Entry<Long, Long> mean, int error);
}
