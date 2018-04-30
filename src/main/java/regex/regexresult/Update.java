package regex.regexresult;

import java.util.List;

public interface Update {

    List<String> getFileList();

    double getPercent();

    double getMean();

    int getError();
}
