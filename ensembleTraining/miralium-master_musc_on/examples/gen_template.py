#!/bin/env/python

import sys
if len(sys.argv) != 3:
    sys.stderr.write('USAGE: %s <num_fields> <ngram-length>\n' % sys.argv[0])
    sys.exit(1)

nextid = 0
num_columns = int(sys.argv[1])
ngram_length = int(sys.argv[2])
start = - ngram_length / 2
end = ngram_length / 2
for column in range(num_columns):
    for i in range(1, ngram_length + 1):
        for j in range(1, i + 1):
            print ("U%d" % nextid) + "/".join(["%%x[%d,%d]" % (x - i + ngram_length / 2 + ngram_length % 2, column) for x in range(j)])
            nextid += 1
print "B"
