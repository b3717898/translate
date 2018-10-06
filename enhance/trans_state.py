#!/usr/bin/env python
# -*- coding: utf-8 -*-

from copy import deepcopy
import sys
py3k = sys.version_info >= (3, 0, 0)

if py3k:
    UEMPTY = ''
else:
    UEMPTY = ''.decode('utf8')

(START, PENDING, END, FAIL) = list(range(4))


class TransState(object):
    def __init__(self, state=START, prefix=UEMPTY):
        self.state = state
        self.len = 0
        self.final = UEMPTY
        self.prefix = prefix

    def find(self, char, map):
        this_word = map[char]
        whole_word = map[self.prefix + char]
        new = None
        if whole_word.have_child: # '了' has child
            if self.state == START: # the first one
                self.prefix = whole_word.from_word
                self.state = PENDING
                # self.len += 1
            elif self.state == PENDING: # wait for next word
                if this_word.have_child: # this word have child
                    new = TransState(PENDING,this_word.to_word)
                    new.final = self.prefix
                    self.len += 1
                    new.len = self.len
                    new.len += 1
                self.prefix = whole_word.from_word
        elif whole_word.is_tail: # '无生所涯' is a tail or the original word
            if self.state == START: # the first one
                if whole_word.is_original: # single word
                    self.state = FAIL
                else: # real tail
                    self.state = END
                    self.final = whole_word.to_word
                    self.len += 1
            elif self.state == PENDING: # '无生所涯' is a tail
                if whole_word.is_original and len(whole_word.from_word)>1:
                    self.state = FAIL
                else:
                    self.state = END
                    self.final += whole_word.to_word
                    self.len += 1
        else: # can not find
            self.state = FAIL

        #
        # if whole_word.have_child: # the whole has child
        #     new = TransState()
        #     new.state = PENDING
        #     new.prefix = whole_word.to_word
        #     new.len = self.len+1
        # elif whole_word.is_tail: # is the tail of some sentence
        #     new = TransState()
        #     new.state = END
        #     new.prefix = whole_word.to_word
        #     new.len = self.len+1
        # else: # last state is invalid, set it to fail
        #     new = deepcopy(self)
        #     new.state = END

        return new

    def append(self, char, map):
        this_word = map[char]
        self.final += this_word.to_word

    def __len__(self):
        return self.len + 1





