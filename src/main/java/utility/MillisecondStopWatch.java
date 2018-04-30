package utility;

public class MillisecondStopWatch implements StopWatch{

    private boolean running;
    private long startTime;

    public MillisecondStopWatch(){
        running = false;
    }

    @Override
    public synchronized void start(){
        running = true;
        startTime = System.currentTimeMillis();
    }

    @Override
    public synchronized void stop(){
        startTime = getTime();
        running = false;
    }

    @Override
    public synchronized long getTime(){
        if (running){
            return 	System.currentTimeMillis() - startTime;
        } else {
            return startTime;
        }
    }
}
