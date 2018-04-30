package reactivestreams;

import io.reactivex.flowables.ConnectableFlowable;
import regex.RegexController;
import regex.regexresult.Result;
import regex.regexresult.SearchingResult;
import regex.regexresult.Update;
import ui.RegexUI;

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
        ui.start();
        ConnectableFlowable<Update> matchObservable = new RegexStream(path, regex, result, depth).matchStream().publish();
        matchObservable.subscribe((update) -> {
            ui.updateResult(update.getNotConsumedFiles(),update.getPercent(),update.getMean(),update.getError());
        });
        matchObservable.doOnComplete( () -> {
            ui.end();
        });
        matchObservable.connect();
        return this;
    }
}
