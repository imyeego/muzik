package com.liuzhao.muzik.common.promise;

public enum Signal {
    THEN(1),
    NEXT(2),
    CATCH(3);

    private final int value;

    Signal(int value) {
        this.value = value;
    }


}
