package eventloop;

import eventloop.asyncfunction.AsyncSpawnTracker;
import eventloop.asyncfunction.FutureFactory;
import eventloop.asyncfunction.fileanalyzer.AsyncFileSearch;
import eventloop.asyncfunction.fileanalyzer.FileWithMatch;
import eventloop.asyncfunction.walker.DirectoryWalker;
import eventloop.asyncfunction.walker.FileAction;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import regex.regexresult.Result;
import ui.RegexUI;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;

public class RegexVerticle extends AbstractVerticle {

    private final static boolean DEBUG = false;
    private RegexUI ui;
    private Result result;
    private Semaphore endEvent;
    private String path;
    private String regex;
    private int depth;
    private boolean askUI;
    private Future<Void> failFuture = Future.future();
    private Map<Integer, Integer> asyncCall = new HashMap<>();

    public RegexVerticle(RegexUI ui, Result result, Semaphore endEvent) {
        this.ui = ui;
        this.result = result;
        this.endEvent = endEvent;
        askUI = true;
    }

    public RegexVerticle(RegexUI ui, Result result, Semaphore endEvent, String path, String regex, int depth) {
        this.ui = ui;
        this.result = result;
        this.endEvent = endEvent;
        this.path = path;
        this.regex = regex;
        this.depth = depth;
        askUI = false;
    }

    @Override
    public void start() {
        failFuture.setHandler(res -> {
            System.out.println("Something failed. " + res.cause());
        });

        if(askUI){
            Future<String> askPath = Future.future();
            vertx.executeBlocking(future -> {
                future.complete(ui.askPath());
            }, askPath.completer());

            askPath.compose( path -> {
                this.path = path;
                if(DEBUG)
                    System.out.println("callback from ask " + Thread.currentThread().getName());
                Future<String> askRegex = Future.future();
                vertx.executeBlocking(future -> {
                    future.complete(ui.askRegex());
                }, askRegex.completer());
                return askRegex;
            }).compose( regex -> {
                this.regex = regex;
                if(DEBUG)
                    System.out.println("callback from ask " + Thread.currentThread().getName());
                Future<Integer> askDepth = Future.future();
                vertx.executeBlocking(future -> {
                    future.complete(ui.askDepth());
                }, askDepth.completer());
                return askDepth;
            }).compose( depth -> {
                this.depth = depth;
                if(DEBUG)
                    System.out.println("callback from ask " + Thread.currentThread().getName());
                search();
            }, failFuture);
        }else{
            search();
        }
    }

    private void search(){
        DirectoryWalker walker = createWalkerForSearch();
        ui.start();
        asyncCall.put(0,1);
        walker.walkDirectories(path, depth, 0);
    }

    private DirectoryWalker createWalkerForSearch(){

        FutureFactory<AsyncSpawnTracker> walkerFuture = () -> {
            Future<AsyncSpawnTracker> walker = Future.future();
            walker.compose( spawns -> {
                if(DEBUG)
                    System.out.println("callback from walker " + Thread.currentThread().getName());
                trackAsyncSpawn(spawns);
            }, failFuture);
            return walker;
        };

        FutureFactory<Entry<FileWithMatch, AsyncSpawnTracker>> searchFuture = () -> {
            /*
             *   result not as monitor but as data structure: used only by event loop (And from JUnit at end)
             * */
            Future<Entry<FileWithMatch, AsyncSpawnTracker>> fileAnalysis = Future.future();
            fileAnalysis.compose( fileMatches -> {
                if(DEBUG)
                    System.out.println("callback from file analysis " + Thread.currentThread().getName() +
                            "for file: " + fileMatches.getKey().getFilename());
                if(fileMatches.getKey().getMatch() == FileWithMatch.IO_ERROR){
                    result.incrementIOException();
                } else if(fileMatches.getKey().getMatch() == 0){
                    result.addNonMatchingFile(fileMatches.getKey().getFilename());
                }else{
                    result.addMatchingFile(fileMatches.getKey().getFilename(), fileMatches.getKey().getMatch());
                }
                try {
                    ui.updateResult(result.getNotConsumedFiles(), result.matchingFilePercent(),
                            result.matchMean(), result.getError());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                trackAsyncSpawn(fileMatches.getValue());
            }, failFuture);
            return fileAnalysis;
        };

        AsyncFileSearch searcher = new AsyncFileSearch(vertx, regex, searchFuture);

        FileAction regexAction = (file, parent) -> {
            if (DEBUG)
                System.out.println("walker spawn new file: " + file);
            searcher.regexCountInFile(file, parent);
        };

         return new DirectoryWalker(vertx, regexAction, walkerFuture);
    }

    private void trackAsyncSpawn(AsyncSpawnTracker tracker){
        if(!asyncCall.containsKey(tracker.getFunctionID())){
            asyncCall.put(tracker.getFunctionID(),tracker.getAsyncSpawn());
        }else{
            asyncCall.replace(tracker.getFunctionID(),
                    asyncCall.get(tracker.getFunctionID()) + tracker.getAsyncSpawn());
        }
        if(!asyncCall.containsKey(tracker.getParentID())){
            asyncCall.put(tracker.getParentID(), -1);
        }else{
            asyncCall.replace(tracker.getParentID(),asyncCall.get(tracker.getParentID()) - 1);
        }

        boolean end = true;
       for(Entry<Integer, Integer> remainedSpawn : asyncCall.entrySet()){
           if(remainedSpawn.getValue() != 0){
               end = false;
           }
           if(DEBUG)
               System.out.println("Remain spawn:" + remainedSpawn);
       }
        if(end){
            ui.end();
            endEvent.release();
        }
    }
}

