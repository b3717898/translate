#!/usr/bin/env python
# -*- coding: utf-8 -*-
# @Time    : 2018-09-03 14:22
# @Author  : yan_shizhi
# @Site    : 
# @File    : trans_run.py
# @Software: PyCharm

from trans_sourcemap import *
from trans_translator import *
import os
import codecs
import datetime
from config.config import *
from util.commandutil import *


# def Traditional2Simplified(sentence):
#     '''
#     将sentence中的繁体字转为简体字
#     :param sentence: 待转换的句子
#     :return: 将句子中繁体字转换为简体字之后的句子
#     '''
#     sentence = Converter('zh-hans').convert(sentence)
#     return sentence
#
#
# def Simplified2Traditional(sentence):
#     '''
#     将sentence中的简体字转为繁体字
#     :param sentence: 待转换的句子
#     :return: 将句子中简体字转换为繁体字之后的句子
#     '''
#     sentence = Converter('zh-hant').convert(sentence)
#     return sentence

def translate(sentence, flag):
    if flag == 'T2S':
        sentence = Translator('zh-hans').convert(sentence)
    elif flag == 'S2T':
        sentence = Translator('zh-hant').convert(sentence)
    else:
        pass
    return sentence


def do_translate(file, flag):
    file_name_tr = os.path.basename(file).split('.')[0] + '_transdone.txt'
    open_file = codecs.open(file, 'r', 'utf-8')
    open_file_w = codecs.open(os.path.join(os.path.dirname(file), file_name_tr), 'w', 'utf-8')
    is_translated = True
    try:
        for line in open_file.readlines():
            trans_line = translate(line, flag)
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


def translate_path(path, flag):
    if os.path.isdir(path):
        for file in os.listdir(path):
            file_path = os.path.join(path, file)
            if os.path.isfile(file_path):
                if '_transdone' in file or '.DS_Store' in file:
                    pass
                elif '.jsp' in file or '.java' in file \
                        or '.js' in file or '.xml' in file or '.properties' in file or '.ini' in file:
                    tran_file = os.path.join(path, file)
                    orignal_tran_file = os.path.join(path, file)
                    is_tran_u = False
                    if '.properties' in file:# convert it to utf8, for the reason of some chinese in properties
                        # must be convert to utf8 then trans from traditional chinese to simple chinese
                        tran_file_u = tran_file + "_u"
                        cmd = UNICODE_2_UTF8_CMD.format(tran_file,tran_file_u)
                        (result,output) = local_com(cmd,JAVA_BIN)
                        if result:
                            tran_file = tran_file_u
                            is_tran_u = True
                        else:
                            print "properties file trans error:file:" + tran_file + ",error:" + str(output)
                    do_translate(tran_file, flag)
                    if '.properties' in file:# convert the utf8 file to unicode
                        if is_tran_u:
                            cmd = UTF8_2_UNICODE_CMD.format(tran_file, orignal_tran_file)
                            (result, output) = local_com(cmd, JAVA_BIN)
                            #remove the trans file
                            os.remove(tran_file)
                            if not result:
                                print "properties file trans back error:file:" + orignal_tran_file + ",error:" + str(output)

            else:
                translate_path(file_path, flag)
    else:
        print 'the input is not a folder:' + path


if __name__ == '__main__':
    initTransMap('zh-hant', zh2Hant);
    initTransMap('zh-hans', zh2Hans);
    del zh2Hant, zh2Hans
    path = '/workspace/PYTHON/translate/test/1'
    # S2T代表简转繁   T2S表示繁转简
    flag = 'S2T'

    translate_path(path, flag)
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

