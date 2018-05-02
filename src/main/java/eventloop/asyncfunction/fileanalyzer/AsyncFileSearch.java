package eventloop.asyncfunction.fileanalyzer;

import eventloop.asyncfunction.AsyncSpawnTracker;
import eventloop.asyncfunction.FutureFactory;
import io.vertx.core.Vertx;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map;

import static utility.FileUtility.countMatch;

public class AsyncFileSearch {

    private final static boolean DEBUG = false;
    private final Vertx vertx;
    private final String regex;
    private final FutureFactory<Map.Entry<FileWithMatch, AsyncSpawnTracker>> futureFactory;

    public AsyncFileSearch(Vertx vertx, String regex, FutureFactory<Map.Entry<FileWithMatch, AsyncSpawnTracker>> futureFactory) {
        this.vertx = vertx;
        this.regex = regex;
        this.futureFactory = futureFactory;
    }

    public void regexCountInFile(String file, int parent){
        vertx.executeBlocking(future -> {
            final int myself = parent + 1;
            if(DEBUG)
                System.out.println("File analysis async execution by: " + Thread.currentThread().getName());
            FileWithMatch fileMatch;
            try {
                long match = countMatch(regex, file);
                fileMatch = new FileWithMatch(file, match);
            } catch (IOException e) {
                fileMatch = new FileWithMatch(file, FileWithMatch.IO_ERROR);
            }
            if(DEBUG)
                System.out.println("file analysis completed " + Thread.currentThread().getName());
            future.complete(new AbstractMap.SimpleEntry<>(fileMatch, new AsyncSpawnTracker(myself, parent)));
        }, false, futureFactory.compose().completer());
    }
}
