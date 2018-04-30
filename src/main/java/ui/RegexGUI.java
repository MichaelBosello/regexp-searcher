package ui;

import utility.MillisecondStopWatch;
import utility.StopWatch;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Pattern;

import static java.util.concurrent.TimeUnit.SECONDS;

public class RegexGUI implements RegexUI{

    private final static int REFRESH_SLEEP = 250;
    private ScheduledExecutorService updateExecutor = Executors.newSingleThreadScheduledExecutor();
    private Runnable runUpdate;
    private Semaphore updateRace = new Semaphore(1);
    private List<String> toAppend = new LinkedList<>();
    private int ioError = 0;
    private int matching = 0;
    private double percent = 0;
    private double mean = 0;
    private StopWatch watch = new MillisecondStopWatch();
    private JFrame frame;
    private JTextField matchingField;
    private JTextField percentField;
    private JTextField meanField;
    private JTextField ioErrorField;
    private JTextArea fileList;
    private Dimension textFieldDimension = new Dimension(150,25);

    public RegexGUI() {
        try {
            SwingUtilities.invokeAndWait( () -> {
                frame = new JFrame();
                frame.setTitle("Regex searcher");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLayout(new BorderLayout());

                JPanel stats = new JPanel(new FlowLayout());

                stats.add(new JLabel("Matching file: "));
                matchingField = new JTextField("0");
                matchingField.setPreferredSize(textFieldDimension);
                stats.add(matchingField);

                stats.add(new JLabel("Percent: "));
                percentField = new JTextField("0");
                percentField.setPreferredSize(textFieldDimension);
                stats.add(percentField);

                stats.add(new JLabel("Mean: "));
                meanField = new JTextField("0");
                meanField.setPreferredSize(textFieldDimension);
                stats.add(meanField, BorderLayout.CENTER);

                stats.add(new JLabel("IO Error: "));
                ioErrorField = new JTextField("0");
                ioErrorField.setPreferredSize(textFieldDimension);
                stats.add(ioErrorField);

                frame.getContentPane().add(stats, BorderLayout.PAGE_START);

                fileList = new JTextArea();
                JScrollPane scroll = new JScrollPane (fileList,
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                frame.getContentPane().add(scroll, BorderLayout.CENTER);

                frame.setSize(1000, 1000);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        runUpdate = () -> {
            try {
                updateRace.acquire();
                // JTextComponent.setText() and JTextArea.append() are thread safe
                for(String file : toAppend) {
                    fileList.append(file + "\n");
                }
                toAppend.clear();
                this.matchingField.setText(Integer.toString(matching));
                this.percentField.setText(Double.toString(percent));
                this.meanField.setText(Double.toString(mean));
                this.ioErrorField.setText(Integer.toString(ioError));

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                updateRace.release();
            }
        };
    }



    @Override
    public String ask(String message) {
        String response = "";
        try {
            FutureTask<String> dialogTask = new FutureTask<>(() -> {
                Object[] possibilities = {"T", "E", "R"};
                return (String) JOptionPane.showInputDialog(
                        frame, message, "", JOptionPane.PLAIN_MESSAGE, null, possibilities, "T");
            });
            SwingUtilities.invokeLater(dialogTask);
            response = dialogTask.get();
            if(response == null){
                response = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public String askPath() {
        String response = "";
        try {
            FutureTask<String> dialogTask = new FutureTask<>(() -> {
                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int selected = fc.showOpenDialog(frame);
                if (selected == JFileChooser.APPROVE_OPTION) {
                    return fc.getSelectedFile().getAbsolutePath();
                } else {
                    return "/";
                }
            });
            SwingUtilities.invokeLater(dialogTask);
            response = dialogTask.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public String askRegex() {
        String response = "";
        Pattern pattern = null;
        while(pattern == null) {
            try {
                FutureTask<String> dialogTask = new FutureTask<>(() ->
                        (String) JOptionPane.showInputDialog(
                        frame, "Insert Regex", "", JOptionPane.PLAIN_MESSAGE, null, null, ""));
                SwingUtilities.invokeLater(dialogTask);
                response = dialogTask.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                pattern = Pattern.compile(response);
            } catch (Exception e) {}
        }
        return response;
    }

    @Override
    public int askDepth() {
        String response = "";
        int depth = -1;
        while(depth < 0) {
            try {
                FutureTask<String> dialogTask = new FutureTask<>(() ->
                        (String) JOptionPane.showInputDialog(
                                frame, "Insert depth", "", JOptionPane.PLAIN_MESSAGE, null, null, ""));
                SwingUtilities.invokeLater(dialogTask);
                response = dialogTask.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                depth = Integer.parseInt(response);
                if (depth < 0)
                    throw new IllegalArgumentException();
            } catch (Exception e) {}
        }
        return depth;
    }

    @Override
    public void updateResult(List<String> files, double percent, double mean, int error) {
        try {
            updateRace.acquire();
            toAppend.addAll(files);
            this.matching += files.size();
            this.percent = percent;
            this.mean = mean;
            this.ioError = error;

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            updateRace.release();
        }
    }

    @Override
    public void start() {
        watch.start();
        fileList.append("Computation started\n");
        updateExecutor.scheduleAtFixedRate(runUpdate, 0, REFRESH_SLEEP, TimeUnit.MILLISECONDS);
    }

    @Override
    public void end() {
        watch.stop();
        try {
            updateExecutor.shutdownNow();
            updateExecutor.awaitTermination(Long.MAX_VALUE, SECONDS);
        } catch (InterruptedException e) { }
        runUpdate.run();
        fileList.append("Computation ended in " + watch.getTime() + "ms\n");
    }
}
