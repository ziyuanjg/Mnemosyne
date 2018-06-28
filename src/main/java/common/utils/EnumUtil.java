package common.utils;

import common.EnumInterface;

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
}

