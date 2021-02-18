package me.liuli.cm;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.network.protocol.TextPacket;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

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
    public void onPacket(DataPacketReceiveEvent event) throws BadHanyuPinyinOutputFormatCombination {
        Player player=event.getPlayer();
        if(player==null){
            return;
        }
        //some trash chat plugins ignore event is cancelled
        //so I'll cancel the chat packet
        if(event.getPacket() instanceof TextPacket){
            TextPacket packet=(TextPacket) event.getPacket();

            UUID uuid=event.getPlayer().getUniqueId();
            String msg=packet.message.toLowerCase().replaceAll("\\s*", "");

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

            msg= PinyinHelper.toHanYuPinyinString(msg,ChatManager.plugin.getHanyuPinyinOutputFormat(),"",false);
            for(String words:ChatManager.banWords) {
                //使用正则表达式进行判断
                Pattern pattern = Pattern.compile(words);
                if(pattern.matcher(msg).find()){
                    event.setCancelled();
                    String prohWord=ChatManager.oriBanWords.get(ChatManager.banWords.indexOf(words));
                    event.getPlayer().sendMessage(ChatManager.bw_warn.replaceAll("%w%",prohWord));
                    return;
                }
            }
        }
    }
}
