package regex.regexresult;

import java.util.List;

public interface Update {

    List<String> getFileList();

    List<String> getNotConsumedFiles();

    double getPercent();

    double getMean();

    int getError();
}
