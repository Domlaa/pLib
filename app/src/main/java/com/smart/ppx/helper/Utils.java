package com.smart.ppx.helper;

import java.util.Random;

public class Utils {


    public static int getNum(int size) {
        Random random = new Random();
        return random.nextInt(size);
    }

}
