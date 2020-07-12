package org.ultramine.mods.ae2gt5p2p;

import appeng.api.implementations.items.IMemoryCard;
import appeng.me.GridAccessException;
import appeng.me.cache.helpers.TunnelCollection;
import appeng.parts.p2p.PartP2PTunnel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;

import java.util.ArrayList;

public abstract class PartDedicatedP2PTunnel<T extends PartDedicatedP2PTunnel> extends PartP2PTunnel<T>
{
	public PartDedicatedP2PTunnel(ItemStack is)
	{
		super(is);
	}

	@Override
	public boolean onPartActivate(final EntityPlayer player, final Vec3 pos )
	{
		final ItemStack is = player.inventory.getCurrentItem();
		if(is != null && is.getItem() instanceof IMemoryCard)
			return super.onPartActivate(player, pos);
		return false;
	}

	@SuppressWarnings("unchecked")
	protected T getInput()
	{
		if( this.getFrequency() == 0 )
		{
			return null;
		}

		try
		{
			final PartP2PTunnel tunnel = this.getProxy().getP2P().getInput( this.getFrequency() );
			if( this.getClass().isInstance( tunnel ) )
			{
				return (T) tunnel;
			}
		}
		catch( final GridAccessException e )
		{
			// :P
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	protected TunnelCollection<T> getOutputs() throws GridAccessException
	{
		if( this.getProxy().isActive() )
		{
			return (TunnelCollection<T>) (TunnelCollection) getProxy().getP2P().getOutputs( this.getFrequency(), this.getClass() );
		}
		return new TunnelCollection( new ArrayList(), this.getClass() );
	}
}
