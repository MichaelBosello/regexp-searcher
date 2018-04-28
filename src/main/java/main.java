import forkjoin.ForkJoinController;
import ui.RegexCommandLineUI;

public class main {

    public static void main(String args[]) {
        new ForkJoinController(new RegexCommandLineUI()).start();
    }

}
