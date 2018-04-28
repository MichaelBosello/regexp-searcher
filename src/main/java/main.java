import forkjoin.forkJoinController;
import ui.RegexCommandLineUI;

public class main {

    public static void main(String args[]) {
        new forkJoinController(new RegexCommandLineUI()).start();
    }

}
