#!/bin/sh

clean=false

while getopts 'c' opt; do
	case $opt in
		c) clean=true;;
		*) echo "Error unknown arg!" exit 1
	esac
done


echo configuring hive ...
if "$clean";
	then if [[ $(echo "show tables like 'redditposts'" | hive | grep 'reddit') ]];
		then echo "deleting redditposts table"
		hive -e "drop table redditposts"
		hive -f ../confg/hive.hql;
	else
		hive -f ../confg/hive.hql;
	fi
else
	hive -f ../confg/hive.hql;
fi 






