package utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 *
 * Thanks to:
 * https://www.java-tips.org/java-se-tips-100019/37-java-util-regex/1716-how-to-apply-regular-expressions-on-the-contents-of-a-file.html
 *
 * */

public class FileUtility {

    public static void walkDirectory(File directory, Consumer<File> executeIfDir, Consumer<File> executeIfFile){
        if(directory.listFiles() != null) {
            for (final File fileEntry : directory.listFiles()) {
                if (fileEntry.isDirectory()) {
                    executeIfDir.accept(fileEntry);
                } else {
                    executeIfFile.accept(fileEntry);
                }
            }
        }
    }

    public static int countMatch(String regex, String file) throws IOException {
        int match = 0;
        try (FileInputStream input = new FileInputStream(file)) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(fromFile(input));
            while (matcher.find()) {
                match++;
            }
        }
        return match;
    }

    private static CharSequence fromFile(FileInputStream input) throws IOException {
        FileChannel channel = input.getChannel();
        ByteBuffer byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, (int)channel.size());
        return Charset.forName("8859_1").newDecoder().decode(byteBuffer);
    }
}
