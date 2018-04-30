package reactivestreams.action;

import forkjoin.action.CountRegexInFileAction;
import io.reactivex.FlowableEmitter;
import regex.regexresult.Result;
import regex.regexresult.Update;

public class CountRegexInFileActionRX extends CountRegexInFileAction {

    FlowableEmitter<Update> emitter;

    public CountRegexInFileActionRX(String file, String regex, Result collector, FlowableEmitter<Update> emitter) {
        super(file, regex, collector);
        this.emitter = emitter;
    }

    @Override
    protected void compute() {
        super.compute();
        emitter.onNext(collector.getUpdate());
    }
}
