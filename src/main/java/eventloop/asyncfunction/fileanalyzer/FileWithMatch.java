package eventloop.asyncfunction.fileanalyzer;

public class FileWithMatch {

    public final static long IO_ERROR = -1;

    private final String filename;
    private final long match;

    public FileWithMatch(String filename, long match) {
        this.filename = filename;
        this.match = match;
    }

    public String getFilename() {
        return filename;
    }

    public long getMatch() {
        return match;
    }
}
