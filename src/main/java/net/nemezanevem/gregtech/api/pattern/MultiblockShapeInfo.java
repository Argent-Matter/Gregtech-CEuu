package net.nemezanevem.gregtech.api.pattern;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.nemezanevem.gregtech.api.tileentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.tileentity.MetaTileEntityHolder;
import net.nemezanevem.gregtech.api.util.BlockInfo;
import net.nemezanevem.gregtech.common.block.MetaBlocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiblockShapeInfo {

    private final BlockInfo[][][] blocks; //[z][y][x]

    public MultiblockShapeInfo(BlockInfo[][][] blocks) {
        this.blocks = blocks;
    }

    public BlockInfo[][][] getBlocks() {
        return blocks;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private List<String[]> shape = new ArrayList<>();
        private Map<Character, BlockInfo> symbolMap = new HashMap<>();

        public Builder aisle(String... data) {
            this.shape.add(data);
            return this;
        }

        public Builder where(char symbol, BlockInfo value) {
            this.symbolMap.put(symbol, value);
            return this;
        }

        public Builder where(char symbol, BlockState blockState) {
            return where(symbol, new BlockInfo(blockState));
        }

        public Builder where(char symbol, MetaTileEntity tileEntity, Direction frontSide) {
            MetaTileEntityHolder holder = new MetaTileEntityHolder();
            holder.setMetaTileEntity(tileEntity);
            holder.getMetaTileEntity().onPlacement();
            holder.getMetaTileEntity().setFrontFacing(frontSide);
            return where(symbol, new BlockInfo(MetaBlocks.MACHINE.defaultBlockState(), holder));
        }

        /**
         * @param partSupplier Should supply either a MetaTileEntity or an BlockState.
         */
        public Builder where(char symbol, Supplier<?> partSupplier, Direction frontSideIfTE) {
            Object part = partSupplier.get();
            if (part instanceof BlockState) {
                return where(symbol, (BlockState) part);
            } else if (part instanceof MetaTileEntity) {
                return where(symbol, (MetaTileEntity) part, frontSideIfTE);
            } else throw new IllegalArgumentException("Supplier must supply either a MetaTileEntity or an BlockState! Actual: " + part.getClass());
        }

        private BlockInfo[][][] bakeArray() {
            BlockInfo[][][] blockInfos = (BlockInfo[][][]) Array.newInstance(BlockInfo.class, shape.get(0)[0].length(), shape.get(0).length, shape.size());
            for (int z = 0; z < blockInfos.length; z++) { //z
                String[] aisleEntry = shape.get(z);
                for (int y = 0; y < shape.get(0).length; y++) {
                    String columnEntry = aisleEntry[y];
                    for (int x = 0; x < columnEntry.length(); x++) {
                        BlockInfo info = symbolMap.getOrDefault(columnEntry.charAt(x), BlockInfo.EMPTY);
                        BlockEntity tileEntity = info.getBlockEntity();
                        if (tileEntity != null) {
                            MetaTileEntityHolder holder = (MetaTileEntityHolder) tileEntity;
                            final MetaTileEntity mte = holder.getMetaTileEntity();
                            holder = new MetaTileEntityHolder();
                            holder.setMetaTileEntity(mte);
                            holder.getMetaTileEntity().onPlacement();
                            holder.getMetaTileEntity().setFrontFacing(mte.getFrontFacing());
                            info = new BlockInfo(info.getBlockState(), holder);
                        }
                        blockInfos[x][y][z] = info;
                    }
                }
            }
            return blockInfos;
        }

        public Builder shallowCopy() {
            Builder builder = new Builder();
            builder.shape = new ArrayList<>(this.shape);
            builder.symbolMap = new HashMap<>(this.symbolMap);
            return builder;
        }

        public MultiblockShapeInfo build() {
            return new MultiblockShapeInfo(bakeArray());
        }

    }

}
