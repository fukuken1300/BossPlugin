package com.github.fukuken1300.bossplugin;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

/**
 * シャワーショットを撃つ
 *
 * @author admin
 *
 */
public class ShowerShot extends Skill {

	/**
	 * コンストラクタ
	 *
	 * @param _boss
	 *            ボス
	 */
	public ShowerShot(Creature _boss) {
		super(_boss);
	}

	/**
	 * 毎tickごとに呼び出される
	 */

	public class ProjectileHitListener implements Listener{
		public ProjectileHitListener(JavaPlugin plugin) {
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
		protected void tick(int count, ProjectileHitEvent event) {
		// 経過時間に応じて処理
		if (count == 20) {
			// 呪文を唱える
			getBoss().getServer().broadcastMessage("§6シャワーショット！");
		} else if (count == 100) {
			// スキルを終了する
			cancel();
		} else if (count >= 40 && count % 5 == 0) {
			// 矢の発射の始点を計算する
			// モンスターの座標は足元の座標なので、だいたい頭上から矢が出るよう高さを調整する
			Location loc = getBoss().getLocation().add(0, 2, 0);

			// 360度全方位に矢を発射する
			for (int angle = 0; angle != 360; angle += 10) {
				// 矢の発射方向を計算する
				Vector v = new Vector(Math.sin(Math.toRadians(angle)) / 50,
						1, Math.cos(Math.toRadians(angle)) / 50).normalize();

				// 矢を発射する
				Arrow arrow = getBoss().getWorld().spawnArrow(loc, v,0.6F, 0);

				// 矢の発射者を設定する
				arrow.setShooter(getBoss());

				// 低確率で
				if (BossPlugin.getRandom().nextInt(10) == 0) {
					//火矢にする
					arrow.setFireTicks(10 * 20);

					//地面を爆破する
					Projectile p = event.getEntity();
					if(p instanceof Arrow){
						//矢が何かにあたった時
						World w = p.getWorld();
						Location l = p.getLocation();

						w.createExplosion(l, 3.0f);//矢の当たった場所に爆発力3.0の爆発を起こす
					}
				}
				}
			}
		}
	}
}
