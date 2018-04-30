package reactivestreams;

import io.reactivex.flowables.ConnectableFlowable;
import regex.RegexController;
import regex.regexresult.Result;
import regex.regexresult.SearchingResult;
import regex.regexresult.Update;
import ui.RegexUI;

import java.util.concurrent.Semaphore;

public class RxController implements RegexController {

    private Result result = new SearchingResult();
    private RegexUI ui;
    private String path;
    private String regex;
    private int depth;

    public RxController(RegexUI ui) {
        this.ui = ui;
        this.path = ui.askPath();
        this.regex = ui.askRegex();
        this.depth = ui.askDepth();
    }
    public RxController(RegexUI ui, String path, String regex, int depth) {
        this.ui = ui;
        this.path = path;
        this.regex = regex;
        this.depth = depth;
    }

    @Override
    public Result getResult() {
        return result;
    }

    @Override
    public RegexController start(){
        Semaphore endEvent = new Semaphore(0);
        ui.start();
        ConnectableFlowable<Update> matchObservable = new RegexStream(path, regex, result, depth).matchStream().publish();
        matchObservable.subscribe((update) -> {
            ui.updateResult(update.getNotConsumedFiles(),update.getPercent(),update.getMean(),update.getError());
        },(Throwable t) -> {
            System.out.println("error  " + t);
        },() -> {
            ui.end();
            endEvent.release();
        });
        matchObservable.connect();

        //not working, end function declared with subscribe instead
        /*
        matchObservable.doOnComplete( () -> {
            ui.end();
            endEvent.release();
        });
        */
        try {
            endEvent.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this;
    }
}
