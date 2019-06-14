package ru.megadevelopers.nanogram

import groovy.json.JsonSlurper

def resource = getClass().getResource('/source_small.json')

def result = new JsonSlurper().parse(resource)
result.data_top.each { Collections.replaceAll(it, '', 0) }
result.data_left.each { Collections.replaceAll(it, '', 0) }

def nanogram = new Nanogram(left: result.data_left, top: result.data_top, height: result.height, width: result.width)
nanogram.init()
nanogram.print(false)

nanogram.solve()
nanogram.print(true)
