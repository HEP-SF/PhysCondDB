#!/usr/bin/env python
# encoding: utf-8
'''
cliadmin.CliAdmin -- shortdesc

cliadmin.CliAdmin is a description

It defines classes_and_methods

@author:     user_name

@copyright:  2015 organization_name. All rights reserved.

@license:    license

@contact:    user_email
@deffield    updated: Updated
'''

import sys, os, getopt,re
import json
import os.path
import random

from xml.dom import minidom
from clint.textui import colored
from datetime import datetime
import hashlib

from PhysCurlSvc import PhysCurl,GlobalTag,Tag,Iov,Payload,PayloadData
from __builtin__ import file

class PhysDBGenerator():
    def __init__(self):
    # process command line options
        try:
            self._command = sys.argv[0]
            self.useSocks = False
            self.t0 = 0
            self.tMax = 1000000
            self.iovspan = 'time'
            self.nobjs=1
            self.fsize=1048000
            self.jsondump=False
            self.dump=False
            self.tag='atag'
            self.outfilename=''
            self.urlsvc='localhost:8080'
            longopts=['help','socks','out=','jsondump','size=','tag=','nObjs=','t0=','tMax=','iovspan=']
            opts,args=getopt.getopt(sys.argv[1:],'',longopts)
            print opts, args
        except getopt.GetoptError,e:
            print e
            self.usage()
            sys.exit(-1)
        self.procopts(opts,args)
        self.execute()

    def usage(self):
        print
        print "usage: GenerateContent.py {<options>} <dburl> <action> {<args>}"
        print "Create conditions content using args"
        print "DBurl is needed in order to provide the system with the base url"
        print "Action determines which rest method to call"
        print "Subcommands are:"
        print " "
        print " - GENERATE <object type> [iov] : use t0 and tMax for time range, nObjs for number of intervals "
        print " "
        print " "
        print "Options: "
        print "  --socks activate socks proxy on localhost 3129 "
        print "  --out={filename} activate dump on filename "
        print "  --jsondump activate a dump of output lines in json format "
        print "  --tag={root tag name to generate: will append _X depending on nObjs option } "
        print "  --nObjs={number of objects to generate} "
        print "  --size={size of generated payload in bytes} "
        print "  --t0={t0 for iovs} "
        print "  --tMax={tMax for iovs} "
        print "  --iovspan={time|date|runlb|timerun|daterun} "
        print "         time: iov in COOL format, allows Inf for infinity"
        print "         date: iov in yyyyMMddHHmmss format"
        print "         runlb: iov in run-lb format, only for run based folders "
        print "         the others make the conversion to run number, does not allow Inf for infinity "
        print "Examples: "
        
        
    def procopts(self,opts,args):
        "Process the command line parameters"
        for o,a in opts:
            print 'Analyse options ' + o + ' ' + a
            if (o=='--help'):
                self.usage()
                sys.exit(0)
            if (o=='--socks'):
                self.useSocks=True
            if (o=='--out'):
                self.dump=True
                self.outfilename=a
            if (o=='--jsondump'):
                self.jsondump=True
            if (o=='--size'):
                self.fsize=int(a)
            if (o=='--nObjs'):
                self.nobjs=int(a)
            if (o=='--tag'):
                self.tag=a
            if (o=='--t0'):
                self.t0=a
            if (o=='--tMax'):
                self.tMax=a
            if (o=='--iovspan'):
                self.iovspan=a
                
        print "Using options [socks, iovspan,jsondump]"
        print self.useSocks
        print self.iovspan
        print self.jsondump
        
        if (len(args)<2):
            raise getopt.GetoptError("Insufficient arguments - need at least 3, or try --help")
        self.urlsvc=args[0]
        self.action=args[1].upper()
        self.args=args[2:]
        print self.args
        
    def randbin(self, d): 
        mx = (2 ** d) - 1 
        a = random.randint(0, mx)
        b = bin(a)       
