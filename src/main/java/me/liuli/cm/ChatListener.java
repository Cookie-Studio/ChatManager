package me.liuli.cm;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatListener implements Listener {
    private final Map<UUID,Long> chatTime=new HashMap<>();
    private final Map<UUID,String> chatMsg=new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event){
        chatTime.put(event.getPlayer().getUniqueId(),System.currentTimeMillis());
        chatMsg.put(event.getPlayer().getUniqueId(),"");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event){
        chatTime.remove(event.getPlayer().getUniqueId());
        chatMsg.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(PlayerChatEvent event){
        UUID uuid=event.getPlayer().getUniqueId();
        String msg=event.getMessage().toLowerCase().replaceAll("\\s*", "");

        for(String words:ChatManager.ignore){
            msg=msg.replaceAll(words,"");
        }

        if((System.currentTimeMillis()-chatTime.get(uuid))<ChatManager.delay){
            event.setCancelled();
            event.getPlayer().sendMessage(ChatManager.dl_warn);
            return;
        }
        chatTime.put(uuid,System.currentTimeMillis());

        if(chatMsg.get(uuid).equals(msg)){
            event.setCancelled();
            event.getPlayer().sendMessage(ChatManager.rp_warn);
            return;
        }
        chatMsg.put(uuid,msg);

        msg=Pinyin.toPinyin(msg);
        for(String words:ChatManager.banWords) {
            if(msg.contains(words)){
                event.setCancelled();
                String prohWord=ChatManager.oriBanWords.get(ChatManager.banWords.indexOf(words));
                event.getPlayer().sendMessage(ChatManager.bw_warn.replaceAll("%w%",prohWord));
                return;
            }
        }
    }
}
