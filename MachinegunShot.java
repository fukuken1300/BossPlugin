package com.github.fukuken1300.bossplugin;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creature;

/**
 * マシンガンショットを撃つ
 *
 * @author admin
 *
 */
public class MachinegunShot extends Skill {

	/**
	 * コンストラクタ
	 *
	 * @param _boss
	 *            ボス
	 */
	public MachinegunShot(Creature _boss) {
		super(_boss);
	}

	/**
	 * 毎tickごとに呼び出される
	 */
	protected void tick(int count) {
		// 経過時間に応じて処理
		if (count == 20) {
			// 呪文を唱える
			getBoss().getServer().broadcastMessage("§6マシンガンショットぉぉぉ！");
		} else if (count == 100) {
			// スキルを終了する
			cancel();
		} else if (count >= 40) {
			// 向いている方向に矢を発射する
			getBoss().launchProjectile(Arrow.class);
		}
	}
}
