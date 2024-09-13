import cv2
from helpers import get_grass_color, get_players_boxes,get_kits_colors,get_kits_classifier,classify_kits,classify_person,get_left_team_label,get_GK2
import numpy as np

def annotate_video(video_path, model, processes, uuid):
    print("Start annotation from inside")
    cap = cv2.VideoCapture(video_path,cv2.CAP_FFMPEG)

    height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
    width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))

    video_name = video_path.split('/')[-1]

    #spécifier le codec à utiliser lors de l'écriture d'une vidéo avec OpenCV. 
    #Cette fonction prend quatre caractères ASCII comme argument, qui représentent le codec vidéo à utiliser.
    #
    fourcc = cv2.VideoWriter_fourcc(*'mp4v')
    file_out = 'Documents'+video_name.split('.')[0] + "_out.mp4"
    output_video = cv2.VideoWriter(file_out,
                                   fourcc,
                                   10.0,
                                   (width, height))
    kits_clf = None
    left_team_label = 0
    grass_hsv = None
    ball_positions = []
    

    box_colors = {
        "0": (150, 50, 50),
        "1": (37, 47, 150),
        "2": (41, 248, 165),
        "3": (166, 196, 10),
        "4": (155, 62, 157),
    }

    while cap.isOpened():
        # Read a frame from the video
        success, frame = cap.read()
        current_frame_idx = cap.get(cv2.CAP_PROP_POS_FRAMES) #indice de la frame
        previous_frame_idx = current_frame_idx -1
        
        if success:
            # Run YOLOv8 inference on the frame
            annotated_frame = cv2.resize(frame, (width, height))
            result = model(annotated_frame, conf=0.5, verbose=False)[0]

            # Get the players boxes and kit colors
            players_imgs, players_boxes = get_players_boxes(result)
            kits_colors = get_kits_colors(players_imgs, grass_hsv, annotated_frame)
            x=get_GK2(players_boxes)

            # Run on the first frame only
            if current_frame_idx == 1:
                kits_clf = get_kits_classifier(kits_colors)
                left_team_label = get_left_team_label(players_boxes, kits_colors, kits_clf)
                grass_color = get_grass_color(result.orig_img)
                grass_hsv = cv2.cvtColor(np.uint8([[list(grass_color)]]), cv2.COLOR_BGR2HSV)

            for box in result.boxes:
                labello = int(box.cls.cpu().numpy()[0])
                x1, y1, x2, y2 = map(int, box.xyxy[0].cpu().numpy())
                center_x = (x1 + x2) // 2
                center_y = (y1 + y2) // 2

                # Construct the text to display (coordinates)
                text = f'({center_x}, {center_y})'

                # Calculate the position to display text (to the right of the center)
                text_position_x = int(center_x) + 40
                text_position_y = int(center_y) + 30
                text_position = (x1 + 50, y1 + 40)  # Choisir la position du texte à l'intérieur du rectangle

                # If the box contains a player, find to which team he belongs
                kit_color = get_kits_colors([result.orig_img[y1: y2, x1: x2]], grass_hsv)
                team = classify_kits(kits_clf, kit_color)

                label=classify_person(team,x1,x)

                if labello ==0 :

                  if (label == "Player1") and (team == left_team_label):
                     cv2.rectangle(annotated_frame, (x1, y1), (x2, y2),box_colors["0"],  2)
                  elif (label == "Player2") or (label == "GK"):
                      cv2.rectangle(annotated_frame, (x1, y1), (x2, y2),box_colors["1"],  2)
                  # If the box contains a Goalkeeper, find to which team he belongs
                  else :
                      cv2.rectangle(annotated_frame, (x1, y1), (x2, y2),box_colors["3"],  2)
                else:
                     cv2.rectangle(annotated_frame, (x1, y1), (x2, y2),box_colors["4"],  2)

                cv2.putText(annotated_frame, text, (text_position_x, text_position_y), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 0), 2)
            # Write the annotated frame
            output_video.write(annotated_frame)


        else:
            # Break the loop if the end of the video is reached
            break

    cv2.destroyAllWindows()
    output_video.release()
    cap.release()
    processes[uuid] = file_out
    print("print processes just after annotation = ")
    print(processes)
    print("Annotation is done")
    return file_out

# Assurez-vous d'avoir toutes les fonctions nécessaires pour la détection d'objet (get_players_boxes, get_kits_colors,
# get_kits_classifier, get_left_team_label, get_grass_color, classify_kits) dans votre code.
       