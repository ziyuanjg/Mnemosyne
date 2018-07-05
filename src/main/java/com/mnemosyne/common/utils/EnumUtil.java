package com.mnemosyne.common.utils;

import com.mnemosyne.common.EnumInterface;

/**
 * Created by 希罗 on 2018/6/6
 */
public class EnumUtil {


    public static <E extends EnumInterface> E findByCode(Class<E> enumClass, Integer code) {

        if (code == null) {
            return null;
        }

        for (E enumeration : enumClass.getEnumConstants()) {
            if (enumeration.getCode().equals(code)) {
                return enumeration;
            }
        }
        return null;
    }

    public static <E extends EnumInterface> E findByMsg(Class<E> enumClass, String msg) {

        if (msg == null || "".equals(msg)) {
            return null;
        }

        for (E enumeration : enumClass.getEnumConstants()) {
            if (enumeration.getMessage().equals(msg)) {
                return enumeration;
            }
        }
        return null;
    }
}

