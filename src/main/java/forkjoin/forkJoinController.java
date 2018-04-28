package forkjoin;

import forkjoin.action.RegexWalkerAction;
import regex.regexresult.RegexResult;
import regex.regexresult.RegexUpdate;
import regex.regexresult.RegexSearchingResult;
import ui.RegexUI;
import regex.RegexController;

import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.SECONDS;

public class forkJoinController implements RegexController {

    private Semaphore updateEvent = new Semaphore(0);
    private ExecutorService updateExecutor = Executors.newSingleThreadExecutor();
    private RegexResult result = new RegexSearchingResult(updateEvent);
    private RegexUI ui;
    private String path;
    private String regex;
    private int depth;

    public forkJoinController(RegexUI ui) {
        this.ui = ui;
        this.path = ui.askPath();
        this.regex = ui.askRegex();
        this.depth = ui.askDepth();
    }
    public forkJoinController(String path, String regex, int depth, RegexUI ui) {
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
            while(!Thread.currentThread().isInterrupted() || updateEvent.availablePermits() > 0) {
                try {
                    updateEvent.acquire();
                    RegexUpdate update = result.getUpdate();
                    ui.updateResult(update.getFileList(), update.getPercent(), update.getMean(), update.getError());
                } catch (InterruptedException e) {}
            }
        });

        final ForkJoinPool forkJoinPool = new ForkJoinPool();

        ui.start();
        forkJoinPool.invoke(new RegexWalkerAction(path, regex, result, depth));
        updateExecutor.shutdownNow();
        try {
            updateExecutor.awaitTermination(Long.MAX_VALUE, SECONDS);
        } catch (InterruptedException e) {
         //TODO
        }
        ui.end();
        return this;
    }
}
