#!/usr/bin/env python
# -*- coding: utf-8 -*-


import commands
import os

def local_com(com, path='/'):
    result = True
    output = None
    try:
        os.chdir(path)
        # print '开始本地执行命令:{}'.format(com)
        # print '执行路径为:{}'.format(path)
        (status, output) = commands.getstatusoutput(com)
        print '执行结果：{},{}'.format(status, output)
        if status != 0:
            raise Exception(output)
    except Exception:
        result = False
    finally:
        return result, output