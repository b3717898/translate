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
import time


class TransferFolder:
    def __init__(self, source_path, target_path):
        self.source_path = source_path
        self.target_path = target_path
        # self.s_file = codecs.open(self.source_file, 'r', 'utf-8').read()
        # self.t_file = codecs.open(self.target_file, 'r', 'utf-8')
        self.source_p_list = []
        self.source_j_list = []
        self.source_dict = {}
        self.source_file_list = []
        self.target_file_list = []
        # self.target_list = []
        # for i in self.t_file.readlines():
        #     self.target_list.append(i)

    def __get_p_list(self, start_num, s_file):
        """
        获取源文件list，将源文件中每两行当做list中一个元素
        针对properties文件规则
        :param start_num: 开始位置，每一次从上一次结束位置算下一次其实位置
        :return: 返回每一次查找的结束位置+1
        """
        # 处理方法与.js文件相同，唯一不同的是注释符号
        first = s_file.find("#", start_num)
        if first == -1:
            return False
        first_h = s_file.find("\n", first)
        second_h = s_file.find("\n", first_h + 1)
        if second_h == -1:
            second_h = len(s_file)
        self.source_p_list.append(s_file[first:second_h])
        return second_h + 1

    def __get_filelist(self, dir, file_list):
        """
        遍历文件夹下所有文件方法
        :param dir: 传入的文件夹
        :param file_list: 一般为空list，用来存放子文件
        :return: 将文件列表作为list返回
        """
        new_dir = dir
        if os.path.isfile(dir):
            file_list.append(dir)
        elif os.path.isdir(dir):
            for s in os.listdir(dir):
                new_dir = os.path.join(dir, s)
                self.__get_filelist(new_dir, file_list)
        return file_list

    def __get_j_list(self, start_num, s_file):
        """
        获取源文件list，将源文件中每两行当做list中一个元素
        针对js文件规则
        :param start_num: 开始位置，每一次从上一次结束位置算下一次其实位置
        :return: 返回每一次查找的结束位置+1
        """
        # 首先从起始位置找第一个注释符
        first = s_file.find("//", start_num)
        # 如果没找到，则表示文件剩下内容中无符合要求内容，返回False退出循环
        if first == -1:
            return False
        # 在第一个注释符找一个换行符，确认第一行
        first_h = s_file.find("\n", first)
        # 在第一个换行符找第二个换行符，确认第二行
        second_h = s_file.find("\n", first_h + 1)
        # 如果第二个换行符找不到，则说明文件已经至末尾，用文件长度替代
        if second_h == -1:
            second_h = len(s_file)
        # 从第一个注释符位置，截取到第二个换行符位置，则将一个注释行并且它的下一行作为一个整体放入list中
        self.source_j_list.append(s_file[first:second_h])
        # 返回结束位置，作为下一次起始位置
        return second_h + 1

    def __circle_list(self, file):
        """
        把源文件内容，按两行一个元素，分割为一个list，因为第一行为jey，第二行为value
        :param file: 需要遍历的文件
        :return:
        """
        # 首先把文件内容读取出
        s_file = codecs.open(file, 'r', 'utf-8').read()
        # 按后缀不同处理
        if file.endswith("js"):
            num = 0
            # 死循环处理，根据文件位置判断是否结束循环
            while True:
                # 循环去读取文件，并返回每次读取完成后的位置
                num = self.__get_j_list(num, s_file)
                # 如果返回位置有值并且不为文件长度，则表示还未循环完成，继续
                if num:
                    if num != len(s_file) + 1:
                        continue
                    # 如果返回位置等于文件长度，结束循环
                    else:
                        break
                # 返回位置为空或False，则表示文件查找完成，结束循环
                else:
                    break
        # 处理方法一样
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
        """
        读取分割好的源文件list，提取其中的key以及value，组合成字典
        :return:
        """
        # self.__circle_list()
        # 读取源文件为.properties的文件list
        for i in self.source_p_list:
            # 两行为一个元素，所以首先按换行符分割
            ki = i.split("\n")
            # 在分割后的第一行，按规则找出注释
            key = re.sub("\[#\]rule.*$", "", ki[0]).replace("#", "").strip()
            # 在分割后的第二行，找出=号之后的内容
            value = re.findall("=.*$", ki[1])
            # 判断value是否有值
            if value:
                # 由于是用的findall方法，所以返回的是list，所以取第0元素再去掉=号
                value = value[0].replace("=", "").strip()
                # 如果value中还包括中文，则此行还为翻译完成，不能作为字典
                if not re.search('[\u4e00-\u9fa5]+', value):
                    # 符合条件的内容，存入字典
                    self.source_dict[key] = value
                else:
                    print("包含中文,不处理")
            else:
                # 内容为空不管
                pass
                # value = ""
            # print(key, value)
            # self.source_dict[key] = value

        for i in self.source_j_list:
            # .js文件处理方式与.properties一直，只不过注释判断规则和内容截取规则不同
            ki = i.split("\n")
            key = re.sub("\[#\]rule.*$", "", ki[0]).replace("//", "").strip()
            value = re.findall(":\".*$", ki[1])
            if value:
                value = value[0].replace(":\"", "").replace("\",", "").replace("\"", "\\\"").strip()
                if not re.search('[\u4e00-\u9fa5]+', value):
                    self.source_dict[key] = value
                else:
                    print("包含中文,不处理")
            else:
                pass
                # value = ""
            # print(key, value)
            # self.source_dict[key] = value

    def __start_js(self, target_list):
        """
        开始翻译js文件
        :param target_list: 目标文件内容list
        :return:
        """
        # 循环字典的key
        for s_key in self.source_dict.keys():
            # 循环文件内容list
            for line in target_list:
                # 判断是否有js注释符号
                if "//" in line:
                    # 如果字典的key和整理出的注释内容相同，则表示这一内容需要翻译
                    if s_key == re.sub("\[#\]rule.*$", "", line).replace("//", "").strip():
                        # 找出这一行在list中的位置
                        index = target_list.index(line)
                        # 需要替换的行为注释行的下一行
                        t_index = index + 1
                        print("before...", target_list[t_index])
                        # 获取待翻译行内容
                        source_str = target_list[t_index]
                        # 截取两个"号之间内容，也就是待翻译部分
                        key_str = source_str[source_str.find('"') + 1:source_str.rfind('"')]
                        # 将待翻译部分与字典中对应的值进行替换，完成翻译
                        target_list[t_index] = source_str.replace(key_str, self.source_dict[s_key])
                        print("after...", target_list[t_index])
        return target_list

    def __start_pro(self, target_list):
        """
        开始翻译properties文件
        :param target_list: 模板文件内容list
        :return:
        """
        # 循环字典的key
        for s_key in self.source_dict.keys():
            # 循环文件内容list
            for line in target_list:
                # 判断文件第一行是否有这句话，有的话判断注释，没有的话判断=号后面内容
                if target_list[0].strip() == "#This is an i18n propteries file":
                    # 判断注释的行首先判断是否有#号
                    if "#" in line:
                        # 如果字典的key和整理出的注释内容相同，则表示这一内容需要翻译
                        if s_key == re.sub("\[#\]rule.*$", "", line).replace("#", "").strip():
                            # 找出这一行在list中的位置
                            index = target_list.index(line)
                            # 需要替换的行为注释行的下一行
                            t_index = index + 1
                            print("before...", target_list[t_index])
                            # 获取待翻译行内容
                            source_str = target_list[t_index]
                            # 截取=号之后，换行符之前内容，也就是待翻译部分
                            key_str = source_str[source_str.find('=') + 2:source_str.rfind('\n')]
                            # 将待翻译部分与字典中对应的值进行替换，完成翻译
                            target_list[t_index] = source_str.replace(key_str, self.source_dict[s_key])
                            print("after...", target_list[t_index])
                else:
                    # 判断=号时候在这一行中
                    if "=" in line:
                        # 有些行中unicode编码有问题，所以try住但不处理
                        try:
                            # 判断字典中的key和=号后面内容从unicode转换成str后是否一样，一样则需要翻译
                            if s_key == line.split("=")[1].strip().encode('utf-8').decode('unicode_escape'):
                                # 找出这一行在list中的位置
                                index = target_list.index(line)
                                print("before...", target_list[index])
                                # 拿出这一行的内容
                                source_str = target_list[index]
                                # 截取=号之后，换行符之前内容，也就是待翻译部分
                                key_str = source_str[source_str.find('=') + 1:source_str.rfind('\n')]
                                # 将待翻译部分与字典中对应的值进行替换，完成翻译
                                target_list[index] = source_str.replace(key_str, self.source_dict[s_key])
                                print("after...", target_list[index])
                        except UnicodeDecodeError:
                            pass
        # 翻译完成后，将替换之后的list返回
        return target_list

    def __get_new_file(self, new_target_file, target_list):
        """
        保存为新文件方法
        :param new_target_file: 新的目标文件名
        :param target_list: 旧的目标文件内容list
        :return:
        """
        # 按utf-8格式打开新文件
        new_file = codecs.open(new_target_file, 'w', 'utf-8')
        # 把内容list整理为文本，写入
        new_context = "".join(target_list)
        new_file.write(new_context)
        new_file.close()

    def __get_files(self):
        """
        遍历目标文件夹和源文件夹，获取所有文件绝对路径
        :return:
        """
        self.source_file_list = self.__get_filelist(self.source_path, self.source_file_list)
        self.target_file_list = self.__get_filelist(self.target_path, self.target_file_list)

    def start_tf(self):
        """
        开始处理源文件
        :return:
        """
        # 获取所有文件，整理成list
        self.__get_files()
        # 首先循环源文件list
        for file in self.source_file_list:
            print("开始整理:{}".format(file))
            print(time.strftime('%Y-%m-%d %H:%M:%S', time.localtime()))
            # 把文件内容分割为list
            self.__circle_list(file)
        # 把整理好的文件内容list整理为字典
        self.__get_dict()

    def end_tf(self):
        """
        开始处理目标文件
        :return:
        """
        # 循环目标文件list
        for file in self.target_file_list:
            target_list = []
            # 定义翻译后文件名，将_zh_CN替换为_en_US，或直接添加_en_US
            target_file_name = os.path.basename(file).split(".")[0]
            if '_zh_CN' in target_file_name:
                new_target_file = file.replace(target_file_name, target_file_name.replace('_zh_CN', "_en_US"))
            else:
                new_target_file = file.replace(target_file_name, target_file_name + "_en_US")
            print("开始翻译:{}".format(file))
            print(time.strftime('%Y-%m-%d %H:%M:%S', time.localtime()))
            # 打开目标文件
            t_file = codecs.open(file, 'r', 'utf-8')
            # 把文件内容按行整理成list，每一元素对应文件中每一行
            for i in t_file.readlines():
                target_list.append(i)
            print("文件读取完成:", time.strftime('%Y-%m-%d %H:%M:%S', time.localtime()))
            # 根据后缀调用不同方法
            if os.path.basename(file).endswith("js"):
                target_list = self.__start_js(target_list)
            elif os.path.basename(file).endswith("properties"):
                target_list = self.__start_pro(target_list)
            # 生成新的目标文件
            self.__get_new_file(new_target_file, target_list)


if __name__ == '__main__':
    tf = TransferFolder("C:/Users/yan_shizhi/Desktop/fff", "C:/Users/yan_shizhi/Desktop/BOH_IMS_zh_CN/i18n")
    tf.start_tf()
    tf.end_tf()
