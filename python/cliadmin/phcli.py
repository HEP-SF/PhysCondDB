#!/usr/bin/env python
# encoding: utf-8
'''
cliadmin.phconddb -- shortdesc

cliadmin.phconddb is a description

It defines classes_and_methods

@author:     user_name

@copyright:  2015 organization_name. All rights reserved.

@license:    license

@contact:    user_email
@deffield    updated: Updated
'''

import sys, os, pickle, getopt,re
import json
import cStringIO
import os.path

#from pygments import highlight, lexers, formatters
from xml.dom import minidom
#from clint.textui import colored
from datetime import datetime

from swagger_client.apis import GlobaltagsApi, TagsApi, IovsApi, SystemsApi

class PhysDBDriver():
    def __init__(self):
    # process command line options
        try:
            self.restserver = {}
            self.resttools = {}
            self._command = sys.argv[0]
            self.useSocks = False
            self.t0 = 0
            self.tMax = 'Inf'
            self.debug = False
            self.trace = 'on'
            self.expand = 'true'
            self.by = 'none'
            self.page = 0
            self.pagesize = 1000
            self.iovspan = 'time'
            self.jsondump=False
            self.dump=False
            self.user='none'
            self.passwd='none'
            self.outfilename=''
            self.urlsvc='localhost:8080/physconddb'
            longopts=['help','socks','out=','jsondump','t0=','tMax=','url=','debug','trace=','expand=','by=','page=','pagesize=','iovspan=','user=','pass=']
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
        print "usage: phcli.py {<options>} <action> {<args>}"
        print "Search conditions DB content using args"
        print "Server url is needed in order to provide the system with the base url; defaults to localhost (see below)"
        print "Action determines which rest method to call"
        print "Actions list:"
        print " - FIND <type> <id> [tags, globaltags, systems]"
        print "        ex: globaltags MY% : retrieve list of global tags following pattern name"
        print "        ex: --trace=on FIND globaltags MYGTAG-01-01 : retrieve one global tag and associated leaf tags"
        print "   remarks: --trace=on will fail in case multiple object (id=xx%) are retrieved"
        print " "
        print " "
        print "Options: "
        print "  --socks activate socks proxy on localhost 3129 "
        print "  --debug activate debugging output "
        print "  --out={filename} activate dump on filename "
        print "  --jsondump activate a dump of output lines in json format "
        print "  --trace [on|off]: trace associations (ex: globaltag ->* tags or tag ->* iovs "
        print "  --expand [true|false]: expand result to complete obj, not only urls "
        print "  --by : comma separated list of conditions for filtering a query (e.g.: by=name:pippo,value<10)"
        print "  --page [0,...N]: page number to retrieve; use it in combination with page size, default is 0"
        print "  --pagesize [1000,30,...]: page size; use it in combination with page, default is 1000"
        print "  --expand [true|false]: expand result to complete obj, not only urls "
        print "  --url [localhost:8080/physconddb]: use a specific server "
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
            if (o=='--debug'):
                self.debug=True
            if (o=='--jsondump'):
                self.jsondump=True
            if (o=='--url'):
                self.urlsvc=a
            if (o=='--t0'):
                self.t0=a
            if (o=='--tMax'):
                self.tMax=a
            if (o=='--user'):
                self.user=a
            if (o=='--trace'):
                self.trace=a
            if (o=='--expand'):
                self.expand=a
            if (o=='--by'):
                self.by=a
            if (o=='--page'):
                self.page=a
            if (o=='--pagesize'):
                self.pagesize=a
            if (o=='--pass'):
                self.passwd=a
            if (o=='--iovspan'):
                self.iovspan=a
                
        if (len(args)<2):
            raise getopt.GetoptError("Insufficient arguments - need at least 3, or try --help")
        self.action=args[0].upper()
        self.args=args[1:]
        
    def printmsg(self,msg,color):
        try:
          from clint.textui import colored
          if color == 'cyan':
          	print colored.cyan(msg)
          elif color == 'blue':
            print colored.blue(msg)
          elif color == 'red':
            print colored.red(msg)
            
        except:
          print msg

    def getsystems(self,by):
        sysapis = SystemsApi()
        coll = sysapis.list_systems(by,page=self.page,size=self.pagesize)
        print coll
        syslist = coll.items
        for system in syslist:
            print system.schema_name,system.node_fullpath,system.node_description,system.tag_name_root
        print 'List of retrieved systems : ',len(syslist)

    def fetchsystem(self,name):
        sysapis = SystemsApi()
        system = sysapis.find_system(name,expand=self.expand,trace=self.trace)
        print system.schema_name,system.node_fullpath,system.node_description,system.tag_name_root

    def getglobaltags(self,by):
        gtapis = GlobaltagsApi()
        coll = gtapis.list_global_tags(by,expand=self.expand,page=self.page,size=self.pagesize)
        gtlist = coll.items
        for gtag in gtlist:
            print gtag.name,gtag.snapshot_time,gtag.validity,gtag.description,gtag.lockstatus
        print 'List of retrieved global tags : ',len(gtlist)

    def fetchglobaltag(self,name):
        gtapis = GlobaltagsApi()
        gtag = gtapis.find_global_tag(name,expand=self.expand,trace=self.trace)
        coll = gtag.global_tag_maps
        if coll is None:
            print 'associated tags not found: may be trace is disabled ? (--trace=on)'
            return
        for map in coll:
            print map.system_tag.name, map.system_tag.time_type, map.record, map.label
        print 'Selected global tag is: '
        print gtag.name,gtag.snapshot_time,gtag.validity,gtag.description,gtag.lockstatus
        print 'List of associated tag : ',len(coll)

    def gettags(self,by):
        tapis = TagsApi()
        coll = tapis.list_tags(by,expand=self.expand,page=self.page,size=self.pagesize)
        tlist = coll.items
        for tag in tlist:
            print tag.name,tag.time_type,tag.object_type,tag.description,tag.last_validated_time
        print 'List of retrieved tags : ',len(tlist)

    def fetchtag(self,name):
        tapis = TagsApi()
        tag = tapis.find_tag(name,expand=self.expand,trace=self.trace)
        print tag.name,tag.time_type,tag.object_type,tag.description,tag.last_validated_time
        coll = tag.global_tag_maps
        if coll is None:
            print 'associated global tags not found: may be trace is disabled ? (--trace=on)'
        for map in coll:
            print map.global_tag.name, map.global_tag.description, map.record, map.label

    def execute(self):
        msg = ('Execute the command for action %s and arguments : %s ' ) % (self.action, str(self.args))
        self.printmsg(msg,'cyan')
            
        start = datetime.now()
        
        if self.dump:
            outfile = open(self.outfilename,"w")
        _dict = {}
        params = {}
        
        
        if (self.action=='FIND'):
            try:
                print 'Action FIND is used to retrieve object from the DB'
                print 'Found N arguments ',len(self.args)
                object=self.args[0]
                msg = ('FIND: selected object is %s ') % (object)
                if object in [ 'globaltags', 'tags', 'systems' ]:
                    self.printmsg(msg,'cyan')
                else:
                    msg = ('FIND: cannot apply command to object %s ') % (object)
                    self.printmsg(msg,'red')
                    return -1        
                
                # load arguments and prepare the data structure for the GET request
                # optional parameters like trace and expand are added
                name = None
                if len(self.args) > 1:
                    name=self.args[1]
                    msg = ('FIND: selected id is %s ') % (name)
                    self.printmsg(msg,'cyan')
                        
                    if '%' in name:
                        print 'Cannot use pattern in this argument; if you want to filter a list, use option by=param[:<>]value,param1[:<>]value,...'
                        return -1
      
                    if object == 'globaltags':
                        self.fetchglobaltag(name)
                    elif object == 'tags':
                        self.fetchtag(name)
                    elif object == 'systems':
                        self.fetchsystem(name)
    
                else:
                    if object == 'globaltags':
                        self.getglobaltags(self.by)
                    elif object == 'tags':
                        self.gettags(self.by)
                    elif object == 'systems':
                        self.getsystems(self.by)

            except Exception, e:
                sys.exit("failed on action FIND: %s" % (str(e)))
                raise

        else:
            print "Command not recognized: please type -h for help"


        tend=datetime.now()
        print 'Time spent (ms): ',tend-start
        
if __name__ == '__main__':
    PhysDBDriver()

