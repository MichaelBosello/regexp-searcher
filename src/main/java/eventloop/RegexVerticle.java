package eventloop;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import regex.regexresult.RegexResult;
import ui.RegexUI;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexVerticle extends AbstractVerticle {

    private final static boolean DEBUG = true;
    private final static int IO_ERROR = -1;
    private RegexUI ui;
    private RegexResult result;
    private String path;
    private String regex;
    private int depth;
    private boolean askUI;
    private Future<Void> failFuture = Future.future();
    private int asyncSpawn = 0;

    public RegexVerticle(RegexUI ui, RegexResult result) {
        this.ui = ui;
        this.result = result;
        askUI = true;
    }

    public RegexVerticle(RegexUI ui, RegexResult result, String path, String regex, int depth) {
        this.ui = ui;
        this.result = result;
        this.path = path;
        this.regex = regex;
        this.depth = depth;
        askUI = false;
    }

    @Override
    public void start() {
        failFuture.setHandler(res -> {
            System.out.println("Something failed.");
        });

        if(askUI){
            Future<String> askPath = Future.future();
            vertx.executeBlocking(future -> {
                future.complete(ui.askPath());
            }, askPath.completer());

            askPath.compose( path -> {
                this.path = path;

                Future<String> askRegex = Future.future();
                vertx.executeBlocking(future -> {
                    future.complete(ui.askRegex());
                }, askRegex.completer());
                return askRegex;
            }).compose( regex -> {
                this.regex = regex;

                Future<Integer> askDepth = Future.future();
                vertx.executeBlocking(future -> {
                    future.complete(ui.askDepth());
                }, askDepth.completer());
                return askDepth;
            }).compose( depth -> {
                this.depth = depth;
                search();
            }, failFuture);
        }else{
            search();
        }
    }

    private void search(){
        ui.start();
        asyncSpawn++;
        walkDirectories(path, depth, composeWalker().completer());
    }

    private Future<Integer> composeWalker(){
        Future<Integer> walker = Future.future();
        walker.compose( spawns -> {
            asyncSpawn += spawns;
            decreaseAsyncSpawn();
        }, failFuture);
        return walker;
    }

    private void decreaseAsyncSpawn(){
        asyncSpawn--;
        if(DEBUG)
            System.out.println(asyncSpawn);
        if(asyncSpawn == 0){
            ui.end();
        }
    }

    private Future<Entry<String, Integer>> composeFileAnalysis(){
        /*
         *   result not as monitor but as data structure: used only by event loop (And from JUnit at end)
         * */
        Future<Entry<String, Integer>> fileAnalysis = Future.future();
        fileAnalysis.compose( fileMatches -> {
            if(fileMatches.getValue() == IO_ERROR){
                result.incrementIOException();
            } else if(fileMatches.getValue() == 0){
                result.addNonMatchingFile(fileMatches.getKey());
            }else{
                result.addMatchingFile(fileMatches.getKey(), fileMatches.getValue());
            }
            ui.updateResult(result.getMatchingFiles(), result.matchingFilePercent(),
                    result.matchMean(), result.getError());
            decreaseAsyncSpawn();
        }, failFuture);
        return fileAnalysis;
    }

    private void walkDirectories(String path, int depth, Handler<AsyncResult<Integer>> walkCallback){
        vertx.executeBlocking(future -> {
            int subDepth = depth - 1;
            int asyncFunctionSpawned = 0;
            File folder = new File(path);
            if(folder.listFiles() != null) {
                for (final File fileEntry : folder.listFiles()) {
                    if (fileEntry.isDirectory()) {
                        if(depth >= 0) {
                            walkDirectories(fileEntry.getPath(), subDepth, composeWalker().completer());
                            asyncFunctionSpawned++;
                        }
                    } else {
                        regexCountInFile(fileEntry.getPath(), composeFileAnalysis().completer());
                        asyncFunctionSpawned++;
                    }
                }
            }
            future.complete(asyncFunctionSpawned);
        }, walkCallback);
    }

    private void regexCountInFile(String file, Handler<AsyncResult<Entry<String, Integer>>> callback){
        vertx.executeBlocking(future -> {
            Entry<String, Integer> fileMatch;
            try {
                int match = 0;
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(fromFile(file));
                while (matcher.find()) {
                    match++;
                }
                fileMatch = new SimpleEntry<>(file, match);
            } catch (IOException e) {
                fileMatch = new SimpleEntry<>(file, IO_ERROR);
            }
            future.complete(fileMatch);
        }, callback);
    }


    private CharSequence fromFile(String filename) throws IOException {
        FileInputStream input = new FileInputStream(filename);
        FileChannel channel = input.getChannel();
        ByteBuffer bbuf = channel.map(FileChannel.MapMode.READ_ONLY, 0, (int)channel.size());
        CharBuffer cbuf = Charset.forName("8859_1").newDecoder().decode(bbuf);
        return cbuf;
    }
}
