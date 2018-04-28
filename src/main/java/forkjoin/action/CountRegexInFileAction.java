package forkjoin.action;

import regex.regexresult.RegexResult;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
*
* Thanks to:
* https://www.java-tips.org/java-se-tips-100019/37-java-util-regex/1716-how-to-apply-regular-expressions-on-the-contents-of-a-file.html
*
* */

public class CountRegexInFileAction extends RecursiveAction {

    String file;
    String regex;
    RegexResult collector;

    public CountRegexInFileAction(String file, String regex, RegexResult collector) {
        this.file = file;
        this.regex = regex;
        this.collector = collector;
    }

    @Override
    protected void compute() {
        try {
            int match = 0;
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(fromFile(file));
            while (matcher.find()) {
                match++;
            }
            if(match > 0){
                collector.addMatchingFile(file,match);
            } else {
                collector.addNonMatchingFile(file);
            }
        } catch (IOException e) {
            collector.incrementIOException();
        }
    }

    private CharSequence fromFile(String filename) throws IOException {
        FileInputStream input = new FileInputStream(filename);
        FileChannel channel = input.getChannel();

        // Create a read-only CharBuffer on the file
        ByteBuffer bbuf = channel.map(FileChannel.MapMode.READ_ONLY, 0, (int)channel.size());
        CharBuffer cbuf = Charset.forName("8859_1").newDecoder().decode(bbuf);
        return cbuf;
    }
}
