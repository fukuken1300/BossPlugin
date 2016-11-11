package com.github.fukuken1300.bossplugin;

import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BossPlugin extends JavaPlugin implements Listener {

	/**
	 * 乱数ジェネレータ
	 */
	static private Random rand = new Random();

	/**
	 * ボスの残りHPゲージの表示方法
	 */
	static private String HEALTHGAUGE = "||||||||||";

	/**
	 * ボスモンスター
	 */
	private Creature boss = null;

	/**
	 * 直前に実行したスキル
	 */
	private Skill skill = null;

	/**
	 * ログ出力用
	 */
	private Logger log;

	/**
	 * プラグインが開始するとき呼び出される
	 */
	public void onEnable() {

		log = this.getLogger();

		// イベントリスナーの登録
		getServer().getPluginManager().registerEvents(this, this);

	}

	/**
	 * プラグインが終了するとき呼び出される
	 */
	public void onDisable() {

		// ボスがまだ生きていたら削除する
		if (boss != null && !boss.isDead()) {
			boss.remove();
			boss=null;
		}
	}

	/**
	 * コマンドが実行されたら呼び出される
	 */
	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {

		// コマンドを実行したプレイヤーを特定する
		Player player = sender.getServer().getPlayerExact(sender.getName());

		// 万一プレイヤーを特定できなかったら失敗
		if (player == null) {
			log.warning("コマンドを実行したプレイヤー(" + sender.getName() + ")を特定できませんでした。");
			return true;
		}

		// ボスモンスターを呼び出すコマンドか
		if (commandLabel.equalsIgnoreCase("boss")) {
			// 既にボスがいるか
			if (boss != null) {
				player.sendMessage("§c前のボスが倒されるまで次のボスを呼び出せません。");
				return true;
			}

			// 難易度のデフォルトは1
			int level = 1;

			// コマンドの引数で難易度が指定されているか
			if (args.length == 1) {

				// 指定された難易度を数値として解釈する
				try {
					level = Integer.parseInt(args[0]);
				} catch (NumberFormatException e) {
					player.sendMessage("§c難易度は数値で指定してください。");
					return false;
				}

				// 難易度を1以上50以下に制限する
				if (level < 0) {
					level = 1;
				} else if (level > 50) {
					level = 50;
				}
			}

			// ボスモンスターを配置する
			boss = (Creature) player.getWorld().spawnEntity(
					player.getLocation(), EntityType.SKELETON);

			// ボスに持たせる武器を作る
			ItemStack weapon = new ItemStack(Material.BOW);

			//ボスの武器にダメージ増加エンチャントをかける
			weapon.addEnchantment(Enchantment.ARROW_DAMAGE, 5);

			// ボスに武器を持たせる
			boss.getEquipment().setItemInHand(weapon);

			//ボスに鉄の防具4点セット（ヘルメット・チェストプレート・レギンス・ブーツ）を装備させる
			boss.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
			boss.getEquipment().setChestplate(
					new ItemStack(Material.DIAMOND_CHESTPLATE));
			boss.getEquipment().setLeggings(
					new ItemStack(Material.DIAMOND_LEGGINGS));
			boss.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS));

			// ボスに名前を付ける
			onBossHealthUpDown();

			// ボスの名前を常に表示する
			boss.setCustomNameVisible(true);

			// ボスのHPを多めに設定
			boss.setMaxHealth(100 * level);
			boss.setHealth(boss.getMaxHealth());

			// ボスに1時間（3600秒）のエンチャント（移動速度増加・攻撃力増加・再生能力・火炎耐性）をかける
			boss.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,
					3600 * 20, 2, false));
			boss.addPotionEffect(new PotionEffect(
					PotionEffectType.INCREASE_DAMAGE, 3600 * 20, 3, false));
			boss.addPotionEffect(new PotionEffect(
					PotionEffectType.REGENERATION, 3600 * 20, 3, false));
			boss.addPotionEffect(new PotionEffect(
					PotionEffectType.DAMAGE_RESISTANCE, 3600 * 20, 2, false));
			boss.addPotionEffect(new PotionEffect(
					PotionEffectType.FIRE_RESISTANCE, 3600 * 20, 2, false));

			// ボスが配置されたことをサーバー全体メッセージで報告する
			getServer().broadcastMessage("§eBOSSBATTLE！");

			return true;
		}

		return false;
	}

	/**
	 * エンティティがエンティティからダメージを受けるとき呼び出される
	 *
	 * @param e
	 */
	@EventHandler
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e) {

		// ダメージを受けたエンティティはボスか
		if (e.getEntity().equals(boss)) {
			// ダメージ原因が矢で、かつその矢がボス自身の放ったものならダメージをキャンセルする
			if (e.getDamager() instanceof Arrow
					&& boss.equals(((Arrow) e.getDamager()).getShooter())) {
				e.setCancelled(true);
				return;
			}

			// ボスのHP表示を更新する
			onBossHealthUpDown();

			// スキルを実行中ではないか
			if (skill == null || !skill.isRunning()) {
				// 低確率でスキルを実行する
				switch (rand.nextInt(2)) {
				case 0:
					// 乱数で1が出たらマシンガンショット
					skill = new MachinegunShot(boss);
					skill.runTaskTimer(this, 0, 0);
					break;
				case 1:
					// 乱数で2が出たらシャワーショット
					skill = new ShowerShot(boss);
					skill.runTaskTimer(this, 0, 0);
					break;
				default:
					//それ以外のときは実行しない（確率下げる用）
					break;
				}
			}
		}
	}

	/**
	 * エンティティがブロックからダメージを受けるとき呼び出される
	 *
	 * @param e
	 */
	@EventHandler
	public void onEntityDamageByBlockEvent(EntityDamageByBlockEvent e) {

		// ダメージを受けたエンティティはボスか
		if (e.getEntity().equals(boss)) {
			// ボスのHP表示を更新する
			onBossHealthUpDown();
		}
	}

	/**
	 * エンティティの体力が回復するとき呼び出される
	 *
	 * @param e
	 */
	@EventHandler
	public void onEntityRegainHealthEvent(EntityRegainHealthEvent e) {

		// エンティティはボスか
		if (e.getEntity().equals(boss)) {
			// ボスのHP表示を更新する
			onBossHealthUpDown();
		}
	}

	/**
	 * ボスの名前と残りHPゲージを表示する
	 */
	private void onBossHealthUpDown() {
		// ボスの残りHPが最大HPの何割か調べる
		int remaining = (int) (boss.getHealth() / (double) boss.getMaxHealth() * HEALTHGAUGE
				.length());

		// ボスの名前と残りHPを表示する
		boss.setCustomName("ゴルゴ・サーティワン §4"
				+ HEALTHGAUGE.substring(0, HEALTHGAUGE.length() - remaining)
				+ "§a" + HEALTHGAUGE.substring(0, remaining) + "§f");
	}

	/**
	 * エンティティが死亡するとき呼び出される
	 *
	 * @param e
	 */
	@EventHandler
	public void onEntityDeathEvent(EntityDeathEvent e) {

		// 死亡したのはボスか
		if (e.getEntity().equals(boss)) {

			// スキルを実行中なら停止する
			if (skill != null && skill.isRunning()) {
				skill.cancel();
			}

			// 討伐報酬として64個以下でランダムな個数のダイヤモンドをドロップする
			e.getEntity()
					.getWorld()
					.dropItem(e.getEntity().getLocation(),
							new ItemStack(Material.DIAMOND, 1+rand.nextInt(64)));

			// エンダードラゴンと同じ量の経験値オーブをドロップする
			e.setDroppedExp(12000);

			// ボスが死んだことをサーバー全体メッセージで報告する
			getServer().broadcastMessage("§eClear!");

			// ボスが死んだので変数を初期化する
			boss = null;
		}
	}

	/**
	 * サーバー停止時に呼び出される
	 */
	@Override
	protected void finalize() throws Throwable {
		try {
			super.finalize();
		} finally {
			//プラグイン終了時の処理を呼び出す
			onDisable();
		}
	}

	/**
	 * 乱数ジェネレータを返す
	 *
	 * @return 乱数ジェネレータ
	 */
	static public Random getRandom() {
		return rand;
	}
}
