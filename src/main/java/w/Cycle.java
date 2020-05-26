package w;

import arc.util.Log;
import arc.util.Time;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class Cycle extends Thread {
    private Thread main;
    private HashMap<Long, String> remove = new HashMap<>();

    public Cycle(Thread mainThread) { main = mainThread;}

    public void run() {
        Main.cycleThread =Thread.currentThread();
        Log.info("Cycle Started");

        while (main.isAlive()) {
            try {
                TimeUnit.SECONDS.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!Main.kickIPTimer.isEmpty()) Main.kickIPTimer.forEach((k,v) -> {
                if (Time.millis() > k) {
                    Main.kickIP.remove(v);
                    remove.put(k, "");
                }
            });
            if (!remove.isEmpty()) remove.forEach((k,v) -> {
                Main.kickIPTimer.remove(k);
            });
            remove.clear();
        }
    }
}
