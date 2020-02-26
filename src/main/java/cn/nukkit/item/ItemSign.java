package cn.nukkit.item;

import cn.nukkit.block.BlockSignOak;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class ItemSign extends Item {

    public ItemSign() {
        this(0, 1);
    }

    public ItemSign(Integer meta) {
        this(meta, 1);
    }

    public ItemSign(Integer meta, int count) {
        super(SIGN, 0, count, "Sign");
        this.block = new BlockSignOak();
    }

    @Override
    public int getMaxStackSize() {
        return 16;
    }
}
