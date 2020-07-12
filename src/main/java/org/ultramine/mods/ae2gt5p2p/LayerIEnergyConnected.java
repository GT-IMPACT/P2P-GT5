package org.ultramine.mods.ae2gt5p2p;

import appeng.api.parts.IPart;
import appeng.api.parts.LayerBase;
import cofh.api.energy.IEnergyReceiver;
import gregtech.api.GregTech_API;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IEnergyConnected;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.util.GT_CoverBehavior;
import gregtech.api.util.GT_Utility;
import ic2.api.energy.tile.IEnergySink;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.enums.GT_Values.V;

public class LayerIEnergyConnected extends LayerBase implements IGregTechTileEntity {
    private static final boolean ignoreUnloadedChunks = true;

    @Override
    public long injectEnergyUnits(byte side, long voltage, long amperage) {
        final IPart part = this.getPart(ForgeDirection.getOrientation(side));
        if (part instanceof IPartGT5Power) {
            return ((IPartGT5Power) part).injectEnergyUnits(voltage, amperage);
        } else {
            ForgeDirection dir = ForgeDirection.getOrientation(side);
            if (part instanceof IEnergySink) {
                TileEntity source = getTileEntityAtSide(side);
                if (source != null && ((IEnergySink) part).acceptsEnergyFrom(source, dir)) {
                    long rUsedAmperes = 0;
                    while (amperage > rUsedAmperes && ((IEnergySink) part).getDemandedEnergy() > 0 && ((IEnergySink) part).injectEnergy(dir, voltage, voltage) < voltage)
                        rUsedAmperes++;
                    return rUsedAmperes;
                }
            }
//			else if(GregTech_API.mOutputRF && part instanceof IEnergyReceiver)
//			{
//				int rfOut = (int) (voltage * GregTech_API.mEUtoRF / 100);
//				if(((IEnergyReceiver) this).receiveEnergy(dir, rfOut, true) == rfOut)
//				{
//					((IEnergyReceiver) this).receiveEnergy(dir, rfOut, false);
//					return 1;
//				}
//			}
        }
        return 0;
    }

    @Override
    public boolean inputEnergyFrom(byte side) {
        ForgeDirection dir = ForgeDirection.getOrientation(side);
        final IPart part = this.getPart(dir);
        if (part instanceof IPartGT5Power)
            return ((IPartGT5Power) part).inputEnergy();
        if (part instanceof IEnergySink) {
            TileEntity source = getTileEntityAtSide(side);
            return source != null && ((IEnergySink) part).acceptsEnergyFrom(source, dir);
        }
        return false;
//		return GregTech_API.mOutputRF && part instanceof IEnergyReceiver && ((IEnergyReceiver) part).canConnectEnergy(dir);
    }

    @Override
    public boolean inputEnergyFrom(byte side, boolean b1) {
        ForgeDirection dir = ForgeDirection.getOrientation(side);
        final IPart part = this.getPart(dir);
        if (part instanceof IPartGT5Power)
            return ((IPartGT5Power) part).inputEnergy();
        if (part instanceof IEnergySink) {
            TileEntity source = getTileEntityAtSide(side);
            return source != null && ((IEnergySink) part).acceptsEnergyFrom(source, dir);
        }
        return false;
//		return GregTech_API.mOutputRF && part instanceof IEnergyReceiver && ((IEnergyReceiver) part).canConnectEnergy(dir);
    }

    @Override
    public boolean outputsEnergyTo(byte side) {
        final IPart part = this.getPart(ForgeDirection.getOrientation(side));
        if (part instanceof IPartGT5Power)
            return ((IPartGT5Power) part).outputsEnergy();
        return false;
    }

    @Override
    public boolean outputsEnergyTo(byte side, boolean b1) {
        final IPart part = this.getPart(ForgeDirection.getOrientation(side));
        if (part instanceof IPartGT5Power)
            return ((IPartGT5Power) part).outputsEnergy();
        return false;
    }

    @Override
    public byte getColorization() {
        return -1;
    }

    @Override
    public byte setColorization(byte b) {
        return -1;
    }

    //
    //
    //

    @Override
    public final World getWorld() {
        return worldObj;
    }

    @Override
    public final int getXCoord() {
        return xCoord;
    }

