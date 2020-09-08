package com.github.brianmmcclain.reactivebotframework.commands;

import com.github.brianmmcclain.reactivebotframework.TwitchMessage;

public class Code extends BotCommand {
    private final String code;

    public Code(String code) {
        super();
        this.code = code;
    }

    @Override
    public String execute(String data, TwitchMessage tMessage) {
        return "@" + tMessage.getSentBy() + " " + code;
    }
}
