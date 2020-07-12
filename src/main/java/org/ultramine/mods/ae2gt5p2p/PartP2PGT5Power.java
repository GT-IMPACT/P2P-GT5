package org.ultramine.mods.ae2gt5p2p;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.me.GridAccessException;
import cofh.api.energy.IEnergyReceiver;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.GregTech_API;
import gregtech.api.interfaces.tileentity.IEnergyConnected;
import gregtech.api.util.GT_Utility;
import ic2.api.energy.tile.IEnergySink;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nullable;

import static appeng.api.networking.GridFlags.REQUIRE_CHANNEL;
import static appeng.api.networking.ticking.TickRateModulation.IDLE;
import static appeng.api.networking.ticking.TickRateModulation.URGENT;
import static gregtech.api.enums.GT_Values.V;

public class PartP2PGT5Power extends PartDedicatedP2PTunnel<PartP2PGT5Power> implements IPartGT5Power, IGridTickable
{
	private long buffer;
	private long voltage;
	private TileEntity cachedTarget;
	private boolean isTargetCached;

	public PartP2PGT5Power(ItemStack is)
	{
		super(is);
		this.getProxy().setFlags(REQUIRE_CHANNEL);
	}

	@Override
	public void onTunnelNetworkChange()
	{
		isTargetCached = false;
		cachedTarget = null;
	}

