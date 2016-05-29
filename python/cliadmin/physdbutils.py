#!/usr/bin/env python
# encoding: utf-8
'''
physdbutil.PhysDBUtils -- shortdesc
Utility functions
this is a description

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

from swagger_client.apis import GlobaltagsApi, TagsApi, IovsApi, SystemsApi, ExpertApi, MapsApi, ExpertcalibrationApi
from swagger_client.models import GlobalTag, Tag, GlobalTagMap, SystemDescription
from swagger_client import ApiClient

class PhysDbUtils():


    def __init__(self,host='http://localhost:8080/physconddb/api/rest',expand='true',trace='on',jsondump=False,outfile=None,debug=False,page=0,pagesize=1000):
    # process command line options
        self.host=host
        self.expand=expand
        self.trace=trace
        self.jsondump=jsondump
        self.outfile=outfile
        self.debug=debug
        self.page=page
        self.pagesize=pagesize
        if self.jsondump:
            self.outf = open(self.outfile,"w")
        
        self.api_client = ApiClient(host=self.host)
        print 'Creating class PhysDBUtils'
    
    def printmsg(self,msg,color):
        try:
          from clint.textui import colored
          if color == 'cyan':
          	print colored.cyan(msg)
          elif color == 'blue':
            print colored.blue(msg)
          elif color == 'green':
            print colored.green(msg)
          elif color == 'red':
            print colored.red(msg)
            
        except:
          print msg

    def close(self):
        if self.jsondump:
            self.outf.close()

    def getsystems(self,by):
        sysapis = SystemsApi(self.api_client)
        coll = sysapis.list_systems(by,page=self.page,size=self.pagesize)
        syslist = coll.items
        self.dumpmodellist(syslist,True,['id','schema_name','node_fullpath','node_description','tag_name_root'])
        print 'List of retrieved systems : ',len(syslist)

    def fetchsystem(self,name):
        sysapis = SystemsApi(self.api_client)
        system = sysapis.find_system(name,expand=self.expand,trace=self.trace)
        self.dumpmodelobject(system,True,['id','schema_name','node_fullpath','node_description','tag_name_root'])

    def getglobaltags(self,by):
        gtapis = GlobaltagsApi(self.api_client)
        coll = gtapis.list_global_tags(by,expand=self.expand,page=self.page,size=self.pagesize)
        gtlist = coll.items
        self.dumpmodellist(gtlist,True,['name','validity','release','snapshot_time','description','lockstatus'])
        print 'List of retrieved global tags : ',len(gtlist)

    def createobject(self,objparams,objtype):
        params = {}
        args = objparams.split(";")
        for _arg in args:
            if self.debug:
                print 'Filling argument ',_arg
            (key, value) = _arg.split("=")
            params[key] = value
        if self.debug:
            print 'created parameter list: ',params
        resp = RESTResponse()
        resp.data = json.dumps(params)
        instance = {}
        instance = self.api_client.deserialize(resp, objtype)
        if self.debug:
            print 'created model object : ',instance, ' for type ',objtype
        return instance

    def helpmodel(self,obj,keylist=[]):
        attrs = obj.attribute_map
        msg = ''
        for key in keylist:
            msg = ('%s%s=...;') % (msg,attrs[key])
        self.printmsg(msg[:-1],'cyan')
        
    def printrow(self, objdict, keys=[]):
        ##print objdict
        (head_format, row_format) = self.getformat(objdict,keys)
        for akey in keys:
            val = objdict[akey]
            if '_time' in akey:
                objdict[akey] = str(objdict[akey])
        ##print 'using row format ',row_format
        return row_format.format(**objdict)

    def printrownoformat(self, objdict, keys=[]):
        (head_format, row_format) = self.getsimpleformat(objdict,keys)
        for akey in keys:
            val = objdict[akey]
            if 'time' in akey:
                objdict[akey] = str(objdict[akey])
        ##print 'using row format ',row_format
        return row_format.format(**objdict)
    
    def getsimpleformat(self, objdict, keys=[]):
        ##print objdict
        if self.debug:
            print 'Get simple format using objdict and keys ',objdict,keys
        head_format = ''
        row_format = ''
        i=0
        for akey in keys:
            val = objdict[akey]
            akey_format='{%s} | ' % (akey)
            head_format += '{0[%d]} | ' % (int(i))
            row_format += akey_format
        return head_format,row_format
    
    def getformat(self, objdict, keys=[]):
        ##print objdict
        if self.debug:
            print 'Get format using objdict and keys ',objdict,keys
        head_format = ''
        row_format = ''
        i=0
        for akey in keys:
            val = objdict[akey]
            num = 30
            if (akey == 'name'):
                num=80
            if ('_time' in akey):
                num=19
            if ('node_' in akey):
                num=50
            if ('status' in akey):
                num=15
            if ('release' in akey):
                num=15
            if ('_type' in akey):
                num=30
            if ('time_type' in akey):
                num=10
            if ('description' in akey):
                num=70
            if ('hash' in akey):
                num=65
                
            akey_format='{%s:<%d.%d} | ' % (akey,num,num)
            if (isinstance(val,(int,float,long))):
                num=12
                if (akey == 'row'):
                    num=5
                akey_format='{%s:<%d} | ' % (akey,num)
            
            head_format += '{0[%d]:<%d} | ' % (int(i),num)
            row_format += akey_format
            i+=1
            
        return head_format,row_format
    
    def printheader(self, objdict, keys=[]):
        ##print objdict
        if self.debug:
            print 'Print header using objdict and keys ',objdict,keys
        (head_format, row_format) = self.getformat(objdict,keys)
        return head_format.format(keys)

    
    def cleanmodellist(self,objdict,keylist):
        if self.debug:
            print 'Clean model list using objdict and keylist ',objdict,keylist
        reducedlist=[]
        if len(keylist)>0:
            for key in keylist:
                if self.debug:
                    print 'found key ',key,'!'
                    print 'value is  ',objdict[key]
                if isinstance(objdict[key],list):
                    print 'skip list...'
                elif key in ['href','res_id']:
                    print 'skip fields'
                else:
                    reducedlist.append(key)
        else:
            for key in objdict:
                if isinstance(objdict[key],list):
                    print 'skip list...'
                elif key in ['href','res_id']:
                    print 'skip fields'
                else:
                    reducedlist.append(key)
                    #print 'Appending key ',key
        return reducedlist
    
    def printtrailer(self,objdict, keylist):
        if self.debug:
            print 'Print trailer using objdict and keylist ',objdict,keylist
        reducedlist = self.cleanmodellist(objdict,keylist)
        headermsg = self.printheader(objdict,reducedlist)
        sep_format = '{:=^%d}' % int(len(headermsg))
        self.printmsg(sep_format.format('========='),'blue')
        
    def dumpmodellist(self,objlist,withheader=False,keylist=[]):
        i=0
        if self.debug:
            print 'Dump model list using objdict and keylist ',objlist,keylist
        savedobj = {}
        for obj in objlist:
            i=(i+1)
            if i == 1:
                if not isinstance(obj,dict):
                    savedobj = obj.to_dict()
                else:
                    savedobj = obj
                self.dumpmodelobject(obj,withheader,keylist)
            else:
                self.dumpmodelobject(obj,False,keylist)
        self.printtrailer(savedobj,keylist)
        
    def dumpmodelobject(self,obj,withheader=False,keylist=[]):
        objdict = {}
        if self.debug:
            print 'Dump model object using obj and keylist ',obj,keylist
            
        if not isinstance(obj,dict):
            objdict = obj.to_dict()
        else:
            objdict = obj
        headermsg = ''
        msg = ''
        reducedlist=self.cleanmodellist(objdict,keylist)
                    
        if withheader is True:
            headermsg = self.printheader(objdict,reducedlist)
            sep_format = '{:=^%d}' % int(len(headermsg))
            self.printmsg(sep_format.format('========='),'blue')
            self.printmsg(headermsg,'blue')
            self.printmsg(sep_format.format('========='),'blue')
        
        msg = self.printrow(objdict,reducedlist)
        self.printmsg(msg,'cyan')
        if self.jsondump:
            dumpmsg = self.printrownoformat(objdict,reducedlist)
            self.outf.write(dumpmsg+"\n")

    def fetchglobaltag(self,name):
        gtapis = GlobaltagsApi(self.api_client)
        gtag = gtapis.find_global_tag(name,expand=self.expand,trace=self.trace)
        coll = gtag.global_tag_maps
        if coll is None:
            print 'associated tags not found: may be trace is disabled ? (--trace=on)'
            return
        i = 0
        rowlist = []
        for map in coll:
            i+=1
            row = { 'row' : i, 'name' : map.system_tag.name, 'time_type' : map.system_tag.time_type, 'record' : map.record, 'label' : map.label}
            rowlist.append(row)
            
        self.dumpmodellist(rowlist,True,['row','name','time_type','record','label'])
        print 'Selected global tag is: '
        self.dumpmodelobject(gtag,True,['name','snapshot_time','validity','description','lockstatus'])
        if self.debug:
            print gtag
        print 'List of associated tag : ',len(coll)

    def gettags(self,by):
        tapis = TagsApi(self.api_client)
        coll = tapis.list_tags(by,expand=self.expand,page=self.page,size=self.pagesize)
        tlist = coll.items
        self.dumpmodellist(tlist,True,['name','time_type','object_type','description','last_validated_time'])
        print 'List of retrieved tags : ',len(tlist)

    def fetchtag(self,name):
        tapis = TagsApi(self.api_client)
        tag = tapis.find_tag(name,expand=self.expand,trace=self.trace)
        print tag.name,tag.time_type,tag.object_type,tag.description,tag.last_validated_time
        coll = tag.global_tag_maps
        if coll is None:
            print 'associated global tags not found: may be trace is disabled ? (--trace=on)'
        rowlist = []
        i=0
        for map in coll:
            i+=1
            row = { 'row' : i, 'name' : map.global_tag.name, 'description' : map.global_tag.description, 'record' : map.record, 'label' : map.label}
            rowlist.append(row)
            
        self.dumpmodellist(rowlist,True,['row','name','description','record','label'])

class RESTResponse(object):
    def __init__(self):
        print 'create response object'
        self.__data = None

    def data(self):
        return self.__data

    def data(self, data):
        self.__data = data
        print 'create data field ',self.__data
# process command line options
