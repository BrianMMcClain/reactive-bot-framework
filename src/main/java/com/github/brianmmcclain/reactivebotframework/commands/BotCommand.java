package com.github.brianmmcclain.reactivebotframework.commands;

import com.github.brianmmcclain.reactivebotframework.TwitchMessage;

public abstract class BotCommand {

    public BotCommand() {
    }

    public String execute(String command, String data, TwitchMessage tMessage) {
        return "Method Not Implimented";
    }
}
