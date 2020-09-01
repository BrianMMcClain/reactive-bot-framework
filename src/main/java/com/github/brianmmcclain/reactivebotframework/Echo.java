package com.github.brianmmcclain.reactivebotframework;

public class Echo extends BotCommand {

    public Echo() {
        super();
    }
    
    @Override
    public String execute(String command, String data, TwitchMessage tMessage) {
        return "@" + tMessage.getSentBy() + " " + data;
    }
}
