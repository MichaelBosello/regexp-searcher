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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexVerticle extends AbstractVerticle {

    private final static int IOERROR = -1;
    private RegexUI ui;
    private RegexResult result;
    private String path;
    private String regex;
    private int depth;
    private boolean askUI;
    Future<Void> failFuture = Future.future();
    int asyncSpown = 0;

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

        Future<Integer> walker = Future.future();
        Future<Map<String, Integer>> fileAnalysis = Future.future();
        asyncSpown++;
        walkDirectories(path, depth, walker.completer(),fileAnalysis.completer());

        walker.compose( spawns -> {
            asyncSpown += spawns;
            asyncSpown--;
            if(asyncSpown == 0){
                ui.end();
            }
        }, failFuture);

        fileAnalysis.compose( fileMatches -> {

        }, failFuture);

    }

    private void walkDirectories(String path, int depth,
                                 Handler<AsyncResult<Integer>> walkCallback,
                                 Handler<AsyncResult<Map<String, Integer>>> fileCallback){
        vertx.executeBlocking(future -> {
            int subDepth = depth - 1;
            int asyncFunctionSpawned = 0;
            File folder = new File(path);
            if(folder.listFiles() != null) {
                for (final File fileEntry : folder.listFiles()) {
                    if (fileEntry.isDirectory()) {
                        if(depth >= 0) {
                            walkDirectories(fileEntry.getPath(), subDepth, walkCallback, fileCallback);
                            asyncFunctionSpawned++;
                        }
                    } else {
                        regexCountInFile(fileEntry.getPath(), fileCallback);
                        asyncFunctionSpawned++;
                    }
                }
            }
            future.complete(asyncFunctionSpawned);
        }, walkCallback);
    }

    private void regexCountInFile(String file, Handler<AsyncResult<Map<String, Integer>>> callback){
        vertx.executeBlocking(future -> {
            Map<String, Integer> fileMatch = new HashMap<>();
            try {
                int match = 0;
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(fromFile(file));
                while (matcher.find()) {
                    match++;
                }
                fileMatch.put(file, match);
            } catch (IOException e) {
                fileMatch.put(file, IOERROR);
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
