import java.util.concurrent.atomic.AtomicInteger;

public class BurnsME {

    // Variable to count processes in critical section (for verification)
    private static AtomicInteger criticalCount = new AtomicInteger(0);

    // shared var F : array [1..N] of flag;
    private static final boolean[] F = new boolean[10000];

    // Some process-local variables
    private final int processID;
    private boolean   atLine7;

    public BurnsME(int processID) {
        this.processID = processID;
        this.atLine7   = false;
    }

    /**
     * Try to enter critical region.
     * 
     * @return T - success; F - failure, need to try again later 
     */
    public boolean enterCriticalRegion() {

        // Burns Lynch Algorithm
        // Mutual Exclusion Using Indivisible Reads and Writes, p. 836
        if (!atLine7) {
            // 3: F[i] down
            F[processID] = false;

            // 4: for j:=1 to i-1 do if F[j] = up goto 3
            for (int process=0; process<processID; process++)
                if (F[process]) return false;

            // 5: F[i] = up
            F[processID] = true;

            // 6: for j:=1 to i-1 do if F[j] = up goto 3
            for (int process=0; process<processID; process++)
                if (F[process]) return false;

            atLine7 = true;
        }

        // 7: for j:=i+1 to N do if F[j] = up goto 7
        for (int process=processID+1; process<F.length; process++) 
            if (F[process]) return false;

        // Verify mutual exclusion
        if (criticalCount.incrementAndGet()>1) {
            System.err.println("TWO PROCESSES ENTERED CRITICAL SECTION!");
            System.exit(1);
        }

        // 8: critical region
        return true;
    }

    /**
     * Leave critical region and allow next process in
     */
    public void leaveCriticalRegion() {

        // Reset state
        atLine7 = false;
        criticalCount.decrementAndGet();

        // Release critical region lock
        // 1: F[i] = down
        F[processID] = false;
    }

    //===============================================================================
    // Test Code

    private static final int THREADS = 50;

    public static void main(String[] args) {

        System.out.println("Launching "+THREADS+" threads...");

        for (int i=0; i<THREADS; i++) {
            final int threadID = i;

            new Thread() {
                @Override
                public void run() {
                    BurnsME mutex = new BurnsME(threadID);

                    while (true) {
                        if (mutex.enterCriticalRegion()) {
                            System.out.println(threadID+" in critical region");
                            mutex.leaveCriticalRegion();
                        }
                    }
                }
            }.start();
        }

        while (true);
    }
}
