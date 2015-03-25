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

import sys, os, pickle, getopt,re
import json
import pycurl
import cStringIO
import os.path

from xml.dom import minidom
from clint.textui import colored
from datetime import datetime

from PhysCurlSvc import PhysCurl,GlobalTag,Tag,Iov

class PhysDBDriver():
    def __init__(self):
    # process command line options
        try:
            self._command = sys.argv[0]
            self.useSocks = False
            self.t0 = 0
            self.tMax = 'Inf'
            self.iovspan = 'time'
            self.jsondump=False
            self.dump=False
            self.outfilename=''
            self.urlsvc='localhost:8080'
            longopts=['help','socks','out=','jsondump','t0=','tMax=','iovspan=']
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
        print "usage: CliAdmin.py {<options>} <dburl> <action> {<args>}"
        print "Search conditions DB content using args"
        print "DBurl is needed in order to provide the system with the base url"
        print "Action determines which rest method to call"
        print "Subcommands are:"
        print " - FIND <type> <id> <opt> [payload, iovs, tag, globaltag] [like]"
        print "        ex: globaltag MY% like : retrieve list of global tags following pattern name"
        print "        ex: globaltag MYTAG trace : retrieve one global tag and associated leaf tags"
        print "        ex: payload <mypyldhash> one : retrieve the full payload corresponding to the hash"
        print " "
        print " - ADDFROMFILE <filename> <object type> [globaltag, tag, iov]"
        print "               <filename> payload <type> [image, txt ..] <streamer> <version>"
        print " "
        print " - ADD <object type> [globaltag, tag, iov, payload] <json content> "
        print "       <object type>      : this will ask directly for fields "
        print "       <object type> help : this will dump needed fields "
        print "       Time format to use in timestamp types: YYYYMMddHHmmss:GMT, e.g. 20141130100000:GMT"
        print " "
        print " - MAPTAG2GLOBALTAG <tag name> <globaltag name>"
        print " "
        print "Options: "
        print "  --socks activate socks proxy on localhost 3129 "
        print "  --out={filename} activate dump on filename "
        print "  --jsondump activate a dump of output lines in json format "
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
        
    def execute(self):
        "Execute the command for action "+self.action+" and using arguments : "
        print self.args
        start = datetime.now()
        restserver = PhysCurl(self.urlsvc, self.useSocks)
        
        if self.dump:
            outfile = open(self.outfilename,"w")
        _dict = {}
        dictarr = []
        dictkeys = []
        dictvalues = []
        params = {}
        
        if (self.action=='GLOBALTAGTRACE'):
            tagpattern = self.args[0]
            tagList = restserver.traceGlobalTags(tagpattern)
            for atag in tagList:
                mpobj = Tag(atag)
                print mpobj.toJson()
                
        elif (self.action=='FIND'):
            type = self.args[0]
            id = self.args[1]
            if len(self.args) < 3:
                params['opt']=""
                if type == "tag":
                    params['opt']="/one"
                if type == "globaltag":
                    params['opt']="/one"
            else:
                params['opt'] = "/"+self.args[2]
            params['type'] = type
            params['id'] = id
            pyld = restserver.find(params)
            if params['type'] == 'tag':
                for atag in pyld:
                    mpobj = Tag(atag)
                    print mpobj.toJson()  
            elif params['type'] == 'globaltag':
                for atag in pyld:
                    mpobj = GlobalTag(atag)
                    print mpobj.toJson()  
            elif params['type'] == 'iovs':
                for aiov in pyld:
                    mpobj = Iov(aiov)
                    print mpobj.toJson()  
            else:
                print pyld
            
        elif (self.action=='MAPTAG2GLOBALTAG'):
            tagname = self.args[0]
            gtagname = self.args[1]
            mapt = restserver.mapTag2GlobalTag(tagname,gtagname)
            print mapt
            
        elif (self.action=='ADDFROMFILE'):
            resp="No response"
            try:
                file = self.args[0]
                object=self.args[1]
                f = open(file,"r")
                data = json.loads(f.read())
            except:
                print "problem loading file or missing arguments"
            if (object == "globaltag"):
                resp = restserver.addGlobalTag(data)
            elif (object == "tag"):
                resp = restserver.addTag(data)
            elif (object == "iov"):
                resp = restserver.addIov(data)
            elif (object == "payload"):
                type = self.args[2]
                streamer = self.args[3]
                version = self.args[4]
                print 'Add payload using arguments: ',type,' ',streamer, ' ', version
                dict = { 'file' : file, 'objectType' : type, 'streamerInfo' : streamer, 'version' : version }
                resp = restserver.addPayload(dict)
            else:
                print "object not found..."
            print resp

        elif (self.action =='ADD'):
            try:
                print 'Found N arguments ',len(self.args)
                object=self.args[0]
                data = {}
                if len(self.args) >= 1:
                    if object == 'globaltag':
                        print 'listing global tag keys'
                        mobj = GlobalTag(data)
                    elif object == 'tag':
                        print 'listing tag keys'
                        mobj = Tag(data)
                    elif object == 'iov':
                        print 'init iov object...',data
                        mobj = Iov(data)
                    elif object == 'payload':
                        mobj = Payload(data)

                print 'determine action...'
                if len(self.args) == 2:
                    row = self.args[1]
                    if row == "help":
                        print mobj.getKeys()
                        return
                    try:
                        print 'Load data using ', row
                        data = json.loads(row)
                    except:
                        print 'Error in loading json...'
                        print 'For Global Tags use : '
                        print GlobalTag({}).help()
                        print 'For Tags use : '
                        print Tag({}).help()
                        print 'For Iovs use : '
                        print Iov({}).help()
                        return
                        
                elif len(self.args) < 2:
                    keys = mobj.getKeys()
                    types = mobj.getTypes()
                    for akey in keys:
                        thetype = types[keys.index(akey)]
                        keyval = raw_input('Insert '+akey+'['+thetype+'] : ')
                        if keyval == '':
                            keyval = None
                            data[akey]=keyval
                        elif "{" in keyval: # this is json input
                            val = json.loads(keyval)
                            print 'Parsing json from user input',val
                            data[akey]=val
                        elif "[" in keyval: # this is json input
                            val = json.loads(keyval)
                            print 'Parsing json from user input',val
                            data[akey]=val                        
                        elif "Payload" in thetype:
                            val = json.loads('{ "hash" : "'+keyval+'"}')
                            data[akey]=val
                        elif "Tag" in thetype:
                            val = json.loads('{ "name" : "'+keyval+'"}')
                            data[akey]=val
                        else:
                            data[akey]=keyval
                    print data
                    
                if data is None:
                    print "Missing data for ADD method"
                    return
                
            except:
                print "problem uploading or missing arguments: ", sys.exc_info()[0]
                raise

            if (object == "globaltag"):
                resp = restserver.addGlobalTag(data)
            elif (object == "tag"):
                resp = restserver.addTag(data)
            elif (object == "iov"):
                resp = restserver.addIov(data)
            elif (object == "payload"):
                type = self.args[2]
                streamer = self.args[3]
                version = self.args[4]
                print 'Add payload using arguments: ',type,' ',streamer, ' ', version
                dict = { 'file' : file, 'objectType' : type, 'streamerInfo' : streamer, 'version' : version }
                resp = restserver.addPayload(dict)
            else:
                print "object not found..."
            print resp

        else:
            print "Command not recognized: please type -h for help"
            

if __name__ == '__main__':
    PhysDBDriver()

