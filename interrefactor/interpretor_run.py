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
from interpretor.java4i18ninterpretor import *
from interpretor.jsp4i18ninterpretor import *
from interpretor.js4i18ninterpretor import *

interpretors = {}

def interpret_path(path, enum_file_java_w, const_file_java_w, jsp_enum_file_java_w, js_enum_file_java_w):
    if os.path.isdir(path):
        for file in os.listdir(path):
            file_path = os.path.join(path, file)
            if os.path.isfile(file_path):
                if '_transdone' in file or '.DS_Store' in file:
                    pass
                elif '.java' in file:
                    tran_file = os.path.join(path, file)
                    interpretors["java"].convertfile(tran_file, enum_file_java_w, const_file_java_w)
                elif '.jsp' in file:
                    tran_file = os.path.join(path, file)
                    interpretors["jsp"].convertfile(tran_file, jsp_enum_file_java_w, const_file_java_w)
                elif '.js' in file:
                    tran_file = os.path.join(path, file)
                    interpretors["js"].convertfile(tran_file, js_enum_file_java_w, const_file_java_w)
                    pass
                elif '.xml' in file:
                    # todo list
                    pass

            else:
                interpret_path(file_path, enum_file_java_w, const_file_java_w, jsp_enum_file_java_w, js_enum_file_java_w)
    else:
        print 'the input is not a folder:' + path


if __name__ == '__main__':
    src_path = '/workspace/PYTHON/translate/test/inter'
    if len(sys.argv) > 1:
        src_path = sys.argv[1]
    i18n_path = '/workspace/PYTHON/translate/test'
    if len(sys.argv) > 2:
        i18n_path = sys.argv[2]
    output_filename = JAVA_COMMON_4_I18N_ENUM_OUTPUT_FILENAME
    const_output_filename = JAVA_COMMON_4_I18N_CONST_OUTPUT_FILENAME
    jsp_output_filename = JAVA_COMMON_4_I18N_JSP_ENUM_OUTPUT_FILENAME
    js_output_filename = JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_FILENAME


    interpretors["java"] = Java4i18nInterpretor()
    interpretors["jsp"] = Jsp4i18nInterpretor()
    interpretors["js"] = Js4i18nInterpretor()
    # interpretors["xml"] = XMLInterpretor()

    # build a enum file
    enum_file_java = os.path.join(i18n_path, output_filename)
    enum_file_java_w = codecs.open(enum_file_java, 'w', 'utf-8')

    jsp_enum_file_java = os.path.join(i18n_path, jsp_output_filename)
    jsp_enum_file_java_w = codecs.open(jsp_enum_file_java, 'w', 'utf-8')

    js_enum_file_java = os.path.join(i18n_path, js_output_filename)
    js_enum_file_java_w = codecs.open(js_enum_file_java, 'w', 'utf-8')

    const_file_java = os.path.join(i18n_path, const_output_filename)
    const_file_java_w = codecs.open(const_file_java, 'w', 'utf-8')
    try:
        const_file_java_content = ["package " + JAVA_COMMON_4_I18N_CONST_PACKAGE_NAME +
                                   ";\n", "\n", "public interface " +
                                   JAVA_COMMON_4_I18N_CONST_OUTPUT_FILENAME.replace(".java", "") + " {\n",
                             "    /** 测试翻译内容 */\n",
                             "    String TEST_TRANS_CONTENT = \"测试翻译内容\";\n"]
        enum_file_java_content = ["#This is an i18n propteries file \n", "\n",
                                  "# 测试翻译内容 \n",
                                  "TEST_TRANS_CONTENT = 测试翻译内容\n"]
        js_file_java_content = ["/*This is an js i18n propteries file */\n", "\n",
                                  "var M4JS = {\n"]
        # todo list
        enum_file_java_w.writelines(enum_file_java_content)
        const_file_java_w.writelines(const_file_java_content)
        jsp_enum_file_java_w.writelines(enum_file_java_content)
        js_enum_file_java_w.writelines(js_file_java_content)
        interpret_path(src_path, enum_file_java_w, const_file_java_w, jsp_enum_file_java_w, js_enum_file_java_w)
        const_file_java_content = ["}\n", "\n"]
        const_file_java_w.writelines(const_file_java_content)

        js_file_java_content = ["}\n", "\n"]
        js_enum_file_java_w.writelines(js_file_java_content)
    except Exception, e:
        print 'build i18n file error:{}'.format(e)
    finally:
        enum_file_java_w.close()
        const_file_java_w.close()
        jsp_enum_file_java_w.close()
        js_enum_file_java_w.close()

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

