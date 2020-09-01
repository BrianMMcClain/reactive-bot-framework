package com.github.brianmmcclain.reactivebotframework;

import reactor.core.scheduler.Schedulers;

public class TwitchBot {
    
    private IRCConnection connection;
    private boolean isAuthenticated = false;
    private String channel;

    public TwitchBot() {
        this.connection = new IRCConnection("irc.chat.twitch.tv", 6667);
        this.connection.connect();
    }

    public TwitchBot(String host, int port) {
        this.connection = new IRCConnection(host, port);
        this.connection.connect();
    }

    public boolean isConnected() {
        return this.connection.isConnected();
    }

    public boolean isAuthenticated() { 
        return this.isAuthenticated;
    }

    public void authorize(String oauth, String nick) {
        this.connection.send("PASS " + oauth);
        this.connection.send("NICK " + nick);
        this.connection.getInputStream()
        .subscribeOn(Schedulers.parallel())
        .subscribe( message -> {
            processMessage(message);
        });
    }

    public void joinChannel(String channel) {
        this.connection.send("JOIN #" + channel);
        this.channel = channel;
    }

    public void sendMessage(String message) {
        System.out.println("SENDING: \"" + message + "\"");
        this.connection.send("PRIVMSG #" + this.channel + " :" + message);
    }

    //TODO: This sucks, there's probably a better way to do this
    public void waitForAuthentication(int waitSeconds) {
        int count = 0;
        while (count < waitSeconds) {
            if (this.isAuthenticated) {
                return;
            } else {
                count++;
                try {
                    Thread.sleep(1000);
                } catch (IllegalArgumentException e) {

                } catch (InterruptedException e) {

                }

            }
        }
    }

    private void processMessage(String message) {
        if (message.contains("Welcome, GLHF!")) {
            this.isAuthenticated = true;
            
        } else if (message.startsWith("PING")) {
            System.out.print("Responding to PING: . . . ");
            sendMessage(message.replace("PING", "PONG"));
            System.out.println("done!");
        } else {
            TwitchMessage tMessage = new TwitchMessage(message);
            System.out.println(tMessage.getSentBy() + ": " + tMessage.getMessage());
        }
    }
}