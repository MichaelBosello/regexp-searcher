import eventloop.VertxController;
import forkjoin.ForkJoinController;
import ui.RegexCommandLineUI;

public class main {

    public static void main(String args[]) {
        new VertxController(new RegexCommandLineUI()).start();
        //new ForkJoinController(new RegexCommandLineUI()).start();
    }

}
