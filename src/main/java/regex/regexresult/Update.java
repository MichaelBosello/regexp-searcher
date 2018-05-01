package regex.regexresult;

import java.util.List;
import java.util.Map;

public interface Update {

    List<String> getFileList();

    List<String> getNotConsumedFiles();

    double getPercent();

    Map.Entry<Long, Long> getMean();

    int getError();
}
