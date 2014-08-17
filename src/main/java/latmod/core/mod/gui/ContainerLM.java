package latmod.core.mod.gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;

public abstract class ContainerLM extends Container
{
	public EntityPlayer player;
	public IInventory inv;
	
	public ContainerLM(EntityPlayer ep, IInventory i)
	{
		player = ep;
		inv = i;
	}
	
	public ItemStack transferStackInSlot(EntityPlayer ep, int i)
	{
		ItemStack is = null;
		Slot slot = (Slot)inventorySlots.get(i);

		if (slot != null && slot.getHasStack())
		{
			ItemStack is1 = slot.getStack();
			is = is1.copy();

			if (i < inv.getSizeInventory())
			{
				if (!mergeItemStack(is1, inv.getSizeInventory(), inventorySlots.size(), true))
					return null;
			}
			else if (!mergeItemStack(is1, 0, inv.getSizeInventory(), false))
				return null;

			if (is1.stackSize == 0)
				slot.putStack((ItemStack)null);
			else
				slot.onSlotChanged();
		}

		return is;
	}
	
	public void addPlayerSlots(int posY)
	{
		for(int y = 0; y < 3; y++) for(int x = 0; x < 9; x++)
		addSlotToContainer(new Slot(player.inventory, x + y * 9 + 9, 8 + x * 18, posY + y * 18));
		
		for(int x = 0; x < 9; x++)
		addSlotToContainer(new Slot(player.inventory, x, 8 + x * 18, posY + 58));
	}
	
	public boolean canInteractWith(EntityPlayer ep)
	{ return true; }
}