
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

/* 
 * 2009-12-27 added support for import/export of CRF++ compatible text models
 */

package edu.lium.mira;

import java.util.*;
import java.util.regex.*;
import java.text.*;
import java.io.*;

class PrintFeatures {

    Pattern templateRegex = Pattern.compile("%x\\[(-?\\d+),(\\d+)\\]");
    class Template implements Serializable {
        static final long serialVersionUID = 1L;
        public String definition;
        String[] prefix;
        String suffix;
        int[] rows;
        int[] columns;
        public Template(String definition) {
            this.definition = definition;
            Vector<String> prefix = new Vector<String>();
            Vector<Integer> rows = new Vector<Integer>();
            Vector<Integer> columns = new Vector<Integer>();
            Matcher matcher = templateRegex.matcher(definition);
            int last = 0;
            while(matcher.find()) {
                rows.add(new Integer(matcher.group(1)));
                columns.add(new Integer(matcher.group(2)));
                prefix.add(definition.substring(last, matcher.start()));
                last = matcher.end();
            }
            this.suffix = definition.substring(last);
            if(this.suffix.length() == 0) this.suffix = null;
            this.prefix = new String[prefix.size()];
            this.rows = new int[rows.size()];
            this.columns = new int[columns.size()];
            for(int i = 0; i < rows.size(); i++) {
                this.rows[i] = rows.get(i).intValue();
                this.columns[i] = columns.get(i).intValue();
                if(prefix.get(i).length() == 0) this.prefix[i] = null;
                else this.prefix[i] = prefix.get(i);
            }
        }
        public String apply(Vector<String[]> parts, int current, int shiftColumns, boolean includeBorderFeatures) {
            StringBuilder output = new StringBuilder();
            for(int i = 0; i < prefix.length; i++) {
                if(prefix[i] != null) output.append(prefix[i]);
                int row = rows[i] + current;
                int column = columns[i] + shiftColumns;
                if(row >= 0 && row < parts.size()) {
                    String[] line = parts.get(row);
                    if(column >= 0 && column < line.length) {
                        output.append(line[column]);
                    } else {
                        System.err.print("ERROR: wrong column in template \"" + definition + "\" for line \"");
                        for(int j = 0; j < line.length - 1; j++) {
                            System.err.print(line[j] + " ");
                        }
                        System.err.println(line[line.length - 1] + "\"");
                        System.exit(1);
                    }
                } else if(includeBorderFeatures) {
                    output.append("_B");
                    output.append(row);
                }
            }
            if(suffix != null) output.append(suffix);
            return output.toString();
        }
    }
    Vector<Template> templates;

    public void loadTemplates(String filename) throws IOException {
        BufferedReader input = new BufferedReader(new FileReader(filename));
        String line;
        templates = new Vector<Template>();
        while(null != (line = input.readLine())) {
            line = line.split("#")[0];
            line = line.trim();
            if(line.equals("")) continue;
            templates.add(new Template(line));
        }
        System.err.println("read " + templates.size() + " templates from \"" + filename + "\"");
    }


    public void printFeatures(BufferedReader input, PrintStream output) throws IOException {
        Vector<String[]> parts = new Vector<String[]>();
        String line;
        while(null != (line = input.readLine())) {
            line = line.trim();
            if(line.length() == 0) {
                for(int i = 0; i < parts.size(); i++) {
                    for(int j = 0; j < templates.size(); j++) {
                        String feature = templates.get(j).apply(parts, i, 0, false);
                        if(j > 0) output.print(" ");
                        output.print(feature);
                    }
                    output.println(" " + parts.get(i)[parts.get(i).length - 1]);
                }
                output.println();
                parts.clear();
            } else {
                String tokens[] = line.split("\\s+");
                parts.add(tokens);
            }
        }
    }

    public static void main(String args[]) {
        try {
            PrintFeatures mira = new PrintFeatures();
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            if(args.length > 1) {
                input = new BufferedReader(new FileReader(args[1]));
            }
            if(args.length > 0) {
                mira.loadTemplates(args[0]);
                mira.printFeatures(input, System.out);
            } else {
                System.err.println("USAGE: java PrintFeatures <template> [input_file]");
                System.exit(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
