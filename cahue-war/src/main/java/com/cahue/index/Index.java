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

    Set<Long> queryByRange(Double latitude, Double longitude, Long range);

    void put(String id, Double latitude, Double longitude, Date time);

    void delete(List<String> ids);

    int deleteBefore(Date date);

    void reset();
}
