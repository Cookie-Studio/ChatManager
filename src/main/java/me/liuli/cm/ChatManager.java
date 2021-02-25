package me.liuli.cm;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.PluginTask;
import com.alibaba.fastjson.JSONObject;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class ChatManager extends PluginBase {
    public static ChatManager plugin;

    public static int delay;
    public static String dl_warn;
    public static String rp_warn;
    public static ArrayList<String> banWords;
    public static ArrayList<String> oriBanWords;
    public static String bw_warn;
    public static ArrayList<String> ignore;
    public static HashMap<Player, Integer> muteTime = new HashMap<>();

    private static HanyuPinyinOutputFormat hanyuPinyinOutputFormat = new HanyuPinyinOutputFormat();

    public static HanyuPinyinOutputFormat getHanyuPinyinOutputFormat() {
        return hanyuPinyinOutputFormat;
    }

    @Override
    public void onEnable() {
        plugin = this;

        if(!this.getServer().getPluginManager().getPlugins().containsKey("FastJSONLib")){
            //download plugin
            try {
                String pluginPath=this.getServer().getPluginPath();
                OtherUtil.downloadFile("https://github.com/liulihaocai/FJL/releases/download/1.0/FastJSONLib-1.0.jar",
                        pluginPath,"FastJSONLib-1.0.jar");
                //then load it
                this.getServer().getPluginManager()
                        .loadPlugin(new File(pluginPath,"FastJSONLib-1.0.jar").getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.getDataFolder().mkdirs();
        if(!new File(this.getDataFolder().getPath()+"/config.yml").exists()){
            OtherUtil.writeFile(this.getDataFolder().getPath()+"/config.yml",
                    OtherUtil.getTextFromResource("config.yml"));
        }

        JSONObject configJSON=JSONObject.parseObject(OtherUtil.y2j(new File(this.getDataFolder().getPath()+"/config.yml")));

        delay=configJSON.getInteger("delay");
        dl_warn=configJSON.getString("dl_warn");
        rp_warn=configJSON.getString("rp_warn");
        banWords=new ArrayList<>();
        oriBanWords=new ArrayList<>();
        for(Object words:configJSON.getJSONArray("banWords")){
            String word=((String) words).toLowerCase();
            try {
                banWords.add(PinyinHelper.toHanYuPinyinString(word,hanyuPinyinOutputFormat,"",false));
            } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
                badHanyuPinyinOutputFormatCombination.printStackTrace();
            }
            oriBanWords.add(word);
        }
        bw_warn=configJSON.getString("bw_warn");
        ignore=new ArrayList<>();
        for(Object words:configJSON.getJSONArray("ignore")){
            ignore.add(((String) words).toLowerCase());
        }

        this.getServer().getPluginManager().registerEvents(new ChatListener(), this);
        registerCommand();
        registerMuteTask();
    }

    public static boolean isPlayerMuted(Player player){
        return muteTime.containsKey(player);
    }

    public int getMuteTime(Player player){
        if (!isPlayerMuted(player))
            return 0;
        else
            return muteTime.get(player);
    }

    public static HashMap<Player, Integer> getMuteTime() {
        return muteTime;
    }

    public static void mutePlayer(Player player,int time){
        muteTime.put(player,time);
    }

    private static void registerMuteTask(){
        Server.getInstance().getScheduler().scheduleRepeatingTask(new PluginTask(ChatManager.plugin) {

            @Override
            public void onRun(int i) {
                HashMap<Player, Integer> muteTime = getMuteTime();
                muteTime.entrySet().forEach(e -> {
                    getMuteTime().put(e.getKey(), e.getValue() - 1);
                    if (muteTime.get(e.getKey()) == 0)
                        muteTime.remove(e.getKey());
                });
            }
        },1);
    }

    private static void registerCommand(){
        Server.getInstance().getCommandMap().register("",new MuteCommand("mute","mute a player /mute player_name"));
    }
}
