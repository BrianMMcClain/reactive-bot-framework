package com.github.brianmmcclain.reactivebotframework.commands;

import com.github.brianmmcclain.reactivebotframework.TwitchMessage;

public class Echo extends BotCommand {
    
    private final String COMMAND = "echo";

    @Override
    public String execute(String data, TwitchMessage tMessage) {
        return "@" + tMessage.getSentBy() + " " + data;
    }
}
