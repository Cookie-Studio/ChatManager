package me.liuli.cm;

import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;

public class MuteCommand extends Command {

    public MuteCommand(String name, String description) {
        super(name, description);
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (strings.length != 1){
            commandSender.sendMessage("args error");
            return true;
        }
        if (!commandSender.isOp())
            return true;
        ChatManager.mutePlayer(Server.getInstance().getPlayer(strings[0]), Integer.parseInt(strings[1]));
        return true;
    }
}
