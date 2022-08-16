package net.flex.FlexTournaments.api;

import org.bukkit.command.CommandSender;

import java.util.Arrays;

public abstract class Command extends org.bukkit.command.Command {
    private final String name;
    private final String usage;
    private final String desc;
    private final String permission;

    public Command(String name, String desc, String usage, String permission, String... aliases) {
        super(name, desc, usage, Arrays.asList(aliases));
        this.name = name;
        this.usage = usage;
        this.desc = desc;
        this.permission = permission;
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission(this.permission)) {
            String msg = "&cBrak uprawnien! &7(&6{PERM}&7)";
            msg = msg.replace("{PERM}", this.getPermission());
            return ChatUtil.sendMessage(sender, msg);
        } else {
            return this.onExecute(sender, args);
        }
    }

    public abstract boolean onExecute(CommandSender var1, String[] var2);

    public String getName() {
        return this.name;
    }

    public String getUsage() {
        return this.usage;
    }

    public String getDesc() {
        return this.desc;
    }

    public String getPermission() {
        return this.permission;
    }
}

