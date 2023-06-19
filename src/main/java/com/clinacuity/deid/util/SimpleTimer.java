
/*
# © Copyright 2019-2023, Clinacuity Inc. All Rights Reserved.
#
# This file is part of CliniDeID.
# CliniDeID is free software: you can redistribute it and/or modify it under the terms of the
# GNU General Public License as published by the Free Software Foundation,
# either version 3 of the License, or any later version.
# CliniDeID is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
# without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
# PURPOSE. See the GNU General Public License for more details.
# You should have received a copy of the GNU General Public License along with CliniDeID.
# If not, see <https://www.gnu.org/licenses/>.
# =========================================================================   
*/

package com.clinacuity.deid.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimpleTimer {
    protected static final Logger LOGGER = LogManager.getLogger();
    private long start;
    private long total = 0;

    public void start() {
        start = System.nanoTime();
    }

    public long stopPrint(String label) {
        return stopPrint(label, 1L);
    }

    public long stopPrint(String label, long quotient) {
        long finish = System.nanoTime();
        LOGGER.error("SimpleTimer Time for {} was {} {}", label, (finish - start) / quotient, determineUnit(quotient));
        return finish - start;
    }

    //below are for cumulative timing of something. Call startCum before and stopCumulative after each section to be timed
    //when all done call printCumulative

    public long stopCumulative() {
        long finish = System.nanoTime();
        total += (finish - start);
        return total;
    }

    public void printCumulative(String label) {
        printCumulative(label, 1L);
    }

    public void printCumulative(String label, long quotient) {
        LOGGER.error("SimpleTimer Cumulative Time for {} was {} {}", label, total / quotient, determineUnit(quotient));
    }

    public void resetCum() {
        total = 0;
    }

    public long getTotal() {
        return total;
    }

    private String determineUnit(long quotient) {
        String unit = "";
        if (quotient == 1) {
            unit = "nano";
        } else if (quotient == 1000) {
            unit = "micro";
        } else if (quotient == 1000000) {
            unit = "milli";
        } else if (quotient == 1000000000) {
            unit = "";
        }
        return unit + "seconds";
    }
}