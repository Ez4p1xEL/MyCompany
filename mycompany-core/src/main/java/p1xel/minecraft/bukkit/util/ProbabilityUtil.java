package p1xel.minecraft.bukkit.util;

import java.util.concurrent.ThreadLocalRandom;

public class ProbabilityUtil {

    /**
     * 支持两位小数的概率判定（范围 0.00 ~ 100.00）
     * 最小精度为 0.01%（即万分之一）
     *
     * @param percent 概率值，支持 0.00 ~ 100.00（也自然兼容 1 ~ 100.00）
     * @return true 命中，false 未命中
     */
    public static boolean isTriggered(double percent) {
        if (percent <= 0.00) {
            return false;
        }
        if (percent >= 100.00) {
            return true;
        }

        // 1. 将 0.01 ~ 100.00 放大 100 倍并四舍五入，变成 1 ~ 10000 的整数阈值
        // 使用 Math.round 防止类似 12.34 * 100 变成 1233.9999999999998 的精度问题
        long threshold = Math.round(percent * 100);

        // 2. 生成 [1, 10000] 闭区间内的随机整数（注意上限 10001 是开区间，实际最大值为 10000）
        int randomRoll = ThreadLocalRandom.current().nextInt(1, 10001);

        // 3. 摇出的点数小于等于阈值即命中
        // 例如：传入 0.01%，threshold 为 1，只有摇出 1 才命中（概率恰好 1/10000）
        return randomRoll <= threshold;
    }

}
