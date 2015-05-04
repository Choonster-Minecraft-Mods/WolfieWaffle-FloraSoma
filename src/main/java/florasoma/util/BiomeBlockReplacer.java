package florasoma.util;

import florasoma.FloraSoma;
import florasoma.common.ConfigVillage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.event.terraingen.BiomeEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.Map;

public class BiomeBlockReplacer implements IEventListener
{
    private Map<Block, List<Pair<String, Block>>> replacements;
    private Map<Block, List<Pair<String, Integer>>> metadata;

    public BiomeBlockReplacer()
    {
        replacements = ConfigVillage.getReplacements();
        metadata = ConfigVillage.getMetadata();
    }

    @Override
    @SubscribeEvent
    public void invoke(Event event)
    {
        if (event instanceof BiomeEvent.GetVillageBlockID)
        {
            BiomeEvent.GetVillageBlockID ev = (BiomeEvent.GetVillageBlockID) event;
            if (replacements.containsKey(ev.original.getBlock()))
            {
                for (Pair<String, Block> pair : replacements.get(ev.original.getBlock()))
                {
                    if(metadata.containsKey(ev.original.getBlock()))
                    {
                        for (Pair<String, Integer> pair1 : metadata.get(ev.original.getBlock()))
                        {
                            if (checkCondition(ev.biome, pair.left))
                            {
                                if (ev.original.getBlock() instanceof BlockStairs)
                                {
                                    ev.replacement = copyStairsState(ev.original, pair.right);
                                } else if (ev.original.getBlock() instanceof BlockSlab)
                                {
                                    ev.replacement = copySlabState(ev.original, pair.right);
                                } else
                                {
                                    if (checkCondition(ev.biome, pair1.left))
                                    {
                                        if (pair1.right == 0)
                                        {
                                            ev.replacement = pair.right.getDefaultState();
                                        } else
                                        {
                                            ev.replacement = pair.right.getStateFromMeta(pair1.right);
                                        }
                                    }
                                    }
                                ev.setResult(Event.Result.DENY);
                            }
                        }
                    }
                }
            }
        }
    }


    private static boolean checkCondition(BiomeGenBase biome, String condition)
    {
        if (biome == null || condition == null)
            return false;
        try
        {
            if (condition.startsWith("b:"))
            {
                String identifier = condition.substring("b:".length());
                return identifier.equalsIgnoreCase(biome.biomeName) || identifier.equals(String.valueOf(biome.biomeID));
            }
            if (condition.startsWith("t:"))
            {
                String identifier = condition.substring("t:".length());
                Type type = Type.valueOf(identifier);
                return type != null && BiomeDictionary.isBiomeOfType(biome, type);
            }
            return false;
        } catch (NullPointerException e)
        {
            FloraSoma.instance.log.warn("NullPointerException when replacing blocks:");
            e.printStackTrace();
            FloraSoma.instance.log.warn("Something was NULL when replacing blocks.");
            FloraSoma.instance.log.warn("Biome class: " + biome.getClass() + "; Condition: " + condition);
            return false;
        }
    }

    private static IBlockState copyStairsState(IBlockState original, Block replacement)
    {
        return replacement.getDefaultState()
                .withProperty(BlockStairs.FACING, original.getValue(BlockStairs.FACING))
                .withProperty(BlockStairs.HALF, original.getValue(BlockStairs.HALF))
                .withProperty(BlockStairs.SHAPE, original.getValue(BlockStairs.SHAPE));
    }

    private static IBlockState copySlabState(IBlockState original, Block replacement)
    {
        return replacement.getDefaultState()
                .withProperty(BlockSlab.HALF, original.getValue(BlockSlab.HALF));
    }
}

