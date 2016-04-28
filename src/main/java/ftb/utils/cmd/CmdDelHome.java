package ftb.utils.cmd;

import ftb.lib.api.ForgePlayerMP;
import ftb.lib.api.ForgeWorldMP;
import ftb.lib.api.cmd.CommandLM;
import ftb.lib.api.cmd.CommandLevel;
import ftb.utils.FTBULang;
import ftb.utils.world.FTBUPlayerDataMP;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class CmdDelHome extends CommandLM
{
	public CmdDelHome()
	{ super("delhome", CommandLevel.ALL); }
	
	@Override
	public String getCommandUsage(ICommandSender ics)
	{ return '/' + commandName + " <ID>"; }
	
	@Override
	public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender ics, String[] args, BlockPos pos)
	{
		if(args.length == 1)
		{
			return getListOfStringsMatchingLastWord(args, FTBUPlayerDataMP.get(ForgeWorldMP.inst.getPlayer(ics)).homes.list());
		}
		
		return super.getTabCompletionOptions(server, ics, args, pos);
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender ics, String[] args) throws CommandException
	{
		ForgePlayerMP p = ForgePlayerMP.get(ics);
		checkArgs(args, 1);
		
		if(FTBUPlayerDataMP.get(p).homes.set(args[0], null))
		{
			FTBULang.home_del.printChat(ics, args[0]);
		}
		
		throw new CommandException("ftbu.cmd.home_not_set", args[0]);
	}
}