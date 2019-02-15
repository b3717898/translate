#!/usr/bin/env python
# -*- coding: utf-8 -*-
# @Time    : 2019-02-15 13:53
# @Author  : yan_shizhi
# @Site    : 
# @File    : TransferFolder.py
# @Software: PyCharm

import re
import codecs
import os


class TransferFolder:
    def __init__(self, source_path, target_path):
        self.source_path = source_path
        self.target_path = target_path
        # self.s_file = codecs.open(self.source_file, 'r', 'utf-8').read()
        # self.t_file = codecs.open(self.target_file, 'r', 'utf-8')
        self.source_p_list = []
        self.source_j_list = []
        self.source_dict = {}
        # self.target_list = []
        # for i in self.t_file.readlines():
        #     self.target_list.append(i)

    def __get_p_list(self, start_num, s_file):
        """
        获取源文件list，将源文件中每两行当做list中一个元素
        :param start_num: 开始位置，每一次从上一次结束位置算下一次其实位置
        :return: 返回每一次查找的结束位置+1
        """
        first = s_file.find("#", start_num)
        if first == -1:
            return False
        first_h = s_file.find("\n", first)
        second_h = s_file.find("\n", first_h + 1)
        if second_h == -1:
            second_h = len(s_file)
        self.source_p_list.append(s_file[first:second_h])
        return second_h + 1

    def __get_j_list(self, start_num, s_file):
        """
        获取源文件list，将源文件中每两行当做list中一个元素
        :param start_num: 开始位置，每一次从上一次结束位置算下一次其实位置
        :return: 返回每一次查找的结束位置+1
        """
        first = s_file.find("//", start_num)
        if first == -1:
            return False
        first_h = s_file.find("\n", first)
        second_h = s_file.find("\n", first_h + 1)
        if second_h == -1:
            second_h = len(s_file)
        self.source_j_list.append(s_file[first:second_h])
        return second_h + 1

    def __circle_list(self, file):
        s_file = codecs.open(file, 'r', 'utf-8').read()
        if file.endswith("js"):
            num = 0
            while True:
                num = self.__get_j_list(num, s_file)
                if num:
                    if num != len(s_file) + 1:
                        continue
                    else:
                        break
                else:
                    break
        elif file.endswith("properties"):
            num = 0
            while True:
                num = self.__get_p_list(num, s_file)
                if num:
                    if num != len(s_file) + 1:
                        continue
                    else:
                        break
                else:
                    break

    def __get_dict(self):
        # self.__circle_list()
        for i in self.source_p_list:
            ki = i.split("\n")
            key = re.sub("\[#\]rule.*$", "", ki[0]).replace("#", "").strip()
            value = re.findall("=.*$", ki[1])
            if value:
                value = value[0].replace("=", "").strip()
            else:
                value = ""
            # print(key, value)
            self.source_dict[key] = value

        for i in self.source_j_list:
            ki = i.split("\n")
            key = re.sub("\[#\]rule.*$", "", ki[0]).replace("//", "").strip()
            value = re.findall(":\".*$", ki[1])
            if value:
                value = value[0].replace(":\"", "").replace("\",", "").replace("\"", "\\\"").strip()
            else:
                value = ""
            # print(key, value)
            self.source_dict[key] = value

    def __start_js(self, target_list):
        for s_key in self.source_dict.keys():
            for line in target_list:
                if "//" in line:
                    if s_key == re.sub("\[#\]rule.*$", "", line).replace("//", "").strip():
                        index = target_list.index(line)
                        t_index = index + 1
                        print("before...", target_list[t_index])
                        source_str = target_list[t_index]
                        key_str = source_str[source_str.find('"') + 1:source_str.rfind('"')]
                        target_list[t_index] = source_str.replace(key_str, self.source_dict[s_key])
                        print("after...", target_list[t_index])
        return target_list

    def __start_pro(self, target_list):
        for s_key in self.source_dict.keys():
            for line in target_list:
                if "#" in line:
                    if s_key == re.sub("\[#\]rule.*$", "", line).replace("#", "").strip():
                        index = target_list.index(line)
                        t_index = index + 1
                        print("before...", target_list[t_index])
                        source_str = target_list[t_index]
                        key_str = source_str[source_str.find('=') + 2:source_str.rfind('\n')]
                        target_list[t_index] = source_str.replace(key_str, self.source_dict[s_key])
                        print("after...", target_list[t_index])
        return target_list

    def __get_new_file(self, new_target_file, target_list):
        new_file = codecs.open(new_target_file, 'w', 'utf-8')
        new_context = "".join(target_list)
        new_file.write(new_context)
        new_file.close()

    def start_tf(self):
        for file in os.listdir(self.source_path):
            print("开始整理:{}".format(os.path.join(self.source_path, file)))
            self.__circle_list(os.path.join(self.source_path, file))
        self.__get_dict()

    def end_tf(self):
        for file in os.listdir(self.target_path):
            target_list = []
            target_file_name = file.split(".")[0]
            new_target_file = os.path.join(self.target_path,
                                           file.replace(target_file_name, target_file_name + "_en_US"))
            print("开始翻译:{}".format(os.path.join(self.target_path, file)))
            t_file = codecs.open(os.path.join(self.target_path, file), 'r', 'utf-8')
            for i in t_file.readlines():
                target_list.append(i)
            if file.endswith("js"):
                target_list = self.__start_js(target_list)
            elif file.endswith("properties"):
                target_list = self.__start_pro(target_list)
            self.__get_new_file(new_target_file, target_list)


if __name__ == '__main__':
    tf = TransferFolder("C:/Users/yan_shizhi/Desktop/fff", "C:/Users/yan_shizhi/Desktop/fff2")
    tf.start_tf()
    tf.end_tf()
