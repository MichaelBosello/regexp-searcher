package ui;

import utility.MillisecondStopWatch;
import utility.StopWatch;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

public class RegexCommandLineUI implements RegexUI{

    private Scanner console = new Scanner(System.in);
    private StopWatch watch = new MillisecondStopWatch();

    @Override
    public String ask(String message){
        System.out.println(message);
        return console.nextLine();
    }

    @Override
    public String askMethod(){
        System.out.println("Select computation method:\n(T) Task [Default]\n(E) Eventloop\n(R)Reactive Stream");
        return console.nextLine();
    }

    @Override
    public String askPath(){
        return ask("Insert base path");
    }

    @Override
    public String askRegex(){
        Pattern pattern = null;
        String regex = "";
        while(pattern == null) {
            System.out.println("Insert Regex");
            try {
                regex = console.nextLine();
                pattern = Pattern.compile(regex);
            } catch (Exception e) {}
        }
        return regex;
    }

    @Override
    public int askDepth(){
        int depth = -1;
        while(depth < 0) {
            System.out.println("Insert depth");
            try {
                depth = Integer.parseInt(console.nextLine());
                if (depth < 0)
                    throw new IllegalArgumentException();
            } catch (Exception e) {}
        }
        return depth;

    }

    @Override
    public void updateResult(List<String> files, double percent, Map.Entry<Long, Long> mean, int error) {
        System.out.println("updated result:");
        System.out.println("% of file with at least one matching: " + percent);
        System.out.println("mean of matches among files with matches: " + mean.getKey() + "." + mean.getValue());
        System.out.println("IO errors: " + error);
        System.out.println("File matching list:");
        files.forEach(System.out::println);
        System.out.println();
        System.out.println();
    }

    @Override
    public void start(){
        System.out.println("Computation started");
        watch.start();
    }

    @Override
    public void end(){
        watch.stop();
        System.out.println("Computation ended in " + watch.getTime() + "ms");
    }
}
