#!/bin/sh
wget http://www.cnts.ua.ac.be/conll2000/chunking/train.txt.gz -O - | gunzip | cut -f1,2 -d " "| python ../examples/pos_features.py > pos-enhanced.train.txt
../mira -t -f 8 -s 0.01 -n 10 pos-enhanced.template pos-enhanced.train.txt pos-enhanced.model
../mira -c pos-enhanced.model pos-enhanced.model.txt
python convert_model.py < pos-enhanced.model.txt > model.js
cat model.js viterbi.js > combined.js
