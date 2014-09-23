package com.cahue.index;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Date: 23.09.14
 *
 * @author francesco
 */
public interface Index {

    Set<Long> query(Double latitude, Double longitude, Long range);

    void put(Long id, Double latitude, Double longitude, Date time);

    void delete(List<String> docIds);

    int deleteBefore(Date date);

    void reset();
}
