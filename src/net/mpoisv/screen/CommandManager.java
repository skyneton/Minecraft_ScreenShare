package net.mpoisv.screen;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;

public class CommandManager implements CommandExecutor {

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO Auto-generated method stub
		if(!(sender instanceof Player)) {
			sender.sendMessage("GetDesktop:: 콘솔에서는 사용할 수 없습니다.");
			return true;
		}
		if(args.length > 0) {
			if(args[0].equalsIgnoreCase("web")) {
				if(args.length > 1) {
					try {
						if(args[1].contains("http://") | args[1].contains("https://"))
							Desktop.getDesktop().browse(new URI(args[1]));
						else
							Desktop.getDesktop().browse(new URI("http://"+args[1]));
						sender.sendMessage("GetDesktop:: 웹 페이지를 성공적으로 띄웠습니다.");
					} catch (IOException | URISyntaxException e) {
						// TODO Auto-generated catch block
						sender.sendMessage("웹 페이지를 띄우는데 실패하였습니다.");
						Bukkit.getLogger().info("GetDesktop:: 웹 페이지를 띄우는데 실패하였습니다.");
					}
					return true;
				}
			}
			
			if(args[0].equalsIgnoreCase("start")) {
				if(!ScreenShare.threadManager.runable) {
					ScreenShare.threadManager.Start();
					sender.sendMessage("GetDesktop:: 시작되었습니다.");
				}else
					sender.sendMessage("GetDesktop:: 이미 시작되었습니다.");
				return true;
			}
			
			if(args[0].equalsIgnoreCase("stop")) {
				if(ScreenShare.threadManager.runable) {
					ScreenShare.threadManager.Exit();
					sender.sendMessage("GetDesktop:: 종료되었습니다.");
					return true;
				}
				sender.sendMessage("GetDesktop:: 이미 종료되었습니다.");
				return true;
			}

			if(args[0].equalsIgnoreCase("get")) {
				ItemStack item = new ItemStack(Material.STICK);
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName("§aPos§f 지정 막대기");
				meta.setLore(Arrays.asList("§f좌/우 클릭으로 §aPOS§f를 지정할 수 있습니다.", "§f지정후 build를 통해 액자를 그릴수 있습니다."));
				item.setItemMeta(meta);
				((Player) sender).getInventory().addItem(item);

				item = new ItemStack(Material.BLAZE_ROD);
				meta = item.getItemMeta();
				meta.setDisplayName("§c마우스 클릭§f 막대기");
				meta.setLore(Arrays.asList("§f좌/우 클릭으로 §c마우스를 클릭§f할 수 있습니다."));
				item.setItemMeta(meta);
				((Player) sender).getInventory().addItem(item);
			}
			
			if(args[0].equalsIgnoreCase("build")) {
				if(!EventManager.pos1.containsKey(((Player) sender).getUniqueId()) || !EventManager.pos2.containsKey(((Player) sender).getUniqueId())) {
					sender.sendMessage("GetDesktop:: 먼저 나무막대기로 위치를 지정해주세요.");
					return true;
				}

				Location pos1 = EventManager.pos1.get(((Player) sender).getUniqueId());
				Location pos2 = EventManager.pos2.get(((Player) sender).getUniqueId());
				
				int maxX = (pos1.getBlockX() >= pos2.getBlockX()) ? pos1.getBlockX() : pos2.getBlockX();
				int minX = pos1.getBlockX() + pos2.getBlockX() - maxX;
				
				int maxY = (pos1.getBlockY() >= pos2.getBlockY()) ? pos1.getBlockY() : pos2.getBlockY();
				int minY = pos1.getBlockY() + pos2.getBlockY() - maxY;
				
				int maxZ = (pos1.getBlockZ() >= pos2.getBlockZ()) ? pos1.getBlockZ() : pos2.getBlockZ();
				int minZ = pos1.getBlockZ() + pos2.getBlockZ() - maxZ;

				int height = ScreenShare.height;
				height = (height%128 != 0) ? height/128+1 : height/128;
				
				int width = ScreenShare.width;
				width = (width%128 != 0) ? width/128+1 : width/128;
					
				if(maxX == minX) {
					
					
					sender.sendMessage("GetDesktop:: 가져오는중..");
					for(int Y = maxY; Y >= minY; Y--) {
						if((Y-maxY)*-1+1 > height)
							break;
						if(((Player) sender).getLocation().getX() >= maxX) {
							for(int Z = maxZ; Z >= minZ; Z--) {
								if(Math.abs(Z-maxZ) >= width)
									break;
								Location loc = new Location(((Player) sender).getWorld(), maxX, Y, Z);
								
								loc.getBlock().setType(Material.DARK_OAK_PLANKS);
								loc.add(1, 0, 0);

								ItemStack item = new ItemStack(Material.LEGACY_MAP);
								
								MapMeta meta = (MapMeta) item.getItemMeta();
								meta.setMapId(((Y-maxY)*-1) * width + (Z-maxZ)*-1);
								
								item.setItemMeta(meta);
								
								ItemFrameSpawn(loc, item);
							}
						}else {
							for(int Z = minZ; Z <= maxZ; Z++) {
								if(Math.abs(Z-minZ) >= width)
									break;
								Location loc = new Location(((Player) sender).getWorld(), maxX, Y, Z);
								
								loc.getBlock().setType(Material.DARK_OAK_PLANKS);
								loc.subtract(1, 0, 0);

								ItemStack item = new ItemStack(Material.LEGACY_MAP);
								
								MapMeta meta = (MapMeta) item.getItemMeta();
								meta.setMapId(((Y-maxY)*-1) * width + (Z-minZ));
								
								item.setItemMeta(meta);
								
								ItemFrameSpawn(loc, item);
							}
						}
					}
				}

				else if(maxZ == minZ) {
					sender.sendMessage("GetDesktop:: 가져오는중..");
					for(int Y = maxY; Y >= minY; Y--) {
						if((Y-maxY)*-1+1 > height)
							break;
						if(((Player) sender).getLocation().getZ() >= maxZ) {
							for(int X = minX; X <= maxX; X++) {
								if(Math.abs(X-minX) >= width)
									break;
								Location loc = new Location(((Player) sender).getWorld(), X, Y, maxZ);

								loc.getBlock().setType(Material.DARK_OAK_PLANKS);
								loc.add(0, 0, 1);
								
								
								ItemStack item = new ItemStack(Material.LEGACY_MAP);
								
								MapMeta meta = (MapMeta) item.getItemMeta();
								meta.setMapId(((Y-maxY)*-1) * width + (X-minX));
								
								item.setItemMeta(meta);
								
								ItemFrameSpawn(loc, item);
							}
						}else {
							for(int X = maxX; X >= minX; X--) {
								if(Math.abs(X-maxX) >= width)
									break;
								Location loc = new Location(((Player) sender).getWorld(), X, Y, maxZ);

								loc.getBlock().setType(Material.DARK_OAK_PLANKS);
								loc.subtract(0, 0, 1);

								ItemStack item = new ItemStack(Material.LEGACY_MAP);
								
								MapMeta meta = (MapMeta) item.getItemMeta();
								meta.setMapId(((Y-maxY)*-1) * width + (X-maxX) * -1);
								
								item.setItemMeta(meta);
								
								ItemFrameSpawn(loc, item);
							}
						}
					}
				}
				
				else
					sender.sendMessage("GetDesktop:: 위치는 무조건 직선이여야 합니다. (X축 또는 Z축이 같아야 함)");
				
				return true;
			}
		}
		sender.sendMessage("/"+label+" build        | 컴퓨터화면을 액자에 그립니다. 막대기로 POS1과 POS2를 정해주세요.");
		sender.sendMessage("/"+label+" web [site] | 웹을 컴퓨터에 띄웁니다.");
		sender.sendMessage("/"+label+" stop        | 컴퓨터화면을 정지합니다.");
		sender.sendMessage("/"+label+" start       | 컴퓨터화면을 시작합니다.");
		sender.sendMessage("/"+label+" get         | 관련된 아이템을 얻습니다.");
		return true;
	}
	
	private static void ItemFrameSpawn(Location loc, ItemStack item) {
		for(Entity entity : loc.getWorld().getNearbyEntities(loc, 1.5, 0.5, 1.5)) {
			if(entity instanceof ItemFrame) {
				entity.remove();
			}
		}
		ItemFrame itemFrame = (ItemFrame) loc.getWorld().spawnEntity(loc, EntityType.ITEM_FRAME);
		
		itemFrame.setItem(item);
	}

}
