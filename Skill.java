package com.github.fukuken1300.bossplugin;

import org.bukkit.entity.Creature;
import org.bukkit.scheduler.BukkitRunnable;
	/**
	 * ボスが使うスキルのベースとなるクラス
	 *
	 * @author admin
	 *
	 */
	abstract public class Skill extends BukkitRunnable {

		/**
		 * ボスモンスター
		 */
		private Creature boss = null;

		/**
		 * スキルを現在実行中ならtrue、実行後ならfalse
		 */
		private boolean isRunning = true;

		/**
		 * 経過時間カウンター
		 */
		private int count = 0;

		/**
		 * コンストラクタ
		 *
		 * @param _boss
		 *            ボス
		 */
		public Skill(Creature _boss) {
			boss = _boss;
		}

		/**
		 * 非同期処理内容
		 */
		public void run() {

			// 経過時間をカウントアップ
			count++;

			// ボスは生きているか
			if (!boss.isDead()) {

				// 毎tickごとの処理を呼び出す
				tick(count);
			}
		}

		/**
		 * 毎tickごとの処理
		 *
		 * @param count
		 *            経過時間カウンター
		 */
		abstract protected void tick(int count);

		/**
		 * ボスを返す
		 *
		 * @return ボス
		 */
		protected Creature getBoss() {
			return boss;
		}

		/**
		 * 非同期処理がキャンセルされたとき呼び出される
		 */
		public void cancel() {
			super.cancel();
			isRunning = false;
		}

		/**
		 * 現在スキルが実行中かを返す
		 *
		 * @return
		 */
		public boolean isRunning() {
			return isRunning;
		}
	}