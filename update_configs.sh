#!/bin/sh

if [ ! -d "$1" ] ; then
	echo "Usage: update_configs.sh SOURCE_DIR"
	echo
	echo "\033[1m<SOURCE_DIR>\033[0m"
	echo "    A directory containing pb files, e.g."
	echo "    DUMP_DIR/product/etc/CarrierSettings"
	exit
fi

BASEDIR=$(dirname "$0")

rm -f $BASEDIR/configs/*.pb
cp $1/*.pb $BASEDIR/configs/
