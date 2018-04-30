package forkjoin.action;

import regex.regexresult.Result;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RecursiveAction;

import static utility.FileUtility.walkDirectory;

public class RegexWalkerAction extends RecursiveAction {

    private final boolean DEBUG = false;
    private File folder;
    private String regex;
    private Result collector;
    private int depth;

    public RegexWalkerAction(String path, String regex, Result collector, int depth) {
        this.folder = new File(path);
        this.regex = regex;
        this.collector = collector;
        this.depth = depth;
    }

    @Override
    protected void compute() {
        int subDepth = depth - 1;
        List<RecursiveAction> forks = new LinkedList<>();
        walkDirectory(folder, (directory) -> {
            if(subDepth >= 0) {
                if(DEBUG)
                    System.out.println("[DEBUG] spawn new directory fork with path: "
                            + directory.getPath() + " depth: " + subDepth );
                forks.add(new RegexWalkerAction(directory.getPath(), regex, collector, subDepth));
            }
        }, (file) -> {
            if(DEBUG)
                System.out.println("[DEBUG] spawn new file fork with filepath: "
                        + file.getPath() );
            forks.add(new CountRegexInFileAction(file.getPath(), regex, collector));
        });
        invokeAll(forks);
    }
}
