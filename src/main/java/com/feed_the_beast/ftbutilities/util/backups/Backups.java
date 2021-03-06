package com.feed_the_beast.ftbutilities.util.backups;

import com.feed_the_beast.ftblib.lib.util.CommonUtils;
import com.feed_the_beast.ftblib.lib.util.FileUtils;
import com.feed_the_beast.ftblib.lib.util.JsonUtils;
import com.feed_the_beast.ftblib.lib.util.ServerUtils;
import com.feed_the_beast.ftblib.lib.util.text_components.Notification;
import com.feed_the_beast.ftbutilities.FTBUConfig;
import com.feed_the_beast.ftbutilities.FTBUFinals;
import com.feed_the_beast.ftbutilities.FTBULang;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.function.Function;

public enum Backups
{
	INSTANCE;

	public static final ResourceLocation NOTIFICATION_ID = FTBUFinals.get("backup");

	public final List<Backup> backups = new ArrayList<>();
	public File backupsFolder;
	public long nextBackup = -1L;
	public ThreadBackup thread;

	public void init()
	{
		backupsFolder = FTBUConfig.backups.folder.isEmpty() ? new File(CommonUtils.folderMinecraft, "/backups/") : new File(FTBUConfig.backups.folder);
		thread = null;

		backups.clear();

		JsonElement element = JsonUtils.fromJson(new File(backupsFolder, "backups.json"));

		if (element.isJsonArray())
		{
			try
			{
				for (JsonElement e : element.getAsJsonArray())
				{
					backups.add(new Backup(e.getAsJsonObject()));
				}
			}
			catch (Throwable ex)
			{
				ex.printStackTrace();
			}
		}
		else if (backupsFolder.exists())
		{
			String[] files = backupsFolder.list();
			int index = 0;

			if (files != null)
			{
				for (String s : files)
				{
					String[] s1 = s.split("-");

					if (s1.length >= 6)
					{
						int year = Integer.parseInt(s1[0]);
						int month = Integer.parseInt(s1[1]);
						int day = Integer.parseInt(s1[2]);
						int hours = Integer.parseInt(s1[3]);
						int minutes = Integer.parseInt(s1[4]);
						int seconds = Integer.parseInt(s1[5].replace(".zip", ""));

						Calendar c = Calendar.getInstance();
						c.set(year, month, day, hours, minutes, seconds);

						if (FTBUConfig.backups.compression_level > 0)
						{
							s += ".zip";
						}

						backups.add(new Backup(c.getTimeInMillis(), s, ++index, true));
					}
				}
			}
		}

		cleanupAndSave();
		FTBUFinals.LOGGER.info("Backups folder - " + backupsFolder.getAbsolutePath());
	}

	public static void notifyAll(Function<ICommandSender, ITextComponent> function, boolean error)
	{
		for (EntityPlayerMP player : ServerUtils.getPlayers())
		{
			ITextComponent component = function.apply(player);
			component.getStyle().setColor(error ? TextFormatting.DARK_RED : TextFormatting.LIGHT_PURPLE);
			Notification.of(NOTIFICATION_ID, component).setImportant(true).send(null);
		}

		FTBUFinals.LOGGER.info(function.apply(null).getUnformattedText());
	}

	public boolean run(MinecraftServer server, ICommandSender sender, String customName)
	{
		if (thread != null)
		{
			return false;
		}

		boolean auto = !(sender instanceof EntityPlayerMP);

		if (auto && !FTBUConfig.backups.enabled)
		{
			return false;
		}

		notifyAll(player -> FTBULang.BACKUP_START.textComponent(player, sender.getName()), false);
		nextBackup = CommonUtils.getWorldTime() + FTBUConfig.backups.ticks();

		try
		{
			if (server.getPlayerList() != null)
			{
				server.getPlayerList().saveAllPlayerData();
			}

			try
			{
				for (int i = 0; i < server.worlds.length; ++i)
				{
					if (server.worlds[i] != null)
					{
						WorldServer worldserver = server.worlds[i];
						worldserver.disableLevelSaving = true;
						worldserver.saveAllChunks(true, null);
					}
				}
			}
			catch (Exception ex1)
			{
				notifyAll(FTBULang.BACKUP_SAVING_FAILED::textComponent, true);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		File wd = server.getEntityWorld().getSaveHandler().getWorldDirectory();

		if (FTBUConfig.backups.use_separate_thread)
		{
			thread = new ThreadBackup(wd, customName);
			thread.start();
		}
		else
		{
			ThreadBackup.doBackup(wd, customName);
		}

		return true;
	}

	public void cleanupAndSave()
	{
		JsonArray a = new JsonArray();

		if (!backups.isEmpty())
		{
			backups.sort(Backup.COMPARATOR);

			int backupsToKeep = FTBUConfig.backups.backups_to_keep;

			if (backupsToKeep > 0)
			{
				if (backups.size() > backupsToKeep)
				{
					int toDelete = backups.size() - backupsToKeep;

					if (toDelete > 0)
					{
						for (int i = toDelete - 1; i >= 0; i--)
						{
							Backup b = backups.get(i);
							FTBUFinals.LOGGER.info(FTBULang.BACKUP_DELETING_OLD.translate(b.fileId));
							FileUtils.delete(b.getFile());
							backups.remove(i);
						}
					}
				}

				for (int i = backups.size() - 1; i >= 0; i--)
				{
					if (!backups.get(i).getFile().exists())
					{
						backups.remove(i);
					}
				}
			}

			for (Backup t : backups)
			{
				a.add(t.toJsonObject());
			}
		}

		JsonUtils.toJson(a, new File(backupsFolder, "backups.json"));
	}

	public void postBackup()
	{
		try
		{
			MinecraftServer server = ServerUtils.getServer();

			for (int i = 0; i < server.worlds.length; ++i)
			{
				if (server.worlds[i] != null)
				{
					WorldServer worldserver = server.worlds[i];

					if (worldserver.disableLevelSaving)
					{
						worldserver.disableLevelSaving = false;
					}
				}
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	int getLastIndex()
	{
		int i = 0;

		for (Backup b : backups)
		{
			i = Math.max(i, b.index);
		}

		return i;
	}
}