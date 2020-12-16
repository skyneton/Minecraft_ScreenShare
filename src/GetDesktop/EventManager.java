package GetDesktop;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.metadata.FixedMetadataValue;

import net.minecraft.server.v1_16_R1.PacketPlayOutMap;

public class EventManager implements Listener {

	public static HashMap<UUID, Location> pos1 = new HashMap<>();
	public static HashMap<UUID, Location> pos2 = new HashMap<>();
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		for(PacketPlayOutMap packet : GetDesktop.threadManager.packets.values()) {
			((CraftPlayer) event.getPlayer()).getHandle().playerConnection.sendPacket(packet);
		}
	}
	
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void OnBlockRightClick(PlayerInteractEvent event) {
		if(!event.getPlayer().isOp() | event.getHand() != EquipmentSlot.HAND)
			return;

		switch(event.getPlayer().getItemInHand().getType()) {
			case STICK:
				event.setCancelled(true);
				posSet(event);
				break;
			case BLAZE_ROD:
				event.setCancelled(true);
				if(GetDesktop.threadManager.runable)
					mouseClick(event);
		}
	}
	
	private void posSet(PlayerInteractEvent event) {
		if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
			if(pos1.containsKey(event.getPlayer().getUniqueId()))
				pos1.replace(event.getPlayer().getUniqueId(), event.getClickedBlock().getLocation());
			else
				pos1.put(event.getPlayer().getUniqueId(), event.getClickedBlock().getLocation());

			event.getPlayer().sendMessage("GetDesktop::§aPOS 1§f "+event.getClickedBlock().getX()+", "+event.getClickedBlock().getY()+", "+event.getClickedBlock().getZ());
		}
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if(pos2.containsKey(event.getPlayer().getUniqueId()))
				pos2.replace(event.getPlayer().getUniqueId(), event.getClickedBlock().getLocation());
			else
				pos2.put(event.getPlayer().getUniqueId(), event.getClickedBlock().getLocation());

			event.getPlayer().sendMessage("GetDesktop::§aPOS 2§f "+event.getClickedBlock().getX()+", "+event.getClickedBlock().getY()+", "+event.getClickedBlock().getZ());
		}
	}
	
	private void mouseClick(PlayerInteractEvent event) {
		Projectile snowball = event.getPlayer().launchProjectile(Snowball.class);
		snowball.setVelocity(event.getPlayer().getEyeLocation().getDirection().multiply(2));
		snowball.setGravity(false);
		snowball.setShooter(event.getPlayer());
		String click;
		if(event.getAction() == Action.LEFT_CLICK_AIR | event.getAction() == Action.LEFT_CLICK_BLOCK)
			click = "left";
		else
			click = "right";
		snowball.setMetadata("click", new FixedMetadataValue(GetDesktop.getPlugin(GetDesktop.class), click));
	}
	
	private void mouseClick(PlayerInteractEntityEvent event) {
		Projectile snowball = event.getPlayer().launchProjectile(Snowball.class);
		snowball.setVelocity(event.getPlayer().getEyeLocation().getDirection().multiply(2));
		snowball.setGravity(false);
		snowball.setShooter(event.getPlayer());
		String click = "right";
		snowball.setMetadata("click", new FixedMetadataValue(GetDesktop.getPlugin(GetDesktop.class), click));
	}
	
	@EventHandler
	public void projectileHit(ProjectileHitEvent event) {
		if(event.getEntity() instanceof Snowball) {
			Projectile snowball = event.getEntity();
			if(!((Player) snowball.getShooter()).isOp() | !snowball.hasMetadata("click"))
				return;
			
			String click = (String) snowball.getMetadata("click").get(0).value();
			
			if(event.getHitEntity() instanceof ItemFrame) {
				ItemFrame frame = (ItemFrame) event.getHitEntity();
				
				if(frame.getItem().getType() != Material.FILLED_MAP)
					return;
				
				MapMeta map = (MapMeta) frame.getItem().getItemMeta();
				
				if(map.getMapId() >= GetDesktop.MaxmapId)
					return;
				
				int height = GetDesktop.height;
				height = (height%128 != 0) ? height/128+1 : height/128;
				
				int width = GetDesktop.width;
				width = (width%128 != 0) ? width/128+1 : width/128;
				
				double persentX, persentY;
				
				if(snowball.getFacing().name().toUpperCase().contains("WEST")) {
//					startX = event.getHitEntity().getLocation().getBlockZ() + map.getMapId()%width;
					persentX = event.getHitEntity().getLocation().getBlockZ() + map.getMapId()%width - snowball.getLocation().getZ();
				}
				else if(snowball.getFacing().name().toUpperCase().contains("EAST")) {
//					startX = event.getHitEntity().getLocation().getBlockZ() - map.getMapId()%width;
					persentX = snowball.getLocation().getZ() - (event.getHitEntity().getLocation().getBlockZ() - map.getMapId()%width);
				}
				else if(snowball.getFacing().name().toUpperCase().contains("SOUTH")) {
//					startX = event.getHitEntity().getLocation().getBlockX() + map.getMapId()%width;
					persentX = event.getHitEntity().getLocation().getBlockX() + map.getMapId()%width - snowball.getLocation().getX();
				}
//				else if(snowball.getFacing().name().toUpperCase().contains("NORTH")) {
				else {
//					startX = event.getHitEntity().getLocation().getBlockX() - map.getMapId()%width;
					persentX = snowball.getLocation().getX() - (event.getHitEntity().getLocation().getBlockX() - map.getMapId()%width);
				}
				
				int startY = event.getHitEntity().getLocation().getBlockY() + map.getMapId()/width;
				
				persentY = (event.getHitEntity().getLocation().getBlockY() + map.getMapId()/width) - snowball.getLocation().getY();

				persentX /= (double) GetDesktop.width/128.0;
				persentY /= (double) GetDesktop.height/128.0;

				persentX = Math.abs(persentX);
				persentY = Math.abs(persentY);
				
				if(persentX > 1) persentX = 1;
				if(persentY > 1) persentY = 1;
				
				int clickX = (int) (GetDesktop.width * persentX);
				int clickY = (int) (GetDesktop.height * persentY);
				
				System.out.println(((Player) snowball.getShooter()).getName() +":: " + String.format("%.2f", persentX)+", "+String.format("%.2f", persentY));
				
				try {
					Robot robot = new Robot();
					robot.mouseMove(clickX, clickY);

					
					switch(click.toUpperCase()) {
					case "LEFT":
						robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
						robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
						break;
					case "RIGHT":
						robot.mousePress(InputEvent.BUTTON2_DOWN_MASK);
						robot.mouseRelease(InputEvent.BUTTON2_DOWN_MASK);
						break;
					}
					
				} catch (AWTException e) {
					// TODO Auto-generated catch block
					System.out.println("마우스 클릭 오류");
				}
			}
		}
	}
	
	@EventHandler
	public void EntityInteract(PlayerInteractEntityEvent event) {
		if(!event.getPlayer().isOp() | event.getPlayer().getItemInHand().getType() != Material.BLAZE_ROD)
			return;
		
		event.setCancelled(true);

		if(GetDesktop.threadManager.runable)
			mouseClick(event);
	}
	
	@EventHandler
	public void projectileDamage(EntityDamageByEntityEvent event) {
		if(event.getDamager() instanceof Snowball) {
			if(((Projectile) event.getDamager()).hasMetadata("click"))
				event.setCancelled(true);
		}
		
		else if(event.getDamager() instanceof Player) {
			if(!((Player) event.getDamager()).isOp())
				return;
			if(((Player) event.getDamager()).getItemInHand().getType() == Material.BLAZE_ROD)
				event.setCancelled(true);
		}
	}
}