    @Override
    public final short getYCoord() {
        return (short) yCoord;
    }

    @Override
    public final int getZCoord() {
        return zCoord;
    }

    @Override
    public final int getOffsetX(byte aSide, int aMultiplier) {
        return xCoord + ForgeDirection.getOrientation(aSide).offsetX * aMultiplier;
    }

    @Override
    public final short getOffsetY(byte aSide, int aMultiplier) {
        return (short) (yCoord + ForgeDirection.getOrientation(aSide).offsetY * aMultiplier);
    }

    @Override
    public final int getOffsetZ(byte aSide, int aMultiplier) {
        return zCoord + ForgeDirection.getOrientation(aSide).offsetZ * aMultiplier;
    }

    @Override
    public final boolean isServerSide() {
        return !worldObj.isRemote;
    }

    @Override
    public final boolean isClientSide() {
        return worldObj.isRemote;
    }

    @Override
    public final boolean openGUI(EntityPlayer aPlayer) {
        return openGUI(aPlayer, 0);
    }

    @Override
    public final boolean openGUI(EntityPlayer aPlayer, int aID) {
        return false;
    }

    @Override
    public final int getRandomNumber(int aRange) {
        return worldObj.rand.nextInt(aRange);
    }

    @Override
    public final BiomeGenBase getBiome(int aX, int aZ) {
        return worldObj.getBiomeGenForCoords(aX, aZ);
    }

    @Override
    public final BiomeGenBase getBiome() {
        return getBiome(xCoord, zCoord);
    }

    @Override
    public final Block getBlockOffset(int aX, int aY, int aZ) {
        return getBlock(xCoord + aX, yCoord + aY, zCoord + aZ);
    }

    @Override
    public final Block getBlockAtSide(byte aSide) {
        return getBlockAtSideAndDistance(aSide, 1);
    }

    @Override
    public final Block getBlockAtSideAndDistance(byte aSide, int aDistance) {
        return getBlock(getOffsetX(aSide, aDistance), getOffsetY(aSide, aDistance), getOffsetZ(aSide, aDistance));
    }

    @Override
    public final byte getMetaIDOffset(int aX, int aY, int aZ) {
        return getMetaID(xCoord + aX, yCoord + aY, zCoord + aZ);
    }

    @Override
    public final byte getMetaIDAtSide(byte aSide) {
        return getMetaIDAtSideAndDistance(aSide, 1);
    }

    @Override
    public final byte getMetaIDAtSideAndDistance(byte aSide, int aDistance) {
        return getMetaID(getOffsetX(aSide, aDistance), getOffsetY(aSide, aDistance), getOffsetZ(aSide, aDistance));
    }

    @Override
    public final byte getLightLevelOffset(int aX, int aY, int aZ) {
        return getLightLevel(xCoord + aX, yCoord + aY, zCoord + aZ);
    }

    @Override
    public final byte getLightLevelAtSide(byte aSide) {
        return getLightLevelAtSideAndDistance(aSide, 1);
    }

    @Override
    public final byte getLightLevelAtSideAndDistance(byte aSide, int aDistance) {
        return getLightLevel(getOffsetX(aSide, aDistance), getOffsetY(aSide, aDistance), getOffsetZ(aSide, aDistance));
    }

    @Override
    public final boolean getOpacityOffset(int aX, int aY, int aZ) {
        return getOpacity(xCoord + aX, yCoord + aY, zCoord + aZ);
    }

    @Override
    public final boolean getOpacityAtSide(byte aSide) {
        return getOpacityAtSideAndDistance(aSide, 1);
    }

    @Override
    public final boolean getOpacityAtSideAndDistance(byte aSide, int aDistance) {
        return getOpacity(getOffsetX(aSide, aDistance), getOffsetY(aSide, aDistance), getOffsetZ(aSide, aDistance));
    }

    @Override
    public final boolean getSkyOffset(int aX, int aY, int aZ) {
        return getSky(xCoord + aX, yCoord + aY, zCoord + aZ);
    }

    @Override
    public final boolean getSkyAtSide(byte aSide) {
        return getSkyAtSideAndDistance(aSide, 1);
    }

