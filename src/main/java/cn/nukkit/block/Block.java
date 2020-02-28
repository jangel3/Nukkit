package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.item.ItemTool;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.Level;
import cn.nukkit.level.MovingObjectPosition;
import cn.nukkit.level.Position;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import cn.nukkit.metadata.MetadataValue;
import cn.nukkit.metadata.Metadatable;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.BlockColor;

import java.lang.reflect.Constructor;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public abstract class Block extends Position implements Metadatable, Cloneable, AxisAlignedBB, BlockID {
    public static Class[] list = null;
    public static Dictionary<Integer, Class> dicList = null;
    public static Dictionary<Integer, Block> fullList = null;
    public static Dictionary<Integer, Integer> light = null;
    public static Dictionary<Integer, Integer> lightFilter = null;
    public static Dictionary<Integer, Boolean> solid = null;
    public static Dictionary<Integer, Double> hardness = null;
    public static Dictionary<Integer, Boolean> transparent = null;
    /**
     * if a block has can have variants
     */
    public static Dictionary<Integer, Boolean> hasMeta = null;

    protected Block() {}

    @SuppressWarnings("unchecked")
    public static void init() {
        if (dicList == null) {
            dicList = new Hashtable<Integer, Class>();
            
            //list = new Class[256];
            fullList = new Hashtable<Integer, Block>();
            light = new Hashtable<Integer, Integer>();
            lightFilter = new Hashtable<Integer, Integer>();
            solid = new Hashtable<Integer, Boolean>();
            hardness = new Hashtable<Integer, Double>();
            transparent = new Hashtable<Integer, Boolean>();
            hasMeta = new Hashtable<Integer, Boolean>();

            for (int id = 0; id < 256; id++) {
                determineBlockClass(id);
            }

            for (int id = 0; id > -256; id--) {
                determineBlockClass(id);
            }
        }
    }

    public static Block get(int id) {
        return fullList.get(id << 4).clone();
    }

    public static Block get(int id, Integer meta) {
        if (meta != null) {
            return fullList.get((id << 4) + meta).clone();
        } else {
            return fullList.get(id << 4).clone();
        }
    }

    @SuppressWarnings("unchecked")
    public static Block get(int id, Integer meta, Position pos) {
        Block block = fullList.get((id << 4) | (meta == null ? 0 : meta)).clone();
        if (pos != null) {
            block.x = pos.x;
            block.y = pos.y;
            block.z = pos.z;
            block.level = pos.level;
        }
        return block;
    }

    public static Block get(int id, int data) {
        return fullList.get((id << 4) + data).clone();
    }

    public static Block get(int fullId, Level level, int x, int y, int z) {
        Block block = fullList.get(fullId).clone();
        block.x = x;
        block.y = y;
        block.z = z;
        block.level = level;
        return block;
    }

    public boolean place(Item item, Block block, Block target, BlockFace face, double fx, double fy, double fz, Player player) {
        return this.getLevel().setBlock(this, this, true, true);
    }

    //http://minecraft.gamepedia.com/Breaking
    public boolean canHarvestWithHand() {  //used for calculating breaking time
        return true;
    }

    public boolean isBreakable(Item item) {
        return true;
    }

    public int tickRate() {
        return 10;
    }

    public boolean onBreak(Item item) {
        return this.getLevel().setBlock(this, new BlockAir(), true, true);
    }

    public int onUpdate(int type) {
        return 0;
    }

    public boolean onActivate(Item item) {
        return this.onActivate(item, null);
    }

    public boolean onActivate(Item item, Player player) {
        return false;
    }

    public double getHardness() {
        return 10;
    }

    public double getResistance() {
        return 1;
    }

    public int getBurnChance() {
        return 0;
    }

    public int getBurnAbility() {
        return 0;
    }

    public int getToolType() {
        return ItemTool.TYPE_NONE;
    }

    public double getFrictionFactor() {
        return 0.6;
    }

    public int getLightLevel() {
        return 0;
    }

    public boolean canBePlaced() {
        return true;
    }

    public boolean canBeReplaced() {
        return false;
    }

    public boolean isTransparent() {
        return false;
    }

    public boolean isSolid() {
        return true;
    }

    public boolean canBeFlowedInto() {
        return false;
    }

    public boolean canBeActivated() {
        return false;
    }

    public boolean hasEntityCollision() {
        return false;
    }

    public boolean canPassThrough() {
        return false;
    }

    public boolean canBePushed() {
        return true;
    }

    public boolean hasComparatorInputOverride() {
        return false;
    }

    public int getComparatorInputOverride() {
        return 0;
    }

    public boolean canBeClimbed() {
        return false;
    }

    public BlockColor getColor() {
        return BlockColor.VOID_BLOCK_COLOR;
    }

    public abstract String getName();

    public abstract int getId();

    /**
     * The full id is a combination of the id and data.
     * @return full id
     */
    public int getFullId() {
        return (getId() << 4);
    }

    public void addVelocityToEntity(Entity entity, Vector3 vector) {

    }

    public int getDamage() {
        return 0;
    }

    public void setDamage(int meta) {
        // Do nothing
    }

    public final void setDamage(Integer meta) {
        setDamage((meta == null ? 0 : meta & 0x0f));
    }

    final public void position(Position v) {
        this.x = (int) v.x;
        this.y = (int) v.y;
        this.z = (int) v.z;
        this.level = v.level;
    }

    public Item[] getDrops(Item item) {
        if (this.getId() < 0 || this.getId() > list.length) { //Unknown blocks
            return new Item[0];
        } else {
            return new Item[]{
                    this.toItem()
            };
        }
    }

    private static double toolBreakTimeBonus0(
            int toolType, int toolTier, boolean isWoolBlock, boolean isCobweb) {
        if (toolType == ItemTool.TYPE_SWORD) return isCobweb ? 15.0 : 1.0;
        if (toolType == ItemTool.TYPE_SHEARS) return isWoolBlock ? 5.0 : 15.0;
        if (toolType == ItemTool.TYPE_NONE) return 1.0;
        switch (toolTier) {
            case ItemTool.TIER_WOODEN:
                return 2.0;
            case ItemTool.TIER_STONE:
                return 4.0;
            case ItemTool.TIER_IRON:
                return 6.0;
            case ItemTool.TIER_DIAMOND:
                return 8.0;
            case ItemTool.TIER_GOLD:
                return 12.0;
            default:
                return 1.0;
        }
    }

    private static double speedBonusByEfficiencyLore0(int efficiencyLoreLevel) {
        if (efficiencyLoreLevel == 0) return 0;
        return efficiencyLoreLevel * efficiencyLoreLevel + 1;
    }

    private static double speedRateByHasteLore0(int hasteLoreLevel) {
        return 1.0 + (0.2 * hasteLoreLevel);
    }

    private static int toolType0(Item item) {
        if (item.isSword()) return ItemTool.TYPE_SWORD;
        if (item.isShovel()) return ItemTool.TYPE_SHOVEL;
        if (item.isPickaxe()) return ItemTool.TYPE_PICKAXE;
        if (item.isAxe()) return ItemTool.TYPE_AXE;
        if (item.isShears()) return ItemTool.TYPE_SHEARS;
        return ItemTool.TYPE_NONE;
    }

    private static boolean correctTool0(int blockToolType, Item item) {
        return (blockToolType == ItemTool.TYPE_SWORD && item.isSword()) ||
                (blockToolType == ItemTool.TYPE_SHOVEL && item.isShovel()) ||
                (blockToolType == ItemTool.TYPE_PICKAXE && item.isPickaxe()) ||
                (blockToolType == ItemTool.TYPE_AXE && item.isAxe()) ||
                (blockToolType == ItemTool.TYPE_SHEARS && item.isShears()) ||
                blockToolType == ItemTool.TYPE_NONE;
    }

    //http://minecraft.gamepedia.com/Breaking
    private static double breakTime0(double blockHardness, boolean correctTool, boolean canHarvestWithHand,
                                     int blockId, int toolType, int toolTier, int efficiencyLoreLevel, int hasteEffectLevel,
                                     boolean insideOfWaterWithoutAquaAffinity, boolean outOfWaterButNotOnGround) {
        double baseTime = ((correctTool || canHarvestWithHand) ? 1.5 : 5.0) * blockHardness;
        double speed = 1.0 / baseTime;
        boolean isWoolBlock = blockId == Block.WOOL, isCobweb = blockId == Block.COBWEB;
        if (correctTool) speed *= toolBreakTimeBonus0(toolType, toolTier, isWoolBlock, isCobweb);
        speed += speedBonusByEfficiencyLore0(efficiencyLoreLevel);
        speed *= speedRateByHasteLore0(hasteEffectLevel);
        if (insideOfWaterWithoutAquaAffinity) speed *= 0.2;
        if (outOfWaterButNotOnGround) speed *= 0.2;
        return 1.0 / speed;
    }

    public double getBreakTime(Item item, Player player) {
        Objects.requireNonNull(item, "getBreakTime: Item can not be null");
        Objects.requireNonNull(player, "getBreakTime: Player can not be null");
        double blockHardness = getHardness();
        boolean correctTool = correctTool0(getToolType(), item);
        boolean canHarvestWithHand = canHarvestWithHand();
        int blockId = getId();
        int itemToolType = toolType0(item);
        int itemTier = item.getTier();
        int efficiencyLoreLevel = Optional.ofNullable(item.getEnchantment(Enchantment.ID_EFFICIENCY))
                .map(Enchantment::getLevel).orElse(0);
        int hasteEffectLevel = Optional.ofNullable(player.getEffect(Effect.HASTE))
                .map(Effect::getAmplifier).orElse(0);
        boolean insideOfWaterWithoutAquaAffinity = player.isInsideOfWater() &&
                Optional.ofNullable(player.getInventory().getHelmet().getEnchantment(Enchantment.ID_WATER_WORKER))
                        .map(Enchantment::getLevel).map(l -> l >= 1).orElse(false);
        boolean outOfWaterButNotOnGround = (!player.isInsideOfWater()) && (!player.isOnGround());
        return breakTime0(blockHardness, correctTool, canHarvestWithHand, blockId, itemToolType, itemTier,
                efficiencyLoreLevel, hasteEffectLevel, insideOfWaterWithoutAquaAffinity, outOfWaterButNotOnGround);
    }

    /**
     * @deprecated This function is lack of Player class and is not accurate enough, use #getBreakTime(Item, Player)
     * @param item item used
     * @return break time
     */
    @Deprecated
    public double getBreakTime(Item item) {
        double base = this.getHardness() * 1.5;
        if (this.canBeBrokenWith(item)) {
            if (this.getToolType() == ItemTool.TYPE_SHEARS && item.isShears()) {
                base /= 15;
            } else if (
                    (this.getToolType() == ItemTool.TYPE_PICKAXE && item.isPickaxe()) ||
                            (this.getToolType() == ItemTool.TYPE_AXE && item.isAxe()) ||
                            (this.getToolType() == ItemTool.TYPE_SHOVEL && item.isShovel())
                    ) {
                int tier = item.getTier();
                switch (tier) {
                    case ItemTool.TIER_WOODEN:
                        base /= 2;
                        break;
                    case ItemTool.TIER_STONE:
                        base /= 4;
                        break;
                    case ItemTool.TIER_IRON:
                        base /= 6;
                        break;
                    case ItemTool.TIER_DIAMOND:
                        base /= 8;
                        break;
                    case ItemTool.TIER_GOLD:
                        base /= 12;
                        break;
                }
            }
        } else {
            base *= 3.33;
        }

        if (item.isSword()) {
            base *= 0.5;
        }

        return base;
    }

    public boolean canBeBrokenWith(Item item) {
        return this.getHardness() != -1;
    }

    public Block getSide(BlockFace face) {
        if (this.isValid()) {
            return this.getLevel().getBlock((int) x + face.getXOffset(), (int) y + face.getYOffset(), (int) z + face.getZOffset());
        }
        return this.getSide(face, 1);
    }

    public Block getSide(BlockFace face, int step) {
        if (this.isValid()) {
            if (step == 1) {
                return this.getLevel().getBlock((int) x + face.getXOffset(), (int) y + face.getYOffset(), (int) z + face.getZOffset());
            } else {
                return this.getLevel().getBlock((int) x + face.getXOffset() * step, (int) y + face.getYOffset() * step, (int) z + face.getZOffset() * step);
            }
        }
        Block block = Block.get(Item.AIR, 0);
        block.x = (int) x + face.getXOffset() * step;
        block.y = (int) y + face.getYOffset() * step;
        block.z = (int) z + face.getZOffset() * step;
        return block;
    }

    public Block up() {
        return up(1);
    }

    public Block up(int step) {
        return getSide(BlockFace.UP, step);
    }

    public Block down() {
        return down(1);
    }

    public Block down(int step) {
        return getSide(BlockFace.DOWN, step);
    }

    public Block north() {
        return north(1);
    }

    public Block north(int step) {
        return getSide(BlockFace.NORTH, step);
    }

    public Block south() {
        return south(1);
    }

    public Block south(int step) {
        return getSide(BlockFace.SOUTH, step);
    }

    public Block east() {
        return east(1);
    }

    public Block east(int step) {
        return getSide(BlockFace.EAST, step);
    }

    public Block west() {
        return west(1);
    }

    public Block west(int step) {
        return getSide(BlockFace.WEST, step);
    }

    @Override
    public String toString() {
        return "Block[" + this.getName() + "] (" + this.getId() + ":" + this.getDamage() + ")";
    }

    public boolean collidesWithBB(AxisAlignedBB bb) {
        return collidesWithBB(bb, false);
    }

    public boolean collidesWithBB(AxisAlignedBB bb, boolean collisionBB) {
        AxisAlignedBB bb1 = collisionBB ? this.getCollisionBoundingBox() : this.getBoundingBox();
        return bb1 != null && bb.intersectsWith(bb1);
    }

    public void onEntityCollide(Entity entity) {

    }

    public AxisAlignedBB getBoundingBox() {
        return this.recalculateBoundingBox();
    }

    public AxisAlignedBB getCollisionBoundingBox() {
        return this.recalculateCollisionBoundingBox();
    }

    protected AxisAlignedBB recalculateBoundingBox() {
        return this;
    }

    @Override
    public double getMinX() {
        return this.x;
    }

    @Override
    public double getMinY() {
        return this.y;
    }

    @Override
    public double getMinZ() {
        return this.z;
    }

    @Override
    public double getMaxX() {
        return this.x + 1;
    }

    @Override
    public double getMaxY() {
        return this.y + 1;
    }

    @Override
    public double getMaxZ() {
        return this.z + 1;
    }

    protected AxisAlignedBB recalculateCollisionBoundingBox() {
        return getBoundingBox();
    }

    public MovingObjectPosition calculateIntercept(Vector3 pos1, Vector3 pos2) {
        AxisAlignedBB bb = this.getBoundingBox();
        if (bb == null) {
            return null;
        }

        Vector3 v1 = pos1.getIntermediateWithXValue(pos2, bb.getMinX());
        Vector3 v2 = pos1.getIntermediateWithXValue(pos2, bb.getMaxX());
        Vector3 v3 = pos1.getIntermediateWithYValue(pos2, bb.getMinY());
        Vector3 v4 = pos1.getIntermediateWithYValue(pos2, bb.getMaxY());
        Vector3 v5 = pos1.getIntermediateWithZValue(pos2, bb.getMinZ());
        Vector3 v6 = pos1.getIntermediateWithZValue(pos2, bb.getMaxZ());

        if (v1 != null && !bb.isVectorInYZ(v1)) {
            v1 = null;
        }

        if (v2 != null && !bb.isVectorInYZ(v2)) {
            v2 = null;
        }

        if (v3 != null && !bb.isVectorInXZ(v3)) {
            v3 = null;
        }

        if (v4 != null && !bb.isVectorInXZ(v4)) {
            v4 = null;
        }

        if (v5 != null && !bb.isVectorInXY(v5)) {
            v5 = null;
        }

        if (v6 != null && !bb.isVectorInXY(v6)) {
            v6 = null;
        }

        Vector3 vector = v1;

        if (v2 != null && (vector == null || pos1.distanceSquared(v2) < pos1.distanceSquared(vector))) {
            vector = v2;
        }

        if (v3 != null && (vector == null || pos1.distanceSquared(v3) < pos1.distanceSquared(vector))) {
            vector = v3;
        }

        if (v4 != null && (vector == null || pos1.distanceSquared(v4) < pos1.distanceSquared(vector))) {
            vector = v4;
        }

        if (v5 != null && (vector == null || pos1.distanceSquared(v5) < pos1.distanceSquared(vector))) {
            vector = v5;
        }

        if (v6 != null && (vector == null || pos1.distanceSquared(v6) < pos1.distanceSquared(vector))) {
            vector = v6;
        }

        if (vector == null) {
            return null;
        }

        int f = -1;

        if (vector == v1) {
            f = 4;
        } else if (vector == v2) {
            f = 5;
        } else if (vector == v3) {
            f = 0;
        } else if (vector == v4) {
            f = 1;
        } else if (vector == v5) {
            f = 2;
        } else if (vector == v6) {
            f = 3;
        }

        return MovingObjectPosition.fromBlock((int) this.x, (int) this.y, (int) this.z, f, vector.add(this.x, this.y, this.z));
    }

    public String getSaveId() {
        String name = getClass().getName();
        return name.substring(16);
    }

    @Override
    public void setMetadata(String metadataKey, MetadataValue newMetadataValue) throws Exception {
        if (this.getLevel() != null) {
            this.getLevel().getBlockMetadata().setMetadata(this, metadataKey, newMetadataValue);
        }
    }

    @Override
    public List<MetadataValue> getMetadata(String metadataKey) throws Exception {
        if (this.getLevel() != null) {
            return this.getLevel().getBlockMetadata().getMetadata(this, metadataKey);

        }
        return null;
    }

    @Override
    public boolean hasMetadata(String metadataKey) throws Exception {
        return this.getLevel() != null && this.getLevel().getBlockMetadata().hasMetadata(this, metadataKey);
    }

    @Override
    public void removeMetadata(String metadataKey, Plugin owningPlugin) throws Exception {
        if (this.getLevel() != null) {
            this.getLevel().getBlockMetadata().removeMetadata(this, metadataKey, owningPlugin);
        }
    }

    public Block clone() {
        return (Block) super.clone();
    }

    public int getWeakPower(BlockFace face) {
        return 0;
    }

    public int getStrongPower(BlockFace side) {
        return 0;
    }

    public boolean isPowerSource() {
        return false;
    }

    public String getLocationHash() {
        return this.getFloorX() + ":" + this.getFloorY() + ":" + this.getFloorZ();
    }

    public int getDropExp() {
        return 0;
    }

    public boolean isNormalBlock() {
        return !isTransparent() && isSolid() && !isPowerSource();
    }

    public static boolean equals(Block b1, Block b2) {
        return equals(b1, b2, true);
    }

    public static boolean equals(Block b1, Block b2, boolean checkDamage) {
        return b1 != null && b2 != null && b1.getId() == b2.getId() && (!checkDamage || b1.getDamage() == b2.getDamage());
    }

    public Item toItem() {
        return new ItemBlock(this, this.getDamage(), 1);
    }

    public boolean canSilkTouch() {
        return false;
    }

    // Helper
    private static void determineBlockClass(int id){
        Class c = dicList.get(id);
        if (c != null) {
            Block block;
            try {
                block = (Block) c.newInstance();
                try {
                    Constructor constructor = c.getDeclaredConstructor(int.class);
                    constructor.setAccessible(true);
                    for (int data = 0; data < 16; ++data) {
                        fullList.put((id << 4) | data, (Block) constructor.newInstance(data));
                    }
                    hasMeta.put(id, true);
                } catch (NoSuchMethodException ignore) {
                    for (int data = 0; data < 16; ++data) {
                        fullList.put((id << 4) | data, block);
                    }
                }
            } catch (Exception e) {
                Server.getInstance().getLogger().error("Error while registering " + c.getName(), e);
                for (int data = 0; data < 16; ++data) {
                    fullList.put((id << 4) | data, new BlockUnknown(id, data));
                }
                return;
            }

            solid.put(id, block.isSolid());
            transparent.put(id, block.isTransparent());
            hardness.put(id, block.getHardness());
            light.put(id, block.getLightLevel());

            if (block.isSolid()) {
                if (block.isTransparent()) {
                    if (block instanceof BlockLiquid || block instanceof BlockIce) {
                        lightFilter.put(id, 2);
                    } else {
                        lightFilter.put(id, 1);
                    }
                } else {
                    lightFilter.put(id, 15);
                }
            } else {
                lightFilter.put(id, 1);
            }
        } else {
            lightFilter.put(id, 1);
            for (int data = 0; data < 16; ++data) {
                fullList.put((id << 4) | data, new BlockUnknown(id, data));
            }
        }

    }

    private static void loadClasses(){
        dicList.put(AIR,BlockAir.class); //0
        dicList.put(STONE, BlockStone.class); //1
        dicList.put(GRASS, BlockGrass.class); //2
        dicList.put(DIRT, BlockDirt.class); //3
        dicList.put(COBBLESTONE, BlockCobblestone.class); //4
        dicList.put(PLANKS, BlockPlanks.class); //5
        dicList.put(SAPLING, BlockSapling.class); //6
        dicList.put(BEDROCK, BlockBedrock.class); //7
        dicList.put(WATER, BlockWater.class); //8
        dicList.put(STILL_WATER, BlockWaterStill.class); //9
        dicList.put(LAVA, BlockLava.class); //10
        dicList.put(STILL_LAVA, BlockLavaStill.class); //11
        dicList.put(SAND, BlockSand.class); //12
        dicList.put(GRAVEL, BlockGravel.class); //13
        dicList.put(GOLD_ORE, BlockOreGold.class); //14
        dicList.put(IRON_ORE, BlockOreIron.class); //15
        dicList.put(COAL_ORE, BlockOreCoal.class); //16
        dicList.put(WOOD, BlockWood.class); //17
        dicList.put(LEAVES, BlockLeaves.class); //18
        dicList.put(SPONGE, BlockSponge.class); //19
        dicList.put(GLASS, BlockGlass.class); //20
        dicList.put(LAPIS_ORE, BlockOreLapis.class); //21
        dicList.put(LAPIS_BLOCK, BlockLapis.class); //22
        dicList.put(DISPENSER, BlockDispenser.class); //23
        dicList.put(SANDSTONE, BlockSandstone.class); //24
        dicList.put(NOTEBLOCK, BlockNoteblock.class); //25
        dicList.put(BED_BLOCK, BlockBed.class); //26
        dicList.put(POWERED_RAIL, BlockRailPowered.class); //27
        dicList.put(DETECTOR_RAIL, BlockRailDetector.class); //28
        dicList.put(STICKY_PISTON, BlockPistonSticky.class); //29
        dicList.put(COBWEB, BlockCobweb.class); //30
        dicList.put(TALL_GRASS, BlockTallGrass.class); //31
        dicList.put(DEAD_BUSH, BlockDeadBush.class); //32
        dicList.put(PISTON, BlockPiston.class); //33
        dicList.put(PISTON_HEAD, BlockPistonHead.class); //34
        dicList.put(WOOL, BlockWool.class); //35
        dicList.put(DANDELION, BlockDandelion.class); //37
        dicList.put(FLOWER, BlockFlower.class); //38
        dicList.put(BROWN_MUSHROOM, BlockMushroomBrown.class); //39
        dicList.put(RED_MUSHROOM, BlockMushroomRed.class); //40
        dicList.put(GOLD_BLOCK, BlockGold.class); //41
        dicList.put(IRON_BLOCK, BlockIron.class); //42
        dicList.put(DOUBLE_STONE_SLAB, BlockDoubleSlabStone.class); //43
        dicList.put(STONE_SLAB, BlockSlabStone.class); //44
        dicList.put(BRICKS_BLOCK, BlockBricks.class); //45
        dicList.put(TNT, BlockTNT.class); //46
        dicList.put(BOOKSHELF, BlockBookshelf.class); //47
        dicList.put(MOSS_STONE, BlockMossStone.class); //48
        dicList.put(OBSIDIAN, BlockObsidian.class); //49
        dicList.put(TORCH, BlockTorch.class); //50
        dicList.put(FIRE, BlockFire.class); //51
        dicList.put(MONSTER_SPAWNER, BlockMobSpawner.class); //52
        dicList.put(WOOD_STAIRS, BlockStairsWood.class); //53
        dicList.put(CHEST, BlockChest.class); //54
        dicList.put(REDSTONE_WIRE, BlockRedstoneWire.class); //55
        dicList.put(DIAMOND_ORE, BlockOreDiamond.class); //56
        dicList.put(DIAMOND_BLOCK, BlockDiamond.class); //57
        dicList.put(WORKBENCH, BlockCraftingTable.class); //58
        dicList.put(WHEAT_BLOCK, BlockWheat.class); //59
        dicList.put(FARMLAND, BlockFarmland.class); //60
        dicList.put(FURNACE, BlockFurnace.class); //61
        dicList.put(BURNING_FURNACE, BlockFurnaceBurning.class); //62
        //dicList.put(SIGN_POST, BlockSignOak.class); //63
        dicList.put(WOOD_DOOR_BLOCK, BlockDoorWood.class); //64
        dicList.put(LADDER, BlockLadder.class); //65
        dicList.put(RAIL, BlockRail.class); //66
        dicList.put(COBBLESTONE_STAIRS, BlockStairsCobblestone.class); //67
        dicList.put(WALL_SIGN, BlockWallSign.class); //68
        dicList.put(LEVER, BlockLever.class); //69
        dicList.put(STONE_PRESSURE_PLATE, BlockPressurePlateStone.class); //70
        dicList.put(IRON_DOOR_BLOCK, BlockDoorIron.class); //71
        dicList.put(WOODEN_PRESSURE_PLATE, BlockPressurePlateWood.class); //72
        dicList.put(REDSTONE_ORE, BlockOreRedstone.class); //73
        dicList.put(GLOWING_REDSTONE_ORE, BlockOreRedstoneGlowing.class); //74
        dicList.put(UNLIT_REDSTONE_TORCH, BlockRedstoneTorchUnlit.class);
        dicList.put(REDSTONE_TORCH, BlockRedstoneTorch.class); //76
        dicList.put(STONE_BUTTON, BlockButtonStone.class); //77
        dicList.put(SNOW_LAYER, BlockSnowLayer.class); //78
        dicList.put(ICE, BlockIce.class); //79
        dicList.put(SNOW_BLOCK, BlockSnow.class); //80
        dicList.put(CACTUS, BlockCactus.class); //81
        dicList.put(CLAY_BLOCK, BlockClay.class); //82
        dicList.put(SUGARCANE_BLOCK, BlockSugarcane.class); //83
        dicList.put(JUKEBOX, BlockJukebox.class); //84
        dicList.put(FENCE, BlockFence.class); //85
        dicList.put(PUMPKIN, BlockPumpkin.class); //86
        dicList.put(NETHERRACK, BlockNetherrack.class); //87
        dicList.put(SOUL_SAND, BlockSoulSand.class); //88
        dicList.put(GLOWSTONE_BLOCK, BlockGlowstone.class); //89
        dicList.put(NETHER_PORTAL, BlockNetherPortal.class); //90
        dicList.put(LIT_PUMPKIN, BlockPumpkinLit.class); //91
        dicList.put(CAKE_BLOCK, BlockCake.class); //92
        dicList.put(UNPOWERED_REPEATER, BlockRedstoneRepeaterUnpowered.class); //93
        dicList.put(POWERED_REPEATER, BlockRedstoneRepeaterPowered.class); //94
        dicList.put(INVISIBLE_BEDROCK, BlockBedrockInvisible.class); //95
        dicList.put(TRAPDOOR, BlockTrapdoor.class); //96
        dicList.put(MONSTER_EGG, BlockMonsterEgg.class); //97
        dicList.put(STONE_BRICKS, BlockBricksStone.class); //98
        dicList.put(BROWN_MUSHROOM_BLOCK, BlockHugeMushroomBrown.class); //99
        dicList.put(RED_MUSHROOM_BLOCK, BlockHugeMushroomRed.class); //100
        dicList.put(IRON_BARS, BlockIronBars.class); //101
        dicList.put(GLASS_PANE, BlockGlassPane.class); //102
        dicList.put(MELON_BLOCK, BlockMelon.class); //103
        dicList.put(PUMPKIN_STEM, BlockStemPumpkin.class); //104
        dicList.put(MELON_STEM, BlockStemMelon.class); //105
        dicList.put(VINE, BlockVine.class); //106
        dicList.put(FENCE_GATE, BlockFenceGate.class); //107
        dicList.put(BRICK_STAIRS, BlockStairsBrick.class); //108
        dicList.put(STONE_BRICK_STAIRS, BlockStairsStoneBrick.class); //109
        dicList.put(MYCELIUM, BlockMycelium.class); //110
        dicList.put(WATER_LILY, BlockWaterLily.class); //111
        dicList.put(NETHER_BRICKS, BlockBricksNether.class); //112
        dicList.put(NETHER_BRICK_FENCE, BlockFenceNetherBrick.class); //113
        dicList.put(NETHER_BRICKS_STAIRS, BlockStairsNetherBrick.class); //114
        dicList.put(NETHER_WART_BLOCK, BlockNetherWart.class); //115
        dicList.put(ENCHANTING_TABLE, BlockEnchantingTable.class); //116
        dicList.put(BREWING_STAND_BLOCK, BlockBrewingStand.class); //117
        dicList.put(CAULDRON_BLOCK, BlockCauldron.class); //118
        dicList.put(END_PORTAL, BlockEndPortal.class); //119
        dicList.put(END_PORTAL_FRAME, BlockEndPortalFrame.class); //120
        dicList.put(END_STONE, BlockEndStone.class); //121
        dicList.put(DRAGON_EGG, BlockDragonEgg.class); //122
        dicList.put(REDSTONE_LAMP, BlockRedstoneLamp.class); //123
        dicList.put(LIT_REDSTONE_LAMP, BlockRedstoneLampLit.class); //124
        //TODO: dicList.put(DROPPER, BlockDropper.class); //125
        dicList.put(ACTIVATOR_RAIL, BlockRailActivator.class); //126
        dicList.put(COCOA, BlockCocoa.class); //127
        dicList.put(SANDSTONE_STAIRS, BlockStairsSandstone.class); //128
        dicList.put(EMERALD_ORE, BlockOreEmerald.class); //129
        dicList.put(ENDER_CHEST, BlockEnderChest.class); //130
        dicList.put(TRIPWIRE_HOOK, BlockTripWireHook.class);
        dicList.put(TRIPWIRE, BlockTripWire.class); //132
        dicList.put(EMERALD_BLOCK, BlockEmerald.class); //133
        dicList.put(SPRUCE_WOOD_STAIRS, BlockStairsSpruce.class); //134
        dicList.put(BIRCH_WOOD_STAIRS, BlockStairsBirch.class); //135
        dicList.put(JUNGLE_WOOD_STAIRS, BlockStairsJungle.class); //136

        dicList.put(BEACON, BlockBeacon.class); //138
        dicList.put(STONE_WALL, BlockWall.class); //139
        dicList.put(FLOWER_POT_BLOCK, BlockFlowerPot.class); //140
        dicList.put(CARROT_BLOCK, BlockCarrot.class); //141
        dicList.put(POTATO_BLOCK, BlockPotato.class); //142
        dicList.put(WOODEN_BUTTON, BlockButtonWooden.class); //143
        dicList.put(SKULL_BLOCK, BlockSkull.class); //144
        dicList.put(ANVIL, BlockAnvil.class); //145
        dicList.put(TRAPPED_CHEST, BlockTrappedChest.class); //146
        dicList.put(LIGHT_WEIGHTED_PRESSURE_PLATE, BlockWeightedPressurePlateLight.class); //147
        dicList.put(HEAVY_WEIGHTED_PRESSURE_PLATE, BlockWeightedPressurePlateHeavy.class); //148
        dicList.put(UNPOWERED_COMPARATOR, BlockRedstoneComparatorUnpowered.class); //149
        dicList.put(POWERED_COMPARATOR, BlockRedstoneComparatorPowered.class); //149
        dicList.put(DAYLIGHT_DETECTOR, BlockDaylightDetector.class); //151
        dicList.put(REDSTONE_BLOCK, BlockRedstone.class); //152
        dicList.put(QUARTZ_ORE, BlockOreQuartz.class); //153
        dicList.put(HOPPER_BLOCK, BlockHopper.class); //154
        dicList.put(QUARTZ_BLOCK, BlockQuartz.class); //155
        dicList.put(QUARTZ_STAIRS, BlockStairsQuartz.class); //156
        dicList.put(DOUBLE_WOOD_SLAB, BlockDoubleSlabWood.class); //157
        dicList.put(WOOD_SLAB, BlockSlabWood.class); //158
        dicList.put(STAINED_TERRACOTTA, BlockTerracottaStained.class); //159
        dicList.put(STAINED_GLASS_PANE, BlockGlassPaneStained.class); //160

        dicList.put(LEAVES2, BlockLeaves2.class); //161
        dicList.put(WOOD2, BlockWood2.class); //162
        dicList.put(ACACIA_WOOD_STAIRS, BlockStairsAcacia.class); //163
        dicList.put(DARK_OAK_WOOD_STAIRS, BlockStairsDarkOak.class); //164
        dicList.put(SLIME_BLOCK, BlockSlime.class); //165

        dicList.put(IRON_TRAPDOOR, BlockTrapdoorIron.class); //167
        dicList.put(PRISMARINE, BlockPrismarine.class); //168
        dicList.put(SEA_LANTERN, BlockSeaLantern.class); //169
        dicList.put(HAY_BALE, BlockHayBale.class); //170
        dicList.put(CARPET, BlockCarpet.class); //171
        dicList.put(TERRACOTTA, BlockTerracotta.class); //172
        dicList.put(COAL_BLOCK, BlockCoal.class); //173
        dicList.put(PACKED_ICE, BlockIcePacked.class); //174
        dicList.put(DOUBLE_PLANT, BlockDoublePlant.class); //175
        dicList.put(STANDING_BANNER, BlockBanner.class); //176
        dicList.put(WALL_BANNER, BlockWallBanner.class); //177
        dicList.put(DAYLIGHT_DETECTOR_INVERTED, BlockDaylightDetectorInverted.class); //178
        dicList.put(RED_SANDSTONE, BlockRedSandstone.class); //179
        dicList.put(RED_SANDSTONE_STAIRS, BlockStairsRedSandstone.class); //180
        dicList.put(DOUBLE_RED_SANDSTONE_SLAB, BlockDoubleSlabRedSandstone.class); //181
        dicList.put(RED_SANDSTONE_SLAB, BlockSlabRedSandstone.class); //182
        dicList.put(FENCE_GATE_SPRUCE, BlockFenceGateSpruce.class); //183
        dicList.put(FENCE_GATE_BIRCH, BlockFenceGateBirch.class); //184
        dicList.put(FENCE_GATE_JUNGLE, BlockFenceGateJungle.class); //185
        dicList.put(FENCE_GATE_DARK_OAK, BlockFenceGateDarkOak.class); //186
        dicList.put(FENCE_GATE_ACACIA, BlockFenceGateAcacia.class); //187

        dicList.put(SPRUCE_DOOR_BLOCK, BlockDoorSpruce.class); //193
        dicList.put(BIRCH_DOOR_BLOCK, BlockDoorBirch.class); //194
        dicList.put(JUNGLE_DOOR_BLOCK, BlockDoorJungle.class); //195
        dicList.put(ACACIA_DOOR_BLOCK, BlockDoorAcacia.class); //196
        dicList.put(DARK_OAK_DOOR_BLOCK, BlockDoorDarkOak.class); //197
        dicList.put(GRASS_PATH, BlockGrassPath.class); //198
        dicList.put(ITEM_FRAME_BLOCK, BlockItemFrame.class); //199
        dicList.put(CHORUS_FLOWER, BlockChorusFlower.class); //200
        dicList.put(PURPUR_BLOCK, BlockPurpur.class); //201

        dicList.put(PURPUR_STAIRS, BlockStairsPurpur.class); //203
        
        dicList.put(UNDYED_SHULKER_BOX, BlockUndyedShulkerBox.class); //205
        dicList.put(END_BRICKS, BlockBricksEndStone.class); //206

        dicList.put(END_ROD, BlockEndRod.class); //208
        dicList.put(END_GATEWAY, BlockEndGateway.class); //209

        dicList.put(MAGMA, BlockMagma.class); //213
        dicList.put(BLOCK_NETHER_WART_BLOCK, BlockNetherWartBlock.class); //214
        dicList.put(RED_NETHER_BRICK, BlockBricksRedNether.class); //215
        dicList.put(BONE_BLOCK, BlockBone.class); //216

        dicList.put(SHULKER_BOX, BlockShulkerBox.class); //218
        dicList.put(PURPLE_GLAZED_TERRACOTTA, BlockTerracottaGlazedPurple.class); //219
        dicList.put(WHITE_GLAZED_TERRACOTTA, BlockTerracottaGlazedWhite.class); //220
        dicList.put(ORANGE_GLAZED_TERRACOTTA, BlockTerracottaGlazedOrange.class); //221
        dicList.put(MAGENTA_GLAZED_TERRACOTTA, BlockTerracottaGlazedMagenta.class); //222
        dicList.put(LIGHT_BLUE_GLAZED_TERRACOTTA, BlockTerracottaGlazedLightBlue.class); //223
        dicList.put(YELLOW_GLAZED_TERRACOTTA, BlockTerracottaGlazedYellow.class); //224
        dicList.put(LIME_GLAZED_TERRACOTTA, BlockTerracottaGlazedLime.class); //225
        dicList.put(PINK_GLAZED_TERRACOTTA, BlockTerracottaGlazedPink.class); //226
        dicList.put(GRAY_GLAZED_TERRACOTTA, BlockTerracottaGlazedGray.class); //227
        dicList.put(SILVER_GLAZED_TERRACOTTA, BlockTerracottaGlazedSilver.class); //228
        dicList.put(CYAN_GLAZED_TERRACOTTA, BlockTerracottaGlazedCyan.class); //229

        dicList.put(BLUE_GLAZED_TERRACOTTA, BlockTerracottaGlazedBlue.class); //231
        dicList.put(BROWN_GLAZED_TERRACOTTA, BlockTerracottaGlazedBrown.class); //232
        dicList.put(GREEN_GLAZED_TERRACOTTA, BlockTerracottaGlazedGreen.class); //233
        dicList.put(RED_GLAZED_TERRACOTTA, BlockTerracottaGlazedRed.class); //234
        dicList.put(BLACK_GLAZED_TERRACOTTA, BlockTerracottaGlazedBlack.class); //235
        dicList.put(CONCRETE, BlockConcrete.class); //236
        dicList.put(CONCRETE_POWDER, BlockConcretePowder.class); //237

        dicList.put(CHORUS_PLANT, BlockChorusPlant.class); //240
        dicList.put(STAINED_GLASS, BlockGlassStained.class); //241
        dicList.put(PODZOL, BlockPodzol.class); //243
        dicList.put(BEETROOT_BLOCK, BlockBeetroot.class); //244
        dicList.put(STONECUTTER, BlockStonecutter.class); //245
        dicList.put(GLOWING_OBSIDIAN, BlockObsidianGlowing.class); //246
        //dicList.put(NETHER_REACTOR, BlockNetherReactor.class); //247 Should not be removed

        //TODO: dicList.put(PISTON_EXTENSION, BlockPistonExtension.class); //250

        dicList.put(OBSERVER, BlockObserver.class); //251

        dicList.put(BLOCK_SCAFFOLD, BlockScaffold.class); //-165
    }
}
