package net.mpoisv.screen;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.metadata.FixedMetadataValue;

import net.minecraft.server.v1_16_R3.PacketPlayOutMap;

public class EventManager implements Listener {

	public static HashMap<UUID, Location> pos1 = new HashMap<>();
	public static HashMap<UUID, Location> pos2 = new HashMap<>();
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		for(PacketPlayOutMap packet : ScreenShare.threadManager.packets.values()) {
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
				if(ScreenShare.threadManager.runable)
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
		snowball.setMetadata("click", new FixedMetadataValue(ScreenShare.getPlugin(ScreenShare.class), click));
	}
	
	private void mouseClick(PlayerInteractEntityEvent event) {
		Projectile snowball = event.getPlayer().launchProjectile(Snowball.class);
		snowball.setVelocity(event.getPlayer().getEyeLocation().getDirection().multiply(2));
		snowball.setGravity(false);
		snowball.setShooter(event.getPlayer());
		String click = "right";
		snowball.setMetadata("click", new FixedMetadataValue(ScreenShare.getPlugin(ScreenShare.class), click));
	}
	
	@EventHandler
	public void EntityInteract(PlayerInteractEntityEvent event) {
		if(!event.getPlayer().isOp() | event.getPlayer().getItemInHand().getType() != Material.BLAZE_ROD)
			return;
		
		event.setCancelled(true);

		if(ScreenShare.threadManager.runable)
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
