package reactivestreams;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import reactivestreams.action.RegexWalkerActionRX;
import regex.regexresult.Result;
import regex.regexresult.Update;

import java.util.concurrent.ForkJoinPool;

public class RegexStream {

    private String path;
    private String regex;
    private Result result;
    private int depth;

    public RegexStream(String path, String regex, Result result, int depth) {
        this.path = path;
        this.regex = regex;
        this.result = result;
        this.depth = depth;
    }

    public Flowable<Update> matchStream(){
        Flowable<Update> source = Flowable.create(emitter -> {
            new Thread(() -> {
                final ForkJoinPool forkJoinPool = new ForkJoinPool();
                forkJoinPool.invoke(new RegexWalkerActionRX(path, regex, result, depth, emitter));
                emitter.onComplete();
            }).start();
        }, BackpressureStrategy.BUFFER);
        return source;
    }
}
