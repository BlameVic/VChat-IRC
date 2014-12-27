package com.github.blamevic.vchat_irc;

import com.github.blamevic.irc.IRCClient;
import com.github.blamevic.irc.IRCMessageParser;
import org.yaml.snakeyaml.Yaml;
import vic.mod.chat.api.bot.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Version(version = "1.3")
public class IRCChatBot implements IChatBot {
    IRCClient irc;
    IBotHandler handler;

    String channel;

    BlockingQueue<String> ircQueue;
    BlockingQueue<String> minecraftChatQueue;

    @Override
    public void onLoad(IBotHandler handler) {
        this.handler = handler;
        this.ircQueue = new LinkedBlockingQueue<>();
        this.minecraftChatQueue = new LinkedBlockingQueue<>();

        File configFile = new File(handler.getBotDir() + "/irc.yml");

        Map<String, Object> conf;
        try {
            conf = (Map<String, Object>) new Yaml().load(new FileInputStream(configFile));
        } catch (FileNotFoundException e) {
            System.err.println("Could not find file " + configFile.getAbsolutePath());
            throw new RuntimeException("VChat api sux");
        }

        String hostname = (String) conf.get("hostname");
        int port = (Integer) conf.get("port");
        String username = (String) conf.get("username");
        String realname = (String) conf.get("realname");
        boolean debug;
        if (conf.get("debug") != null)
            debug = (Boolean) conf.get("debug");
        else
            debug = false;

        this.channel = (String) conf.get("channel");

        this.irc = new IRCClient(hostname, port, username, realname, debug);

        try {
            irc.connect();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("VChat api sux");
        }
        irc.login();
        irc.waitForCommand("001");
        irc.joinChannel(channel);

        new Thread(() -> {
            while (true) {
                String message = null;
                try {
                    message = ircQueue.take();
                } catch (InterruptedException e) {
                    continue;
                }

                irc.sendMessage(message, this.channel);
            }
        }).start();

        new Thread(() -> {
            while (true) {
                String line = irc.readLine();
                if (line == null) continue;
                if (irc.processPing(line)) continue;

                IRCMessageParser.PrivateMessage privateMessage = IRCMessageParser.parsePrivateMessage(line);
                if (privateMessage == null) continue;
                minecraftChatQueue.add("<" + privateMessage.prefix.name + "> " + privateMessage.message);
                System.out.println("<" + privateMessage.prefix.name + "> " + privateMessage.message);
            }
        }).start();
    }

    @Override
    public void onServerLoad() {

    }

    @Override
    public void onServerUnload() {

    }

    @Override
    public String getName() {
        return "VChat-IRC";
    }

    @Override
    public void onMessage(String message, IChatEntity sender, IChannelBase channel) {
        System.out.println(channel.getName());
        if (!handler.getDefaultChannel().equals(channel)) return;
        if (sender.isBot()) return;
        if (sender.isServer()) return;

        ircQueue.add("<" + sender.getDisplayName() + "> " + message);
    }

    @Override
    public void onPrivateMessage(String message, IChatEntity sender) {

    }

    @Override
    public void onCommandMessage(String command, String[] args, String message) {

    }

    @Override
    public void onTick() {
        if (!minecraftChatQueue.isEmpty())
        {
            handler.sendMessage(handler.getDefaultChannel(), minecraftChatQueue.remove());
        }
    }

    @Override
    public String getDisplayName() {
        return channel;
    }
}
