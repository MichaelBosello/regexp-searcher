package forkjoin.action;

import regex.regexresult.Result;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RecursiveAction;

import static utility.FileUtility.walkDirectory;

public class RegexWalkerAction extends RecursiveAction {

    protected static final boolean DEBUG = false;
    protected File folder;
    protected String regex;
    protected Result collector;
    protected int depth;

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
                forks.add(newWalker(directory.getPath(), subDepth));
            }
        }, (file) -> {
            if(DEBUG)
                System.out.println("[DEBUG] spawn new file fork with filepath: "
                        + file.getPath() );
            forks.add(newAnalyzer(file.getPath()));
        });
        invokeAll(forks);
    }

    protected RegexWalkerAction newWalker(String subPath, int subDepth){
        return new RegexWalkerAction(subPath, regex, collector, subDepth);
    }

    protected CountRegexInFileAction newAnalyzer(String subPath){
        return new CountRegexInFileAction(subPath , regex, collector);
    }
}
