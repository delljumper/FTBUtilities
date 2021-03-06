package com.feed_the_beast.ftbutilities.net;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbutilities.cmd.CmdEditNBT;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class MessageEditNBTResponse extends MessageToServer<MessageEditNBTResponse>
{
	private NBTTagCompound info, mainNbt;

	public MessageEditNBTResponse()
	{
	}

	public MessageEditNBTResponse(NBTTagCompound i, NBTTagCompound nbt)
	{
		info = i;
		mainNbt = nbt;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBUNetHandler.NBTEDIT;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeNBT(info);
		data.writeNBT(mainNbt);
	}

	@Override
	public void readData(DataIn data)
	{
		info = data.readNBT();
		mainNbt = data.readNBT();
	}

	@Override
	public void onMessage(MessageEditNBTResponse m, EntityPlayer player)
	{
		if (CmdEditNBT.EDITING.get(player.getGameProfile().getId()).equals(m.info))
		{
			CmdEditNBT.EDITING.remove(player.getGameProfile().getId());

			switch (m.info.getString("type"))
			{
				case "player":
				{
					ForgePlayer player1 = Universe.get().getPlayer(m.info.getUniqueId("id"));

					if (player1 != null && player1.isOnline())
					{
						EntityPlayer entity = player1.getPlayer();
						entity.deserializeNBT(m.mainNbt);

						if (entity.isEntityAlive())
						{
							player.world.updateEntityWithOptionalForce(entity, true);
						}
					}

					break;
				}
				case "tile":
				{
					BlockPos pos = new BlockPos(m.info.getInteger("x"), m.info.getInteger("y"), m.info.getInteger("z"));

					TileEntity tile = player.world.getTileEntity(pos);

					if (tile != null)
					{
						m.mainNbt.setInteger("x", pos.getX());
						m.mainNbt.setInteger("y", pos.getY());
						m.mainNbt.setInteger("z", pos.getZ());
						m.mainNbt.setString("id", m.info.getString("id"));
						tile.readFromNBT(m.mainNbt);
						tile.markDirty();
						tile.getWorld().markChunkDirty(pos, tile);
						IBlockState state = tile.getWorld().getBlockState(pos);
						tile.getWorld().notifyBlockUpdate(pos, state, state, 255);

						if (state != Blocks.AIR.getDefaultState())
						{
							tile.getWorld().updateComparatorOutputLevel(pos, state.getBlock());
						}
					}

					break;
				}
				case "entity":
				{
					Entity entity = player.world.getEntityByID(m.info.getInteger("id"));

					if (entity != null)
					{
						entity.deserializeNBT(m.mainNbt);

						if (entity.isEntityAlive())
						{
							player.world.updateEntityWithOptionalForce(entity, true);
						}
					}

					break;
				}
			}
		}
	}
}