	@Override
	public void onNeighborChanged()
	{
		isTargetCached = false;
		cachedTarget = null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected IIcon getTypeTexture()
	{
		return Blocks.stone.getIcon(0, 0);
	}

	@Override
	public void readFromNBT(NBTTagCompound data)
	{
		super.readFromNBT(data);
		buffer = data.getLong("e");
		voltage = data.getLong("v");
	}

	@Override
	public void writeToNBT(NBTTagCompound data)
	{
		super.writeToNBT(data);
		data.setLong("e", buffer);
		data.setLong("v", voltage);
	}

	@Override
	public boolean onPartActivate(final EntityPlayer player, final Vec3 pos )
	{
		if(!super.onPartActivate(player, pos) && !player.worldObj.isRemote && player.inventory.getCurrentItem() == null)
		{
			PartP2PGT5Power input = getInput();
			String inputLoc;
			if(input == null)
			{
				inputLoc = "no input";
			}
			else
			{
				TileEntity te = input.getHost().getTile();
				inputLoc = "[" + te.getWorldObj().provider.dimensionId + "](" + te.xCoord + ", " + te.yCoord + ", " + te.zCoord + ")";
			}
			player.addChatMessage(chatComponent("------", ""));
			player.addChatMessage(chatComponent("Input location: ", (input == this ? "this block " : "") + inputLoc));
			player.addChatMessage(chatComponent("Name: ", input != null ? input.getCustomName() : getCustomName()));
			player.addChatMessage(chatComponent("Freq: ", ""+getFrequency()));
			player.addChatMessage(chatComponent("Voltage: ", (input == null ? "no input" : (""+input.voltage))));
			return true;
		}
		return false;
	}

	private static IChatComponent chatComponent(String title, String value)
	{
		ChatComponentText cct = new ChatComponentText(title);
		cct.getChatStyle().setColor(EnumChatFormatting.GOLD);
		ChatComponentText ccv = new ChatComponentText(value);
		ccv.getChatStyle().setColor(EnumChatFormatting.YELLOW);
		cct.appendSibling(ccv);
		return cct;
	}

	private long getMaxBufferSize(long voltage)
	{
		return voltage*64;
	}

	@Override
	public long injectEnergyUnits(long voltage, long amperage)
	{
		if(isOutput() || !isActive() || voltage <= 0 || amperage <= 0 || buffer >= getMaxBufferSize(voltage))
			return 0;

		long lastBuffer = this.buffer;
		long lastVoltage = this.voltage;

		this.voltage = voltage;
		this.buffer += amperage*voltage;

		if(lastBuffer < voltage && this.buffer >= voltage || lastVoltage <= 0)
		{
			try
			{
				this.getProxy().getTick().alertDevice( this.getProxy().getNode() );
			}
			catch( final GridAccessException e )
			{
				// :P
			}
		}

		return amperage;
	}

	@Override
	public boolean inputEnergy()
	{
		return !isOutput();
	}

	@Override
	public boolean outputsEnergy()
	{
		return isOutput();
	}

	@Override
	public TickingRequest getTickingRequest(IGridNode node)
	{
		return new TickingRequest(1, 20, false, true);
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall)
	{
		long voltage = this.voltage;
		if(isOutput() || voltage <= 0 || this.buffer < voltage)
			return IDLE;

		long amperes = buffer / voltage;
		long amperesUsed = 0;

		try
		{
			for(final PartP2PGT5Power t : this.getOutputs())
			{
				long received = t.doOutput(voltage, amperes);
				amperesUsed += received;
				amperes -= received;
				if(amperes <= 0)
					break;
			}
		}
		catch(final GridAccessException ignored)
		{
		}

		buffer -= amperesUsed*voltage;
//		canOverflowBuffer = buffer < getMaxBufferSize(voltage);

		return (buffer < voltage) ? IDLE : URGENT /*(amperesUsed == 0 ? SLOWER : URGENT)*/;
	}

	@Nullable
	private TileEntity getTarget()
	{
		if(isTargetCached)
		{
			TileEntity te = cachedTarget;
			if(te == null || !te.isInvalid())
				return te;
		}

		isTargetCached = true;

		final TileEntity self = this.getTile();
		ForgeDirection side = this.getSide();
		return cachedTarget = self.getWorldObj().getTileEntity(self.xCoord + side.offsetX, self.yCoord + side.offsetY, self.zCoord + side.offsetZ);
	}

	private long doOutput(long aVoltage, long aAmperage)
	{
		if(!isOutput())
			return 0;
		final TileEntity te = getTarget();
		if(te == null)
			return 0;
		final ForgeDirection oppositeSide = getSide().getOpposite();

		if(te instanceof IEnergyConnected)
		{
			return ((IEnergyConnected) te).injectEnergyUnits((byte) oppositeSide.ordinal(), aVoltage, aAmperage);
		}
		else if(te instanceof IEnergySink)
		{
			if(((IEnergySink) te).acceptsEnergyFrom(this.getTile(), oppositeSide))
			{
				long rUsedAmperes = 0;
				while(aAmperage > rUsedAmperes && ((IEnergySink) te).getDemandedEnergy() > 0 && ((IEnergySink) te).injectEnergy(oppositeSide, aVoltage, aVoltage) < aVoltage)
					rUsedAmperes++;
				return rUsedAmperes;
			}
		}
		else if(GregTech_API.mOutputRF && te instanceof IEnergyReceiver)
		{
			int rfOut = (int) (aVoltage * GregTech_API.mEUtoRF / 100);
			if(((IEnergyReceiver) te).receiveEnergy(oppositeSide, rfOut, true) == rfOut)
			{
				((IEnergyReceiver) te).receiveEnergy(oppositeSide, rfOut, false);
				return 1;
			}
			if(GregTech_API.mRFExplosions && GregTech_API.sMachineExplosions && ((IEnergyReceiver) te).getMaxEnergyStored(oppositeSide) < rfOut * 600)
			{
				if(rfOut > 32 * GregTech_API.mEUtoRF / 100)
				{
					int aExplosionPower = rfOut;
					float tStrength = aExplosionPower < V[0] ? 1.0F : aExplosionPower < V[1] ? 2.0F : aExplosionPower < V[2] ? 3.0F : aExplosionPower < V[3] ? 4.0F : aExplosionPower < V[4] ? 5.0F : aExplosionPower < V[4] * 2 ? 6.0F : aExplosionPower < V[5] ? 7.0F : aExplosionPower < V[6] ? 8.0F : aExplosionPower < V[7] ? 9.0F : 10.0F;
					int tX = te.xCoord, tY = te.yCoord, tZ = te.zCoord;
					World tWorld = te.getWorldObj();
					GT_Utility.sendSoundToPlayers(tWorld, GregTech_API.sSoundList.get(209), 1.0F, -1, tX, tY, tZ);
					tWorld.setBlock(tX, tY, tZ, Blocks.air);
					if(GregTech_API.sMachineExplosions)
						tWorld.createExplosion(null, tX + 0.5, tY + 0.5, tZ + 0.5, tStrength, true);
				}
			}
		}

		return 0;
	}
}
