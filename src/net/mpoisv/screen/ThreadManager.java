package net.mpoisv.screen;

import java.awt.AWTException;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.libs.org.apache.commons.codec.binary.Base64;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_16_R3.PacketPlayOutMap;

public class ThreadManager {
	
	private static Renderer renderer;
	
//	private long time;
	
	private Robot r;
	
	private Rectangle rec;

	ExecutorService es = Executors.newFixedThreadPool(1);
	ExecutorService esGetPacket = Executors.newFixedThreadPool(1);
	ExecutorService esSendPacket = Executors.newFixedThreadPool(1);
	ExecutorService esCapture = Executors.newFixedThreadPool(1);
	
	public boolean runable = false;
	
	public HashMap<Integer, int[]> vs = new HashMap<>();

	HashMap<Integer, String> ImgHashLast = new HashMap<>();
	public HashMap<Integer, PacketPlayOutMap> packets = new HashMap<>();
	
	public int width, height;
	
	byte[] buf;
	
//	public Renderer renderer;
	
	public ThreadManager() {
		try {
			this.r = new Robot();
//			rec = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
			rec = new Rectangle(ScreenShare.width, ScreenShare.height);
			
			renderer = new Renderer(ScreenShare.width, ScreenShare.height);
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void Start() {
		runable = true;
		
//		time = System.currentTimeMillis();

		height = ScreenShare.height;
		height = (height%128 != 0) ? height/128+1 : height/128;

		width = ScreenShare.width;
		width = (width%128 != 0) ? width/128+1 : width/128;
		
		esCapture.submit(new CaptureWorker());

		for(int i = 0; i < height; i += 2) {
			es.submit(new MainWorker(i, width));
		}
	}
	
	public void Exit() {
		runable = false;
	}
	
	public void Stop() {
		runable = false;
		esSendPacket.shutdown();
		esGetPacket.shutdown();
		esCapture.shutdown();
		es.shutdown();
		while(!es.isShutdown() | !esGetPacket.isShutdown() | !esCapture.isShutdown() | !esSendPacket.isShutdown());
	}
	
	public String getMD5(byte[] bytes) {
		
		try {
			
			MessageDigest md = MessageDigest.getInstance("MD5");
			
			md.reset();
			md.update(bytes);
			
			Base64 base64 = new Base64();
			byte[] byteData = base64.encode(md.digest());
			
			return new String(byteData);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public BufferedImage ImageResize(BufferedImage img, int width, int height) {
		try {
			BufferedImage output = new BufferedImage(width, height, img.getType());
			
			Graphics2D graphics2d = output.createGraphics();
			graphics2d.drawImage(img, 0, 0, width, height, null);
			graphics2d.dispose();
			
			return output;
		}catch(Exception e) {
			return img;
		}
	}
	
	class CaptureWorker implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(runable) {
//				BufferedImage bufferedImage = ImageResize(r.createScreenCapture(rec), GetDesktop.width, GetDesktop.height);
				BufferedImage bufferedImage = r.createScreenCapture(rec);
				buf = renderer.render(bufferedImage);
			}
		}
		
	}
	
	class MainWorker implements Runnable {
		public final int h, width;
		
		public MainWorker(int h, int width) {
			// TODO Auto-generated constructor stub
			this.h = h;
			this.width = width;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
//			while(runable) {
//				BufferedImage bufferedImage = ImageResize(r.createScreenCapture(rec), GetDesktop.width, GetDesktop.height);
////				es.submit(new GetPacket(renderer.render(bufferedImage)));
//				for(int i = 0; i < width; i++) {
//					Future<?> x = esGetPacket.submit(new GetPacket(bufferedImage, width * h + i));
//					while(!x.isDone());
//				}
//				
//				long now = System.currentTimeMillis();
////				System.out.println(now-time+"ms");
//				time = now;
//			}
			
			Timer timer = new Timer();
			TimerTask task = new TimerTask() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					while(runable) {
						
	//					es.submit(new GetPacket(renderer.render(bufferedImage)));
						for(int i = 0; i < width * 2 && width * h + i < width * height; i++) {
							Future<?> x = esGetPacket.submit(new GetPacket(width * h + i));
							while(!x.isDone());
						}
	
//						long now = System.currentTimeMillis();
//						System.out.println(now-time+"ms");
//						time = now;
					}
				}
				
			};
			
			timer.schedule(task, 1);
		}
		
	}
	
	class GetPacket implements Runnable {
		
		int i;
		
		public GetPacket(int i) {
			this.i = i;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			byte[] SplitImg = GetSplitImg(vs.get(i)[1] * 128, vs.get(i)[0] * 128);
			
//			byte[] SplitImg = MapPalette.imageToBytes(buf.getSubimage(vs.get(i)[1] * 128, vs.get(i)[0] * 128, (GetDesktop.width < vs.get(i)[1] * 128 + 128) ? GetDesktop.width - vs.get(i)[1] * 128 : 128, (GetDesktop.height < vs.get(i)[0] * 128 + 128) ? GetDesktop.height - vs.get(i)[0] * 128 : 128));
			String hash = getMD5(SplitImg);
			
			if(ImgHashLast.containsKey(i)) {
				if(!ImgHashLast.get(i).equalsIgnoreCase(hash)) {
					ImgHashLast.replace(i, hash);
					
					PacketPlayOutMap packet = new PacketPlayOutMap(i, (byte) 0, false, false, new ArrayList<>(), SplitImg, 0, 0, 128, 128);
					esSendPacket.submit(new SendPacket(packet));
					
					packets.put(i, packet);
				}
			}else {
				ImgHashLast.put(i, hash);
				
				PacketPlayOutMap packet = new PacketPlayOutMap(i, (byte) 0, false, false, new ArrayList<>(), SplitImg, 0, 0, 128, 128);
				esSendPacket.submit(new SendPacket(packet));
				
				packets.put(i, packet);
			}
		}
		
		public byte[] GetSplitImg(int sx, int sy) {
			byte[] b = new byte[16384];
			
			for(int y = 0; y < 128 && sy + y < ScreenShare.height; y++) {
				for(int x = 0; x < 128 && sx + x < ScreenShare.width; x++) {
					b[y * 128 + x] = buf[ScreenShare.width * (sy + y) + (sx + x)];
				}
			}
			
			return b;
		}
		
	}
	
	class SendPacket implements Runnable {
		
		PacketPlayOutMap packet;
		
		public SendPacket(PacketPlayOutMap packet) {
			this.packet = packet;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			for(Player player : Bukkit.getOnlinePlayers())
				((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
		}
	}
}
