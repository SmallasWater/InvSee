package com.smallaswater.invsee.utils;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.form.element.ElementDropdown;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementLabel;
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
        simple.addButton(new ResponseElementButton("&a查询/编辑玩家背包").onClicked(FormHelper::sendSelectionPlayer));
        player.showFormWindow(simple);
    }

    public static void sendSelectionPlayer(Player player) {
        AdvancedFormWindowCustom custom = new AdvancedFormWindowCustom("InvSee");
        custom.addElement(new ElementLabel("请直接输入名字 或 通过下拉菜单选择玩家")); //0
        custom.addElement(new ElementInput("玩家名称", "玩家名称", "")); //1
        ArrayList<String> players = new ArrayList<>();
        for (Player p : Server.getInstance().getOnlinePlayers().values()) {
            players.add(p.getName());
            if (players.size() > 30) { //玩家过多时下拉菜单体验会很差
                break;
            }
        }
        custom.addElement(new ElementDropdown("请选择玩家(若已输入玩家名称则此项无效！)", players));

        custom.onResponded((formResponseCustom, cp) -> {
            String inputResponse = formResponseCustom.getInputResponse(1);
            if ("".equalsIgnoreCase(inputResponse.trim())) {
                Server.getInstance().dispatchCommand(cp, "inv c " + formResponseCustom.getDropdownResponse(2).getElementContent());
            }else {
                Server.getInstance().dispatchCommand(cp, "inv c " + inputResponse);
            }
        });

        player.showFormWindow(custom);
    }

}
