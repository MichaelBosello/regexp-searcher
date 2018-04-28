package regex.regexresult;

import java.util.List;

public interface RegexUpdate {

    List<String> getFileList();

    double getPercent();

    double getMean();

    int getError();
}
