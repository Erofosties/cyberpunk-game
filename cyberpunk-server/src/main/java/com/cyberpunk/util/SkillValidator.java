package com.cyberpunk.util;

import com.cyberpunk.exception.InvalidSkillDistributionException;
import com.cyberpunk.gameBalance.GameBalance;

public class SkillValidator {

    public static void validateSkillDistribution(int... skills) {
        int sum = 0;
        for (int skill : skills) {
            sum += skill;
        }
        if (sum != GameBalance.TOTAL_CHARACTER_SKILL_POINTS) {
            throw new InvalidSkillDistributionException("Debes repartir exactamente " + GameBalance.TOTAL_CHARACTER_SKILL_POINTS + " puntos");
        }
    }
}