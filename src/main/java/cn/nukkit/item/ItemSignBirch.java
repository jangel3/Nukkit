package cn.nukkit.item;

import cn.nukkit.block.BlockSignBirch;

/* 
 * author: jangel3
 * Nukkit Project
 */
public class ItemSignBirch extends Item{

    public ItemSignBirch(){
        this(0, 1);
    }

    public ItemSignBirch(Integer meta){
        this(meta, 1);
    }

    public ItemSignBirch(Integer meta, int count){
        super(BIRCH_SIGN, 0, count, "Birch Sign");
        this.block = new BlockSignBirch();
    }

    @Override
    public int getMaxStackSize(){
        return 16;
    }
}