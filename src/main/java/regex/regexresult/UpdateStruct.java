package regex.regexresult;

import java.util.List;
import java.util.Map;

public class UpdateStruct implements Update {
    List<String> fileList;
    List<String> notConsumed;
    double percent;
    Map.Entry<Long, Long> mean;
    int error;

    public UpdateStruct(List<String> fileList, List<String> notConsumed, double percent, Map.Entry<Long, Long> mean, int error) {
        this.fileList = fileList;
        this.notConsumed = notConsumed;
        this.percent = percent;
        this.mean = mean;
        this.error = error;
    }

    @Override
    public List<String> getFileList() {
        return fileList;
    }
    @Override
    public List<String> getNotConsumedFiles() {
        return notConsumed;
    }
    @Override
    public double getPercent() {
        return percent;
    }
    @Override
    public Map.Entry<Long, Long> getMean() {
        return mean;
    }
    @Override
    public int getError() {
        return error;
    }
}
