#!/usr/bin/env python
# -*- coding: utf-8 -*-

try:
    import psyco
    psyco.full()
except:
    pass

try:
    from enhance.map.zh_wiki import zh2Hant, zh2Hans
except ImportError:
    from enhance.map.zh_wiki import zh2Hant, zh2Hans

import sys
py3k = sys.version_info >= (3, 0, 0)

from trans_word import *

if py3k:
    UEMPTY = ''
else:
    _zh2Hant, _zh2Hans = {}, {}
    for old, new in ((zh2Hant, _zh2Hant), (zh2Hans, _zh2Hans)):
        for k, v in old.items():
            new[k.decode('utf8')] = v.decode('utf8')
    zh2Hant = _zh2Hant
    zh2Hans = _zh2Hans
    UEMPTY = ''.decode('utf8')


MAPS = {}


class SourceMap(object):
    def __init__(self, name, mapping=None):
        self.name = name
        self._map = {}
        if mapping:
            self.set_convert_map(mapping)

    def set_convert_map(self, mapping):
        convert_map = {}
        have_child = {}
        max_key_length = 0
        for key in sorted(mapping.keys()):
            if len(key)>1:
                for i in range(1, len(key)):
                    parent_key = key[:i]
                    have_child[parent_key] = True
            have_child[key] = False
            max_key_length = max(max_key_length, len(key))
        for key in sorted(have_child.keys()):
            convert_map[key] = (key in mapping, have_child[key],
                    mapping.get(key, UEMPTY))
        self._map = convert_map
        self.max_key_length = max_key_length

    def __getitem__(self, k):
        try:
            is_tail, have_child, to_word  = self._map[k]
            return Word(k, to_word, is_tail, have_child)
        except:
            return Word(k)

    def __contains__(self, k):
        return k in self._map

    def __len__(self):
        return len(self._map)


def initTransMap(name, mapping):
    global MAPS
    MAPS[name] = SourceMap(name, mapping)




