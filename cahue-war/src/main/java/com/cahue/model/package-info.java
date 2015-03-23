@XmlJavaTypeAdapters({
        @XmlJavaTypeAdapter(value=DateAdapter.class, type=Date.class)
})
package com.cahue.model;

import com.cahue.model.adapter.DateAdapter;

import javax.xml.bind.annotation.adapters.*;
import java.util.Date;