    @Override
    public final boolean getSkyAtSideAndDistance(byte aSide, int aDistance) {
        return getSky(getOffsetX(aSide, aDistance), getOffsetY(aSide, aDistance), getOffsetZ(aSide, aDistance));
    }

    @Override
    public final boolean getAirOffset(int aX, int aY, int aZ) {
        return getAir(xCoord + aX, yCoord + aY, zCoord + aZ);
    }

    @Override
    public final boolean getAirAtSide(byte aSide) {
        return getAirAtSideAndDistance(aSide, 1);
    }

    @Override
    public final boolean getAirAtSideAndDistance(byte aSide, int aDistance) {
        return getAir(getOffsetX(aSide, aDistance), getOffsetY(aSide, aDistance), getOffsetZ(aSide, aDistance));
    }

    @Override
    public final TileEntity getTileEntityOffset(int aX, int aY, int aZ) {
        return getTileEntity(xCoord + aX, yCoord + aY, zCoord + aZ);
    }

    @Override
    public final TileEntity getTileEntityAtSideAndDistance(byte aSide, int aDistance) {
        if (aDistance == 1) return getTileEntityAtSide(aSide);
        return getTileEntity(getOffsetX(aSide, aDistance), getOffsetY(aSide, aDistance), getOffsetZ(aSide, aDistance));
    }

    @Override
    public final IInventory getIInventory(int aX, int aY, int aZ) {
        TileEntity tTileEntity = getTileEntity(aX, aY, aZ);
        if (tTileEntity instanceof IInventory) return (IInventory) tTileEntity;
        return null;
    }

    @Override
    public final IInventory getIInventoryOffset(int aX, int aY, int aZ) {
        TileEntity tTileEntity = getTileEntityOffset(aX, aY, aZ);
        if (tTileEntity instanceof IInventory) return (IInventory) tTileEntity;
        return null;
    }

    @Override
    public final IInventory getIInventoryAtSide(byte aSide) {
        TileEntity tTileEntity = getTileEntityAtSide(aSide);
        if (tTileEntity instanceof IInventory) return (IInventory) tTileEntity;
        return null;
    }

    @Override
    public final IInventory getIInventoryAtSideAndDistance(byte aSide, int aDistance) {
        TileEntity tTileEntity = getTileEntityAtSideAndDistance(aSide, aDistance);
        if (tTileEntity instanceof IInventory) return (IInventory) tTileEntity;
        return null;
    }

    @Override
    public final IFluidHandler getITankContainer(int aX, int aY, int aZ) {
        TileEntity tTileEntity = getTileEntity(aX, aY, aZ);
        if (tTileEntity instanceof IFluidHandler) return (IFluidHandler) tTileEntity;
        return null;
    }

    @Override
    public final IFluidHandler getITankContainerOffset(int aX, int aY, int aZ) {
        TileEntity tTileEntity = getTileEntityOffset(aX, aY, aZ);
        if (tTileEntity instanceof IFluidHandler) return (IFluidHandler) tTileEntity;
        return null;
    }

    @Override
    public final IFluidHandler getITankContainerAtSide(byte aSide) {
        TileEntity tTileEntity = getTileEntityAtSide(aSide);
        if (tTileEntity instanceof IFluidHandler) return (IFluidHandler) tTileEntity;
        return null;
    }

    @Override
    public final IFluidHandler getITankContainerAtSideAndDistance(byte aSide, int aDistance) {
        TileEntity tTileEntity = getTileEntityAtSideAndDistance(aSide, aDistance);
        if (tTileEntity instanceof IFluidHandler) return (IFluidHandler) tTileEntity;
        return null;
    }

    @Override
    public final IGregTechTileEntity getIGregTechTileEntity(int aX, int aY, int aZ) {
        TileEntity tTileEntity = getTileEntity(aX, aY, aZ);
        if (tTileEntity instanceof IGregTechTileEntity) return (IGregTechTileEntity) tTileEntity;
        return null;
    }

    @Override
    public final IGregTechTileEntity getIGregTechTileEntityOffset(int aX, int aY, int aZ) {
        TileEntity tTileEntity = getTileEntityOffset(aX, aY, aZ);
        if (tTileEntity instanceof IGregTechTileEntity) return (IGregTechTileEntity) tTileEntity;
        return null;
    }

