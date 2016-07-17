package com.liangmayong.androidhook.demo.patchs;

import com.liangmayong.androidhook.PatchObject;
import com.liangmayong.androidhook.annotation.Patch;
import com.liangmayong.androidhook.demo.Demo;

/**
 * Created by 007 on 2016/7/17.
 */
@Patch(Hook_getCount.class)
public class DemoPatch extends PatchObject<Demo> {
    /**
     * PatchObject
     *
     * @param baseObject baseObject
     */
    public DemoPatch(Demo baseObject) {
        super(baseObject);
    }
}
