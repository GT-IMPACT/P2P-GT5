package org.ultramine.mods.ae2gt5p2p;

import appeng.api.AEApi;
import com.google.common.base.Optional;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@Mod(modid = "AE2-GT5-P2P", name = "AE2-GT5-P2P", version = "@version@")
public class AE2GT5P2P
{
	@Mod.EventHandler
	public void init(FMLInitializationEvent e)
	{
		AEApi.instance().partHelper().registerNewLayer(
				"org.ultramine.mods.ae2gt5p2p.LayerIEnergyConnected",
				"gregtech.api.interfaces.tileentity.IEnergyConnected"
		);
		ItemP2PGT5 part = new ItemP2PGT5();
		GameRegistry.registerItem(part, "part");
		AEApi.instance().partHelper().setItemBusRenderer(part);
		Optional<ItemStack> p2pME = AEApi.instance().definitions().parts().p2PTunnelME().maybeStack(1);
		if(p2pME.isPresent())
		{
			GameRegistry.addShapelessRecipe(new ItemStack(part), p2pME.get());
			GameRegistry.addShapelessRecipe(p2pME.get(), new ItemStack(part));
		}
	}
}
