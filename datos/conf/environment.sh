#!/usr/bin/env bash

#RELEASE_VERSION
search_dir="target/scala-2.11/"
jars_found=0

for entry in `ls ${search_dir}`; do
    version=`expr "$entry" | cut -d'-' -f 2`
    if [[ "$entry" == *\.jar && "$version" =~ [0-9\.]+ ]]; then
       DATOS_VERSION="$version"
       jars_found=$((jars_found+1))
       jars[jars_found]="$entry"
    fi
done

DATOS_VERSION=${DATOS_VERSION:?Error Required JAR File not found in target directory}
if [[ jars_found -gt 1 ]]; then
  echo "[ERROR] Multiple Versions of Datos Build found in ${search_dir} (( ${jars[*]} )) "
  echo "[INFO] Exiting Datos"
  exit
fi
echo "[INFO] Using Datos Version = ${DATOS_VERSION}"
ASSEMBLY_JAR_PATH="target/scala-2.11/datos-${DATOS_VERSION}-assembly.jar"
export ASSEMBLY_JAR_PATH

# JVM Configuration for better performance
JVM_GC_OPTS="-XX:SurvivorRatio=8 -XX:NewRatio=1 -XX:-UseAdaptiveSizePolicy -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=30 -XX:MetaspaceSize=512M"
JVM_MEM_OPTS="-Xmx1g -Xms1g"
JVM_OPTS="$JVM_GC_OPTS $JVM_MEM_OPTS"
