package com.smallaswater.invsee.utils;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.form.element.ElementDropdown;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.response.FormResponseData;
import cn.nukkit.utils.TextFormat;
import com.smallaswater.invsee.handles.InventoryHandle;
import com.smallaswater.invsee.utils.form.element.ResponseElementButton;
import com.smallaswater.invsee.utils.form.windows.AdvancedFormWindowCustom;
import com.smallaswater.invsee.utils.form.windows.AdvancedFormWindowSimple;

import java.util.ArrayList;

/**
 * @author LT_Name
 */
public class FormHelper {

    public static void sendMain(Player player) {
        AdvancedFormWindowSimple simple = new AdvancedFormWindowSimple("InvSee");
        simple.addButton(new ResponseElementButton("查询/编辑玩家背包").onClicked(FormHelper::sendSelectionPlayer));
        player.showFormWindow(simple);
    }

    public static void sendMain(Player player, InventoryHandle handle) {
        AdvancedFormWindowSimple simple = new AdvancedFormWindowSimple("InvSee");

        if (!handle.isUp()) {
            simple.addButton(new ResponseElementButton((handle.isSync() ? "关闭" : "开启") + "实时背包同步\n(将编辑后的背包实时同步到玩家)")
                    .onClicked(cp -> Server.getInstance().dispatchCommand(cp, "inv sync")));
        }
        if (!handle.isSync()) {
            simple.addButton(new ResponseElementButton((handle.isUp() ? "关闭" : "开启") + "实时更新\n(更新时无法编辑 可用作实时观察玩家背包)")
                    .onClicked(cp -> Server.getInstance().dispatchCommand(cp, "inv au")));
        }
        simple.addButton(new ResponseElementButton("清空背包")
                .onClicked(cp -> Server.getInstance().dispatchCommand(cp, "inv cl")));
        simple.addButton(new ResponseElementButton("刷新玩家背包")
                .onClicked(cp -> Server.getInstance().dispatchCommand(cp, "inv a")));
        simple.addButton(new ResponseElementButton("将背包同步给玩家")
                .onClicked(cp -> Server.getInstance().dispatchCommand(cp, "inv p")));
        simple.addButton(new ResponseElementButton("保存并退出")
                .onClicked(cp -> Server.getInstance().dispatchCommand(cp, "inv save")));
        simple.addButton(new ResponseElementButton("放弃编辑并退出")
                .onClicked( cp -> Server.getInstance().dispatchCommand(cp, "inv quit")));

        player.showFormWindow(simple);
    }

    public static void sendSelectionPlayer(Player player) {
        AdvancedFormWindowCustom custom = new AdvancedFormWindowCustom("InvSee");
        custom.addElement(new ElementLabel("请直接输入名字 或 通过下拉菜单选择玩家")); //0
        custom.addElement(new ElementInput("玩家名称", "玩家名称", "")); //1
        ArrayList<String> players = new ArrayList<>();
        for (Player p : Server.getInstance().getOnlinePlayers().values()) {
            if (p == player) { //跳过自己
                continue;
            }
            players.add(p.getName());
            if (players.size() > 30) { //玩家过多时下拉菜单体验会很差
                break;
            }
        }
        if (!players.isEmpty()) {
            custom.addElement(new ElementDropdown("请选择玩家(若已输入玩家名称则此项无效！)", players)); //2
        }

        custom.onResponded((formResponseCustom, cp) -> {
            String inputResponse = formResponseCustom.getInputResponse(1);
            if ("".equalsIgnoreCase(inputResponse.trim())) {
                FormResponseData dropdownResponse = formResponseCustom.getDropdownResponse(2);
                if (dropdownResponse != null) {
                    Server.getInstance().dispatchCommand(cp, "inv c " + dropdownResponse.getElementContent());
                }else {
                    cp.sendMessage("§c请输入目标玩家名称！");
                }
            }else {
                Server.getInstance().dispatchCommand(cp, "inv c " + inputResponse);
            }
        });

        player.showFormWindow(custom);
    }

}
