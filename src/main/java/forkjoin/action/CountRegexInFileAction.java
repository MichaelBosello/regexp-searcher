package forkjoin.action;

import regex.regexresult.Result;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static utility.FileUtility.countMatch;

public class CountRegexInFileAction extends RecursiveAction {

    String file;
    String regex;
    Result collector;

    public CountRegexInFileAction(String file, String regex, Result collector) {
        this.file = file;
        this.regex = regex;
        this.collector = collector;
    }

    @Override
    protected void compute() {
        try {
            int match = countMatch(regex, file);
            if(match > 0){
                collector.addMatchingFile(file, match);
            } else {
                collector.addNonMatchingFile(file);
            }
        } catch (IOException e) {
            collector.incrementIOException();
        }
    }
}
