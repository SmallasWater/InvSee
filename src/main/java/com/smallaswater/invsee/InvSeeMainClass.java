package com.smallaswater.invsee;

import cn.nukkit.OfflinePlayer;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.Listener;
import cn.nukkit.plugin.PluginBase;
import com.smallaswater.invsee.command.InvSeeCommand;
import com.smallaswater.invsee.handles.InventoryHandle;
import com.smallaswater.invsee.inventorys.OffOnlineInventory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author SmallasWater
 * Create on 2021/8/21 11:23
 * Package com.smallaswater.invsee
 */
public class InvSeeMainClass extends PluginBase implements Listener {

    private static InvSeeMainClass invSeeMainClass;

    private ScheduledExecutorService service = Executors.newScheduledThreadPool(10);

    private static ArrayList<InventoryHandle> handles = new ArrayList<>();

    public static InvSeeMainClass getInstance() {
        return invSeeMainClass;
    }

    @Override
    public void onEnable() {
        invSeeMainClass = this;

        service.execute(()->{
            while (true) {
               if (handles.size() > 0) {
                   Iterator<InventoryHandle> handleIterator = handles.iterator();
                   InventoryHandle handle;
                   while (handleIterator.hasNext()) {
                       handle = handleIterator.next();
                       if (!handle.getPlayer().isOnline()) {
                           handle.setClose(true);
                       }
                       if (handle.isClose()) {
                           handleIterator.remove();
                           continue;
                       }
                       if (handle.isUp() && handle.update && !handle.isSync()) {
                           handle.onUpdate();
                       }
                       if (handle.isSync() && !handle.isUp() && handle.update) {
                           handle.syncToPlayer();
                       }
                       if(handle.update) {
                           handle.synchronizationInventory();
                       }
                   }
               }
               try {
                   Thread.sleep(500);
               }catch (Exception e){
                   break;
               }
           }
        });

        this.getServer().getPluginManager().registerEvents(this,this);

        this.getServer().getCommandMap().register("inv", new InvSeeCommand());

        this.getLogger().info("InvSee 插件加载成功~");
    }

    public static InventoryHandle getHandle(Player player){
        InventoryHandle handle = new InventoryHandle(player);
        if(handles.contains(handle)){
            return handles.get(handles.indexOf(handle));
        }
        return null;
    }


    public static InventoryHandle getHandleByTarget(String target){
        for(InventoryHandle handle: handles){
            if(handle.getChangePlayer().getName().equalsIgnoreCase(target)){
                return handle;
            }
        }
        return null;
    }

    public static boolean addHandle(Player player, String target){
        Player t = Server.getInstance().getPlayer(target);
        if(t != null){
            handles.add(new InventoryHandle(player,t));
            return true;
        }else{
            OfflinePlayer p = OffOnlineInventory.getOffOnlinePlayerByName(target);
            if(p != null){
                handles.add(new InventoryHandle(player,p));
                return true;
            }
        }
        return false;
    }




}
