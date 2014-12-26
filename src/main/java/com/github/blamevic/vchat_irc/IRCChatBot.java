package com.github.blamevic.vchat_irc;

import com.github.blamevic.irc.IRCClient;
import org.yaml.snakeyaml.Yaml;
import vic.mod.chat.api.bot.IBotHandler;
import vic.mod.chat.api.bot.IChannelBase;
import vic.mod.chat.api.bot.IChatBot;
import vic.mod.chat.api.bot.IChatEntity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Objects;

public class IRCChatBot implements IChatBot {
    IRCClient irc;
    IBotHandler handler;

    @Override
    public void onLoad(IBotHandler handler) {
        this.handler = handler;

        File configFile = new File(handler.getBotDir() + "/irc.yml");

        Map<String, Object> conf;
        try {
            conf = (Map<String, Object>) new Yaml().load(new FileInputStream(configFile));
        } catch (FileNotFoundException e) {
            System.err.println("Could not find file" + configFile.getPath());
            throw new RuntimeException("VChat api sux");
        }

        String hostname = (String) conf.get("hostname");
        int port = (int) conf.get("port");
        String username = (String) conf.get("username");
        String realname = (String) conf.get("realname");
        boolean debug;
        if (conf.get("debug") != null)
            debug = (boolean) conf.get("debug");
        else
            debug = false;

        this.irc = new IRCClient(hostname, port, username, realname, debug);
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

    }

    @Override
    public void onPrivateMessage(String message, IChatEntity sender) {

    }

    @Override
    public void onCommandMessage(String command, String[] args, String message) {

    }
}
