package com.smallaswater.invsee.command;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;
import com.smallaswater.invsee.InvSeeMainClass;
import com.smallaswater.invsee.handles.InventoryHandle;

/**
 * @author SmallasWater
 * Create on 2021/8/21 12:56
 * Package com.smallaswater.invsee.command
 */
public class InvSeeCommand extends Command {
    public InvSeeCommand() {
        super("inv", "查询编辑背包","/inv help");
        this.setPermission("inv");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if(sender instanceof Player) {
            if (args.length > 0) {
                switch (args[0]) {
                    case "help":
                    case "?":
                        sender.sendMessage(TextFormat.colorize('&',"&b-------->>-------"));
                        sender.sendMessage(TextFormat.colorize('&',"&e/inv help &a查看帮助"));
                        sender.sendMessage(TextFormat.colorize('&',"&e/inv c <玩家> &a查询/编辑玩家背包"));
                        sender.sendMessage(TextFormat.colorize('&',"&e/inv au &a开启/关闭 实时更新 &c(更新时无法编辑 可用作实时观察玩家背包)"));
                        sender.sendMessage(TextFormat.colorize('&',"&e/inv cl &a清空 &c(若开启实时更新则无效果)"));
                        sender.sendMessage(TextFormat.colorize('&',"&e/inv a &a刷新玩家背包"));
                        sender.sendMessage(TextFormat.colorize('&',"&e/inv p &a将背包同步给玩家"));
                        sender.sendMessage(TextFormat.colorize('&',"&e/inv sync &a将编辑后的背包实时同步到玩家 &c(玩家会根据此特性进行刷物品) (最好不要将同步更新与这个同时打开，不确定会出现什么情况)"));
                        sender.sendMessage(TextFormat.colorize('&',"&e/inv save &a保存并退出"));
                        sender.sendMessage(TextFormat.colorize('&',"&e/inv quit &a放弃编辑退出"));
                        sender.sendMessage(TextFormat.colorize('&',"&b-------->>-------"));
                        return true;
                    case "c":
                        if(args.length > 1){
                            String name = args[1];
                            if(InvSeeMainClass.getHandle((Player)sender) != null){
                                sender.sendMessage(TextFormat.colorize('&',"&c请先退出当前玩家背包 再进入编辑背包模式"));
                                return true;
                            }
                            if(InvSeeMainClass.addHandle((Player) sender,name)){
                                sender.sendMessage(TextFormat.colorize('&',"&a你已进入查询 &e"+name+" &a背包模式"));
                            }else{
                                sender.sendMessage(TextFormat.colorize('&',"&c进入查询 &e"+name+" &a背包失败 未查找到玩家的背包数据"));
                            }
                        }else{
                            return false;
                        }
                        return true;

                    default:
                        break;
                }
                return cmdChange(sender, args);
            } else {
                return false;
            }
        }else{
            sender.sendMessage("请不要在控制台执行");
        }
        return true;
    }

    private boolean cmdChange(CommandSender sender,String[] args){
        InventoryHandle handle = InvSeeMainClass.getHandle((Player) sender);
        if (handle == null) {
            sender.sendMessage(TextFormat.colorize('&', "&c未查询任何玩家背包"));
            return true;
        }
        switch (args[0]) {
            case "sync":
                if(handle.getChangePlayer().isOnline()) {
                    if (handle.isUp()) {
                        sender.sendMessage(TextFormat.colorize('&', "&c你已开启 背包更新 请不要在开启同步"));
                        break;
                    }
                    handle.setSync(!handle.isSync());
                    sender.sendMessage(TextFormat.colorize('&', (handle.isSync() ? "&a开启" : "&c关闭") + "自动同步背包"));
                }else{
                    sender.sendMessage(TextFormat.colorize('&', "&c离线背包无法开启同步"));
                }
                break;
            case "quit":
                handle.setClose(true);
                sender.sendMessage(TextFormat.colorize('&',"&a已放弃编辑并退出"));
                break;
            case "au":
                if(handle.getChangePlayer().isOnline()) {
                    if (handle.isSync()) {
                        sender.sendMessage(TextFormat.colorize('&', "&c你已开启 背包同步 请不要在开启更新"));
                        break;
                    }
                    handle.setUp(!handle.isUp());
                    sender.sendMessage(TextFormat.colorize('&', (handle.isUp() ? "&a开启" : "&c关闭") + "自动刷新背包"));
                }else{
                    sender.sendMessage(TextFormat.colorize('&', "&c离线背包无法实时更新"));
                }
                break;
            case "p":
                handle.syncToPlayer();
                sender.sendMessage(TextFormat.colorize('&', "&a背包同步完成"));
                break;
            case "a":
                handle.onUpdate();
                sender.sendMessage(TextFormat.colorize('&', "&a背包刷新完成"));
                break;
            case "cl":
                handle.setOffhandOperationInventory(new Item(0));
                handle.getOperationInventory().clearAll();
                sender.sendMessage(TextFormat.colorize('&', "&a玩家背包已清空"));
                break;
            case "save":
                handle.save();
                sender.sendMessage(TextFormat.colorize('&', "&a当前编辑已保存"));

                break;
            default:break;
        }
        return false;
    }
}
