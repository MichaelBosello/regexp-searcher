package eventloop.asyncfunction;

public class AsyncSpawnTracker {
    private final int functionID;
    private final int parentID;
    private int asyncSpawn;

    public AsyncSpawnTracker(int functionID, int parentID, int asyncSpawn) {
        this.functionID = functionID;
        this.parentID = parentID;
        this.asyncSpawn = asyncSpawn;
    }

    public AsyncSpawnTracker(int functionID, int parentID) {
        this(functionID, parentID, 0);
    }

    public void incrementAsyncSpawn() {
        asyncSpawn++;
    }

    public int getFunctionID() {
        return functionID;
    }

    public int getParentID() {
        return parentID;
    }

    public int getAsyncSpawn() {
        return asyncSpawn;
    }
}