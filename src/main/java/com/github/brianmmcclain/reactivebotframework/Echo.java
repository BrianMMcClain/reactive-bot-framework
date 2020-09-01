package com.github.brianmmcclain.reactivebotframework;

public class Echo extends BotCommand {

    public Echo(String command, String data, TwitchMessage tMessage) {
        super(command, data, tMessage);
    }
    
    @Override
    public String execute() {
        return "@" + tMessage.getSentBy() + " " + data;
    }
}
