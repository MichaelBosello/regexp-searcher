package regex.regexresult;

import java.util.List;

public class RegexUpdateStruct implements RegexUpdate{
    List<String> fileList;
    double percent;
    double mean;
    int error;

    public RegexUpdateStruct(List<String> fileList, double percent, double mean, int error) {
        this.fileList = fileList;
        this.percent = percent;
        this.mean = mean;
        this.error = error;
    }

    @Override
    public List<String> getFileList() {
        return fileList;
    }
    @Override
    public double getPercent() {
        return percent;
    }
    @Override
    public double getMean() {
        return mean;
    }
    @Override
    public int getError() {
        return error;
    }
}
