#!/usr/bin/env python3
"""Generates all pixel-art textures of the mod (items, traps, armor layers, discs)."""
import os, colorsys
from PIL import Image, ImageDraw

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources", "assets", "tvtime", "textures")
ATT = "/mnt/user-data/attachments/53cf66a3-9928-514d-a8ef-9eabfd62e6fc/"

RED=(201,32,44,255); DRED=(140,16,30,255); WHITE=(245,245,245,255); YEL=(255,214,40,255); DYEL=(200,150,20,255)
BLK=(22,22,28,255); GREY=(125,125,135,255); DGREY=(70,70,80,255); LGREY=(170,170,180,255)
CY=(60,210,230,255); DCY=(30,140,170,255); CLEAR=(0,0,0,0)

def new(w,h): return Image.new("RGBA",(w,h),CLEAR)
def rect(im,x,y,w,h,c): ImageDraw.Draw(im).rectangle([x,y,x+w-1,y+h-1],fill=c)
def px(im,x,y,c): im.putpixel((x,y),c)
def save(im,*p):
    path=os.path.join(ROOT,*p); os.makedirs(os.path.dirname(path),exist_ok=True); im.save(path)

# ---------------- music discs: vanilla disc shapes recoloured to the Tenna palette ----------------
def disc(src): return Image.open(ATT+src).convert("RGBA").resize((16,16),Image.NEAREST)
def recolor(im, body, label):
    """greys (disc body) -> gradient of `body`, saturated pixels (label) -> gradient of `label`; (dark,light) RGB pairs."""
    out=im.copy()
    for x in range(16):
        for y in range(16):
            r,g,b,a=im.getpixel((x,y))
            if a==0: continue
            h,sat,v=colorsys.rgb_to_hsv(r/255,g/255,b/255)
            lo,hi=(label if sat>0.25 else body)
            t=min(1,v*1.15)
            out.putpixel((x,y),tuple(int(lo[i]+(hi[i]-lo[i])*t) for i in range(3))+(a,))
    return out
RED_B=((60,6,14),(205,40,52)); PUR_B=((30,10,50),(130,50,170)); GOLD_B=((70,48,6),(235,190,40)); TEAL_B=((6,40,50),(40,170,190))
YEL_L=((170,120,10),(255,230,70)); CYAN_L=((20,120,150),(150,250,255)); MAG_L=((140,20,110),(255,110,220)); WHT_L=((150,150,160),(255,255,255))
yellow=disc("0613ed3d-image.png"); cyan=disc("1799628a-image.png"); black=disc("515ecf98-image.png")
save(recolor(yellow,RED_B,CYAN_L),"item","music_disc_tv_time.png")
save(recolor(black,PUR_B,YEL_L),"item","music_disc_tenna_ex.png")
save(recolor(cyan,GOLD_B,MAG_L),"item","music_disc_end_you.png")
save(recolor(yellow,TEAL_B,WHT_L),"item","music_disc_kaizo.png")

# ---------------- items ----------------
# TV scrap: broken CRT chunk
im=new(16,16); rect(im,3,4,10,8,DGREY); rect(im,4,5,8,6,GREY); rect(im,5,6,6,4,CY); rect(im,5,6,3,1,WHITE)
px(im,12,4,CLEAR); px(im,3,11,CLEAR); px(im,11,5,CLEAR)
rect(im,7,2,1,2,LGREY); rect(im,9,1,1,3,LGREY); rect(im,6,12,4,1,YEL); px(im,2,6,YEL); px(im,13,9,RED)
save(im,"item","tv_scrap.png")

# TV remote
im=new(16,16); rect(im,5,1,6,14,DGREY); rect(im,6,2,4,12,BLK)
px(im,7,3,RED); px(im,8,3,RED); rect(im,6,5,2,2,CY); rect(im,8,5,2,2,YEL); rect(im,6,8,2,2,WHITE); rect(im,8,8,2,2,GREY); rect(im,6,11,4,2,GREY)
save(im,"item","tv_remote.png")

