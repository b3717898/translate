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

def interpret_path(path, enum_file_java_w, const_file_java_w, jsp_enum_file_java_w, js_enum_file_java_w,
                   common_enum_file_java_w, jsp_el_file_java_w):
    if os.path.isdir(path):
        for file in os.listdir(path):
            file_path = os.path.join(path, file)
            if os.path.isfile(file_path):
                if '_transdone' in file \
                        or '.DS_Store' in file \
                        or 'ligerComboBox.js' in file:
                    pass
                elif '.java' in file:
                    tran_file = os.path.join(path, file)
                    interpretors["java"].convertfile(tran_file, common_enum_file_java_w, const_file_java_w, jsp_el_file_java_w)
                elif '.jsp' in file:
                    # continue
                    tran_file = os.path.join(path, file)
                    interpretors["jsp"].convertfile(tran_file, common_enum_file_java_w, const_file_java_w, jsp_el_file_java_w)
                elif '.js' in file:
                    # continue
                    tran_file = os.path.join(path, file)
                    interpretors["js"].convertfile(tran_file, js_enum_file_java_w, const_file_java_w, jsp_el_file_java_w)
                elif '.xml' in file:
                    # todo list
                    continue
            else:
                interpret_path(file_path, enum_file_java_w, const_file_java_w, jsp_enum_file_java_w,
                               js_enum_file_java_w, common_enum_file_java_w, jsp_el_file_java_w)
    else:
        print 'the input is not a folder:' + path

def filter_el_file(el_file_path):
    file_name = os.path.basename(el_file_path).split('.')[0]
    file_name_tr = os.path.basename(el_file_path).split('.')[0] + '_transdone.txt'
    open_file = codecs.open(el_file_path, 'r', 'utf-8')
    open_file_w = codecs.open(os.path.join(os.path.dirname(el_file_path), file_name_tr), 'w', 'utf-8')
    is_translated = True
    el_keys = {}
    try:
        for line in open_file.readlines():
            if line not in el_keys:
                open_file_w.write(line)
                el_keys[line] = "1"
            # print trans_line
        # print os.path.abspath(file) + ":translate done"
    except Exception, e:
        is_translated = False
        print 'filter el file error:{}'.format(e)
    finally:
        open_file.close()
        open_file_w.close()
    if is_translated:
        #   rename translated file to original file & del the translated file
        try:
            abs_file = os.path.abspath(el_file_path)
            file_rename_to = os.path.join(os.path.dirname(el_file_path), os.path.basename(el_file_path) + '_rename')
            os.rename(abs_file, file_rename_to)
            os.rename(os.path.join(os.path.dirname(el_file_path), file_name_tr), abs_file)
            os.remove(file_rename_to)
            print abs_file + ":filter el file done:" + str(datetime.datetime.now())
        except Exception, e:
            print 'filter el file error:{}'.format(e)

if __name__ == '__main__':

    src_path = '/workspace/PYTHON/translate_data'
    if len(sys.argv) > 1:
        src_path = sys.argv[1]
    i18n_path = '/workspace/PYTHON/translate/test'
    if len(sys.argv) > 2:
        i18n_path = sys.argv[2]
    output_filename = JAVA_COMMON_4_I18N_ENUM_OUTPUT_FILENAME
    const_output_filename = JAVA_COMMON_4_I18N_CONST_OUTPUT_FILENAME
    jsp_output_filename = JAVA_COMMON_4_I18N_JSP_ENUM_OUTPUT_FILENAME
    js_output_filename = JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_FILENAME
    common_output_filename = JAVA_COMMON_4_I18N_COMMON_ENUM_OUTPUT_FILENAME
    jsp_el_output_filename = JAVA_COMMON_4_I18N_JSP_FMT_OUTPUT_FILENAME

    interpretors["java"] = Java4i18nInterpretor()
    interpretors["jsp"] = Jsp4i18nInterpretor()
    interpretors["js"] = Js4i18nInterpretor()
    # interpretors["xml"] = XMLInterpretor()

    # build a enum file
    # enum_file_java = os.path.join(i18n_path, output_filename)
    # enum_file_java_w = codecs.open(enum_file_java, 'w', 'utf-8')

    # jsp_enum_file_java = os.path.join(i18n_path, jsp_output_filename)
    # jsp_enum_file_java_w = codecs.open(jsp_enum_file_java, 'w', 'utf-8')

    common_enum_file_java = os.path.join(i18n_path, common_output_filename)
    common_enum_file_java_w = codecs.open(common_enum_file_java, 'w', 'utf-8')

    jsp_el_file_java = os.path.join(i18n_path, jsp_el_output_filename)
    jsp_el_file_java_w = codecs.open(jsp_el_file_java, 'w', 'utf-8')

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
        jsp_el_file_java_content = ["<!-- this is common fmt el file --> \n"]
        # todo list
        # enum_file_java_w.writelines(enum_file_java_content)
        const_file_java_w.writelines(const_file_java_content)
        # jsp_enum_file_java_w.writelines(enum_file_java_content)
        js_enum_file_java_w.writelines(js_file_java_content)
        common_enum_file_java_w.writelines(enum_file_java_content)
        jsp_el_file_java_w.writelines(jsp_el_file_java_content)
        interpret_path(src_path, None, const_file_java_w, None, js_enum_file_java_w, common_enum_file_java_w, jsp_el_file_java_w)

        const_file_java_content = ["}\n", "\n"]
        const_file_java_w.writelines(const_file_java_content)

        js_file_java_content = ["}\n", "\n"]
        js_enum_file_java_w.writelines(js_file_java_content)
    except Exception, e:
        print 'build i18n file error:{}'.format(e)
    finally:
        # enum_file_java_w.close()
        const_file_java_w.close()
        # jsp_enum_file_java_w.close()
        js_enum_file_java_w.close()
        common_enum_file_java_w.close()
        jsp_el_file_java_w.close()
    # filter the el file (message4EL.jsp) to remove the repeat var
    filter_el_file(jsp_el_file_java)
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

