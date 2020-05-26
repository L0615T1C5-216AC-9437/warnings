package w;

import arc.Events;
import arc.net.Server;
import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Time;
import mindustry.core.NetServer;
import mindustry.entities.type.Player;
import mindustry.game.EventType;
import mindustry.gen.Call;
import mindustry.net.Administration;
import mindustry.plugin.Plugin;

import java.util.HashMap;

import static mindustry.Vars.netServer;

public class Main extends Plugin {
    //Var
    public static HashMap<String, String> nameUUID = new HashMap<>();
    public static HashMap<String, String> kickIP = new HashMap<>();
    public static HashMap<Long, String> kickIPTimer = new HashMap<>();

    public static Thread cycleThread;
    ///Var
    //on start
    public Main() {
        Thread c = new Cycle(Thread.currentThread());
        c.setDaemon(false);
        c.start();

        Events.on(EventType.PlayerJoin.class, event -> {
            Player player = event.player;
            String name = "[#"+player.color+"]"+player.name;
            if (nameUUID.containsKey(player.uuid)) {
                if (!nameUUID.get(player.uuid).equals(name)) {
                    Call.sendMessage("[yellow]> [white]"+nameUUID.get(player.uuid)+" [yellow]changed name to [white]"+name+" [yellow]!");
                    nameUUID.put(player.uuid,name);
                }
            } else {
                nameUUID.put(player.uuid,name);
            }

            if (kickIPTimer.containsValue(player.con.address)) {
                if (!kickIP.get(player.con.address).equals(name)) {
                    Call.sendMessage("\n[yellow]> [white]"+kickIP.get(player.con.address)+" [yellow]changed name to [white]"+name+" [yellow]!");
                }
                Call.sendMessage("\n[yellow]WARNING! "+name +" [yellow]has evaded kick!\n");
                Log.warn(player.uuid + " has evaded kick!");
            }
        });
        Events.on(EventType.PlayerLeave.class, event -> {
            Player player = event.player;
            if (player.getInfo().lastKicked > Time.millis()) {
                String name = "[#"+player.color+"]"+player.name;
                kickIP.put(player.con.address,name);
                kickIPTimer.put(player.getInfo().lastKicked, player.con.address);
            }
        });
        Events.on(EventType.WaveEvent.class, event -> {
            if (cycleThread != null && !cycleThread.isAlive()) {
                Log.warn("Cycle Thread Died - Restarting Cycle Thread");
                Thread a = new Cycle(Thread.currentThread());
                a.setDaemon(false);
                a.start();
            }
        });
    }

    public void registerServerCommands(CommandHandler handler) {
        handler.register("kicked", "Lists all kicked players", arg -> {
            if (kickIPTimer.isEmpty()) {
                Log.info("No current kicks");
            } else {
                StringBuilder builder = new StringBuilder();
                builder.append("\n\nKick List:\n");
                kickIPTimer.forEach((k,v) -> {
                    builder.append(byteCode.noColors(kickIP.get(v))).append(" : ").append(v).append(" : ").append((int)(k-Time.millis())/1000).append("s remaining.").append("\n");
                });
                Log.info(builder.toString());
            }
        });
    }
}