    @Override
    public final IGregTechTileEntity getIGregTechTileEntityAtSide(byte aSide) {
        TileEntity tTileEntity = getTileEntityAtSide(aSide);
        if (tTileEntity instanceof IGregTechTileEntity) return (IGregTechTileEntity) tTileEntity;
        return null;
    }

    @Override
    public final IGregTechTileEntity getIGregTechTileEntityAtSideAndDistance(byte aSide, int aDistance) {
        TileEntity tTileEntity = getTileEntityAtSideAndDistance(aSide, aDistance);
        if (tTileEntity instanceof IGregTechTileEntity) return (IGregTechTileEntity) tTileEntity;
        return null;
    }

    private boolean crossedChunkBorder(int aX, int aZ) {
        return aX >> 4 != xCoord >> 4 || aZ >> 4 != zCoord >> 4;
    }

    @Override
    public final Block getBlock(int aX, int aY, int aZ) {
        if (ignoreUnloadedChunks && crossedChunkBorder(aX, aZ) && !worldObj.blockExists(aX, aY, aZ)) return Blocks.air;
        return worldObj.getBlock(aX, aY, aZ);
    }

    @Override
    public final byte getMetaID(int aX, int aY, int aZ) {
        if (ignoreUnloadedChunks && crossedChunkBorder(aX, aZ) && !worldObj.blockExists(aX, aY, aZ)) return 0;
        return (byte) worldObj.getBlockMetadata(aX, aY, aZ);
    }

    @Override
    public final byte getLightLevel(int aX, int aY, int aZ) {
        if (ignoreUnloadedChunks && crossedChunkBorder(aX, aZ) && !worldObj.blockExists(aX, aY, aZ)) return 0;
        return (byte) (worldObj.getLightBrightness(aX, aY, aZ) * 15);
    }

    @Override
    public final boolean getSky(int aX, int aY, int aZ) {
        if (ignoreUnloadedChunks && crossedChunkBorder(aX, aZ) && !worldObj.blockExists(aX, aY, aZ)) return true;
        return worldObj.canBlockSeeTheSky(aX, aY, aZ);
    }

    @Override
    public final boolean getOpacity(int aX, int aY, int aZ) {
        if (ignoreUnloadedChunks && crossedChunkBorder(aX, aZ) && !worldObj.blockExists(aX, aY, aZ)) return false;
        return GT_Utility.isOpaqueBlock(worldObj, aX, aY, aZ);
    }

    @Override
    public final boolean getAir(int aX, int aY, int aZ) {
        if (ignoreUnloadedChunks && crossedChunkBorder(aX, aZ) && !worldObj.blockExists(aX, aY, aZ)) return true;
        return GT_Utility.isBlockAir(worldObj, aX, aY, aZ);
    }

    @Override
    public final TileEntity getTileEntity(int aX, int aY, int aZ) {
        if (ignoreUnloadedChunks && crossedChunkBorder(aX, aZ) && !worldObj.blockExists(aX, aY, aZ)) return null;
        return worldObj.getTileEntity(aX, aY, aZ);
    }

    @Override
    public final TileEntity getTileEntityAtSide(byte aSide) {
        int tX = getOffsetX(aSide, 1), tY = getOffsetY(aSide, 1), tZ = getOffsetZ(aSide, 1);
        if (crossedChunkBorder(tX, tZ)) {
            if (ignoreUnloadedChunks && !worldObj.blockExists(tX, tY, tZ)) return null;
        }

        return worldObj.getTileEntity(tX, tY, tZ);
    }

    @Override
    public boolean isDead() {
        return isInvalidTileEntity();
    }

    @Override
    public void sendBlockEvent(byte b, byte b1) {
        // nope
    }

    @Override
    public long getTimer() {
        return 0;
    }

    @Override
    public void setLightValue(byte b) {
        // nope
    }

    @Override
    public boolean isInvalidTileEntity() {
        return isInvalid();
    }

    //

    @Override
    public int getErrorDisplayID() {
        return 0;
    }

    @Override
    public void setErrorDisplayID(int i) {

    }

    @Override
    public int getMetaTileID() {
        return 0;
    }

    @Override
    public int setMetaTileID(short i) {
        return 0;
    }

    @Override
    public IMetaTileEntity getMetaTileEntity() {
        return null;
    }

