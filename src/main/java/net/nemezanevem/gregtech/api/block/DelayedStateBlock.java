package net.nemezanevem.gregtech.api.block;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import javax.annotation.Nonnull;
import java.beans.JavaBean;

/**
 * This class allows lazy initialization of block state of block
 * Useful when you need some parameters from constructor to construct a BlockStateContainer
 * All child classes must call initBlockState() in their constructors
 */
public abstract class DelayedStateBlock extends Block {

    public DelayedStateBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    protected void initBlockState() {
        StateDefinition<Block, BlockState> stateContainer = createStateContainer();
        //ObfuscationReflectionHelper.<Block, StateDefinition<Block, BlockState>>setPrivateValue(Block.class, this, stateContainer, "f_49792_"); //this.stateDefinition
        this.stateDefinition = stateContainer;
        this.registerDefaultState(stateContainer.any());
    }

    protected abstract StateDefinition<Block, BlockState> createStateContainer();

    public RenderType getRenderType() {
        return RenderType.cutoutMipped();
    }

}
