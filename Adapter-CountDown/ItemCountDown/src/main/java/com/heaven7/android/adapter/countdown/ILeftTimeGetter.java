package com.heaven7.android.adapter.countdown;

import com.heaven7.adapter.ISelectable;

/**
 * the left time getter
 * Created by heaven7 on 2016/9/9.
 */
public interface ILeftTimeGetter extends ISelectable{

    /**
     * get the left time
     * @return the left time
     */
    long getLeftTime();

    /**
     * decrease left time
     * @param delta the delta time
     * @return true, if  success. false indicate it will be removed.
     */
    boolean decreaseLeftTime(long  delta);

}
