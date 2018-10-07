#!/usr/bin/env python
# -*- coding: utf-8 -*-
# @Time    : 2018-09-03 14:22
# @Author  : yan_shizhi
# @Site    :
# @File    : trans_run.py
# @Software: PyCharm

import os
import codecs
import datetime
import sys
from config.config import *
from util.commandutil import *
from interpretor.abstractinterpretor import *
from interpretor.javainterpretor import *

interpretors = {}

def interpret_path(path, enum_file_java_w):
    if os.path.isdir(path):
        for file in os.listdir(path):
            file_path = os.path.join(path, file)
            if os.path.isfile(file_path):
                if '_transdone' in file or '.DS_Store' in file:
                    pass
                elif '.java' in file:
                    tran_file = os.path.join(path, file)
                    interpretors["java"].convertfile(tran_file, enum_file_java_w)
                elif '.jsp' in file:
                    # todo list
                    pass
                elif '.js' in file:
                    # todo list
                    pass
                elif '.xml' in file:
                    # todo list
                    pass

            else:
                interpret_path(file_path, enum_file_java_w)
    else:
        print 'the input is not a folder:' + path


if __name__ == '__main__':
    src_path = '/workspace/PYTHON/translate/test/inter'
    if len(sys.argv) > 1:
        src_path = sys.argv[1]
    i18n_path = '/workspace/PYTHON/translate/test'
    if len(sys.argv) > 2:
        i18n_path = sys.argv[2]
    output_filename = 'Trans4Java.java'
    if len(sys.argv) > 3:
        output_filename = sys.argv[3]

    interpretors["java"] = JavaInterpretor()
    # interpretors["jsp"] = JspInterpretor()
    # interpretors["js"] = JsInterpretor()
    # interpretors["xml"] = XMLInterpretor()

    # build a enum file
    enum_file_java = os.path.join(i18n_path, output_filename)
    enum_file_java_w = codecs.open(enum_file_java, 'w', 'utf-8')
    try:
        enum_file_java_content = ["package com.rydeen.boh.core.i18n;\n", "\n", "public interface Trans4Java {\n",
                             "    /** 测试翻译内容 */\n",
                             "    String TEST_TRANS_CONTENT = \"测试翻译内容\";\n"]
        # todo list
        enum_file_java_w.writelines(enum_file_java_content)
        interpret_path(src_path, enum_file_java_w)
        enum_file_java_content = ["}\n", "\n"]
        enum_file_java_w.writelines(enum_file_java_content)
    except Exception, e:
        print 'build i18n file error:{}'.format(e)
    finally:
        enum_file_java_w.close()

    # if os.path.isdir(path):
    #     for file in os.listdir(path):
    #         if os.path.isfile(os.path.abspath(file)):
    #             if '_transdone' in file or '.DS_Store' in file:
    #                 pass
    #             elif '.jsp' in file or '.java' in file \
    #                     or '.js' in file or '.xml' in file or '.properties' in file or '.ini' in file:
    #                 do_translate(os.path.join(path, file), flag)
    #         else
    # elif os.path.isfile(path):
    #     do_translate(path, flag)

