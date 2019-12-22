package amata1219.like;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import amata1219.like.bookmark.Bookmark;
import amata1219.like.bookmark.BookmarkDatabase;
import amata1219.like.config.LikeDatabase;
import amata1219.like.config.LikeLimitDatabase;
import amata1219.like.config.MainConfig;
import amata1219.like.masquerade.dsl.component.Layout;
import amata1219.like.masquerade.enchantment.GleamEnchantment;
import amata1219.like.masquerade.listener.UIListener;
import amata1219.like.monad.Maybe;
import amata1219.like.player.PlayerData;
import amata1219.like.player.PlayerDatabase;
import amata1219.like.reflection.Field;
import amata1219.like.reflection.SafeCast;
import amata1219.like.tuplet.Tuple;
import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin {
	
	private static Main plugin;
	
	public static final String INVITATION_TOKEN = UUID.randomUUID().toString();
	public static final String OPERATOR_PERMISSION = "like.likeop";
	
	public static Main plugin(){
		return plugin;
	}
	
	private Economy economy;
	
	private MainConfig config;
	private LikeDatabase likeDatabase;
	private PlayerDatabase playerDatabase;
	private LikeLimitDatabase likeLimitDatabase;
	private BookmarkDatabase bookmarkDatabase;
	
	public final HashMap<Long, Like> likes = new HashMap<>();
	public final HashMap<UUID, PlayerData> players = new HashMap<>();
	public final HashMap<String, Bookmark> bookmarks = new HashMap<>();
	public final HashMap<UUID, Long> descriptionEditors = new HashMap<>();
	public final HashSet<UUID> cooldownMap = new HashSet<>();
	
	@Override
	public void onEnable(){
		plugin = this;
		
		Plugin valut = getServer().getPluginManager().getPlugin("Vault");
		if(!(valut instanceof Vault)) new NullPointerException("Not found Vault.");

		RegisteredServiceProvider<Economy> provider = getServer().getServicesManager().getRegistration(Economy.class);
		if(provider == null) new NullPointerException("Not found Vault.");

		economy = provider.getProvider();
		
		getServer().getPluginManager().registerEvents(new UIListener(), this);

		Field<Enchantment, Boolean> acceptingNew = Field.of(Enchantment.class, "acceptingNew");
		acceptingNew.set(null, true);
		try{
			Enchantment.registerEnchantment(GleamEnchantment.INSTANCE);
		}catch(Exception e){
			
		}finally{
			acceptingNew.set(null, false);
		}
		
		config = new MainConfig();
		
		likeDatabase = new LikeDatabase();
		Tuple<HashMap<Long, Like>, HashMap<UUID, List<Like>>> maps = likeDatabase.load();
		maps.first.forEach((id, like) -> likes.put(id, like));
		
		playerDatabase = new PlayerDatabase();
		playerDatabase.load(maps.second).forEach((uuid, data) -> players.put(uuid, data));
		
		likeLimitDatabase = new LikeLimitDatabase();
		bookmarkDatabase = new BookmarkDatabase();
		bookmarkDatabase.load().forEach((name, bookmark) -> bookmarks.put(name, bookmark));
	}
	
	@Override
	public void onDisable(){
		bookmarkDatabase.save();
		likeLimitDatabase.save();
		playerDatabase.save();
		likeDatabase.save();
		
		getServer().getOnlinePlayers().forEach(player -> {
			Maybe.unit(player.getOpenInventory())
			.map(InventoryView::getTopInventory)
			.map(Inventory::getHolder)
			.flatMap(x -> SafeCast.cast(x, Layout.class))
			.apply(x -> player.closeInventory());
		});

		HandlerList.unregisterAll(this);
	}
	
	public Economy economy(){
		return economy;
	}
	
	public MainConfig config(){
		return config;
	}
	
	public LikeDatabase likeDatabase(){
		return likeDatabase;
	}
	
	public PlayerDatabase playerDatabase(){
		return playerDatabase;
	}
	
	public LikeLimitDatabase likeLimitDatabase(){
		return likeLimitDatabase;
	}
	
	public BookmarkDatabase bookmarkDatabase(){
		return bookmarkDatabase;
	}

}
