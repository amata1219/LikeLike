package amata1219.like;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.milkbowl.vault.economy.Economy;

public class LikeInvs {

	private List<Inventory> mines = new LinkedList<>();
	private List<Inventory> likes = new LinkedList<>();
	private List<Long> mineList = new ArrayList<>();
	private List<Long> likeList = new ArrayList<>();
	private int mineLen;
	private int likeLen;

	public LikeInvs(UUID uuid){
		mines.add(newPage(0, true));
		likes.add(newPage(0, false));

		for(OldLike like : Util.MyLikes.get(uuid).values())
			registerLike(like);

		if(!Util.Mines.containsKey(uuid))
			return;

		for(OldLike like : Util.Mines.get(uuid))
			addMine(like);
	}

	public boolean hasMine(OldLike like){
		return mineList.contains(like.getId());
	}

	public void addMine(OldLike like){
		if(isFull(mineLen))
			mines.add(newPage(mines.size() - 1, true));
		mines.get(mines.size() - 1).addItem(newIcon(like, true));
		mineList.add(like.getId());
		mineLen++;
	}

	public void removeMine(OldLike like){
		String id = like.getStringId();
		for(Inventory inventory : mines){
			for(ItemStack item : inventory.getContents()){
				if(item == null || item.getType() == Material.AIR)
					continue;

				if(!item.hasItemMeta())
					continue;

				if(!id.equals(item.getItemMeta().getDisplayName()))
					continue;

				inventory.remove(item);
				mineList.remove((Object) like.getId());
				mineLen--;
				break;
			}
		}
	}

	public void moveMine(OldLike like){
		removeMine(like);
		addMine(like);
	}

	public Inventory firstMine(){
		return mines.get(0);
	}

	public boolean hasBeforeMine(int page){
		return page > 0;
	}

	public Inventory getBeforeMine(int page){
		if(!hasBeforeMine(page))
			return mines.get(page);

		return mines.get(page - 1);
	}

	public boolean hasNextMine(int page){
		return page < mines.size() - 1;
	}

	public Inventory getNextMine(int page){
		if(!hasNextMine(page))
			return mines.get(page);

		return mines.get(page + 1);
	}

	public boolean hasLike(OldLike like){
		return likeList.contains(like.getId());
	}

	public void addLike(OldLike like){
		if(isFull(likeLen))
			likes.add(newPage(likes.size() - 1, false));

		likes.get(likes.size() - 1).addItem(newIcon(like, false));
		likeList.add(like.getId());
		likeLen++;
	}

	public void removeLike(OldLike like){
		String id = like.getStringId();
		for(Inventory inventory : likes){
			for(ItemStack item : inventory.getContents()){
				if(item == null || item.getType() == Material.AIR)
					continue;

				if(!item.hasItemMeta())
					continue;

				if(!id.equals(item.getItemMeta().getDisplayName()))
					continue;

				inventory.remove(item);
				likeList.remove((Object) like.getId());
				likeLen--;
				break;
			}
		}
	}

	public void moveLike(OldLike like){
		removeLike(like);
		registerLike(like);
	}

	public Inventory firstLike(){
		return likes.get(0);
	}

	public boolean hasBeforeLike(int page){
		return page > 0;
	}

	public Inventory getBeforeLike(int page){
		if(!hasBeforeLike(page))
			return likes.get(page);

		return likes.get(page - 1);
	}

	public boolean hasNextLike(int page){
		return page < likes.size() - 1;
	}

	public Inventory getNextLike(int page){
		if(!hasNextLike(page))
			return likes.get(page);

		return likes.get(page + 1);
	}

	private boolean isFull(int len){
		return len < 52 ? false : len % 52 == 0;
	}

	private Inventory newPage(int page, boolean isMine){
		Inventory inventory = Util.createInventory(54, (isMine ? "Mine" : "MyLike") + "@" + String.valueOf(page));
		inventory.setItem(45, Util.newItem(Util.PageButton, ChatColor.GREEN + "前のページへ"));
		inventory.setItem(53, Util.newItem(Util.PageButton, ChatColor.GREEN + "次のページへ"));
		return inventory;
	}

	private ItemStack newIcon(OldLike like, boolean isMine){
		ItemStack item = Util.newItem(Util.LikeIcon, ChatColor.WHITE + like.getStringId());
		ItemMeta meta = item.getItemMeta();
		List<String> lore = new ArrayList<>();
		lore.add(ChatColor.WHITE + like.getLore());
		lore.add("§7----------------------------------------");//x40
		if(!isMine)
			lore.add("§a作成者:§f " + Util.getName(like.getOwner()));
		lore.add("§aお気に入り数:§f " + like.getLikeCount());
		lore.add("§a作成日:§f " + like.getCreationTimestamp());
		lore.add("§aワールド:§f " + Util.Worlds.get(like.getWorld().getName()));
		lore.add("§a座標:§f X: " + like.getX() + " Y: " + like.getY() + " Z: " + like.getZ());
		lore.add("");
		Economy economy = OldMain.getEconomy();
		lore.add("§a左クリック:§f LikeにTP(コスト: " + economy.format(Util.Tp) + ")");
		lore.add("§a右クリック:§f 半径" + Util.Range + "マス以内にいるプレイヤーに");
		lore.add("             §f招待ボタンを送信(コスト: " + economy.format(Util.Invite) + ")");
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	public static OldLike toLike(ItemStack item){
		if(item == null || item.getType() == Material.AIR || !item.hasItemMeta())
			return null;

		ItemMeta meta = item.getItemMeta();
		if(!meta.hasDisplayName() || !meta.hasLore())
			return null;

		return Util.Likes.get(Long.parseLong(meta.getDisplayName()));
	}

}
