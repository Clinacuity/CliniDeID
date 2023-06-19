
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

package com.clinacuity.deid.clearTkChunking;

import java.util.Set;
import java.util.TreeSet;

/**
 * Created by andrewtrice on 7/26/17. Creates WordShape feature for use by mallet in NamedEntityChunker, mimics results of CoreNLP chris2uselc
 */
public class WordShape {// TODO: consider removing GREEK code, does it happen in real reports and is useful?
    //   private static final String GREEK_ALPHABET = "alpha beta gamma delta epsilon zeta eta theta iota kappa lambda mu nu xi omicron pi rho sigma tau upsilon phi chi psi omega";
    private static final Set<String> GREEK_ALPHABET = Set.of("alpha", "beta", "gamma", "delta", "epsilon", "zeta", "eta", "theta", "iota", "kappa", "lambda", "mu", "nu", "xi", "omicron", "pi", "rho", "sigma", "tau", "upsilon", "phi", "chi", "psi", "omega");

    private WordShape() {
    }

    public static String wordShapeBuilder(String word) {
        if (word.length() <= 4) {
            return shortWordShapeBuilder(word);
        } else {
            return longWordShapeBuilder(word);
        }
    }

    private static String shortWordShapeBuilder(String shortWord) {
        StringBuilder outputString = new StringBuilder();
        char[] shortWordCharArray = shortWord.toCharArray();

        if (GREEK_ALPHABET.contains(shortWord.toLowerCase())) {
            return "g";
        }

        for (Character c : shortWordCharArray) {
            if (Character.isDigit(c)) {
                outputString.append('d');
            } else if (Character.isLowerCase(c)) {
                outputString.append('x');
            } else if (Character.isUpperCase(c)) {
                outputString.append('X');
            } else {
                outputString.append(c);
            }
        }

        // if(shortWord.toLowerCase().equals(shortWord)) {
        // outputString.append('k');
        // }

        return outputString.toString();
    }

    private static String longWordShapeBuilder(String longWord) {
        Set<Character> seen = new TreeSet<>();
        StringBuilder outputString = new StringBuilder();
        char[] beginChars = longWord.substring(0, 2).toCharArray();
        char[] middleChars = longWord.substring(2, longWord.length() - 2).toCharArray();
        char[] endChars = longWord.substring(longWord.length() - 2, longWord.length()).toCharArray();

        if (GREEK_ALPHABET.contains(longWord.toLowerCase())) {
            return "g";
        }

        for (Character c : beginChars) {
            if (Character.isDigit(c)) {
                outputString.append('d');
            } else if (Character.isLowerCase(c)) {
                outputString.append('x');
            } else if (Character.isUpperCase(c)) {
                outputString.append('X');
            } else {
                outputString.append(c);
            }
        }

        for (Character c : middleChars) {
            if (Character.isDigit(c)) {
                seen.add('d');
            } else if (Character.isLowerCase(c)) {
                seen.add('x');
            } else if (Character.isUpperCase(c)) {
                seen.add('X');
            } else {
                seen.add(c);
            }
        }

        seen.forEach(outputString::append);

        for (Character c : endChars) {
            if (Character.isDigit(c)) {
                outputString.append('d');
            } else if (Character.isLowerCase(c)) {
                outputString.append('x');
            } else if (Character.isUpperCase(c)) {
                outputString.append('X');
            } else {
                outputString.append(c);
            }
        }

        // if(longWord.toLowerCase().equals(longWord)) {
        // outputString.append('k');
        // }

        return outputString.toString();
    }
}
