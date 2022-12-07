#!/usr/bin/python
# Originally:
# This scripts loads a pretrained model and a input file in CoNLL format (each line a token, sentences separated by an empty line).
# The input sentences are passed to the model for tagging. Prints the tokens and the tags in a CoNLL format to stdout
# Usage: python RunModel_ConLL_Format.py modelPath inputPathToConllFile
# For pretrained models see docs/

# Now it takes 2 parameters, the modelPath and a port number to communicate with Java app

from __future__ import print_function

import logging
logger = logging.getLogger('RunDeid')
logger.setLevel(logging.DEBUG)

from util.preprocessing import readCoNLLSocket, createMatrices, addCharInformation, addCasingInformation
from neuralnets.BiLSTM import BiLSTM
import sys

import socket
import time

if len(sys.argv) < 3:
    print ("Usage: python RunDeid.py modelPath portNumber")
    exit()

modelPath = sys.argv[1]
portNumber = sys.argv[2]
# :: Load the model ::
lstmModel = BiLSTM.loadModel(modelPath)
print('model loaded')

#socket
# Create a TCP/IP socket
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

sock.settimeout(None)
# Bind the socket to the address given on the command line
server_address = ('', int(portNumber)) #was 4444
sock.bind(server_address)
print('starting up on %s port %s' % sock.getsockname())
sock.listen(1)

endMsg = b"--<eosc>--"
endMsgE = b"--<eoscE>--"

inputColumns = {0: "tokens", 1: "begin", 2: "end", 3: "tnum", 4: "snum", 5: "fname"}

jobEnd = "no"

totalDecode =0
totalRead=0
totalCasing=0
totalMatrix=0
totalTag=0
totalOutput=0
totalSend=0
totalCharInfo=0
totalLoopRead=0

while True:
  
    print('waiting for a connection')
    connection, client_address = sock.accept()
    try:
        print(client_address)
        data = b''
        #endMsgE could get split
        rd = b''

        while True:
            start=time.time()
            rd = connection.recv(30000)        #was 10000
            #print ("rd:")
            #print (rd)
            #print("data:")
            #print (data)
            if rd.strip() == endMsg:
                jobEnd = "yes"
                break
        
            if rd.strip().endswith(endMsgE):
                data += rd.replace(endMsgE, b"")
                break
            
            data += rd
        
            if data.strip().endswith(endMsgE):
                data = data.replace(endMsgE, b"")
                break

            #if data.strip() == endMsg:
            #    jobEnd = "yes"
            #    break

        end=time.time()
        totalLoopRead+=(end-start)
        if jobEnd == "yes":
            print("detected jobEnd yes, breaking in try")
            break               
        print('Tagging...')

        start = time.time()
        data = data.decode('utf-8')
        end = time.time()
        totalDecode += (end-start)
        
        # :: Prepare the input ::
        start = time.time()
        sentences = readCoNLLSocket(data, inputColumns)
        end = time.time()
        totalRead += (end-start)

        start = time.time()
        addCharInformation(sentences)
        end = time.time()
        totalCharInfo += (end-start)

        start = time.time()
        addCasingInformation(sentences)
        end = time.time()
        totalCasing += (end-start)

        start = time.time()
        dataMatrix = createMatrices(sentences, lstmModel.mappings, True)
        end = time.time()
        totalMatrix += (end-start)

        start = time.time()

        # :: Tag the input ::
        tags = lstmModel.tagSentences(dataMatrix)
        end = time.time()
        totalTag += (end-start)

        start = time.time()

        # :: Output to stdout ::
        output = ''        
        for sentenceIdx in range(len(sentences)):
            tokens = sentences[sentenceIdx]['tokens']
            begins = sentences[sentenceIdx]['begin']
            ends = sentences[sentenceIdx]['end']
            tnums = sentences[sentenceIdx]['tnum']
            snums = sentences[sentenceIdx]['snum']
            fnames = sentences[sentenceIdx]['fname']
            
            for tokenIdx in range(len(tokens)):
                tokenTags = []
                for modelName in sorted(tags.keys()):
                    tokenTags.append(tags[modelName][sentenceIdx][tokenIdx])

                #print("%s\t%s\n" % (tokens[tokenIdx], "\t".join(tokenTags)))
                #output += ("%s\t%s\n" % (tokens[tokenIdx], "\t".join(tokenTags)))
                #print("%s\t%s\t%s\n" % (tokens[tokenIdx], begins[tokenIdx], "\t".join(tokenTags)))      

                #GARY. *************
                #output += ("%s\t%s\t %s\t%s\t %s\t%s\tO\t%s\n" % (tokens[tokenIdx], begins[tokenIdx], 
                #ends[tokenIdx], tnums[tokenIdx], snums[tokenIdx], fnames[tokenIdx], "\t".join(tokenTags)))      

                output += tokens[tokenIdx] + "\t "+ begins[tokenIdx] + "\t "+                 ends[tokenIdx] + "\t "+ tnums[tokenIdx] + "\t "                 + snums[tokenIdx] + "\t "+ fnames[tokenIdx] + "\t0\t\t".join(tokenTags)
             
            #print("")
            output += "\n"
    
        #print output
        #print("%s" % output.encode('utf-8'))        
        end = time.time()
        totalOutput += (end-start)

        start = time.time()
        connection.sendall(output.encode('utf-8'))
        end = time.time()
        totalSend += (end-start)

    finally:
        connection.close()
        print("connection closed")
        
    if jobEnd == "yes":
        break        
        
print("Job done")
#divide by 200 documents, *1000 to change from seconds to milliseconds, == *5
print(" totalDecode: ", round(totalDecode*5,1))
print(" totalRead: ", round(totalRead*5,1))
print(" totalCasing: ", round(totalCasing*5,1))
print(" totalSend: ", round(totalSend*5,1))
print(" totalOutput: ", round(totalOutput*5,1))
print(" totalTag: ", round(totalTag*5,1))
print(" totalMatrix: ", round(totalMatrix*5,1))
print(" totalCharInfo: ", round(totalCharInfo*5,1))
print(" totalLoopRead: ", round(totalLoopRead*5,1))

