package com.vladlevchik.util;


import lombok.experimental.UtilityClass;

import java.util.Currency;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@UtilityClass
public class Validation {
    private static final Set<String> currencyCodes;

    static {
        Set<Currency> currencySet = Currency.getAvailableCurrencies();
        currencyCodes = new HashSet<>();
        for (Currency currency : currencySet) {
            currencyCodes.add(currency.getCurrencyCode());
        }
    }

    public static boolean isValidCurrencyCode(String code) {
        return currencyCodes.contains(code);
    }

    public static boolean isValidCurrencyName(String name) {
        // Проверка на отсутствие цифр
        if (name.matches(".*\\d.*")) {
            return false;
        }
        // Проверка на начало с заглавной буквы
        if (!Character.isUpperCase(name.charAt(0))) {
            return false;
        }

        // Проверка на отсутствие символов, кроме пробелов
        // Проверка на отсутствие символов, кроме пробелов и английских букв
        Pattern pattern = Pattern.compile("[^A-Za-z\\s]+");
        Matcher matcher = pattern.matcher(name);
        return !matcher.find();
    }

    public static boolean isValidCurrencySign(String sign) {
        //Длинна выбрана не случайно, самый длинный значок это максимум 3 символа
        return sign.length() >= 1 && sign.length() <= 3;
    }
}
