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
        self.chinese_words = re.compile(u"\"[^\"]*[\u4e00-\u9fa5]+[^\"]*\"")
        self.chinese_words_with_CASE = re.compile(u"case \"[^\"]*[\u4e00-\u9fa5]+[^\"]*\"")
        self.comment_words = re.compile(u"//.*[\u4e00-\u9fa5]+.*")
        # self.index = 1
        self.line = 1
        self.prop_name = {}
        self.globe_count = 1

    def __str__(self):
        return str(self.headers)


    def __repr__(self):
        return repr(self.headers)

    def convertfile(self, input_file, output_file, const_output_file):
        # self.index = 1
        self.line = 1
        file_name = os.path.basename(input_file).split('.')[0]
        file_name_tr = os.path.basename(input_file).split('.')[0] + '_transdone.txt'
        open_file = codecs.open(input_file, 'r', 'utf-8')
        open_file_w = codecs.open(os.path.join(os.path.dirname(input_file), file_name_tr), 'w', 'utf-8')
        is_translated = True
        enum_lines = [] # ['','']
        try:
            for line in open_file.readlines():
                trans_line, enum_lines, const_lines = self._convertline(line, file_name)
                open_file_w.write(trans_line)
                if len(enum_lines) > 0:
                    output_file.writelines(enum_lines)
                if len(const_lines) > 0:
                    const_output_file.writelines(const_lines)
                self.line += 1
                # print trans_line
            # print os.path.abspath(file) + ":translate done"
        except Exception, e:
            is_translated = False
            print 'convert file error:{}'.format(e)
        finally:
            open_file.close()
            open_file_w.close()
        if is_translated:
            #   rename translated file to original file & del the translated file
            try:
                abs_file = os.path.abspath(input_file)
                file_rename_to = os.path.join(os.path.dirname(input_file), os.path.basename(input_file) + '_rename')
                os.rename(abs_file, file_rename_to)
                os.rename(os.path.join(os.path.dirname(input_file), file_name_tr), abs_file)
                os.remove(file_rename_to)
                print abs_file + ":convert done:" + str(datetime.datetime.now())
            except Exception, e:
                print 'convert file error:{}'.format(e)


    @abstractmethod
    def _convertline(self, line, file_name): pass