    @Override
    public void setMetaTileEntity(IMetaTileEntity iMetaTileEntity) {

    }

    @Override
    public void issueTextureUpdate() {

    }

    @Override
    public void issueClientUpdate() {

    }

    @Override
    public void doExplosion(long l) {

    }

    @Override
    public void setOnFire() {

    }

    @Override
    public void setToFire() {

    }

    @Override
    public String setOwnerName(String s) {
        return null;
    }

    @Override
    public String getOwnerName() {
        return null;
    }

    @Override
    public void setInitialValuesAsNBT(NBTTagCompound nbtTagCompound, short i) {

    }

    @Override
    public void onLeftclick(EntityPlayer entityPlayer) {

    }

    @Override
    public boolean onRightclick(EntityPlayer entityPlayer, byte b, float v, float v1, float v2) {
        return false;
    }

    @Override
    public float getBlastResistance(byte b) {
        return 0;
    }

    @Override
    public ArrayList<ItemStack> getDrops() {
        return null;
    }

    @Override
    public int getLightOpacity() {
        return 0;
    }

    @Override
    public void addCollisionBoxesToList(World world, int i, int i1, int i2, AxisAlignedBB axisAlignedBB, List<AxisAlignedBB> list, Entity entity) {

    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int i, int i1, int i2) {
        return null;
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int i, int i1, int i2, Entity entity) {

    }

    @Override
    public String[] getDescription() {
        return new String[0];
    }

    @Override
    public boolean canPlaceCoverIDAtSide(byte b, int i) {
        return false;
    }