# ---------------- block textures (traps) ----------------
import random
random.seed(7)
im=Image.new("RGBA",(16,16),GREY)
for x in range(16):
    for y in range(16):
        n=random.random()
        c=GREY
        if n<.2: c=DGREY
        elif n<.28: c=(150,90,50,255)     # rust
        elif n>.9: c=LGREY
        im.putpixel((x,y),c)
for i in range(16): px(im,i,0,BLK); px(im,i,15,BLK); px(im,0,i,BLK); px(im,15,i,BLK)
save(im,"block","bear_trap.png")

im=Image.new("RGBA",(16,16),DGREY)
for x in range(16):
    for y in range(16):
        if (x%4==1 and y%4==1): im.putpixel((x,y),BLK)
        elif random.random()<.15: im.putpixel((x,y),GREY)
        elif random.random()<.05: im.putpixel((x,y),(150,90,50,255))
for i in range(16): px(im,i,0,BLK); px(im,i,15,BLK); px(im,0,i,BLK); px(im,15,i,BLK)
save(im,"block","spike_trap.png")

# ---------------- armor + icons cut from the SUPPLIED SKIN ----------------
SKIN=Image.open(ATT+"15ace13e-image.png").convert("RGBA")
def crop(x,y,w,h,src=None): return (src or SKIN).crop((x,y,x+w,y+h))
def paste(dst,im,x,y): dst.alpha_composite(im,(x,y))
def region(layer_dy, box):   # copy box of skin (x,y,w,h) shifted down by layer_dy (overlay layer = +16 rows, head hat = +32 cols)
    return crop(*box)
def build(parts, overlay=False):
    out=new(64,32)
    for (x,y,w,h) in parts:
        if not overlay: paste(out,crop(x,y,w,h),x,y)
        else:
            sx,sy=(x+32,y) if y<16 else (x,y+16)       # hat is right of head, jacket/sleeve/pants are 16 rows lower
            paste(out,crop(sx,sy,w,h),x,y)
    return out
HEAD=(0,0,32,16); BODY=(16,16,24,16); ARM=(40,16,16,16); LEGS=(0,16,16,16)
BOOT=(0,26,16,6); BODYLOW=(16,29,24,3)
save(build([HEAD,BODY,ARM,BOOT]),"entity","equipment","humanoid","tenna.png")
save(build([HEAD,BODY,ARM,BOOT],True),"entity","equipment","humanoid","tenna_overlay.png")
save(build([LEGS,BODYLOW]),"entity","equipment","humanoid_leggings","tenna.png")
save(build([LEGS,BODYLOW],True),"entity","equipment","humanoid_leggings","tenna_overlay.png")

def icon(draw):
    im=new(16,16); draw(im); save(im,"item",draw.__name__+".png")
def face(x,y,w,h,over=None):
    f=crop(x,y,w,h)
    if over: f.alpha_composite(crop(*over))
    return f
def tenna_helmet(im):
    f=face(8,8,8,8,(40,8,8,8)).resize((16,16),Image.NEAREST); paste(im,f,0,0)
def tenna_chestplate(im):
    paste(im,face(44,20,4,12,(44,36,4,12)),0,2); paste(im,face(44,20,4,12,(44,36,4,12)).transpose(Image.FLIP_LEFT_RIGHT),12,2)
    paste(im,face(20,20,8,12,(20,36,8,12)),4,2)
def tenna_leggings(im):
    paste(im,face(20,29,8,3,(20,45,8,3)),4,1)
    paste(im,face(4,20,4,12,(4,36,4,12)),4,4); paste(im,face(4,20,4,12,(4,36,4,12)),8,4)
def tenna_boots(im):
    b=face(4,26,4,6,(4,42,4,6)).resize((8,12),Image.NEAREST)
    paste(im,b,0,3); paste(im,b,8,3)
for fn in (tenna_helmet,tenna_chestplate,tenna_leggings,tenna_boots): icon(fn)
print("textures ok")
