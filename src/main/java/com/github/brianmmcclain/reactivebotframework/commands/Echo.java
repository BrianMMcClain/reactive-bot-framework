package com.github.brianmmcclain.reactivebotframework.commands;

import com.github.brianmmcclain.reactivebotframework.TwitchMessage;

public class Echo extends BotCommand {

    public Echo() {
        super();
    }
    
    @Override
    public String execute(String command, String data, TwitchMessage tMessage) {
        return "@" + tMessage.getSentBy() + " " + data;
    }
}
