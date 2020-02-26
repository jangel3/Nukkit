package cn.nukkit.item;

import cn.nukkit.block.BlockSignDarkOak;

/**
 * author: jangel3
 * Nukkit Project
 */
public class ItemSignDarkOak extends Item {

    public ItemSignDarkOak() {
        this(0, 1);
    }

    public ItemSignDarkOak(Integer meta) {
        this(meta, 1);
    }

    public ItemSignDarkOak(Integer meta, int count) {
        super(DARK_OAK_SIGN, 0, count, "Dark Oak Sign");
        this.block = new BlockSignDarkOak();
    }

    @Override
    public int getMaxStackSize() {
        return 16;
    }
}
