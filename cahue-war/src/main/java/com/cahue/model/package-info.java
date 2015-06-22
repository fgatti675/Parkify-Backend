@XmlJavaTypeAdapters({
        @XmlJavaTypeAdapter(value=DateAdapter.class, type=Date.class)
})
package com.cahue.model;

import com.cahue.model.adapter.DateAdapter;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import java.util.Date;