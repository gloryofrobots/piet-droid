from PIL import Image
import os
path = "res"
sourcePath = "ic_launcher-web.png"
destName = "ic_launcher.png"

assets = [("drawable-mdpi",1), ("drawable-hdpi",1.5), ("drawable-xhdpi",2), ("drawable-xxhdpi",2.5),("drawable-ldpi",3)]
base = 48

sourceImage = Image.open(sourcePath)

for dirname,scale in assets:
    scaledSize = int(base * scale)
    scaledImage = sourceImage.resize((scaledSize,scaledSize), Image.ANTIALIAS)
    scaledPath = os.path.join(path,dirname,destName)
    scaledImage.save(scaledPath,"PNG",quality=100)



