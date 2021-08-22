package com.smallaswater.invsee.handles;

import cn.nukkit.IPlayer;
import cn.nukkit.OfflinePlayer;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import com.smallaswater.invsee.InvSeeMainClass;
import com.smallaswater.invsee.inventorys.OffOnlineInventory;

import java.util.Map;


/**
 * @author SmallasWater
 * Create on 2021/8/21 11:24
 * Package com.smallaswater.invsee.handles
 */
public class InventoryHandle {

    private Player player;

    private IPlayer changePlayer;

    private Map<Integer,Item> masterInventory;

    private boolean close = false;

    private boolean up = false;

    private boolean sync;

    private Item offhandMasterInventory;

    private Inventory operationInventory;

    private Item offhandOperationInventory = new Item(0);

    /**
     * 仅用作判断存在
     * */
    public InventoryHandle(Player player){
        this.player = player;
    }

    public boolean isUp() {
        return up;
    }

    public void setUp(boolean up) {
        this.up = up;
    }

    public Player getPlayer() {
        return player;
    }

    public InventoryHandle(Player player, IPlayer changePlayer){
        this.player = player;
        this.changePlayer = changePlayer;
        this.masterInventory = player.getInventory().getContents();
        this.offhandMasterInventory = player.getOffhandInventory().getItem(0);
        if(changePlayer instanceof OfflinePlayer){
            this.operationInventory = OffOnlineInventory.getPlayerInventoryByOffOnline((OfflinePlayer) changePlayer);
            this.offhandOperationInventory = ((OffOnlineInventory)this.operationInventory).getOffhandInventory();
        }else if((changePlayer instanceof Player)){
            this.operationInventory = ((Player) changePlayer).getInventory();
            this.offhandOperationInventory = ((Player) changePlayer).getOffhandInventory().getItem(0);

        }
        if(operationInventory != null) {
            player.getInventory().setContents(operationInventory.getContents());
            player.getOffhandInventory().setItem(0,offhandOperationInventory);

        }

    }

    public IPlayer getChangePlayer() {
        return changePlayer;
    }

    public boolean isSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }

    public boolean isClose() {
        return close;
    }

    public void setClose(boolean close) {
        this.close = close;
    }

    public void synchronizationInventory(){
        if (this.operationInventory != null) {
            this.operationInventory.setContents(this.player.getInventory().getContents());
        }
        this.setOffhandOperationInventory(player.getOffhandInventory().getItem(0));
    }

    public void onUpdate(){
        if(changePlayer instanceof Player){
            if(changePlayer.isOnline()) {
                operationInventory = ((Player) changePlayer).getInventory();
                offhandOperationInventory = ((Player) changePlayer).getOffhandInventory().getItem(0);
            }else{
                changePlayer = new OfflinePlayer(Server.getInstance(),changePlayer.getUniqueId());
            }
        }
        if(changePlayer instanceof OfflinePlayer){
            //Always true?
            if(operationInventory instanceof PlayerInventory){
                operationInventory = OffOnlineInventory.onLineInventoryToOffLine((PlayerInventory) operationInventory);
            }
            if(operationInventory instanceof OffOnlineInventory) {
                offhandOperationInventory = ((OffOnlineInventory) operationInventory).getOffhandInventory();
            }
        }

        if(operationInventory != null) {
            player.getInventory().setContents(operationInventory.getContents());
            player.getOffhandInventory().setItem(0,offhandOperationInventory);

        }

    }

    public void syncToPlayer(){
        if(changePlayer instanceof Player){
            if(changePlayer.isOnline()) {
                ((Player) changePlayer).getInventory().setContents(operationInventory.getContents());
                ((Player) changePlayer).getOffhandInventory().setItem(0,offhandOperationInventory);
            }else{
                changePlayer = new OfflinePlayer(Server.getInstance(),changePlayer.getUniqueId());
            }
        }
        if(changePlayer instanceof OfflinePlayer){
            if(changePlayer.isOnline()){
                //解决突然在线bug
                changePlayer = changePlayer.getPlayer();

                ((Player) changePlayer).getInventory().setContents(operationInventory.getContents());
                ((Player) changePlayer).getOffhandInventory().setItem(0,offhandOperationInventory);
                return;
            }
            if(!(operationInventory instanceof OffOnlineInventory)) {
                operationInventory = OffOnlineInventory.onLineInventoryToOffLine((PlayerInventory) operationInventory);
            }
            ((OffOnlineInventory) operationInventory).save();

        }

    }

    public boolean update = true;

    public void save(){
        update = false;
        syncToPlayer();
        player.getInventory().setContents(masterInventory);
        player.getOffhandInventory().setItem(0,offhandMasterInventory);
        close = true;
    }

    public Inventory getOperationInventory() {
        return operationInventory;
    }

    public void setOffhandOperationInventory(Item offhandOperationInventory) {
        this.offhandOperationInventory = offhandOperationInventory;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof InventoryHandle){
            return player == ((InventoryHandle) obj).player;
        }
        return false;
    }
}
