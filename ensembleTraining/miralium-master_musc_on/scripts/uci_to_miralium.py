import sys, re

continuous = []
for line in open(sys.argv[1]): ### names file
    line = line.strip()
    line = line.split("|")[0]
    if ":" in line:
        if line.find(": continuous.") != -1:
            continuous.append(True)
        else:
            continuous.append(False)

for line in open(sys.argv[2]): ### data file
    line = line.split("|")[0]
    tokens = line.strip().split(",")
    if len(tokens) == 0: continue
    output = []
    for i in xrange(len(tokens) - 1):
        if continuous[i]:
            output.append(("%d:" % i) + tokens[i].strip())
        else:
            output.append(tokens[i].strip())
    output.append(re.sub("\.$", "", tokens[-1].strip()))
    print " ".join(output)
    print

