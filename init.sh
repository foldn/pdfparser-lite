#!/bin/bash

export projectName=${1}
export gitRepository=${2}
if [ ! ${projectName} ];then
    echo "please input your project name"
else
    echo ${projectName} "init begin"
    echo "rename project folder to ${projectName}"
    cd ..
    mv templateproject ${projectName}
    cd ${projectName}
## change configuration *.xml (pom.xml, spring-*.xml)
    echo "init xml files"
    find . -name '*.xml' | xargs perl -pi -e 's|templateproject|'${projectName}'|g'

## change configuration *.properties
    echo "init properties files"
    find . -name '*.properties' | xargs perl -pi -e 's|templateproject|'${projectName}'|g'

## chang idea project info
    echo "init idea project info"
    find ./.idea/ -name ".name" | xargs perl -pi -e 's|templateproject|'${projectName}'|g'
    for files in `find . -name "*.iml"`
    do
#        echo ${files}
## change modules name
        mv ${files} ${files//templateproject/${projectName}}
    done

    ## remove template git info
    echo "remove templateproject git info"
    rm -rf .git

## change package name
    echo "change package name"
    for files in `find . -type d | grep  'templateproject$'`
    do
        echo "move " ${files} " to " ${files//templateproject/${projectName}}
        mv ${files} ${files//templateproject/${projectName}}
    done

## init git info
    if [ ${gitRepository} ]; then
        echo "init git info"
        git init
        git remote add origin ${gitRepository}

    fi

    echo ${projectName} "init completed"
fi
