package com.github.brianmmcclain.reactivebotframework;

import com.github.brianmmcclain.reactivebotframework.commands.*;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReactiveBotFrameworkApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactiveBotFrameworkApplication.class, args);

		String oauth = System.getenv("TWITCH_OAUTH");
		String nick = System.getenv("TWITCH_NICK");
		String channel = System.getenv("TWITCH_CHANNEL");
		String code = System.getenv("CODE_REPO_LINK");

		TwitchBot bot = new TwitchBot();
		bot.authorize(oauth, nick);
		bot.joinChannel(channel);

		// Register commands
		bot.registerCommand("echo", new Echo());
		bot.registerCommand("counter", new Counter());
		bot.registerCommand("dice", new Dice());
		bot.registerCommand("code", new Code(code));
	}
}