    @Override
    public boolean canPlaceCoverItemAtSide(byte b, ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean dropCover(byte b, byte b1, boolean b2) {
        return false;
    }

    @Override
    public void setCoverDataAtSide(byte b, int i) {

    }

    @Override
    public void setCoverIDAtSide(byte b, int i) {

    }

    @Override
    public void setCoverItemAtSide(byte b, ItemStack itemStack) {

    }

    @Override
    public int getCoverDataAtSide(byte b) {
        return 0;
    }

    @Override
    public int getCoverIDAtSide(byte b) {
        return 0;
    }

    @Override
    public ItemStack getCoverItemAtSide(byte b) {
        return null;
    }

    @Override
    public GT_CoverBehavior getCoverBehaviorAtSide(byte b) {
        return null;
    }

    @Override
    public byte getInternalInputRedstoneSignal(byte b) {
        return 0;
    }

    @Override
    public void setInternalOutputRedstoneSignal(byte b, byte b1) {

    }

    @Override
    public void issueCoverUpdate(byte b) {

    }

    @Override
    public boolean isUniversalEnergyStored(long l) {
        return false;
    }

    @Override
    public long getUniversalEnergyStored() {
        return 0;
    }

    @Override
    public long getUniversalEnergyCapacity() {
        return 0;
    }

    @Override
    public long getOutputAmperage() {
        return 0;
    }

    @Override
    public long getOutputVoltage() {
        return 0;
    }

    @Override
    public long getInputAmperage() {
        return 0;
    }

    @Override
    public long getInputVoltage() {
        return 0;
    }

    @Override
    public boolean decreaseStoredEnergyUnits(long l, boolean b) {
        return false;
    }

    @Override
    public boolean increaseStoredEnergyUnits(long l, boolean b) {
        return false;
    }

    @Override
    public boolean drainEnergyUnits(byte b, long l, long l1) {
        return false;
    }

    @Override
    public long getAverageElectricInput() {
        return 0;
    }

    @Override
    public long getAverageElectricOutput() {
        return 0;
    }

    @Override
    public long getStoredEU() {
        return 0;
    }

    @Override
    public long getEUCapacity() {
        return 0;
    }

    @Override
    public long getStoredSteam() {
        return 0;
    }

    @Override
    public long getSteamCapacity() {
        return 0;
    }

    @Override
    public boolean increaseStoredSteam(long l, boolean b) {
        return false;
    }

    @Override
    public boolean isDigitalChest() {
        return false;
    }

    @Override
    public ItemStack[] getStoredItemData() {
        return new ItemStack[0];
    }

    @Override
    public void setItemCount(int i) {

    }

    @Override
    public int getMaxItemCount() {
        return 0;
    }

    @Override
    public boolean acceptsRotationalEnergy(byte b) {
        return false;
    }

    @Override
    public boolean injectRotationalEnergy(byte b, long l, long l1) {
        return false;
    }

    @Override
    public boolean isGivingInformation() {
        return false;
    }

    @Override
    public String[] getInfoData() {
        return new String[0];
    }

    @Override
    public boolean hasInventoryBeenModified() {
        return false;
    }

    @Override
    public boolean isValidSlot(int i) {
        return false;
    }

    @Override
    public boolean addStackToSlot(int i, ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean addStackToSlot(int i, ItemStack itemStack, int i1) {
        return false;
    }

    @Override
    public void onMachineBlockUpdate() {

    }

    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public boolean isMufflerUpgradable() {
        return false;
    }

    @Override
    public boolean isSteamEngineUpgradable() {
        return false;
    }

    @Override
    public boolean addMufflerUpgrade() {
        return false;
    }

    @Override
    public boolean addSteamEngineUpgrade() {
        return false;
    }

    @Override
    public boolean hasMufflerUpgrade() {
        return false;
    }

    @Override
    public boolean hasSteamEngineUpgrade() {
        return false;
    }

    @Override
    public int getProgress() {
        return 0;
    }

    @Override
    public int getMaxProgress() {
        return 0;
    }

    @Override
    public boolean increaseProgress(int i) {
        return false;
    }

    @Override
    public boolean hasThingsToDo() {
        return false;
    }

    @Override
    public boolean hasWorkJustBeenEnabled() {
        return false;
    }

    @Override
    public void enableWorking() {

    }

    @Override
    public void disableWorking() {

    }

    @Override
    public boolean isAllowedToWork() {
        return false;
    }

    @Override
    public byte getWorkDataValue() {
        return 0;
    }

    @Override
    public void setWorkDataValue(byte b) {

    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void setActive(boolean b) {

    }

    @Override
    public void setGenericRedstoneOutput(boolean b) {

    }

    @Override
    public void issueBlockUpdate() {

    }

    @Override
    public byte getOutputRedstoneSignal(byte b) {
        return 0;
    }

    @Override
    public void setOutputRedstoneSignal(byte b, byte b1) {

    }

    @Override
    public byte getStrongOutputRedstoneSignal(byte b) {
        return 0;
    }

    @Override
    public void setStrongOutputRedstoneSignal(byte b, byte b1) {

    }

    @Override
    public byte getComparatorValue(byte b) {
        return 0;
    }

    @Override
    public byte getInputRedstoneSignal(byte b) {
        return 0;
    }

    @Override
    public byte getStrongestRedstone() {
        return 0;
    }

    @Override
    public boolean getRedstone() {
        return false;
    }

    @Override
    public boolean getRedstone(byte b) {
        return false;
    }

    @Override
    public ITexture[] getTexture(Block block, byte b) {
        return new ITexture[0];
    }

    @Override
    public byte getFrontFacing() {
        return 0;
    }

    @Override
    public void setFrontFacing(byte b) {

    }

    @Override
    public byte getBackFacing() {
        return 0;
    }

    @Override
    public boolean isValidFacing(byte b) {
        return false;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int p_94128_1_) {
        return new int[0];
    }

    @Override
    public boolean canInsertItem(int p_102007_1_, ItemStack p_102007_2_, int p_102007_3_) {
        return false;
    }

    @Override
    public boolean canExtractItem(int p_102008_1_, ItemStack p_102008_2_, int p_102008_3_) {
        return false;
    }

    @Override
    public int getSizeInventory() {
        return 0;
    }

    @Override
    public ItemStack getStackInSlot(int p_70301_1_) {
        return null;
    }

    @Override
    public ItemStack decrStackSize(int p_70298_1_, int p_70298_2_) {
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int p_70304_1_) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int p_70299_1_, ItemStack p_70299_2_) {

    }

    @Override
    public String getInventoryName() {
        return null;
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 0;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer p_70300_1_) {
        return false;
    }

    @Override
    public void openInventory() {

    }

    @Override
    public void closeInventory() {

    }

    @Override
    public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) {
        return false;
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        return 0;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        return null;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        return null;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return false;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return false;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return new FluidTankInfo[0];
    }
}

