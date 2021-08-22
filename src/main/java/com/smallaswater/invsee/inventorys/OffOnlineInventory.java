package com.smallaswater.invsee.inventorys;

import cn.nukkit.OfflinePlayer;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import com.smallaswater.invsee.InvSeeMainClass;

import java.io.File;
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * @author SmallasWater
 * Create on 2021/8/21 11:56
 * Package com.smallaswater.invsee.inventorys
 */
public class OffOnlineInventory extends PlayerInventory {
    private static final Pattern P = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}.dat$");
    private final OfflinePlayer player;
    private CompoundTag namedTag;
    private Item offhandInventory = new Item(0);


    private OffOnlineInventory(OfflinePlayer player) {
        super(null);
        this.player = player;
    }

    private OffOnlineInventory(CompoundTag tag,OfflinePlayer player) {
        super(null);
        this.player = player;
        this.namedTag = tag;
    }

    public void setOffhandInventory(Item offhandInventory) {
        this.offhandInventory = offhandInventory;
    }

    private OfflinePlayer getPlayer() {
        return this.player;
    }

    @Override
    public boolean setItem(int index, Item item) {
        if (index >= 0 && index < this.size) {
            if (item.getId() != 0 && item.getCount() > 0) {
                this.slots.put(index, item.clone());
                return true;
            } else {
                return this.clear(index);
            }
        } else {
            return false;
        }
    }
    //逆向

    public void save() {
        ListTag<CompoundTag> inventoryTag = new ListTag<>("Inventory");
        for (int slot = 0; slot < 9; ++slot) {
            inventoryTag.add(new CompoundTag()
                    .putByte("Count", 0)
                    .putShort("Damage", 0)
                    .putByte("Slot", slot)
                    .putByte("TrueSlot", -1)
                    .putShort("id", 0)
            );
        }

        int slotCount = Player.SURVIVAL_SLOTS + 9;
        for (int slot = 9; slot < slotCount; ++slot) {
            Item item = this.getItem(slot - 9);
            inventoryTag.add(NBTIO.putItemHelper(item, slot));
        }

        for (int slot = 100; slot < 104; ++slot) {
            Item item = this.getItem(this.getSize() + slot - 100);
            if (item != null && item.getId() != Item.AIR) {
                inventoryTag.add(NBTIO.putItemHelper(item, slot));
            }
        }

        if (this.offhandInventory != null) {
            Item item = this.offhandInventory;
            if (item.getId() != Item.AIR) {
                inventoryTag.add(NBTIO.putItemHelper(item, -106));
            }
        }
        namedTag.putList(inventoryTag);

        Server.getInstance().saveOfflinePlayerData(player.getUniqueId(),namedTag,false);

    }

    public Item getOffhandInventory() {
        return this.offhandInventory;
    }

    public static OffOnlineInventory getPlayerInventoryByOffOnline(OfflinePlayer player) {
        OffOnlineInventory inventory = new OffOnlineInventory(player);
        CompoundTag namedTag;
        try {
            Class<?> playerClass = Class.forName("cn.nukkit.OfflinePlayer");
            Field o = playerClass.getDeclaredField("namedTag");
            o.setAccessible(true);
            namedTag = (CompoundTag) o.get(player);
        } catch (Exception var8) {
            InvSeeMainClass.getInstance().getLogger().error("获取离线玩家数据时发生错误：", var8);
            return inventory;
        }
        inventory = new OffOnlineInventory(Server.getInstance().getOfflinePlayerData(player.getUniqueId()),player);

        if (namedTag.contains("Inventory") && namedTag.get("Inventory") instanceof ListTag) {
            ListTag<CompoundTag> inventoryList = namedTag.getList("Inventory", CompoundTag.class);
            for(CompoundTag item:inventoryList.getAll()){
                int slot = item.getByte("Slot");
                if (slot >= 100 && slot < 104) {
                    inventory.setItem(inventory.getSize() + slot - 100, NBTIO.getItemHelper(item));
                } else {
                    inventory.setItem(slot - 9, NBTIO.getItemHelper(item));
                }
                if(slot == -106){
                    inventory.setOffhandInventory(NBTIO.getItemHelper(item));
                }
            }
        } else {
            return inventory;
        }
        return inventory;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof OffOnlineInventory && ((OffOnlineInventory) obj).getPlayer().equals(this.player);
    }

    public static OfflinePlayer getOffOnlinePlayerByName(String name) {
        Player player = Server.getInstance().getPlayer(name);
        if (player != null) {
            return (OfflinePlayer) Server.getInstance().getOfflinePlayer(player.getUniqueId());
        } else {
            File dataDirectory = new File(Server.getInstance().getDataPath(), "players/");
            File[] files = dataDirectory.listFiles((filex) -> {
                String names = filex.getName();
                return P.matcher(names).matches() && names.endsWith(".dat");
            });
            if (files != null) {
                for (File file : files) {
                    String uu = file.getName();
                    uu = uu.substring(0, uu.length() - 4);
                    UUID uuid = UUID.fromString(uu);
                    if (Server.getInstance().getOfflinePlayer(uuid).getName().equals(name)) {
                        return (OfflinePlayer) Server.getInstance().getOfflinePlayer(uuid);
                    }
                }
            }

            return null;
        }
    }

    public static OffOnlineInventory onLineInventoryToOffLine(PlayerInventory inventory){
         OffOnlineInventory inventory1 = new OffOnlineInventory(inventory.getHolder().namedTag,new OfflinePlayer(Server.getInstance(),inventory.getHolder().getUniqueId()));
         inventory1.setOffhandInventory(inventory.getHolder().getOffhandInventory().slots.get(0));
         inventory1.setContents(inventory.getContents());
         return inventory1;
    }
}
