package com.cahue.util;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by Francesco on 12/04/2015.
 */
public class BooleanAdapter extends XmlAdapter<Boolean, Boolean> {
    @Override
    public Boolean unmarshal(Boolean v) throws Exception {
        return v == null ? false : v;
    }

    @Override
    public Boolean marshal(Boolean v) throws Exception {
        return v ? true : null;
    }
}
