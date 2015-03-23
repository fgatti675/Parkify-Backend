package com.cahue.model.adapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class DateAdapter extends XmlAdapter<String, Date> {

    private SimpleDateFormat dateFormat;

    public DateAdapter() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public String marshal(Date date) throws Exception {
        return dateFormat.format(date);
    }

    @Override
    public Date unmarshal(String string) throws Exception {
        return dateFormat.parse(string);
    }

}