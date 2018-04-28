package forkjoin.action;

import regex.regexresult.RegexResult;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RecursiveAction;

public class RegexWalkerAction extends RecursiveAction {

    private final boolean DEBUG = false;
    File folder;
    String regex;
    RegexResult collector;
    int depth;

    public RegexWalkerAction(String path, String regex, RegexResult collector, int depth) {
        this.folder = new File(path);
        this.regex = regex;
        this.collector = collector;
        this.depth = depth;
    }

    @Override
    protected void compute() {
        int subDepth = depth - 1;
        List<RecursiveAction> forks = new LinkedList<>();
        if(folder.listFiles() != null) {
            for (final File fileEntry : folder.listFiles()) {
                if (fileEntry.isDirectory()) {
                    if(subDepth >= 0) {
                        if(DEBUG)
                            System.out.println("[DEBUG] spawn new directory fork with path: "
                                    + fileEntry.getPath() + " depth: " + subDepth );
                        forks.add(new RegexWalkerAction(fileEntry.getPath(), regex, collector, subDepth));
                    }
                } else {
                    if(DEBUG)
                        System.out.println("[DEBUG] spawn new file fork with filepath: "
                                + fileEntry.getPath() );
                    forks.add(new CountRegexInFileAction(fileEntry.getPath(), regex, collector));
                }
            }
        }
        invokeAll(forks);
    }
}
