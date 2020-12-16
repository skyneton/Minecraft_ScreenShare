package GetDesktop;

import java.awt.Toolkit;

import org.bukkit.Bukkit;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;

public class GetDesktop extends JavaPlugin {
	
	public static int width, height;
	public static int MaxmapId = 0;
	
	public static ThreadManager threadManager;
	
	@SuppressWarnings("deprecation")
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new EventManager(), this);
		getCommand("GetDesktop").setExecutor(new CommandManager());
		
		width = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth());
		height = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight());
		
		threadManager = new ThreadManager();
		
		for(int y = 0; y <= height/128; y++) {
			if(y*128 >= height)
				break;
			for(int x = 0; x <= width/128; x++) {
				if(x*128 >= width)
					break;
				MapView v;
				if(Bukkit.getMap(MaxmapId) == null) {
					v = Bukkit.createMap(Bukkit.getWorlds().get(0));
				} else
					v = Bukkit.getMap(MaxmapId);
				for(MapRenderer vi : v.getRenderers())
					v.removeRenderer(vi);
				threadManager.vs.put(MaxmapId, new int[]{y, x});
				
				MaxmapId++;
			}
		}
	}
	
	public void onDisable() {
		threadManager.Stop();
	}
}
