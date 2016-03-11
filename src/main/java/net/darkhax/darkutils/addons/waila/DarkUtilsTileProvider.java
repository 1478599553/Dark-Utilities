package net.darkhax.darkutils.addons.waila;

import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.darkhax.bookshelf.lib.util.ItemStackUtils;
import net.darkhax.darkutils.blocks.BlockFeeder;
import net.darkhax.darkutils.blocks.BlockFilter;
import net.darkhax.darkutils.blocks.BlockSneaky;
import net.darkhax.darkutils.blocks.BlockTimer;
import net.darkhax.darkutils.blocks.BlockTrapMovement;
import net.darkhax.darkutils.blocks.BlockUpdateDetector;
import net.darkhax.darkutils.tileentity.TileEntityFeeder;
import net.darkhax.darkutils.tileentity.TileEntitySneaky;
import net.darkhax.darkutils.tileentity.TileEntityTimer;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;

public class DarkUtilsTileProvider implements IWailaDataProvider {
    
    private static final String CONFIG_FILTER_TYPE = "darkutils.filter.type";
    private static final String CONFIG_TIMER_TIME = "darkutils.timer.time";
    private static final String CONFIG_SNEAKY_OWNERS = "darkutils.sneaky.owner";
    private static final String CONFIG_FEEDER_INFO = "darkutils.feeder.info";
    
    @Override
    public ItemStack getWailaStack (IWailaDataAccessor data, IWailaConfigHandler cfg) {
        
        final Block block = Block.getBlockFromItem(data.getStack().getItem());
        
        if (block instanceof BlockSneaky && !data.getTileEntity().isInvalid()) {
            
            TileEntitySneaky tile = (TileEntitySneaky) data.getTileEntity();
            
            if (cfg.getConfig(CONFIG_SNEAKY_OWNERS) && tile.playerID != null && !tile.playerID.isEmpty() && data.getPlayer().getUniqueID().toString().equals(tile.playerID))
                return data.getStack();
                
            if (tile.heldState != null)
                return new ItemStack(tile.heldState.getBlock(), 1, tile.heldState.getBlock().getMetaFromState(tile.heldState));
        }
        
        return new ItemStack(data.getStack().getItem(), 1, 0);
    }
    
    @Override
    public List<String> getWailaHead (ItemStack stack, List<String> tip, IWailaDataAccessor data, IWailaConfigHandler cfg) {
        
        return tip;
    }
    
    @Override
    public List<String> getWailaBody (ItemStack stack, List<String> tip, IWailaDataAccessor data, IWailaConfigHandler cfg) {
        
        if (data.getBlock() instanceof BlockFilter && cfg.getConfig(CONFIG_FILTER_TYPE) && !(stack.getMetadata() > BlockFilter.EnumType.getTypes().length))
            tip.add(StatCollector.translateToLocal("tooltip.darkutils.filter.type") + ": " + EnumChatFormatting.AQUA + StatCollector.translateToLocal("tooltip.darkutils.filter.type." + BlockFilter.EnumType.getTypes()[stack.getMetadata()]));
            
        else if (data.getBlock() instanceof BlockTimer && cfg.getConfig(CONFIG_TIMER_TIME) && data.getTileEntity() instanceof TileEntityTimer && !data.getTileEntity().isInvalid()) {
            
            int delay = data.getNBTData().getInteger("TickRate");
            int currentTime = data.getNBTData().getInteger("CurrentTime");
            
            tip.add(StatCollector.translateToLocal("gui.darkutils.timer.delay") + ": " + delay);
            tip.add(StatCollector.translateToLocal("gui.darkutils.timer.remaining") + ": " + StringUtils.ticksToElapsedTime((delay - currentTime)));
        }
        
        else if (data.getBlock() instanceof BlockFeeder && cfg.getConfig(CONFIG_FEEDER_INFO) && data.getTileEntity() instanceof TileEntityFeeder && !data.getTileEntity().isInvalid()) {
            
            TileEntityFeeder feeder = (TileEntityFeeder) data.getTileEntity();
            
            if (feeder.foodType != null && !feeder.foodType.equalsIgnoreCase("null"))
                tip.add(StatCollector.translateToLocal("tooltip.darkutils.feeder.type") + ": " + ItemStackUtils.createStackFromString(feeder.foodType).getDisplayName());
            
            tip.add(StatCollector.translateToLocal("tooltip.darkutils.feeder.amount") + ": " + data.getMetadata());
        }
        
        return tip;
    }
    
    @Override
    public List<String> getWailaTail (ItemStack stack, List<String> tip, IWailaDataAccessor data, IWailaConfigHandler cfg) {
        
        return tip;
    }
    
    @Override
    public NBTTagCompound getNBTData (EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
        
        if (te != null && !te.isInvalid())
            te.writeToNBT(tag);
            
        return tag;
    }
    
    public static void registerAddon (IWailaRegistrar register) {
        
        DarkUtilsTileProvider dataProvider = new DarkUtilsTileProvider();
        register.registerStackProvider(dataProvider, BlockTrapMovement.class);
        register.registerStackProvider(dataProvider, BlockUpdateDetector.class);
        register.registerStackProvider(dataProvider, BlockSneaky.class);
        register.registerBodyProvider(dataProvider, BlockFilter.class);
        register.registerBodyProvider(dataProvider, BlockTimer.class);
        register.registerBodyProvider(dataProvider, BlockFeeder.class);
        
        register.registerNBTProvider(dataProvider, BlockTimer.class);
        register.registerNBTProvider(dataProvider, BlockSneaky.class);
        register.registerNBTProvider(dataProvider, BlockFeeder.class);
        
        register.addConfig("DarkUtils", CONFIG_FILTER_TYPE);
        register.addConfig("DarkUtils", CONFIG_TIMER_TIME);
        register.addConfig("DarkUtils", CONFIG_SNEAKY_OWNERS);
        register.addConfig("DarkUtils", CONFIG_FEEDER_INFO);
    }
}