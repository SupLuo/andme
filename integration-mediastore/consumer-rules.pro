
#图片选取/拍照/预览库
#PictureSelector
#https://github.com/LuckSiege/PictureSelector/blob/master/README_CN.md
-keep class com.luck.picture.lib.** { *; }

#Ucrop
-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }

#Okio
-dontwarn org.codehaus.mojo.animal_sniffer.*