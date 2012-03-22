package cah.melonar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class ChunkyCarts extends JavaPlugin implements Runnable, Listener {
	private final Logger logger = Logger.getLogger("Minecraft");
	private final PluginManager pm = getServer().getPluginManager();
	private final BukkitScheduler sched = getServer().getScheduler();
	
	@Override
	public void onDisable(){
		PluginDescriptionFile pdfFile = this.getDescription();
		logger.info(pdfFile.getName() + " is now disabled.");
	}
	
	@Override
	public void onEnable(){
		sched.scheduleSyncRepeatingTask(this, this, 6000L, 6000L);
		pm.registerEvents(this, this);
		PluginDescriptionFile pdfFile = this.getDescription();
		logger.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled.");	
	}

	@EventHandler
	public final void onMinecartMove(VehicleMoveEvent vmEvent){
		if(vmEvent.getVehicle() instanceof Minecart) {
			for(int x = -3; x<= 3; x++) {
				for(int z=-3; z<=3; z++) {
					Location here = vmEvent.getTo();
					World world = here.getWorld();
					world.loadChunk( (here.getChunk().getX() + x), (here.getChunk().getZ() + z) );				}
			}
		}
	}

	@Override
	public void run() {
		Iterator<World> it = getServer().getWorlds().iterator();
		while(it.hasNext()){
			World currentWorld = it.next();
			Iterator<Chunk> chunks = (new ArrayList<Chunk>(Arrays.asList(currentWorld.getLoadedChunks())) ).iterator();
			while(chunks.hasNext()){
				Chunk lChunk = chunks.next();
				ChunkUnloadEvent lChunkUnloadEvent = new ChunkUnloadEvent(lChunk);
				pm.callEvent(lChunkUnloadEvent);
				if(lChunkUnloadEvent.isCancelled()){
					continue;
				}
				lChunk.unload(true, true);
			}
		}
	}
	
	@EventHandler
	public final void ccChunkUnload(ChunkUnloadEvent cuEvent){
		World currentWorld = cuEvent.getWorld();
		for (int x = -3; x <= 3; x++){
			for (int z = -3; z<= 3; z++){
				@SuppressWarnings("unchecked")
				Iterator<Minecart> carts = (Iterator<Minecart>)currentWorld.getEntitiesByClass(Minecart.class).iterator();
				while(carts.hasNext()){
					Minecart currentCart = carts.next();
					if((currentCart.getVelocity().getX() == 0.0D) && (currentCart.getVelocity().getZ() == 0.0D) && (currentCart.getVelocity().getY() == 0.0D) || (currentCart.getLocation().getChunk().getX() != x + cuEvent.getChunk().getX()) || (currentCart.getLocation().getChunk().getZ() != z + cuEvent.getChunk().getZ())) {
						continue;
					}
					cuEvent.setCancelled(true);
					return;
				}
			}
		}
	}
}
