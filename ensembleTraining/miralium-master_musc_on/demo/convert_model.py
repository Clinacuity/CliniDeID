#!/usr/bin/env python2

# convert a miralium text model to a javascript model
import sys

section = "header"
tags = []
templates = []
ids = {}
weights = []
for line in sys.stdin:
    line = line.strip()
    if line == "":
        if section == "header":
            section = "tags"
        elif section == "tags":
            section = "templates"
        elif section == "templates":
            section = "ids"
        elif section == "ids":
            section = "weights"
    else:
        if section == "tags":
            tags.append( "'%s'" % line.replace("\\", "\\\\").replace("'", "\\'"))
        elif section == "templates":
            templates.append( "'%s'" % line.replace("\\", "\\\\").replace("'", "\\'"))
        elif section == "ids":
            tokens = line.split()
            key = tokens[1].replace("\\", "\\\\").replace("'", "\\'")
            ids[key]=int(tokens[0])
            #ids.append("'%s':%s" % (tokens[1].replace("\\", "\\\\").replace("'", "\\'"), tokens[0]))
        elif section == "weights":
            value = float(line)
            weights.append(value)

new_weights = {}
for key, location in ids.items():
    output = []
    if key.startswith('U'):
        for i in range(len(tags)):
            if ("%.5f" % weights[location + i]) != "0.00000":
                output.append("%d:%.5f" % (i, weights[location + i]))
    elif key.startswith('B'):
        for i in range(len(tags) * len(tags)):
            if weights[location + i] != 0:
                output.append("%d:%.5f" % (i, weights[location + i]))
    if len(output) > 0:
        new_weights[key] = "{" + ",".join(output) + "}"

sys.stdout.write("var tags=[" + ",".join(tags) + "];\n")
sys.stdout.write("var templates=[" + ",".join(templates) + "];\n")
sys.stdout.write("var weights={" + ",".join(["'%s':%s" % (x, y) for x, y in new_weights.items()]) + "};\n")
