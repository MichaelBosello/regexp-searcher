package regex.regexresult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class RegexSearchingResult implements RegexResult {

    private final static boolean DEBUG = false;
    private Semaphore updateEvent = null;
    private List<String> matchingFiles = new ArrayList<>();
    private int analyzedFile = 0;
    private int totalMatches = 0;
    private int exception = 0;

    public RegexSearchingResult() {}

    public RegexSearchingResult(Semaphore updateEvent) {
        this.updateEvent = updateEvent;
    }

    @Override
    public synchronized List<String> getMatchingFiles() {
        return new ArrayList(matchingFiles);
    }

    @Override
    public synchronized double matchingFilePercent(){
        return analyzedFile == 0 ? 0 : (double) matchingFiles.size()/analyzedFile;
    }

    @Override
    public synchronized double matchMean(){
        return matchingFiles.size() == 0 ? 0 : (double) totalMatches/matchingFiles.size();
    }

    @Override
    public synchronized int getError(){
        return exception;
    }

    @Override
    public synchronized RegexUpdate getUpdate(){
        return new RegexUpdateStruct(getMatchingFiles(), matchingFilePercent(), matchMean(), getError());
    }

    @Override
    public synchronized void addMatchingFile(String file, int matches){
        matchingFiles.add(file);
        totalMatches += matches;
        incrementAnalyzedFile();
    }

    @Override
    public synchronized void addNonMatchingFile(String file){
        incrementAnalyzedFile();
    }

    @Override
    public synchronized void incrementIOException(){
        exception++;
    }

    private void incrementAnalyzedFile(){
        analyzedFile++;
        sendUpdate();
    }

    private void sendUpdate(){
        //only this monitor release on this semaphore so no need for check and act
        if(updateEvent != null && updateEvent.availablePermits() == 0){
            updateEvent.release();
        }
        if(DEBUG)
            System.out.println("updateEvent released by monitor");

        updateObserver();//do nothing if there isn't observers (deprecated, first attempt)
    }











    //first attempt, no more used

    Executor updateExecutor = null;
    List<WalkObserver> observers = new ArrayList<>();
    private void updateObserver(){
        if(observers.size() > 0) {
            List<String> fileList = getMatchingFiles();
            double percent = matchingFilePercent();
            double mean = matchMean();
            for (WalkObserver observer : observers) {
                if (updateExecutor == null) {
                    updateExecutor = Executors.newSingleThreadExecutor();
                }
                updateExecutor.execute(() -> observer.updateResult(fileList, percent, mean, exception));
            }
        }
    }

    @Deprecated
    @Override
    public synchronized void addObserver(WalkObserver observer){
        observers.add(observer);
    }

    //eventually create method 'close' to shutdown Executor

}
