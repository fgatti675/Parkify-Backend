package com.cahue.debug;

import com.cahue.api.Location;
import com.cahue.util.FusionUtil;
import com.google.api.services.fusiontables.Fusiontables;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Francesco on 22/10/2014.
 */
public class RandomSpotsGenerator {

    private static final String TEST_TABLE_ID = "1Pa5hqK1KxKwgZmbgBFJ5opcbRGHELFsCL6CyE8bf";
    private static final Location CENTER = new Location(40.435165, -3.69684243, 0F);

    Fusiontables fusiontables;



    public RandomSpotsGenerator() {
        fusiontables = FusionUtil.createFusionTablesInstance();
    }

    public void generate(){


    }

}
