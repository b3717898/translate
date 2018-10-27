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

props = {}
js_props = {}
case_props = {}

class AbstractInterpretor(object):
    __metaclass__ = ABCMeta

    def __init__(self):


        self.chinese_words = re.compile(u"\"[^\"]*[\u4e00-\u9fa5]+[^\"]*\"")        # abc = "吃饭吃饭"
        self.chinese_without_gt_words = re.compile(u"\"[^\">]*[\u4e00-\u9fa5]+[^\">]*\"")  # abc = "吃饭吃饭"
        self.chinese_words_with_CASE = re.compile(u"case \"[^\"]*[\u4e00-\u9fa5]+[^\"]*\"")     # case "吃饭吃饭"
        self.comment_words = re.compile(u"//.*[\u4e00-\u9fa5]+.*")          # //吃饭吃饭
        self.gtlt_words = re.compile(u">[^><]*[\u4e00-\u9fa5]+[^<>]*<")      # >吃饭吃饭</
        self.squote_words = re.compile(u"'[^']*[\u4e00-\u9fa5]+[^']*'")     # abc = '吃饭吃饭'
        self.slash_squote_words = re.compile(u"\\\\'[^']*[\u4e00-\u9fa5]+[^']*\\\\'")  # abc = \'吃饭吃饭\'
        self.qtlt_with_squote_words = re.compile(u"'\s*<.*>[^<]*[\u4e00-\u9fa5]+[^<]*</")    # cashDailyPosItemConfigTableDiv.push('<th width="120">项目</th>');
        self.qtlt_with_quote_words = re.compile(u"\"\s*<.*>[^<]*[\u4e00-\u9fa5]+[^<]*</")    # + "<a href='#' class='blue'  onclick=\"updateSpecialCashier('"+this.guid+"')\" >修改</a>&nbsp;&nbsp;"
        self.gt_dollar_words = re.compile(u">[^<>}]*[\u4e00-\u9fa5]+[^$<>]*\\$\\{")    # <span class="span_left-rgm" ><h4>订单号:${result.storeOrderInfoBean.orderCode} &nbsp;&nbsp;订货日期： $/dfdfdfd>
        self.dollar_dollar_words = re.compile(u"}[^}>]*[\u4e00-\u9fa5]+[^$>]*\\$\\{")    # storeOrderInfoBean.orderCode} &nbsp;&nbsp;订货日期： ${/dfdfdfd>
        self.dollar_lt_words = re.compile(u"}[^}]*[\u4e00-\u9fa5]+[^<>}]*<")    # displayName }  已确认调入接收完成！ </span>

        self.squote_dollar_words = re.compile(u"'[^'{]*[\u4e00-\u9fa5]+[^$]*\\$\\{")    # 2. '吃饭${item.manhourTypeDesc}(非餐期)${dfdf}吃饭吃饭'
        self.dollar_squote_words = re.compile(u"}[^}]*[\u4e00-\u9fa5]+[^'}]*'")    # 2. '吃饭${item.manhourTypeDesc}(非餐期)${dfdf}吃饭吃饭'
        self.quote_dollar_words = re.compile(
            u"\"[^\"{]*[\u4e00-\u9fa5]+[^$]*\\$\\{")  # 2. "吃饭${item.manhourTypeDesc}(非餐期)${dfdf}吃饭吃饭"
        self.dollar_quote_words = re.compile(
            u"}[^}]*[\u4e00-\u9fa5]+[^\"}]*\"")  # 2. "吃饭${item.manhourTypeDesc}(非餐期)${dfdf}吃饭吃饭"
        self.slash_quote_words = re.compile(u"\\\\\"[^\"]*[\u4e00-\u9fa5]+[^\"]*\\\\\"")  # abc = \"吃饭吃饭\"
        self.tab_lt_words = re.compile(u"\\t[^\\t<\"{]*[\u4e00-\u9fa5]+[^<{]*<")  #      吃饭<
        self.gt_nl_words = re.compile(u">[^><$]*[\u4e00-\u9fa5]+[^<$]*\\r")  #      >吃饭\n

        self.tab_nl_words = re.compile(u"\\t[^\\t<;]*[\u4e00-\u9fa5]+[^<]*\\r")  # 吃饭\n
        self.gt_gt_words = re.compile(u">[^><]*[\u4e00-\u9fa5]+[^<>]*>")  # >  吃饭>
        self.tab_dollar_words = re.compile(u"\\t[^\\t{]*[\u4e00-\u9fa5]+[^$<]*\\$\\{")  #  吃饭${
        self.dollar_nl_words = re.compile(u"}[^}]*[\u4e00-\u9fa5]+[^}]*\\r")  # }吃饭\n
        self.comment_comment_words = re.compile(u"<!--.*[\u4e00-\u9fa5]+.*-->")  # <!--.吃饭.*-->

        self.gt_quote_words = re.compile(u">[^><]*[\u4e00-\u9fa5]+[^\"]*\"")  # >吃饭吃饭"
        self.gt_squote_words = re.compile(u">[^><]*[\u4e00-\u9fa5]+[^']*'")  # >吃饭吃饭'
        self.squote_lt_words = re.compile(u"'[^'><]*[\u4e00-\u9fa5]+[^'<>]*<")  # '全部骑手合计订单数：<span
        self.quote_lt_words = re.compile(u"\"[^\"><]*[\u4e00-\u9fa5]+[^\"<>]*<")  # "全部骑手合计订单数：<





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

    def findkey(self, key, value, flag):# key:properties 里面的key，或者js里面的key；flag：是java/jsp还是js的key;value:传入的显示内容
        global props
        global js_props
        global case_props
        retvalue = key
        other_key = None
        matched = False
        if flag == "P": # java & jsp properties
            if value in props:
                retvalue = props[value]
                matched = True
            else:
                props[value] = key
            if value in js_props:
                other_key = js_props[value]
        elif flag == "J": # js properties
            if value in js_props:
                retvalue = js_props[value]
                matched = True
            else:
                js_props[value] = key
            if value in props:
                other_key = props[value]
        elif flag == "C": # case props
            if value in case_props:
                retvalue = case_props[value]
                matched = True
            else:
                case_props[value] = key
        return retvalue, other_key, matched