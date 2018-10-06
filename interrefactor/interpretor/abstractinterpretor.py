#!/usr/bin/env python
# -*- coding: utf-8 -*-

from abc import ABCMeta, abstractmethod
import sys
reload(sys)
sys.setdefaultencoding('utf8')
import re
import os
import codecs
import datetime


class AbstractInterpretor(object):
    __metaclass__ = ABCMeta

    def __init__(self):
        self.headers = ''
        self.chinese_words = re.compile(u"\"[^\"]*[\u4e00-\u9fa5]+[^\"]*\"")

    def __str__(self):
        return str(self.headers)


    def __repr__(self):
        return repr(self.headers)

    def convertfile(self, file):
        file_name_tr = os.path.basename(file).split('.')[0] + '_transdone.txt'
        open_file = codecs.open(file, 'r', 'utf-8')
        open_file_w = codecs.open(os.path.join(os.path.dirname(file), file_name_tr), 'w', 'utf-8')
        is_translated = True
        try:
            for line in open_file.readlines():
                trans_line = self._convertline(line)
                open_file_w.write(trans_line)
                # print trans_line
            # print os.path.abspath(file) + ":translate done"
        except Exception, e:
            is_translated = False
            print 'translate error:{}'.format(e)
        finally:
            open_file.close()
            open_file_w.close()
        if is_translated:
            #   rename translated file to original file & del the translated file
            try:
                abs_file = os.path.abspath(file)
                file_rename_to = os.path.join(os.path.dirname(file), os.path.basename(file) + '_rename')
                os.rename(abs_file, file_rename_to)
                os.rename(os.path.join(os.path.dirname(file), file_name_tr), abs_file)
                os.remove(file_rename_to)
                print abs_file + ":translate done:" + str(datetime.datetime.now())
            except Exception, e:
                print 'translate file error:{}'.format(e)


    @abstractmethod
    def _convertline(self, line): pass