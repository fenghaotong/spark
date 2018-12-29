# -*- coding: utf-8 -*-
import os
import sys

selfname = sys.argv[0].split('/')[-1]
filenames = os.listdir(os.getcwd())
title = os.getcwd().split('\\')[-1]
h1 = '# {0}'.format(title)
note = 'note@htfeng'
describe = '学习北风网spark从入门到精通[{0}](README.md)学习笔记'.format(title)
catalogname = '**目录**'

print("start produce catalog...")
with open("README.md", 'w', encoding = 'utf-8') as file:
    file.write(h1 + '\n' + note + '\n\n' + describe + '\n\n' + catalogname + '\n\n')
for filename in filenames:
    if filename != "README.md" and filename not in selfname \
            and filename != "img" and filename != 'src' and filename != 'data':
        with open("README.md", 'a', encoding = 'utf-8') as file:
            file.write('- [X] [{0}]({0}/README.md)\n'.format(filename))


print("end produce catalog!!")