# -*- coding: utf-8 -*-
"""
Created on Sat Mar 30 20:34:09 2019
"""

import boto3
from boto3 import client

s3_resource = boto3.resource("s3", region_name="")
key=None
key2=[]
text2=''

def UploadDelete():
    conn = client('s3')
    for key in conn.list_objects(Bucket='')['Contents']:
        if(key['Key']=='b.txt'):
            continue

        conn.delete_object(Bucket='', Key= key['Key'])

def handler():
    global key2,key
    A = 0
    conn = client('s3')  # again assumes boto.cfg setup, assume AWS S3
    while(1):
        if(A==1):
            print("trigger")
            break
        for key in conn.list_objects(Bucket='')['Contents']:
            print(key['Key'])
            key2.append(key['Key'])
            if(key['Key']=='a.txt'):
                A = 1
                break
    return key2     

if __name__ == "__main__":
    while(True):
        key2.append(handler()) #file upload
        conn = client('s3')
        ## loop
        for key in conn.list_objects(Bucket='')['Contents']:
            if(key['Key']=='a.txt'):
                continue
            if(key['Key']=='comment.txt'):
                continue
            if(key['Key']=='Thumbs.db'):
                continue 
            if(key['Key']=='b.txt'):
                continue
            bucket=''
            photo = key['Key']
            client1=boto3.client('rekognition',region_name="")
            
            response = client1.detect_labels(
                Image={
                    'S3Object': {
                        'Bucket': bucket,
                        'Name': photo,
                    },
                },
                
            )
            #exif_dict = piexif.load(dick[ph])
           
            response2 = client1.detect_faces(Image={'S3Object': {'Bucket': bucket, 'Name': photo}},
            Attributes=['ALL'])
            bif = 0 #count the creat statement(detect the duplicate count)

            #make text file
            text=''
            text1=''
            Lcount = 0
            
            # detect the rest picture variable
            animal = 0
            First = 0 # first variable
            FLabel =''# first label name
            Fconf = 0 # first label percentage
            selftag=0  
            for label in response['Labels']:
                print(label['Name'])
                if((label['Name']!='Person')and label['Name']!='Human' and label['Name']!='Food'):
                    if(label['Name']!='Apparel' and label['Name']!='Face' and label['Name']!='Clothing'): # avoiding big topic(label)
                        if(Lcount!=4):
                            Lcount += 1
                            text=text+'/'+label['Name']
                First = First + 1
                if(First == 1):
                    FLabel = label['Name']
                    Fconf = label['Confidence']
                if(label['Name'] == 'Animal'):
                    animal = animal + 1
                    bif = 2
                    
            # face area
            face = 0 # face detecting variable 
            AP = 0 #Group face detecting variable    
            for faceDetail in response2['FaceDetails']:
                face = face + 1 # count face
                box = faceDetail['BoundingBox']
                left = box['Left']
                top = box['Top']
                width = box['Width']
                height = box['Height']
                xy = height*width # calcualte face area
                print ("    Top: " + str(top))
                print ("    Left: " + str(left))
                print ("    Width: " +  str(width))
                print ("    Height: " +  str(height))
                print ("    Area: " +  str(xy))
                if(face==1):
                    Fxy = xy
                    
                if(xy>=0.1 and (bif==0 or bif==3)): # self picture 
                    bif = 3
                    selftag= selftag + 1
                   
                if(xy >= Fxy*0.6): # Group picture
                    AP += 1
                    if(AP >=5):
                        bif=3
                        text1="group"+text
                                
                #############################################################################################
        # detecting objecting area
            print()
            Face_d = 0 # face count
            count = 0 # body count
            s=0
            s1=0
            label = 0 # label count
            for label in response['Labels']:
                     label = label + 1 ## label counting
                     
                if(label['Name']=='Face' and label['Confidence'] > 80):
                    Face_d = Face_d + 1
                    for instance in label['Instances']:
                        pass
                
                if(label['Name']=='Person' and label['Confidence'] > 90):    
                    for instance in label['Instances']:
                        y1 = instance['BoundingBox']['Top'] # top of the body
                        x1 = instance['BoundingBox']['Left'] # left side of the body
                        x2 = instance['BoundingBox']['Left'] + instance['BoundingBox']['Width'] #right side of the body
                        if(s1==0):
                            s = instance['BoundingBox']['Width'] * instance['BoundingBox']['Height']
                            s1 = s1 + 1
                        print(s)
                        count += 1
                        if( y1 >= 0.1 and y1 <= 0.5 and bif==0): # composition picture condition 
                            if( (x1 <= 0.4 and x1 >= 0.15) or (x2 <= 0.85 and x2 >= 0.4)):
                                if(face>3): # if counting face is more than 3
                                    bif = 1
                                    text1="line"+'/'+"group"+text
                                    
                                else:
                                    bif = 1
                                    text1="line"+text
                                    
                               
                        for parent in label['Parents']:
                            pass
                            
                            
                if((label['Name']=='Food' or label['Name']=='Drink' or label['Name']=='Meal') and label['Confidence'] > 90 and bif==0): # food condition
                    bif = 1
                    text1="food"+text
                    
        ###############################################################################################################
            # rest picture side
            if((bif == 0 or bif == 2)and((First+face) < 13 or FLabel == 'Text' or Fconf < 75 or animal >= 1) and s<=0.1 and label <= 7):
                bif = 1
                text1="rest"+text
                
            if(((count >= 9 and Face_d == 0)or(count==0 and Face_d==0))and bif==0): # landscape photography condition
                if(face >3): # count face
                    bif = 1
                    text1="land"+'/'+"group"+text
                    
                else:
                    bif = 1
                    text1="land"+'/'+text
                    
        #####################################
          
            if(selftag >= 1 and face >2 and(bif == 3 or bif==2)):
                text1="self"+'/'+"group"+text
                bif=1
                    
            if(selftag == 1 and (bif == 3 or bif==2)):
                text1="self"+text
                bif=1
          
            if(s>0.1 and face>2 and(bif ==0 or bif ==3 or bif ==2)): 
                text1="pbig"+'/'+"group"+text
                bif=1
            if(s>0.1 and (bif ==0 or bif ==3 or bif ==2)): 
                text1="pbig"+text
                bif=1
                
            
            if(AP>=5 and bif == 3):
                bif=1
            
            if(bif == 0):
                text1="land"+'/'+text
           
            print(Face_d)
            print(selftag)
            
            print('\n------------------------\n')
            print('user comment of meta data :\n')
            print(photo+"/"+text1)
            text2=text2+photo+"<"+text1+">"
        UploadDelete()        
        f = open('', 'w')
        f.write(text2)
        f.close()
        text2=''
        upload = s3_resource.Bucket('')
        upload.upload_file('', 'comment.txt')
        f = open('', 'w')
        f.write(text2)
        f.close()
