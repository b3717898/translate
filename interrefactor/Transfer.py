#!/usr/bin/env python
# -*- coding: utf-8 -*-
# @Time    : 2018-12-28 14:20
# @Author  : yan_shizhi
# @Site    : 
# @File    : Transfer.py
# @Software: PyCharm

import re
import codecs
import os


class Transfer:
    def __init__(self, source_file, target_file, new_target_file=None):
        self.source_file = source_file
        self.target_file = target_file
        if not new_target_file:
            self.target_file_name = os.path.basename(self.target_file).split(".")[0]
            self.new_target_file = self.target_file.replace(self.target_file_name, self.target_file_name + "_new")
        else:
            self.new_target_file = new_target_file
        self.s_file = codecs.open(self.source_file, 'r', 'utf-8').read()
        self.t_file = codecs.open(self.target_file, 'r', 'utf-8')
        self.source_list = []
        self.source_dict = {}
        self.target_list = []
        for i in self.t_file.readlines():
            self.target_list.append(i)

    def __get_list(self, start_num):
        first = self.s_file.find("#", start_num)
        if first == -1:
            return False
        first_h = self.s_file.find("\n", first)
        second_h = self.s_file.find("\n", first_h + 1)
        if second_h == -1:
            second_h = len(self.s_file)
        self.source_list.append(self.s_file[first:second_h])
        return second_h + 1

    def __circle_list(self):
        num = 0
        while True:
            num = self.__get_list(num)
            if num != len(self.s_file) + 1:
                continue
            else:
                break

    def __get_dict(self):
        self.__circle_list()
        for i in self.source_list:
            ki = i.split("\n")
            key = re.sub("\[#\]rule.*$", "", ki[0]).replace("#", "").strip()
            value = re.findall("=.*$", ki[1])
            if value:
                value = value[0].replace("=", "").strip()
            else:
                value = ""
            print(key, value)
            self.source_dict[key] = value

    def __start_js(self):
        for s_key in self.source_dict.keys():
            for line in self.target_list:
                if "//" in line:
                    if s_key == re.sub("\[#\]rule.*$", "", line).replace("//", "").strip():
                        index = self.target_list.index(line)
                        t_index = index + 1
                        print("before...", self.target_list[t_index])
                        source_str = self.target_list[t_index]
                        key_str = source_str[source_str.find('"') + 1:source_str.rfind('"')]
                        self.target_list[t_index] = source_str.replace(key_str, self.source_dict[s_key])
                        print("after...", self.target_list[t_index])

    def __get_new_file(self):
        new_file = codecs.open(self.new_target_file, 'w', 'utf-8')
        new_context = "".join(self.target_list)
        new_file.write(new_context)
        new_file.close()

    def start_trans(self):
        self.__get_dict()
        self.__start_js()
        self.__get_new_file()



if __name__ == '__main__':
    a = Transfer("D:/BOH2G/boh2g_doc/04 TestDoc/99 Sharing/SH/Versioncode/taojie/message4i18nCommon_en_US.properties", "D:/BOH2G/boh2g_doc/04 TestDoc/99 Sharing/SH/Versioncode/taojie/message4i18nInJs_en_US.js")
    a.start_trans()



