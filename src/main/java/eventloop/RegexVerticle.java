package eventloop;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import regex.regexresult.Result;
import ui.RegexUI;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;

import static utility.FileUtility.countMatch;
import static utility.FileUtility.walkDirectory;

public class RegexVerticle extends AbstractVerticle {

    private final static boolean DEBUG = false;
    private final static int IO_ERROR = -1;
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
        ui.start();
        asyncCall.put(0,1);
        walkDirectories(path, depth, 0);
    }

    private void walkDirectories(String path, int depth, int parent){
        vertx.executeBlocking(future -> {
            final int myself = parent + 1;
            if(DEBUG)
                System.out.println("Walker async execution by: " + Thread.currentThread().getName() +
                                    " for path: " + path);
            int subDepth = depth - 1;
            AsyncSpawnTracker asyncFunctionSpawned = new AsyncSpawnTracker(myself,parent);
            File folder = new File(path);
            walkDirectory(folder, (directory) -> {
                if(subDepth >= 0) {
                    if(DEBUG)
                        System.out.println("walker spawn new path: " + directory.getPath());
                    walkDirectories(directory.getPath(), subDepth, myself);
                    asyncFunctionSpawned.incrementAsyncSpawn();
                }
            }, (file) -> {
                if(DEBUG)
                    System.out.println("walker spawn new file: " + file.getPath());
                regexCountInFile(file.getPath(), myself);
                asyncFunctionSpawned.incrementAsyncSpawn();
            });

            if(DEBUG)
                System.out.println("Walk completed " + Thread.currentThread().getName());
            future.complete(asyncFunctionSpawned);
        }, false, composeWalkerFuture().completer());
    }

    private void regexCountInFile(String file, int parent){
        vertx.executeBlocking(future -> {
            final int myself = parent + 1;
            if(DEBUG)
                System.out.println("File analysis async execution by: " + Thread.currentThread().getName());
            fileWithMatch fileMatch;
            try {
                long match = countMatch(regex, file);
                fileMatch = new fileWithMatch(file, match);
            } catch (IOException e) {
                fileMatch = new fileWithMatch(file, (long) IO_ERROR);
            }
            if(DEBUG)
                System.out.println("file analysis completed " + Thread.currentThread().getName());
            future.complete(new SimpleEntry<>(fileMatch, new AsyncSpawnTracker(myself, parent)));
        }, false, composeFileAnalysisFuture().completer());
    }

    private Future<AsyncSpawnTracker> composeWalkerFuture(){
        Future<AsyncSpawnTracker> walker = Future.future();
        walker.compose( spawns -> {
            if(DEBUG)
                System.out.println("callback from walker " + Thread.currentThread().getName());
            trackAsyncSpawn(spawns);
        }, failFuture);
        return walker;
    }

    private Future<Entry<fileWithMatch, AsyncSpawnTracker>> composeFileAnalysisFuture(){
        /*
         *   result not as monitor but as data structure: used only by event loop (And from JUnit at end)
         * */
        Future<Entry<fileWithMatch, AsyncSpawnTracker>> fileAnalysis = Future.future();
        fileAnalysis.compose( fileMatches -> {
            if(DEBUG)
                System.out.println("callback from file analysis " + Thread.currentThread().getName() +
                                    "for file: " + fileMatches.getKey().getFilename());
            if(fileMatches.getKey().getMatch() == IO_ERROR){
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



    private class AsyncSpawnTracker{
        private final int functionID;
        private final int parentID;
        private int asyncSpawn;

        public AsyncSpawnTracker(int functionID, int parentID, int asyncSpawn) {
            this.functionID = functionID;
            this.parentID = parentID;
            this.asyncSpawn = asyncSpawn;
        }

        public AsyncSpawnTracker(int functionID, int parentID) {
            this(functionID, parentID, 0);
        }

        public void incrementAsyncSpawn() {
            asyncSpawn++;
        }

        public int getFunctionID() {
            return functionID;
        }

        public int getParentID() {
            return parentID;
        }

        public int getAsyncSpawn() {
            return asyncSpawn;
        }
    }

    private class fileWithMatch{
        private final String filename;
        private final long match;

        public fileWithMatch(String filename, long match) {
            this.filename = filename;
            this.match = match;
        }

        public String getFilename() {
            return filename;
        }

        public long getMatch() {
            return match;
        }
    }
}

