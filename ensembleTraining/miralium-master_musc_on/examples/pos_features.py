#!/usr/bin/env python

import sys, re

first = True
for line in sys.stdin.readlines():
    tokens = line.strip().split()
    # skip empty lines
    if len(tokens) == 0:
        sys.stdout.write("\n")
        first = True
        continue
    #1: word, 2: contains number(Y/N), 3: capitalized(Y/N), 4:contains symbol (Y/N) 5..8 (prefixes from 1 to 4) 9..12 (suffixes from 1 to 4).
    word = tokens[0]
    if len(tokens) > 1:
        label = tokens[1]
    else:
        label = None
    features = [word, word.lower()]
    if re.search(r'\d', word): features.append('Y')
    else: features.append('N')
    if first: features.append('Y')
    else: features.append('N')
    if re.match(r'^[A-Z][a-z]', word):features.append('Y')
    else: features.append('N')
    if re.search(r'[^\dA-Za-z]', word): features.append('Y')
    else: features.append('N')
    if len(word) > 0: features.append(word[:1])
    else: features.append('__nil__')
    if len(word) > 1: features.append(word[:2])
    else: features.append('__nil__')
    if len(word) > 2: features.append(word[:3])
    else: features.append('__nil__')
    if len(word) > 3: features.append(word[:4])
    else: features.append('__nil__')
    if len(word) > 0: features.append(word[-1])
    else: features.append('__nil__')
    if len(word) > 1: features.append(word[-2:])
    else: features.append('__nil__')
    if len(word) > 2: features.append(word[-3:])
    else: features.append('__nil__')
    if len(word) > 3: features.append(word[-4:])
    else: features.append('__nil__')
    if label:
        features.append(label)
    sys.stdout.write(" ".join(features) + "\n")
    first = False
