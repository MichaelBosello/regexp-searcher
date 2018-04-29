package ui;

import utility.MillisecondStopWatch;
import utility.StopWatch;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class RegexGUI implements RegexUI{

    private List<String> showedFiles = new LinkedList<>();
    private StopWatch watch = new MillisecondStopWatch();
    private JFrame frame;
    private JTextField matching;
    private JTextField percent;
    private JTextField mean;
    private JTextField ioError;
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
                matching = new JTextField("0");
                matching.setPreferredSize(textFieldDimension);
                stats.add(matching);

                stats.add(new JLabel("Percent: "));
                percent = new JTextField("0");
                percent.setPreferredSize(textFieldDimension);
                stats.add(percent);

                stats.add(new JLabel("Mean: "));
                mean = new JTextField("0");
                mean.setPreferredSize(textFieldDimension);
                stats.add(mean, BorderLayout.CENTER);

                stats.add(new JLabel("IO Error: "));
                ioError = new JTextField("0");
                ioError.setPreferredSize(textFieldDimension);
                stats.add(ioError);

                frame.getContentPane().add(stats, BorderLayout.PAGE_START);

                fileList = new JTextArea();
                JScrollPane scroll = new JScrollPane (fileList,
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                frame.getContentPane().add(scroll, BorderLayout.CENTER);

                frame.setSize(1000, 1000);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    //not thread safe asks method
    String response;
    @Override
    public String ask(String message) {
        try {
            SwingUtilities.invokeAndWait( () -> {
                Object[] possibilities = {"T", "E", "R"};
                response = (String) JOptionPane.showInputDialog(
                        frame, message, "", JOptionPane.PLAIN_MESSAGE, null, possibilities, "T");
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return response;
    }

    String responsePath;
    @Override
    public String askPath() {
        try {
            SwingUtilities.invokeAndWait( () -> {
                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int selected = fc.showOpenDialog(frame);
                if (selected == JFileChooser.APPROVE_OPTION) {
                    responsePath = fc.getSelectedFile().getAbsolutePath();
                } else {
                    responsePath = "/";
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(responsePath);
        return responsePath;
    }

    String responseRegex;
    @Override
    public String askRegex() {
        Pattern pattern = null;
        while(pattern == null) {
            try {
                SwingUtilities.invokeAndWait( () -> {
                    responseRegex = (String) JOptionPane.showInputDialog(
                            frame, "Insert Regex", "", JOptionPane.PLAIN_MESSAGE, null, null, "");
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                pattern = Pattern.compile(responseRegex);
            } catch (Exception e) {}
        }
        return responseRegex;
    }

    String responseDepth;
    @Override
    public int askDepth() {
        int depth = -1;
        while(depth < 0) {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    responseDepth = (String) JOptionPane.showInputDialog(
                            frame, "Insert depth", "", JOptionPane.PLAIN_MESSAGE, null, null, "");
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                depth = Integer.parseInt(responseDepth);
                if (depth < 0)
                    throw new IllegalArgumentException();
            } catch (Exception e) {}
        }
        return depth;
    }

    @Override
    public void updateResult(List<String> files, double percent, double mean, int error) {
        SwingUtilities.invokeLater( () -> {

            for (String file: files) {
                if(!showedFiles.contains(file)){
                    showedFiles.add(file);
                    fileList.append(file + "\n");
                }
            }
            /*if(!files.isEmpty()) {
                fileList.append(files.get(files.size() - 1) + "\n");
            }*/
            this.matching.setText(Integer.toString(files.size()));
            this.percent.setText(Double.toString(percent));
            this.mean.setText(Double.toString(mean));
            this.ioError.setText(Integer.toString(error));
        });
    }

    @Override
    public void start() {
        watch.start();
        SwingUtilities.invokeLater( () -> fileList.append("Computation started\n") );
    }

    @Override
    public void end() {
        watch.stop();
        SwingUtilities.invokeLater( () -> fileList.append("Computation ended in " + watch.getTime() + "ms\n") );
    }
}
