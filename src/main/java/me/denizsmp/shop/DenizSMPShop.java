package me.denizsmp.shop;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class DenizSMPShop extends JavaPlugin implements Listener {

    private Economy economy;
    private NamespacedKey actionKey;
    private NamespacedKey categoryKey;
    private NamespacedKey materialKey;

    private final Map<String, Map<Material, Double>> prices =
            new LinkedHashMap<>();

    @Override
    public void onEnable() {
        actionKey = new NamespacedKey(this, "action");
        categoryKey = new NamespacedKey(this, "category");
        materialKey = new NamespacedKey(this, "material");

        RegisteredServiceProvider<Economy> provider =
                getServer().getServicesManager()
                        .getRegistration(Economy.class);

        if (provider == null) {
            getLogger().severe("Vault Economy bulunamadi!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        economy = provider.getProvider();

        addCategory("CPVP", Material.DIAMOND_SWORD, "CPvP");
        addCategory("END", Material.END_STONE, "End");
        addCategory("NETHER", Material.NETHERRACK, "Nether");
        addCategory("FOOD", Material.COOKED_BEEF, "Yemek");

        addPrice("CPVP", Material.OBSIDIAN, 80);
        addPrice("CPVP", Material.END_CRYSTAL, 250);
        addPrice("CPVP", Material.RESPAWN_ANCHOR, 500);
        addPrice("CPVP", Material.GLOWSTONE, 50);
        addPrice("CPVP", Material.TOTEM_OF_UNDYING, 1500);
        addPrice("CPVP", Material.ENDER_PEARL, 100);
        addPrice("CPVP", Material.GOLDEN_APPLE, 300);
        addPrice("CPVP", Material.EXPERIENCE_BOTTLE, 50);
        addPrice("CPVP", Material.ARROW, 5);

        addPrice("END", Material.END_STONE, 8);
        addPrice("END", Material.END_CHEST, 2500);
        addPrice("END", Material.ENDER_PEARL, 100);
        addPrice("END", Material.DRAGON_BREATH, 1200);
        addPrice("END", Material.END_ROD, 100);
        addPrice("END", Material.CHORUS_FRUIT, 15);
        addPrice("END", Material.SHULKER_SHELL, 1000);
        addPrice("END", Material.SHULKER_BOX, 2500);

        addPrice("NETHER", Material.BLAZE_ROD, 200);
        addPrice("NETHER", Material.NETHER_WART, 20);
        addPrice("NETHER", Material.GLOWSTONE_DUST, 20);
        addPrice("NETHER", Material.MAGMA_CREAM, 25);
        addPrice("NETHER", Material.GHAST_TEAR, 500);
        addPrice("NETHER", Material.QUARTZ, 20);
        addPrice("NETHER", Material.SOUL_SAND, 40);
        addPrice("NETHER", Material.CRYING_OBSIDIAN, 200);

        addPrice("FOOD", Material.POTATO, 10);
        addPrice("FOOD", Material.SWEET_BERRIES, 10);
        addPrice("FOOD", Material.MELON_SLICE, 5);
        addPrice("FOOD", Material.CARROT, 10);
        addPrice("FOOD", Material.APPLE, 15);
        addPrice("FOOD", Material.COOKED_CHICKEN, 25);
        addPrice("FOOD", Material.COOKED_BEEF, 30);
        addPrice("FOOD", Material.GOLDEN_CARROT, 40);
        addPrice("FOOD", Material.GOLDEN_APPLE, 300);

        for (String category : prices.keySet()) {
            for (Map.Entry<Material, Double> entry
                    : prices.get(category).entrySet()) {
                String path = "prices." + category + "."
                        + entry.getKey().name();
                getConfig().addDefault(path, entry.getValue());
            }
        }

        getConfig().options().copyDefaults(true);
        saveConfig();

        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("DenizSMPShop etkin!");
    }

    private void addCategory(
            String id, Material icon, String displayName) {
        // Kategorilerin varsayilan ikonlari
        getConfig().addDefault("categories." + id + ".icon",
                icon.name());
        getConfig().addDefault("categories." + id + ".name",
                displayName);
    }

    private void addPrice(
            String category, Material material, double price) {
        prices.computeIfAbsent(category, k -> new LinkedHashMap<>())
                .put(material, price);
    }

    @Override
    public boolean onCommand(
            CommandSender sender, Command command,
            String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Bu komut oyuncular icindir.");
            return true;
        }

        if (!player.hasPermission("denizsmpshop.use")) {
            player.sendMessage(color("&cBu komutu kullanamazsin."));
            return true;
        }

        openMainMenu(player);
        return true;
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private Inventory createMenu(String title, String type,
                                 String category) {
        ShopHolder holder = new ShopHolder(type, category);
        Inventory inventory =
                Bukkit.createInventory(holder, 54, color(title));
        holder.setInventory(inventory);
        return inventory;
    }

    private ItemStack makeItem(
            Material material, String name, List<String> lore,
            String action, String category, String materialName) {

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(color(name));

        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(color(line));
        }
        meta.setLore(coloredLore);

        meta.getPersistentDataContainer().set(
                actionKey, PersistentDataType.STRING, action);

        if (category != null) {
            meta.getPersistentDataContainer().set(
                    categoryKey, PersistentDataType.STRING, category);
        }

        if (materialName != null) {
            meta.getPersistentDataContainer().set(
                    materialKey, PersistentDataType.STRING, materialName);
        }

        item.setItemMeta(meta);
        return item;
    }

    private void openMainMenu(Player player) {
        Inventory inv = createMenu(
                "&8DenizSMP &7| &6Shop", "main", null);

        inv.setItem(20, makeItem(
                Material.DIAMOND_SWORD, "&cCPvP Shop",
                List.of("&7CPvP esyalari", "&eAcmak icin tikla"),
                "category", "CPVP", null));

        inv.setItem(22, makeItem(
                Material.END_STONE, "&dEnd Shop",
                List.of("&7End esyalari", "&eAcmak icin tikla"),
                "category", "END", null));

        inv.setItem(24, makeItem(
                Material.NETHERRACK, "&6Nether Shop",
                List.of("&7Nether esyalari", "&eAcmak icin tikla"),
                "category", "NETHER", null));

        inv.setItem(31, makeItem(
                Material.COOKED_BEEF, "&aYemek Shop",
                List.of("&7Yiyecekler", "&eAcmak icin tikla"),
                "category", "FOOD", null));

        player.openInventory(inv);
    }

    private void openCategory(Player player, String category) {
        if (!prices.containsKey(category)) {
            return;
        }

        Inventory inv = createMenu(
                "&8DenizSMP &7| &6" + category,
                "category", category);

        int slot = 0;

        for (Map.Entry<Material, Double> entry
                : prices.get(category).entrySet()) {

            Material material = entry.getKey();
            double price = getConfig().getDouble(
                    "prices." + category + "." + material.name(),
                    entry.getValue());

            inv.setItem(slot++, makeItem(
                    material,
                    "&e" + prettyName(material),
                    List.of(
                            "&7Fiyat: &6" + formatMoney(price),
                            "&aSatın almak için tıkla",
                            "&8Adet: 1"
                    ),
                    "buy", category, material.name()));

            if (slot >= 45) {
                break;
            }
        }

        inv.setItem(49, makeItem(
                Material.ARROW, "&cGeri",
                List.of("&7Ana mağazaya dön"),
                "back", null, null));

        player.openInventory(inv);
    }

    private String prettyName(Material material) {
        String[] words = material.name().toLowerCase().split("_");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (result.length() > 0) result.append(" ");
            result.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1));
        }
        return result.toString();
    }

    private String formatMoney(double amount) {
        if (amount == Math.floor(amount)) {
            return String.format(Locale.US, "%.0f", amount);
        }
        return String.format(Locale.US, "%.2f", amount);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();

        if (!(top.getHolder() instanceof ShopHolder holder)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (event.getRawSlot() < 0
                || event.getRawSlot() >= top.getSize()) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) {
            return;
        }

        ItemMeta meta = clicked.getItemMeta();
        String action = meta.getPersistentDataContainer().get(
                actionKey, PersistentDataType.STRING);

        if (action == null) return;

        if (action.equals("category")) {
            String category = meta.getPersistentDataContainer().get(
                    categoryKey, PersistentDataType.STRING);
            if (category != null) openCategory(player, category);
            return;
        }

        if (action.equals("back")) {
            openMainMenu(player);
            return;
        }

        if (action.equals("buy")) {
            String category = meta.getPersistentDataContainer().get(
                    categoryKey, PersistentDataType.STRING);
            String materialName = meta.getPersistentDataContainer().get(
                    materialKey, PersistentDataType.STRING);

            if (category == null || materialName == null) return;

            Material material;
            try {
                material = Material.valueOf(materialName);
            } catch (IllegalArgumentException exception) {
                return;
            }

            double defaultPrice = prices.getOrDefault(
                    category, Collections.emptyMap())
                    .getOrDefault(material, -1.0);

            if (defaultPrice < 0) return;

            double price = getConfig().getDouble(
                    "prices." + category + "." + material.name(),
                    defaultPrice);

            if (!canFitOne(player, material)) {
                player.sendMessage(color(
                        "&cEnvanterinde yer yok!"));
                return;
            }

            if (!economy.has(player, price)) {
                player.sendMessage(color(
                        "&cYeterli paran yok! &7Fiyat: &6"
                                + formatMoney(price)));
                return;
            }

            economy.withdrawPlayer(player, price);

            Map<Integer, ItemStack> leftovers =
                    player.getInventory().addItem(
                            new ItemStack(material, 1));

            if (!leftovers.isEmpty()) {
                economy.depositPlayer(player, price);
                player.sendMessage(color(
                        "&cEsya verilemedi; paran iade edildi."));
                return;
            }

            player.sendMessage(color(
                    "&aSatın alındı: &e" + prettyName(material)
                            + " &7(-&6" + formatMoney(price) + "&7)"));
        }
    }

    private boolean canFitOne(Player player, Material material) {
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType().isAir()) {
                return true;
            }

            if (item.isSimilar(new ItemStack(material))
                    && item.getAmount() < item.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    private static final class ShopHolder implements InventoryHolder {
        private final String type;
        private final String category;
        private Inventory inventory;

        private ShopHolder(String type, String category) {
            this.type = type;
            this.category = category;
        }

        private void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }
  }
