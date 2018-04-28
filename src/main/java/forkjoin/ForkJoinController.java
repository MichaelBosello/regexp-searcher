package forkjoin;

import forkjoin.action.RegexWalkerAction;
import regex.regexresult.RegexResult;
import regex.regexresult.RegexUpdate;
import regex.regexresult.RegexSearchingResult;
import ui.RegexUI;
import regex.RegexController;

import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.SECONDS;

public class ForkJoinController implements RegexController {

    private final static boolean DEBUG = false;
    private Semaphore updateEvent = new Semaphore(0);
    private ExecutorService updateExecutor = Executors.newSingleThreadExecutor();
    private RegexResult result = new RegexSearchingResult(updateEvent);
    private RegexUI ui;
    private String path;
    private String regex;
    private int depth;

    public ForkJoinController(RegexUI ui) {
        this.ui = ui;
        this.path = ui.askPath();
        this.regex = ui.askRegex();
        this.depth = ui.askDepth();
    }
    public ForkJoinController(RegexUI ui, String path, String regex, int depth) {
        this.ui = ui;
        this.path = path;
        this.regex = regex;
        this.depth = depth;
    }

    @Override
    public RegexResult getResult() {
        return result;
    }

    @Override
    public RegexController start(){
        //result.addObserver(ui :: updateResult); //first attempt with observer pattern

        updateExecutor.execute(() -> {
            boolean interrupted = false;
            while( (!Thread.currentThread().isInterrupted() && !interrupted)
                    || updateEvent.availablePermits() > 0 ) {
                if(DEBUG){
                    System.out.println("Update while isInterrupted: " + Thread.currentThread().isInterrupted() +
                                        " permits: " + updateEvent.availablePermits());
                }
                try {
                    updateEvent.acquire();
                    if(DEBUG)
                    System.out.println("updateEvent acquired");
                    RegexUpdate update = result.getUpdate();
                    ui.updateResult(update.getFileList(), update.getPercent(), update.getMean(), update.getError());
                } catch (InterruptedException e) {
                    interrupted = true;
                    if(DEBUG)
                        System.out.println("interrupted exception");
                }
            }
        });

        final ForkJoinPool forkJoinPool = new ForkJoinPool();

        ui.start();
        forkJoinPool.invoke(new RegexWalkerAction(path, regex, result, depth));
        updateExecutor.shutdownNow();
        if(DEBUG)
            System.out.println("shutdownNow requested");
        try {
            updateExecutor.awaitTermination(Long.MAX_VALUE, SECONDS);
        } catch (InterruptedException e) {
         //TODO
        }
        ui.end();
        return this;
    }
}
