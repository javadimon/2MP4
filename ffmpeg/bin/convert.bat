ffmpeg.exe -i %1 -vcodec libx264 -bufsize 10000k -b:v 1000k -bt 1000k -maxrate 1000k -map 0:0 -map 0:1 -threads 8 %2
rem -map 0:0 -map 0:1 -acodec libfaac -ac 2 -b:a 128k -ar 44100 666