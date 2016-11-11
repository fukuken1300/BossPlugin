package com.github.fukuken1300.bossplugin;

import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creature;
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
	protected void tick(int count) {
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

				// 低確率で火矢にする
				if (BossPlugin.getRandom().nextInt(10) == 0) {
					arrow.setFireTicks(10 * 20);
				}
			}
		}
	}
}