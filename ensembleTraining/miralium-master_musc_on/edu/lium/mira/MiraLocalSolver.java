
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

/* Copyright (C) (2009) (Benoit Favre) <benoit.favre@gmail.com>

This program is free software; you can redistribute it and/or 
modify it under the terms of the GNU Lesser General Public License 
as published by the Free Software Foundation; either 
version 2 of the License, or (at your option) any later 
version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA. */

package edu.lium.mira;

import java.util.*;
import java.util.regex.*;
import java.util.zip.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.text.*;
import gnu.trove.*;

/* this class outputs localsolver problems */
class MiraLocalSolver extends Mira {
    protected Example decodeViterbi(Example example) {
        final int factor = 10000;
        Example optimal = super.decodeViterbi(example);
        System.err.println((int) (factor * optimal.score));
        System.out.print("maximize sum(");
        boolean first = true;
        for(int position = 0; position < example.labels.length; position++) {
            for(int label = 0; label < numLabels; label++) {
                int labelScore = (int)(factor * computeScore(example, position, label));
                if(labelScore != 0) {
                    if(!first) System.out.print(", ");
                    first = false;
                    System.out.print("" + labelScore + " x_" + position + "_" + label);
                }
                if(position > 0) {
                    for(int previousLabel = 0; previousLabel < numLabels; previousLabel ++) {
                        int scoreByPrevious = (int)(factor * computeScore(example, position, label, previousLabel));
                        if(scoreByPrevious != 0) {
                            if(!first) System.out.print(", ");
                            first = false;
                            System.out.print("" + scoreByPrevious + " and(x_" + position + "_" + label + ", x_" + (position - 1) + "_" + previousLabel + ")");
                        }
                    }
                }
            }
        }
        System.out.println(");");
        for(int position = 0; position < example.labels.length; position++) {
            System.out.print("constraint booleansum(");
            for(int label = 0; label < numLabels; label++) {
                if(label != 0) System.out.print(", ");
                System.out.print("x_" + position + "_" + label);
            }
            System.out.println(") = 1;");
        }
        for(int position = 0; position < example.labels.length; position++) {
            for(int label = 0; label < numLabels; label++) {
                System.out.println("x_" + position + "_" + label + " <- bool();");
            }
        }
        System.exit(0);
        return null;
    }
    public static void main(String args[]) {
        try {
            MiraLocalSolver mira = new MiraLocalSolver();
            String modelName = args[0];
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            if(modelName.endsWith(".txt")) mira.loadTextModel(modelName);
            else mira.loadModel(modelName);
            mira.test(input, null);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
