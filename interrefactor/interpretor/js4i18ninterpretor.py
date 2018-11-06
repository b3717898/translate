#!/usr/bin/env python
# -*- coding: utf-8 -*-

import re
import sys
reload(sys)
sys.setdefaultencoding('utf8')
from config.config import *
from abstractinterpretor import *


class Js4i18nInterpretor(AbstractInterpretor):

    def _convertline(self, line, file_name):
        retvalue = line
        retvalue = unicode(retvalue)

        it_comment = re.finditer(self.comment_words, retvalue)
        comment_temp_str = ""
        for match in it_comment:
            comment_temp_str = match.group()
            retvalue = retvalue.replace(match.group(), "[$TEMP_COMMENT_STR$]")

        it = re.finditer(self.qtlt_with_squote_words, retvalue)
        enum_lines = []
        index = 1
        for match in it:
            retvalue_sub = match.group()
            it_sub = re.finditer(self.quot_lt_words, retvalue_sub)  # 0.96 '     <xxxxxx>xxx&quot;吃饭&x</ddd>' 中的&quot;吃饭&
            for match_sub in it_sub:
                # print (match.group())
                prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                # print match.group()
                if prop_name in self.prop_name:
                    prop_name = prop_name + "_" + str(self.globe_count)
                    self.globe_count += 1
                self.prop_name[prop_name] = "1"
                # CommonI18n.rb.getString("COMMON.SEARCH");

                match_str = match_sub.group()
                match_str = match_str[6:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "0.96", True)
                replace_str_sub = "&quot;' + {}.{} + '&".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1

            it_sub = re.finditer(self.gt_squote_words, retvalue_sub)
            for match_sub in it_sub:
                retvalue_sub_sub = match_sub.group()
                it_sub_sub = re.finditer(self.gtlt_words,
                                         retvalue_sub_sub)  # 0.979 '     <xxxxxx>吃饭'xxxx</ddd>' 中的>xx>吃饭<xxx' 中的>吃饭<
                for match_sub_sub in it_sub_sub:
                    # print (match.group())
                    prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                    # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                    # print match.group()
                    if prop_name in self.prop_name:
                        prop_name = prop_name + "_" + str(self.globe_count)
                        self.globe_count += 1
                    self.prop_name[prop_name] = "1"
                    # CommonI18n.rb.getString("COMMON.SEARCH");

                    match_str = match_sub_sub.group()
                    match_str = match_str[1:-1]
                    prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "0.979", True)
                    replace_str_sub_sub = ">' + {}.{} + '<".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                    retvalue_sub_sub = retvalue_sub_sub.replace(match_sub_sub.group(), replace_str_sub_sub)
                    enum_lines.extend(prop_lines)
                    index += 1
                it_sub_sub = re.finditer(self.squote_words,
                                         retvalue_sub_sub)  # 0.9795 '     <xxxxxx>吃饭'xxxx</ddd>' 中的>xx>xxx'吃饭'xxx<xxx' 中的>xxx'吃饭'xxx<
                for match_sub_sub in it_sub_sub:
                    # print (match.group())
                    prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                    # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                    # print match.group()
                    if prop_name in self.prop_name:
                        prop_name = prop_name + "_" + str(self.globe_count)
                        self.globe_count += 1
                    self.prop_name[prop_name] = "1"
                    # CommonI18n.rb.getString("COMMON.SEARCH");

                    match_str = match_sub_sub.group()
                    match_str = match_str[1:-1]
                    prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "0.9795", True)
                    replace_str_sub_sub = " {}.{} ".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                    retvalue_sub_sub = retvalue_sub_sub.replace(match_sub_sub.group(), replace_str_sub_sub)
                    enum_lines.extend(prop_lines)
                    index += 1
                it_sub_sub = re.finditer(self.chinese_words,
                                         retvalue_sub_sub)  # 0.9796 '     <xxxxxx>吃饭'xxxx</ddd>' 中的>xx>xxx"吃饭"xxx<xxx' 中的>xxx"吃饭"xxx<
                for match_sub_sub in it_sub_sub:
                    # print (match.group())
                    prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                    # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                    # print match.group()
                    if prop_name in self.prop_name:
                        prop_name = prop_name + "_" + str(self.globe_count)
                        self.globe_count += 1
                    self.prop_name[prop_name] = "1"
                    # CommonI18n.rb.getString("COMMON.SEARCH");

                    match_str = match_sub_sub.group()
                    match_str = match_str[1:-1]
                    prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "0.9796", True)
                    replace_str_sub_sub = " {}.{} ".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                    retvalue_sub_sub = retvalue_sub_sub.replace(match_sub_sub.group(), replace_str_sub_sub)
                    enum_lines.extend(prop_lines)
                    index += 1
                retvalue_sub = retvalue_sub.replace(match_sub.group(), retvalue_sub_sub)


            it_sub = re.finditer(self.gt_squote_words, retvalue_sub)  # 0.98 '     <xxxxxx>吃饭'xxxx</ddd>' 中的>吃饭'
            for match_sub in it_sub:
                # print (match.group())
                prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                # print match.group()
                if prop_name in self.prop_name:
                    prop_name = prop_name + "_" + str(self.globe_count)
                    self.globe_count += 1
                self.prop_name[prop_name] = "1"
                # CommonI18n.rb.getString("COMMON.SEARCH");

                match_str = match_sub.group()
                match_str = match_str[1:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "0.98", True)
                replace_str_sub = ">' + {}.{} ".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1
            it_sub = re.finditer(self.squote_lt_words, retvalue_sub)  # 0.99 '     <xxxxxx>'吃饭</ddd>' 中的'吃饭<
            for match_sub in it_sub:
                # print (match.group())
                prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                # print match.group()
                if prop_name in self.prop_name:
                    prop_name = prop_name + "_" + str(self.globe_count)
                    self.globe_count += 1
                self.prop_name[prop_name] = "1"
                # CommonI18n.rb.getString("COMMON.SEARCH");

                match_str = match_sub.group()
                match_str = match_str[1:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "0.99", True)
                replace_str_sub = " {}.{} + '<".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1

            it_sub = re.finditer(self.gtlt_words, retvalue_sub)  # 0.9998 '     <xxxxxx>xxx"吃饭"xxx</ddd>' 中的"吃饭"
            for match_sub in it_sub:
                retvalue_sub_sub = match_sub.group()
                it_sub_sub = re.finditer(self.chinese_words,
                                         retvalue_sub_sub)  # 0.9998 '     <xxxxxx>xxx"吃饭"xxx</ddd>' 中的"吃饭"
                for match_sub_sub in it_sub_sub:
                    # print (match.group())
                    prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                    # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                    # print match.group()
                    if prop_name in self.prop_name:
                        prop_name = prop_name + "_" + str(self.globe_count)
                        self.globe_count += 1
                    self.prop_name[prop_name] = "1"
                    # CommonI18n.rb.getString("COMMON.SEARCH");

                    match_str = match_sub_sub.group()
                    match_str = match_str[1:-1]
                    prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "0.9998", True)
                    replace_str_sub_sub = " {}.{} ".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                    retvalue_sub_sub = retvalue_sub_sub.replace(match_sub_sub.group(), replace_str_sub_sub)
                    enum_lines.extend(prop_lines)
                    index += 1
                it_sub_sub = re.finditer(self.squote_words,
                                         retvalue_sub_sub)  # 0.99981 '     <xxxxxx>xxx'吃饭'xxx</ddd>' 中的'吃饭'
                for match_sub_sub in it_sub_sub:
                    # print (match.group())
                    prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                    # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                    # print match.group()
                    if prop_name in self.prop_name:
                        prop_name = prop_name + "_" + str(self.globe_count)
                        self.globe_count += 1
                    self.prop_name[prop_name] = "1"
                    # CommonI18n.rb.getString("COMMON.SEARCH");

                    match_str = match_sub_sub.group()
                    match_str = match_str[1:-1]
                    prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "0.99981", True)
                    replace_str_sub_sub = " {}.{} ".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                    retvalue_sub_sub = retvalue_sub_sub.replace(match_sub_sub.group(), replace_str_sub_sub)
                    enum_lines.extend(prop_lines)
                    index += 1
                retvalue_sub = retvalue_sub.replace(match_sub.group(), retvalue_sub_sub)



            it_sub = re.finditer(self.gtlt_words, retvalue_sub) # 1 '     <xxxxxx>吃饭</ddd>' 中的>吃饭<
            for match_sub in it_sub:
                # print (match.group())
                prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                # print match.group()
                if prop_name in self.prop_name:
                    prop_name = prop_name + "_" + str(self.globe_count)
                    self.globe_count += 1
                self.prop_name[prop_name] = "1"
                # CommonI18n.rb.getString("COMMON.SEARCH");

                match_str = match_sub.group()
                match_str = match_str[1:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "1", True)
                replace_str_sub = ">' + {}.{} + '<".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1

            retvalue = retvalue.replace(match.group(), retvalue_sub)

        it = re.finditer(self.qtlt_with_quote_words,
                         retvalue)
        for match in it:
            retvalue_sub = match.group()
            it_sub = re.finditer(self.quote_lt_words, retvalue_sub)  # 2.05 " 工作站,排人优先级顺序有误<xxx 中的 " 工作站,排人优先级顺序有误<
            for match_sub in it_sub:
                # print (match.group())
                prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                # print match.group()
                if prop_name in self.prop_name:
                    prop_name = prop_name + "_" + str(self.globe_count)
                    self.globe_count += 1
                self.prop_name[prop_name] = "1"
                # CommonI18n.rb.getString("COMMON.SEARCH");

                match_str = match_sub.group()
                match_str = match_str[1:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "2.05", True)
                replace_str_sub = " {}.{} + \"<".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1

            it_sub = re.finditer(self.gtlt_words,
                                 retvalue_sub)  # "     <xxxxxx>吃饭"xxxx</ddd>" 中的>吃饭"  这是三级梦境
            for match_sub in it_sub:
                retvalue_sub_sub = match_sub.group()
                it_sub_sub = re.finditer(self.chinese_words,
                                         retvalue_sub_sub)  # 2.058 "     <xxxxxx>xxx”吃饭“xxx</ddd>" 中的"吃饭"
                for match_sub_sub in it_sub_sub:
                    # print (match.group())
                    prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                    # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                    # print match.group()
                    if prop_name in self.prop_name:
                        prop_name = prop_name + "_" + str(self.globe_count)
                        self.globe_count += 1
                    self.prop_name[prop_name] = "1"
                    # CommonI18n.rb.getString("COMMON.SEARCH");

                    match_str = match_sub_sub.group()
                    match_str = match_str[1:-1]
                    prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "2.058", True)
                    replace_str_sub_sub = " {}.{} ".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                    retvalue_sub_sub = retvalue_sub_sub.replace(match_sub_sub.group(), replace_str_sub_sub)
                    enum_lines.extend(prop_lines)
                    index += 1
                it_sub_sub = re.finditer(self.gt_quote_words, retvalue_sub_sub)  # 2.059 "     <xxxxxx>吃饭"xxxx</ddd>" 中的>吃饭"
                for match_sub_sub in it_sub_sub:
                    # print (match.group())
                    prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                    # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                    # print match.group()
                    if prop_name in self.prop_name:
                        prop_name = prop_name + "_" + str(self.globe_count)
                        self.globe_count += 1
                    self.prop_name[prop_name] = "1"
                    # CommonI18n.rb.getString("COMMON.SEARCH");

                    match_str = match_sub_sub.group()
                    match_str = match_str[1:-1]
                    prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "2.059", True)
                    replace_str_sub_sub = ">\" + {}.{} ".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                    retvalue_sub_sub = retvalue_sub_sub.replace(match_sub_sub.group(), replace_str_sub_sub)
                    enum_lines.extend(prop_lines)
                    index += 1
                retvalue_sub = retvalue_sub.replace(match_sub.group(), retvalue_sub_sub)

            it_sub = re.finditer(self.gtlt_words,
                                 retvalue_sub)  # 2.06 + "<a href='#' class='blue'  onclick=\"updateSpecialCashier('"+this.guid+"')\" >修改</a>&nbsp;&nbsp;"
            for match_sub in it_sub:
                # print (match.group())
                prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                # print match.group()
                if prop_name in self.prop_name:
                    prop_name = prop_name + "_" + str(self.globe_count)
                    self.globe_count += 1
                self.prop_name[prop_name] = "1"
                # CommonI18n.rb.getString("COMMON.SEARCH");

                match_str = match_sub.group()
                match_str = match_str[1:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "2.06", True)
                replace_str_sub = ">\" + {}.{} + \"<".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1

            it_sub = re.finditer(self.chinese_words, retvalue_sub)  # 2.069 "     <xxxxxx>" + "xxx'吃饭'xxx" + "</ddd>"	中的'吃饭'
            for match_sub in it_sub:
                retvalue_sub_sub = match_sub.group()
                it_sub_sub = re.finditer(self.squote_words,
                                         retvalue_sub_sub)  # 2.069 "     <xxxxxx>" + "xxx'吃饭'xxx" + "</ddd>"	中的'吃饭'
                for match_sub_sub in it_sub_sub:
                    # print (match.group())
                    prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                    # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                    # print match.group()
                    if prop_name in self.prop_name:
                        prop_name = prop_name + "_" + str(self.globe_count)
                        self.globe_count += 1
                    self.prop_name[prop_name] = "1"
                    # CommonI18n.rb.getString("COMMON.SEARCH");

                    match_str = match_sub_sub.group()
                    match_str = match_str[1:-1]
                    prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "2.069", True)
                    replace_str_sub_sub = "\" + {}.{} + \"".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                    retvalue_sub_sub = retvalue_sub_sub.replace(match_sub_sub.group(), replace_str_sub_sub)
                    enum_lines.extend(prop_lines)
                    index += 1
                retvalue_sub = retvalue_sub.replace(match_sub.group(), retvalue_sub_sub)

            it_sub = re.finditer(self.chinese_words, retvalue_sub)  # 2.07 "     <xxxxxx>" + "吃饭" + "</ddd>"	中的"吃饭“
            for match_sub in it_sub:
                # print (match.group())
                prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                # print match.group()
                if prop_name in self.prop_name:
                    prop_name = prop_name + "_" + str(self.globe_count)
                    self.globe_count += 1
                self.prop_name[prop_name] = "1"
                # CommonI18n.rb.getString("COMMON.SEARCH");

                match_str = match_sub.group()
                match_str = match_str[1:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "2.07", True)
                replace_str_sub = " {}.{} ".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1
            it_sub = re.finditer(self.gt_quote_words, retvalue_sub) #2.1 "     <xxxxxx>吃饭“xxxx</ddd>" 中的>吃饭“
            for match_sub in it_sub:
                # print (match.group())
                prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                # print match.group()
                if prop_name in self.prop_name:
                    prop_name = prop_name + "_" + str(self.globe_count)
                    self.globe_count += 1
                self.prop_name[prop_name] = "1"
                # CommonI18n.rb.getString("COMMON.SEARCH");

                match_str = match_sub.group()
                match_str = match_str[1:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "2.1", True)
                replace_str_sub = ">\" + {}.{} ".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1

            retvalue = retvalue.replace(match.group(), retvalue_sub)

        it = re.finditer(self.quote_lt_quote_words,     # "xxx<xxxxx吃饭xxxxx"
                         retvalue)
        for match in it:
            retvalue_sub = match.group()
            it_sub = re.finditer(self.gtlt_words, retvalue_sub)  # 2.5 "xxx<xxxxx>吃饭<xxxxx" 中的>吃饭<
            for match_sub in it_sub:
                # print (match.group())
                prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                # print match.group()
                if prop_name in self.prop_name:
                    prop_name = prop_name + "_" + str(self.globe_count)
                    self.globe_count += 1
                self.prop_name[prop_name] = "1"
                # CommonI18n.rb.getString("COMMON.SEARCH");

                match_str = match_sub.group()
                match_str = match_str[1:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "2.5", True)
                replace_str_sub = ">\" + {}.{} + \"<".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1
            retvalue = retvalue.replace(match.group(), retvalue_sub)
        # print retvalue
        it = re.finditer(self.squote_lt_squote_words,  # 'xxx<xxxxx吃饭xxxxx'
                         retvalue)
        for match in it:
            retvalue_sub = match.group()
            it_sub = re.finditer(self.gtlt_words, retvalue_sub)  # 2.6 'xxx<xxxxx>吃饭<xxxxx' 中的>吃饭<
            for match_sub in it_sub:
                # print (match.group())
                prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                # print match.group()
                if prop_name in self.prop_name:
                    prop_name = prop_name + "_" + str(self.globe_count)
                    self.globe_count += 1
                self.prop_name[prop_name] = "1"
                # CommonI18n.rb.getString("COMMON.SEARCH");

                match_str = match_sub.group()
                match_str = match_str[1:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "2.6", True)
                replace_str_sub = ">' + {}.{} + '<".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1
            retvalue = retvalue.replace(match.group(), retvalue_sub)

        retvalue = retvalue.replace("\\\'", "[$TEMP_SQUOTE_STR$]")
        it = re.finditer(self.squote_words, retvalue)  # for 'xxxxxx'
        for match in it:
            retvalue_sub = match.group()
            it_sub = re.finditer(self.gtlt_words, retvalue_sub)  # 2.99 'xxxxxxx>吃饭<xxxx' 中的吃饭
            for match_sub in it_sub:
                # print (match.group())
                prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                # print match.group()
                if prop_name in self.prop_name:
                    prop_name = prop_name + "_" + str(self.globe_count)
                    self.globe_count += 1
                self.prop_name[prop_name] = "1"
                # CommonI18n.rb.getString("COMMON.SEARCH");

                match_str = match_sub.group().replace("[$TEMP_QUOTE_STR$]", "\\\"")
                match_str = match_str.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
                match_str = match_str[1:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "2.99", True)
                replace_str_sub = ">' + {}.{} + '<".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1
            retvalue = retvalue.replace(match.group(), retvalue_sub)

        retvalue = retvalue.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
        # print retvalue
        it = re.finditer(self.slash_squote_words,
                         retvalue)  # 3.0 for cashDailyPosItemConfigTableDiv.push('\'项目\'');
        for match in it:
            # print (match.group())
            prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
            # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
            # print match.group()
            if prop_name in self.prop_name:
                prop_name = prop_name + "_" + str(self.globe_count)
                self.globe_count += 1
            self.prop_name[prop_name] = "1"
            # CommonI18n.rb.getString("COMMON.SEARCH");

            match_str = match.group().replace("[$TEMP_QUOTE_STR$]", "\\\"")
            match_str = match_str.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
            # print(match_str)
            match_str = match_str[2:-2]
            prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "3.0", True)
            replace_str = "\\\'' + {}.{} + '\\\'".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
            retvalue = retvalue.replace(match.group(), replace_str)
            enum_lines.extend(prop_lines)
            index += 1

        it = re.finditer(self.slash_quote_words,
                         retvalue)  # 3.02 for cashDailyPosItemConfigTableDiv.push("\"项目\"");
        for match in it:
            # print (match.group())
            prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
            # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
            # print match.group()
            if prop_name in self.prop_name:
                prop_name = prop_name + "_" + str(self.globe_count)
                self.globe_count += 1
            self.prop_name[prop_name] = "1"
            # CommonI18n.rb.getString("COMMON.SEARCH");

            match_str = match.group().replace("[$TEMP_QUOTE_STR$]", "\\\"")
            match_str = match_str.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
            # print(match_str)
            match_str = match_str[2:-2]
            prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "3.02", True)
            replace_str = "\\\"\" + {}.{} + \"\\\"".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
            retvalue = retvalue.replace(match.group(), replace_str)
            enum_lines.extend(prop_lines)
            index += 1

        retvalue = retvalue.replace("\\\"", "[$TEMP_QUOTE_STR$]")
        retvalue = retvalue.replace("\\\'", "[$TEMP_SQUOTE_STR$]")
        it = re.finditer(self.chinese_words, retvalue)  # for "xxxxxx"
        for match in it:
            retvalue_sub = match.group()
            it_sub = re.finditer(self.squote_words, retvalue_sub)  # 3.03 for abc = "xxxx'xxxx吃饭xxxx'xxx"
            for match_sub in it_sub:
                retvalue_sub_sub = match_sub.group()
                retvalue_sub_sub = retvalue_sub_sub.replace("[$TEMP_QUOTE_STR$]", "\\\"")
                it_sub_sub = re.finditer(self.slash_quote_words,
                                         retvalue_sub_sub)  # 3.03 "     <xxxxxx>" + 'xxx\"吃饭\"xxx' + "</ddd>"	中的\"吃饭\"
                for match_sub_sub in it_sub_sub:
                    # print (match.group())
                    prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                    # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                    # print match.group()
                    if prop_name in self.prop_name:
                        prop_name = prop_name + "_" + str(self.globe_count)
                        self.globe_count += 1
                    self.prop_name[prop_name] = "1"
                    # CommonI18n.rb.getString("COMMON.SEARCH");

                    match_str = match_sub_sub.group()
                    match_str = match_str[1:-1]
                    prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "3.03", True)
                    replace_str_sub_sub = "\\\"\" + {}.{} + \"\\\"".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                    retvalue_sub_sub = retvalue_sub_sub.replace(match_sub_sub.group(), replace_str_sub_sub)
                    enum_lines.extend(prop_lines)
                    index += 1
                retvalue_sub_sub = retvalue_sub_sub.replace("\\\"", "[$TEMP_QUOTE_STR$]")
                retvalue_sub = retvalue_sub.replace(match_sub.group(), retvalue_sub_sub)
            retvalue = retvalue.replace(match.group(), retvalue_sub)

        retvalue = retvalue.replace("[$TEMP_QUOTE_STR$]", "\\\"")
        retvalue = retvalue.replace("[$TEMP_SQUOTE_STR$]", "\\\'")

        retvalue = retvalue.replace("\\\"", "[$TEMP_QUOTE_STR$]")
        retvalue = retvalue.replace("\\\'", "[$TEMP_SQUOTE_STR$]")
        const_lines = []
        el_lines = []

        it = re.finditer(self.squote_words, retvalue)  # 'xxxxx'
        for match in it:
            retvalue_sub = match.group()
            it_sub = re.finditer(self.quot_lt_words, retvalue_sub)  # 3.1 for 'xx&quot;吃饭&x' 中的 &quot;吃饭&
            for match_sub in it_sub:
                # print (match.group())
                prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                # print match.group()
                if prop_name in self.prop_name:
                    prop_name = prop_name + "_" + str(self.globe_count)
                    self.globe_count += 1
                self.prop_name[prop_name] = "1"
                # CommonI18n.rb.getString("COMMON.SEARCH");

                match_str = match_sub.group().replace("[$TEMP_QUOTE_STR$]", "\\\"")
                match_str = match_str.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
                match_str = match_str[6:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "3.1", True)
                replace_str_sub = "&quot;' + {}.{} + '&".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1
            retvalue = retvalue.replace(match.group(), retvalue_sub)

        it = re.finditer(self.chinese_words, retvalue)
        for match in it:
            retvalue_sub = match.group()
            it_sub = re.finditer(self.quot_lt_words, retvalue_sub)  # 3.05 for "xx&quot;吃饭&x" 中的 &quot;吃饭&
            for match_sub in it_sub:
                # print (match.group())
                prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                # print match.group()
                if prop_name in self.prop_name:
                    prop_name = prop_name + "_" + str(self.globe_count)
                    self.globe_count += 1
                self.prop_name[prop_name] = "1"
                # CommonI18n.rb.getString("COMMON.SEARCH");

                match_str = match_sub.group().replace("[$TEMP_QUOTE_STR$]", "\\\"")
                match_str = match_str.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
                match_str = match_str[6:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "3.05", True)
                replace_str_sub = "&quot;\" + {}.{} + \"&".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1
            it_sub = re.finditer(self.gtlt_words, retvalue_sub) # 3.9 for abc = "xxxx>吃饭<xxxx"
            for match_sub in it_sub:
                # print (match.group())
                prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                # print match.group()
                if prop_name in self.prop_name:
                    prop_name = prop_name + "_" + str(self.globe_count)
                    self.globe_count += 1
                self.prop_name[prop_name] = "1"
                # CommonI18n.rb.getString("COMMON.SEARCH");

                match_str = match_sub.group().replace("[$TEMP_QUOTE_STR$]", "\\\"")
                match_str = match_str.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
                match_str = match_str[1:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "3.9", True)
                replace_str_sub = ">\" + {}.{} + \"<".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1
            it_sub = re.finditer(self.quote_lt_words, retvalue_sub) # 3.905 for abc = "xxxx吃饭<xxxx" 中的 "xxx吃饭<
            for match_sub in it_sub:
                # print (match.group())
                prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                # print match.group()
                if prop_name in self.prop_name:
                    prop_name = prop_name + "_" + str(self.globe_count)
                    self.globe_count += 1
                self.prop_name[prop_name] = "1"
                # CommonI18n.rb.getString("COMMON.SEARCH");

                match_str = match_sub.group().replace("[$TEMP_QUOTE_STR$]", "\\\"")
                match_str = match_str.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
                match_str = match_str[1:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "3.905", True)
                replace_str_sub = " {}.{} + \"<".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1
            it_sub = re.finditer(self.gt_quote_words, retvalue_sub)  # 3.906 for abc = "x>xxx吃饭" 中的 >xxx吃饭"
            for match_sub in it_sub:
                # print (match.group())
                prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                # print match.group()
                if prop_name in self.prop_name:
                    prop_name = prop_name + "_" + str(self.globe_count)
                    self.globe_count += 1
                self.prop_name[prop_name] = "1"
                # CommonI18n.rb.getString("COMMON.SEARCH");

                match_str = match_sub.group().replace("[$TEMP_QUOTE_STR$]", "\\\"")
                match_str = match_str.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
                match_str = match_str[1:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "3.906", True)
                replace_str_sub = ">\" + {}.{} ".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1
            it_sub = re.finditer(self.squote_words, retvalue_sub)     #3.91 for abc = "xxxx'吃饭'xxxx"
            for match_sub in it_sub:
                # print (match.group())
                prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                # print match.group()
                if prop_name in self.prop_name:
                    prop_name = prop_name + "_" + str(self.globe_count)
                    self.globe_count += 1
                self.prop_name[prop_name] = "1"
                # CommonI18n.rb.getString("COMMON.SEARCH");

                match_str = match_sub.group().replace("[$TEMP_QUOTE_STR$]", "\\\"")
                match_str = match_str.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
                match_str = match_str[1:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "3.91", True)
                replace_str_sub = "'\" + {}.{} + \"'".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1
            retvalue = retvalue.replace(match.group(), retvalue_sub)

        it = re.finditer(self.chinese_words, retvalue)  # 4 for abc = "吃饭吃饭"
        for match in it:
            # print (match.group())
            prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
            #file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
            # print match.group()
            if prop_name in self.prop_name:
                prop_name = prop_name + "_" + str(self.globe_count)
                self.globe_count += 1
            self.prop_name[prop_name] = "1"
            # CommonI18n.rb.getString("COMMON.SEARCH");


            match_str = match.group().replace("[$TEMP_QUOTE_STR$]", "\\\"")
            match_str = match_str.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
            match_str = match_str[1:-1]
            prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "4", False)
            replace_str = "{}.{}".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
            retvalue = retvalue.replace(match.group(), replace_str)
            enum_lines.extend(prop_lines)
            index += 1

        it = re.finditer(self.squote_words, retvalue)  # 'xxxxx'
        for match in it:
            retvalue_sub = match.group()
            it_sub = re.finditer(self.squote_lt_words, retvalue_sub)  # 4.8 for 'xx吃饭<x' 中的 ‘xx吃饭<
            for match_sub in it_sub:
                # print (match.group())
                prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                # print match.group()
                if prop_name in self.prop_name:
                    prop_name = prop_name + "_" + str(self.globe_count)
                    self.globe_count += 1
                self.prop_name[prop_name] = "1"
                # CommonI18n.rb.getString("COMMON.SEARCH");

                match_str = match_sub.group().replace("[$TEMP_QUOTE_STR$]", "\\\"")
                match_str = match_str.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
                match_str = match_str[1:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "4.8", True)
                replace_str_sub = " {}.{} + '<".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1
            it_sub = re.finditer(self.gtlt_words, retvalue_sub)  # 4.805 for 'xx>吃饭<x'
            for match_sub in it_sub:
                # print (match.group())
                prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                # print match.group()
                if prop_name in self.prop_name:
                    prop_name = prop_name + "_" + str(self.globe_count)
                    self.globe_count += 1
                self.prop_name[prop_name] = "1"
                # CommonI18n.rb.getString("COMMON.SEARCH");

                match_str = match_sub.group().replace("[$TEMP_QUOTE_STR$]", "\\\"")
                match_str = match_str.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
                match_str = match_str[1:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "4.805", True)
                replace_str_sub = ">' + {}.{} + '<".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1
            it_sub = re.finditer(self.gt_squote_words, retvalue_sub)    # 4.81 for 'x>吃饭' 中的 >吃饭'
            for match_sub in it_sub:
                # print (match.group())
                prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                # print match.group()
                if prop_name in self.prop_name:
                    prop_name = prop_name + "_" + str(self.globe_count)
                    self.globe_count += 1
                self.prop_name[prop_name] = "1"
                # CommonI18n.rb.getString("COMMON.SEARCH");

                match_str = match_sub.group().replace("[$TEMP_QUOTE_STR$]", "\\\"")
                match_str = match_str.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
                match_str = match_str[1:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "4.81", True)
                replace_str_sub = ">' + {}.{} ".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1
            retvalue = retvalue.replace(match.group(), retvalue_sub)



        it = re.finditer(self.squote_words, retvalue)  # 5 for 'xxxxxx'
        for match in it:
            # print (match.group())
            prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
            # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
            # print match.group()
            if prop_name in self.prop_name:
                prop_name = prop_name + "_" + str(self.globe_count)
                self.globe_count += 1
            self.prop_name[prop_name] = "1"
            # CommonI18n.rb.getString("COMMON.SEARCH");

            match_str = match.group().replace("[$TEMP_QUOTE_STR$]", "\\\"")
            match_str = match_str.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
            match_str = match_str[1:-1]
            prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "5", True)
            replace_str = "{}.{}".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
            retvalue = retvalue.replace(match.group(), replace_str)

            enum_lines.extend(prop_lines)
            index += 1

        retvalue = retvalue.replace("[$TEMP_QUOTE_STR$]", "\\\"")
        retvalue = retvalue.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
        retvalue = retvalue.replace("[$TEMP_COMMENT_STR$]", comment_temp_str)

        return retvalue, enum_lines, const_lines, el_lines

    def get_common_prop_lines(self, prop_name, match_str, rule, replace_quote_flag):
        retvalue=[]
        key, other_key, matched = self.findkey(prop_name, match_str, "J")
        if not matched: # key not matched
            if other_key is None:
                comment_str = "//{} [#]rule:%s \n" % rule
            else:
                comment_str = "//{} [#]rule:%s #matched in properties of %s\n" % (rule, other_key)
            retvalue.append(comment_str.format(match_str))
            if replace_quote_flag:
                retvalue.append("{}:\"{}\",\n".format(key, match_str.replace("\"", "\\\"")))
            else:
                retvalue.append("{}:\"{}\",\n".format(key, match_str))
        return retvalue, key