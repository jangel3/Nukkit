package cn.nukkit.item;

import cn.nukkit.block.BlockSignPost;

/**
 * author: jangel3
 * Nukkit Project
 */
public class ItemDarkOakSign extends Item {

    public ItemDarkOakSign() {
        this(0, 1);
    }

    public ItemDarkOakSign(Integer meta) {
        this(meta, 1);
    }

    public ItemDarkOakSign(Integer meta, int count) {
        super(DARK_OAK_SIGN, 0, count, "Dark Oak Sign");
        this.block = new BlockDarkOakSign();
    }

    @Override
    public int getMaxStackSize() {
        return 16;
    }
}
