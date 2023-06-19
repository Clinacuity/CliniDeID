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
import java.io.*;

class AsrSemanticModel extends Mira {
    double plattA = -1.0;
    double plattB = 5;
    public double getNgramScore(String text) {
        return getNgramScore(text.split(" "));
    }
    public double getNgramScore(String text[]) {
        Vector<String[]> parts = new Vector<String[]>();
        for(int i = 0; i < text.length; i++) {
            String tokens[] = text[i].split("@");
            if(tokens[0].startsWith("[")) continue; // skip [carillon] but keep <s> </s>...
            if(tokens.length == 2) {
                parts.add(tokens);
            } else {
                parts.add(new String[]{tokens[0], "o"});
            }
        }
        Example example = encodeFeatures(parts, false, false); // no new features, no features for begining/end of string
        example.score = 0;
        for(int position = 0; position < example.labels.length; position++) {
            example.score += computeScore(example, position, example.labels[position]);
            if(position > 0) example.score += computeScore(example, position, example.labels[position], example.labels[position - 1]);
        }
        // perform platt calibration
        return 1.0 / (1.0 + Math.exp(plattA * example.score + plattB));
    }

    /*public double getNgramScore(int id1, int id2) {
        double score = 0;
        score += unigramMapping[id1];
        score += bigramMapping[id1 + id2 * lexiconSize];
        score += conceptBigramMapping[id1 + id2 * lexiconSize];
        return score;
    }*/

    public AsrSemanticModel(String modelName, double plattA, double plattB) {
        this.plattA = plattA;
        this.plattB = plattB;
        try {
            if(modelName.endsWith(".txt")) loadTextModel(modelName);
            else loadModel(modelName);
        } catch(Exception e) {
            System.err.println("ERROR: could not load MIRA model \"" + modelName + "\"");
            e.printStackTrace();
        }
    }
    public AsrSemanticModel(String modelName) {
        try {
            if(modelName.endsWith(".txt")) loadTextModel(modelName);
            else loadModel(modelName);
        } catch(Exception e) {
            System.err.println("ERROR: could not load MIRA model \"" + modelName + "\"");
            e.printStackTrace();
        }
    }
    public static void main(String args[]) {
        if(args.length != 3 && args.length != 1) {
            System.err.println("USAGE: java -cp mira.jar edu.lium.mira.AsrSemanticModel <model_name> [<calibration_A> <calibration_B>");
            System.err.println("Put word sequences in stdin, get bigrams + scores in stdout.");
            System.err.println("Scores are transformed by 1 / (1 + exp(A * score + B)) A = -1 and B = 5 are the default");
            System.exit(1);
        }
        AsrSemanticModel model = null;
        if(args.length == 3) model = new AsrSemanticModel(args[0], Double.parseDouble(args[1]), Double.parseDouble(args[2]));
        else if(args.length == 1) model = new AsrSemanticModel(args[0]);
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            String line;
            Vector<String> words = new Vector<String>();
            while(null != (line = input.readLine())) {
                String tokens[] = line.trim().split(" ");
                for(int i = 0; i < tokens.length; i++) {
                    words.add(tokens[i]);
                }
            }
            for(int i = 0; i < words.size() - 1; i++) {
                String[] bigram = new String[]{words.get(i), words.get(i + 1)};
                double score = model.getNgramScore(bigram);
                System.out.println(bigram[0] + " " + bigram[1] + " " + score);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
