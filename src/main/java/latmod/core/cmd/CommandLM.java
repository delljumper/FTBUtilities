package latmod.core.cmd;

import java.util.*;

import latmod.core.*;
import latmod.core.util.LatCore;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumChatFormatting;

public abstract class CommandLM extends CommandBase
{
	protected static final String FINE = EnumChatFormatting.WHITE + "";
	
	public final String commandName;
	
	public CommandLM(String s)
	{ commandName = s; }
	
	public final String getCommandName()
	{ return commandName; }
	
	public String getCommandUsage(ICommandSender ics)
	{ return "/" + commandName; }
	
	public final void processCommand(ICommandSender ics, String[] args)
	{
		if(args == null) args = new String[0];
		
		String[] scmds = getSubcommands(ics);
		if(scmds != null && args.length == 0)
		{ LatCoreMC.printChat(ics, "Subcommands: " + LatCore.strip(scmds)); return; }
		
		String s = onCommand(ics, args);
		if(s != null) LatCoreMC.printChat(ics, EnumChatFormatting.RED + s);
		onPostCommand(ics, args);
	}
	
	public abstract String[] getSubcommands(ICommandSender ics);
	public abstract void printHelp(ICommandSender ics);
	public abstract String onCommand(ICommandSender ics, String[] args);
	
	public String retHelp(ICommandSender ics, String cmd)
	{ return "Invalid command syntax!"; }
	
	public final void printHelpLine(ICommandSender ics, String args)
	{ LatCoreMC.printChat(ics, "/" + commandName + (args != null && args.length() > 0 ? args : "")); }
	
	public void onPostCommand(ICommandSender ics, String[] args) {}
	
	@SuppressWarnings("all")
	public final List addTabCompletionOptions(ICommandSender ics, String[] args)
	{
		String[] s = getTabStrings(ics, args, args.length - 1);
		if(s != null && s.length > 0)
		{
			if(sortStrings(ics, args, args.length - 1)) Arrays.sort(s);
			return getListOfStringsMatchingLastWord(args, s);
		}
		return null;
	}
	
	public final int isUsername(int i)
	{ return 0; }
	
	/**
	 * null - none
	 * true - online
	 * false - all */
	public Boolean isUsername(String[] args, int i)
	{ return null; }
	
	public boolean isArg(String[] args, int i, String... s)
	{
		if(args != null && i >= 0 && i < args.length)
		{
			for(int j = 0; j < s.length; j++)
				if(args[i].equals(s[j])) return true;
		}
		
		return false;
	}
	
	public String[] getTabStrings(ICommandSender ics, String args[], int i)
	{
		Boolean un = isUsername(args, i);
		if(un == null) return null;
		return LMPlayer.getAllDisplayNames(un);
	}
	
	public boolean sortStrings(ICommandSender ics, String args[], int i)
	{ return isUsername(args, i) == null; }
	
	public static EntityPlayerMP getPlayer(ICommandSender ics, String s)
	{
		EntityPlayerMP ep = getLMPlayer(s).getPlayer();
		if(ep != null) return ep;
		throw new PlayerNotFoundException();
	}
	
	public static LMPlayer getLMPlayer(String s)
	{
		LMPlayer p = LMPlayer.getPlayer(s);
		if(p == null) throw new PlayerNotFoundException();
		return p;
	}
	
	public static boolean isOP(UUID id)
	{ return MinecraftServer.getServer().func_152358_ax().func_152652_a(id) != null; }
}