#        b = bin(random.randint(0, mx)) 
        return b[2:].rjust(d, '0') 

    def generateFile(self, size, filename="test.txt"):
        f = open(filename, "wb")
        filesize = 0
        while filesize < size:
            line = str(self.randbin(filesize))
            f.write(line)
            filesize += len(line)
        f.close()

    def hashfile(self, afile, hasher, blocksize=65536):
        buf = afile.read(blocksize)
        while len(buf) > 0:
            hasher.update(buf)
            buf = afile.read(blocksize)
        hashstr = ''.join(x.encode('hex') for x in str(hasher.digest()))
        return hashstr

    def execute(self):
        "Execute the command for action "+self.action+" and using arguments : "
        print self.args
        start = datetime.now()
        
        if self.dump:
            outfile = open(self.outfilename,"w")
        _dict = {}
        params = {}
        
        if (self.action=='GENERATE'):
            resp="No response"
            try:
                filename = self.args[1]
                print "Opening file ",filename
                objecttype=self.args[0]
                f = open(filename,"r")
                print f
                data = json.loads(f.read())
                print "loaded data from file ",data
                if objecttype == "iov":
                    restserver = PhysCurl(self.urlsvc, self.useSocks)
                    for it in range(0, self.nobjs):
                        print 'generate element ',it
                        sincetime = random.randint(self.t0, self.tMax)
                        iov = Iov(data)
                        iov.setParameter('since', sincetime)
                        iov.setParameter('tag', { 'name' : self.tag })
                        print 'Generated element ',iov,' with since ',iov.getParameter('since')
                        
                        print 'create a new payload object from file '
                        fname = '.'.join(("test.out",str(sincetime)))
                        self.generateFile(self.fsize, fname)
                        hash = self.hashfile(open(fname, 'rb'), hashlib.sha256())
#                    hash = self.hashfile(open("test.out", 'rb'), hashlib.md5())
                        print 'Hash for file is ',str(hash)
                        dict = { 'file' : fname, 'objectType' : 'anobject', 'streamerInfo' : 'none', 'version' : '1.0' }
                        resp = restserver.addPayload(dict)
                        print 'Server send response ',resp
                        if hash in resp['data']:
                            print 'Found same key for payload !'
                        else:
                            print 'You are using different ways of hashing !!'
                        iov.setParameter('payload',{ 'hash' : hash })
                        data = json.loads(iov.toJson())
                        resp = restserver.addIov(data)
                        print 'Received response from server ',resp
                        
                elif objecttype == "globaltag":
                    print 'create a new global tag object from file'
                    globaltagname = data['name']
                    print 'inserting global tag with name ',globaltagname
                    restserver = PhysCurl(self.urlsvc, self.useSocks)
                    resp = restserver.addGlobalTag(data)

                    restserver2 = PhysCurl(self.urlsvc, self.useSocks)                    
                    for it in range(0, self.nobjs):
                        print 'generate element ',it
                        thetaglist = (self.tag,str(it))
                        tagname = '_'.join(thetaglist)
                        print 'generate tag using name ',tagname
                        newtag = Tag({})
                        newtag.setParameter('name', tagname)
                        newtag.setParameter('timeType', self.iovspan)
                        newtag.setParameter('objectType', 'test')
                        newtag.setParameter('synchronization', 'none')
                        newtag.setParameter('description', 'generated tag')
                        newtag.setParameter('lastValidatedTime', -1)
                        newtag.setParameter('endOfValidity', -1)
                        data = json.loads(newtag.toJson())
                        print 'Generated element ',data,' with name ',newtag.getParameter('name')
                        resp = restserver2.addTag(data)
                        print 'Received response from server ',resp
                        mapt = restserver.mapTag2GlobalTag(tagname,globaltagname)
 
                elif objecttype == "payload":
                    print 'create a new payload object from file '
                    self.generateFile(self.fsize, "test.out")
                    hash = self.hashfile(open("test.out", 'rb'), hashlib.sha256())
#                    hash = self.hashfile(open("test.out", 'rb'), hashlib.md5())
                    print 'Hash for file is ',str(hash)
                    
            except ValueError as e:
                print "error({0}): {1}".format(e.errno, e.strerror)
                print "problem loading file or missing arguments : ", sys.exc_info()[0]
        else:
            print "Command not recognized: please type -h for help"
        exend = datetime.now()
        print "Finish generation: elapsed time is ",exend-start
            


if __name__ == '__main__':
    PhysDBGenerator()

