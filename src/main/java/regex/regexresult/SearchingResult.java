package regex.regexresult;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class SearchingResult implements Result {

    private final static boolean DEBUG = false;
    private Semaphore updateEvent = null;
    private List<String> matchingFiles = new ArrayList<>();
    private List<String> notConsumed = new ArrayList<>();
    private int analyzedFile = 0;
    private long totalMatches = 0;
    private int exception = 0;

    public SearchingResult() {}

    public SearchingResult(Semaphore updateEvent) {
        this.updateEvent = updateEvent;
    }

    @Override
    public synchronized List<String> getMatchingFiles() {
        return new ArrayList<>(matchingFiles);
    }

    @Override
    public synchronized List<String> getNotConsumedFiles() {
        List<String> notConsumedTmp = new ArrayList<>(notConsumed);
        notConsumed.clear();
        return notConsumedTmp;
    }

    @Override
    public synchronized double matchingFilePercent(){
        return analyzedFile == 0 ? 0 : (double) matchingFiles.size()/analyzedFile;
    }

    @Override
    public synchronized Map.Entry<Long, Long> matchMean(){
        long quotient = matchingFiles.size() == 0 ? 0 : totalMatches/matchingFiles.size();
        long remainder = matchingFiles.size() == 0 ? 0 : totalMatches%matchingFiles.size();
        return new AbstractMap.SimpleEntry<>(quotient,remainder);
    }

    @Override
    public synchronized int getError(){
        return exception;
    }

    @Override
    public synchronized Update getUpdate(){
        Update update = new UpdateStruct(getMatchingFiles(), getNotConsumedFiles(), matchingFilePercent(), matchMean(), getError());
        return update;
    }

    @Override
    public synchronized void addMatchingFile(String file, long matches){
        matchingFiles.add(file);
        notConsumed.add(file);
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
            Map.Entry<Long, Long> mean = matchMean();
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
