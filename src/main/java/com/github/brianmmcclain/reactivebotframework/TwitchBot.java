package com.github.brianmmcclain.reactivebotframework;

import java.util.HashMap;
import java.util.Map;

import com.github.brianmmcclain.reactivebotframework.commands.*;

import org.springframework.beans.factory.annotation.Autowired;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import reactor.core.scheduler.Schedulers;

public class TwitchBot {

    private IRCConnection connection;
    private boolean isAuthenticated = false;
    private String channel;

    private Map<String, BotCommand> commandRegistry;
    
    private Map<String, Counter> commandMetrics;
    private Counter totalBotCommands_counter;

    private MeterRegistry registry;

    /**
     * Connect using the default hostname and port
     */
    public TwitchBot() {
        this.connection = new IRCConnection("irc.chat.twitch.tv", 6667);
        this.connection.connect();

        this.registry = new SimpleMeterRegistry();

        this.commandRegistry = new HashMap<String, BotCommand>();
        this.commandMetrics = new HashMap<String, Counter>();

        // Enable metrics for schedulers
        Schedulers.enableMetrics();

        // Build the counter to gather metreics
        this.totalBotCommands_counter = Counter.builder("botcommand_total_counter")
            .description("Total invocations of all bot commands")
            .register(Metrics.globalRegistry);
    }

    /**
     * Registers a new command for the bot to respond to
     * 
     * @param command Message prefix to scan for (ie. !command)
     * @param botCommand Instance of a BotCommand object that handles the logic of the command
     */
    public void registerCommand(String command, BotCommand botCommand) {
        this.commandRegistry.put(command, botCommand);
        
        // Build the counter to gather metreics
        Counter counter = Counter.builder("botcommand_" + command + "_counter")
            .description("Invocation of the " + command + " command")
            .register(Metrics.globalRegistry);
        this.commandMetrics.put(command, counter);
    }

    /**
     * @return True of the underlying IRC connection is active. Otherwise, returns false.
     */
    public boolean isConnected() {
        return this.connection.isConnected();
    }

    /**
     * 
     * @return True if the underlying IRC connection is properly authenticated with the server. Otherwise, returns false
     */
    public boolean isAuthenticated() {
        return this.isAuthenticated;
    }

    /**
     * Authorize against the configured IRC server
     * 
     * @param oauth OAuth token for the account to authoerize with
     * @param nick Username to authenticate with
     */
    public void authorize(String oauth, String nick) {
        this.connection.send("PASS " + oauth);
        this.connection.send("NICK " + nick);

        // Subscribe to the Flux stream and process the messages as they come in
        this.connection.getInputStream().metrics().subscribeOn(Schedulers.parallel()).subscribe(message -> {
            processMessage(message);
        });

        this.waitForAuthentication(10);
    }

    /**
     * 
     * @param channel IRC/Twitch channel to join
     */
    public void joinChannel(String channel) {
        this.connection.send("JOIN #" + channel);
        this.channel = channel;
        System.out.println("Joined to #" + this.channel);
    }

    /**
     * Send a message to the channel the bot is currently joined to
     * 
     * @param message Message to send
     */
    public void sendMessage(String message) {
        //System.out.println("SENDING: \"" + message + "\"");
        this.connection.send("PRIVMSG #" + this.channel + " :" + message);
    }

    /**
     * Waits until either the underlying IRC connection is authenticated or the timeout is reached
     * 
     * @param waitSeconds How long to wait before giving up, in seconds
     */
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

    /**
     * Basic message processing. Automatically handles Twitch API specific messages,
     * then attempts to see if any message is invoking a registered command
     * 
     * @param message The message to process
     */
    private void processMessage(String message) {
        if (message.contains("Welcome, GLHF!")) {
            this.isAuthenticated = true;
            System.out.println("AUTHENTICATED TO SERVER");
        } else if (message.startsWith("PING")) {
            System.out.print("Responding to PING: . . . ");
            this.connection.send(message.replace("PING", "PONG"));
            System.out.println("done!");
        } else {
            TwitchMessage tMessage = new TwitchMessage(message);
            System.out.println(tMessage.getSentBy() + ": " + tMessage.getMessage());
            if (tMessage.getMessage().startsWith("!")) {
                processCommand(tMessage);
            }
        }
    }

    /**
     * Invoked with a command message has been detected, look it up
     * in the registered commands to ensure it's valid, and then
     * execute it
     * 
     * @param tMessage The TwitchMessage obeject containing the command
     */
    private void processCommand(TwitchMessage tMessage) {
        String command = tMessage.getMessage().split(" ")[0].replace("!", "");
        String data = tMessage.getMessage().replace("!" + command, "").trim();

        if (this.commandRegistry.keySet().contains(command)) {
            BotCommand botCommand = this.commandRegistry.get(command);
            String retMessage = botCommand.execute(data, tMessage);

            // Increment the counters
            this.commandMetrics.get(command).increment();
            this.totalBotCommands_counter.increment();

            this.sendMessage(retMessage);
        }

        // TODO: Command not found
    }
}