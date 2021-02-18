package me.liuli.cm;

import java.util.HashMap;
import java.util.Map;

public class Pinyin {
    private static final Map<String,String> pinyinMap=new HashMap<>();

    public static void init(){
        String[] dict=OtherUtil.getTextFromResource("pinyin").split(";");

        int count=0;
        for(String word:dict){
            String[] wordData=word.split(",");
            pinyinMap.put(wordData[0],wordData[1]);
            count++;
        }
        ChatManager.plugin.getLogger().warning("Successful loaded "+count+" chinese pinyin words!");
    }

    public static String toPinyin(String str){
        String[] strSections = str.split("");
        StringBuilder result= new StringBuilder();
        for(String section:strSections){
            String resTmp=pinyinMap.get(section);
            if(resTmp==null){
                result.append(section);
            }else{
                result.append(resTmp);
            }
        }
        return result.toString();
    }
}
