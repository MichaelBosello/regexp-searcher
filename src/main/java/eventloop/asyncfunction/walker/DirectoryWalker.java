package eventloop.asyncfunction.walker;

import eventloop.asyncfunction.AsyncSpawnTracker;
import eventloop.asyncfunction.FutureFactory;
import io.vertx.core.Vertx;

import java.io.File;

import static utility.FileUtility.walkDirectory;

public class DirectoryWalker {

    private final static boolean DEBUG = false;
    private final Vertx vertx;
    private final FileAction fileAction;
    private final FutureFactory<AsyncSpawnTracker> futureFactory;

    public DirectoryWalker(Vertx vertx, FileAction fileAction, FutureFactory<AsyncSpawnTracker> futureFactory) {
        this.vertx = vertx;
        this.fileAction = fileAction;
        this.futureFactory = futureFactory;
    }

    public void walkDirectories(String path, int depth, int parent){
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
                fileAction.act(file.getPath(), myself);
                asyncFunctionSpawned.incrementAsyncSpawn();
            });

            if(DEBUG)
                System.out.println("Walk completed " + Thread.currentThread().getName());
            future.complete(asyncFunctionSpawned);
        }, false, futureFactory.compose().completer());
    }
}
