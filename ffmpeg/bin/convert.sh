#!/bin/bash

# -vf "transpose=1" clockwise 90 degrees
# -vpre ipod640

if [ $3 ]; 
then
   echo "With subtitles $3";
   ffmpeg -fix_sub_duration -i $1 -i $3 -vcodec copy -acodec copy -scodec mov_text -metadata:s:s:0 language=en $2

else 
   ffmpeg -i $1 -map 0:0 -map 0:1 -acodec libfaac -ac 2 -b:a 128k -ar 44100 -vcodec libx264 \
   -bufsize 10000k -b:v 1000k -bt 1000k -maxrate 1000k -threads 8 $2

fi
