package reactivestreams.action;

import forkjoin.action.CountRegexInFileAction;
import forkjoin.action.RegexWalkerAction;
import io.reactivex.FlowableEmitter;
import regex.regexresult.Result;
import regex.regexresult.Update;

public class RegexWalkerActionRX extends RegexWalkerAction {

    FlowableEmitter<Update> emitter;

    public RegexWalkerActionRX(String path, String regex, Result collector, int depth, FlowableEmitter<Update> emitter) {
        super(path, regex, collector, depth);
        this.emitter = emitter;
    }

    @Override
    protected RegexWalkerAction newWalker(String subPath, int subDepth){
        return new RegexWalkerActionRX(subPath, regex, collector, subDepth, emitter);
    }

    @Override
    protected CountRegexInFileAction newAnalyzer(String subPath){
        return new CountRegexInFileActionRX(subPath , regex, collector, emitter);
    }
}
